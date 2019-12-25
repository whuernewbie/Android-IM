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
    private const TYPE = 'type';

    /**
     * api search post 必需字段
     */
    private const SEARCH_KEYS = [
        'uid',
    ];

    /**
     * 隐私字段 过滤 不显示
     */
    private const PRIVATE_KEY = [
        'password',
//        'email',
        'createTime',
    ];

    /**
     * @return mixed
     * action 动作
     */
    public function run()
    {
        $type = @$this->get[self::TYPE];
        switch ($type) {
            case 'user':
                $this->getUserInfo();
                break;
            case 'group':
                if (empty($this->post['gid'])) {
                    $this->gateway->notice(['status' => 'error', 'msg' => 'no gid']);
                    break;
                }
                $bool = isset($this->get['detail']);
                $this->getGroupInfo($this->post['gid'], $bool);
                break;
            default:
                $this->gateway->notice(['status' => 'error', 'msg' => 'type error']);
        }
    }

    /**
     * 检测参数
     * 缺少参数则返回参数 key
     * 通过返回 true
     * @return bool|mixed
     */
    public function check()
    {
        foreach (self::SEARCH_KEYS as $key) {
            if (empty($this->post[$key])) {
                return $key;
            }
        }

        return true;
    }

    /**
     * 获取用户信息
     */
    private function getUserInfo()
    {
        post_check:          // 检测 用户 id

        $ok = $this->check();
        if (true !== $ok) {
            $this->gateway->notice(['status' => 'error', 'msg' => $ok . ' error']);

            return;
        } else {
            mysql_search:
            $mysql = (new Mysql())->getInstance();
            $sql   = (new Sql())
                ->setTable(HttpMysql::USER_TABLE)
                ->select(['*'])
                ->whereAnd(['uid', '=', $this->post['uid']])
                ->getSql();


            $result = $mysql->query($sql)->fetch();
            // uid 不存在
            if (empty($result)) {

                $this->gateway->notice(['status' => 'error', 'msg' => '用户不存在']);
            } else {
                // 过滤隐私字段
                (function (&$result) {
                    foreach (self::PRIVATE_KEY as $v) {
                        unset($result[$v]);
                    }
                })($result);

                $this->gateway->notice(['status' => 'ok', 'userInfo' => $result]);
            }

        }

        return;
    }

    /**
     * @param int $gid
     * 获取群聊信息
     * @param bool $getDetail 详细信息 会获取 群的所有成员
     */
    private function getGroupInfo(int $gid, bool $getDetail = false)
    {
        $mysql = (new Mysql())->getInstance();
        $sql = new Sql();
        $query = $sql
            ->setTable(HttpMysql::GROUP_INFO_TABLE)
            ->select(
                ['*']
            )
            ->whereAnd(
                [
                    'gid', '=', $gid,
                ]
            )
            ->getSql();

        $result = $mysql->query($query)->fetch();

        if (empty($result)) {
            $this->gateway->notice(['status' => 'error', 'msg' => '群聊不存在']);

            return;
        } else {
            if (false === $getDetail) {
                $this->gateway->notice(
                    [
                        'status'    => 'ok',
                        'groupInfo' => $result,
                    ]
                );

                return;
            } else {
                $back['groupInfo'] = $result;

                $query = $sql
                    ->setTable(HttpMysql::GROUP_USER_TABLE)
                    ->select(
                        [
                            'uid',
                        ]
                    )
                    ->whereAnd(
                        [
                            'gid', '=', $gid,
                        ]
                    )
                    ->getSql();
                $result = $mysql->query($query)->fetchAll();
                $result = array_column($result, 'uid');
                $back['person'] = $result;

                $this->gateway->notice(
                    [
                        'status'    => 'ok',
                        'groupInfo' => $back,
                    ]
                );
            }

        }
    }
}