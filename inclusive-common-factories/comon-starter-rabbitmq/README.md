### common-starer-rabbitmq工程，整合rabbitmq消息中间件

##### Linux平台下rabbitmq单机版安装
* centos8和centos7版本安装不同，参考网址https://www.rabbitmq.com/install-rpm.html
* 安装可视化插件，rabbitmq-plugins enable rabbitmq_management
* 装完之后，设置后台启动步骤，第一：systemctl enable rabbitmq-server。第二：systemctl start rabbitmq-server
* 由于guest账号只能localhost网址访问，因此添加新用户。第一步添加用户：rabbitmqctl add_user admin admin，第二步授管理员(administrator)角色：
rabbitmqctl set_user_tags admin administrator，第三步查看用户权限：rabbitmqctl list_users，第四步开启远程访问：
rabbitmqctl set_permissions -p "/" admin ".*" ".*" ".*"，第五步查看权限：rabbitmqctl list_permissions -p /
* 至此rabbitmq单机版安装完成，远程访问端口，http://ip:15672

##### rabbitmq应用场景
* 跨系统之间的通信，消息发送和接受
* 消息负载均衡
* 数据异步处理

##### rabbitmq相关问题
* message的正确传递是通过ack（acknowledged）模式进行确认，只有消费者显示的或者自动进行ack确认才能保证消息被正确传到。ack的机制可以起到限流的作用，接受到消息后过几秒再进行ack确认，用于延迟消费。（Channel类中basicAck方法）
* 消息拒绝策略，第一种是直接删除该message，第二种是将message发送给下一个consumer。


##### rabbitmq相关概念
* 消息队列服务都有三个概念:生产者、消费者、消息队列。
* rabbitmq消息队列主要包括了交换机(exchange)和队列(queue)，其中队列需要绑定到交换机中进行使用。
* 虚拟主机：一个虚拟主机持有一组交换机、队列和绑定。一般需要多个虚拟主机，RabbitMQ当中，用户只能在虚拟主机的粒度进行权限控制。 因此，如果需要禁止A组访问B组的交换机/队列/绑定，必须为A和B分别创建一个虚拟主机。每一个RabbitMQ服务器都有一个默认的虚拟主机“/”
* 交换机：Exchange 用于转发消息，但是它不会做存储 ，如果没有 Queue bind 到 Exchange 的话，它会直接丢弃掉 Producer 发送过来的消息。 这里有一个比较重要的概念：路由键 。消息到交换机的时候，交互机会转发到对应的队列中，那么究竟转发到哪个队列，就要根据该路由键
进行绑定，交换机和队列之间属于多对多的关系。
* rabbitmq建立在tcp连接上，使用channel进行数据传输。
##### rabbitmq死信队列
* 没有被及时消费的消息队列，需要重新被消费
* 主要三个产生原因：消息被拒绝（basic.reject/ basic.nack）并且不再被重新投递requeue=false，则加入到死信交换机中DLX(dead-letter-exchange)
* TTL(time-to-live) 消息超时未消费
* 达到最大队列长度
* 应用场景，延迟任务，实现思路为创建一个没有消费者的队列，设置TTL过期时间，并绑定到死信交换机中。弊端： RabbitMQ 消息死亡并非异步化，而是阻塞的，需要每条消息的死亡相互独立这种场景.
