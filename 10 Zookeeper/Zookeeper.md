                              Zookeeper文档

## zookeeper介绍和安装
### 介绍  官网:https://zookeeper.apache.org
   分布式协调服务,数据保存在内存中,zookeeper很少单独使用,一般都是组件集群使用,主从架构,目录树结构,节点存数据很少,1M,
   不要把zookeeper当做数据库用.客户端与zk建立连接就是建立会话,依托seesion有了临时节点,分布式锁;seesion在所就在,session
   挂了锁也就消失了,不需要像redis中需要另一个线程去监控,不需要设置过期时间. zk使用了最终一致性
===>集群中有一个leader,肯定有挂掉的风险,但是zk集群能快速的恢复出一个leader,极其高可用
 总结出:zk集群运行的状态  可用状态-->不可用状体(无主,主挂掉)-->可用状态 
 所以:由不可用恢复到可用状态当然是越快越好,官方文档:zk推选出新的领导者不超过200ms

### 安装
1:到官网去下载zookeeper安装包,并上传到linux系统上的/usr/local/zookeeper文件夹
2:解压 tar xf apache-zookeeper-3.6.2.tar.gz 
3:将解压包到另外的位置  mv apache-zookeeper-3.6.2  /opt/jqk/
4:cd /opt/jqk/apache-zookeeper-3.6.2    ll    cd conf
5:将模板配置文件拷贝一份,名字为zoo.cfg   cp zoo_sample.cfg  zoo.cfg
6:vi zoo.cfg 进行修改配置文件,注意:过半数=行数/2+1 ,开始没有leader,都是通过3888连接;有了leader,会创建2888,后续都会连接2888
7:进入到配置文件里配置的存放文件的地方  cd /var/jqk/zookeeper
8:创建myid  vi myid    在里面写入一个1就好了
9:将当前所在机器拷贝至其他机器上   scp -r ./jqk/ node02:'pwd'   回车输入密码即可
10:其他机器需要 mkdir -p /var/jqk/zookeeper     echo 2 > /var/jqk/zookeeper/myid   cat /var/jqk/zookeeper/myid
11:全部机器都要配置环境变量 vi /etc/profile  在下面加上 export ZOOKEEPER_HOME=/opt/jqk/apache-zookeeper-3.6.2-bin
   在export PATH=$PATH:$JAVA_HOME/bin后加上:$ZOOKEEPER_HOME/bin
   然后,启动配置文件  . /etc/profile或者source /etc/profile
12:启动:zkServer.sh  start(后台启动) 或者 zkServer start-foreground(前台启动)
13:进去客户端:zkCli.sh   输入help查看常用命令,一个客户端连接的时候会创建一个sessionId,这个也会在集群中同步,写给内存
   会话结束也会将sessionId从集群内存中删除

### 常用命令
1:创建节点   create /OOXX ""   注意data不能省略,不写的话用""代替
            create -e /xoxo ""  创建临时节点
            create -s /xxoo ""  多个客户端创建节点,可能会覆盖,但是这里能规避掉,会在节点后自动递增加上数字,不会覆盖创建
2:显示根目录  ls /      显示子目录  ls /xxoo
3:获取节点中的数据   get /OOXX    
   注意:cZxid:leader维护的一个创建事务id,64位,前32位表示leader纪元,是第几个leader;后32位计数
        mZxid:维护的修改的事务id,每次增删改都会使后32位加1
        pZxid:当前节点下创建的最后的一个事务id
        ephemeralOwner:临时节点归属者.节点分持久节点,和临时节点(归属会话session,session结束,节点消失)
4:设置节点的数据   set /ooxx 回车输入数据用双引号括起来即可,数据最多是1M,而且数据是二进制安全的(字节数组)
5:删除节点  rmr xxoo
netstat -natp | eqrep '(2888|3888)'   查看当前客户端与其他zookeeper的连接

## 原理
### pasos
Paxos:基于消息传递的一致性算法,Google的Chubby，Apache的Zookeeper都是基于它的理论来实现的，Paxos还被认为是到目前为止
唯一的分布式一致性算法，其它的算法都是Paxos的改进或简化.有一个前提:没有拜占庭将军问题,即信任所有的计算机环境,这个环境是
不会被入侵破坏的.
如果角色平等,会产生活锁:即每个角色都发出提议,每人一票,不过半但是都是投自己的提议,问题得不到解决,但是一直在运作
==>为了解决活锁,产生了一个leader,总有leader有权发出提议.其他角色如果有提议,必须发给总统,交由它来提议.

### ZAB 
ZAB作用在leader可用状态,不可用时zookeeper暂停对外提供服务,直至选出leader.
#### leader可用工作机制

#### leader可不用
每个议员都会有自己的myid,和Zxid 
如何产生新的leader?
如果leader挂了,剩下的议员中肯定有和leader一样数据的,那么根据什么选择?
1:经验最丰富的Zxid   2:myid的大小(大的上)
第一次启动集群:机器数量大于集群过半数才能选出leader,比如集群4台,只有最少开启3台的时候才能选出leader
重启集群,leader挂了后:场景,集群四台,4挂了
只有一台3发现leader挂了,其余的心跳时间还没到,还没发现,且这一台的Zxid是最小的
a:3首先将自己的Zxid,myid发送给剩余的两台,并首先给自己投一票,此时3一票
2:1,2收到数据后,比较Zxid和myid,如果没有自己强,就否决这张票,并把自己的Zxid和myid发送给3,并给自己投一票,并广播自己的票
3:机器拿到其他机器的信息之后比较,并发送结果,到最后,每个内存当中都会出现leader的票数如果有一方票数大于过半数,领导产生
总结:3888造成两两通信;只有任何人投票,都会触发准leader发起自己的投票

注意:挂掉之后的机器<=过半数,集群处于不可用状态.

### watch 观察















