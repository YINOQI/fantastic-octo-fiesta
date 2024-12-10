package com.lwl.social_media_platform.domain.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TreadsPageQuery extends PageQuery{
    // 关键字
    private String key;

    // 根据用户查询
    private Long userId;
    private Long tagId;
}
