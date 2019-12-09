<?php

require_once __DIR__ . '/ws.php';
require_once __DIR__ . '/Chat/MessageType.php';
require_once __DIR__ . '/Chat/MessageField.php';
require_once __DIR__ . '/Chat/WsRedis.php';
require_once __DIR__ . '/Chat/Event.php';
require_once __DIR__ . '/Chat/Online.php';
require_once __DIR__ . '/Chat/Offline.php';
require_once __DIR__ . '/Chat/Chat.php';
require_once __DIR__ . '/Chat/SingleChat.php';

require_once __DIR__ . '/../Common/Redis.php';

message_type_init:
\ImWebSocket\Chat\Event::init();

redis_init:
\Common\Redis::init();

/**
 * websocket start
 */
echo 'ws start' . PHP_EOL;
$ws->start();