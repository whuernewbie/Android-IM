
/* 用户表 */
drop table if exists `user`;

create table if not exists `user` (
    `uid` int primary key auto_increment COMMENT ' 用户 id ',
    `uname` varchar(64) not null COMMENT ' 用户名 ',
    `password` varchar(32) not null COMMENT ' 密码 ',
    `email` varchar(32) not null COMMENT '绑定邮箱',
    `sex` tinyint(1) default null COMMENT '性别',
    `create_time` int not null COMMENT '创建时间',
    `another_info` text COMMENT '额外信息 json 格式保存',
    unique key (email) COMMENT 'email 唯一'
) COMMENT = '用户表';

alter table `user` auto_increment = 100000;

/* 用户注册表 */
drop table if exists `register`;

create table `register` (
    `email` varchar(32) primary key COMMENT '验证邮箱',
    `auth` varchar(10) not null COMMENT '验证码'
) COMMENT = '用户注册验证表';