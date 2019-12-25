<?php


namespace ImWebSocket\Chat;


use Common\Mysql;
use Common\Redis;
use Tools\Sql;

class GroupInvite extends Message
{

    private const ACTION_TYPE = [
        'request' => 'request',
        'agree'   => 'agree',
    ];


    public function run()
    {
        $this->init();
        $actionType = @$this->msg->{MessageField::ACTION_TYPE};
        switch ($actionType) {
            case self::ACTION_TYPE['request']:
                $this->request();
                break;
            case self::ACTION_TYPE['agree']:
                $this->agree();
                break;
            default:
                break;
        }
    }

    /**
     * 处理请求
     */
    private function request()
    {

    }

    /**
     * 处理同意
     */
    private function agree()
    {
        $uid = $this->msg->{MessageField::TO};                // 发送者
        $gid = $this->msg->{MessageField::FROM};              // 群号

        // 反转
        $this->msg->{MessageField::TO}   = $gid;                // 群号
        $this->msg->{MessageField::FROM} = $uid;                // 发送者
        $this->data                      = json_encode($this->msg);

        // 1.  获取群聊 所有在线成员
        $users = $this->redis->sMembers(WsRedis::GROUP_PREFIX . $gid);

        // 2. 先同步 uid 到 redis 中 再广播
        $this->syncRedis($gid, $uid);

        // 广播给 所有 群成员
        foreach ($users as $user) {
            $fd = $this->getUserFd($user);
            $this->userOnline($fd, $user, $gid, $this->data);
        }

        // 3. 插入用户到 群聊表中
        $this->insertUserToGroup($gid, $uid);

        // 4. 更新群人数
        $this->updateGroupPersonNumber($gid);
    }

    /**
     * @param $uid
     * 获取用户 fd
     * @return string
     */
    private function getUserFd($uid)
    {

        return $this->redis->hGet(WsRedis::USER_PREFIX . $uid, WsRedis::SOCKET_FD);
    }

    /**
     * 建立群聊后 插入群聊成员 信息
     * @param $gid
     * @param $uid
     */
    private function insertUserToGroup($gid, $uid)
    {
        // 1. 拿到用户名
        $query = $this->sql
            ->setTable(WsMysql::USER_INFO_TABLE)
            ->select(
                ['uname']
            )
            ->whereAnd(
                [
                    'uid', '=', $uid,
                ]
            )
            ->getSql();

        $remark = $this->mysql->query($query)->fetch()['uname'];

        // 2. 获取当前同意时 mid 的最大值

        $mid = $this->getGroupMaxMid($gid);

        // 3. 插入 群聊 成员 信息
        $query = $this->sql
            ->setTable(WsMysql::USER_GROUP_TABLE)
            ->insert(
                [
                    'gid'       => $gid,
                    'uid'       => $uid,
                    'joinTime'  => time(),
                    'remark'    => $remark,
                    'lastMsgId' => $mid,    // 用户不能拿到 入群之间的群消息
                ]
            )
            ->getSql();

        $this->mysql->exec($query);
    }

    /**
     * @param $gid
     * @param $uid
     * 把当前用户插入到 group:$gid 的集合中
     */
    private function syncRedis($gid, $uid)
    {

        /**
         * 同意入群
         * 1. 将群号添加到 user:$uid 的 hash 键中
         * 2. 将 uid 添加到 group:$gid 的 set 中
         */
        $this->redis->hSet(WsRedis::USER_PREFIX . $uid, $gid, 0);
        $this->redis->sAdd(WsRedis::GROUP_PREFIX . $gid, $uid);
    }

    /**
     * 获取群聊的 最大的 mid
     * @param $gid
     * @return int
     */
    private function getGroupMaxMid($gid)
    {
        $query = 'select max(`mid`) from `' . WsMysql::GROUP_TABLE_PREFIX . $gid . '`';

        $result = (int)($this->mysql->query($query)->fetch(\PDO::FETCH_NUM)[0]);

        return $result;
    }

    /**
     * @param $gid
     * 更新群人数
     * 使用 update 利用 mysql 锁 防止 多进程 导致数据异常
     * * 不能使用 先取出 加 1 然后再写入
     */
    private function updateGroupPersonNumber(int $gid)
    {
        $query = 'update `' . WsMysql::GROUP_INFO_TABLE . '` set `number` = `number` + 1 where `gid` = ' . $gid;

        $this->mysql->exec($query);
    }
}