package com.study.forum.config;

import com.study.forum.util.CommunityConstant;
import com.study.forum.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * 管理员：nowcoder11 密码：123456
 * @author wy
 * @version 1.0
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    @Override
    public void configure(WebSecurity web) throws Exception {
        // 忽略静态资源的访问: 所有resources下的资源都可以直接访问
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 授权
        http.authorizeRequests()
                // 匹配到以下请求时
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/user/password",
                        "/publishpost",
                        "/comment/add/**",
                        "/letter/**",
                        "/letter/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                // 用户必须具有以下权限才可以访问
                .hasAnyAuthority(
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                )
                /* 版主可以置顶和加精 */
                .antMatchers(
                        "/top",
                        "/wonderful")
                .hasAnyAuthority(
                        AUTHORITY_MODERATOR
                )
                /* 管理员具有删帖的权限 */
                .antMatchers("/delete")
                .hasAnyAuthority(AUTHORITY_ADMIN)
                // 放行除此之外的所有请求
                .anyRequest().permitAll()
                // 禁用csrf
                .and().csrf().disable();

        // 权限不够时的处理
        http.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    // 没有登录
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                        // 根据 header 中的 key：x-requested-with获得请求方式
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            /* 异步请求(该值为XMLHttpRequest)：返回json字符串 */
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "你还没有登录哦!"));
                        } else {
                            /* 同步请求(该值为null)：重定向一个页面 */
                            response.sendRedirect("/login");
                        }
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {
                    // 权限不足
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "你没有访问此功能的权限!"));
                        } else {
                            response.sendRedirect("/denied");
                        }
                    }
                });

        // Security底层默认会拦截/logout请求,进行退出处理.
        // 为了在系统中执行我们自己的/logout退出代码，不执行security底层的逻辑，将其配置为一个不存在的路径
        http.logout().logoutUrl("/securitylogout");
    }
}
