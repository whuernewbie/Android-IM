<?php

/**
 * ws 模块需要在推送时需要考虑 客户端突然断线情况
 * 原因分析：
 * 服务端收到客户端请求，操作 数据库 对于 io 操作来说，是耗时操作，虽然少量用户 看不出 花费太多时间
 * 但是 我们可以模拟出这种效果
 * 1. 我们使用 sleep 让服务端在 push 之前停住，在这个阶段让客户端 断线
 * 2. 使用 node 测试，在 ws 建立后 5ms 即关闭
 */

/**
 * array_walk 不能修改 key 只能修改 value
 */

/**
 * 关联数组 对 key 加前缀
 * 因为 关联数组中 key 一定不同
 * @param array $array
 * @param string $prefix
 */

function add_prefix_for_key(array &$array, $prefix = 'prefix-') {
    $keys = array_keys($array);
    // 修改 key 即可
    array_walk($keys, function (&$v) use ($prefix) {
        $v = $prefix . $v;
    });
    $array = array_combine($keys, $array);
}

(function() {
    $a = range(1, 5);
    var_dump($a);
    add_prefix_for_key($a);
    var_dump($a);
})();