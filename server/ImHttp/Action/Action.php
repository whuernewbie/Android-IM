<?php


namespace ImHttp\Action;


/**
 * Interface Action
 * @package ImHttp\Action
 */
interface Action
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
     * @return mixed
     * action 动作
     */
    public function run();
}
