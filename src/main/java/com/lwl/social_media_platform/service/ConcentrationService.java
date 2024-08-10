package com.lwl.social_media_platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lwl.social_media_platform.common.Result;
import com.lwl.social_media_platform.domain.dto.PageDTO;
import com.lwl.social_media_platform.domain.pojo.Concentration;
import com.lwl.social_media_platform.domain.query.ConcentrationPageQuery;
import com.lwl.social_media_platform.domain.vo.UserVo;

public interface ConcentrationService extends IService<Concentration> {

    Result<String> saveConcentration(Concentration concentration);

    Result<String> cancelConcentration(Long toUserId);

    /**
     * 获取关注该用户的粉丝列表
     * @param concentrationPageQuery 分页条件
     * @return 粉丝 userVo 分页
     */
    Result<PageDTO<UserVo>> getConcentration(ConcentrationPageQuery concentrationPageQuery);

    /**
     * 获取该用户的关注列表
     * @param concentrationPageQuery 分页条件
     * @return 关注的 userVo 分页
     */
    Result<PageDTO<UserVo>> getToConcentration(ConcentrationPageQuery concentrationPageQuery);

    /**
     * 该用户的粉丝数
     * @return 粉丝数
     */
    Result<Long> getConcentrationNum();

    /**
     * 该用户的关注数
     * @return 关注数
     */
    Result<Long> getToConcentrationNum();
}
