
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
publish ooxx hello     消费端监听以后，每次推送给xxoo的消息才可以被看到,先要订阅
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
##### 复制集群:每个redis的数据最终一致,但是不是实时一致
   CAP原则：一致性，可用性，分区容错性，三者只能实现二者，止不可能三者兼顾
   单机可能有的问题： 1:单点故障    2：容量有限    3:连接数，CPU压力    一般对主做高可用HA
   AKF：x轴，y轴，z轴
        基于x轴：增加备机，全量镜像，解决了单点故障，和部分压力
        基于y轴：对业务功能对数据进行划分，每个redis存储功能数据，由一个redis实例变成多个redis实例，相当于mysql的分库，解决容量有限
        基于z轴：按照优先级逻辑再拆分，解决容量有限和压力
   弊端：1：通过AKF的一变多，写入数据，如何保证数据一致性？
            a:所有节点阻塞，直到数据全部一致，强一致性，如果有一个备机网络什么有问题，会出现超时，容易破坏可用性。同步阻塞方式
            b:容忍部分数据丢失，弱一致性：客户端接收后直接返回，通过异步方式写入其他备机，redis为了快，默认使用异步复制
            c:数据最终一致：主机和备机之间加入kafka(可靠，集群，响应速度足够快)，主机和kafaka同步阻塞，备机从kafka中取数据
                弊端：客户端取数据，可能会取到不一致的数据，强调：强一致性
           
   总共3个，一般集群使用基数台，承担风险更高，成本较低
   1个：通过网络去访问几台服务机器，拿到不同的结果，网络分区，脑裂数据不一致。所以要求一般要过半，但是不是绝对坏事,和分区容忍度有关,比如做缓存。
   2个： 一部分OK，另外一部分不算数，不存在分歧了

   主从复制：在主上，可以知道有多少个从来追随它， 需要人工去维护，因此引出了高可用HA，Redis的Sentinel
         redis-server  ./6379.conf  --replicaof  127.0.0.1 6380     6380跟随6379
         redis-server  ./16379.conf  --sentinel                    启动哨兵机制
       主知道有那些从，哨兵只要监控了主，就可以知道有哪些从.发布订阅
   redis哨兵是如何知道其他哨兵的？
       redis自带发布订阅：在监控主的时候检测到从，并开启主的消息订阅发现其他哨兵，哨兵是会修改自己的配置文件的

2^32:一致性哈希算法将整个哈希值空间映射成一个虚拟的圆环，整个哈希空间的取值范围为0~232-1。整个空间按顺时针方向组织。
0~2^32-1在零点中方向重合。
虚拟节点解决数据倾斜的问题：两个redis对应两个物理界点,可能出现数据集体往一个redis中存的情况.所以可以将redis的ip+一个数字十次
                        并hash,这个换上有二十个节点,每十个对应一个redis,可以减少redis倾斜的概率.
注意:
1:主从:不会在主挂了,将从变成主,master挂了,不再提供写服务
2:Sentinel哨兵:主挂了,会将一个从变成主,就算原先的主恢复了,也变成了从.
3:Cluster分片:所有的节点都是一主一从（也可以是一主多从），其中从不提供服务，仅作为备用.客户端可以连接任何一个主节点进行读写

##### 分片集群:集群redis数据加起来是完整的数据
集群代理：tw,predixy(性能最好),cluster,codis(豌豆荚团队，对redis源码进行过修改)
客户端拆分有弊端：3个模式不能做数据库使用.
预分区:
   1:哈希法:一开始把魔术值设置的较大,比如10个,取模是10,模数值得范围是:0,1..9,中间加一层mapping，
           0,1,2,3,4给第一个redis,5,6,7,8,9给第二个redis;如果有新的redis,那么让前两个redis让出一些槽位即可。
           只需要迁移部分数据即可，比如3,4,8,9给第三台redis.
   2:redis是怎么做的？(无主模型)
     客户端get k1,随机连接一台redis,先hash%n,得到槽位，如果当前redis的槽位中有，取数据；否则，因为每个redis都知道其他
     redis的槽位，返回客户端，应该去那个redis,进行重定向，重复上述操作。

数据分治带来的问题：聚合操作，事务很难实现，可能所需要的key不在同一个redis上。  
  方案： hash tag
       把几个key使用相同的字符串进行hash，就可以把这几个key弄到一个redis上

