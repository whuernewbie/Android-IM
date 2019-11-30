<?php


namespace ImHttp\Action;

use Common\Mysql;

/**
 * Class Login
 * @package ImHttp\Action
 * api 登录模块  url?action=login
 * 所需参数
 * * uid 用户 id
 * * password 用户密码
 */
class Login implements Action
{
    /**
     * @var Gateway
     */
    private $gateway;

    /**
     * @var array http 请求头 post 数据
     */
    private $post;

    /**
     * Login constructor.
     * @param Gateway $gateway
     */
    public function __construct(Gateway $gateway)
    {
        $this->gateway = $gateway;
        $this->post = $this->gateway->req->post;
    }

    public function run() {
        //:TODO 匹配用户名以及密码
        check_post:
        // 检测 post 数据包
        if (empty($this->post['uid'])) {
            $this->gateway->notice(['status' => 'error', 'msg' => 'login <- no uid']);
            return;
        }
        if (empty($this->post['password'])) {
            $this->gateway->notice(['status' => 'error', 'msg' => 'login <- no password']);
            return;
        }

        check_uid_pass:
        // 验证用户名 密码
        $mysql = (new Mysql())->getInstance();

        $uid = $mysql->quote($this->post['uid']);
        $sql = 'select `uid`, `password` from `' . self::USER_TABLE . '` where `uid` = ' . $uid;
        $fetch = $mysql->query($sql)->fetch();

        // 没有对应的 uid 终止
        if (false === $fetch) {
            $this->gateway->notice(['status' => 'error', 'msg' => '用户不存在']);
            return;
        }
        else {
            $password = $fetch['password'];
            // 密码认证成功
            if ($password === $this->post['password']) {
                $this->gateway->notice(['status' => 'ok', 'msg' => '认证成功']);
            }
            else {
                $this->gateway->notice(['status' => 'error', 'msg' => '密码错误']);
            }
        }

    }
}
