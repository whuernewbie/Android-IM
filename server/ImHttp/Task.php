<?php


namespace ImHttp;


use Common\Mysql;
use ImHttp\Action\HttpMysql;
use ImHttp\Action\Mail;
use Log\HttpLog;
use Tools\Sql;

final class Task
{
    public const TASK_TYPE = [
        'mail',
    ];

    public static function run(\Swoole\Server\Task $task)
    {
        $type = @$task->data['type'];
        if (in_array($type, self::TASK_TYPE)) {
            call_user_func_array([self::class, $type], [$task]);
        }
    }

    public static function mail(\Swoole\Server\Task $task)
    {
        static $keys = [
            'to_mail',
            'auth',
            'mail_type',
        ];
        $data    = $task->data;

        /**
         * 检测参数 (理论上 一定会通过)
         */
        foreach ($keys as $key) {
            if (empty($data[$key])) {
                return;
            }
        }

        $to_mail = $data['to_mail'];
        $auth    = $data['auth'];
        $type    = $data['mail_type'];

        // 发送邮件
        $ok = (new Mail($to_mail, $auth, $type))->send();
        HttpLog::log("send mail {$to_mail} {$auth} {$type} {$ok}");
    }

    /**
     * 定时任务，清除 过期验证码
     */
    public static function time() {
        static $table = [
            HttpMysql::REGISTER_TABLE,
            HttpMysql::RESET_TABLE,
        ];

        $time = \time() - HttpMysql::EXPIRE_TIME;
        $sql = new Sql();
        $mysql = (new Mysql())->getInstance();

        foreach ($table as $item) {
            $query = $sql
                ->setTable($item)
                ->delete()
                ->whereAnd(
                    [
                        'expire_time', '<', $time,
                    ]
                )
                ->getSql();
            $mysql->exec($query);
        }
        HttpLog::log(__METHOD__ . ' ' . __LINE__ . ' clean expire verification code');
    }
}