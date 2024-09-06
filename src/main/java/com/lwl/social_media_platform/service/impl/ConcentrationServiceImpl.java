package com.lwl.social_media_platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lwl.social_media_platform.common.BaseContext;
import com.lwl.social_media_platform.common.Result;
import com.lwl.social_media_platform.domain.dto.PageDTO;
import com.lwl.social_media_platform.domain.pojo.Concentration;
import com.lwl.social_media_platform.domain.pojo.User;
import com.lwl.social_media_platform.domain.query.ConcentrationPageQuery;
import com.lwl.social_media_platform.domain.vo.UserConcentrationVo;
import com.lwl.social_media_platform.mapper.ConcentrationMapper;
import com.lwl.social_media_platform.service.ConcentrationService;
import com.lwl.social_media_platform.service.UserService;
import com.lwl.social_media_platform.utils.BeanUtils;
import com.lwl.social_media_platform.utils.CollUtils;
import com.lwl.social_media_platform.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.lwl.social_media_platform.utils.RedisConstant.FOLLOW_LIST_KEY;

@Service
@RequiredArgsConstructor
public class ConcentrationServiceImpl extends ServiceImpl<ConcentrationMapper, Concentration> implements ConcentrationService {
    private final UserService userService;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Result<String> saveConcentration(Concentration concentration) {
        Long userID = BaseContext.getCurrentId();
        concentration.setUserId(userID)
                .setCreateTime(LocalDateTime.now());
        this.save(concentration);
        stringRedisTemplate.opsForZSet().add(FOLLOW_LIST_KEY + concentration.getToUserId(), concentration.getUserId().toString(), System.currentTimeMillis());
        return Result.success("关注成功");
    }

    @Override
    public Result<String> cancelConcentration(Long toUserId) {
        Long userId = BaseContext.getCurrentId();
        this.lambdaUpdate()
                .eq(Concentration::getToUserId, toUserId)
                .eq(Concentration::getUserId, userId).remove();
        stringRedisTemplate.opsForZSet().remove(FOLLOW_LIST_KEY + toUserId, userId.toString());
        return Result.success("取消关注成功");
    }

    @Override
    public Result<PageDTO<UserConcentrationVo>> getFans(ConcentrationPageQuery concentrationPageQuery) {
        return Result.success(getUserVoPageDTO(Concentration::getToUserId, Concentration::getUserId, concentrationPageQuery));
    }

    @Override
    public Result<PageDTO<UserConcentrationVo>> getConcentration(ConcentrationPageQuery concentrationPageQuery) {
        return Result.success(getUserVoPageDTO(Concentration::getUserId, Concentration::getToUserId, concentrationPageQuery));
    }

    @Override
    public Long getFansNum(Long userId) {
        return getNum(Concentration::getToUserId, userId);
    }

    @Override
    public Long getConcentrationNum(Long userId) {
        return getNum(Concentration::getUserId, userId);
    }

    /**
     * 根据传入方法 判断是获取关注列表还是粉丝列表
     *
     * @param user                   获取当前用户关注列表/获取当前该用户的粉丝列表
     * @param toUser                 根据 {@param user } 获取详细用户信息
     * @param concentrationPageQuery 分页条件
     * @return userVo 分页
     */
    private PageDTO<UserConcentrationVo> getUserVoPageDTO(SFunction<Concentration, Long> user, SFunction<Concentration, Long> toUser, ConcentrationPageQuery concentrationPageQuery) {
        Long userId = concentrationPageQuery.getUserId();

        Page<Concentration> concentrationPage = this.lambdaQuery()
                .eq(user, userId)
                .page(concentrationPageQuery.toMpPageDefaultSortByCreateTimeDesc());

        List<Concentration> concentrationList = concentrationPage.getRecords();

        List<Long> toUserIdList = concentrationList.stream().map(toUser).toList();
        if (CollUtils.isNotEmpty(toUserIdList)) {

            List<User> toUserList = userService.lambdaQuery().in(User::getId, toUserIdList).list();

            List<UserConcentrationVo> toUserVoList = toUserList.stream().map(userItem -> {
                Long currentUserId = BaseContext.getCurrentId();
                boolean isFollow = this.lambdaQuery()
                        .eq(Concentration::getUserId, currentUserId)
                        .eq(Concentration::getToUserId, userItem.getId())
                        .exists();
                return BeanUtils.copyProperties(userItem, UserConcentrationVo.class).setIsFollow(isFollow);
            }).toList();

            return PageUtils.of(concentrationPage, toUserVoList);
        } else {
            return PageUtils.empty(concentrationPage);
        }
    }

    /**
     * 获取关注数
     *
     * @param function 根据传入方法判断获取关注数还是被关注数
     * @return 关注数
     */
    private Long getNum(SFunction<Concentration, Long> function, Long usrId) {
        return this.lambdaQuery().eq(function, usrId).count();
    }

}
