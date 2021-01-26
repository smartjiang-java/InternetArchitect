
## Redis：最大的特点就是快
### 1：什么是Redis?
Redis是基于内存的，速度比较快，基于键值对（K，V）的，工作线程worker是单线程的，IO操作是多线程的，连接很多，连接池比较大,epoll(NIO)。
k,v有五种类型，有本地方法，可以很好的实现计算向数据移动，IO优化。
整体模型是串行化/原子:并行vs串行

### 2：memcached和redis对比的优势？
--memcached也是k,v的，也是做缓存的，value只能是string，也没有本地方法，取数据的时候只能取全部的数据，把字符串转成数组，再去根据下标取
redis在取数据的时候不需要取全局数据，计算过程在redis内部
1：多个请求来临的时候：epoll多路复用器的作用是：并不是读数据，而是告诉一个事件，知道有哪些可以读
2：redis进行IO读取，单线程串行化，比数据库串行化比较快，数据库保证同步，要不是加锁，要不是使用事务，比redis多了这些操作
弊端：服务端的另外的核心cpu岂不是浪费了？
在同一个IO里读到的是有序的
==》传统：c2 read，计算，写回   ;  c1 read, 计算， 写回  6.0以前
现在： worker线程做计算，其他线程进行读取和写回     6.0以后,不是默认开启的，需要开启
3：计算，也是串行化

### 1：redis场景和五种基本类型
nosql 命令行

JVM：一个线程德成本1MB，可以调小
    1：线程多了调度成本增加，CPU浪费
    2：成本问题

redis是原语的，redis里面默认是16个库，0-15，每个库之间是隔离的。默认进去0号库
select 8：选择8号库

key是字符串，value有五种基本类型  help @
#### string :字符串 ，数值，二进制位操作  
字符串，数值：session，k v缓存，数值计算计算器,ks文件系统(小碎片文件，磁盘IO)，内存
##### 字符串操作：
set k1 00xx nx  表示k1不存在就去设置，创建
set k2 hello xx  只有存在的时候才能去修改，更新
mset k3 a k4 b   创建多个字符串
mget k3 k4
append k1 'world'  k1后面添加
getrange k1  start end    取字符串的start到end  ，
 注意 ：[redis有正反向索引，末尾-1，倒数第二个-2.  第一个是0，第二个是1 ]
setrange k1 start  jiangqikun  从start开始用后面的字符串开始替换，超过的长度自动替换
strlen k1  获得k1的字节长度,一个字符是一个字节，中文是两到三个字节
type k1    k1所在的分组的类型  set k3的时候，查看k3在那个分组，type k3的时候就是分类的类型
object encoding k3  查看k3的编码类型，embstr是string,raw  int 
getset k1 mashibing  会把k1的值替换成后面的值，并把老的值返回
##### 数值操作：抢购，秒杀，详情页，规避并发下对数据库的事务操作，完全由redis的内存操作代替
incr k1  k1加1
incrby k1 22  k1累加上22
decr k1  k1减一
decrby k1 22   k1减去22
incrbufloat  k1 0.5  k1加上浮点数0.5
[redis是二进制安全的，只取字节流,一般redis使用ASCII编码，超过的部分以16进制表示，但是redis-cli --raw
 表示使用相应的编码集，redis里面是没有数据类型的]例如,redis，zookeper,hbase,kafka
注意:ASCII码表：一个字节，从左到右0,1,2,3,4,5,6,7，第一个必须为0，后面的可以随机变化，
    其他的编码叫做补充编码机，不再ASCII进行重编码
##### bitmap操作，二进制位
[bitmap：遵从下面的规则，每次开辟一个字节，超过了自动再次开辟 1个字节有8个二进制位，每一位都有索引，0-7 两个字节0-15]
0XXXXXXX,读一个字节；如果是110XXXXX，那么读三个字节
setbit k1 1 1  k1这个字节的索引1处 为1   0100 0000 在AscII中是@,十进制是64
setbit k1 9 1  9超过了一个字节的范围，因此变成了 0100 0000 0100 0000，@@
bitpos  k1 1 start end  查看k1中的start和end字节索引范围内出现的第一个1/0的位的位置
bitcount k1 start end   统计k1中从start自己索引到end索引范围内的1的总数量
bitop and andkey k1,k1   k1与k2按位与操作存放在andkey里面
bittop or orkey k1,k2    k1与k2按位或操作存放在orkey中
>>使用场景1：用户系统，统计用户登录天数，且窗口随机   用户id为key，天数为位 
   矩阵：zhangsan  01110101010...
        lisi      01001001101...
  存在数据库中，建表数据随着用户的增多变得很庞大。可以用redis解决，每一天对应一个二进制位，一年不到50个字节：节省空间，速度也很快，内存位操作
   setbit zhangsan  1,1  张三第一天登录        
   setbit zhangsan  28,1  张三第28天登录
   每用户占用46KB  * 用户数  
   统计:bitcount zhangsan 0  -1
       strlen zhangsan   统计张三占用的字节
