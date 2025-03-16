package com.lwl.social_media_platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lwl.social_media_platform.domain.pojo.Image;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ImageMapper extends BaseMapper<Image> {
    List<Image> getImageList(@Param("treadId") Long treadId);
}
