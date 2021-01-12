
## Redis
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

五种基本类型：在不同的业务场景下，需要选择不同的技术选型  
#### string :字符串 ，数值，二进制位操作  
字符串，数值：session，k v缓存，数值计算计算器,ks文件系统(小碎片文件，磁盘IO)，内存
##### 字符串操作：
set k1 00xx nx  表示k1不存在就去设置，表创建
set k2 hello xx  只有存在的时候才能去修改，表更新
mset k3 a k4 b   创建多个字符串
mget k3 k4
append k1 'world'  k1后面添加
getranfe k1  start end    取字符串的start到end  ，
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

#### list:双向链表，key持有头结点和尾节点
同向：栈,
非同向：队列
index():模拟数组
ltrim 0，-2 ,删除区间之外的数据，优化redis内存量
顺序概念：放入的顺序
场景：评论列表，消息队列，替代java容器，让JVM无状态

hash：hashMap,分治法
场景：用户详情

set:集合，无序，不重复，HashSet
场景：随机事件，多实例，不推荐集合交并



















