> 使用场景2：公司给客户做反馈活动，2亿客户，6.18要送礼物，需要备多少用户   天数（日期）为key，用户id为位
  矩阵：  20200112   01000110101...
         20200113   01010101010...
   分析：客户分僵尸用户，冷热用户/忠诚客户 ，需要知道前段时间的活跃用户，然后再多备用20%
     统计1-3日的用户登录数目，并去重，1：使用数据库   2：使用redis
   setbit  20200112  用户id 1
   setbit  20200113  用户id 1
   统计这两日的活跃用户数：bitop or orkey 20200112 20200113
                       bitcount destkey 0  -1

#### value是list:双向链表，是有序的，放入的顺序，key持有头结点和尾节点
同向：栈,lpush ,lpop
非同向：队列,rpush,rpop
模拟数组
阻塞队列，单波订阅，FIFO(先进先出)
lpush k1  a b c d e   向链表里面从左边开始存元素，头插法
lpop  k1              弹出左边的第一个元素
rpush k2  a b c d e   向链表里面从由边开始存元素，尾插法
rpop  k2              弹出右边的第一个元素
lrange k1 start end   从左开始弹出k1的start元素索引到end长度的所有元素
lindex k1  temp       取出k1的temp索引处的元素
lset  k1  temp  xxxx  将k1的temp索引处的元素代替成xxxx
lrem  k3  count value  移除k3中的count个value，若count>0.做左边开始;count<0，从右边开始
linsert k3 after value xx  在k3的value后面插入一个元素xx
linsert k3 before value oo  在k3的value前面插入一个元素oo
注意：如果存在两个value，则只在第一个前后操作，而不会涉及到第二个
llen k3                统计k3中有多少个元素
blpop  k4  timeout      阻塞等待pop
ltrim 0，-2 ,删除区间之外的数据，优化redis内存量
[场景：评论列表，消息队列，替代java容器，让JVM无状态]

#### value是hash：hashMap,分治法，key是String
set 张三：： name zahngsan         设置张三的name
get 张三：： name                  取出张三的name
keys 张三*                        取出与张三有关的信息
问题来了：如果字段很多，这会很麻烦，使用hash比较简单
hset 张三 name zhangsan           hash设置一个name
hmeset 张三 age 18 address anhui  设置多个字段
hget  张三 name                   取出张三的name
hmget 张三  name age              取出张三的name ，age
hkeys 张三                        取出张三的所有字段名称
hvals 张三                        取出张三的字段对应的值信息
hgetall 张三                      取出张三的字段及其对应的值信息
hincebyfloat 张三 age 0.5         对张三的age的value增加0.5
[场景：用户详情,商品详情,点赞]

#### set:集合，取出无序，不重复（去重），HashSet
sadd k1 a b c d e   添加数据
smembers k1         详细查看k1的所有元素
srem k1 ooxx  xxoo   移除k1里面的元素
sinter k2 k3         返回k2，k3的交集
sinterstore temp k2 k3   取 k2和k3的交集存储在temp中,减少数据IO
sunion  k2,k3        返回k2,k3的并集
sunionstore temmp k2 k3
sdiff  k2  k3        返回的k2的差集（去掉交集）
srandmember k1 5 -5 10 -10   为正数的时候,取出一个去重的结果集(不超过已有元素数量);负数，取出一个带重复的结果集，一定满足要的数量
     [用于用户抽奖，可重复或者不重复;还可以解决家庭争斗 ]
spop k1               会从k1的元素中随机的弹出一个
[场景：随机事件(抽奖)，多实例，集合操作]

#### zset:sorted set：数据被去重，且实时对元素进行排序，元素有正反向索引
排序：既要给出元素，也要给出分值；分值都为1按照名称的字典顺序
zadd k1 8 apple 2 banana 3 orange   存进去，按照分值排序，物理内存：左小内大
zrange k1 start end                 取出k1中start到end的元素
zrange k1 start end  withscores     取出k1中start到end的元素及其分值
zrangebyscore  k1  mix max          取出k1中分值处于min到max分数之间的元素
zrevange k1  start end              取出k1中start到end的元素，由高到低
zscore k1 value                     取出k1中value的分值
zrank k1 value                      取出k1中value的排名(索引)
zincrby k1 2.5 value                将k1中value的分值增加2.5
集合操作：
zunionstore  unkey  2 k1 k2         对多个集合做并集
zunionstore  unkey  3 k1 k2 k3
zunionstore  unkey1  2 k1 k2  weight 1 0.5       对多个集合做并集,并加上权重
zunionstore  unkey2  2 k1 k2   aggregate max     对多个集合做并集,分数默认求和，这里可以是最大值
[面试题目：排序是怎么实现的?增删改查的速度？   ==底层实现 skip list跳跃表，链表分层，随机的分层]
[应用场景：歌曲排行榜]

