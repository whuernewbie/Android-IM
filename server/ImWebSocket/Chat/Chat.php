<?php


namespace ImWebSocket\Chat;

use Swoole\WebSocket\Frame;
use Swoole\WebSocket\Server as WebSocket;

abstract class Chat
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
    public $msg;

    public function __construct(WebSocket $ws, Frame $frame)
    {

        $this->ws    = $ws;
        $this->frame = $frame;
        $this->msg   = json_decode($this->frame->data);
    }

    abstract public function run();
}