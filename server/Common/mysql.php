<?php

namespace Common;
/*
 * Mysql 辅助 产生 数据库连接实例
 * 不使用单例模式
 */
class Mysql {
    private static $config;

    private $pdo = null;

    // 初始化数据库配置文件
    public static function init() {
        self::$config = require_once __DIR__ . '/mysql.config.php';
    }

    public function __construct()
    {
        $dsn = 'mysql:dbname=' . self::$config['db'] . ';host=' . self::$config['host'];
        try {
            $this->pdo = new \PDO($dsn, self::$config['user'], self::$config['pass']);

            // 设置 fetch 方式为 索引数组
            $this->pdo->setAttribute(\PDO::ATTR_DEFAULT_FETCH_MODE, \PDO::FETCH_ASSOC);
            // 设置 pdo 错误为 抛出异常 便于调试
            $this->pdo->setAttribute(\PDO::ATTR_ERRMODE, \PDO::ERRMODE_EXCEPTION);
            // 设置 数据库字段为小写
            $this->pdo->setAttribute(\PDO::ATTR_CASE, \PDO::CASE_LOWER);
        } catch (\PDOException $e) {
            echo 'pdo connect mysql fail ' . $e->getMessage() . PHP_EOL;
        }

    }

    /**
     * 返回 数据库连接句柄
     * @return \PDO|null
     */
    public function getInstance() {

        return $this->pdo;
    }
}