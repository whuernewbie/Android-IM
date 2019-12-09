<?php


namespace ImHttp\Action;

use Common\Mysql;
use Tools\Sql;

/**
 * Class Auth
 * @package ImHttp\Action
 * auth api 接口 用户注册认证 | 用户重置密码
 * http     url?action=auth&type=register | url?action=auth&type=reset
 *
 * type=register api 所需参数
 * * email      邮箱
 * * auth       验证码
 * * uname      用户名
 * * password   密码
 *
 * type=reset api 所需参数
 * * email      邮箱
 * * auth       验证码
 * * password   新密码
 */
class Auth extends Action
{
    private const REGISTER = 'register';
    private const RESET    = 'reset';

    /**
     * 注册认证
     */
    public function register_auth()
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
        $email = $mysql->quote($this->post['email']);

        auth_check:
        $sql = 'select `auth` from `' . self::REGISTER_TABLE . '` where `email` = ' . $email;

        // 取出验证码
        $auth = $mysql->query($sql)->fetch();

        // 没有查到对应 邮箱 终止
        if (false === $auth) {
            $this->gateway->notice(['status' => 'error', 'msg' => '不存在此邮箱']);
            return;
        } else {
            // 验证码 错误
            if ($auth['auth'] !== $this->post['auth']) {
                $this->gateway->notice(['status' => 'error', 'msg' => '验证码错误']);
                return;
            } else {
                $sql = 'delete from `' . self::REGISTER_TABLE . '` where `email` = ' . $email;
                $mysql->query($sql);

                $uname    = $mysql->quote($this->post['uname']);
                $password = $mysql->quote($this->post['password']);
                $sql      = "insert into `" . self::USER_TABLE . "` values (null, {$uname}, {$password}, {$email}, null, null,unix_timestamp(now()), null)";
                $mysql->query($sql);

                $sql = 'select `uid` from `' . self::USER_TABLE . '` where `email` = ' . $email;
                $uid = $mysql->query($sql)->fetch()['uid'];
                $this->gateway->notice(['status' => 'ok', 'uid' => $uid]);
            }

        }
    }

    /**
     * 重置认证
     */
    public function reset_auth()
    {
        // 检查参数
        check_argument:
        // 用户 email
        if (empty($this->post['email'])) {
            $this->gateway->notice(['status' => 'error', 'msg' => 'no email']);
            return;
        }
        // 验证码
        if (empty($this->post['auth'])) {
            $this->gateway->notice(['status' => 'error', 'msg' => 'no auth']);
            return;
        }
        // 新密码
        if (empty($this->post['password'])) {
            $this->gateway->notice(['status' => 'error', 'msg' => 'no password']);
            return;
        }
        echo $sql = (new Sql())->setTable(self::RESET_TABLE)
                            ->select(['auth'])
                            ->whereAnd(
                                [
                                    'email', '=', $this->post['email'],
                                ]
                            )
                            ->getSql();
        $mysql = (new Mysql())->getInstance();
        $result = $mysql->query($sql)->fetchAll();

        // 结果为空 1. 用户并没有进行密码重置 2. 验证码失效 被删除
        if (empty($result)) {
            $this->gateway->notice(['status' => 'error', 'msg' => '操作失败']);
            return;
        }
        else {
            if ($result[0]['auth'] !== $this->post['auth']) {
                $this->gateway->notice(['status' => 'error', 'msg' => '验证码错误']);
                return;
            }
            // 通过验证
            else {

                // 移除 重置表中的数据
                $sql = (new Sql())->setTable(self::RESET_TABLE)
                                ->delete()
                                ->whereAnd(['email', '=', $this->post['email']])
                                ->getSql();
                $mysql->query($sql);
                // 更新密码
                $sql = (new Sql())->setTable(self::USER_TABLE)
                                    ->update(['password' => $this->post['password']])
                                    ->whereAnd(['email', '=', $this->post['email']])
                                    ->getSql();
                $mysql->query($sql);

                $this->gateway->notice(['status' => 'ok', 'msg' => '密码修改成功']);
            }
        }
    }

    /**
     * api 处理
     */
    public function run()
    {
        if (empty($this->get['type'])) {
            $this->gateway->notice(['status' => 'error', 'msg' => 'no type']);
            return;
        }
        switch ($this->get['type']) {
            case self::REGISTER:
                $this->register_auth();
                break;
            case self::RESET:
                $this->reset_auth();
                break;
            default:
                $this->gateway->notice(['status' => 'error', 'msg' => 'type error']);
        }
    }

}