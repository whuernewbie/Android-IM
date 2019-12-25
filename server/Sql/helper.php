<?php

/**
 * 文件说明
 * 使用 .sql 文件 辅助创建数据库表
 */

/*
 * mysql 方式
 * *nix 系统中 主机 设为 localhost 则会使用 sock 通信 避免 tcp/ip
 *  主机设为 127.0.0.1 则利用 tcp/ip 通信
 * win 系统中 localhost 和 127.0.0.1 一样 都是 利用 tcp/ip
 *
 * * *nix 下建议 使用 sock 通信
 */

file_exists(__DIR__ . '/init.lock') and die('数据库已初始化，重新初始化请删除 lock 文件' . PHP_EOL);

const HOST = 'localhost'; // win 下 使用 127.0.0.1 unix 下 使用 localhost 会使用 sock
const PORT = 3306;
const USER = '';
const PASSWORD = '';
const SOCK = '/var/lib/mysql/mysql.sock';
const DB = '';

// 平台判断

if (':' === PATH_SEPARATOR) {   // linux 平台
    $mysql = new mysqli(HOST, USER, PASSWORD, null, PORT, SOCK) or die('mysql connect fail');
}
else {                          // win 平台
    $mysql = new mysqli(HOST, USER, PASSWORD) or die('mysql connect fail');
}


// 删除数据库 DB
first:
$sql = 'drop database if exists ' . DB;
$mysql->query($sql) or die('数据库删除失败');

second:
// 创建并选择数据库进行操作
$sql = 'create database ' . DB;
$mysql->query($sql) or die('创建数据库失败');
$mysql->select_db(DB) or die ('选择数据库 error');

$fp = fopen(__DIR__ . '/database.sql', 'r');

/**
 *  读取 sql 文件 获取语句并执行
 */
$str = '';
while (!feof($fp)) {
    $line = fgets($fp);
    if ($line[0] === ';') break;                            // 终止符
    $line = preg_replace('/\/\*.*\*\//', '', $line);        // 过滤注释
    $str .= $line;
}

$sql = explode(';', $str);

array_walk($sql, function (&$v) {
    $v = trim($v);
});

// 弹出 最后一个 空项
array_pop($sql);

foreach ($sql as &$v) {
    $mysql->query($v) or die('命令执行出错 ' . $v);
}
unset($v);

touch(__DIR__ . '/init.lock');      // 创建 lock 文件

echo 'mysql database init ok' . PHP_EOL;