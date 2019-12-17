<?php


namespace ImWebSocket\Chat;


/**
 * Interface WsMysql
 * @package ImWebSocket\Message
 */
interface WsMysql
{
    /**
     * 用户信息表
     */
    const USER_INFO_TABLE = 'user';
    /**
     * 群聊信息表
     */
    const GROUP_INFO_TABLE = 'group_info';
    /**
     * 私聊消息表
     */
    const PRI_MSG = 'pri_msg';
    /**
     * 好友关系表
     */
    const FRIEND = 'friend';
    /**
     * 群聊 好友关系表
     */
    const USER_GROUP_TABLE = 'group_person';
    /**
     * 群聊消息 存储表 前缀
     */
    const GROUP_TABLE_PREFIX = 'group_';
}