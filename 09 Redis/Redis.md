
## Redis�������ص���ǿ�
### 1��ʲô��Redis?
Redis�ǻ����ڴ�ģ��ٶȱȽϿ죬���ڼ�ֵ�ԣ�K��V���ģ������߳�worker�ǵ��̵߳ģ�IO�����Ƕ��̵߳ģ����Ӻܶ࣬���ӳرȽϴ�,epoll(NIO)��
k,v���������ͣ��б��ط��������Ժܺõ�ʵ�ּ����������ƶ���IO�Ż���
����ģ���Ǵ��л�/ԭ��:����vs����

### 2��memcached��redis�Աȵ����ƣ�
--memcachedҲ��k,v�ģ�Ҳ��������ģ�valueֻ����string��Ҳû�б��ط�����ȡ���ݵ�ʱ��ֻ��ȡȫ�������ݣ����ַ���ת�����飬��ȥ�����±�ȡ
redis��ȡ���ݵ�ʱ����Ҫȡȫ�����ݣ����������redis�ڲ�
1������������ٵ�ʱ��epoll��·�������������ǣ������Ƕ����ݣ����Ǹ���һ���¼���֪������Щ���Զ�
2��redis����IO��ȡ�����̴߳��л��������ݿ⴮�л��ȽϿ죬���ݿⱣ֤ͬ����Ҫ���Ǽ�����Ҫ����ʹ�����񣬱�redis������Щ����
�׶ˣ�����˵�����ĺ���cpu�����˷��ˣ�
��ͬһ��IO��������������
==����ͳ��c2 read�����㣬д��   ;  c1 read, ���㣬 д��  6.0��ǰ
���ڣ� worker�߳������㣬�����߳̽��ж�ȡ��д��     6.0�Ժ�,����Ĭ�Ͽ����ģ���Ҫ����
3�����㣬Ҳ�Ǵ��л�

### 1��redis���������ֻ�������
nosql ������

JVM��һ���̵߳³ɱ�1MB�����Ե�С
    1���̶߳��˵��ȳɱ����ӣ�CPU�˷�
    2���ɱ�����

redis��ԭ��ģ�redis����Ĭ����16���⣬0-15��ÿ����֮���Ǹ���ġ�Ĭ�Ͻ�ȥ0�ſ�
select 8��ѡ��8�ſ�

key���ַ�����value�����ֻ�������  help @
#### string :�ַ��� ����ֵ��������λ����  
�ַ�������ֵ��session��k v���棬��ֵ���������,ks�ļ�ϵͳ(С��Ƭ�ļ�������IO)���ڴ�
##### �ַ���������
set k1 00xx nx  ��ʾk1�����ھ�ȥ���ã�����
set k2 hello xx  ֻ�д��ڵ�ʱ�����ȥ�޸ģ�����
mset k3 a k4 b   ��������ַ���
mget k3 k4
append k1 'world'  k1��������
getrange k1  start end    ȡ�ַ�����start��end  ��
 ע�� ��[redis��������������ĩβ-1�������ڶ���-2.  ��һ����0���ڶ�����1 ]
setrange k1 start  jiangqikun  ��start��ʼ�ú�����ַ�����ʼ�滻�������ĳ����Զ��滻
strlen k1  ���k1���ֽڳ���,һ���ַ���һ���ֽڣ����������������ֽ�
type k1    k1���ڵķ��������  set k3��ʱ�򣬲鿴k3���Ǹ����飬type k3��ʱ����Ƿ��������
object encoding k3  �鿴k3�ı������ͣ�embstr��string,raw  int 
getset k1 mashibing  ���k1��ֵ�滻�ɺ����ֵ�������ϵ�ֵ����
##### ��ֵ��������������ɱ������ҳ����ܲ����¶����ݿ�������������ȫ��redis���ڴ��������
incr k1  k1��1
incrby k1 22  k1�ۼ���22
decr k1  k1��һ
decrby k1 22   k1��ȥ22
incrbufloat  k1 0.5  k1���ϸ�����0.5
[redis�Ƕ����ư�ȫ�ģ�ֻȡ�ֽ���,һ��redisʹ��ASCII���룬�����Ĳ�����16���Ʊ�ʾ������redis-cli --raw
 ��ʾʹ����Ӧ�ı��뼯��redis������û���������͵�]����,redis��zookeper,hbase,kafka
ע��:ASCII�����һ���ֽڣ�������0,1,2,3,4,5,6,7����һ������Ϊ0������Ŀ�������仯��
    �����ı��������������������ASCII�����ر���
