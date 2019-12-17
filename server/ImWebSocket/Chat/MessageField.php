<?php


namespace ImWebSocket\Chat;


/**
 * Interface MessageField
 * @package ImWebSocket\Message
 */
interface MessageField
{
    /**
     * 消息类型 key
     */
    const TYPE = 'msgType';
    /**
     * 消息发送者 key
     */
    const FROM = 'msgFrom';
    /**
     *  消息接受者 key
     */
    const TO  = 'msgTo';
    /**
     *  消息内容 key
     */
    const MSG = 'msg';
}