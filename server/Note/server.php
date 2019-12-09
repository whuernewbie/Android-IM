<?php

use Swoole\Http\Server;
use Swoole\Http\Request;
use Swoole\Http\Response;

/**
 * Swoole 在回调函数启动时会自动创建协程环境进行调度
 * 在这种情况下，php 原生的 io 阻塞函数依旧为阻塞态，
 * Swoole 提供的 协程版 redis mysql 客户端 可以避免 io 等待 co::sleep()
 *
 * Swoole 进程模型中，task 进程默认为同步阻塞
 * 'task_enable_coroutine' => true 可以使得 task 使用协程 swoole 提供的 api
 *
 * \Swoole\Runtime::enableCoroutine(true); 的作用是协程化 php 原生的 io 函数
 */

/**
 * 因为协程并非异步回调，实际上依旧是单线程执行，只是将 io 操作的执行向后移
 * io 请求发出后，执行运行，利用 io 在后台的这段时间可以处理很多请求 (协程的原理)
 * swoole 默认不改变 php 提供的阻塞 io
 * 于是在 swoole 的协程环境下 使用 阻塞 io 和同步阻塞的效果相同
 * 协程就变成了 一个等待执行的队列函数
 *
 */
$server = new Server('0.0.0.0', 8899);

$server->set(
    [
        'worker_num'            => 1,
    ]
);

$server->on('connect', function () {
    echo 'connect' . PHP_EOL;
});

/**
 * @param $req Request http 请求对象
 * @param $res Response http 回应对象
 */
$server->on('request', function (Request $req, Response $res) use ($server) {
    $res->header('Content-Type', 'text/html;charset=utf-8');
    $res->end('hello');
    co::sleep(10);
    echo 'end' . PHP_EOL;
});

/**
 * worker 以及 task 进程创建时 的回调函数，可以用来对特定进程进行全局设置
 * @param $server
 * @param $work_id { 第二个参数 这里没有使用 }
 * * 因为 work_id 可以通过 $server->worker_id 获取到
 */
$server->on('workerStart', function (Server $server) {

});

$server->start();