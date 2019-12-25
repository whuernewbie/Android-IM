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
    const SINGLE_CHAT = 'USERCHAT';
    /**
     * 群聊
     */
    const GROUP_CHAT = 'GROUPCHAT';
    /**
     * 好友申请
     */
    const FRIEND_REQUEST = 'FriendReq';
    /**
     * 群聊申请
     */
    const GROUP_REQUEST = 'GroupReq';
    /**
     * 创建群聊
     */
    const GROUP_CREATE = 'GroupCreate';

    /**
     * 群聊邀请
     */
    const GROUP_INVITE = 'GroupInvite';

    /**
     * 心跳
     */
    const HEART_BEAT = 'BEAT';
}