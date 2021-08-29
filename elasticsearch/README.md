# Elasticsearch操作
###
    说明：本项目中只是列出了简单的es相关索引 文档及数据的crud 无法正常运行起来是正常的
    具体使用时参考相关的操作数据及全文搜索实现方法；记录下简单的应用希望在实际的项目中有所帮助
###

###
在线参考网址：
https://www.jianshu.com/p/c1f2161a5d22
https://www.jianshu.com/p/2ff05a83816e
https://blog.csdn.net/angellee1988/article/details/108897016
https://segmentfault.com/a/1190000016828596
https://www.cnblogs.com/wangzhuxing/tag/ES/
###

>#####采用rest client方式版本7.9.2

>>######配置es 集群或者本地单机版windows 
 ~~~~~
     文件elasticsearch.yml 属性配置
     cluster.name: standard-lib	#集群名称，如果有多个集群，那么每个集群名就得是唯一的	
     node.name: node-1	#节点名称
     node.master: true		#该节点是否是master，true表示是的，false表示否，默认是true
     node.data: true			#该节点是否存储数据，默认true表示是的
     bootstrap.memory_lock: false #是否锁住内存，避免交换(swapped)带来的性能损失,默认值是: false
     bootstrap.system_call_filter: false #是否支持过滤掉系统调用。elasticsearch 5.2以后引入的功能，在bootstrap的时候check是否支持seccomp
     cluster.initial_master_nodes: ["node-1"]  #使用本机设置的节点node-1引导集群
     http.port: 9200			#http访问端口，默认是9200，通过这个端口，调用方可以索引查询请求
     #transport.tcp.port: 9300	#节点之间通信的端口-用于多es机器集群，默认为9300
     network.host: 0.0.0.0		#访问地址 配置外网访问
     path.data: E:/elasticsearch/data  #设置存储数据的目录路径
     path.logs: E:/elasticsearch/logs  #设置日志存储的目录路径
     #discovery.zen.ping.unicast.hosts: ["127.0.0.1:9300", "127.0.0.1:8300"]
     #node.max_local_storage_nodes: 2		#设置一台机子能运行的节点数目，一般采用默认的1即可，因为我们一般也只在一台机子上部署一个节点
 ~~~~~