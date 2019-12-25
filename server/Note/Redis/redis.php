<?php

const HOST = '127.0.0.1';
const PORT = '6379';

try {
    $redis = new Redis();
    $redis->connect(HOST);

} catch (RedisException $e) {
    echo $e->getMessage() . PHP_EOL;
}

/**
 * 哈希 hash 操作 设置多个键
 */
$redis->hMSet('user:1', ['fd' => 1, 'group:100' => 1000]);
$redis->hGetAll('key');

/**
 * 集合 set 操作
 */
$key = 'user';
// 为 set 中添加元素
$redis->sAdd($key, 1);
$redis->sAddArray($key, [1, 2, 3, 1]);
// 取回 set 中的所有元素
$redis->sMembers($key);
