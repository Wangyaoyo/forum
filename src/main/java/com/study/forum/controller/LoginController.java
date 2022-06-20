package com.study.forum.controller;

import com.study.forum.pojo.User;
import com.study.forum.service.UserService;
import com.study.forum.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

/**
 * @author wy
 * @version 1.0
 */
@Controller
public class LoginController {

    @Autowired
    UserService userService;

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if(map == null || map.isEmpty()){
            model.addAttribute("msg", "注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活!");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        }else{
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    @RequestMapping(value = "/activation/{id}/{code}", method = RequestMethod.GET)
    public String activation(Model model,
                             @PathVariable("id") Integer id,
                             @PathVariable("code") String code){
        int result = userService.activate(id, code);
        if(result == CommunityConstant.ACTIVATION_SUCCESS){
            model.addAttribute("msg", "激活成功,您的账号已经可以正常使用了!");
            model.addAttribute("target", "/login");
        }else if(result == CommunityConstant.ACTIVATION_REPEATE){
            model.addAttribute("msg", "无效操作,该账号已经激活过了!!");
            model.addAttribute("target", "/index");
        }else{
            model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(){
        return "/site/login";
    }

}
