<?php


namespace ImWebSocket\Chat;


use Common\Mysql;
use Common\Redis;
use Tools\Sql;

class GroupChat extends Chat
{
    /**
     * mysql 群聊信息表前缀
     */
    private const GROUP_PREFIX = 'group_';

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

        // 获取群成员
        $msg    = json_decode($this->data);
        $groups = $redis->sMembers($msg->{MessageField::TO}); // 不可能为空

        // 在线推送
        foreach ($groups as $v) {
            /**
             * $v 只是用户 id
             * 通过 hash 拿到 id 对应的 fd
             */
            $fd = $redis->hGet(WsRedis::USER_PREFIX, WsRedis::SOCKET_FD);
            $this->ws->push($fd, $this->data);
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
        $from = @$msg->{MessageField::FROM};
        $gid  = @$msg->{MessageField::TO};

//        // 出错终止
//        if (null === $gid) {
//
//            return;
//        }

        // 准备语句 插入数据库
        $mysql = (new Mysql())->getInstance();
        $sql   = (new Sql())
            ->setTable(self::GROUP_PREFIX)
            ->insert(
                [
                    'mid'  => null,             // 消息 id 自增
                    'from' => $from,            // 发送者
                    'msg'  => $this->data,      // 消息体
                ]
            )
            ->getSql();
        $mysql->query($sql);
    }
}