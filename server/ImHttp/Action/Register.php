<?php

namespace ImHttp\Action;

use Common\Mysql;
use Tools\Sql;

/**
 * Class Register
 * @package ImHttp\Action
 */
class Register extends Action
{
    /**
     * api 参数
     */
    private const REGISTER_KEYS = [
        'email',
    ];

    /*
     *  验证码长度
     */
    private const AUTHSIZE = 6;

    /**
     *  生成验证信息插入注册表
     */
    public function run()
    {
        post_check:  // 验证 参数 错误则终止

        $ok = $this->check();
        if (true !== $ok) {
            $this->gateway->notice(['status' => 'error', 'msg' => 'no ' . $ok]);

            return;
        }

        $mysql = (new Mysql())->getInstance();
        $email = $this->post['email'];

        // 检测 email 是否已被使用
        check_user_exist:

        $sql   = new Sql();
        $query = $sql
            ->setTable(HttpMysql::USER_TABLE)
            ->select(['email'])
            ->whereAnd(['email', '=', $email])
            ->getSql();
        $fetch = $mysql->query($query)->fetch();

        // email 已存在 user 表中
        if (!empty($fetch)) {
            $this->gateway->notice(['status' => 'error', 'msg' => '此邮箱已被注册']);

            return;
        }

        // 检查 email 是否在 register 表中
        check_register_exist:

        $query = $sql
            ->setTable(HttpMysql::REGISTER_TABLE)
            ->select(['email'])
            ->whereAnd(['email', '=', $email])
            ->getSql();

        $fetch = $mysql->query($query)->fetch();

        $auth = self::auth();       // 生成验证码

        // email 已存在 register 表中 则为 重发 验证码操作 更新验证码
        if (!empty($fetch)) {
            $query = $sql
                ->setTable(HttpMysql::REGISTER_TABLE)
                ->update(
                    [
                        'auth'        => $auth,                                    // 验证码
                        'expire_time' => time() + HttpMysql::EXPIRE_TIME,   // 过期时间
                    ]
                )
                ->whereAnd(['email', '=', $email])
                ->getSql();

            $mysql->exec($query);

        } else {      // 不在 register 表中 即第一次注册 插入数据即可
            $query = $sql
                ->setTable(HttpMysql::REGISTER_TABLE)
                ->insert(
                    [
                        $email,                                 // 邮箱
                        $auth,                                  // 验证码
                        HttpMysql::EXPIRE_TIME + time(),        // 过期时间
                    ]
                )
                ->getSql();

            $mysql->exec($query);
        }

        /**
         * 这里假设邮箱不会发送失败
         */
        $this->gateway->notice(['status' => 'ok', 'msg' => '验证码发送成功']);

        // 投递到 task 进程发送邮件
        $this->gateway->server->task(
            [
                'type'      => 'mail',
                'mail_type' => Mail::REGISTER,
                'to_mail'   => $email,
                'auth'      => $auth,
            ]
        );
    }

    /**
     * function 生成验证码
     * @return string
     */
    private static function auth()
    {
        $arr = array_pad([], self::AUTHSIZE, 0);
        array_walk($arr, function (&$v) {
            $v = mt_rand(0, 9);
        });

        return implode('', $arr);
    }

    /**
     * 检测参数
     * 缺少参数则返回参数 key
     * 通过返回 true
     * @return bool|mixed
     */
    public function check()
    {
        foreach (self::REGISTER_KEYS as $key) {
            if (empty($this->post[$key])) {
                return $key;
            }
        }

        return true;
    }
}