package com.easylive.aspect;

import com.easylive.annotation.MessageInterceptor;
import com.easylive.component.RedisComponent;
import com.easylive.entity.constants.Constants;
import com.easylive.entity.dto.TokenUserInfoDto;
import com.easylive.entity.vo.ResponseVo;
import com.easylive.enums.MessageTypeEnum;
import com.easylive.enums.UserActionTypeEnum;
import com.easylive.service.UserMessageService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @projectName: easylive
 * @author: Li
 * @description:
 */

@Aspect
@Component
@Slf4j
public class MessageOperationAspect {

    @Resource
    private UserMessageService userMessageService;

    @Resource
    private RedisComponent redisComponent;


    private static final String PARAMETERS_VIDEO_ID = "videoId";
    private static final String PARAMETERS_ACTION_TYPE = "actionType";
    private static final String PARAMETERS_REPLY_COMMENT_ID = "replyCommentId";
    private static final String PARAMETERS_COMMENT_ID = "commentId";
    private static final String PARAMETERS_CONTENT = "content";
    private static final String PARAMETERS_AUDIT_REJECT_REASON = "reason";

    @Pointcut("@annotation(com.easylive.annotation.MessageInterceptor)")
    private void requestInterceptor() {

    }

    @Around("requestInterceptor()")
    public ResponseVo interceptorDo(ProceedingJoinPoint joinPoint) throws Throwable {
        ResponseVo responseVo = (ResponseVo) joinPoint.proceed();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        MessageInterceptor interceptor = method.getAnnotation(MessageInterceptor.class);
        if (interceptor != null) {
            saveMessage(interceptor, method.getParameters(), joinPoint.getArgs());
        }
        return responseVo;
    }

    private void saveMessage(MessageInterceptor interceptor, Parameter[] parameters, Object[] args) {
        String videoId = null;
        Integer actionType = null;
        Integer replyCommentId = null;
        Integer commentId = null;
        String content = null;
        for (int i = 0; i < parameters.length; i++) {
            if (PARAMETERS_VIDEO_ID.equals(parameters[i].getName())) {
                videoId = (String) args[i];
            } else if (PARAMETERS_ACTION_TYPE.equals(parameters[i].getName())) {
                actionType = (Integer) args[i];
            } else if (PARAMETERS_REPLY_COMMENT_ID.equals(parameters[i].getName())) {
                replyCommentId = (Integer) args[i];
            } else if (PARAMETERS_COMMENT_ID.equals(parameters[i].getName())) {
                commentId = (Integer) args[i];
            } else if (PARAMETERS_CONTENT.equals(parameters[i].getName())) {
                content = (String) args[i];
            } else if (PARAMETERS_AUDIT_REJECT_REASON.equals(parameters[i].getName())) {
                content = (String) args[i];
            }
        }
        if (replyCommentId == null || replyCommentId <= 0) {
            replyCommentId = commentId == null ? Constants.ZERO : commentId;
        }

        MessageTypeEnum messageTypeEnum = interceptor.messageType();
        if (UserActionTypeEnum.VIDEO_COLLECT.getType().equals(actionType)) {
            messageTypeEnum = MessageTypeEnum.COLLECTION;
        }

        TokenUserInfoDto userInfo = getRequestUserInfo();
        userMessageService.saveMessage(
                videoId,
                userInfo == null ? null : userInfo.getUserId(),
                messageTypeEnum,
                content,
                replyCommentId,
                actionType);
    }

    private TokenUserInfoDto getRequestUserInfo() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader(Constants.WEB_TOKEN);
        return redisComponent.getTokenInfo4Web(token);
    }
}
