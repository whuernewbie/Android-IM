<?php


namespace ImWebSocket\Chat;

use Common\Mysql;
use Common\Redis;
use Tools\Sql;

class SingleChat extends Message
{
    /**
     * 私聊处理
     */
    public function run()
    {
        $msg = json_decode($this->data);

        // 获取目标用户 检测在线状态
        $to     = @$msg->{MessageField::TO};
        $redis  = (new Redis())->getInstance();
        $aim_fd = $redis->hGet(WsRedis::USER_PREFIX . $to, WsRedis::SOCKET_FD);

        // 存在则会返回 用户对应的 fd 否者返回 false
        $aim_fd ? $this->aimOnline($aim_fd) : $this->aimOffline();
    }

    /**
     * 目标 在线处理
     * @param int $aim_fd 目标用户 连接符
     */
    private function aimOnline(int $aim_fd)
    {
        // 推送失败 进行离线处理 可能因为 用户意外掉线
        if (!$this->ws->push($aim_fd, $this->data)) {
            $this->aimOffline();
        }
    }

    /**
     * 目标离线处理
     * 消息存入数据库
     */
    private function aimOffline()
    {
        $msg = json_decode($this->data);

        // 发送者 与 接收者
        $from = $msg->{MessageField::FROM};
        $to   = $msg->{MessageField::TO};

        // 离线消息储存
        $mysql = (new Mysql())->getInstance();
        $sql   = (new Sql())
            ->setTable(WsMysql::PRI_MSG)
            ->insert(
                [
                    'to_uid'   => $to,                  // 发送者
                    'from_uid' => $from,                // 接收者
                    'msg'      => $this->data,          // 消息
                ]
            )
            ->getSql();

        $mysql->exec($sql);
    }
}