<?php


namespace ImHttp\Action;

/**
 * Interface HttpMysql
 * @package ImHttp\Action
 *
 * 定义 http 模块与 mysql 相关的 常量
 */
interface HttpMysql
{
    /**
     * * 数据库 用户表名
     */
    const USER_TABLE = 'user';
    /**
     *  * 数据库 注册表名
     */
    const REGISTER_TABLE = 'register';

    /**
     * * 数据库 密码重置表
     */
    const RESET_TABLE = 'found_lost';

    /**
     * * 好友信息表
     */
    const FRIEND_TABLE = 'friend';

    /**
     * 后台 客服 昵称
     */
    const SERVICE_NAME = '客服';

    /**
     * 后台客服 id
     */
    const SERVICE_ID = 1000000;

    /**
     * 欢迎 消息
     */
    const WELCOME = '欢迎注册 Talk Talk 账号';

    /**
     * 验证码 过期时间 5 分钟
     */
    const EXPIRE_TIME = 60 * 5;
    /**
     * 群聊信息表
     */
    const GROUP_INFO_TABLE = 'group_info';

    /**
     * 群聊表 前缀
     */
    const GROUP_TABLE_PREFIX = 'group_';

    /**
     * 群聊 用户关系表
     */
    const GROUP_USER_TABLE = 'group_person';

    /**
     * 私聊关系表
     */
    const PRI_MSG_TABLE = 'pri_msg';

    /**
     * 动态表
     */
    const SHARE_TABLE = 'share';
}