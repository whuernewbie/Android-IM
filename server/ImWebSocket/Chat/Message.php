<?php


namespace ImWebSocket\Chat;

use Common\Mysql;
use Common\Redis;
use Swoole\WebSocket\Frame;
use Swoole\WebSocket\Server as WebSocket;
use Tools\Sql;


abstract class Message
{
    /**
     * @var WebSocket
     */
    public $ws;
    /**
     * @var Frame
     */
    public $frame;

    /**
     * @var string json 消息体
     */
    public $data;
    /**
     * @var mixed json 对象
     */
    public $msg;
    /**
     * @var \PDO|null
     */
    public $mysql;
    /**
     * @var \Redis
     */
    public $redis;
    /**
     * @var Sql
     */
    public $sql;

    public function __construct(WebSocket $ws, Frame $frame)
    {

        $this->ws    = $ws;
        $this->frame = $frame;
        $this->data  = $frame->data;

        $this->msg = json_decode($this->data);
        $this->run();
    }

    abstract public function run();

    /**
     * @param $fd
     * @param $to
     * @param $from
     * @param $msg
     * 在线推送
     */
    public function userOnline($fd, $to, $from, $msg)
    {
        // 推送失败 则离线
        $this->ws->push($fd, $msg) or $this->UserOffline($to, $from, $msg, $fd);
    }

    /**
     * @param $fd  int      ws 连接
     * @param $to  int      目标用户
     * @param $from int     发送者
     * @param $msg string   消息
     * 离线入库
     * * 用户离线时 fd = -1 不关闭 fd
     * * 用户在线 但是推送失败 说明用于 意外掉线 此时 $fd 需要关掉 $fd
     */
    public function userOffline($to, $from, $msg, $fd = -1)
    {
        -1 === $fd or $this->ws->close($fd);        // 根据情况 关闭 $fd

        // 离线 到 数据库
        $query = $this->sql
            ->setTable(WsMysql::PRI_MSG)
            ->insert(
                [
                    'msgTo'   => $to,
                    'msgFrom' => $from,
                    'msg'     => $msg,
                ]
            )
            ->getSql();
        $this->mysql->exec($query);
    }

    /**
     * 初始化句柄
     */
    public function init()
    {
        $this->mysql = (new Mysql())->getInstance();
        $this->redis = (new Redis())->getInstance();
        $this->sql   = new Sql();
    }

    /**
     * @param $to
     * @param $from
     * @param $msg
     * 推送消息
     */
    public function pushMsg($to, $from, $msg) {
        // 1. 获取 $to 的 $fd
        $fd = $this->redis->hGet(WsRedis::USER_PREFIX . $to, WsRedis::SOCKET_FD);

        // 2. 若 redis 中存在 用户信息
        if (false !== $fd) {
            $this->userOnline($fd, $to, $from, $msg);
        } else {
            $this->userOffline($to, $from, $msg);
        }
    }
}