<?php


namespace ImWebSocket\Chat;


use Common\Mysql;
use Common\Redis;
use Tools\Sql;

class GroupReq extends Message
{
    private const FIELD = 'actionType';
    /**
     * 请求 子类型
     */
    private const ACTION_TYPE = [
        'req'   => 'request',
        'agree' => 'agree',
        'refuse',
    ];

    public function run()
    {
        // 获取加群 申请 同意 类型
        $action_type = @$this->msg->{self::FIELD};
        switch ($action_type) {
            case self::ACTION_TYPE['req']:
                $this->group_req();
                break;
            case self::ACTION_TYPE['agree']:
                $this->group_agree();
                break;
            default:
                break;
        }
    }

    /**
     * 加群申请
     */
    private function group_req()
    {
        $this->init();
        // 获取群主 id
        $gid     = @$this->msg->{MessageField::TO};
        $aim_uid = $this->getGroupOwner($gid);

        if (false === $aim_uid) {       // 不存在指定的群 ( 如果保证客户端正确，此处永远为 假 )

            return;
        } else {
            $aim_fd = $this->getUserFd($aim_uid);
            false !== $aim_fd ? $this->aimOnline($aim_fd, $aim_uid) : $this->aimOffline($aim_uid);
        }
    }

    /**
     * 加群通过
     */
    private function group_agree()
    {
        $this->init();

        // 找到申请者 推送消息
        $to_uid   = @$this->msg->{MessageField::FROM};
        $from_gid = @$this->msg->{MessageField::TO};

        // 反转 uid 与 gid
        $this->msg->{MessageField::TO}   = $to_uid;
        $this->msg->{MessageField::FROM} = $from_gid;
        $this->data = json_encode($this->msg);

        $to_fd = $this->getUserFd($to_uid);

        false !== $to_fd ? $this->aimOnline($to_fd, $to_uid, true) : $this->aimOffline($to_uid);

        // 更新群信息 添加 gid <=> uid
        $this->group_mysql($from_gid, $to_uid);
    }

    /**
     * @param $gid
     * 获取 群主  uid
     * @return bool
     */
    private function getGroupOwner($gid)
    {

        $query = $this->sql
            ->setTable(WsMysql::GROUP_INFO_TABLE)
            ->select(
                [
                    'owner',
                ]
            )
            ->whereAnd(
                [
                    'gid', '=', $gid,
                ]
            )
            ->getSql();
        $result = $this->mysql->query($query)->fetch();

        // 抑制错误
        if (empty($result)) return false;
        else return $result['owner'];
    }

    /**
     * @param $aim_uid
     * @return string
     */
    private function getUserFd($aim_uid)
    {
        $this->redis = (new Redis())->getInstance();
        $aim_fd      = $this->redis->hGet(WsRedis::USER_PREFIX . $aim_uid, WsRedis::SOCKET_FD);

        return $aim_fd;
    }

    /**
     * @param int $aim_fd
     * 在线推送
     * @param int $aim_uid
     * @param bool $isAgree
     */
    private function aimOnline(int $aim_fd, int $aim_uid, bool $isAgree = false)
    {
        // 处理意外 则离线
        if (!$this->ws->push($aim_fd, $this->data)) {
            // 触发 close
            $this->ws->close($aim_fd);
            $this->aimOffline($aim_uid);
        }

        // 加群请求同意操作
        if ($isAgree) {
            // 拿到 群号
            $group = $this->msg->{MessageField::FROM};
            $redis = (new Redis())->getInstance();
            // 1. 修改 redis 成员 set
            $redis->sAddArray(WsRedis::GROUP_PREFIX . $group, [$aim_uid]);
            // 2. 增加 uid 的 hash 键   // 将 值 设置为 0 (没有影响)
            $redis->hSet(WsRedis::USER_PREFIX . $aim_uid, $group, 0);
        }
    }

    /**
     * @param $aim_uid
     * 离线保存
     */
    private function aimOffline($aim_uid)
    {
        // 加群请求 群主 或者申请者 不在线，则插入 私聊信息表中
        $query = $this->sql
            ->setTable(WsMysql::PRI_MSG)
            ->insert(
                [
                    $aim_uid,
                    $this->msg->{MessageField::FROM},
                    $this->data,
                ]
            )
            ->getSql();
        $this->mysql->exec($query);
    }

    /**
     * 加群成功后 将 uid 与 gid 绑定 插入数据库
     * 更新 group_info 群人数信息
     * @param $gid int 群号 id
     * @param $uid int 用户 id
     */
    private function group_mysql(int $gid, int $uid)
    {
        // 1. 拿到 uid 用户名
        $query  = $this->sql
            ->setTable(WsMysql::USER_INFO_TABLE)
            ->select(['uname'])
            ->whereAnd(
                ['uid', '=', $uid,]
            )
            ->getSql();

        $result = $this->mysql->query($query)->fetch();

        if (empty($result)) {               // 此处 理应 为假

            return;
        }
        $remark = $result['uname'];

        // 拿到 群聊的 最大消息 id 作为当前用户 加入时 初始 last_msg_id
        $query = 'select max(`mid`) from `' . WsMysql::GROUP_TABLE_PREFIX . $gid . '`';

        // 当群聊刚建立时，group_$gid 为空表 max(`mid`) 为 null 特殊处理一下
        $mid = (int)($this->mysql->query($query)->fetch(\PDO::FETCH_NUM)[0]);

        $query = $this->sql
            ->setTable(WsMysql::USER_GROUP_TABLE)
            ->insert(
                [
                    'gid'         => $gid,
                    'uid'         => $uid,
                    'join_time'   => 'unix_timestamp(now())',
                    'remark'      => $remark,
                    'last_msg_id' => $mid,
                ]
            )
            ->getSql();

        $this->mysql->exec($query);

        // 2. 更新 group_info 群人数信息
        // 先取回 person_number 加 1 后再存回 会出现覆盖
        // 使用 sql 中 update 保证多进程更新不会重复覆盖

        $query = 'update `' . WsMysql::GROUP_INFO_TABLE . '` set `person_number` = `person_number` + 1 where `gid` = ' . $gid;

        $this->mysql->exec($query);
    }
}