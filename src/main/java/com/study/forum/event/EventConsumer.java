package com.study.forum.event;

import com.alibaba.fastjson.JSONObject;
import com.study.forum.mapper.MessageMapper;
import com.study.forum.pojo.Event;
import com.study.forum.pojo.Message;
import com.study.forum.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wy
 * @version 1.0
 */
@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageMapper messageMapper;

    /*
        消息接收者接收指定事件，将事件封装成 Message 存入数据库
     */
    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record) {
        // 判空参数
        if (record == null || record.value() == null) {
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误！");
            return;
        }

        // 用event封装 Message对象
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);     // 系统通知
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());
        message.setToId(event.getEntityUserId());
        message.setStatus(MESSAGE_UNREAD);

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityUserId());

        // 将event中的map数据封装到message的content中，方便前端的消息提醒
        Map<String, Object> data = event.getData();
        if(!data.isEmpty()){
            for(String key : data.keySet()){
                content.put(key, data.get(key));
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageMapper.insert(message);
    }
}
