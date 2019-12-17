<?php


namespace ImWebSocket\Chat;

use Swoole\WebSocket\Frame;
use Swoole\WebSocket\Server as WebSocket;

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
    public  $data;
    /**
     * @var mixed json 对象
     */
    public $msg;

    public function __construct(WebSocket $ws, Frame $frame)
    {

        $this->ws    = $ws;
        $this->frame = $frame;
        $this->data  = $frame->data;
        $this->msg   = json_decode($this->data);

        $this->run();
    }

    abstract public function run();
}