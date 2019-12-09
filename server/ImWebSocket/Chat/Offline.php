<?php


namespace ImWebSocket\Chat;
use Swoole\WebSocket\Server as WebSocket;
use Common\Redis;

class Offline implements WsRedis
{
    /**
     * @var WebSocket
     */
    private $ws;
    /**
     * @var int
     */
    private $fd;

    public function __construct(WebSocket $ws, int $fd)
    {

        $this->ws = $ws;
        $this->fd = $fd;
        $this->run();
    }

    public function run() {
        $redis = (new Redis())->getInstance();

        $user = $redis->get(self::SOCKET_FD . $this->fd);

        // 抑制错误 获取 uid 错误时 返回 null 不会影响后面处理
        $uid = @explode(':', $user)[1];
        $redis->unlink([self::SOCKET_FD . $this->fd, self::USER_PREFIX . $uid]);
        echo 'close' . PHP_EOL;

        //TODO: redis 同步 mysql
    }
}