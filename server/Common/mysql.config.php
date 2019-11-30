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
    'db'   => '',				// 数据库名
    'user' => '',				// 用户
    'pass' => ''				// 密码
	'sock' => '/var/lib/mysql/mysql.sock', 	// sock 位置 可以不设置 pdo 方式会自行使用 sock
];
