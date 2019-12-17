<?php


namespace ImHttp\Action;

use Common\Mysql;
use Tools\Sql;

/**
 * Class Login
 * @package ImHttp\Action
 * api 登录模块  url?action=login
 * 所需参数
 * * uid 用户 id
 * * password 用户密码
 */
class Login extends Action
{

    private const LOGIN_KEY = [
        'uid',
        'password',
    ];

    public function run() {

        check_post:

        $ok = $this->check();

        // 缺少参数
        if (true !== $ok) {
            $this->gateway->notice(['status' => 'error', 'msg' => 'no ' . $ok]);

            return;
        }

        check_uid_pass:
        // 验证用户名 密码
        $mysql = (new Mysql())->getInstance();

        $sql = (new Sql())
            ->setTable(HttpMysql::USER_TABLE)
            ->select(
                [
                    'password',
                ]
            )
            ->whereAnd(['uid', '=', $this->post['uid']])
            ->getSql();

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
            } else {

                $this->gateway->notice(['status' => 'error', 'msg' => '账号或密码错误']);
            }
        }

    }

    /**
     * 检测参数
     * 缺少参数则返回参数 key
     * 通过返回 true
     * @return bool|mixed
     */
    public function check()
    {
        foreach (self::LOGIN_KEY as $key) {
            if (empty($this->post[$key])) {
                return $key;
            }
        }

        return true;
    }
}