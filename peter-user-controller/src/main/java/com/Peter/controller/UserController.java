package com.Peter.controller;

import com.Peter.Param.BaseResult;
import com.Peter.Param.PageResultWrapper;
import com.Peter.Param.UserParam;
import com.Peter.UserService;
import com.Peter.dto.QueryUserInfoDto;
import com.Peter.dto.RegisterUserDto;
import com.Peter.dto.UpdateUserInfoDto;
import com.Peter.dto.UserInfoDto;
import com.Peter.entity.User;
import com.Peter.utils.BaseResultUtils;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
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
        log.info("注册用户-controller-入参：{}", userParam);
        //步骤一：参数校验
        checkRegisterUserParam(userParam);
        //步骤二：用户注册
        RegisterUserDto registerUserDto = new RegisterUserDto();
        BeanUtils.copyProperties(userParam, registerUserDto);//前面的是数据来源，后面是数据接收方
        int count= userService.register(registerUserDto);
        log.info(count>0?"注册成功":"注册失败");
        if(count>0){
            return BaseResultUtils.generateSuccess(count>0);
        }else {
            return BaseResultUtils.generateError("注册失败");
        }
    }
    @PostMapping("/login")
    public BaseResult<UserInfoDto> login(@RequestBody UserParam userParam){
        log.info("用户登录-controller-入参：{}", userParam);
        //步骤一：参数校验
        checkLoginUserParam(userParam);
        //步骤二：用户登录
        UserInfoDto userInfoDto = userService.login(userParam.getUsername(), userParam.getPassword());
        log.info(userInfoDto!=null?"登录成功":"登录失败");
        //步骤三：返回值
        if(userInfoDto!=null){
            return BaseResultUtils.generateSuccess(userInfoDto);
        }else{
            return BaseResultUtils.generateError("登录失败，请检查用户名或密码是否正确");
        }
    }
    @GetMapping("/query/user/info")
    public BaseResult<UserInfoDto> queryUserInfo(@RequestParam("id") String id){
        log.info("查询用户信息-controller-入参：{}",id);
        Assert.isTrue(StringUtils.isNotBlank( id), "用户ID不能为空");
        QueryUserInfoDto queryUserInfoDto = new QueryUserInfoDto();
        queryUserInfoDto.setId(Long.valueOf(id));
        List<UserInfoDto> userInfoDtos=userService.queryUserInfoByParam(queryUserInfoDto);
        if(CollectionUtils.isEmpty(userInfoDtos)){
            return BaseResultUtils.generateError("用户不存在");
        }
        return BaseResultUtils.generateSuccess(userInfoDtos.get(0));
    }
    @GetMapping("/query/user/list")
    public PageResultWrapper<UserInfoDto> queryUserList(UserParam userParam,
                                                        @RequestParam("PageNum") Integer pageNum,
                                                        @RequestParam("PageSize") Integer pageSize){
        log.info("查询用户信息-controller-入参：{},{},{}",userParam,pageSize,pageNum);
        QueryUserInfoDto queryUserInfoDto = new QueryUserInfoDto();
        BeanUtils.copyProperties(userParam, queryUserInfoDto);//userParam queryUserInfoDto

        PageInfo<User> userPageInfo = userService.queryUserListByPage(queryUserInfoDto, pageNum, pageSize);
        if(CollectionUtils.isEmpty(userPageInfo.getList())){
            return PageResultWrapper.absent();
        }
        List<UserInfoDto> resultList=new ArrayList<>();
        List<User> list=userPageInfo.getList();
        for(int i=0;i<list.size();i++){
            User user=list.get(i);
            UserInfoDto userInfoDto=new UserInfoDto();
            BeanUtils.copyProperties(user,userInfoDto);
            resultList.add(userInfoDto);
        }
        return  PageResultWrapper.page(resultList,(int) userPageInfo.getTotal(),pageNum,pageSize);
    }
    @PostMapping("/update/user/info")
    public BaseResult<Boolean> updateUserInfo(@RequestBody UserParam userParam){
        log.info("修改用户信息-controller-入参：{}", userParam);
        //步骤一：参数校验
        checkUpdateUserInfoParam(userParam);
        //步骤二：修改用户信息
        UpdateUserInfoDto updateUserInfoDto = buildUpdateUserInfoDto(userParam);
        int count =userService.updateUserInfo(updateUserInfoDto);
        log.info(count>0?"修改成功":"修改失败");
        if(count>0){
            return BaseResultUtils.generateSuccess(count>0);
        }else{
            return BaseResultUtils.generateError("修改失败");
        }

    }
    @PostMapping("/delete")
    public BaseResult<Boolean> delete(@RequestBody UserParam userParam){
        log.info("删除用户-controller-入参：{}", userParam);
        //步骤一：参数校验
        Assert.isTrue(userParam != null, "入参对象不能为空");
        Assert.isTrue(userParam.getId()!=null, "用户ID不能为空");
        Assert.isTrue(StringUtils.isNotBlank(userParam.getUsername()), "用户名不能为空");

        //步骤二：删除用户
        UpdateUserInfoDto updateUserInfoDto = buildUpdateUserInfoDto(userParam);
        int count =userService.deleteUser(updateUserInfoDto);
        log.info(count>0?"删除用户成功":"删除用户失败");
        if(count>0){return BaseResultUtils.generateSuccess(count>0);
        }else{
            return BaseResultUtils.generateError("删除用户失败");
        }

    }
    @PostMapping("/update/user/status")
    public BaseResult<Boolean> updateUserStatus(@RequestBody UserParam userParam){
        log.info("修改用户状态-controller-入参：{}", userParam);
        //步骤一：参数校验
        Assert.isTrue(userParam != null, "入参对象不能为空");
        Assert.isTrue(userParam.getStatus()!=null, "用户状态不能为空");
        Assert.isTrue(userParam.getUsername()!=null, "用户名不能为空");
        Assert.isTrue(userParam.getId()!=null, "用户ID不能为空");
        //步骤二：修改用户状态
        UpdateUserInfoDto updateUserInfoDto = buildUpdateUserInfoDto(userParam);
        int count =userService.updateUserInfo(updateUserInfoDto);
        log.info(count>0?"修改用户状态成功":"修改用户状态失败");
        if(count>0){ return BaseResultUtils.generateSuccess(count>0);
        }else {
            return BaseResultUtils.generateError("修改用户状态失败");
        }

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
        //Assert.isTrue(StringUtils.isNotBlank(userParam.getPassword()),"密码不能为空");
//        Assert.isTrue(StringUtils.isNotBlank(userParam.getEmail()),"邮箱不能为空");
//        Assert.isTrue(StringUtils.isNotBlank(userParam.getPhone()),"手机号不能为空");
//        Assert.isTrue(Validator.isEmail(userParam.getEmail()),"邮箱格式不正确");
//        Assert.isTrue(Validator.isMobile(userParam.getPhone()),"手机号格式不正确");
      Assert.isTrue(!userService.isUsernameExists(userParam.getUsername()), "用户名已存在");
    }

}

