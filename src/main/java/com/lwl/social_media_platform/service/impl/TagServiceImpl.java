package com.lwl.social_media_platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lwl.social_media_platform.common.Result;
import com.lwl.social_media_platform.mapper.TagMapper;
import com.lwl.social_media_platform.domain.pojo.Tag;
import com.lwl.social_media_platform.service.TagService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
    @Override
    public Result<List<Tag>> getTagList() {
        return Result.success(this.list());
    }

    @Override
    public Result<Tag> saveTag(Tag tag) {
        this.save(tag);
        return Result.success(tag);
    }

    @Override
    public Result<String> deleteTag(Long id) {
        this.removeById(id);
        return Result.success("删除标签成功");
    }

    @Override
    public List<Tag> getTagDetails(Long treadId) {
        return this.baseMapper.getTagDetails(treadId);
    }
}
