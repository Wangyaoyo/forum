package com.study.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.forum.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wy
 * @version 1.0
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
