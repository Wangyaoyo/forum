package com.study.forum.controller.interceptor;

import com.study.forum.pojo.User;
import com.study.forum.service.MessageService;
import com.study.forum.util.CommunityConstant;
import com.study.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 得到未读消息数量
 * @author wy
 * @version 1.0
 */
@Component
public class MessageIntercepter implements HandlerInterceptor, CommunityConstant {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    /**
     * 在调Controller之后，模板渲染之前执行即可
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user != null && modelAndView != null){
            int unReadMessageCount = messageService.getUnReadMessage(user.getId(), null);
            int noticeUnreadCount = messageService.findNoticeCount(user.getId(), null, MESSAGE_UNREAD);
            modelAndView.addObject("unReadMessageCount",unReadMessageCount);
            modelAndView.addObject("noticeUnreadCount",noticeUnreadCount);
            modelAndView.addObject("allUnreadCount", unReadMessageCount+noticeUnreadCount);
        }
    }
}