##### bitmap������������λ
[bitmap���������Ĺ���ÿ�ο���һ���ֽڣ��������Զ��ٴο��� 1���ֽ���8��������λ��ÿһλ����������0-7 �����ֽ�0-15]
0XXXXXXX,��һ���ֽڣ������110XXXXX����ô�������ֽ�
setbit k1 1 1  k1����ֽڵ�����1�� Ϊ1   0100 0000 ��AscII����@,ʮ������64
setbit k1 9 1  9������һ���ֽڵķ�Χ����˱���� 0100 0000 0100 0000��@@
bitpos  k1 1 start end  �鿴k1�е�start��end�ֽ�������Χ�ڳ��ֵĵ�һ��1/0��λ��λ��
bitcount k1 start end   ͳ��k1�д�start�Լ�������end������Χ�ڵ�1��������
bitop and andkey k1,k1   k1��k2��λ����������andkey����
bittop or orkey k1,k2    k1��k2��λ����������orkey��
>>ʹ�ó���1���û�ϵͳ��ͳ���û���¼�������Ҵ������   �û�idΪkey������Ϊλ 
   ����zhangsan  01110101010...
        lisi      01001001101...
  �������ݿ��У��������������û��������ú��Ӵ󡣿�����redis�����ÿһ���Ӧһ��������λ��һ�겻��50���ֽڣ���ʡ�ռ䣬�ٶ�Ҳ�ܿ죬�ڴ�λ����
   setbit zhangsan  1,1  ������һ���¼        
   setbit zhangsan  28,1  ������28���¼
   ÿ�û�ռ��46KB  * �û���  
   ͳ��:bitcount zhangsan 0  -1
       strlen zhangsan   ͳ������ռ�õ��ֽ�
> ʹ�ó���2����˾���ͻ����������2�ڿͻ���6.18Ҫ�������Ҫ�������û�   ���������ڣ�Ϊkey���û�idΪλ
  ����  20200112   01000110101...
         20200113   01010101010...
   �������ͻ��ֽ�ʬ�û��������û�/�ҳϿͻ� ����Ҫ֪��ǰ��ʱ��Ļ�Ծ�û���Ȼ���ٶ౸��20%
     ͳ��1-3�յ��û���¼��Ŀ����ȥ�أ�1��ʹ�����ݿ�   2��ʹ��redis
   setbit  20200112  �û�id 1
   setbit  20200113  �û�id 1
   ͳ�������յĻ�Ծ�û�����bitop or orkey 20200112 20200113
                       bitcount destkey 0  -1

#### value��list:˫��������������ģ������˳��key����ͷ����β�ڵ�
ͬ��ջ,lpush ,lpop
��ͬ�򣺶���,rpush,rpop
ģ������
�������У��������ģ�FIFO(�Ƚ��ȳ�)
lpush k1  a b c d e   �������������߿�ʼ��Ԫ�أ�ͷ�巨
lpop  k1              ������ߵĵ�һ��Ԫ��
rpush k2  a b c d e   ������������ɱ߿�ʼ��Ԫ�أ�β�巨
rpop  k2              �����ұߵĵ�һ��Ԫ��
lrange k1 start end   ����ʼ����k1��startԪ��������end���ȵ�����Ԫ��
lindex k1  temp       ȡ��k1��temp��������Ԫ��
lset  k1  temp  xxxx  ��k1��temp��������Ԫ�ش����xxxx
lrem  k3  count value  �Ƴ�k3�е�count��value����count>0.����߿�ʼ;count<0�����ұ߿�ʼ
linsert k3 after value xx  ��k3��value�������һ��Ԫ��xx
linsert k3 before value oo  ��k3��valueǰ�����һ��Ԫ��oo
ע�⣺�����������value����ֻ�ڵ�һ��ǰ��������������漰���ڶ���
llen k3                ͳ��k3���ж��ٸ�Ԫ��
blpop  k4  timeout      �����ȴ�pop
ltrim 0��-2 ,ɾ������֮������ݣ��Ż�redis�ڴ���
[�����������б�����Ϣ���У����java��������JVM��״̬]

#### value��hash��hashMap,���η���key��String
set �������� name zahngsan         ����������name
get �������� name                  ȡ��������name
keys ����*                        ȡ���������йص���Ϣ
�������ˣ�����ֶκܶ࣬�����鷳��ʹ��hash�Ƚϼ�
hset ���� name zhangsan           hash����һ��name
hmeset ���� age 18 address anhui  ���ö���ֶ�
hget  ���� name                   ȡ��������name
hmget ����  name age              ȡ��������name ��age
hkeys ����                        ȡ�������������ֶ�����
hvals ����                        ȡ���������ֶζ�Ӧ��ֵ��Ϣ
hgetall ����                      ȡ���������ֶμ����Ӧ��ֵ��Ϣ
hincebyfloat ���� age 0.5         ��������age��value����0.5
[�������û�����,��Ʒ����,����]

