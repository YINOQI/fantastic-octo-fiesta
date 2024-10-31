package com.lwl.social_media_platform.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.lwl.social_media_platform.common.BaseContext;
import com.lwl.social_media_platform.common.exception.LoginException;
import com.lwl.social_media_platform.domain.vo.UserLoginVo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.lwl.social_media_platform.utils.RedisConstant.USER_LOGIN_KEY;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        // 从请求头中获取令牌
        String token = request.getHeaders().getFirst("Authorization");

        String userStr = stringRedisTemplate.opsForValue().get(USER_LOGIN_KEY + token);
        if (StrUtil.isBlank(userStr)) {
            throw new LoginException("token令牌已过期或用户未登录");
        } else {
            UserLoginVo userLoginVo = JSONUtil.toBean(userStr, UserLoginVo.class);
            Long id = userLoginVo.getUser().getId();
            attributes.put("id", id.toString());
            stringRedisTemplate.expire(USER_LOGIN_KEY + token, 30, TimeUnit.MINUTES);
            return true;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception ex) {
        // 握手后处理逻辑（如果有）
    }


}
