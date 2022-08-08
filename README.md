## 秒杀系统

### 开发环境
1、JDK: 1.8
2、Mysql: 5.7
3、redis: 3.2
4、RabbitMQ: 4.X

### 启动说明
1、启动前，请配置 application.properties 中相关redis、mysql、rabbitmq地址。
2、执行data\seckill.sql中的所有sql
3、rabbitmq的队列名为seckill.queue
