package com.study.forum.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * @author wy
 * @version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@TableName("comment")
public class Comment {
    private int id;
    private int userId;
    // 帖子or评论(该评论有可能是帖子的或评论的)
    private int entityType;
    // 帖子或者评论的id
    private int entityId;
    private int targetId;
    private String content;
    private int status;
    private Date createTime;
}
