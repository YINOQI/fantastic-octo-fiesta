package com.lwl.social_media_platform.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * 点赞 实体
 */
@Data
public class Support {
    Long id;
    Long userId;
    Long treadsId;
    // 是否取消（0：是 1：否）
    @TableField(exist = false)
    Integer isCancel;
}
