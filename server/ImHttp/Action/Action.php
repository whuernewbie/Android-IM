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

    /**
     * check api 参数完整性
     */
    abstract public function check();
}