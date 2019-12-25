<?php


namespace ImWebSocket\User;


use Common\Mysql;
use ImWebSocket\Chat\WsMysql;
use Tools\Sql;

/**
 * Trait UserInfo
 * @package ImWebSocket\User
 */
trait UserInfo
{
    public function getUserFd($uid) {
        $this->sql = new Sql();
    }

    /**
     * @param $gid
     * @param $uid
     * 用户 群聊信息绑定
     */
    public function bindUserGroup($gid, $uid) {
        // 1. 拿到用户名
        $query = $this->sql
            ->setTable(WsMysql::USER_INFO_TABLE)
            ->select(
                ['uname']
            )
            ->whereAnd(
                [
                    'uid', '=', $uid,
                ]
            )
            ->getSql();

        $remark = $this->mysql->query($query)->fetch()['uname'];

        // 2. 插入关系
        $query = $this->sql
            ->setTable(WsMysql::USER_GROUP_TABLE)
            ->insert(
                [
                    'gid'       => $gid,
                    'uid'       => $uid,
                    'joinTime'  => time(),
                    'remark'    => $remark,
                    'lastMsgId' => 0,           // 群聊刚建立 初始化为 0
                ]
            )
            ->getSql();

        $this->mysql->exec($query);
    }
}