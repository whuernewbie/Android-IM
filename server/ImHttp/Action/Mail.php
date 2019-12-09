<?php


namespace ImHttp\Action;

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\SMTP;
use PHPMailer\PHPMailer\Exception;

require_once __DIR__ . '/mail/vendor/autoload.php';

class Mail
{
    public const  PATH     = __DIR__ . '/Mail.php';
    public const  OK       = 'ok';
    public const  ERROR    = 'error';
    public const  REGISTER = 'register';        // 注册
    public const  RESET    = 'reset';           // 重置

    private const MAIL = '2339738942@qq.com';
    private $tomail;
    private $mailer;
    private $auth;
    private $type;

    public function __construct($tomail, $auth, $type)
    {
        $this->tomail = $tomail;
        $this->auth   = $auth;
        $this->mailer = new PHPMailer(true);
        $this->type   = $type;
    }

    public function send()
    {
        try {
            $this->mailer->CharSet   = "UTF-8";                     // 设定邮件编码
            $this->mailer->SMTPDebug = 0;                        // 调试模式输出
            $this->mailer->isSMTP();                             // 使用SMTP
            $this->mailer->Host       = 'smtp.qq.com';                // SMTP服务器
            $this->mailer->SMTPAuth   = true;                      // 允许 SMTP 认证
            $this->mailer->Username   = self::MAIL;                // SMTP 用户名  即邮箱的用户名
            $this->mailer->Password   = 'fctethnstdnpdjhg';             // SMTP 密码  部分邮箱是授权码(例如163邮箱)
            $this->mailer->SMTPSecure = 'ssl';                    // 允许 TLS 或者ssl协议
            $this->mailer->Port       = 465;                            // 服务器端口 25 或者465 具体要看邮箱服务器支持

            $this->mailer->setFrom(self::MAIL, 'Talk Talk');  //发件人
            $this->mailer->addAddress($this->tomail, $this->tomail);  // 收件人

            $this->mailer->addReplyTo('2339738942@qq.com', 'info'); //回复的时候回复给哪个邮箱 建议和发件人一致
            $this->mailer->isHTML(true);

            $register = <<<EOF
<h3>您正在注册 Talk Talk 账号</h3><p>验证码为 <span style="color: red">{$this->auth}</span> !</p>
EOF;
            $reset    = <<<EOF
<h3>您正在重置 Talk Talk 账号密码</h3><p>验证码为 <span style="color: red">{$this->auth}</span> !</p>
EOF;
            if (self::REGISTER === $this->type) {
                $body = $register;
            } else if (self::RESET === $this->type) {
                $body = $reset;
            } else {
                echo self::ERROR;
                return;
            }


            $this->mailer->Subject = 'Talk Talk';
            $this->mailer->Body    = $body;
            $this->mailer->AltBody = '您正在注册 Talk Talk 账号, 验证码为 ' . $this->auth . ' !';
            $this->mailer->send();
            echo self::OK;
        } catch (\Exception $e) {
            echo self::ERROR;
        }
    }
}

if ($argc !== 4) {
    echo '参数错误' . PHP_EOL;
}
else {
    (new Mail($argv[1], $argv[2], $argv[3]))->send();
}