<?php

require_once __DIR__ . '/work.php';
require_once __DIR__ . '/Action/Action.php';
require_once __DIR__ . '/Action/Gateway.php';
require_once __DIR__ . '/Action/Register.php';
require_once __DIR__ . '/Action/Auth.php';
require_once __DIR__ . '/Action/Login.php';

require_once __DIR__ . '/../Common/mysql.php';

// 初始化 mysql 配置
mysql_init:
    \Common\Mysql::init();

/*
 * 启动服务
 */

$server->start();
