<?php


namespace ImHttp\Action;

use Common\Mysql;
use Tools\Sql;


class Update extends Action
{
    /**
     * 可修改字段
     */
    private const VALID_FIELD = [
        'uname'     => null,
        'password'  => null,
        'email'     => null,
        'sex'       => null,
        'age'       => null,
        'more_info' => null,
    ];

    /**
     * @return mixed
     * action 动作
     */
    public function run()
    {
        if (empty($this->post['uid'])) {
            $this->gateway->notice(['status' => 'error', 'msg' => 'no uid']);
            return;
        }
        else {
            // 过滤 post 中键值不在 VALID_FIELD 中的数据
            $new_info = array_intersect_key($this->post, self::VALID_FIELD);

            $sql    = new Sql();
            $mysql  = (new Mysql())->getInstance();

            $result = $mysql->query(
                $sql->setTable(self::USER_TABLE)
                    ->select(['*'])
                    ->whereAnd(['uid', '=', $this->post['uid']])
                    ->getSql()
            )->fetchAll();

            // 没有对应 uid 返回 错误
            if (empty($result)) {
                $this->gateway->notice(['status' => 'error', 'msg' => '用户不存在']);
                return;
            }
            else {
                // 清除 sql 语句
                $sql->reset();

                // 返回 受影响 行数 ok = 1  修改成功 ok = 0 失败
                // ok = 0 信息没有更新
                $ok = $mysql->exec(
                    $sql->setTable(self::USER_TABLE)
                        ->update($new_info)
                        ->whereAnd(['uid', '=', $this->post['uid']])
                        ->getSql()
                );

                $this->gateway->notice(
                    [
                        'status' => $ok ? 'ok' : 'error',
                        'msg'    => $ok ? '修改成功' : '修改失败',
                    ]
                );
            }


        }
    }
}