<?php


namespace ImWebSocket\Chat;

use Common\Mysql;
use Common\Redis;
use Log\Log;
use Log\WsLog;
use Swoole\Http\Request;
use Swoole\WebSocket\Server;
use Tools\Sql;


final class Online
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
    private $redis;         // 复用 redis
    /**
     * @var \PDO|null
     */
    private $mysql;         // 复用 mysql
    /**
     * @var Sql
     */
    private $sql;           // 复用
    /**
     * @var array 好友列表
     */
    private $friends;
    /**
     * @var array
     * 用户 群聊
     */
    private $groups;
    /**
     * @var array
     * 用户 离线消息
     */
    private $msg;
    /**
     * @var array gid <=> mid 映射
     */
    private $gid_msg;
    /**
     * @var Server
     */
    private $ws;
    /**
     * @var Request
     */
    private $req;


    public function __construct(Server $ws, Request $req, int $uid)
    {
        $this->ws  = $ws;
        $this->req = $req;
        $this->fd  = $req->fd;
        $this->uid = $uid;

        $this->run();

    }

    /**
     *
     */
    private function run()
    {
        if (isset($this->req->get['new'])) {
            $this->new_device();
        }
        else {
            $this->old_device();
        }
    }

    /**
     * 用户 信息 绑定 到 redis
     */
    private function user_bind()
    {
        $redis = $this->redis;
        /**
         * ws socket uid 双向绑定
         * 绑定效果如下
         *
         * user:1000 fd 1 100 1000 101 800
         * user:$uid 为哈希结构 表示 user 1000 对应 fd 1
         * 同时有两个群 群号为 100 已读消息 1000 群号 101 已读消息 800
         *
         * fd:1 1000 表示 文件描述符 1 绑定 用户 1000
         */
        $uid = $this->uid;
        $redis->set(WsRedis::SOCKET_FD . $this->fd, WsRedis::USER_PREFIX . $uid);
        $redis->hSet(WsRedis::USER_PREFIX . $uid, WsRedis::SOCKET_FD, $this->fd);
    }

    /**
     * mysql 群聊信息缓存到 redis 中
     */
    private function mysql_redis()
    {
        $this->user_bind();
        // 当前用户没有 群聊 不处理
        if (empty($this->gid_msg)) {

            return;
        } else {
            // 使用哈希方式 与 user 建立关联
            $this->redis->hMSet(
                WsRedis::USER_PREFIX . $this->uid,
                $this->gid_msg
            );
            // 建立在线 群聊 <=> 用户关系 将当前用户添加到所在群聊的 set 中
            foreach ($this->groups as $v) {             // $v 现在 的格式 就是 group:$gid
                $this->redis->sAdd(WsRedis::GROUP_PREFIX . $v, $this->uid);
            }

            return;
        }
    }

    /**
     * @return array
     * 获取所有好友
     */
    private function getFriend(): array
    {
        $sql   = $this->sql;
        $query = $sql
            ->setTable(WsMysql::FRIEND)
            ->select(
                [
                    'uid_1',
                    'uid_2',
                ]
            )
            ->whereOr(
                ['uid_1', '=', $this->uid,],
                ['uid_2', '=', $this->uid,]
            )
            ->getSql();

        $friends = $this->mysql->query($query)->fetchAll();

        // 过滤 自身
        $friends = array_map(function ($value) {
            return $value['uid_1'] != $this->uid ? $value['uid_1'] : $value['uid_2'];
        }, $friends);

        $this->friends = $friends;

        return $this->friends;
    }

    /**
     * 获取 未读私聊消息
     */
    private function pri_msg(): array
    {
        $query   = $this->sql
            ->setTable(WsMysql::PRI_MSG)
            ->select(
                [
                    'msg',
                ]
            )
            ->whereAnd(
                [
                    'to_uid', '=', $this->uid
                ]
            )
            ->getSql();
        $pri_msg = $this->mysql->query($query)->fetchAll();

        return $pri_msg;
    }

    /**
     * 获取 用户的所有群聊
     */
    private function getGroups(): array
    {

        $query = $this->sql
            ->setTable(WsMysql::USER_GROUP_TABLE)
            ->select(
                [
                    'gid',                          // 群号
                    'last_msg_id',                  // 最大 msg id
                ]
            )
            ->whereAnd(['uid', '=', $this->uid])
            ->getSql();

        // 取回此用户的所有 群号
        $groups = $this->mysql->query($query)->fetchAll();

        // 调换索引结构 形成 gid <=> mid 结构
        $this->gid_msg = array_column($groups, 'last_msg_id', 'gid');
        $this->groups  = array_keys($this->gid_msg);
        return $this->groups;
    }

    /**
     * @return array 返回离线消息
     */
    private function group_msg(): array
    {
        $msg = [];
        // 循环取回每个 群的离线消息 再组合
        foreach ($this->gid_msg as $gid => $mid) {
            $group_table          = WsMysql::GROUP_TABLE_PREFIX . $gid;
            $query                = $this->sql
                ->setTable($group_table)
                ->select(
                    [
                        'msg',
                    ]
                )
                ->whereAnd(['mid', '>', $mid])
                ->getSql();
            $result               = $this->mysql->query($query)->fetchAll();
            $msg['group_' . $gid] = $result;
        }

        return $msg;
    }

    /**
     * 构造 mysql redis sql 供后续函数使用
     */
    private function init_reuse(): void
    {
        $this->mysql = (new Mysql())->getInstance();
        $this->sql   = new Sql();
        $this->redis = (new Redis())->getInstance();

        return;
    }

    /**
     * 新设备第一次登录
     * 1. 拿到所有 好友 以及 群组 信息
     * 2. 拿到所有离线消息
     */
    private function new_device()
    {
        $this->init_reuse();
        /**
         * 考虑 用户上线
         */

        // 好友 info 处理
        $friends                   = $this->getFriend();
        $this->msg['friends']      = $friends;
        $this->msg['friends_info'] = $this->getFriendsInfo($friends);
        // 群聊 info 处理
        $groups                   = $this->getGroups();
        $this->msg['groups']      = $groups;
        $this->msg['groups_info'] = $this->getGroupsInfo($groups);

        // 好友 offline message 处理
        $this->msg['offline_message'] = $this->pri_msg(); // 因为包含 好友申请信息 这样命名比较合适

        // 群聊 offline message 处理
        $this->msg['groups_message'] = $this->group_msg();

        $this->push();
    }

    /**
     * 非 新设备登录
     * 只需要推送离线消息即可
     */
    private function old_device() {
        $this->init_reuse();

        // 好友 offline message 处理
        $this->msg['offline_message'] = $this->pri_msg(); // 因为包含 好友申请信息 这样命名比较合适

        // 获取 群聊 离线消息前 需要获取 群聊信息
        $this->getGroups();
        // 群聊 offline message 处理
        $this->msg['groups_message'] = $this->group_msg();

        $this->push();
    }

    /**
     * 消息推送
     */
    private function push() {
        /**
         * 虽然是刚上线，也有可能突然掉线，进行意外处理
         * 判断 ws 是否中断，判断消息推送是否成功，
         * 失败则不处理
         * 成功则 将用户绑定 到 redis 中 同时 删掉已推送的离线消息
         */
        if ($this->ws->isEstablished($this->fd)) {
            $ok = $this->ws->push($this->fd, json_encode($this->msg));

            // mysql redis 同步 在线状态交由 redis 处理
            // 删除 离线消息
            // 失败说明用户掉线 不处理即可

            if ($ok) {
                $this->mysql_redis();
                $this->delete_pri_msg();
            }
        }
    }

    /**
     * @param array $friends uid 集合
     * @return array
     * 获取 uid 的用户信息
     */
    private function getFriendsInfo(array $friends): array
    {
        // 见 http 模块 search api
        static $private_key = [
            'create_time',
            'email',
            'password',
        ];
        // 这里使用 原生语句 pdo prepare

        $query = 'select `*` from ' . WsMysql::USER_INFO_TABLE . ' where `uid` = ?';
        $sql   = $this->mysql->prepare($query);

        $info = [];
        foreach ($friends as $friend) {
            $sql->execute([$friend]);
            $info[$friend] = $sql->fetch();
        }

        // 过滤 隐私字段
        $filter = function (&$input) use ($private_key) {
            foreach ($private_key as $v) {
                unset($input[$v]);
            }
        };

        array_walk($info, $filter);

        return $info;
    }

    /**
     * @param array $groups
     * @return array
     * 返回群聊信息
     */
    private function getGroupsInfo(array $groups): array
    {
        $info  = [];
        $query = 'select `*` from ' . WsMysql::GROUP_INFO_TABLE . ' where `gid` = ?';

        $sql = $this->mysql->prepare($query);
        foreach ($groups as $group) {
            $sql->execute([$group]);
            $info[$group] = $sql->fetch();          // gid 保证唯一性 所以 不必 fetchAll()
        }

        return $info;
    }

    /**
     * 用户上线接收到 离线消息后 删除离线消息
     */
    private function delete_pri_msg()
    {
        // 在 pri_msg 表中找到 所有 to_uid 为 uid 的消息 删除
        $query = $this->sql
            ->setTable(WsMysql::PRI_MSG)
            ->delete()
            ->whereAnd(
                [
                    'to_uid', '=', $this->uid,
                ]
            )
            ->getSql();
        $this->mysql->exec($query);
        WsLog::log(__FILE__ . ' ' . __LINE__ . ' ' . $query);
    }
}