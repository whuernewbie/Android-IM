<?php


namespace ImWebSocket\Chat;

use Common\Mysql;
use Common\Redis;
use Log\WsLog;
use Tools\Sql;

class FriendReq extends Message
{
    private const FIELD = 'actionType';

    private const ACTION_TYPE = [
        'req'   => 'request',                           // 申请者 请求
        'agree' => 'agree',                           // 被申请者 同意
        'refuse',                                       // 被申请者 拒绝 // 暂时不用
    ];
    /**
     * @var \Redis
     */
    private $redis;
    /**
     * @var \PDO|null
     */
    private $mysql;
    /**
     * @var Sql
     */
    private $sql;

    /**
     * @var object 消息体 json 对象
     */

    public function run()
    {
        var_dump($this->msg);
        $this->msg   = json_decode($this->data);
        var_dump($action_type = @$this->msg->{self::FIELD});

        switch ($action_type) {
            case self::ACTION_TYPE['req']:
                $this->friend_req();
                break;
            case self::ACTION_TYPE['agree']:
                $this->friend_agree();
                break;
            default:
                break;
        }
    }

    /**
     * 获取 用户 fd
     * @param $aim_uid
     * @return string
     */
    private function getUserFd($aim_uid)
    {
        $this->redis = (new Redis())->getInstance();
        $aim_fd      = $this->redis->hGet(WsRedis::USER_PREFIX . $aim_uid, WsRedis::SOCKET_FD);

        return $aim_fd;
    }

    /**
     * 好友请求处理
     */
    private function friend_req()
    {
        $msg     = json_decode($this->data);
        // 获取目标用户信息
        $aim_uid = @$msg->{MessageField::TO};
        $aim_fd = $this->getUserFd($aim_uid);

        if (false !== $aim_fd) {

            $this->aimOnline($aim_fd);
            WsLog::log(__FILE__ . ' ' . __LINE__ . ' ' . $msg . ' push ok ');
        } else {

            $this->init();
            $this->aimOffline();
        }
        WsLog::log(__FILE__ . ' ' . __LINE__ . ' ' . $msg . ' push error mysql ');
    }

    /**
     * @param $aim_fd
     * 对方在线 即推送
     */
    private function aimOnline($aim_fd)
    {
        // 处理意外 则离线
        if (!$this->ws->push($aim_fd, $this->data)) {
            // 触发 close
            $this->ws->close($aim_fd);
            $this->aimOffline();
        }
    }

    /**
     * 对方离线 插入数据库
     */
    private function aimOffline()
    {
        $msg      = $this->msg;
        $to_uid   = $msg->{MessageField::TO};
        $from_uid = $msg->{MessageField::FROM};

        $query       = $this->sql
            ->setTable(WsMysql::PRI_MSG)
            ->insert(
                [
                    $to_uid,
                    $from_uid,
                    $this->data,
                ]
            )
            ->getSql();

        $this->mysql->exec($query);
    }

    /**
     * 好友申请同意
     */
    private function friend_agree()
    {
        // 初始化 mysql 与 sql
        $this->init();

        // 申请同意后要向 申请者 发送消息
        $to_uid   = @$this->msg->{MessageField::FROM};
        $from_uid = @$this->msg->{MessageField::TO};

        $this->msg->{MessageField::FROM} = $from_uid;
        $this->msg->{MessageField::TO}   = $to_uid;

        // 反转 消息体中的 发送者和申请者
        $this->data = json_encode($this->msg);
        // 获取 aim fd
        $to_fd = $this->getUserFd($to_uid);

        false !== $to_fd ? $this->aimOnline($to_fd) : $this->aimOffline();

        // 好友关系保存至 数据库

        $this->friend_mysql($to_uid, $from_uid);
    }

    /**
     * 好友关系插入 数据库
     * @param $uid_1
     * @param $uid_2
     */
    private function friend_mysql($uid_1, $uid_2)
    {
        $query = $this->sql
            ->setTable(WsMysql::USER_INFO_TABLE)
            ->select(
                [
                    'uid',
                    'uname',
                ]
            )
            ->whereOr(
                [
                    'uid', '=', $uid_1,
                ],
                [
                    'uid', '=', $uid_2,
                ]
            )
            ->getSql();

        // mysql 返回结果是 按 uid 排序的 所以不必处理
        $result = $this->mysql->query($query)->fetchAll();

        $query = $this->sql
            ->setTable(WsMysql::FRIEND)
            ->insert(
                [
                    'uid_1' => $result[0]['uid'],
                    'uid_2' => $result[1]['uid'],
                    'remark_1_2' => $result[1]['uname'],
                    'remark_2_1' => $result[0]['uname'],
                ]
            )
            ->getSql();
        $this->mysql->exec($query);
    }

    /**
     * 初始化 可共用的 mysql 连接
     */
    private function init() {
        $this->mysql = (new Mysql())->getInstance();
        $this->sql   = new Sql();
    }
}