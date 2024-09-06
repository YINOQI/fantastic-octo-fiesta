package com.lwl.social_media_platform.domain.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class Treads {
    private Long id;
    private Long userId;
    private String content;
    private String state;
    private Long supportNum;
    private LocalDateTime createTime;
}
