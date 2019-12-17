<?php


namespace Log;


class Log
{
    private const FILE_PATH = __DIR__ . '/log/im.log';

    /**
     * @param string $str
     * 日志记录
     */
    public static final function log(string $str) {
        try {
            $now = (new \DateTime())->format('Y-m-d H:i:s u');
        } catch (\Exception $e) {
            $now = date('Y-m-d H:i:s');
        }

        $time = '[' . $now . '] ';
        file_put_contents(static::FILE_PATH, $time. $str . "\n", FILE_APPEND);
    }
}

final class WsLog extends Log
{
    public const FILE_PATH = __DIR__ . '/log/ws.log';
}

final class HttpLog extends Log
{
    public const FILE_PATH = __DIR__ . '/log/http.log';
}