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
- 使用redis高级数据类型完成统计 独立访问者 和 每日活跃用户 的功能