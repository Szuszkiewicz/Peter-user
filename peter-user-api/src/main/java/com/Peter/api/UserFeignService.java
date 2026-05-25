package com.Peter.api;

import com.Peter.Param.BaseResult;
import com.Peter.dto.UserInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
//对外提供服务的接口
@FeignClient("peter-user")
public interface UserFeignService {

    @GetMapping("/user/query/user/info")
    public BaseResult<UserInfoDto> queryUserInfoById(@RequestParam("id") String id);


}
