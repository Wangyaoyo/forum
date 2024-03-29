package com.study.forum.controller;

import com.google.code.kaptcha.Producer;
import com.study.forum.pojo.User;
import com.study.forum.service.UserService;
import com.study.forum.util.CommunityConstant;
import com.study.forum.util.CommunityUtil;
import com.study.forum.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author wy
 * @version 1.0
 */
@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    UserService userService;

    @Autowired
    Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;


    /* 注册页面 */
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    /* 注册功能 */
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活!");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    /* 邮件激活账号 */
    @RequestMapping(value = "/activation/{id}/{code}", method = RequestMethod.GET)
    public String activation(Model model,
                             @PathVariable("id") Integer id,
                             @PathVariable("code") String code) {
        int result = userService.activate(id, code);
        if (result == CommunityConstant.ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功,您的账号已经可以正常使用了!");
            model.addAttribute("target", "/login");
        } else if (result == CommunityConstant.ACTIVATION_REPEATE) {
            model.addAttribute("msg", "无效操作,该账号已经激活过了!!");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    /* 登录页面 */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginPage() {
        return "/site/login";
    }

    @RequestMapping(value = "/kaptcha")
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        // 生成验证码和图片
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 保存进session
        // session.setAttribute("kaptcha", text);

        /* 使用redis重构(kaptchaOwner标识每一个用户，此时用户未登录，不能用userId标识)  */
        String kaptchaOwner = CommunityUtil.generateUUID();
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        // 过期时间60s
        cookie.setMaxAge(60);
        response.addCookie(cookie);
        response.setContentType("image/png");
        // 存入并设置过期时间
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);

        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败:{}", e.getMessage());
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code,
                        boolean rememberme,
//                        HttpSession session,
                        @CookieValue("kaptchaOwner") String kaptchaOwner,
                        HttpServletResponse response,
                        Model model) {
        // 校验code
        // String sessionCode = (String) session.getAttribute("kaptcha");

        /* redis重构：验证码 */
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        String kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);


        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确");
            logger.info("验证码不正确");
            return "/site/login";
        }

        // 用户名密码是否正确
        int seconds = rememberme ? CommunityConstant.REMEMBER_EXPIRED_SECONDS : CommunityConstant.DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, seconds);

        // 返回ticket 并放入session
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            response.addCookie(cookie);
            logger.info("用户{}登录成功！", username);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            logger.info("{}", map.get("usernameMsg"));
            logger.info("{}", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        logger.info("ticket为：{}欲登出！", ticket);
        userService.logout(ticket);
        logger.info("登出成功！");
        /* 清理security的上下文环境 */
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    @RequestMapping(value = "/forget", method = RequestMethod.GET)
    public String forgetPage() {
        return "/site/forget";
    }


    @RequestMapping(value = "/getCode", method = RequestMethod.GET)
    @ResponseBody
    public String getForgetCode(String email, Model model) {
        userService.getCode(email);
        return CommunityUtil.getJSONString(0);
    }

    /**
     * 重置密码：测试通过：root : wywy
     * @param email
     * @param code
     * @param pass
     * @param model
     * @return
     */
    @RequestMapping(value = "/resetpass", method = RequestMethod.GET)
    public String reset(String email, String code, String pass, Model model) {
        // 1.重置
        int res = userService.checkAndReset(email, code, pass);
        if(res == 1){
            return CommunityUtil.getJSONString(1, "重置失败！");
        }
        return "/site/login";
    }

}
