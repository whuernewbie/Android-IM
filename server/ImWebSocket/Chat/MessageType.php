<?php


namespace ImWebSocket\Chat;


/**
 * Interface MessageType
 * 消息类型 实现 enum
 * @package ImWebSocket\Chat
 */
interface MessageType
{
    /**
     * 单聊
     */
    const SINGLE_CHAT = 'SingleChat';
    /**
     * 群聊
     */
    const GROUP_CHAT = 'GroupChat';
    /**
     * 心跳
     */
    const HEART_BEAT = 'Beat';
}