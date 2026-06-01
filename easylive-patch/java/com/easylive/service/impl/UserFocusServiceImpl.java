package com.easylive.service.impl;

import java.util.Date;
import java.util.List;

import com.easylive.entity.po.UserFocus;
import com.easylive.entity.po.UserInfo;
import com.easylive.entity.query.UserFocusQuery;
import com.easylive.entity.query.UserInfoQuery;
import com.easylive.entity.vo.PaginationResultVo;
import com.easylive.entity.query.SimplePage;
import com.easylive.enums.PageSizeEnum;
import com.easylive.enums.ResponseEnum;
import com.easylive.enums.StatisticsTypeEnum;
import com.easylive.exception.BusinessException;
import com.easylive.mappers.UserInfoMapper;
import com.easylive.service.StatisticsInfoService;
import com.easylive.service.UserMessagePushService;
import com.easylive.service.UserMessageService;
import com.easylive.service.UserFocusService;
import com.easylive.mappers.UserFocusMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 与 Downloads/easylive-main 工程保持同步；关注/取关后 SSE 推送粉丝数、关注数。
 */
@Service("userFocusService")
public class UserFocusServiceImpl implements UserFocusService {

    @Resource
    private UserFocusMapper<UserFocus, UserFocusQuery> userFocusMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private StatisticsInfoService statisticsInfoService;

    @Resource
    private UserMessageService userMessageService;

    @Resource
    private UserMessagePushService userMessagePushService;

    public List<UserFocus> findListByParam(UserFocusQuery query) {
        return userFocusMapper.selectList(query);
    }

    public Integer findCountByParam(UserFocusQuery query) {
        return userFocusMapper.selectCount(query);
    }

    public PaginationResultVo<UserFocus> findListByPage(UserFocusQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSizeEnum.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<UserFocus> list = this.findListByParam(query);
        return new PaginationResultVo<>(count, page.getPageSize(), page.getPageNo(), list, page.getPageTotal());
    }

    public Integer add(UserFocus bean) {
        return userFocusMapper.insert(bean);
    }

    public Integer addBatch(List<UserFocus> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return userFocusMapper.insertBatch(listBean);
    }

    public Integer addOrUpdateBatch(List<UserFocus> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return userFocusMapper.insertOrUpdateBatch(listBean);
    }

    public UserFocus getUserFocusByUserIdAndFocusUserId(String userId, String focusUserId) {
        return userFocusMapper.selectByUserIdAndFocusUserId(userId, focusUserId);
    }

    public Integer updateUserFocusByUserIdAndFocusUserId(UserFocus bean, String userId, String focusUserId) {
        return userFocusMapper.updateByUserIdAndFocusUserId(bean, userId, focusUserId);
    }

    public Integer deleteUserFocusByUserIdAndFocusUserId(String userId, String focusUserId) {
        return userFocusMapper.deleteByUserIdAndFocusUserId(userId, focusUserId);
    }

    @Override
    public void focus(String userId, String focusUserId) {
        if (userId.equals(focusUserId)) {
            throw new BusinessException("不能关注自己");
        }
        UserInfo dbUserInfo = userInfoMapper.selectByUserId(focusUserId);
        if (dbUserInfo == null) {
            throw new BusinessException(ResponseEnum.CODE_600);
        }
        UserFocus dbUserFocus = userFocusMapper.selectByUserIdAndFocusUserId(userId, focusUserId);
        if (dbUserFocus != null) {
            return;
        }
        UserFocus focus = new UserFocus();
        focus.setUserId(userId);
        focus.setFocusUserId(focusUserId);
        focus.setFocusTime(new Date());
        userFocusMapper.insert(focus);
        statisticsInfoService.incrementStatistics(focusUserId, StatisticsTypeEnum.FANS.getType(), 1);
        userMessageService.saveFollowMessage(focusUserId, userId);
        pushFollowStats(userId, focusUserId);
    }

    @Override
    public void cancelFocus(String userId, String focusUserId) {
        userFocusMapper.deleteByUserIdAndFocusUserId(userId, focusUserId);
        statisticsInfoService.incrementStatistics(focusUserId, StatisticsTypeEnum.FANS.getType(), -1);
        userMessageService.removeFollowMessage(focusUserId, userId);
        pushFollowStats(userId, focusUserId);
    }

    private int countFans(String focusUserId) {
        UserFocusQuery query = new UserFocusQuery();
        query.setFocusUserId(focusUserId);
        Integer count = findCountByParam(query);
        return count == null ? 0 : count;
    }

    private int countFocus(String userId) {
        UserFocusQuery query = new UserFocusQuery();
        query.setUserId(userId);
        Integer count = findCountByParam(query);
        return count == null ? 0 : count;
    }

    private void pushFollowStats(String operatorUserId, String focusUserId) {
        if (userMessagePushService == null) {
            return;
        }
        userMessagePushService.onFansCountChanged(focusUserId, countFans(focusUserId));
        userMessagePushService.onFocusCountChanged(operatorUserId, countFocus(operatorUserId));
    }
}
