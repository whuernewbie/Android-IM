#!/bin/bash
# root 运行

if [[ `id | grep -P 'uid=(\d+)' -o | grep -P '\d+' -o` -ne 0 ]]
then
    echo 'please run as root';
    exit -1;
fi

ulimit -n `cat /proc/sys/fs/file-max`   # 改变进程可持有的最大文件描述符
(cd ./ImHttp/ && php start.php 1111111) && (cd ./ImWebSocket/ && php start.php 2222222);
echo 'server is running';