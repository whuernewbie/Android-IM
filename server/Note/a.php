<?php

/**
 * 协程示例 请注意 最后两个
 */


/**
 * @param int $i
 * @return mixed
 */
function test($i = 0) {

    // 示例 1
    $case[] = function () {
        foreach (range(1, 10) as $v) {
            go(function () {
                co::sleep(5);
                echo 'ok' . PHP_EOL;
            });
        }
    };

    // 示例 2
    $case[] = function () {
        foreach (range(1, 10) as $v) {
            go(function (){
                sleep(5);
                echo 'ok' . PHP_EOL;
            });
        }
    };

    // 示例 3
    $case[] = function () {
        \Swoole\Runtime::enableCoroutine(true);
        foreach (range(1, 10) as $v) {
            go(function () {
                co::sleep(5);
                echo 'ok' . PHP_EOL;
            });
        }
    };

    // 示例 4
    $case[] = function () {
        \Swoole\Runtime::enableCoroutine(true);
        foreach (range(1, 10) as $v) {
            go(function () {
                co::sleep(5);
                echo 'ok' . PHP_EOL;
            });
        }
    };

    // 示例 5
    $case[] = function () {
        foreach (range(1, 10) as $v) {
            go(function () {
                co::sleep(5);
                echo 'ok' . PHP_EOL;
            });
        }

        foreach (range(1, 10) as $v) {
            go(function () {
                sleep(5);
                echo 'ok' . PHP_EOL;
            });
        }
    };

    // 示例 6
    $case[] = function () {
        \Swoole\Runtime::enableCoroutine(true);
        foreach (range(1, 10) as $v) {
            go(function () {
                co::sleep(5);
                echo 'ok' . PHP_EOL;
            });
        }

        foreach (range(1, 10) as $v) {
            go(function () {
                sleep(5);
                echo 'ok' . PHP_EOL;
            });
        }
    };

    return $case[($i % count($case))];
}

$i = 0;
if ($argc == 2) {
    $i = (int)($argv[1]);
    test($i)();
}

?>
COMMAND:
    php a.php number
