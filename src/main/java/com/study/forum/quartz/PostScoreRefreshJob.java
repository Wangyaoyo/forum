package com.study.forum.quartz;

import com.study.forum.pojo.DiscussPost;
import com.study.forum.service.CommentService;
import com.study.forum.service.DiscussPostService;
import com.study.forum.service.ElasticSearchService;
import com.study.forum.service.LikeService;
import com.study.forum.util.CommunityConstant;
import com.study.forum.util.RedisKeyUtil;
import javafx.geometry.Pos;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 从redis中获取到需要计算分数的帖子(点赞、评论、加精)，此处完成更新帖子score的任务
 *
 * @author wy
 * @version 1.0
 */
public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败！", e);
        }
    }

    /**
     * 计算帖子的分数
     *
     * @param jobExecutionContext
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        /* 从redis中获取需要计算分数的postId */

        // 1. 判断redis中是否有数据存在
        String postKey = RedisKeyUtil.getPostKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(postKey);
        if (operations.size() == 0) {
            logger.info("【任务取消】 没有需要刷新的帖子！");
            return;
        }
        logger.info("【任务开始】 正在刷新帖子分数：" + operations.size());

        // 2. 计算分数
        while (operations.size() > 0) {
            this.refresh((Integer) operations.pop());
        }

        logger.info("【任务结束】 帖子分数刷新完毕！");
    }

    /**
     * 根据帖子Id刷新帖子的分数
     *
     * @param postId
     */
    public void refresh(int postId) {
        DiscussPost post = discussPostService.getById(postId);
        if (post == null) {
            logger.info("postId为{}的帖子为空！", postId);
            return;
        }

        /*  计算分数  */
        // 是否精华
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeService.count(ENTITY_TYPE_POST, postId);

        // 计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 分数 = 帖子权重 + 距离天数
        double score = Math.log10(Math.max(w, 1))
                + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        // 更新分数
        discussPostService.updateScore(postId, score);

        post.setScore(score);
        // 更新es数据库
        elasticSearchService.addPost(post);
    }
}
