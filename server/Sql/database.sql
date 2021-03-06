
/* 此文件中只允许单行注释 */

/* 用户表 */
drop table if exists `user`;

create table if not exists `user` (
    `uid` int primary key auto_increment COMMENT ' 用户 id ',
    `uname` varchar(64) not null COMMENT ' 用户名 ',
    `password` varchar(32) not null COMMENT ' 密码 ',
    `email` varchar(32) not null COMMENT '绑定邮箱',
    `sex` tinyint default null COMMENT '性别',
    `age` tinyint default null COMMENT '年龄',
    `createTime` int not null COMMENT '创建时间',
    `imageUrl` varchar(128) default null COMMENT '头像',
    `moreInfo` text COMMENT '额外信息 json 格式保存',
    unique key (email) COMMENT 'email 唯一'
) COMMENT = '用户表';

alter table `user` auto_increment = 1000000;

/* 注册 管理员 */
insert into `user` values (
       1000000,
       '客服',
       'admin',
       'admin@admin.com',
       null,
       null,
       unix_timestamp(now()),
       null,
       null
);

/* 用户注册表 */
drop table if exists `register`;

create table `register` (
    `email` varchar(32) primary key COMMENT '验证邮箱',
    `auth` varchar(10) not null COMMENT '验证码',
    `expire_time` int not null COMMENT '失效时间'
) COMMENT = '用户注册验证表';

/* 重置密码信息表 */
drop table if exists `found_lost`;

create table if not exists `found_lost` (
    `email` varchar(32) primary key COMMENT '验证邮箱',
    `auth` varchar(10) not null COMMENT '验证码',
    `expire_time` int not null COMMENT '失效时间'
) COMMENT = '重置密码信息表';

/*  好友关系表    */
drop table if exists `friend`;

create table if not exists `friend` (
    `uid_1` int not null COMMENT '用户 1',
    `uid_2` int not null COMMENT '用户 2',
    `remark_1_2` varchar(64) COMMENT '用户1 对 用户2 的备注',
    `remark_2_1` varchar(64) COMMENT '用户2 对 用户1 的备注',
    `group_1_2`  varchar(64) COMMENT '用户1 对 用户2 的分组',
    `group_2_1`  varchar(64) COMMENT '用户2 对 用户1 的分组',
    primary key(`uid_1`, `uid_2`) COMMENT '好友关系 唯一性'
) COMMENT = '好友关系表';


/* 私聊离线消息表  */
drop table if exists `pri_msg`;

create table if not exists `pri_msg` (
    `msgTo` int not null COMMENT '接受者 id',
    `msgFrom` int not null COMMENT '发送者 id',
    `msg` text not null COMMENT '消息 json 格式',
    index (`msgTo`) COMMENT '建立索引'
) COMMENT '私聊离线消息表';


/* 群聊信息表 */
drop table if exists `group_info`;

create table if not exists `group_info` (
    `gid` int primary key auto_increment COMMENT '群聊 id 自增',
    `owner` int not null COMMENT '群主',
    `gname` varchar(64) not null COMMENT '群名称',
    `headImageUrl` varchar(128) default null COMMENT '群头像',
    `createTime` int not null COMMENT '建群时间',
    `number` int not null COMMENT '群人数'
) COMMENT = '群聊信息表';

/* 设置群聊自增初值 */
alter table `group_info` auto_increment = 1000;

/* 群聊好友关系表 */
drop table if exists `group_person`;

create table if not exists `group_person` (
    `gid` int not null COMMENT '群聊 id',
    `uid` int not null COMMENT '用户 id',
    `joinTime` int not null COMMENT '加群时间',
    `remark` varchar(64) COMMENT '群聊备注',
    `lastMsgId` int not null COMMENT '已读的当前群聊最后一条消息 id',
    primary key(`gid`, `uid`)
) COMMENT = '群聊 用户关系表';

create table if not exists `share` (
    `uid` int not null COMMENT '用户 id',
    `uname` varchar(64) not null comment '用户名',
    `content` text not null COMMENT '动态内容',
    `address` varchar(1024) not null COMMENT '地址',
    `time` int not null COMMENT '发布时间',
    `locationx` double not null COMMENT '经度',
    `locationy` double not null COMMENT '维度',
    index (`uid`) COMMENT '建立索引'
) COMMENT = '动态表';

;;
/* 群聊分表设计 */
create table `group_gid` (
    `mid` int primary key auto_increment COMMENT '消息 id, 自增',
    `msgFrom` int not null COMMENT '发送者 id',
    `msg` text not null COMMENT '群聊消息'
) COMMENT = '群聊离线消息列表';