#### set:���ϣ�ȡ�����򣬲��ظ���ȥ�أ���HashSet
sadd k1 a b c d e   ��������
smembers k1         ��ϸ�鿴k1������Ԫ��
srem k1 ooxx  xxoo   �Ƴ�k1�����Ԫ��
sinter k2 k3         ����k2��k3�Ľ���
sinterstore temp k2 k3   ȡ k2��k3�Ľ����洢��temp��,��������IO
sunion  k2,k3        ����k2,k3�Ĳ���
sunionstore temmp k2 k3
sdiff  k2  k3        ���ص�k2�Ĳ��ȥ��������
srandmember k1 5 -5 10 -10   Ϊ������ʱ��,ȡ��һ��ȥ�صĽ����(����������Ԫ������);������ȡ��һ�����ظ��Ľ������һ������Ҫ������
     [�����û��齱�����ظ����߲��ظ�;�����Խ����ͥ���� ]
spop k1               ���k1��Ԫ��������ĵ���һ��
[����������¼�(�齱)����ʵ�������ϲ���]

#### zset:sorted set�����ݱ�ȥ�أ���ʵʱ��Ԫ�ؽ�������Ԫ��������������
���򣺼�Ҫ����Ԫ�أ�ҲҪ������ֵ����ֵ��Ϊ1�������Ƶ��ֵ�˳��
zadd k1 8 apple 2 banana 3 orange   ���ȥ�����շ�ֵ���������ڴ棺��С�ڴ�
zrange k1 start end                 ȡ��k1��start��end��Ԫ��
zrange k1 start end  withscores     ȡ��k1��start��end��Ԫ�ؼ����ֵ
zrangebyscore  k1  mix max          ȡ��k1�з�ֵ����min��max����֮���Ԫ��
zrevange k1  start end              ȡ��k1��start��end��Ԫ�أ��ɸߵ���
zscore k1 value                     ȡ��k1��value�ķ�ֵ
zrank k1 value                      ȡ��k1��value������(����)
zincrby k1 2.5 value                ��k1��value�ķ�ֵ����2.5
���ϲ�����
zunionstore  unkey  2 k1 k2         �Զ������������
zunionstore  unkey  3 k1 k2 k3
zunionstore  unkey1  2 k1 k2  weight 1 0.5       �Զ������������,������Ȩ��
zunionstore  unkey2  2 k1 k2   aggregate max     �Զ������������,����Ĭ����ͣ�������������ֵ
[������Ŀ����������ôʵ�ֵ�?��ɾ�Ĳ���ٶȣ�   ==�ײ�ʵ�� skip list��Ծ���������ֲ㣬����ķֲ�]
[Ӧ�ó������������а�]

### 2:redis�Ľ���ʹ��
�ܵ������ܶ����������ͣ�����֮��ʹ��\n������ʹ��ͨ�ŵĳɱ����
#### redis����Ϣ����
subscribe  00xx        ������Ϣ
publish ooxx hello     ���Ѷ˼����Ժ�ÿ�����͸�xxoo����Ϣ�ſ��Ա�����,��Ҫ����
ע�⣺���Խ����ں�ʱ����Ϊ��ֵ����Ϣ��Ϊvalue���д洢

#### redis���񣺲�������ô������һ�ж��Ǳ��ſ�ȥ�ģ�û�лع�����
help @transactions
ע�⣺redis�ǵ��̵߳�,�Ǹ��ͻ��˵�exec�ȵ������ִ��
multi   �������� 
...
exec    ִ������

watch ��أ�������cas,��س�ʼֵ
set k1 2
watch k1
multi
...     ������������޸���k1��ֵ
exec    k1��ֵ��watchʱ��ͬ������ִ��

[redis�Ĳ�¡������]��������洩͸���⣺��С�Ŀռ�������ƥ�䣺���ʽ������99%
������û�У�ֱ��ȥ���ݿ���ʣ���ʵ���ݿ���Ҳû�� ��bitmap��bloom.so����redis�У�ʹ��clienr�˱Ƚ���
1�������е���bitmap�б��  
2������Ŀ���������
3��һ�����ʻ�������ٷ��д�͸���ɱ���
bf.add   k1  aaa
bf.exists k1   aaa
���ʹ�ù�������
1:����redis.io������Ϸ�Modules
2�����RedisBloom��С���ӣ�����github�����ַ���Ҽ�������������https://githubcom/RedisBloom//archive/master.zip
3��linux��wget https://github.com/RedisBloom/RedisBloom/archive/master.zip
4: �����װ��unzip��unzip *.zip ;û��װ�Ļ�yum install unzip����ȥ��ѹ
5�����뵽��ѹ���ļ��У�make
6:cp bloom.so  /usr/local/redis.6.0.9/
7:cd bin 
8��redis-server  --loadmodule  /usr/local/redis-6.0.9/redisbloom.so
9:redis-cli 
10:bf.add  k1 aaa      cf.add k2 bbb
11:bf.exits k1 aaa     cf.exits k2 bbb

