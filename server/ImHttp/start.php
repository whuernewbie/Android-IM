<?php

require_once __DIR__ . '/work.php';
require_once __DIR__ . '/Action/Api.php';
require_once __DIR__ . '/Action/Action.php';
require_once __DIR__ . '/Action/Gateway.php';
require_once __DIR__ . '/Action/Register.php';
require_once __DIR__ . '/Action/Auth.php';
require_once __DIR__ . '/Action/Login.php';
require_once __DIR__ . '/Action/Search.php';
require_once __DIR__ . '/Action/Reset.php';
require_once __DIR__ . '/Action/Update.php';
require_once __DIR__ . '/Action/Mail.php';
require_once __DIR__ . '/Action/File.php';

require_once __DIR__ . '/../Common/Mysql.php';
require_once __DIR__ . '/../Common/Redis.php';
require_once __DIR__ . '/../Tools/Sql.php';

// api 初始化

\ImHttp\Action\Gateway::init();

// 初始化 mysql 与 redis
mysql_init:
    \Common\Mysql::init();

redis_init:
    \Common\Redis::init();

/*
 * 启动服务
 */

$server->start();