<?php


namespace Tools;


/**
 * Class Sql 组合 sql 语句
 * @package Common
 */
class Sql
{
    /**
     * mysql 数据库 内置的函数以及特殊值 只限于 insert 不处理
     */
    private const INNER = [
        null,                                   // null 值
        'unix_timestamp(now())',                // 时间戳
    ];
    /**
     * @var string
     */
    private $sql = '';
    /**
     * @var string 选择的数据表
     */
    private $table = '';

    /**
     * Sql constructor.
     */
    public function __construct()
    {

    }

    /**
     * reset sql 对象，重用生成语句
     */
    public function reset()
    {
        $this->sql   = null;
        $this->table = null;
        return $this;
    }

    /**
     * 返回生成的 sql 语句
     * 执行后并清空 sql 便于对象 反复 利用
     * @return string
     */
    public function getSql()
    {
        $sql = $this->sql;
        $this->reset();
        return $sql;
    }

    /**
     * 实现 pdo quote
     * 将 ' 转化为 \'
     * @param string $value
     * @return string
     */
    public static function quote(string $value)
    {
        $search  = array("\\", "\x00", "\n", "\r", "'", '"', "\x1a");
        $replace = array("\\\\", "\\0", "\\n", "\\r", "\'", '\"', "\\Z");

        return '\'' . str_replace($search, $replace, $value) . '\'';
    }

    /**
     * @param string $table
     * @return $this
     */
    public function setTable(string $table)
    {
        $this->table = "`$table`";
        return $this;
    }

    /**
     * 组合 insert 语句
     * @param array $kv
     * @return Sql
     */
    public function insert(array $kv)
    {
        /**
         * 实现 insert 语句
         * 1. 指定 key value 对应插入 $kv 为关联数组
         * 2. 全项 插入 $kv 为索引数组
         */

        if (!array_key_exists(0, $kv)) {            // case 1
            // 为 key 添加 ``
            $keys = array_keys($kv);
            array_walk($keys, function (&$v) {
                $v = "`$v`";
            });

            // 为 value 添加 '' 并过滤
            $values = array_values($kv);
            array_walk($values, function (&$v) {
                // 处理 数据库内置语句
                if (in_array($v, self::INNER, true)) {
                    null === $v ? $v = 'null' : 0;          // null 值特殊处理
                } else {
                    $v = self::quote($v);
                }
            });

            // 组合
            $this->sql = 'insert into ' . $this->table . ' (' . implode(', ', $keys) . ') values (' . implode(', ', $values) . ')';

        } else {
            // 为 value 添加 '' 并过滤
            $values = array_values($kv);
            array_walk($values, function (&$v) {
                // 处理 数据库内置语句
                if (in_array($v, self::INNER, true)) {
                    null === $v ? $v = 'null' : 0;          // null 值特殊处理
                } else {
                    $v = self::quote($v);
                }
            });

            // 组合
            $this->sql = 'insert into ' . $this->table . ' values (' . implode(', ', $values) . ')';
        }

        return $this;
    }

    /**
     * 删除语句
     */
    public function delete()
    {
        $this->sql = 'delete from ' . $this->table;

        return $this;
    }

    /**
     * select 语句
     * @param array $field
     * @return Sql
     */
    public function select(array $field)
    {
        /**
         * select * from `table`;
         * select `id`, `name` form `table`
         */

        array_walk($field, function (&$v) {
            $v = "`$v`";                            // 为每个 字段加 ``
        });

        $select    = implode(', ', $field);
        $select    = 'select ' . $select;
        $this->sql = $select . ' from ' . $this->table;
        return $this;
    }

    /**
     * update 语句
     * @param array $kv
     * @return Sql
     */
    public function update(array $kv)
    {
        /**
         * update table set `filed` = `new value`
         */

        array_walk($kv, function (&$v, &$k) {
            $v = self::quote($v);                 // 处理 value quote方式
            $k = "`$k`";                            // 字段处理
            $v = $k . ' = ' . $v;                   // 组合语句
        });

        $this->sql = "update {$this->table} set " . implode(', ', $kv);

        return $this;
    }

    /**
     * @param array ...$condition
     * @return $this
     */
    public function whereAnd(array ...$condition)
    {
        /**
         * where `id` = 1 and `name` like '%mysql%';
         * $condition 格式
         * [key, compare, value]        // 3个数据的数组
         */
        array_walk($condition, function (&$v) {
            $v[0] = "`$v[0]`";                              // 为 key 添加 ``
            $v[2] = self::quote($v[2]);                    // 为 value 添加 ''
            $v    = implode(' ', $v);                          // 组合条件
        });
        $where     = implode(' and ', $condition);
        $this->sql .= ' where ' . $where;                   // 组合完整语句

        return $this;
    }

    /**
     * @param array ...$condition 条件
     * @return Sql
     */
    public function whereOr(array ...$condition)
    {

        /*
         * 同上 whereAnd
         */
        array_walk($condition, function (&$v) {
            $v[0] = "`$v[0]`";                              // 为 key 添加 ``
            $v[2] = self::quote($v[2]);                    // 为 value 添加 ''
            $v    = implode(' ', $v);                          // 组合条件
        });
        $where     = implode(' or ', $condition);
        $this->sql .= ' where ' . $where;                   // 组合完整语句

        return $this;
    }

    /**
     * 重载魔术方法
     * @return string
     */
    public function __toString()
    {
        return $this->getSql();
    }

    /**
     * 重载实现函数调用
     * @return string
     */
    public function __invoke()
    {
        return $this->getSql();
    }
}