### 2:redis的进阶使用
管道：将很多命令打包发送，命令之间使用\n隔开，使得通信的成本变低
#### redis的消息订阅
subscribe  00xx        订阅消息
publish ooxx hello     消费端监听以后，每次推送给xxoo的消息才可以被看到
注意：可以将日期和时间作为分值，消息作为value进行存储

#### redis事务：并不是那么完整，一切都是奔着快去的，没有回滚操作
help @transactions
注意：redis是单线程的,那个客户端的exec先到达的先执行
multi   开启事务 
...
exec    执行事务

watch 监控，类似于cas,监控初始值
set k1 2
watch k1
multi
...     如果其他事务修改了k1的值
exec    k1的值与watch时不同，不会执行

[redis的布隆过滤器]：解决缓存穿透问题：用小的空间解决大量匹配：概率解决问题99%
缓存中没有，直接去数据库访问，其实数据库中也没有 将bitmap和bloom.so放在redis中，使得clienr端比较轻
1：将已有的向bitmap中标记  
2：请求的可能呗误标记
3：一定概率会大量减少放行穿透，成本低
bf.add   k1  aaa
bf.exists k1   aaa
如何使用过滤器：
1:访问redis.io，点击上方Modules
2：点击RedisBloom的小房子，来到github代码地址，右键复制下载连接https://githubcom/RedisBloom//archive/master.zip
3：linux中wget https://github.com/RedisBloom/RedisBloom/archive/master.zip
4: 如果安装了unzip，unzip *.zip ;没安装的话yum install unzip，再去解压
5：进入到解压的文件夹，make
6:cp bloom.so  /usr/local/redis.6.0.9/
7:cd bin 
8：redis-server  --loadmodule  /usr/local/redis-6.0.9/redisbloom.so
9:redis-cli 
10:bf.add  k1 aaa      cf.add k2 bbb
11:bf.exits k1 aaa     cf.exits k2 bbb

1：查询穿透了，数据库中卻不存在，客户端增加redis中的key，value标记不存在，下次再查询，就不走布隆了，直接返回不存在
2：数据库脱离redis， 如果数据库中增加了元素，需要增加元素对布隆的增加

[redis作为缓存和数据库的区别]
做缓存：缓存数据其实不重要,不是全量数据;缓存应该随着访问变化(热数据)，数据可以丢
redis做缓存，减少后端数据库访问压力，那么redis里的数据怎么能随着业务变化，只保留热数据，因为内存大小是有限的，也就是瓶颈
引出：
1：key的有效期
 a:由业务逻辑来推动，比如以天为单位把数据写入数据库， 
    设置过期时间 1,2倒计时，3定时
    1:  set k1 aaa ex 20 :说明这个数据20s后过期
        ttl k1           :查找这个key还有多长时间过期
    2:  set k2  bbb
        expire k1 50      :设置50s后过期
        set k2  ccc
    3:  expireat          定时清除
     [重新访问后过期时间是不会延长的，时间到了就访问不到这个数据了;如果发生了写，会剔除过期时间]
    http://redis.cn/commands/expire.html
    过期判定原理：1:被动访问判定    2：周期轮询时间判定(增量)
        目的：稍微牺牲下内存，但是保住了redis性能为王
 b:业务运转产生，因为内存是有限的，随着访问的变化，就淘汰冷数据
    redis内存多大呢？  maxmemory参数配置最大内存(一般1-10G),内存使用将尽，有回收策略，一般多使用allkeys-lru：加入键的时候，如果过限，
            首先通过LRU算法驱逐最久没有使用的键;和volatile-lru：加入键的时候如果过限，首先从设置了过期时间的键集合中驱逐最久没有使用的键.
            一般看缓存中数据设置过期时间的占比，大量数据设置了过期时间选择后者，反之选择前者。

