<?php


namespace ImHttp\Action;

use Swoole\Http\Server;
use Swoole\Http\Response;
use Swoole\Http\Request;

/**
 * Class Gateway
 * @package ImHttp\Action
 * http api 服务网关
 * 根据 api 参数 选择对应服务
 */
class Gateway
{
    /**
     * @array api 服务 数组
     */
    private static $api = [];
    /**
     * @var Server
     */
    public $server;
    /**
     * @var Request
     */
    public $req;
    /**
     * @var Response
     */
    public $res;

    /**
     * Gateway constructor.
     * @param Server $server 全局服务 master 进程 全局控制
     * @param Request $req http 请求体
     * @param Response $res http 响应体
     */
    public function __construct(Server $server, Request $req, Response $res)
    {
        $this->server = $server;
        $this->req    = $req;
        $this->res    = $res;
    }

    /**
     *  http 动作响应
     */
    public function run()
    {
        // 非指定动作 不处理
        if (empty($this->req->get['action'])) {
            $this->notice(['status' => 'error', 'msg' => 'no action']);
            return;
        }
        $action = $this->req->get['action'];

        // 找到对应 api 提供服务
        if (in_array($action, self::$api, true)) {
            $class_name = __NAMESPACE__ . '\\' . $action;
            (new $class_name($this))->run();
        }
        else {
            $this->notice(['status' => 'error', 'msg' => 'action error']);
        }

    }

    /**
     * @param array $msg 响应消息体
     * 回应 html 消息 json 格式输出
     */
    public function notice(array $msg)
    {
        $this->res->header('Content-Type', 'text/json;charset=utf-8');
        $msg = json_encode($msg);
        $this->res->end($msg);
    }

    /**
     *  利用匿名对象 以及反射 实现枚举 常量读取
     *  初始化 $api
     */
    public static function init() {
        try {
            self::$api = (new \ReflectionClass(
                (new class() implements Api {})
            ))->getConstants();
        } catch (\Exception $e) {
            echo $e->getMessage() . PHP_EOL;
        }

    }
}