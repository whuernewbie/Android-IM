<?php


namespace ImWebSocket\Chat;


/**
 * Interface MessageField
 * @package ImWebSocket\Chat
 */
interface MessageField
{
    /**
     * 消息类型 key
     */
    const TYPE = 'type';
    /**
     * 消息发送者 key
     */
    const FROM = 'from';
    /**
     *  消息接受者 key
     */
    const TO  = 'to';
    /**
     *  消息内容 key
     */
    const MSG = 'msg';
}