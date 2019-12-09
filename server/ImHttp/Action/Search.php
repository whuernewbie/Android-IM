<?php


namespace ImHttp\Action;
use Common\Mysql;
use Tools\Sql;

/**
 * Class Search
 * @package ImHttp\Action
 */
class Search extends Action
{
    private const PRIVATE_KEY= ['password', 'email', 'create_time'];

    /**
     * @return mixed
     * action 动作
     */
    public function run()
    {
        check_uid:
        if (empty($this->post['uid'])) {
            $this->gateway->notice(['status' => 'error', 'msg' => 'no uid']);
        }
        else {
            mysql_search:
            $sql = (new Sql())->setTable(self::USER_TABLE)
                            ->select(['*'])
                            ->whereAnd(['uid', '=', $this->post['uid']])
                            ->getSql();

            $mysql = (new Mysql())->getInstance();
            $back = $mysql->query($sql);
            $result = $back->fetchAll();
            // uid 不存在
            if (empty($result)) {
                $this->gateway->notice(['status' => 'error', 'msg' => '用户不存在']);
            }
            else {
                // uid 唯一 只有 1条数据
                $result = $result[0];

                // 过滤隐私字段
                (function(&$result){
                    foreach (self::PRIVATE_KEY as $v) {
                        unset($result[$v]);
                    }
                })($result);

                $this->gateway->notice(['status' => 'ok', 'userInfo' => $result]);
            }

        }
    }
}