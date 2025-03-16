package com.lwl.social_media_platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lwl.social_media_platform.common.Result;
import com.lwl.social_media_platform.domain.pojo.Tag;

import java.util.List;

public interface TagService extends IService<Tag> {

    Result<List<Tag>> getTagList();

    Result<Tag> saveTag(Tag tag);

    Result<String> deleteTag(Long id);

    List<Tag> getTagDetails(Long treadId);
}
