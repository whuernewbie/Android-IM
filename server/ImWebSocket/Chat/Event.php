<?php


namespace ImWebSocket\Chat;

use Common\Mysql;
use Common\Redis;
use Swoole\Http\Request;
use \Swoole\WebSocket\Server as WebSocket;
use \Swoole\WebSocket\Frame;
use Tools\Sql;

/**
 * Class Event
 * @package ImWebSocket\Chat
 */
final class Event implements MessageType, MessageField, WsMysql
{
    private const GET_UID = 'uid';
    /**
     * @array 消息类型数组
     */
    private static $message_type;

    /**
     * 初始化 message_type 消息类型
     */
    public static function init() {
        try {
            self::$message_type = (new \ReflectionClass(
                    (new class implements MessageType {})
                ))
                ->getConstants();

        } catch (\Exception $e) {
            echo $e->getMessage() . PHP_EOL;
        }
    }

    /**
     * 上线事件
     * @param WebSocket $ws
     * @param Request $req
     */
    public static function online(WebSocket $ws, Request $req)
    {

        // url 中不含 uid 关闭连接
        if (empty($req->get[self::GET_UID])) {
            $ws->close($req->fd);
            return;
        }
        else {
            $uid = $req->get[self::GET_UID];
            (new Online($uid, $req->fd));
        }
    }

    /**
     * 下线事件
     * @param WebSocket $ws
     * @param int $fd
     */
    public static function offline(WebSocket $ws, int $fd)
    {
        (new Offline($fd));
    }

    /**
     * 在线消息事件
     * @param WebSocket $ws
     * @param Frame $frame
     */
    public static function message(WebSocket $ws, Frame $frame) {

        $msg = json_decode($frame->data);
        $type = @$msg->{MessageField::TYPE};

        // 不存在 对应 type
        if (!in_array($type, self::$message_type, true)) {
            $ws->push(
                $frame->fd,
                json_encode(['status' => 'error', 'msg' => 'no type'])
                );

            return;
        } else {
            $class_name = __NAMESPACE__ . '\\' . $type;
            (new $class_name($ws, $frame));
        }
    }
}