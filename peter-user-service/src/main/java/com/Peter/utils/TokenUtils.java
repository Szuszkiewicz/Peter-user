package com.Peter.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.Peter.UserService;
import com.Peter.dto.QueryUserInfoDto;
import com.Peter.dto.UserInfoDto;
import com.Peter.dto.UserTokenInfoDto;
import com.alibaba.fastjson2.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Component;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class TokenUtils {
    private static UserService staticUserService;

    @Resource
    UserService userService;

    @Autowired
    private  static  RestTemplate  restTemplate;

    @PostConstruct
    public void setUserService(){staticUserService = userService;}
    /**
     * 生成Token
     */
    public static String createToken(String data,String sign){
        return JWT.create().withAudience(data)// 将UserId保存到token里面，作为载荷
        .withExpiresAt(DateUtil.offsetDay(new Date(), 7))// 设置过期时间
                .sign(Algorithm.HMAC256(sign));// 以password作为token的密钥
    }
    /**
     * 获取当前登录的用户信息
     */
    public static UserInfoDto getCurrentUser(){
     try{
         RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
         if(requestAttributes==null){
             return new UserInfoDto();
         }
         HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
         String token=request.getHeader(Constants.TOKEN);
         if(ObjectUtil.isNotEmpty(token)) {
             UserTokenInfoDto userTokenInfoDto = JSONObject.parseObject(JWT.decode(token).getAudience().get(0), UserTokenInfoDto.class);
             String userRole=JWT.decode(token).getAudience().get(0);
            String userId=userRole.split("-")[0];
             String role =userRole.split("-")[1];
             QueryUserInfoDto queryUserInfoDto=new QueryUserInfoDto();
             queryUserInfoDto.setId(Long.valueOf(userId));
             String url = "http://localhost:8489/query/user/info?id=" + userId;
             List<UserInfoDto> userInfoDtos =restTemplate.getForObject(url, List.class);//希望返回的是的类型是List
             if(CollectionUtils.isEmpty(userInfoDtos)){
                 return  new UserInfoDto();
             }else{
                 return userInfoDtos.getFirst();
             }
         }
     }catch (Exception e){
         log.error("获取当前信息出错",e);
     }
     return new UserInfoDto();//返回空的账号对象
  }
}
