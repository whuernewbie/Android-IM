<?php


namespace ImHttp\Action;

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\SMTP;
use PHPMailer\PHPMailer\Exception;

require_once __DIR__ . '/mail/vendor/autoload.php';

/*
 * Mail 供其他接口使用
 * 直接以子进程方式调用
 * 协程会自动避免 io
 *
 */

class Mail
{
	/*
	 * 发送者邮箱
	 */
    private const mail = '';

	/*
	 * 接受者邮箱
	 */
    private $tomail;

	/*
	 * mail 对象
	 */
    private $mailer;

	/*
	 * 验证码
	 */
    private $auth;

    public function __construct($tomail, $auth)
    {
        $this->tomail = $tomail;
        $this->auth   = $auth;
        $this->mailer = new PHPMailer(true);
    }

    public function send()
    {
        try {
            $this->mailer->CharSet   = "UTF-8";                     // 设定邮件编码
            $this->mailer->SMTPDebug = 0;                        // 调试模式输出
            $this->mailer->isSMTP();                             // 使用SMTP
            $this->mailer->Host       = 'smtp.qq.com';                // SMTP服务器
            $this->mailer->SMTPAuth   = true;                      // 允许 SMTP 认证
            $this->mailer->Username   = self::mail;                // SMTP 用户名  即邮箱的用户名
            $this->mailer->Password   = '';             // SMTP 密码  部分邮箱是授权码(例如163邮箱)
            $this->mailer->SMTPSecure = 'ssl';                    // 允许 TLS 或者ssl协议
            $this->mailer->Port       = 465;                            // 服务器端口 25 或者465 具体要看邮箱服务器支持

            $this->mailer->setFrom(self::mail, 'TalkTalk');  //发件人
            $this->mailer->addAddress($this->tomail, $this->tomail);  // 收件人

            $this->mailer->addReplyTo(self::mail, 'info'); //回复的时候回复给哪个邮箱 建议和发件人一致
            $this->mailer->isHTML(true);

            $body = <<<EOF
<h3>您正在注册 TalkTalk 账号</h3><p>验证码为 <span style="color: red">{$this->auth}</span> !</p>
EOF;
            $this->mailer->Subject = 'Talk Talk';
            $this->mailer->Body    = $body;
            $this->mailer->AltBody = '您正在注册 TalkTalk 账号, 验证码为 ' . $this->auth . ' !';
            $this->mailer->send();
            echo 'ok';
        } catch (\Exception $e) {
            echo $e->getMessage();
        }
    }
}

if ($argc !== 3) {
    echo '参数错误' . PHP_EOL;
}
else {
    (new Mail($argv[1], $argv[2]))->send();
}
