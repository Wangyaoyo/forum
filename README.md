## 使用redis优化以往的模块
- 验证码存储从session修改到redis
  以前：验证码存储在session, 登录时将前端用户输入的和session中的比较是否一致
  现在：存储到redis，设置过期时间为60s，由于此时用户未登录，为每个用户生成随机字符串为键，存储验证码，在登录时从redis中获取以验证
  key: kapcha+随机字符串
  value: 验证码
- ticket存储从mysql修改到redis
  使用：拦截器在每个controller请求之前（preHandle）将cookie中存储的ticket获取到，使用ticket凭证去数据库查询用户信息
       在每个请求之后（postHandle）将user对象设置到modelAndView对象中以便前端页面使用
  LoginTicket的存储：
    以前：在登录时生成；存储在数据库中，每次根据ticket查询
    现在：现在在登录时生成；存储在redis
      key: ticket
      value: LoginTicket对象
- user从由ticket查询mysql获取修改到redis
  User的存储：
    以前：根据id在数据库查询
    现在：根据id在redis查询
  

## spring 整合kafka
- 引入依赖
- 配置server、 consumer
- 访问kafka
- 启动kafka(进入kafka目录)
bin\windows\kafka-server-start.bat config\server.properties
  
- 启动zookeeper 
bin\windows\zookeeper-server-start.bat config\zookeeper.properties
  

## kafka完成消息通知功能（在一个用户对另一个用户点赞、评论、关注等操作时，系统通知接收消息的用户）
- 开发Event对象
- 开发Producer和Consumer对象
- 在点赞、评论、关注发生时，封装Event,并用Producer发送消息，将消息存入消息队列
- Consumer从消息队列中获得Event对象，封装成Message存入数据库


## EasticSerach使用
- 下载安装修改配置文件配置环境变量
- 安装对应版本的中文分词插件并解压到plugins目录下(github)
- 下载postman工具(非必要)
- 启动es: 运行elasticsearch.bat 
  - 可用命令方式访问服务器
  - 可直接在postman工具发送post请求
- Spring整合es
  - 引入依赖
  - 配置es: cluster-name   cluster-nodes
  - 两个类：ElasticsearchTemplate  ElasticsearchRepository
  - 通过注解配置 @Document... : 建立和ES的联系
  - 创建接口继承es的接口
  - 测试类测试



## 权限控制
- 废弃之前的 拦截器登录检查
- 认证
  - 绕过springsecurity认证流程，采用系统原有的认证方案
- 授权
  - 对需要控制的请求配置
- CSRF配置
  - 防止CSRF攻击
  

### 权限控制实现
- 引入依赖
- 创建配置类
- 开发权限管理模块

## 置顶、加精、删除
- 引入thymeleaf+security5包
- 开发service
- 开发controller
  - 触发一个event事件
  - 在Consumer中添加监听一个主题的方法
- 为前端返回json状态码
- thymeleaf结合seurity包解决权限控制问题
  

## Redis高级数据类型
- HyperLogLog
  可以将数据合并，并将合并后的数据去重
- Bitmap
  按索引存储 true/false, 统计为真的个数, 并对数据做 与或非 运算
  一个连续的字符串，每一位的0/1代表真和假，按索引存取数据，支持按位存取，可以看成byte数组
  
### 业务场景
- uv: unique vistor：通过用户IP去重统计数据：关注访问量（包括登录与未登录用户、游客等 ）
- DAU: daily active user: 通过用户ID去重统计数据： 只包括登录用户
### 功能
- uv
  - 统计单日uv和区间段uv
- DAU
  - 统计单日DAU和区间段DAU
  
### 实现
- Service 实现以上方法
- 在拦截器中调用统计今日数据的方法
- Controller中实现对数据的查询和返回给页面的数据

总结：此处出现的bug: 管理员访问/data路径404，关闭idea清除浏览器缓存数据即可，气死！
   Edge页面未能完整显示的部分谷歌浏览器可以显示完全
- 使用redis高级数据类型完成统计 独立访问者 和 每日活跃用户 的功能

## 任务执行和调度
### 线程池分类
- JDK线程池(Test类中测试方法)
  - ExecutorService(普通线程池)
  - ScheduleExecutorService(可执行定时任务的线程池)
  
- Spring线程池(spring配置文件中配置、注入)
  - ThreadPoolTaskExecutor
  - ThreadPoolTaskScheduler(在配置类中配置)
    - 定义一个包含定时任务的方法
  - 调用方式 ：方法 + 注解的方式
    - @Async：可被异步调用
    - @Schedule : 标识一个定时任务方法
- 分布式定时任务
  - Spring Quartz
    - 依赖于DB
    - 提前创建数据库表
  
### 问题
- 分布式环境下各个服务器都有的定时任务会重复执行
  - 不会数据共享，所以无法解决问题
- 定时任务组件改成：Quartz可以解决分布式环境下的定时任务问题
  - 程序运行的参数存储到数据库，可以对数据库的数据加锁保证线程安全
  
### Quartz的使用
- Job
  - 定义一个任务
  - 删除任务：写方法将表中的数据删掉，并将配置类中对应的@bean注释掉
- Quartz配置类
  - JobDetail
    配置这个任务
  - Trigger
    配置这个任务
  
配置好程序启动时Quartz读取这个信息，并存入表中，数据初始化好之后，配置便不再使用

### 热帖排行
#### 业务层面
- 使用定时任务计算帖子的热帖分数(5min)
- 点赞加精评论时不立即算分，将分数变化的帖子放入缓存(Redis)，定时计算缓存中帖子的分数
  - 点赞、评论、加精时将帖子放入redis缓存
  - 新增帖子时为帖子初始化一个分数
  
#### 代码层面
- 新建一个Job
  - 计算分数并刷新帖子( mysql和 es)
- 配置类中增加 bean

## 跳过生成长图和上传文件到云服务器

## 优化网站性能

### 加缓存并对前后进行压力测试
- 一级缓存
  - 数据缓存在应用服务器上，性能最好(和应用在一台服务器，速度快，空间小)
  - 工具：Ehcache、Guava、Caffeine
- 二级缓存
  - 数据缓存在Nosql数据库上，跨服务器(有网络开销，空间大)
  - 工具：Redis、Memcache
- 多级缓存
  - 一级缓存(本地缓存) > 二级缓存(分布式缓存) > DB
  - 优点：可以避免缓存雪崩导致大量请求直达DB，以提高系统的可用性
  
### 多级缓存的使用
#### 帖子热门程度的列表
原因：列表间隔一段时间计算一次分数，列表可以在一定时间内保持不变

#### 使用
- 一级缓存
  - 下载caffeine , 引入pom中
  - properties中对caffeine进行配置
  - 核心接口
    - cache(I)
      - LoadingCache(I)
        - 排队等待返回数据
        - AsyncCache(I)
          - 并发的取数据，异步的
  - 缓存帖子列表和帖子总数
    
  - 使用@PostConstruct初始化缓存
  - userId为0时走缓存，即 首页
  - 修改代码：使之兼容之前的逻辑  
  - 配置Jmeter进行性能测试
    - 注释掉 @Aspect
    - 启动应用
  - 性能比较：
    - 不加Caffeine : 吞吐量：17/sec
    - 加Caffeine : 吞吐量：194/sec