1����ѯ��͸�ˣ����ݿ��Ѕs�����ڣ��ͻ�������redis�е�key��value��ǲ����ڣ��´��ٲ�ѯ���Ͳ��߲�¡�ˣ�ֱ�ӷ��ز�����
2�����ݿ�����redis�� ������ݿ���������Ԫ�أ���Ҫ����Ԫ�ضԲ�¡������

[redis��Ϊ��������ݿ������]
�����棺����������ʵ����Ҫ,����ȫ������;����Ӧ�����ŷ��ʱ仯(������)�����ݿ��Զ�
redis�����棬���ٺ�����ݿ����ѹ������ôredis���������ô������ҵ��仯��ֻ���������ݣ���Ϊ�ڴ��С�����޵ģ�Ҳ����ƿ��
������
1��key����Ч��
 a:��ҵ���߼����ƶ�����������Ϊ��λ������д�����ݿ⣬ 
    ���ù���ʱ�� 1,2����ʱ��3��ʱ
    1:  set k1 aaa ex 20 :˵���������20s�����
        ttl k1           :�������key���ж೤ʱ�����
    2:  set k2  bbb
        expire k1 50      :����50s�����
        set k2  ccc
    3:  expireat          ��ʱ���
     [���·��ʺ����ʱ���ǲ����ӳ��ģ�ʱ�䵽�˾ͷ��ʲ������������;���������д�����޳�����ʱ��]
    http://redis.cn/commands/expire.html
    �����ж�ԭ����1:���������ж�    2��������ѯʱ���ж�(����)
        Ŀ�ģ���΢�������ڴ棬���Ǳ�ס��redis����Ϊ��
 b:ҵ����ת��������Ϊ�ڴ������޵ģ����ŷ��ʵı仯������̭������
    redis�ڴ����أ�  maxmemory������������ڴ�(һ��1-10G),�ڴ�ʹ�ý������л��ղ��ԣ�һ���ʹ��allkeys-lru���������ʱ��������ޣ�
            ����ͨ��LRU�㷨�������û��ʹ�õļ�;��volatile-lru���������ʱ��������ޣ����ȴ������˹���ʱ��ļ��������������û��ʹ�õļ�.
            һ�㿴�������������ù���ʱ���ռ�ȣ��������������˹���ʱ��ѡ����ߣ���֮ѡ��ǰ�ߡ�

�����ݿ�:���ݲ��ܶ�,redis+mysql�������µ����⣺����һ���ԣ�һ�㲻����ǿһ���ԣ�ʹ���첽һ����   �ٶ�+�־���
        ��Ϊ�ڴ��еĶ��������е�����ʧ��
   �����־û�:�洢��һ�㶼�п���/������RDB��,��־(AOF)��
      ����RDB:ʱ���ԣ������濪�����һ����㹻��
         ���ʵ�֣�  1:������redis�������ṩ����(����ȡ)
                   2:����������ͬ�������ṩ����,
              �ܵ���1:�ν�ǰһ������������Ϊ��һ�����������   2���ᴥ�������ӽ��� echo ��BASHPID | more
              ����˼�룺����֮�������Ǹ����    ����˼�룺�����������ǿ������ӽ��̿����ġ��ӽ��̵��޸Ĳ����ƻ�������;��֮��Ȼ
            �����������redis���ڴ����ݱ�����10��G���������ٶȺ��ڴ�ռ乻������������---->
            ϵͳ����fork()+�ں˻���copy ana write():�ٶ���ԽϿ죬�ռ���Խ�С
            дʱ���ƣ������ӽ��̵�ʱ�򲢲��������ƣ�ʹ�ô������̱���ˣ����ݣ������ָ�룬�����Ǹ������ݣ�����ָ�����ݵ�ָ��
            �����̶����ṩ��ɾ�Ĳ�ķ����ӽ��̶����������д�ļ����ǲ����޸����ݵ�
         ��δ�����  sava,ǰ̨����,����ػ�ά��   bqsave��ǰ̨������ 
                   �����ļ��и���bqsave�Ĺ���save�������ʶ�����Ǵ�������bqsave
         �׶ˣ�1����֧����������Զ��ֻ��һ��dump.rdb,��Ҫÿ�춨ʱ������������Ḳ��
              2����ʧ���ݱȽ϶࣬������ʵʱ��¼���ݵı仯��ʱ����ʱ��֮�䴰�����ݶ�ʧ��8��õ�һ��rdb,9���Ҫ�����һ���
         �ŵ㣺����java�е����л����ָ����ٶ���Կ�
      ��־AOF:
         redis��д������¼���ļ��У���ʧ������,redis��RDB��AOF����ͬʱ��������ֻ��ʹ��AOF��4.0֮��AOF�а���RDBȫ�������Ӽ�¼��д����
         �׶ˣ�1���������ޱ�󣬻ָ���������һ�㶼�Ὣ��־�㹻С��4.0֮ǰ����д��ɾ�������ģ��ϲ��ظ��ģ�����Ҳ��һ����ָ�����־�ļ�;
                 4.0֮��Ҳ����д�����ϵ�����RDB��AOF�ļ��У�����������ָ��ķ�ʽ���ӵ�AOF��AOF��һ������壬������RDB�Ŀ��AOF������ȫ
         ԭ�㣺redis���ڴ����ݿ⣬д�����ᴥ��IO�������������дIO��NO��buffer���˿�ʼˢ���̣������ݽӽ�һ��buffer����
              always(������ɿ������ඪһ��), ÿ��(Ĭ�ϵģ���ʧ��������һ��buffer)

