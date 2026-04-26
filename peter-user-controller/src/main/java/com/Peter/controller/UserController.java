package com.Peter.controller;

import cn.hutool.core.lang.Validator;
import com.Peter.Param.BaseResult;
import com.Peter.Param.UserParam;
import com.Peter.UserService;
import com.Peter.dto.RegisterUserDto;
import com.Peter.dto.UpdateUserInfoDto;
import com.Peter.dto.UserInfoDto;
import com.Peter.utils.BaseResultUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    /**
     * 1.注册用户
     * 2.登录用户
     * 3.修改用户信息
     * 4.删除用户
     * 5.用户启用禁用
     * 6.密码加密
     * @return
     */
    @PostMapping("/register")
    public BaseResult<Boolean> registerUser(@RequestBody UserParam userParam){
        //步骤一：参数校验
        checkRegisterUserParam(userParam);
        //步骤二：用户注册
        RegisterUserDto registerUserDto = new RegisterUserDto();
        BeanUtils.copyProperties(userParam, registerUserDto);//前面的是数据来源，后面是数据接收方
        int count= userService.register(registerUserDto);
        //步骤三：返回值
        return BaseResultUtils.generateSuccess(count>0);
    }
    @PostMapping("/login")
    public BaseResult<UserInfoDto> login(@RequestBody UserParam userParam){
        //步骤一：参数校验
        checkLoginUserParam(userParam);
        //步骤二：用户登录
        UserInfoDto userInfoDto = userService.login(userParam.getUsername(), userParam.getPassword());
        //步骤三：返回值
        return BaseResultUtils.generateSuccess(userInfoDto);
    }
    @PostMapping("/update/user/info")
    public BaseResult<Boolean> updateUserInfo(@RequestBody UserParam userParam){
        //步骤一：参数校验
        checkUpdateUserInfoParam(userParam);
        //步骤二：修改用户信息
        UpdateUserInfoDto updateUserInfoDto = buildUpdateUserInfoDto(userParam);
        int count =userService.updateUserInfo(updateUserInfoDto);
        return BaseResultUtils.generateSuccess(count>0);
    }
    @PostMapping("/delete")
    public BaseResult<Boolean> delete(@RequestBody UserParam userParam){
        //步骤一：参数校验
        Assert.isTrue(userParam != null, "入参对象不能为空");
        Assert.isTrue(userParam.getId()!=null, "用户ID不能为空");
        Assert.isTrue(StringUtils.isNotBlank(userParam.getUsername()), "用户名不能为空");

        //步骤二：删除用户
        UpdateUserInfoDto updateUserInfoDto = buildUpdateUserInfoDto(userParam);
        int count =userService.deleteUser(updateUserInfoDto);
        //步骤三：返回值
        return BaseResultUtils.generateSuccess(count>0);
    }

    private static @NonNull UpdateUserInfoDto buildUpdateUserInfoDto(UserParam userParam) {
        UpdateUserInfoDto updateUserInfoDto = new UpdateUserInfoDto();
        BeanUtils.copyProperties(userParam, updateUserInfoDto);
        updateUserInfoDto.setId(Long.valueOf(userParam.getId()));
        return updateUserInfoDto;
    }

    private void checkUpdateUserInfoParam(UserParam userParam) {
        Assert.isTrue(userParam != null, "入参对象不能为空");
        Assert.isTrue(userParam.getId() != null, "用户ID不能为空");
        Assert.isTrue(StringUtils.isNotBlank(userParam.getUsername()), "用户名不能为空");
    }


    private void checkLoginUserParam(UserParam userParam) {
        Assert.isTrue(userParam!=null,"入参对象不能为空");
        Assert.isTrue(StringUtils.isNotBlank(userParam.getUsername()),"用户名不能为空");
        Assert.isTrue(StringUtils.isNotBlank(userParam.getPassword()),"密码不能为空");
    }

    private void checkRegisterUserParam(UserParam userParam) {
        Assert.isTrue(userParam!=null,"入参对象不能为空");
        Assert.isTrue(StringUtils.isNotBlank(userParam.getUsername()),"用户名不能为空");
        Assert.isTrue(StringUtils.isNotBlank(userParam.getPassword()),"密码不能为空");
        Assert.isTrue(StringUtils.isNotBlank(userParam.getEmail()),"邮箱不能为空");
        Assert.isTrue(StringUtils.isNotBlank(userParam.getPhone()),"手机号不能为空");
        Assert.isTrue(Validator.isEmail(userParam.getEmail()),"邮箱格式不正确");
        Assert.isTrue(Validator.isMobile(userParam.getPhone()),"手机号格式不正确");
    }
}
