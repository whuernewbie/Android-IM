<?php


namespace ImHttp\Action;

use Common\Mysql;
use Log\HttpLog;
use Tools\Sql;


class Update extends Action
{
    /**
     * api update post 必需字段
     */
    private const UPDATE_KEYS = [
        'uid',
    ];

    /**
     * 可修改字段
     */
    private const VALID_FIELD = [
        'uname'    => null,
        'password' => null,
        'email'    => null,
        'sex'      => null,
        'age'      => null,
        'imageUrl' => null,
        'moreInfo' => null,
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
        } else {
            // 过滤 post 中键值不在 VALID_FIELD 中的数据
            $new_info = array_intersect_key($this->post, self::VALID_FIELD);

            $mysql = (new Mysql())->getInstance();
            $sql   = new Sql();
            $query = $sql
                ->setTable(HttpMysql::USER_TABLE)
                ->select(['*'])
                ->whereAnd(['uid', '=', $this->post['uid']])
                ->getSql();

            $result = $mysql->query($query)->fetch();

            // 没有对应 uid 返回 错误
            if (empty($result)) {
                $this->gateway->notice(['status' => 'error', 'msg' => '用户不存在']);

                return;
            } else {
                // 更新信息
                $query = $sql
                    ->setTable(HttpMysql::USER_TABLE)
                    ->update($new_info)
                    ->whereAnd(['uid', '=', $this->post['uid']])
                    ->getSql();

                try {

                    $mysql->exec($query);
                    $this->gateway->notice(['status' => 'ok', 'msg' => '修改成功']);

                } catch (\PDOException $e) {

                    HttpLog::log($e->getTraceAsString());
                    $this->gateway->notice(['status' => 'error', 'msg' => '修改失败']);
                    return;
                }
                // TODO: 前台保证 更新的信息与 旧信息不同
            }
        }

        return;
    }

    /**
     * 检测参数
     * 缺少参数则返回参数 key
     * 通过返回 true
     * @return bool|mixed
     */
    public function check()
    {
        foreach (self::UPDATE_KEYS as $key) {
            if (empty($this->post[$key])) {
                return $key;
            }
        }

        return true;
    }
}