<?php


namespace ImWebSocket\Chat;


interface WsRedis
{
    const SOCKET_FD    = 'fd:';
    const USER_PREFIX  = 'user:';
    const GROUP_PREFIX = 'group:';
}