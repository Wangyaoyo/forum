package com.study.forum.controller;

import com.study.forum.annotation.LoginRequired;
import com.study.forum.pojo.User;
import com.study.forum.service.LikeService;
import com.study.forum.service.UserService;
import com.study.forum.util.CommunityUtil;
import com.study.forum.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author wy
 * @version 1.0
 */
@RequestMapping("/user")
@Controller
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    /* 头像存储的磁盘路径 */
    @Value("${community.path.upload}")
    private String upload;

    /* IP:port */
    @Value("${community.path.domain}")
    private String domain;


    @LoginRequired
    @RequestMapping(value = "/setting", method = RequestMethod.GET)
    public String getSetting() {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String upload(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没选择图片!");
            logger.error("您还没选择图片!");
            return "/site/setting";
        }
        // 将头像数据存储到磁盘
        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.indexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确！");
            logger.error("文件的格式不正确！");
            return "/site/setting";
        }
        filename = CommunityUtil.generateUUID() + suffix;

        // 根据id更新user中的头像url
        // http://localhost:8080/user/headerurl/filename
        User user = hostHolder.getUser();
        String headerUrl = domain + "user/headerurl/" + filename;
        if (userService.updateHeader(user.getId(), headerUrl) <= 0) {
            logger.info("更新头像:{}失败！", headerUrl);
        }

        // 存储文件至：D:/projectForWork/forum-data/filename.suffix
        File dest = new File(upload + filename);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("存储文件出错：{}", e);
        }
        logger.info("id为{}的用户头像存储至{}", user.getId(), dest.getName());
        return "redirect:/index";
    }


    @RequestMapping(value = "/headerurl/{filename}", method = RequestMethod.GET)
    public void getHeaderUrl(@PathVariable("filename") String filename, HttpServletResponse response) {
        if (StringUtils.isBlank(filename)) {
            logger.error("未找到头像！");
            return;
        }
        filename = upload + filename;
        String suffix = filename.substring(filename.lastIndexOf("."));
        response.setContentType("image/" + suffix);
        try (
                OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(filename);
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败：{}", e);
        }
    }

    @LoginRequired
    @RequestMapping(value = "/password", method = RequestMethod.POST)
    public String changePassword(Model model, String oldPass, String newPass, String newPass2) {
        if (StringUtils.isBlank(oldPass) || StringUtils.isBlank(newPass) || StringUtils.isBlank(newPass2)) {
            model.addAttribute("passwordMsg", "密码不能为空!");
            return "/site/setting";
        }
        if (!newPass.equals(newPass2)) {
            model.addAttribute("newpassMsg", "新密码不一致！");
            return "/site/setting";
        }
        User user = hostHolder.getUser();
        String secret = CommunityUtil.md5(oldPass + user.getSalt());
        if (!user.getPassword().equals(secret)) {
            model.addAttribute("oldpassMsg", "原密码不正确！");
            return "/site/setting";
        }
        int i = userService.changePass(user.getId(), CommunityUtil.md5(newPass+user.getSalt()));
        logger.info("id为:{}的用户正在修改密码", user.getId());
        if(i <= 0){
            model.addAttribute("passwordMsg", "修改失败！");
            return "/site/setting";
        }else
            return "redirect:/index";
    }

    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }

        // 用户
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        return "/site/profile";
    }
}
