<?php


namespace ImWebSocket\Chat;


use Log\Log;

/**
 * Class Beat
 * @package ImWebSocket\Chat
 * 心跳处理
 */
class Beat extends Message
{

    /**
     * null 函数
     */
    public function run()
    {
//        Log::log('beat');
        return;
    }
}