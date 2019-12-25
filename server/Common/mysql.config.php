<?php

namespace Common;

/*
 * pdo 方式下 localhost 会自动选择 sock  127.0.0.1 会选择 tcp/ip 方式
 * 所以 win 下 pdo 不能使用 localhost
 * 不同于 mysqli
 *
*/
return $mysql_config = [
    'host' => '127.0.0.1',
    'port' => 3306,
    'db'   => 'ims',				        // 数据库名
    'user' => 'root',				        // 用户
    'pass' => 'root',				        // 密码
	'sock' => '/var/lib/mysql/mysql.sock', 	// sock 位置 可以不设置 pdo 方式会自行使用 sock
];