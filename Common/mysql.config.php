<?php

namespace Common;

/*
 * pdo 方式下 localhost 会自动选择 sock  127.0.0.1 会选择 tcp/ip 方式
 * 不同于 mysqli 
 *
*/
return $mysql_config = [
    'host' => 'localhost',
    'port' => 3306,
    'db'   => 'im',
    'user' => 'root',
    'pass' => 'root@mlover',
	'sock' => '/var/lib/mysql/mysql.sock'
];
