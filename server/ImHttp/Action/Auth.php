<?php

namespace ImHttp\Action;

use Common\Mysql;

/**
 * Class Auth
 * @package ImHttp\Action
 * auth api 接口 用户注册认证
 * http     url?action=auth
 * api 所需参数
 * * email      邮箱
 * * auth       验证码
 * * uname      用户名
 * * password   密码
 */
class Auth implements Action
{
    /**
     * @var Gateway 上层 网关 api
     */
    private $gateway;

    /**
     * @var array http 请求头 post 数据
     */
    private $post;

    /**
     * Auth constructor.
     * @param Gateway $gateway
     */
    public function __construct(Gateway $gateway)
    {
        $this->gateway = $gateway;
        $this->post    = $this->gateway->req->post;
    }

    /**
     * api 处理
     */
    public function run()
    {

        //:TODO 错误检测
        do_check: // 服务检测

        // post 不存在 email 终止
        if (empty($this->post['email'])) {
            $this->gateway->notice(['status' => 'error', 'msg' => 'auth <- no email']);
            return;
        }

        // post 不存在 auth 终止
        if (empty($this->post['auth'])) {
            $this->gateway->notice(['status' => 'error', 'msg' => 'auth <- no auth']);
            return;
        }

        // post 不存在 uname
        if (empty($this->post['uname'])) {
            $this->gateway->notice(['status' => 'error', 'msg' => 'auth <- no uname']);
            return;
        }

        // post 不存在 password
        if (empty($this->post['password'])) {
            $this->gateway->notice(['status' => 'error', 'msg' => 'auth <- no password']);
            return;
        }

        //:TODO post 数据验证通过 认证验证码
        check_ok:

        // 建立 数据库 连接
        $mysql = (new Mysql())->getInstance();

        // 防 sql 注入 简单 转义
        $email    = $mysql->quote($this->post['email']);

        auth_check:
       $sql = 'select `auth` from `' . self::REGISTER_TABLE. '` where `email` = ' . $email;

        // 取出验证码
        $auth = $mysql->query($sql)->fetch();

        // 没有查到对应 邮箱 终止
        if (false === $auth) {
            $this->gateway->notice(['status' => 'error', 'msg' => '不存在此邮箱']);
            return;
        }
        else {
            // 验证码 错误
            if ($auth['auth'] !== $this->post['auth']) {
                $this->gateway->notice(['status' => 'error', 'msg' => '验证码错误']);
                return;
            }
            else {
                $sql = 'delete from `' . self::REGISTER_TABLE . '` where `email` = ' . $email;
                $mysql->query($sql);

                $uname    = $mysql->quote($this->post['uname']);
                $password = $mysql->quote($this->post['password']);
                $sql = "insert into `" . self::USER_TABLE . "` values (null, {$uname}, {$password}, {$email}, null,unix_timestamp(now()), null)";
                $mysql->query($sql);

                $sql = 'select `uid` from `' . self::USER_TABLE .'` where `email` = ' . $email;
                $uid = $mysql->query($sql)->fetch()['uid'];
                $this->gateway->notice(['status' => 'ok', 'uid' => $uid]);
            }

        }

    }

}
