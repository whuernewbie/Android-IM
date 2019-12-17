<?php


namespace ImWebSocket\Chat;


/**
 * Interface MessageType
 * 消息类型 实现 enum
 * @package ImWebSocket\Message
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
     * 好友申请
     */
    const FRIEND_REQUEST = 'FriendReq';
    /**
     * 群聊申请
     */
    const GROUP_REQUEST = 'GroupReq';
    /**
     * 心跳
     */
    const HEART_BEAT = 'Beat';
}