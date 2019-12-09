<?php


namespace ImHttp\Action;
use Common\Mysql;
use Tools\Sql;

/**
 * Class Reset
 * @package ImHttp\Action
 */
class Reset extends Action
{
    /**
     * 验证码长度
     */
    private const AUTHSIZE = 6;
    /**
     * 验证码有效期
     */
    private const EXPIRE_TIME = 5 * 60;

    /**
     * @return mixed
     * action 动作
     */
    public function run()
    {
        check_uid_email_exist:          // 检测 用户 id 和邮箱参数
        if (empty($this->post['uid'])) {
            $this->gateway->notice(['status' => 'error', 'msg' => 'no uid']);
            return;
        }
        if (empty($this->post['email'])) {
            $this->gateway->notice(['status' => 'error', 'msg' => 'no email']);
            return;
        }

        check_uid_email_match:          // 检测 用户 id 和 邮箱是否匹配
        $sql = (new Sql())->setTable(self::USER_TABLE)
            ->select(['email'])
            ->whereAnd(['uid', '=', $this->post['uid']])
            ->getSql();

        $mysql = (new Mysql())->getInstance();

        $back = $mysql->query($sql)->fetchAll();

        // 数据为空 即 没有对应 的 uid
        if (empty($back)) {
            $this->gateway->notice(['status' => 'error', 'msg' => '用户 id 不存在']);
        }
        else {
            $email = $back[0]['email'];
            // 检测 id 与 email 是否匹配
            if ($email !== $this->post['email']) {
                $this->gateway->notice(['status' => 'error', 'msg' => 'id 或 邮箱错误']);
            }
            else {
                // 发送邮件
                $mail_path = Mail::PATH;
                $auth = $this->auth();
                $type = Mail::RESET;
                $back = `php $mail_path $email $auth $type`;

                // 邮件发送失败
                if (Mail::OK !== $back) {
                    $this->gateway->notice(['status' => 'error', 'msg' => '邮件发送失败']);
                }
                // 邮件发送成功
                else {
                    $this->gateway->notice(['status' => 'ok', 'msg' => '邮件发送成功']);
                    //TODO: 检测 重置表格里是否有 此邮箱
                    $sql = (new Sql())->setTable(self::RESET_TABLE)
                            ->select(['*'])
                            ->whereAnd(['email', '=', $email])
                            ->getSql();
                    // 重置表格里没有 此邮箱 插入
                    if (empty($mysql->query($sql)->fetchAll())) {
                        $sql = (new Sql())->setTable(self::RESET_TABLE)
                                    ->insert(
                                        [
                                            $email,
                                            $auth,
                                            time() + self::EXPIRE_TIME,
                                        ]
                                    )
                                    ->getSql();
                        $mysql->query($sql);
                    }
                    // 更新验证码
                    else {
                        $sql = (new Sql())->setTable(self::RESET_TABLE)
                                        ->update(
                                            [
                                                'auth' => $auth,
                                                'expire_time' => time() + self::EXPIRE_TIME,
                                            ])
                                        ->whereAnd(['email', '=', $email])
                                        ->getSql();
                        $mysql->query($sql);
                    }

                }
            }

        }
    }

    /**
     * function 生成验证码
     * @return string
     */
    private function auth() {
        $arr = array_pad([], self::AUTHSIZE, 0);
        array_walk($arr, function (&$v) {
            $v = mt_rand(0, 9);
        });

        return implode('', $arr);
    }
}