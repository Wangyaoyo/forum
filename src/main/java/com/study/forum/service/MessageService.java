package com.study.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.forum.mapper.MessageMapper;
import com.study.forum.pojo.Message;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wy
 * @version 1.0
 */
@Service
public class MessageService {
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    private MessageMapper messageMapper;


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
        if(current == null)
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
        if(curerent == null)
            curerent = 0;
        return messageMapper.getMessagesList(new Page<>(curerent, limit), userId);
    }

    public int getUnReadMessage(int userId, String conversationId) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("from_id", userId);
        wrapper.eq("conversation_id", conversationId);
        wrapper.eq("status", 0);
        return messageMapper.selectCount(wrapper);
    }

}
