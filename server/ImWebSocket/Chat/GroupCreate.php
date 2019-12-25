<?php


namespace ImWebSocket\Chat;


use ImWebSocket\User\UserInfo;
use Log\WsLog;

class GroupCreate extends Message
{

    private const ACTION_TYPE = 'actionType';

    const CREATE_TABLE_SQL = <<<EOF
create table `group_gid` (
`mid` int primary key auto_increment COMMENT '消息 id, 自增',
`msgFrom` int not null COMMENT '发送者 id',
`msg` text not null COMMENT '群聊消息'
) COMMENT = '群聊离线消息列表';
EOF;

    /**
     * @var  int 群号
     */
    private $gid;

    public function run()
    {
        try {
            $this->init();
            $uid   = $this->msg->{MessageField::FROM};
            $gname = $this->msg->{'gname'};
            $gid   = $this->createGroup($uid, $gname);

            $msg = json_encode(
                [
                    MessageField::TYPE => MessageType::GROUP_CREATE,
                    'status'           => 'ok',
                    'gid'              => $gid,
                ]
            );

            // 回应群主消息
            $this->sendOwner($gid, $msg);

            // 插入群主信息
            $this->bindUserGroup($gid, $uid);

            // 建立 群聊消息表
            $this->createGroupTable($gid);

            // 邀请 好友
            $this->inviteFriend($gid);

        } catch (\Exception $e) {
            WsLog::log($e->getTraceAsString());
        }
    }


    /**
     * 创建群聊
     * @param $uid
     * @param $gname
     * @return mixed
     */
    private function createGroup($uid, $gname)
    {
        $query = $this->sql
            ->setTable('group_info')
            ->insert(
                [
                    'gid'          => null,                 // 群 id 自增
                    'owner'        => $uid,                 // 群主
                    'gname'        => $gname,               // 群名称
                    'createTime'   => time(),               // 创建时间
                    'number' => 1,                    // 群成员
                ]
            )
            ->getSql();

        $this->mysql->exec($query);

        // 拿回 gid
        $query     = $this->sql
            ->setTable(WsMysql::GROUP_INFO_TABLE)
            ->select(
                [
                    'gid',
                ]
            )
            ->whereAnd(
                [
                    'owner', '=', $uid,
                ]
            )->getSql();
        $result    = $this->mysql->query($query)->fetchAll();

        /**
         * 同一用户 不可能同时大量创建 group 取回最大值即可
         */
        $this->gid = max(array_column($result, 'gid'));

        return $this->gid;
    }

    /**
     * 邀请好友
     * @param $gid
     * @throws \Exception
     */
    private function inviteFriend($gid)
    {
        $friends = @$this->msg->{'person'};
        if (empty($friends)) {
            throw new \Exception('create group but friend is empty');
        }

        $getFriendFd = function ($uid) {
            $fd = $this->redis->hGet(WsRedis::USER_PREFIX . $uid, WsRedis::SOCKET_FD);
            return $fd;
        };

        foreach ($friends as $friend) {
            $msg = json_encode(
                [
                    MessageField::TYPE => MessageType::GROUP_INVITE,
                    self::ACTION_TYPE  => 'request',
                    'msgFrom'          => $gid,
                    'msgTo'            => $friend,
                ]
            );
            $fd  = $getFriendFd($friend);

            if (false !== $fd) {
                $this->online($fd, $friend, $msg);
            } else {
                $this->offline($friend, $msg);
            }
        }
    }

    /**
     * @param $fd
     * 在线推送
     * @param $uid
     * @param $msg
     */
    private function online($fd, $uid, $msg)
    {
        $ok = $this->ws->push($fd, $msg);

        if (false === $ok) {
            $this->offline($uid, $msg);
        }
    }

    /**
     * @param $uid
     * 离线推送
     * @param $msg
     */
    private function offline($uid, $msg)
    {
        $query = $this->sql
            ->setTable(WsMysql::PRI_MSG)
            ->insert(
                [
                    'msgTo'   => $uid,
                    'msgFrom' => $this->gid,
                    'msg'     => $msg,
                ]
            )
            ->getSql();
        $this->mysql->exec($query);
    }

    /**
     * @param $gid
     * @param $msg
     * 回送创建者消息
     */
    private function sendOwner($gid, $msg)
    {
        // 发送成功 则把群聊 同步到当前用户 redis 中
        $fd  = $this->frame->fd;
        $uid = $this->msg->{MessageField::FROM};
        /**
         * 推送成功，说明群主此时在线，为群主的 user:$uid  添加 hash 键 gid <=> 0
         * 为 group:$gid 添加 群主 uid
         */

        if ($this->ws->push($fd, $msg)) {

            $this->redis->hSet(WsRedis::USER_PREFIX . $uid, $gid, 0);
            $this->redis->sAdd(WsRedis::GROUP_PREFIX . $gid, $uid);
        } else {
            // 失败 则存到离线表里
            $this->offline($uid, $msg);
        }
    }

    /**
     * 创建 群成员信息表
     * @param $gid
     */
    private function createGroupTable($gid)
    {
        $sql = str_replace('gid', $gid, self::CREATE_TABLE_SQL);

        $this->mysql->exec($sql);
    }

    use UserInfo;
}