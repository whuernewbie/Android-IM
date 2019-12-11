<?php


namespace ImHttp\Action;


/**
 * abstract class Action
 * @package ImHttp\Action
 */
abstract class Action
{
    /**
     * @var Gateway 上层 网关 api
     */
    protected $gateway;
    /**
     * @var array http 请求体 post 数据
     */
    protected $post;
    /**
     * @var array http 请求头 get 数据
     */
    protected $get;

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
     * Action constructor.
     * @param Gateway $gateway
     */
    public function __construct(Gateway $gateway)
    {
        $this->gateway = $gateway;
        $this->post    = $this->gateway->req->post;
        $this->get     = $this->gateway->req->get;
    }

    /**
     * @return mixed
     * action 动作
     */
    abstract public function run();

}