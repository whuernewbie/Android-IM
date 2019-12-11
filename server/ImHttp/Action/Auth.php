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
    /**
     * 注册认证
     */
    private const REGISTER = 'register';

    /**
     * 密码重置认证
     */
    private const RESET    = 'reset';

    /**
     * 注册认证
     */
    public function register_auth()
    {
        do_check: // api 参数 检测

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

        check_auth: // 检测验证码

        $email = $this->post['email']; // 邮箱
        $mysql = (new Mysql())->getInstance();

        $sql   = new Sql();

        echo $query = $sql
            ->setTable(self::REGISTER_TABLE)
            ->select(['auth'])
            ->whereAnd(['email', '=', $email])
            ->getSql();

        $result = $mysql->query($query)->fetch();

        // 结果为空 即 注册表中没有此邮箱信息
        if (empty($result)) {
            $this->gateway->notice(['status' => 'error', 'msg' => '不存在此邮箱']);

            return;
        } else {
            $auth = $result['auth'];

            // 对比 验证码
            if ($auth === $this->post['auth']) {
                // 移除 注册表中的信息
                $sql->reset();
                $query = $sql
                        ->setTable(self::REGISTER_TABLE)
                        ->delete()
                        ->whereAnd(['email', '=', $email])
                        ->getSql();

                $mysql->exec($query);

                // 插入用户信息
                $sql->reset();
                $query = $sql
                    ->setTable(self::USER_TABLE)
                    ->insert(
                        [
                            'uid'         => null,
                            'uname'       => $this->post['uname'],
                            'password'    => $this->post['password'],
                            'email'       => $this->post['email'],
                            'create_time' => 'unix_timestamp(now())',
                        ]
                    )
                    ->getSql();

                $mysql->exec($query);

                /**
                 * 拿回用户 id
                 * 原因在于 多进程模式下，不能直接拿回 user 表的最大值
                 */
                $sql->reset();
                $query = $sql->setTable(self::USER_TABLE)
                        ->select(['uid'])
                        ->whereAnd(['email', '=', $this->post['email']])
                        ->getSql();

                $result = $mysql->query($query)->fetch();
                $uid = $result['uid'];

                // 先响应 后台添加 客服
                $this->gateway->notice(['status' => 'ok', 'uid' => $uid]);

                // 添加 客服管理员
                //TODO:: 客服 昵称以及 客服 id 设置
                $sql->reset();
                $query = $sql
                        ->setTable(self::FRIEND_TABLE)
                        ->insert(
                            [
                                'uid_1' => 1000000,
                                'uid_2' => $uid,
                            ]
                        )
                        ->getSql();

                $mysql->exec($query);

                return;
            } else {

                $this->gateway->notice(['status' => 'error', 'msg' => '验证码错误']);

                return;
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
        $mysql  = (new Mysql())->getInstance();
        $result = $mysql->query($sql)->fetchAll();

        // 结果为空 1. 用户并没有进行密码重置 2. 验证码失效 被删除
        if (empty($result)) {
            $this->gateway->notice(['status' => 'error', 'msg' => '操作失败']);
            return;
        } else {
            if ($result[0]['auth'] !== $this->post['auth']) {
                $this->gateway->notice(['status' => 'error', 'msg' => '验证码错误']);
                return;
            } // 通过验证
            else {

                // 移除 重置表中的数据
                $sql = (new Sql())
                    ->setTable(self::RESET_TABLE)
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