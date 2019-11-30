<?php

use Swoole\Http\Request as Request;
use Swoole\WebSocket\Server as WebSocket;

$ws = new WebSocket('0.0.0.0', 8080);

$ws->on('open', function (WebSocket $server, Request $req){
    // Do nothing
});

/**
 * websocket message 处理
 * * 检测用户 真实性
 * * 绑定 fd 与 uid 双向绑定
 */
$ws->on('message', function (WebSocket $server, \Swoole\WebSocket\Frame $req) {
    $msg = json_decode($req->data);
    global $redis;
    //  认证 绑定 user 与 fd
    if ('auth' === $msg->messageType) {
        $redis->set('user:' . $msg->user, 'fd:' . $req->fd);
        $redis->set('fd:' . $req->fd, 'user:' . $msg->user);
        $server->push($req->fd, 'auth ok');
    }
    else if ('beat' === $msg->messageType) {
        // do nothing
    }
    else {
        $from = $msg->sendUserId;
        $to = $msg->receiverId;
        $check = $redis->get('user:' . $to);
        if (false !== $check) {
            $tofd = explode(':', $check)[1];
            $server->push($tofd, $from . ' send ' . $msg->message);
        }
        else {
			$fromfd = explode(':', $redis->get('user:' . $from))[1];
            $server->push($fromfd, '目标用户不在线');
        }

    }
});

/**
 * websocket close 连接断开处理
 * 找到 fd 对应的 uid redis 删除此键
 * 标记为离线
 */
$ws->on('close', function (WebSocket $server, int $fd) {
    global $redis;
    // 关闭连接 时 消除 redis 记录
    $user = explode(':', $redis->get('fd:' . $fd))[1];
    $redis->del('fd:' . $fd, 'user:' . $user);
});

/**
 * 心跳检测
 */
//$ws->set(
//    [
//        'heartbeat_check_interval' => 60,
//        'heartbeat_idle_time' => 300,
//    ]
//);

$ws->on('workerStart', function () use ($ws) {
    // 多进程 每个进程设置一个 mysql 和 redis 连接 避免多次实例化
    $GLOBALS['redis'] = new Redis();
    $GLOBALS['redis']->pconnect('127.0.0.1');
    $GLOBALS['redis']->auth('');
//    $GLOBALS['mysql'] = new PDO('mysql:dbname=im;host=127.0.0.1', 'root', 'root');
//    echo $ws->worker_pid . PHP_EOL;
});

echo 'severing' . PHP_EOL;
$ws->start();
