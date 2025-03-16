package com.lwl.social_media_platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lwl.social_media_platform.domain.pojo.Treads;
import com.lwl.social_media_platform.domain.vo.TreadsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TreadsMapper extends BaseMapper<Treads> {
    IPage<TreadsVo> getTreadsVoPage(@Param("userId") Long userId, IPage<TreadsVo> page,@Param("followId") String followId);
}