#### redis�ļ�Ⱥ
   CAPԭ��һ���ԣ������ԣ������ݴ��ԣ�����ֻ��ʵ�ֶ��ߣ����������߼��
   ���������е����⣺ 1:�������    2����������    3:��������CPUѹ��    һ��������߿���HA
   AKF��x�ᣬy�ᣬz��
        ����x�᣺���ӱ�����ȫ�����񣬽���˵�����ϣ��Ͳ���ѹ��
        ����y�᣺��ҵ���ܶ����ݽ��л��֣�ÿ��redis�洢�������ݣ���һ��redisʵ����ɶ��redisʵ�����൱��mysql�ķֿ⣬�����������
        ����z�᣺�������ȼ��߼��ٲ�֣�����������޺�ѹ��
   �׶ˣ�1��ͨ��AKF��һ��࣬д�����ݣ���α�֤����һ���ԣ�
            a:���нڵ�������֪������ȫ��һ�£�ǿһ���ԣ������һ����������ʲô�����⣬����ֳ�ʱ�������ƻ������ԡ�ͬ��������ʽ
            b:���̲������ݶ�ʧ����һ���ԣ��ͻ��˽��պ�ֱ�ӷ��أ�ͨ���첽��ʽд������������redisΪ�˿죬Ĭ��ʹ���첽����
            c:��������һ�£������ͱ���֮�����kafka(�ɿ�����Ⱥ����Ӧ�ٶ��㹻��)��������kafakaͬ��������������kafka��ȡ����
                �׶ˣ��ͻ���ȡ���ݣ����ܻ�ȡ����һ�µ����ݣ�ǿ����ǿһ����
           
   �ܹ�3����һ�㼯Ⱥʹ�û���̨���е����ո��ߣ��ɱ��ϵ�
   1����ͨ������ȥ���ʼ�̨����������õ���ͬ�Ľ��������������������ݲ�һ�¡�����Ҫ��һ��Ҫ���룬���ǲ��Ǿ��Ի���,�ͷ������̶��й�,���������档
   2���� һ����OK������һ���ֲ������������ڷ�����

   ���Ӹ��ƣ������ϣ�����֪���ж��ٸ�����׷������ ��Ҫ�˹�ȥά������������˸߿���HA��Redis��Sentinel
         redis-server  ./6379.conf  --replicaof  127.0.0.1 6380     6380����6379
         redis-server  ./16379.conf  --sentinel                    �����ڱ�����
       ��֪������Щ�ӣ��ڱ�ֻҪ����������Ϳ���֪������Щ��.��������
   redis�ڱ������֪�������ڱ��ģ�
       redis�Դ��������ģ��ڼ������ʱ���⵽�ӣ�������������Ϣ���ķ��������ڱ����ڱ��ǻ��޸��Լ��������ļ���

