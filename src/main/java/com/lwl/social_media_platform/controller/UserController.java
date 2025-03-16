package com.lwl.social_media_platform.controller;

import com.lwl.social_media_platform.common.BaseContext;
import com.lwl.social_media_platform.common.Result;
import com.lwl.social_media_platform.domain.pojo.Concentration;
import com.lwl.social_media_platform.domain.pojo.User;
import com.lwl.social_media_platform.domain.vo.UserLoginVo;
import com.lwl.social_media_platform.domain.vo.UserVo;
import com.lwl.social_media_platform.service.ConcentrationService;
import com.lwl.social_media_platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ConcentrationService concentrationService;

    @PostMapping("/login")
    public Result<UserLoginVo> login(String username, String password) {
        return Result.success(userService.login(username, password));
    }
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        // 默认头像
        user.setPic("https://homework1015.oss-cn-beijing.aliyuncs.com/pic.png");
        userService.save(user);
        return Result.success(user);
    }
    @GetMapping("/getUser")
    public Result<List<User>> getUser(){
        List<User> list = userService.list();
        return Result.success(list);
    }

    @GetMapping("/getUserById")
    public Result<UserVo> getUserById(@RequestParam("id") Long id){
        UserVo userVo = userService.getUserById(id);
        Long currentUserId = BaseContext.getCurrentId();


        boolean isFollow = concentrationService.lambdaQuery()
                .eq(Concentration::getUserId, currentUserId)
                .eq(Concentration::getToUserId, userVo.getId())
                .exists();

        long followCount = concentrationService.getConcentrationNum(userVo.getId());
        long fansCount = concentrationService.getFansNum(userVo.getId());

        userVo.setIsFollow(isFollow)
                .setFansNum(fansCount)
                .setFollowNum(followCount);

        return Result.success(userVo);
    }

    @PostMapping("/update")
    public Result<UserVo> updateUser(@RequestBody User user) throws IOException {
        return Result.success(userService.updateUser(user));
    }
}
