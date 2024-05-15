### netty-chat工程

im-server端，参照[即时网讯网sdk](http://www.52im.net/)

---

**1.消息流程介绍**

+ **client和server建立连接后，client发起登录消息，server在收到登录消息并校验成功后，会回复用户登录成功消息，并存储channel和userid之间的映射。
  **
+ **client定时向server端发送心跳消息，server收到后回复心跳消息，防止client端被server端断开连接。**
+ **client发送聊天消息，server收到后先投递到MQ中，再回复一个ACK应答消息给client端代表server已收到消息，此时client端可以进行本地存储，server端ACK
  应答消息内异步转发接收方，接收方在线则实时推送，否则走离线存储并极光推送，待下次接收方上线时再进行消息同步。**

**2.消息顺序性如何保证**

+ **单聊消息会生成会话id，群聊消息会生成群id，在进行投递到MQ中会根据会话id或者群id进行消息选择策略，保证消息顺序性。**

**3.会话列表数据获取**

+ **app/pc端会话消息从client本地数据库中进行查询，并在上线时先同步会话列表，再通过最后一条会话消息序列偏移（last_msgId）进行离线消息同步。
  **
+ **网页端会话列表从数据库中进行获取，最后一条消息（以会话id为Redis键）及数字红点（采用Redis
  hash数据结构，以每个用户id为键，hash中key单聊为对方id，群聊为群id，hash中value为未读数）显示则在每一次发送消息时存储到Redis中，方便获取。
  **

**4.群聊消息已读和未读存储**

+ **[群聊消息已读和未读策略链接](http://www.52im.net/thread-3054-1-1.html)**

**5.如何实现大量离线消息的可靠投递**

+ **[离线消息投递策略链接](http://www.52im.net/thread-3069-1-1.html)**

**6.钉钉即时消息服务设计**

+ **[钉钉即时消息服务设计链接](http://www.52im.net/thread-4012-1-1.html)**

**7.分布式IM架构设计**

+ **[分布式IM架构设计链接](http://www.52im.net/thread-4564-1-1.html)**

**8.从零开发移动端IM资料**

+ **[从零开发移动端IM资料链接](http://www.52im.net/thread-464-1-1.html)**