2^32:һ���Թ�ϣ�㷨��������ϣֵ�ռ�ӳ���һ�������Բ����������ϣ�ռ��ȡֵ��ΧΪ0~232-1�������ռ䰴˳ʱ�뷽����֯��
0~2^32-1������з����غϡ�
����ڵ���������б�����⣺����redis��Ӧ�����������,���ܳ������ݼ�����һ��redis�д�����.���Կ��Խ�redis��ip+һ������ʮ��
                        ��hash,��������ж�ʮ���ڵ�,ÿʮ����Ӧһ��redis,���Լ���redis��б�ĸ���.

��Ⱥ������tw,predixy(�������),cluster,codis(�㶹���Ŷӣ���redisԴ����й��޸�)

�ͻ��˲���б׶ˣ�3��ģʽ���������ݿ�ʹ��.

Ԥ����:
   1:��ϣ��:һ��ʼ��ħ��ֵ���õĽϴ�,����10��,ȡģ��10,ģ��ֵ�÷�Χ��:0,1..9,�м��һ��mapping��
           0,1,2,3,4����һ��redis,5,6,7,8,9���ڶ���redis;������µ�redis,��ô��ǰ����redis�ó�һЩ��λ���ɡ�
           ֻ��ҪǨ�Ʋ������ݼ��ɣ�����3,4,8,9������̨redis.
   2:redis����ô���ģ�(����ģ��)
     �ͻ���get k1,�������һ̨redis,��hash%n,�õ���λ�������ǰredis�Ĳ�λ���У�ȡ���ݣ�������Ϊÿ��redis��֪������
     redis�Ĳ�λ�����ؿͻ��ˣ�Ӧ��ȥ�Ǹ�redis,�����ض����ظ�����������

���ݷ��δ��������⣺�ۺϲ������������ʵ�֣���������Ҫ��key����ͬһ��redis�ϡ�  
  ������ hash tag
       �Ѽ���keyʹ����ͬ���ַ�������hash���Ϳ��԰��⼸��keyŪ��һ��redis��

ʵ�ʲ�����
github������twemproxy  
��ʶ������yumװ�����������ڲֿ⣬���ܰ汾�ϵ�
1:Linuxϵͳ���½��ļ��У�git clone ... ,���ʧ��  yum update nss,�����°汾���м�ѡy
2:yum install automoke  libtool  :��װautomoke��libtool
3��autoreconf -fvi    ���������autoreconf�汾̫�ͣ���Ҫ��https://developer.aliyun.com/mirror�ҵ�epel
   ���� wget -O /etc/yum.repos.d/epel.repo http://mirrors.aliyun.com/repo/epel-6.repo
   linux �� cd /etc/yum.repo.d/  Ȼ��ճ��ִ�������Ǿ仰��Ȼ��Ͷ���һ��ָ����ֿ��λ�ã�Ȼ�� yum clean all���建�档
   �ٻص�ԭ�����ļ���  cd /usr/local/twemproxy/twemproxy ,ִ�� yum search antoconf,�ҵ��߰汾��yum install autoconf�汾�ţ�
   ִ����ɺ� autoreconf�汾��  -fvi
