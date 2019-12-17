<?php


namespace ImHttp\Action;

use Common\Mysql;
use Common\Redis;
use ImWebSocket\Chat\WsRedis;
use Tools\Sql;

/**
 * api 说明
 * url?action=creategroup
 * post 参数
 * uid 群主 id
 * gname 群名称
 *
 * 响应参数
 * gid => $gid
 */

/**
 * Class CreateGroup
 * @package ImHttp\Action
 * 创建群聊
 */
class CreateGroup extends Action
{
    const CREATE_TABLE_SQL = <<<EOF
create table `group_gid` (
`mid` int primary key auto_increment COMMENT '消息 id, 自增',
`from_uid` int not null COMMENT '发送者 id',
`msg` text not null COMMENT '群聊消息'
) COMMENT = '群聊离线消息列表'
EOF;

    /**
     * api 参数 key
     */
    private const CREATE_GROUP_KEYS = [
        'uid',
        'gname',
    ];
    /**
     * @var \PDO|null
     */
    private $mysql;
    /**
     * @var Sql
     */
    private $sql;

    /**
     * @return mixed
     * action 动作
     */
    public function run()
    {
        do_check:
        $ok = $this->check();

        if (true !== $ok) {
            $this->gateway->notice(['status' => 'error', 'msg' => 'no ' . $ok]);

            return;
        } else {

            $this->makeGroup();
            return;
        }
    }

    /**
     * check api 参数完整性
     */
    public function check()
    {
        foreach (self::CREATE_GROUP_KEYS as $key) {
            if (empty($this->post[$key])) {

                return $key;
            }
        }

        return true;
    }

    /**
     * 创建群聊
     */
    private function makeGroup()
    {
        $uid   = $this->post['uid'];
        $uname = $this->getUserName($uid);

        if (null === $uname) {
            $this->gateway->notice(['status' => 'error', 'msg' => '用户不存在']);

            return;
        } else {
            // 1. 数据库添加群聊信息
            $gname = $this->post['gname'];
            $query = $this->sql
                ->setTable(HttpMysql::GROUP_INFO_TABLE)
                ->insert(
                    [
                        null,                           // gid 自增
                        $uid,                           // 群主 id
                        $gname,                         // 群聊 名称
                        'unix_timestamp(now())',        // time()
                        1,                              // 创建群聊 则只有自己 1 人
                    ]
                )
                ->getSql();
            $this->mysql->exec($query);

            // 2. 用户不会快速创建群聊 利用 uid 拿回创建的 gid

            $query = $this->sql
                ->setTable(HttpMysql::GROUP_INFO_TABLE)
                ->select(
                    ['gid']
                )
                ->whereAnd(
                    [
                        'owner', '=', $uid,
                    ]
                )
                ->getSql();

            // 即使用户创建了多个群聊，我们拿回 最大值 即是刚创建的 群聊
            $result = $this->mysql->query($query)->fetchAll();

            $result = array_column($result, 'gid');
            $gid    = max($result);
            $this->gateway->notice(['status' => 'ok', 'gid' => $gid]);

            // 3. 创建 gid 的群聊信息表
            $this->createTable($gid);

            // 4. 添加 group_person 信息
            $query = $this->sql
                ->setTable(HttpMysql::GROUP_USER_TABLE)
                ->insert(
                    [
                        'gid'         => $gid,              // 群号
                        'uid'         => $uid,              // 群主 id
                        'remark'      => $uname,            // 群主的用户名 作为备注
                        'join_time'   => time(),
                        'last_msg_id' => 0,                 // 初始时，消息为 0
                    ]
                )
                ->getSql();
            $this->mysql->exec($query);
            //TODO : 群聊建立后 需要 redis api 接口测试没有必要启用 redis
//            $this->redisSyncInfo($uid, $gid);
        }
    }

    /**
     * @param int $uid
     * @return mixed
     */
    private function getUserName(int $uid)
    {
        $this->mysql = (new Mysql())->getInstance();

        $this->sql = new Sql();

        $query = $this->sql
            ->setTable(HttpMysql::USER_TABLE)
            ->select(
                ['uid', 'uname']
            )
            ->whereAnd(
                ['uid', '=', $uid]
            )
            ->getSql();

        $result = $this->mysql->query($query)->fetch();

        // 没有数据 返回 null
        return @$result['uname'];
    }

    /**
     * @param $gid
     * 为 gid 的表创建 消息表
     */
    private function createTable($gid)
    {
        // 1. 创建群聊消息表
        $query = str_replace('gid', $gid, self::CREATE_TABLE_SQL);
        $this->mysql->exec($query);
    }

    /**
     * 测试时 不使用此函数
     *
     * 群聊创建后 同步到 群主的 redis 信息中
     * @param int $uid
     * @param int $gid
     */
    private function redisSyncInfo(int $uid, int $gid) {
        $redis = (new Redis())->getInstance();

        // 群聊刚建立 last_msg_id 设置为 0 即可
        $redis->hSet(WsRedis::USER_PREFIX . $uid, $gid, 0);
        // group:$gid set 添加 $uid
        $redis->sAdd(WsRedis::GROUP_PREFIX . $gid, $uid);
    }
}