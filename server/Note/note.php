<?php

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