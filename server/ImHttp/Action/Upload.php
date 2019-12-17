<?php


namespace ImHttp\Action;


class Upload extends Action
{

    private const UPLOAD_FILE_DIR = __DIR__ . '/../../File/files/';
    /**
     * @return mixed
     * action 动作
     */
    public function run()
    {
        echo 'upload' . PHP_EOL;
        if (empty($this->gateway->req->files)) {

            $this->gateway->notice(['status' => 'error', 'msg' => 'no file']);
            return;
        }

        foreach ($this->gateway->req->files as $file) {
            if (is_uploaded_file($file['tmp_name'])) {
                move_uploaded_file($file['tmp_name'], self::UPLOAD_FILE_DIR . $file['name']);
            }
        }

        $this->gateway->notice(['status' => 'ok', 'msg' => '']);

        return;
    }

    /**
     * check api 参数完整性
     */
    public function check()
    {
        // TODO: Implement check() method.
    }
}