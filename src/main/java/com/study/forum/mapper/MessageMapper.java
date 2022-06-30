package com.study.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.forum.pojo.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author wy
 * @version 1.0
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    @Select(value = "SELECT * FROM message WHERE id IN ( \n" +
            "SELECT MAX(id) FROM message \n" +
            "\tWHERE from_id != 1 \n" +
            "\tAND STATUS != 2 \n" +
            "\tAND (from_id = ${userId} OR to_id = ${userId})\n" +
            "\tGROUP BY conversation_id )")
    Page<Message> getMessagesList(Page<Message> page, @Param("userId") int userId);
}
