package com.study.forum.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.forum.pojo.Message;
import com.study.forum.pojo.User;
import com.study.forum.service.MessageService;
import com.study.forum.service.UserService;
import com.study.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wy
 * @version 1.0
 */
@Controller
@RequestMapping("/letter")
public class MessageController {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String messageList(@RequestParam(value = "current", required = false) Integer current,
                              Model model) {
        Page<Message> page = messageService.getMessageList(hostHolder.getUser().getId(), current, 10);
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (page.getRecords() != null) {
            for (Message message : page.getRecords()) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("target", userService.findUserById(message.getToId()));
                Page<Message> list = messageService.getConversation(message.getConversationId(), 1, 10);
                map.put("letterCount", list.getTotal());
                map.put("unreadCount", messageService.getUnReadMessage(message.getFromId(), message.getConversationId()));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);
        model.addAttribute("page", page);
        return "/site/letter";
    }

    @RequestMapping(value = "/detail/{conversationId}", method = RequestMethod.GET)
    public String messageDetail(@PathVariable("conversationId") String conversationId,
                                @RequestParam(value = "current", required = false) Integer current,
                                Model model) {
        Page<Message> page = messageService.getConversation(conversationId, current, 5);
        List<Map<String, Object>> letters = new ArrayList<>();
        if (page != null) {
            for (Message message : page.getRecords()) {
                Map<String, Object> map = new HashMap<>();
                map.put("fromUser", userService.findUserById(message.getFromId()));
                map.put("letter", message);
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);
        model.addAttribute("target", getLetterTarget(conversationId));
        model.addAttribute("page", page);
        return "/site/letter-detail";
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if(hostHolder.getUser().getId() == id0){
            return userService.findUserById(id1);
        }else
            return userService.findUserById(id0);
    }
}
