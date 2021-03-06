<?php

/*
 * 全局协程化 后续使用
 */
\Swoole\Runtime::enableCoroutine(true);

use Swoole\Http\Server;
use Swoole\Http\Request;
use Swoole\Http\Response;
use ImHttp\Action;

const HOST = '0.0.0.0';
const PORT = 8899;

const TIMER = 1000 * 60;                            // 定时任务 1分钟一次

$server = new Server(HOST, PORT);

/*
 * http server 设置
 */
$server->set(
    [
        'worker_num'            => 2,                   // worker 进程数量
        'task_worker_num'       => 1,                   // task  进程数量
        'task_enable_coroutine' => true,                // task 允许协程
        'daemonize'             => 1,                   // 启用守护进程
        'log_file'              => __DIR__ . '/../Log/http.log',    // 设置 log 文件
    ]
);

/*
 * 心跳检测
 */
$server->set(
    [
        'heartbeat_check_interval' => 30,
        'heartbeat_idle_time' => 60,
    ]
);

/*
 * http 连接建立 (tcp 连接建立时就会触发)
 * 继承于 server
 */
$server->on('connect', function (Server $server, int $fd, int $reactor_id) {
    // do nothing
});

/*
 * http 请求触发
 */
$server->on('request', function (Request $req, Response $res) use ($server) {
    // 处理 浏览器 对 favicon 的请求
    if ('/favicon.ico' === $req->server['request_uri']) {
        return;
    }

    // 交给 Gateway 处理
    (new Action\Gateway($server, $req, $res))->run();

});

$server->on('close', function (Server $server, $fd, $reactor_id) {
    // do nothing
});

/*
 * master  start 回调
 * 设置 master 进程名
 */
$server->on('Start', function (Server $server) {
    cli_set_process_title('php http master');
});

/*
 * manager 进程启动回调
 * 设置 manager 进程名
 */
$server->on('managerStart', function () {
    cli_set_process_title('php http manager');
});
/*
 * workerStart 回调
 * 设置 worker 以及 task 进程名
 */
$server->on('workerStart', function (Server $server) {
    // 判断是否是 task 进程
    if ($server->taskworker) {
        cli_set_process_title('php http task ' . $server->worker_id);

    } else {
        cli_set_process_title('php http worker ' . $server->worker_id);

        // 第一个 worker 进程执行定时任务
        if ($server->worker_id === 0) {
            $server->tick(TIMER, ['\ImHttp\Task', 'time']);
            $server->tick(TIMER * 60 * 24, ['\ImHttp\task', 'cleanLog']);
        }
    }

});

/*
 * task 回调 分发任务
 */
$server->on('Task', function (Server $server, \Swoole\Server\Task $task) {
    \ImHttp\Task::run($task);
});

/*
 * task 任务完成回调
 * 可以不设置回调函数，则不处理 task 结果
 */
$server->on('finish', function (Server $server, $task_id, $data) {
    // do nothing
});