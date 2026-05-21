package com.Peter.common;

import cn.hutool.core.util.ObjectUtil;
import com.Peter.UserService;
import com.Peter.dto.UserInfoDto;
import com.Peter.dto.UserTokenInfoDto;
import com.Peter.enums.ResultCodeEnums;
import com.Peter.exception.CustomException;
import com.Peter.utils.Constants;
import com.alibaba.fastjson2.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {
    @Resource
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 从http请求的header中获取token
        String token = request.getHeader(Constants.TOKEN);
        if (ObjectUtil.isEmpty(token)) {
            // 如果没拿到，从参数里再拿一次
            token = request.getParameter(Constants.TOKEN);
        }
        // 2. 开始执行认证
        if (ObjectUtil.isEmpty(token)) {
            return true;
//            throw new CustomException(ResultCodeEnum.TOKEN_INVALID_ERROR);
        }
        UserInfoDto userInfoDto = new UserInfoDto();
        try {
            // 解析token获取存储的数据
            String tokenData = JWT.decode(token).getAudience().get(0);
            UserTokenInfoDto userTokenInfoDto = JSONObject.parseObject(tokenData, UserTokenInfoDto.class);
            BeanUtils.copyProperties(userTokenInfoDto, userInfoDto);

        } catch (Exception e) {
            throw new CustomException(ResultCodeEnums.TOKEN_CHECK_ERROR);
        }
        if (userInfoDto.getId()==null) {
            throw new CustomException(ResultCodeEnums.USER_NOT_EXIST_ERROR);
        }
        try {
            // 用户密码加签验证 token
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(userInfoDto.getPassword())).build();
            jwtVerifier.verify(token); // 验证token
        } catch (JWTVerificationException e) {
            throw new CustomException(ResultCodeEnums.TOKEN_CHECK_ERROR);
        }
        return true;
    }


}