实际操作：
github上搜索twemproxy  
常识：来自yum装的软件来自于仓库，可能版本较低
1:Linux系统，新建文件夹，git clone ... ,如果失败  yum update nss,升级下版本，中间选y
2:yum install automoke  libtool  :安装automoke和libtool
3：autoreconf -fvi    如果报错，autoreconf版本太低，需要到https://developer.aliyun.com/mirror找到epel
   复制 wget -O /etc/yum.repos.d/epel.repo http://mirrors.aliyun.com/repo/epel-6.repo
   linux 中 cd /etc/yum.repo.d/  然后粘贴执行上面那句话，然后就多了一个指向阿里仓库的位置，然后 yum clean all，清缓存。
   再回到原本的文件夹  cd /usr/local/twemproxy/twemproxy ,执行 yum search antoconf,找到高版本，yum install autoconf版本号，
   执行完成后 autoreconf版本号  -fvi
4：./configure --enable-debug=full
5： make
6:  cd scripts，然后 cp nutcracker.init  /etc/init.d/twemproxy
7:  cd /etc/init.d/    ,然后chmod +x twemproxy   
8:  vi twemproxy 打开文件，然后linux重新打开一个页面，按照文件的指示去操作。 ,新打开页面，mkdir /etc/nutcracker
9:  cd /usr/local/twemproxy/twemproxy/conf,然后执行cp ./*  /etc/nutcracker/
10： cd /usr/local/twemproxy/twemproxy/src ,然后 cp nutcracker  /usr/bin ,在操作系统的任何位置都可以使用命令
11： cd /etc/nutcracker/ ，然后nutcracker]# cp nutcracker.yml  nutcracker.yml .bak，将文件拷贝一下
12： vi nutcracker.yml,  参考https://github.com/twitter/twemproxy   这个网站下的东西进行配置
13:  然后分别启动service下的redis服务器，redis-server --port 6379  并启动tw代理： service twemproxy  start
14:  新开窗口，进入到代理端口   redis-cli  -p  22121(自己配置的)
15：  然后去set ,get等等操作，数据会根据算法存储在redis中，但是对客户端透明,客户端连接的是代理服务器
弊端：
数据分治，代理层keys * 不支持  ，watcht k1不支持，MULTI事务不支持

github上搜索predixy
1：不要尝试编译，我们一般是centos，而它需要需要支持C++11的编译器，因此我们需要编译完成的。
   在Release中寻找https://github.com/joyieldInc/predixy/releases，右键复制链接地址.也可以下载完成后上传到linux.
2：进入到linux系统中，wget 链接地址进行下载,下载完成后进行解压tar xf predixy-1.0.5-bin-amd64-linux.tar.gz
3: cd conf    vi predixy.conf ：查看配置文件并进行修改,二个模式只能出现一种
4： vi sentinel.conf    配置哨兵 光标定位到某个位置，shift+：，然后输入.,$y,代表复制光标到最后的，来到最后，np复制
    来到光标位置，shift+：,然后输入.,$s/#//,代表从光标到最后把#代替换成空
5： 来到每个哨兵中，比如26379中进行配置，三个哨兵配置文件除了端口号不一样，监控的都一样
6： 启动哨兵：redis-server  26379.conf  --sentinel    ,三个都差不多
7： 启动主  cd /test   makdir 36379   cd 36379   redis-server --port 36379   注意：新建文件touch 36379.conf
8： 启动从  cd /test   makdir 36380   cd 36380   redis-server --port 36380 --replicaof  127.0.0.1 36379（主地址和端口）
9： 上面的是一套主从，另外的主从类似，注意端口和地址
10：新建一个Linux系统. cd /usr/local/predixy/predixy-1.0.5/bin,启动predixy： ./predixy  ../conf/predixy.conf
    默认启动端口号7617
11：新打开一个linux页面。启动客户端: redis-cli -p 7617
12: set k1 aaa  ,set k2 aaa,  set {oo} k3 ccc   set {oo} k4 ddd 也可以get到，相同标识的可以放到一台机器中
13： 新打开一个linux系统中，redis-cli -p 36379 ,然后keys * ,去查看数据如何落的。
弊端：这里开启了两个主，不支持事务，一个主才支持事务。关闭了主，哨兵会让备变成主，这有一个过程，不是那么快。

redis自身的集群：每个主机认领一些槽位  槽位代表的就是数据
1：/usr/local/redis-6.0.9/utils/create-cluster ,然后vi create-cluster，查看修改配置文件，修改节点数和redis-server路径
2：然后./create-cluster start，把全部节点品跑起来
3：分主备 ./create-cluster create,需要输入yes
4: 如果启动的是普通客户端 redis-cli -p 30001,set k1 SAFa,经过hash后，计算这个key存储的槽位不在这个redis上;所以一般这样启动：
   redis-cli -c -p 30001,set k的时候如果key的槽不在当前redis上，回先跳转到应该存储的redis,然后再去存储
   注意：set {oo}k2 ssss   set{oo} k3 dfs,会放进同一个redis,那么对这个key，是支持事务和watch的。
5:关闭  ./create-cluster stop     ./create-cluster clean
6:重点：手动启动   不同机器上需要加上 cluster-enabled yes这个配置项
  ./create-cluster start
   redis-cli --cluster create 127.0.0.1:30001  127.0.0.1:30002  127.0.0.1:30003  127.0.0.1:30004 
      127.0.0.1:30005  127.0.0.1:30006  --cluster-replicas  1       
   注意：地址+端口号 ，后面跟上副本数量，中间需要输入yes      redis-cli --cluster help 可以查看命令
   redis-cli --cluster reshard 127.0.0.1:30001  :给新结点分配槽位
   redis-cli --cluster info 127.0.0.1:30001     ：查看具体的主节点及其槽位信息，会出来全部的
   redis-cli --cluster check 127.0.0.1:30001    ：查看全部的槽位分配范围。

####面试常问：
场景： redis做缓存，后面还有一个DB,有很多客户端来访问redis
[1：击穿]
对key做了过期时间，要么开启了淘汰策略(LRU,LFU).刚清除数据A，就有高并发来访问A的，造成必须去访问数据库了。
 注意：如果不是高并发访问数据库，那么是没必要处理的，必须是在高并发场景下，而且数据过期，导致数据库压力很大。
解决方法： 
    推导：  高并发有了-->阻止并发到达DB,redis又没有key-->让第一个去数据库请求，其他请求阻塞，直至第一个拿到key更新至redis.
           redis是单进程实例,假设一万个人排队来访问redis,第一个访问没有key
           回到客户端排到并发的尾部，等到所有的并发都访问失败后都返回客户端队列，原来的第一个还是第一个。第一个就setnx(),
           --->锁。只有设置锁成功的，才能去获取锁。只有获取锁才能去访问db,请求失败的睡眠一会，醒了之后从访问key开始，那么还是取
           不到key的,就尝试去获取锁，还是失败的，又陷入睡眠；直到第一个从数据库拿到key更新到redis中他们才能取到key。
弊端：
     如果第一个挂了，会导致死锁。---->可以设置锁的过期时间---->问题来了：锁时间长肯定不可取，如果时间短了，第一个没挂，
     数据库拥塞了，锁过期了，第二个开始设置锁请求数据库了。----->一个线程取库，一个线程监控是否取回来，更新锁时间(第一个线程
     如果挂了，没有去更新锁时间，会超时，会有新的线程去请求;没挂处于阻塞状态，延长锁的时间)---->
     会让客户端代码复杂度变高（到这里：redis自己实现分布式协调很麻烦,为了引出zookeper）
关于监控线程状态：线程池的int activeCount = ((ThreadPoolExecutor)executor).getActiveCount()

[2：穿透]
从业务接收查询的数据是你系统根本不存在的数据：redis中没有，db中也没有，数据库做一些空查询，消耗性能
解决方法：
      布隆过滤器：大概率阻止请求到达数据库
              1.1：客户端植入布隆过滤器的算法，这样连redis压力都没有了
              1.2：客户端只包含算法，bitmap在redis,客户端无状态。
              1.3：redis中直接集成布隆，
       弊端：只能增加，不能删除；删除的时候置空，或者更换过滤器，必如布谷鸟

[3：雪崩]
概率比击穿的概率大，大量的key同时失效,间接造成大量的访问到达数据库。
解决方法：
      选择随机过期时间，但是对于两种场景：一种是零点换血，最容易雪崩;另一种是时点无关性。
      1：零点换血：强依赖击穿方案；也可以在业务层做判断，做零点延时，不把流量放到redis,再去强依赖击穿方案
      2：时点无关性，选择随机过期时间，不至于在一个时间点造成大量访问

[4：分布式锁]
A：1:setnx
   2:过期时间
   3:多线程(守护线程：延长过期时间)
B:redisson
C:zookeeper(做分布式锁做容易，虽然没有redis快，但是已经是锁了，对效率要求不是那么高，要求准确度和一致性比较强烈)

[5：API(jedis,lettuce,Redisson,springboot:low/high level)]
redis对java支持比较好的有jedis(线程不安全),lettuce,Redisson,但是spring支持jedis,lettuce，因此Redisson变成了可选项
redis使用的是epoll,jedis准备连接池化。
1:连接到redis
2:选择高阶api还是使用低阶的api去使用它
3：数据经过怎样的加工，序列化能存放进去

在linux的redis的客户端，查看redis的所有配置：config get *
注意限制远程连接的：config get protected-mode
如果是yes  ，需要改成no  
config set protected-mode no

注意：1:通过简单的程序，通过IDEA连接redis，存进去一个<hello,china>
       观察linux中redis中的 keys * ，会发现存进去的是乱码
     2:使用string,如果出现了其他类型，会出现类型不匹配
===》自定义序列化





















































