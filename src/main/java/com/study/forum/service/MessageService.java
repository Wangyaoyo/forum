package com.study.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.forum.mapper.MessageMapper;
import com.study.forum.pojo.Message;
import com.study.forum.util.CommunityConstant;
import com.study.forum.util.CommunityUtil;
import com.study.forum.util.SensitiveFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * @author wy
 * @version 1.0
 */
@Service
public class MessageService implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    private MessageMapper messageMapper;


    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * 查询某个会话所包含的私信
     *
     * @param conversationId
     * @return
     */
    public Page<Message> getConversation(String conversationId, Integer current, int limit) {
        if (StringUtils.isBlank(conversationId)) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        if (current == null)
            current = 0;
        QueryWrapper<Message> wrapper = new QueryWrapper();
        Page<Message> page = new Page<>(current, limit);
        wrapper.eq("conversation_id", conversationId);
        // 排除失效消息
        wrapper.ne("status", 2);
        messageMapper.selectPage(page, wrapper);
        return page;
    }

    /**
     * 得到某用户的私信列表
     *
     * @param userId
     * @param curerent
     * @param limit
     * @return
     */
    public Page<Message> getMessageList(int userId, Integer curerent, int limit) {
        if (curerent == null)
            curerent = 0;
        return messageMapper.getMessagesList(new Page<>(curerent, limit), userId);
    }

    public int getUnReadMessage(int userId, String conversationId) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("to_id", userId);
        wrapper.eq("conversation_id", conversationId);
        wrapper.eq("status", 0);
        return messageMapper.selectCount(wrapper);
    }

    /**
     * 发送一条私信
     *
     * @param message
     * @return
     */
    public int insertMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insert(message);
    }


    /**
     * 修改私信状态：已读/删除
     *
     * @param id
     * @param status
     */
    public void changeMessageStatus(Integer id, int status) {
        if (id == null) {
            throw new IllegalArgumentException("参数为空!");
        }
        UpdateWrapper<Message> wrapper = new UpdateWrapper<>();
        wrapper.set("status", status).eq("id", id);
        messageMapper.update(null, wrapper);
    }

    /**
     * 通知的消息总数(或某一类通知的未读/已读消息)
     *
     * @param userId
     * @param topic
     * @param read
     * @return
     */
    public int findNoticeCount(int userId, String topic, int read) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("to_id", userId)
                .eq("from_id", 1);
        if (!StringUtils.isBlank(topic)) {
            wrapper.eq("conversation_id", topic);
        }
        if (read == MESSAGE_UNDELETED) {
            wrapper.ne("status", MESSAGE_UNDELETED);
        } else {
            wrapper.eq("status", read);
        }
        return messageMapper.selectCount(wrapper);
    }


    /**
     * 查询某类通知的最近一条通知
     *
     * @param userId
     * @param topic
     * @return
     */
    public Message findLatestNotice(int userId, String topic) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("to_id", userId)
                .ne("status", CommunityConstant.MESSAGE_UNDELETED)
                .eq("from_id", 1)
                .eq("conversation_id", topic)
                .orderByDesc("id")
                .last("limit 1");
        Message message = messageMapper.selectOne(wrapper);
        return message;
    }

    /**
     * 查询某个主题所包含的通知列表(未读)
     *
     * @param userId
     * @param topic
     * @return
     */
    public Page<Message> selectNotices(int userId, String topic, Integer offset, Integer limit) {
        if (offset == null) {
            offset = 0;
        }
        Page<Message> messagePage = new Page<>(offset, limit);
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("to_id", userId)
                .ne("status", MESSAGE_UNDELETED)
                .eq("from_id", 1)
                .eq("conversation_id", topic)
                .orderByDesc("id");
        messageMapper.selectPage(messagePage, wrapper);
        return messagePage;
    }

}
