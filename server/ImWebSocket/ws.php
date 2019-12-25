<?php

/**
 * 全局允许协程
 * 所有进程 均避免 io
 */
\Swoole\Runtime::enableCoroutine(true);

use Swoole\Http\Request as Request;
use Swoole\WebSocket\Server as WebSocket;
use ImWebSocket\Chat\Event;

$ws = new WebSocket('0.0.0.0', 8080);

$ws->set(
    [
        'worker_num'            => 2,                               // worker 进程数量
//        'task_worker_num'       => 1,                               // task  进程数量
//        'task_enable_coroutine' => true,                            // task 允许协程
        'daemonize'             => 1,                               // 启用守护进程
        'log_file'              => __DIR__ . '/../Log/ws.log',      // 设置 log 文件
//        'websocket_subprotocol' => 'Talk Talk',                     // 设置子协议 （验证）

        'heartbeat_check_interval' => 30,                           // 心跳检测 60秒关闭连接
        'heartbeat_idle_time' => 60,
    ]
);

/**
 * open 回调
 * 重要操作
 */
$ws->on('open', function (WebSocket $server, Request $req){

    Event::online($server, $req);

});

/**
 * 事件处理
 */
$ws->on('message', function (WebSocket $server, \Swoole\WebSocket\Frame $frame) {

    Event::message($server, $frame);

});

/**
 * close 回调
 * 关键操作
 */
$ws->on('close', function (WebSocket $server, int $fd) {

    Event::offline($server, $fd);

});


/*
 * master  start 回调
 * 设置 master 进程名
 */
$ws->on('Start', function (WebSocket $server) {

    cli_set_process_title('php ws master');

});

/*
 * manager 进程启动回调
 * 设置 manager 进程名
 */
$ws->on('managerStart', function () {

    cli_set_process_title('php ws manager');

});
/*
 * workerStart 回调
 * 设置 worker 以及 task 进程名
 */
$ws->on('workerStart', function (WebSocket $server) {
    // 判断是否是 task 进程
    if ($server->taskworker) {
        cli_set_process_title('php ws task ' . $server->worker_id);
    }
    else {
        cli_set_process_title('php ws worker ' . $server->worker_id);
    }

});