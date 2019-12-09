<?php


namespace ImWebSocket\Chat;

use Swoole\Http\Request;
use Swoole\WebSocket\Server as WebSocket;
use Common\Redis;

class Online implements WsRedis
{

    /**
     * @var WebSocket
     */
    private $ws;
    /**
     * @var Request
     */
    private $req;

    public function __construct(WebSocket $ws, Request $req)
    {

        $this->ws  = $ws;
        $this->req = $req;

        $this->run();
    }

    public function run()
    {
        $req   = $this->req;
        $redis = (new Redis())->getInstance();

        /**
         * ws socket uid 双向绑定
         */
        $uid = $req->get[self::GET_UID];
        $redis->set(self::SOCKET_FD . $req->fd, self::USER_PREFIX . $uid);
        $redis->set(self::USER_PREFIX . $uid, self::SOCKET_FD . $req->fd);

        //TODO: mysql 数据库同步
    }
}