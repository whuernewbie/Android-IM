<?php


namespace ImHttp\Action;

use Common\Mysql;
use Tools\Sql;

/**
 * Class Reset
 * @package ImHttp\Action
 * 密码重置
 */
class Reset extends Action
{
    private const RESET_KEYS = [
        'uid',
        'email',
    ];
    /**
     * 验证码长度
     */
    private const AUTHSIZE = 6;

    /**
     * @return mixed
     * action 动作
     */
    public function run()
    {
        post_check:                     // 检测 用户 id 和邮箱参数

        $ok = $this->check();
        if (true !== $ok) {
            $this->gateway->notice(['status' => 'error', 'msg' => 'no ' . $ok]);

            return;
        }

        check_uid_email_match:          // 检测 用户 id 和 邮箱是否匹配

        $mysql = (new Mysql())->getInstance();
        $sql   = new Sql();

        $query = $sql
            ->setTable(HttpMysql::USER_TABLE)
            ->select(['email'])
            ->whereAnd(['uid', '=', $this->post['uid']])
            ->getSql();

        $back = $mysql->query($query)->fetch();

        // 数据为空 即 没有对应 的 uid
        if (empty($back)) {
            $this->gateway->notice(['status' => 'error', 'msg' => '用户 id 不存在']);

            return;
        } else {
            // 检测 id 与 email 是否匹配
            $email = $back['email'];
            if ($email !== $this->post['email']) {
                $this->gateway->notice(['status' => 'error', 'msg' => 'id 或 邮箱错误']);

                return;
            } else {
                $auth = self::auth();                   // 生成验证码
                $query = $sql
                    ->setTable(HttpMysql::RESET_TABLE)
                    ->select(['*'])
                    ->whereAnd(['email', '=', $email])
                    ->getSql();

                // 重置表格里没有 此邮箱 插入
                if (empty($mysql->query($query)->fetch())) {
                    $query = $sql
                        ->setTable(HttpMysql::RESET_TABLE)
                        ->insert(
                            [
                                $email,
                                $auth,
                                time() + HttpMysql::EXPIRE_TIME,
                            ]
                        )
                        ->getSql();
                    $mysql->exec($query);
                } else {
                    $query = $sql
                        ->setTable(HttpMysql::RESET_TABLE)
                        ->update(
                            [
                                'auth'        => $auth,
                                'expire_time' => time() + HttpMysql::EXPIRE_TIME,
                            ])
                        ->whereAnd(
                            ['email', '=', $email]
                        )
                        ->getSql();
                    $mysql->exec($query);
                }

                $this->gateway->notice(['status' => 'ok', 'msg' => '邮件发送成功']);
                // task process 发送邮件
                $this->gateway->server->task(
                    [
                        'type' => 'mail',
                        'mail_type' => Mail::RESET,
                        'to_mail' => $email,
                        'auth' => $auth,
                    ]
                );
            }

            return;
        }
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
        foreach (self::RESET_KEYS as $key) {
            if (empty($this->post[$key])) {
                return $key;
            }
        }

        return true;

    }
}