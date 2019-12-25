<?php


namespace ImHttp\Action;


use Common\Mysql;
use Im\Func\Worker;
use Tools\Sql;

class Share extends Action
{

    private const UPLOAD = 'upload';
    private const FETCH  = 'fetch';

    private const UPLOAD_KEYS = [
        'uid',
        'content',
        'address',
        'locationx',
        'locationy',
    ];

    private const FETCH_KEYS = [
        'uid',
        'locationx',
        'locationy',
        'start',
        'end',
        'distance',
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
        $action = @$this->get['type'];

        switch ($action) {
            case self::UPLOAD:
                $this->upload();
                break;
            case self::FETCH;
                $this->fetch();
                break;
            default:
                $this->gateway->notice(['status' => 'error', 'msg' => 'type error']);
                break;
        }

        return;
    }

    /**
     * 发布操作
     */
    private function upload()
    {
        $ok = $this->uploadCheckKey();

        if (true !== $ok) {
            $this->gateway->notice(['status' => 'error', 'msg' => 'no ' . $ok]);

            return;
        } else {
            $this->init();

            if (!$this->checkUserExist($uname)) {
                $this->gateway->notice(['status' => 'error', 'msg' => '用户不存在']);

                return;
            } else {
                $this->insertShare($uname);

                $this->gateway->notice(['status' => 'ok', 'msg' => '动态发布成功']);
            }
        }
    }

    /**
     * 发布动态 post 键检测
     */
    private function uploadCheckKey()
    {
        foreach (self::UPLOAD_KEYS as $key) {
            if (empty($this->post[$key])) {
                return $key;
            }
        }

        return true;
    }

    /**
     * 获取动态 键 检测
     */
    private function fetchCheckKey()
    {
        foreach (self::FETCH_KEYS as $key) {
            if (!isset($this->post[$key])) {
                return $key;
            }
        }

        return true;
    }

    /**
     * 判断用户真实性
     * @param $uname
     * @return
     */
    private function checkUserExist(&$uname)
    {
        $query = $this->sql
            ->setTable(HttpMysql::USER_TABLE)
            ->select(
                ['uname']
            )
            ->whereAnd(
                [
                    'uid', '=', $this->post['uid'],
                ]
            )
            ->getSql();
        $uname = $this->mysql->query($query)->fetch()['uname'];
        return !empty($uname);
    }

    /**
     *  初始化 mysql
     */
    private function init()
    {
        $this->mysql = (new Mysql())->getInstance();
        $this->sql   = new Sql();
    }

    /**
     * check api 参数完整性
     */
    public function check()
    {
        // TODO: Implement check() method.
    }

    /**
     * 插入 动态信息 到 数据库
     * @param $uname
     */
    private function insertShare($uname)
    {
        $query = $this->sql
            ->setTable(HttpMysql::SHARE_TABLE)
            ->insert(
                [
                    'uid'       => $this->post['uid'],
                    'uname'     => $uname,
                    'content'   => $this->post['content'],
                    'address'   => $this->post['address'],
                    'time'      => time(),
                    'locationx' => $this->post['locationx'],
                    'locationy' => $this->post['locationy'],
                ]
            )
            ->getSql();
        $this->mysql->exec($query);
    }

    /**
     * 动态获取 api
     */
    private function fetch()
    {

        if (true !== ($ok = $this->fetchCheckKey())) {
            $this->gateway->notice(['status' => 'error', 'msg' => 'no ' . $ok]);

            return;
        } else {
            // 初始化 mysql
            $this->init();

            if (!$this->checkUserExist($temp)) {
                $this->gateway->notice(['status' => 'error', 'msg' => '用户不存在']);

                return;
            } else {

                // 提供 好友 与 附近的人 服务
                if (isset($this->get['nearby'])) {
                    $this->getNearbyShare();
                } else {
                    $this->getFriendShare();
                }
            }
        }
    }

    /**
     * 获取好友动态
     */
    private function getFriendShare()
    {
        $this->init();
        $uid   = $this->post['uid'];
        $table = HttpMysql::FRIEND_TABLE;

        $query = "select `uid_1`  as `fr` from `{$table}` where `uid_2` = {$uid} union all select `uid_2` as `fr` from `{$table}` where `uid_1` = {$uid}";

        // 获取所有好友
        $friends = $this->mysql->query($query)->fetchAll();
        $friends = array_column($friends, 'fr');
        // 添加自己
        $friends[] = $uid;

        $query = [];
        $table = HttpMysql::SHARE_TABLE;
        foreach ($friends as $friend) {
            $query[] = "select `*` from `{$table}` where `uid` = {$friend}";
        }

        // 组合 sql 语句
        $start = (int)$this->post['start'] < 0 ?: 0;        // 保证 sql 语句正确性
        $end   = (int)$this->post['end'];
        $query = implode(' union ', $query);
        $query .= ' order by `time` desc limit ' . $start . ', ' . $end;

        $result = $this->mysql->query($query)->fetchAll();

        if (empty($result)) {
            $this->gateway->notice(['status' => 'noMore', 'msg' => '已经到底了']);
        } else {
            $this->gateway->notice(
                [
                    'status' => 'ok',
                    'msg'    => $result,
                ]
            );
        }
    }

    /**
     * 获取附近的人
     */
    private function getNearbyShare()
    {
        $lat      = $this->post['locationx'];     // 经度
        $lng      = $this->post['locationy'];     // 纬度
        $distance = (int)@$this->post['distance'] < 0 ?: 1;
        $fields   = Worker::getNearBy((float)$lat, (float)$lng, (float)$distance);  // 获取范围

        $query = $this->sql
            ->setTable(HttpMysql::SHARE_TABLE)
            ->select(['*'])
            ->whereAnd(
                [
                    'locationx', '>', $fields['lat'][0],
                ],
                [
                    'locationx', '<', $fields['lat'][1],
                ],
                [
                    'locationy', '>', $fields['lng'][0],
                ],
                [
                    'locationy', '<', $fields['lng'][1],
                ]
            )
            ->getSql();

        $start = (int)$this->post['start'] < 0 ?: 0;        // 保证 sql 语句正确性
        $end   = (int)$this->post['end'];

        $query .= ' order by `time` desc limit ' . $start . ', ' . $end;

        $result = $this->mysql->query($query)->fetchAll();

        if (empty($result)) {
            $this->gateway->notice(['status' => 'noMore', 'msg' => '已经到底了']);
        } else {
            $this->gateway->notice(
                [
                    'status' => 'ok',
                    'msg'    => $result,
                ]
            );
        }
    }
}