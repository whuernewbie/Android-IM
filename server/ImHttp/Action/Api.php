<?php


namespace ImHttp\Action;


/**
 * Interface Api
 * 作为 enum 类型，提供 api 常量
 * @package ImHttp\Action
 */
interface Api
{
    public const REGISTER = 'register';
    public const AUTH     = 'auth';
    public const LOGIN    = 'login';
    public const SEARCH   = 'search';
    public const RESET    = 'reset';
    public const FILE     = 'file';
    public const UPDATE   = 'update';
    public const UPLOAD   = 'upload';
    public const SHARE    = 'share';
    //    public const CREATE_GROUP = 'creategroup';
}