<?php


namespace ImWebSocket\Chat;


use Common\Mysql;
use Common\Redis;
use Tools\Sql;

class GroupChat extends Message
{

    public function run()
    {
        $this->group_push();
    }

    /**
     * 群聊推送给所有在线 用户
     */
    private function group_push()
    {
        $redis = (new Redis())->getInstance();

        $msg    = json_decode($this->data);

        // 群号
        $gid = $msg->{MessageField::TO};
        // 拿到所有 群聊在线用户 ( 不可能为空 如果一个 set 为空 redis 中不会存在这个键 )
        $users = $redis->sMembers(WsRedis::GROUP_PREFIX . $gid);

        // 在线推送给 群聊所有在线成员
        foreach ($users as $uid) {
            /**
             * $uid 只是用户 id
             * 通过 hash 拿到 id 对应的 fd
             */
            $fd = $redis->hGet(WsRedis::USER_PREFIX . $uid, WsRedis::SOCKET_FD);

            // 意外掉线 则会推送失败
            if (!$this->ws->push($fd, $this->data)) {

                // 主动关闭 触发 close 事件  close 中会处理 redis 与 mysql
                $this->ws->close($fd);
            }
        }

        // 后台保存到数据库

        $this->msg_mysql();
    }

    /**
     * 群聊消息 存入数据库
     */
    private function msg_mysql()
    {

        // 拿到群号 与 发送者 id
        $msg  = json_decode($this->data);
        $from = @$msg->{MessageField::FROM};            // 发送者 uid
        $gid  = @$msg->{MessageField::TO};              // 群聊 gid

        // 准备语句 插入数据库
        $mysql = (new Mysql())->getInstance();
        $sql   = (new Sql())
            ->setTable(WsMysql::GROUP_TABLE_PREFIX . $gid)       // 群聊表
            ->insert(
                [
                    'mid'  => null,                 // 消息 id 自增
                    'from_uid' => $from,            // 发送者
                    'msg'  => $this->data,          // 消息体
                ]
            )
            ->getSql();
        $mysql->exec($sql);
    }
}