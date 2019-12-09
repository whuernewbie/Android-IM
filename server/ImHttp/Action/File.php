<?php


namespace ImHttp\Action;


class File extends Action
{

    private const FILE_PATH = __DIR__ . '/../../File/image';

    /**
     * @return mixed
     * action 动作
     */
    public function run()
    {
        // 下载服务
//        $this->gateway->res->header('Content-Disposition', 'attachment');
//        $this->gateway->res->header('filename', '1.png');
//        $this->gateway->res->sendFile(self::FILE_PATH . '/image.jpg');
        // 在线显示
        $this->gateway->res->header('Content-Type', 'image/jpg');
        $this->gateway->res->sendFile(self::FILE_PATH . '/image.jpg');
    }
}