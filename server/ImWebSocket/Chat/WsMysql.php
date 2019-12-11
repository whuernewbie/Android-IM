<?php


namespace ImWebSocket\Chat;


/**
 * Interface WsMysql
 * @package ImWebSocket\Chat
 */
interface WsMysql
{
    /**
     * 私聊消息表
     */
    const PRI_MSG     = 'pri_msg';
    /**
     * 群聊 好友关系表
     */
    const USER_GROUP_TABLE = 'group_person';
}