做数据库:数据不能丢,redis+mysql，会有新的问题：数据一致性，一般不开启强一致性，使用异步一致性   速度+持久性
        因为内存中的东西，都有掉电易失。
   单机持久化:存储层一般都有快照/副本（RDB）,日志(AOF)。
      快照RDB:时点性，做缓存开启这个一般就足够了
         如何实现？  1:阻塞，redis不对外提供服务(不可取)
                   2:非阻塞：边同步，边提供服务,
              管道：1:衔接前一个命令的输出作为后一个命令的输入   2：会触发创建子进程 echo ￥BASHPID | more
              常规思想：进程之间数据是隔离的    进阶思想：父进程数据是可以让子进程看到的。子进程的修改不会破坏父进程;反之亦然
            如果父进程是redis，内存数据比如是10个G，会面临速度和内存空间够不够两个问题---->
            系统调用fork()+内核机制copy ana write():速度相对较快，空间相对较小
            写时复制，创建子进程的时候并不发生复制，使得创建进程变快了，数据，玩的是指针，并不是复制数据，增加指向数据的指针
            主进程对外提供增删改查的服务，子进程读数据向磁盘写文件，是不会修改数据的
         如何触发？  sava,前台阻塞,比如关机维护   bqsave，前台非阻塞 
                   配置文件中给出bqsave的规则：save的这个标识，但是触发的是bqsave
         弊端：1：不支持拉链，永远都只有一个dump.rdb,需要每天定时拉出来，否则会覆盖
              2：丢失数据比较多，并不是实时记录数据的变化，时点与时点之间窗口数据丢失，8点得到一个rdb,9点刚要做，挂机了
         优点：类似java中的序列化，恢复的速度相对快
      日志AOF:
         redis的写操作记录到文件中，丢失数据少,redis中RDB和AOF可以同时开启，则只会使用AOF，4.0之后AOF中包含RDB全量，增加记录的写操作
         弊端：1：体量无限变大，恢复慢，所以一般都会将日志足够小，4.0之前，重写，删除抵消的，合并重复的，最终也是一个纯指令的日志文件;
                 4.0之后，也是重写，将老的数据RDB到AOF文件中，将增量的以指令的方式增加到AOF，AOF是一个混合体，利用了RDB的快和AOF的数据全
         原点：redis是内存数据库，写操作会触发IO，有三个级别的写IO：NO（buffer满了开始刷磁盘，丢数据接近一个buffer），
              always(数据最可靠，顶多丢一条), 每秒(默认的，丢失数据少于一个buffer)

#### redis的集群
   CAP原则：一致性，可用性，分区容错性，三者只能实现二者，不可能三者兼顾
   单机可能有的问题： 1:单点故障    2：容量有限    3:连接数，CPU压力    一般对主做高可用HA
   AKF：x轴，y轴，z轴
        基于x轴：增加备机，全量镜像，解决了单点故障，和部分压力
        基于y轴：对业务功能对数据进行划分，每个redis存储功能数据，由一个redis实例变成多个redis实例，相当于mysql的分库，解决容量有限
        基于z轴：按照优先级逻辑再拆分，解决容量有限和压力
   弊端：1：通过AKF的一变多，写入数据，如何保证数据一致性？
            a:所有节点阻塞，知道数据全部一致，强一致性，如果有一个备机网络什么有问题，会出现超时，容易破坏可用性。同步阻塞方式
            b:容忍部分数据丢失，弱一致性：客户端接收后直接返回，通过异步方式写入其他备机，redis为了快，默认使用异步复制
            c:数据最终一致：主机和备机之间加入kafka(可靠，集群，响应速度足够快)，主机和kafaka同步阻塞，备机从kafka中取数据
                弊端：客户端取数据，可能会取到不一致的数据，强调：强一致性
           
   总共3个，一般集群使用基数台，承担风险更高，成本较低
   1个：通过网络去访问几台服务机器，拿到不同的结果，网络分区，脑裂数据不一致。所以要求一般要过半，但是不是绝对坏事,和分区容忍度有关,比如做缓存。
   2个： 一部分OK，另外一部分不算数，不存在分歧了

   主从复制：在主上，可以知道有多少个从来追随它， 需要人工去维护，因此引出了高可用HA，Redis的Sentinel
         redis-server  ./6379.conf  --replicof  127.0.0.1 6380     6380跟随6379
         redis-server  ./16379.conf  --sentinel                    启动哨兵机制
       主知道有那些从，哨兵只要监控了主，就可以知道有哪些从.发布订阅
   redis哨兵是如何知道其他哨兵的？
       redis自带发布订阅：在监控主的时候检测到从，并开启主的消息订阅发现其他哨兵，哨兵是会修改自己的配置文件的

 

  
     





缓存的击穿

缓存的雪崩

缓存的穿透

缓存的一致性(双写）























































