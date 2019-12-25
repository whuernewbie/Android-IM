<?php

require_once __DIR__ . '/ws.php';

require_once __DIR__ . '/Trait/UserInfo.php';

require_once __DIR__ . '/Chat/MessageType.php';
require_once __DIR__ . '/Chat/MessageField.php';
require_once __DIR__ . '/Chat/WsRedis.php';
require_once __DIR__ . '/Chat/WsMysql.php';
require_once __DIR__ . '/Chat/Event.php';
require_once __DIR__ . '/Chat/Online.php';
require_once __DIR__ . '/Chat/Offline.php';
require_once __DIR__ . '/Chat/Message.php';
require_once __DIR__ . '/Chat/UserChat.php';
require_once __DIR__ . '/Chat/GroupChat.php';
require_once __DIR__ . '/Chat/Beat.php';
require_once __DIR__ . '/Chat/FriendReq.php';
require_once __DIR__ . '/Chat/GroupReq.php';
require_once __DIR__ . '/Chat/GroupCreate.php';
require_once __DIR__ . '/Chat/GroupInvite.php';


require_once __DIR__ . '/../Common/Mysql.php';
require_once __DIR__ . '/../Common/Redis.php';

require_once __DIR__ . '/../Tools/Sql.php';
require_once __DIR__ . '/../Log/Log.php';

/**
 * 消息类型初始化
 */
message_type_init:
\ImWebSocket\Chat\Event::init();

/**
 * Mysql 配置初始化
 */
mysql_init:
\Common\Mysql::init();

/**
 * Redis 配置初始化
 */
redis_init:
\Common\Redis::init();

/**
 * websocket start
 */
$ws->start();