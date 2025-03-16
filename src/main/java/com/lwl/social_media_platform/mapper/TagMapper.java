package com.lwl.social_media_platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lwl.social_media_platform.domain.pojo.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    List<Tag> getTagDetails(@Param("treadId") Long treadId);
}
