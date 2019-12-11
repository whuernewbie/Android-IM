<?php

namespace ImHttp\Action;

use Common\Mysql;

/**
 * Class Register
 * @package ImHttp\Action
 */
class Register extends Action
{
    /*
     *  验证码长度
     */
    public const AUTHSIZE = 6;

    /**
     *  生成验证信息插入注册表
     */
    public function run() {
        // 不存在 email 数据 终止
        if (empty($this->post['email'])) {
            $this->gateway->notice(['status' => 'error', 'msg' => 'register <- no email']);
            return;
        }
        $mysql = (new Mysql())->getInstance();
        $email = $mysql->quote($this->post['email']);

        // 检测 email 是否已被使用
        check_user_exist:

        $sql = 'select `email` from `' . self::USER_TABLE .'` where `email` = ' . $email;
        $fetch = $mysql->query($sql)->fetch();
        // email 已存在 user 表中
        if (false !== $fetch) {
            $this->gateway->notice(['status' => 'error', 'msg' => '此邮箱已被注册']);
            return;
        }

        // 检查 email 是否在 register 表中
        check_register_exist:
        $auth = $mysql->quote(self::auth());       // 生成验证码

        $sql = 'select `email` from `' . self::REGISTER_TABLE . '` where `email` = ' . $email;
        $fetch = $mysql->query($sql)->fetch();

        // email 已存在 register 表中 则为 重发 验证码操作 更新验证码
        if (false !== $fetch) {
            $sql = 'update `' . self::REGISTER_TABLE . '` set `auth` = ' . $auth . ' where `email` = ' . $email;
            $mysql->exec($sql);
        }
        else {      // 不在 register 表中 即第一次注册 插入数据即可
            $expire_time = time() + 5 * 60;
            echo $sql = 'insert into `' . self::REGISTER_TABLE . "` values ({$email}, {$auth}, {$expire_time})";
            $mysql->exec($sql);
        }

        $mail_path = Mail::PATH;
        $type = Mail::REGISTER;
        echo `php $mail_path $email $auth $type` . PHP_EOL;
        $this->gateway->notice(['status' => 'ok', 'msg' => '验证码发送成功']);
    }

    /**
     * function 生成验证码
     * @return string
     */
    private static function auth() {
        $arr = array_pad([], self::AUTHSIZE, 0);
        array_walk($arr, function (&$v) {
            $v = mt_rand(0, 9);
        });

        return implode('', $arr);
    }
}