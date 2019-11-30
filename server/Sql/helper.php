#!/usr/bin/php
<?php

file_exists(__DIR__ . '/init.lock') and die('数据库已初始化，重现初始化请删除 lock 文件' . PHP_EOL);

const HOST = 'localhost'; // win 下 使用 127.0.0.1 unix 下 使用 localhost 会使用 sock
const PORT = 3306;
const USER = 'root';
const PASSWORD = 'root@mlover';
const SOCK = '/var/lib/mysql/mysql.sock';

const DB = 'im';

$mysql = new mysqli(HOST, USER, PASSWORD, null, PORT, SOCK) or die('mysql connect fail');

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

$str = '';
while (!feof($fp)) {
    $line = fgets($fp);
    $line = preg_replace('/\/\*.*\*\//', '', $line);
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

touch(__DIR__ . '/init.lock');
echo 'mysql database init ok' . PHP_EOL;