4��./configure --enable-debug=full
5�� make
6:  cd scripts��Ȼ�� cp nutcracker.init  /etc/init.d/twemproxy
7:  cd /etc/init.d/    ,Ȼ��chmod +x twemproxy   
8:  vi twemproxy ���ļ���Ȼ��linux���´�һ��ҳ�棬�����ļ���ָʾȥ������ ,�´�ҳ�棬mkdir /etc/nutcracker
9:  cd /usr/local/twemproxy/twemproxy/conf,Ȼ��ִ��cp ./*  /etc/nutcracker/
10�� cd /usr/local/twemproxy/twemproxy/src ,Ȼ�� cp nutcracker  /usr/bin ,�ڲ���ϵͳ���κ�λ�ö�����ʹ������
11�� cd /etc/nutcracker/ ��Ȼ��nutcracker]# cp nutcracker.yml  nutcracker.yml .bak�����ļ�����һ��
12�� vi nutcracker.yml,  �ο�https://github.com/twitter/twemproxy   �����վ�µĶ�����������
13:  Ȼ��ֱ�����service�µ�redis��������redis-server --port 6379  ������tw������ service twemproxy  start
14:  �¿����ڣ����뵽�����˿�   redis-cli  -p  22121(�Լ����õ�)
15��  Ȼ��ȥset ,get�ȵȲ��������ݻ�����㷨�洢��redis�У����ǶԿͻ���͸��,�ͻ������ӵ��Ǵ���������
�׶ˣ�
���ݷ��Σ�������keys * ��֧��  ��watcht k1��֧�֣�MULTI����֧��

github������predixy
1����Ҫ���Ա��룬����һ����centos��������Ҫ��Ҫ֧��C++11�ı����������������Ҫ������ɵġ�
   ��Release��Ѱ��https://github.com/joyieldInc/predixy/releases���Ҽ��������ӵ�ַ.Ҳ����������ɺ��ϴ���linux.
2�����뵽linuxϵͳ�У�wget ���ӵ�ַ��������,������ɺ���н�ѹtar xf predixy-1.0.5-bin-amd64-linux.tar.gz
3: cd conf    vi predixy.conf ���鿴�����ļ��������޸�,����ģʽֻ�ܳ���һ��
4�� vi sentinel.conf    �����ڱ� ��궨λ��ĳ��λ�ã�shift+����Ȼ������.,$y,�������ƹ�굽���ģ��������np����
    �������λ�ã�shift+��,Ȼ������.,$s/#//,�����ӹ�굽����#���滻�ɿ�
5�� ����ÿ���ڱ��У�����26379�н������ã������ڱ������ļ����˶˿ںŲ�һ������صĶ�һ��
6�� �����ڱ���redis-server  26379.conf  --sentinel    ,���������
7�� ������  cd /test   makdir 36379   cd 36379   redis-server --port 36379   ע�⣺�½��ļ�touch 36379.conf
8�� ������  cd /test   makdir 36380   cd 36380   redis-server --port 36380 --replicaof  127.0.0.1 36379������ַ�Ͷ˿ڣ�
9�� �������һ�����ӣ�������������ƣ�ע��˿ں͵�ַ
10���½�һ��Linuxϵͳ. cd /usr/local/predixy/predixy-1.0.5/bin,����predixy�� ./predixy  ../conf/predixy.conf
    Ĭ�������˿ں�7617
11���´�һ��linuxҳ�档�����ͻ���: redis-cli -p 7617
12: set k1 aaa  ,set k2 aaa,  set {oo} k3 ccc   set {oo} k4 ddd Ҳ����get������ͬ��ʶ�Ŀ��Էŵ�һ̨������
13�� �´�һ��linuxϵͳ�У�redis-cli -p 36379 ,Ȼ��keys * ,ȥ�鿴���������ġ�
�׶ˣ����￪��������������֧������һ������֧�����񡣹ر��������ڱ����ñ������������һ�����̣�������ô�졣

redis�����ļ�Ⱥ��ÿ����������һЩ��λ  ��λ�����ľ�������
1��/usr/local/redis-6.0.9/utils/create-cluster ,Ȼ��vi create-cluster���鿴�޸������ļ����޸Ľڵ�����redis-server·��
2��Ȼ��./create-cluster start����ȫ���ڵ�Ʒ������
3�������� ./create-cluster create,��Ҫ����yes
4: �������������ͨ�ͻ��� redis-cli -p 30001,set k1 SAFa,����hash�󣬼������key�洢�Ĳ�λ�������redis��;����һ������������
   redis-cli -c -p 30001,set k��ʱ�����key�Ĳ۲��ڵ�ǰredis�ϣ�������ת��Ӧ�ô洢��redis,Ȼ����ȥ�洢
   ע�⣺set {oo}k2 ssss   set{oo} k3 dfs,��Ž�ͬһ��redis,��ô�����key����֧�������watch�ġ�
5:�ر�  ./create-cluster stop     ./create-cluster clean
6:�ص㣺�ֶ�����   ��ͬ��������Ҫ���� cluster-enabled yes���������
  ./create-cluster start
   redis-cli --cluster create 127.0.0.1:30001  127.0.0.1:30002  127.0.0.1:30003  127.0.0.1:30004 
      127.0.0.1:30005  127.0.0.1:30006  --cluster-replicas  1       
   ע�⣺��ַ+�˿ں� ��������ϸ����������м���Ҫ����yes      redis-cli --cluster help ���Բ鿴����
   redis-cli --cluster reshard 127.0.0.1:30001  :���½������λ
   redis-cli --cluster info 127.0.0.1:30001     ���鿴��������ڵ㼰���λ��Ϣ�������ȫ����
   redis-cli --cluster check 127.0.0.1:30001    ���鿴ȫ���Ĳ�λ���䷶Χ��

####���Գ��ʣ�
������ redis�����棬���滹��һ��DB,�кܶ�ͻ���������redis
[1������]
��key���˹���ʱ�䣬Ҫô��������̭����(LRU,LFU).���������A�����и߲���������A�ģ���ɱ���ȥ�������ݿ��ˡ�
 ע�⣺������Ǹ߲����������ݿ⣬��ô��û��Ҫ�����ģ��������ڸ߲��������£��������ݹ��ڣ��������ݿ�ѹ���ܴ�
��������� 
    �Ƶ���  �߲�������-->��ֹ��������DB,redis��û��key-->�õ�һ��ȥ���ݿ�������������������ֱ����һ���õ�key������redis.
           redis�ǵ�����ʵ��,����һ������Ŷ�������redis,��һ������û��key
           �ص��ͻ����ŵ�������β�����ȵ����еĲ���������ʧ�ܺ󶼷��ؿͻ��˶��У�ԭ���ĵ�һ�����ǵ�һ������һ����setnx(),
           --->����ֻ���������ɹ��ģ�����ȥ��ȡ����ֻ�л�ȡ������ȥ����db,����ʧ�ܵ�˯��һ�ᣬ����֮��ӷ���key��ʼ����ô����ȡ
           ����key��,�ͳ���ȥ��ȡ��������ʧ�ܵģ�������˯�ߣ�ֱ����һ�������ݿ��õ�key���µ�redis�����ǲ���ȡ��key��
�׶ˣ�
     �����һ�����ˣ��ᵼ��������---->�����������Ĺ���ʱ��---->�������ˣ���ʱ�䳤�϶�����ȡ�����ʱ����ˣ���һ��û�ң�
     ���ݿ�ӵ���ˣ��������ˣ��ڶ�����ʼ�������������ݿ��ˡ�----->һ���߳�ȡ�⣬һ���̼߳���Ƿ�ȡ������������ʱ��(��һ���߳�
     ������ˣ�û��ȥ������ʱ�䣬�ᳬʱ�������µ��߳�ȥ����;û�Ҵ�������״̬���ӳ�����ʱ��)---->
     ���ÿͻ��˴��븴�Ӷȱ�ߣ������redis�Լ�ʵ�ֲַ�ʽЭ�����鷳,Ϊ������zookeper��
���ڼ���߳�״̬���̳߳ص�int activeCount = ((ThreadPoolExecutor)executor).getActiveCount()

[2����͸]
��ҵ����ղ�ѯ����������ϵͳ���������ڵ����ݣ�redis��û�У�db��Ҳû�У����ݿ���һЩ�ղ�ѯ����������
���������
      ��¡���������������ֹ���󵽴����ݿ�
              1.1���ͻ���ֲ�벼¡���������㷨��������redisѹ����û����
              1.2���ͻ���ֻ�����㷨��bitmap��redis,�ͻ�����״̬��
              1.3��redis��ֱ�Ӽ��ɲ�¡��
       �׶ˣ�ֻ�����ӣ�����ɾ����ɾ����ʱ���ÿգ����߸��������������粼����

[3��ѩ��]
���ʱȻ����ĸ��ʴ󣬴�����keyͬʱʧЧ,�����ɴ����ķ��ʵ������ݿ⡣
���������
      ѡ���������ʱ�䣬���Ƕ������ֳ�����һ������㻻Ѫ��������ѩ��;��һ����ʱ���޹��ԡ�
      1����㻻Ѫ��ǿ��������������Ҳ������ҵ������жϣ��������ʱ�����������ŵ�redis,��ȥǿ������������
      2��ʱ���޹��ԣ�ѡ���������ʱ�䣬��������һ��ʱ�����ɴ�������

[4���ֲ�ʽ��]
A��1:setnx
   2:����ʱ��
   3:���߳�(�ػ��̣߳��ӳ�����ʱ��)
B:redisson
C:zookeeper(���ֲ�ʽ�������ף���Ȼû��redis�죬�����Ѿ������ˣ���Ч��Ҫ������ô�ߣ�Ҫ��׼ȷ�Ⱥ�һ���ԱȽ�ǿ��)

[5��API(jedis,lettuce,Redisson,springboot:low/high level)]
redis��java֧�ֱȽϺõ���jedis(�̲߳���ȫ),lettuce,Redisson,����spring֧��jedis,lettuce�����Redisson����˿�ѡ��
redisʹ�õ���epoll,jedis׼�����ӳػ���
1:���ӵ�redis
2:ѡ��߽�api����ʹ�õͽ׵�apiȥʹ����
3�����ݾ��������ļӹ������л��ܴ�Ž�ȥ

��linux��redis�Ŀͻ��ˣ��鿴redis���������ã�config get *
ע������Զ�����ӵģ�config get protected-mode
�����yes  ����Ҫ�ĳ�no  
config set protected-mode no

ע�⣺1:ͨ���򵥵ĳ���ͨ��IDEA����redis�����ȥһ��<hello,china>
       �۲�linux��redis�е� keys * ���ᷢ�ִ��ȥ��������
     2:ʹ��string,����������������ͣ���������Ͳ�ƥ��
===���Զ������л�




















































