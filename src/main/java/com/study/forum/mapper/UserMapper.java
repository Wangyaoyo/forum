package com.study.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.forum.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wy
 * @version 1.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
