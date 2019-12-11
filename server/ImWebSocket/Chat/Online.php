<?php


namespace ImWebSocket\Chat;

use Common\Mysql;
use Common\Redis;
use Tools\Sql;


class Online implements WsRedis, WsMysql
{
    /**
     * @var int  用户 id
     */
    private $uid;
    /**
     * @var int ws 文件描述符 fd
     */
    private $fd;
    /**
     * @var \Redis redis 连接 跨函数使用
     */
    private $redis;

    public function __construct(int $uid, int $fd)
    {
        $this->uid = $uid;
        $this->fd  = $fd;

        $this->run();
    }

    private function run()
    {
        $redis = (new Redis())->getInstance();

        /**
         * ws socket uid 双向绑定
         * 绑定效果如下
         *
         * user:1000 fd 1 group:100 1000 group:101 800
         * user:$uid 为哈希结构 表示 user 1000 对应 fd 1
         * 同时有两个群 群号为 100 已读消息 1000 群号 101 已读消息 800
         *
         * fd:1 1000 表示 文件描述符 1 绑定 用户 1000
         */
        $uid = $this->uid;

        $redis->set(self::SOCKET_FD . $this->fd, self::USER_PREFIX . $uid);
        $redis->hSet(self::USER_PREFIX . $uid, self::SOCKET_FD , $this->fd);

        $this->redis = $redis;

        $this->mysql_redis();
        //TODO: mysql 数据库同步
    }

    /**
     * mysql 信息缓存到 redis 中
     */
    private function mysql_redis() {
        $mysql = (new Mysql())->getInstance();
        $sql = new Sql();
        $query = $sql
                ->setTable(WsMysql::USER_GROUP_TABLE)
                ->select(
                    [
                        'gid',                          // 群号
                        'last_msg_id',                  // 最大 msg id
                    ]
                )
                ->whereAnd(['uid', '=', $this->uid])
                ->getSql();

        // 取回所有群聊数据
        $groups = $mysql->query($query)->fetchAll();

        // 当前用户没有 群聊 不处理
        if (empty($groups)) {

            return;
        } else {
            // 生成 gid => msg 数组

            $gid_msg = array_column($groups, 'last_msg_id', 'gid');

            // 为键增加 前缀

            $keys = array_keys($gid_msg);
            array_walk($keys, function (&$v) {
               $v = WsRedis::GROUP_PREFIX . $v;
            });

            $gid_msg = array_combine($keys, $gid_msg);

            // 使用哈希方式 与 user 建立关联
            $this->redis->hMSet(
                WsRedis::USER_PREFIX . $this->uid,
                $gid_msg
                );

            // 建立群聊关系 将当前用户添加到所在群聊的 set 中

            foreach ($keys as $v) {             // $v 现在 的格式 就是 group:$gid
                $this->redis->sAdd($v, $this->uid);
            }

            return;
        }
    }
}