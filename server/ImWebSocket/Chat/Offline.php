<?php


namespace ImWebSocket\Chat;
use Common\Redis;

class Offline
{
    /**
     * @var int
     */
    private $fd;
    /**
     * @var \Redis
     */
    private $redis;

    public function __construct(int $fd)
    {

        $this->fd = $fd;
        $this->run();
    }

    public function run() {

        //TODO: redis 同步 mysql

        $this->redis_clean();
    }

    private function redis_mysql() {

    }

    /**
     * 清除用户在 redis 中的 信息
     */
    private function redis_clean() {
        $redis = (new Redis())->getInstance();

        // 获取 user 类似 user:1000000 作为键获取 hash 表
        $user = $redis->get(WsRedis::SOCKET_FD . $this->fd);

        $groups = $redis->hGetAll($user);

        // 弹出第一项 fd:
        array_shift($groups);
        // 用户 id 从移除 群聊 set 中移除当前 uid
        $uid = @explode(':', $user)[1];
        foreach ($groups as  $k => $v) {
            $redis->sRem($k, $uid);
        }

        /**
         * 移除相关 key
         * 1 -> fd:$fd
         * 2 -> user:$uid
         */
        $redis->del(WsRedis::SOCKET_FD . $this->fd, $user);
    }
}