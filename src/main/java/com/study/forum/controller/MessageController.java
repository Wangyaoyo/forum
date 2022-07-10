package com.study.forum.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.forum.pojo.Message;
import com.study.forum.pojo.User;
import com.study.forum.service.MessageService;
import com.study.forum.service.UserService;
import com.study.forum.util.CommunityConstant;
import com.study.forum.util.HostHolder;
import com.study.forum.util.SensitiveFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author wy
 * @version 1.0
 */
@Controller
@RequestMapping("/letter")
public class MessageController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;


    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String messageList(@RequestParam(value = "current", required = false) Integer current,
                              Model model) {
        int userId = hostHolder.getUser().getId();
        Page<Message> page = messageService.getMessageList(userId, current, 10);
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (page.getRecords() != null) {
            for (Message message : page.getRecords()) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                int targetId = userId == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));
                Page<Message> list = messageService.getConversation(message.getConversationId(), 1, 10);
                map.put("letterCount", list.getTotal());
                map.put("unreadCount", messageService.getUnReadMessage(userId, message.getConversationId()));
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
                // 修改私信状态为已读
                if(hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0){
                    message.setStatus(MESSAGE_HAS_READ);
                    messageService.changeMessageStatus(message.getId(), message.getStatus());
                }
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
        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else
            return userService.findUserById(id0);
    }

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    @ResponseBody
    public String insert(Integer toId, String content) {
        if (toId == null) {
            logger.error("私信对象为空！");
        }
        int i = 3/0;
        Message message = new Message();
        message.setContent(content);
        message.setCreateTime(new Date());
        int fromId = hostHolder.getUser().getId();
        message.setFromId(fromId);
        message.setToId(toId);
        String cid = fromId > toId ? toId + "_" + fromId : fromId + "_" + toId;
        message.setConversationId(cid);
        messageService.insertMessage(message);
        return "0";
    }
}
