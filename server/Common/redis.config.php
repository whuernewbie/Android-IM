<?php

namespace Common;

/**
 * * https://github.com/phpredis/phpredis
 * redis 配置文件
 * *nix 下建议使用 sock 避免 tcp/ip
 * localhost 等价于 127.0.0.1 都会使用 tcp/ip 连接
 */

return $redis_config = [
    'host' => 'localhost',
    'sock' => '',
    'port' => 6379,
    'auth' => '',
];