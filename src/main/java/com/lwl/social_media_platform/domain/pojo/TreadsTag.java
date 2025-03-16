package com.lwl.social_media_platform.domain.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class TreadsTag {
    private Long id;
    private Long treadsId;
    private Long tagId;
    private LocalDateTime createTime;
}
