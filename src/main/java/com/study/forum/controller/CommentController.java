package com.study.forum.controller;

import com.study.forum.pojo.Comment;
import com.study.forum.service.CommentService;
import com.study.forum.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * @author wy
 * @version 1.0
 */
@Controller
@RequestMapping("/comment")
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    /**
     * 增加评论
     * @param id
     * @param comment
     * @return
     */
    @RequestMapping(value = "/add/{postId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("postId") int id, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setCreateTime(new Date());
        comment.setStatus(0);

        int add = commentService.add(comment);
        return "redirect:/postdetail/" + id;
    }
}
