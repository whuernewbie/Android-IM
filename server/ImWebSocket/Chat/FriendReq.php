<?php


namespace ImWebSocket\Chat;

use Common\Mysql;
use Common\Redis;
use Log\WsLog;
use Tools\Sql;

class FriendReq extends Message
{
    private const FIELD = 'actionType';

    private const ACTION_TYPE = [
        'req'   => 'request',                           // 申请者 请求
        'agree' => 'agree',                           // 被申请者 同意
        'refuse',                                       // 被申请者 拒绝 // 暂时不用
    ];

    public function run()
    {
        $this->init();
        $action_type = @$this->msg->{self::FIELD};

        switch ($action_type) {
            case self::ACTION_TYPE['req']:
                $this->friendReq();
                break;
            case self::ACTION_TYPE['agree']:
                $this->friendAgree();
                break;
            default:
                break;
        }
    }

    /**
     * 好友请求处理
     */
    private function friendReq()
    {

        $fromUid = @$this->msg->{MessageField::FROM};
        $aimUid  = @$this->msg->{MessageField::TO};

        // 推送 msg
        $this->pushMsg($aimUid, $fromUid, $this->data);

    }

    /**
     * 好友申请同意
     */
    private function friendAgree()
    {
        // 申请同意后要向 申请者 发送消息
        $toUid   = @$this->msg->{MessageField::FROM};
        $fromUid = @$this->msg->{MessageField::TO};

        $this->msg->{MessageField::FROM} = $fromUid;
        $this->msg->{MessageField::TO}   = $toUid;

        // 反转 消息体中的 发送者和申请者
        $this->data = json_encode($this->msg);

        $this->pushMsg($toUid, $fromUid, $this->data);

        // 好友关系保存至 数据库

        $this->friendToMysql($toUid, $fromUid);
    }

    /**
     * 好友关系插入 数据库
     * @param $uid_1
     * @param $uid_2
     * * 抛出异常一般有两种情况
     * * 1. uid_1 或 uid_2 不存在
     * * 2. uid_1 与 uid_2 已经是好友关系 再次建立 好友关系 会出错
     */
    private function friendToMysql($uid_1, $uid_2)
    {
        try {
            $query = $this->sql
                ->setTable(WsMysql::USER_INFO_TABLE)
                ->select(
                    [
                        'uid',
                        'uname',
                    ]
                )
                ->whereOr(
                    [
                        'uid', '=', $uid_1,
                    ],
                    [
                        'uid', '=', $uid_2,
                    ]
                )
                ->getSql();

            // mysql 返回结果是 按 uid 排序的 所以不必处理 (保证好友关系 uid 较小的在前)
            $result = $this->mysql->query($query)->fetchAll();

            $query = $this->sql
                ->setTable(WsMysql::FRIEND)
                ->insert(
                    [
                        'uid_1'      => $result[0]['uid'],
                        'uid_2'      => $result[1]['uid'],
                        'remark_1_2' => $result[1]['uname'],
                        'remark_2_1' => $result[0]['uname'],
                    ]
                )
                ->getSql();

            $this->mysql->exec($query);

        } catch (\PDOException $e) {

            WsLog::log($e->getTraceAsString());
        }
    }
}