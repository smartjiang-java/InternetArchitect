自动补全;
yum -y install bash-completion   安装自动补全命令
tab键
linux 命令

ifconfig  :查看linux的ip
ll:显示文件夹
cd/   到根目录
cd ../ 返回上一层
vi 文件名  查看文件
按insert键  进入编辑linux文件
按esc  :wq  保存并退出
tail 文件名 查看文件末尾十行
source 文件名  重新执行修改的文件，使之生效


//解压jdk
tar -zxvf jdk-8u11-linux-x64.tar.gz -C /usr/local/

//配置环境变量
export JAVA_HOME=/usr/local/jdk1.8.0_11
export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=.:$JAVA_HOME/lib/dt/jar:$JAVA_HOME/jre/lib/rt.jar

source etc/profile  立即生效
yum install -y unzip  下载zip解压包
unzip  .zip压缩包   解压zip文件
mv 文件名  ///   移动文件
rm -rf 文件  删除文件



进入到mysql
cd usr/local/mysql-5.7.18/bin
./mysql -uroot -p  

Xftp文件下载系统
通过Sshell连接linux系统，以后在这里操作命令

mvn -Prelease-all  -DskipTests clean install -U




tomcat安装
1:直接在Xftp中将安装包拖到home/mytest下
2:进入命令行
   cd home/mytest
   ll
   tar -zxvf tomcat文件名 -C /usr/local/
  
3:完成后,进入到 cd/usr/local
  ll
4:进入到tomcat安装包中
   cd bin 
   ll
   ./startup.sh   启动Tomcat
   cd logs/
   cat catalina.out   查看Tomcat打印日志
   ./shutdown.sh   关闭Tomcat


Mysql安装
 1:安装包拖进去
 2:检查是否有mairaDB,如果有,删除,与mysql有冲突
    yum :安装,卸载,检查软件用的
    rm-rf 文件名  卸载软件
    yum list installed | grep mariadb   检查是否存在
    yum -y remove mariadb-libs.x86_64   卸载mariadb
 3:解压mysql  
    cd home/mytest
    ll
    tar -zxvf Mysql文件名 -C /usr/local/
 4:修改解压后的文件名,建议改名为mysql-5.7.18
    mv mysql名称 mysql-5.7.18   改名
    ll
 5:创建数据文件夹
    mkdir data    创建 data文件夹,用来保存数据
    ll
 6:创建用户执行mysqld命令
    useradd mysql   创建mysql用户
 7:初始化mysql
     cd /mysql -5.7.18/bin/
     ls    清除
    ./mysqld  --initialize  --user=mysql --datadir=/usr/local/mysql-5.7.18/data  --basedir=/usr/local/mysql-5.7.18
    复制最后的那个密码(一定要注意,@localhost后面)  gkiav<>)B0jR

 8:启动安全功能
     ./mysql_ssl_rsa_setup --datadir=/usr/local/mysql-5.7.18/data  进行传输加密
 9:修改mysql权限,将权限给mysql用户
   chown -R mysql:mysql /usr/local/mysql-5.7.18
    注意:mysql:msql表示文件夹的所属用户,所属组
         R:表示递归,更改目录中所有子文件夹的权限
 10:启动mysql
   在mysql-5.7.18/bin的目录下启动
   ./mysqld_safe &    &表示后台启动,安全方式
    切回来 ctrl+c
 11:查看mysql是否启动
    ps -ef | grep mysql     查看mysql进程
 12:使用mysql客户端进入mysql
    在mysql-5.7.18/bin的目录下
    ./mysql -uroot -p    修改密码,输入复制的密码

  注意:sql命令要加分号
 13:修改mysql密码
    alter user 'root'@'localhost' identified by '123456';
 14:查看数据库
    show databases;
 15:远程授权访问
   在没授权之前,只能通过本机访问
   grant all privileges on *.* to root@'%' identified by '123456';
   flush privileges;  刷新权限
 16:测试mysql客户端访问mysql
quit
    如果连接报错,需要关闭linux防火墙
   systemctl status firewalld   查看防火墙状态
   systemctl enable firewalld   让防火墙可用
   systemctl disable firewalld  让防火墙不可用
   systemctl start firewalld    开启防火墙
   systemctl stop firewalld     禁用防火墙,直接这个
 17:把项目部署到Linux中(war包,这里是crm.war包)
   tomcat的webapp文件夹下
   发布服务器,打开浏览器访问 http://linux的ip/crm


