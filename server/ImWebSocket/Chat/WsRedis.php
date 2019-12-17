<?php


namespace ImWebSocket\Chat;


/**
 * Interface WsRedis
 * @package ImWebSocket\Chat
 * 用来 声明 websocket redis 交互使用的常量
 */
interface WsRedis
{
    /**
     * redis 用户对应的 文件描述符 标记
     */
    const SOCKET_FD    = 'fd:';
    /**
     * redis 用户 前缀
     */
    const USER_PREFIX  = 'user:';
    /**
     * redis 群聊前缀
     */
    const GROUP_PREFIX = 'group:';
}

/**
 * redis 中数据示例
 * user:1000001 fd: 2 group:2 100 group:3 156 ...   // hash 结构
 * fd:2 1000001                                     // key value
 * group:2 1000001 1000003 1000005 ...              // set 结构
 */