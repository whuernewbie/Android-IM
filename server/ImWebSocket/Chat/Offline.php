<?php


namespace ImWebSocket\Chat;
use Common\Mysql;
use Common\Redis;
use Tools\Sql;

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

        $this->redis_clean();
    }

    /**
     * 清除用户在 redis 中的 信息
     * 1. 对当前用户所在群聊的操作，将群聊的 set 都删除此用户 ( 相当于标记离线 )
     * 2. 解绑 fd 与 user group 关系 // 删除 key 即可
     *
     * * 3. 理应先获取 uid 对应的所有群聊 然后 删除 uid 这个键
     * * 4. 删除键后再 对各个群聊进行操作 移除当前用户
     */
    private function redis_clean() {
        $redis = (new Redis())->getInstance();

        // 获取 user 结果 类似 user:1000000 作为键获取 hash 表
        $user = $redis->get(WsRedis::SOCKET_FD . $this->fd);

        // redis 中不存在 对应的 uid 不处理 ( 针对 online 中 client 意外掉线情形)
        if (false === $user) {

            return;
        }

        $groups = $redis->hGetAll($user);

        /**
         * 移除相关 key
         * 1 -> fd:$fd
         * 2 -> user:$uid
         */
        $redis->del(WsRedis::SOCKET_FD . $this->fd, $user);
        // 移除第一项 fd:
        unset($groups[key($groups)]);
        // 用户 id  群聊 set 中移除当前 uid
        $uid = @explode(':', $user)[1];

        $groups = array_keys($groups);

        foreach ($groups as  $gid) {
            $redis->sRem(WsRedis::GROUP_PREFIX . $gid, $uid);
        }

        // 更新 mysql
        $this->redis_mysql($groups, $uid);
    }

    /**
     * redis 群聊信息 同步到 mysql 中 主要 是为了保证 last_msg_id
     * @param array $gid_array 群聊集合
     * @param int $uid 用户 id
     */
    private function redis_mysql(array $gid_array, int $uid) {
        $sql = new Sql();
        $mysql = (new Mysql())->getInstance();

        foreach ($gid_array as $gid) {
            $query = 'select max(`mid`) from `' . WsMysql::GROUP_TABLE_PREFIX . $gid . '`';

            // 群聊消息为 空表会返回 null 用 0 取代
            $result = (int)($mysql->query($query)->fetch(\PDO::FETCH_NUM)[0]);

            // 更新 last_msg_id
            $query = $sql
                ->setTable(WsMysql::USER_GROUP_TABLE)
                ->update(
                    [
                        'last_msg_id' => $result,
                    ]
                )
                ->whereAnd(
                    [
                        'gid', '=', $gid,
                    ],
                    [
                        'uid', '=', $uid,
                    ]
                )
                ->getSql();

            $mysql->exec($query);
        }
    }
}