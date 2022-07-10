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