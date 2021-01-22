## redisdemo
### redis的学习网站：
1.  redis.cn
2.  redis.io
3.  db-engines.com

###  讲课用的图片在IMAGE目录中
```
1.  01Redis前无古人后无来者
2.  02REDIS集群知识点
3.  pos格式的文件可以导入到 https://processon.com/  或者其他的脑图软件
```

###  API代码的学习：
```
1.  redis.io 的client 中有JAVA语言的客户端：jedis、lettuce等可以分别访问他们的github学习
2.  另外是基于spring的使用，主动通过spring.io官网学习spring.data.redis
3.  spring.io中:   https://spring.io/projects/spring-data-redis
```


redis的启动：进入到bin中  ./redis-server  /usr/local/redis-6.0.9/etc/redis.conf
ps aux|grep redis
./ redis-cli
输入密码：auth "123456"
关闭： ps aux|grep redis
kill id号    例如：kill 83850
ps aux|grep redis