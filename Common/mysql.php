<?php

namespace Common;

class Mysql {
    private static $config;

    private $pdo = null;

    public static function init() {
        self::$config = require_once __DIR__ . '/mysql.config.php';
    }

    public function __construct()
    {
        $dsn = 'mysql:dbname=' . self::$config['db'] . ';host=' . self::$config['host'];
        try {
            $this->pdo = new \PDO($dsn, self::$config['user'], self::$config['pass']);

            // 设置 异常 以及 fetch 方式为 索引数组
            $this->pdo->setAttribute(\PDO::ATTR_DEFAULT_FETCH_MODE, \PDO::FETCH_ASSOC);
            $this->pdo->setAttribute(\PDO::ATTR_ERRMODE, \PDO::ERRMODE_EXCEPTION);
        } catch (\PDOException $e) {
            echo 'pdo connect mysql fail ' . $e->getMessage();
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
