<?php


namespace ImWebSocket\Chat;

use Log\WsLog;
use Swoole\Http\Request;
use \Swoole\WebSocket\Server as WebSocket;
use \Swoole\WebSocket\Frame;

/**
 * Class Event
 * @package ImWebSocket\Message
 */
final class Event
{
    private const GET_UID = 'uid';
    /**
     * @array 消息类型数组
     */
    private static $message_type;

    /**
     * 利用反射 初始化 message_type 消息类型
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

            $ws->push($req->fd, json_encode(['status' => 'error']));
            $ws->close($req->fd);
            return;
        }
        else {
            $uid = $req->get[self::GET_UID];
            (new Online($ws, $req, $uid));
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

        MessageType::HEART_BEAT !== $type and WsLog::log($frame->data);     // log 消息 (除了 beat)

        // 不存在 对应 type 关闭连接
        if (!in_array($type, self::$message_type, true)) {

            $ws->push($frame->fd, json_encode(['status' => 'error', 'msg' => 'type error']));
            $ws->close($frame->fd);
            return;
        } else {

            $class_name = __NAMESPACE__ . '\\' . $type;
            (new $class_name($ws, $frame));
        }
    }
}