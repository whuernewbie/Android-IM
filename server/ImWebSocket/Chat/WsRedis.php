<?php


namespace ImWebSocket\Chat;


interface WsRedis
{
    const GET_UID     = 'uid';
    const SOCKET_FD   = 'fd:';
    const USER_PREFIX = 'user:';
}