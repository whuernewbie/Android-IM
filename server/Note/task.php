<?php

use Swoole\Http\Server;
use Swoole\Http\Request;
use Swoole\Http\Response;
use Swoole\Server\Task;

$server = new Server('0.0.0.0', 8899);

$server->set(
    // 启动一个 worker 和 一个 task 避免任务分配到多个进程中
    [
        'worker_num'            => 1,
        'task_worker_num'       => 1,
        'task_enable_coroutine' => true,
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
    // 收到请求就 开启一个任务，观察协程的效果
    $res->header('Content-Type', 'text/html;charset=utf-8');
    $res->end('hello');
    $server->task('hello');
});

/**
 * worker 以及 task 进程创建时 的回调函数，可以用来对特定进程进行全局设置
 * @param $server
 * @param $work_id { 第二个参数 这里没有使用 }
 * * 因为 work_id 可以通过 $server->worker_id 获取到
 */
$server->on('workerStart', function (Server $server) {
    /*
     * 如果是 task 进程 就将此进程协程化
     * * 作用就是 使当前进程不再 受限于 swoole 提供的 api
     */
    if ($server->taskworker) {
        \Swoole\Runtime::enableCoroutine(true);
    }
});

/**
 * 回调函数参数 使用协程特性的回调函数格式
 * 如果不使用 协程，请使用对应的 回调函数
 * @param $server
 * @param $task { 协程化 用来保存 协程信息的对象 }
 * @important 协程化之后，就不需要 使用 go 来创造 协程环境
 */
$server->on('task', function (Server $server, Task $task) {
    /*
     * 注意下面的 sleep 虽然不是 swoole 的协程 api 但是 我们在上面设置了 使 task 以 协程方式工作
     * 所以 不会造成阻塞，可以把上面的 onworkerStart 回调注释掉 观察效果
     * * finish 可以使用多次 每次使用都会触发 worker 进程的 onfinish 回调
     * * return 不可以用来返回 数据
     */
    `sleep 5`;
    $task->finish($task->data . ' ' . microtime(true));
    sleep(5);
    $task->finish(date('H:i:s'));
});

/**
 * 回调函数参数
 * @param $server { 全局管理对象 }
 * @param $task_id {任务 id 注意 不是 task 的进程 id }
 * $server->worker_id 可以获取当前 进程的 id 并非 pid
 * @param $data task 任务完成后的 返回数据
 */
$server->on('finish', function (Server $server, int $task_id, $data) {
    // 显示 id
    echo 'task id ' . $task_id . ' worker id ' . $server->worker_id . PHP_EOL;
    echo $data . PHP_EOL;
});


$server->start();