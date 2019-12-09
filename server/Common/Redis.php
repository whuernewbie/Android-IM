<?php


namespace Common;


/**
 * Class Redis
 * @package Common
 *
 * 同 mysql 一样 不使用 单例模式
 *
 */
class Redis
{
    private static $config;

    private $redis;
    /**
     *  加载 redis 配置文件
     */
    public static function init() {
        self::$config = require_once __DIR__ . '/redis.config.php';
    }

    /**
     * Redis constructor.
     * 初始化 $redis 建立连接并进行认证
     */
    public function __construct() {
        $this->redis = new \Redis();
        try {
            // 判断使用 sock 还是 tcp 进行 连接
            if (!empty(self::$config['sock'])) {
                $this->redis->connect(self::$config['sock']);
            }
            else {
                $this->redis->connect(self::$config['host']);
            }

            // 判断是否有密码认证
            if (!empty(self::$config['auth'])) {
                $ok = $this->redis->auth(self::$config['auth']);
                if (!$ok) {
                    throw new \Exception('redis auth error');
                }
            }
        } catch (\Exception $e) {
            echo $e->getMessage() . PHP_EOL;
        }

    }

    /**
     * @return \Redis
     */
    public function getInstance() {

        return $this->redis;
    }
}