<?php


namespace ImWebSocket\Chat;


use Common\Redis;

class SingleChat extends Chat implements MessageField, WsRedis
{

    /**
     * 私聊处理
     */
    public function run()
    {
        $to = @$this->msg->{MessageField::TO};
        $redis = (new Redis())->getInstance();
        $aim_fd = @explode(':', $redis->get(WsRedis::USER_PREFIX . $to))[1];
        $aim_fd ? $this->aimOnline($aim_fd) : $this->aimOffline();
    }

    /**
     * 目标 在线处理
     * @param int $aim_fd 目标用户 连接符
     */
    public function aimOnline(int $aim_fd) {
        var_dump($this->ws->push($aim_fd, json_encode($this->msg)));
    }

    /**
     * 目标离线处理
     */
    private function aimOffline() {

    }
}