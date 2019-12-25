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
     * 注册认证 api 必要参数
     */
    private const REGISTER_KEY = [
        'email',                // 邮箱
        'auth',                 // 验证码
        'uname',                // 用户名
        'password',             // 密码
    ];

    /**
     * 密码重置认证
     */
    private const RESET = 'reset';

    /**
     * 密码重置认证 api 参数
     */

    private const RESET_KEY = [
        'email',                    // 邮箱
        'auth',                     // 验证码
        'password',                 // 新密码
    ];

    /**
     * api 处理 判断 register 与 reset
     */
    public function run()
    {
        $type = @$this->get['type'];
        switch ($type) {
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

    /**
     * 注册认证
     */
    public function register_auth()
    {
        do_check: // api 参数 检测

        $ok = $this->register_check();

        // 缺失参数 回应错误
        if (true !== $ok) {
            $this->gateway->notice(['status' => 'error', 'msg' => 'no ' . $ok]);
            return;
        }

        check_auth: // 检测验证码

        $email = $this->post['email'];      // 邮箱
        $mysql = (new Mysql())->getInstance();

        $sql = new Sql();

        $query = $sql
            ->setTable(HttpMysql::REGISTER_TABLE)
            ->select(['auth'])
            ->whereAnd(['email', '=', $email])
            ->getSql();

        $result = $mysql->query($query)->fetch();

        // 结果为空 即 注册表中没有此邮箱信息 终止
        if (empty($result)) {

            $this->gateway->notice(['status' => 'error', 'msg' => '操作失败']);
            return;
        } else {
            // 对比 验证码
            $auth = $result['auth'];
            if ($auth === $this->post['auth']) {
                // 移除 注册表中的信息
                $query = $sql
                    ->setTable(HttpMysql::REGISTER_TABLE)
                    ->delete()
                    ->whereAnd(['email', '=', $email])
                    ->getSql();

                $mysql->exec($query);

                // 插入用户信息
                $query = $sql
                    ->setTable(HttpMysql::USER_TABLE)
                    ->insert(
                        [
                            'uid'        => null,
                            'uname'      => $this->post['uname'],
                            'password'   => $this->post['password'],
                            'email'      => $this->post['email'],
                            'createTime' => 'unix_timestamp(now())',
                        ]
                    )
                    ->getSql();

                $mysql->exec($query);

                /**
                 * 拿回用户 id
                 * 原因在于 多进程模式下，不能直接拿回 user 表的最大值 作为 uid
                 */
                $query = $sql->setTable(HttpMysql::USER_TABLE)
                    ->select(['uid'])
                    ->whereAnd(
                        [
                            'email', '=', $this->post['email']
                        ]
                    )
                    ->getSql();

                $result = $mysql->query($query)->fetch();
                $uid    = $result['uid'];

                // 先响应 后台添加 客服
                $this->gateway->notice(['status' => 'ok', 'uid' => $uid]);

                // 添加 客服管理员
                $query = $sql
                    ->setTable(HttpMysql::FRIEND_TABLE)
                    ->insert(
                        [
                            'uid_1'      => HttpMysql::SERVICE_ID,              // 客服 id
                            'uid_2'      => $uid,                               // 用户 id
                            'remark_1_2' => $this->post['uname'],               // 用户名
                            'remark_2_1' => HttpMysql::SERVICE_NAME,            // 客服
                        ]
                    )
                    ->getSql();

                $mysql->exec($query);

                // 插入欢迎消息
                $query = $sql
                    ->setTable(HttpMysql::PRI_MSG_TABLE)
                    ->insert(
                        [
                            'msgTo'   => $uid,
                            'msgFrom' => HttpMysql::SERVICE_ID,
                            'msg'     => json_encode(
                                [
                                    'msgType' => 'USERCHAT',
                                    'msgFrom' => HttpMysql::SERVICE_ID,
                                    'msgTo'   => $uid,
                                    'message' => HttpMysql::WELCOME,
                                ]
                            )
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

        $ok = $this->reset_check();

        // 验证不通过 终止
        if (true !== $ok) {
            $this->gateway->notice(['status' => 'error', 'msg' => 'no ' . $ok]);

            return;
        }

        $sql    = new Sql();
        $query  = $sql
            ->setTable(HttpMysql::RESET_TABLE)
            ->select(['auth'])
            ->whereAnd(
                [
                    'email', '=', $this->post['email'],
                ]
            )
            ->getSql();
        $mysql  = (new Mysql())->getInstance();
        $result = $mysql->query($query)->fetch();

        // 结果为空
        if (empty($result)) {
            $this->gateway->notice(['status' => 'error', 'msg' => '操作失败']);

            return;
        } else {
            // 验证码对比
            if ($result['auth'] !== $this->post['auth']) {
                $this->gateway->notice(['status' => 'error', 'msg' => '验证码错误']);

                return;
            } else {

                // 优先相应 后台修改密码
                $this->gateway->notice(['status' => 'ok', 'msg' => '密码修改成功']);

                // 1. 移除 重置表中的数据
                $query = $sql
                    ->setTable(HttpMysql::RESET_TABLE)
                    ->delete()
                    ->whereAnd(['email', '=', $this->post['email']])
                    ->getSql();
                $mysql->query($query);

                // 2. 更新密码
                $query = $sql
                    ->setTable(HttpMysql::USER_TABLE)
                    ->update(['password' => $this->post['password']])
                    ->whereAnd(['email', '=', $this->post['email']])
                    ->getSql();
                $mysql->query($query);
            }
        }
    }


    /**
     * check api 参数完整性
     */
    public function check()
    {
        // TODO: Implement check() method.
    }

    /**
     * register 参数检测
     */
    private function register_check()
    {
        foreach (self::REGISTER_KEY as $key) {
            if (empty($this->post[$key])) {
                return $key;
            }
        }

        return true;
    }

    /**
     * 重置密码 参数 检测
     * @return mixed
     */
    private function reset_check()
    {
        foreach (self::RESET_KEY as $key) {
            if (empty($this->post[$key])) {
                return $key;
            }
        }

        return true;
    }

    /**
     * 注册时添加客服
     */
    private function add_service()
    {

    }
}