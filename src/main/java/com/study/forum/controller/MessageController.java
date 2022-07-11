package com.study.forum.controller;

import com.alibaba.fastjson.JSONObject;
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
import org.springframework.web.util.HtmlUtils;

import javax.jws.WebParam;
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
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
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
        int i = 3 / 0;
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

    @RequestMapping(value = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        Integer userId = hostHolder.getUser().getId();

        /* 查询评论类通知  */
        Message comment = messageService.findLatestNotice(userId, TOPIC_COMMENT);
        // 1 封装评论类数据
        Map<String, Object> commentVo = new HashMap<>();
        // 最新一条通知
        commentVo.put("message", comment);
        if (comment != null) {
            String content = HtmlUtils.htmlUnescape(comment.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            // 存入发送人的信息
            commentVo.put("user", userService.findUserById((Integer) data.get("userId")));
            commentVo.put("entityType", data.get("entityType"));
            commentVo.put("entityId", data.get("entityId"));
            commentVo.put("postId", data.get("postId"));
            commentVo.put("count", messageService.findNoticeCount(userId, TOPIC_COMMENT, MESSAGE_UNDELETED));
            commentVo.put("unread", messageService.findNoticeCount(userId, TOPIC_COMMENT, MESSAGE_UNREAD));
        }
        model.addAttribute("commentNotice", commentVo);


        /* 查询点赞类通知  */
        Message like = messageService.findLatestNotice(userId, TOPIC_LIKE);
        // 1 封装评论类数据
        Map<String, Object> likeVo = new HashMap<>();
        // 最新一条通知
        likeVo.put("message", like);
        if (like != null) {
            String content = HtmlUtils.htmlUnescape(like.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            // 存入发送人的信息
            likeVo.put("user", userService.findUserById((Integer) data.get("userId")));
            likeVo.put("entityType", data.get("entityType"));
            likeVo.put("entityId", data.get("entityId"));
            likeVo.put("postId", data.get("postId"));
            likeVo.put("count", messageService.findNoticeCount(userId, TOPIC_LIKE, MESSAGE_UNDELETED));
            likeVo.put("unread", messageService.findNoticeCount(userId, TOPIC_LIKE, MESSAGE_UNREAD));
        }
        model.addAttribute("likeNotice", likeVo);

        /* 查询关注类通知 */
        Message follow = messageService.findLatestNotice(userId, TOPIC_FOLLOW);
        // 1 封装评论类数据
        Map<String, Object> followVo = new HashMap<>();
        // 最新一条通知
        followVo.put("message", follow);
        if (follow != null) {
            String content = HtmlUtils.htmlUnescape(follow.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            // 存入发送人的信息
            followVo.put("user", userService.findUserById((Integer) data.get("userId")));
            followVo.put("entityType", data.get("entityType"));
            followVo.put("entityId", data.get("entityId"));
            followVo.put("count", messageService.findNoticeCount(userId, TOPIC_FOLLOW, MESSAGE_UNDELETED));
            followVo.put("unread", messageService.findNoticeCount(userId, TOPIC_FOLLOW, MESSAGE_UNREAD));
        }
        model.addAttribute("followNotice", followVo);

        // 三类消息总的未读消息数量
        int noticeUnreadCount = messageService.findNoticeCount(userId, null, MESSAGE_UNREAD);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        int unReadMessage = messageService.getUnReadMessage(userId, null);
        model.addAttribute("letterUnreadCount", unReadMessage);
        return "/site/notice";
    }


    @RequestMapping(value = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic,
                                  @RequestParam(value = "current", required = false) Integer current,
                                  Model model) {
        Integer userId = hostHolder.getUser().getId();
        /* 分页获取消息 */
        Page<Message> messagePage = messageService.selectNotices(userId, topic, current, 5);
        List<Map<String, Object>> noticeVOList = new ArrayList<>();
        if (messagePage.getTotal() > 0) {
            for (Message notice : messagePage.getRecords()) {
                Map<String, Object> map = new HashMap<>();
                map.put("notice", notice);
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                map.put("fromUser", userService.findUserById(notice.getFromId()));
                /* 封装到list中 */
                noticeVOList.add(map);
                /*  将消息设置成已读 */
                messageService.changeMessageStatus(notice.getId(), MESSAGE_HAS_READ);
            }
        }
        model.addAttribute("notices", noticeVOList);
        model.addAttribute("page", messagePage);
        return "/site/notice-detail";
    }
}
