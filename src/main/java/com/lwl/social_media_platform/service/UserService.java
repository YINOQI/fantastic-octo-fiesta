package com.lwl.social_media_platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lwl.social_media_platform.domain.pojo.User;
import com.lwl.social_media_platform.domain.vo.UserLoginVo;
import com.lwl.social_media_platform.domain.vo.UserVo;

import java.io.IOException;

public interface UserService extends IService<User> {
    UserVo getUserById(Long id);

    UserVo updateUser(User user) throws IOException;

    UserLoginVo login(String username, String password);
}
