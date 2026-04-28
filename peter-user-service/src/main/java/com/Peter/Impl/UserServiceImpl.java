package com.Peter.Impl;

import com.Peter.Constants;
import com.Peter.RolesEnums;
import com.Peter.UserService;
import com.Peter.dao.UserDao;
import com.Peter.dto.RegisterUserDto;
import com.Peter.dto.UpdateUserInfoDto;
import com.Peter.dto.UserInfoDto;
import com.Peter.entity.User;
import com.Peter.entity.UserExample;
import com.Peter.utils.PasswordUtils;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;
    @Value("${ip}")
    private String ip;
    @Value("${server.port:8489}")
    private String port;

    @Override
    public int register(RegisterUserDto registerUserDto) {
        try {
            log.info("注册用户-register-入参：{}", JSON.toJSONString(registerUserDto));
            User user = new User();
            BeanUtils.copyProperties(registerUserDto, user);//entity转dto
           //填充默认数据
            filledDefaultData(user,registerUserDto);
            int count=userDao.insertSelective(user);
            log.info("注册用户-register-出参：{}", count);
            return count;
        }
        catch (Exception e) {
            log.error("注册用户-register-异常：", e);
            return -1;
        }
    }

    private void filledDefaultData(User user, RegisterUserDto registerUserDto) {
        //默认角色
            user.setRole(registerUserDto.getRole()==null?(byte)RolesEnums.USER.getCode(): registerUserDto.getRole());
        //默认密码
        if(StringUtils.isBlank(user.getPassword())){
            user.setPassword(PasswordUtils.passwordWithMd5(Constants.USER_DEFAULT_PASSWORD));
        }else{
            user.setPassword(PasswordUtils.passwordWithMd5(user.getPassword()));
        }
         //默认头像
        if(StringUtils.isBlank(user.getAvatar())){
            String defaultAvatar="https://" + ip + " : "+ port+ "/user/file/default_avatar.png";
            user.setAvatar(defaultAvatar);
        }
    }

    @Override
    public UserInfoDto login(String username, String password) {
        try {
            log.info("用户登录-login-入参：{},{}", username, password);
            User user = new User();
            UserExample userExample = new UserExample();
            UserExample.Criteria criteria = userExample.createCriteria();
            criteria.andUsernameEqualTo(username);//查询用户名
            criteria.andIsDeleteEqualTo(0);
            userExample.setLimit(1);//限制条数
            List<User> users=userDao.selectByExample(userExample);
            if(CollectionUtils.isEmpty(users)){
                log.info("用户不存在：{}", username);
                return null;
            }
            User userfromDb = users.get(0);
            //密码校验
            if(PasswordUtils.passwordWithMd5(password).equals(userfromDb.getPassword())){
                UserInfoDto userInfoDto = new UserInfoDto();
                BeanUtils.copyProperties(userfromDb, userInfoDto);
                log.info("用户登录成功：{}", username);
                return userInfoDto;
            }
            return null;
        }
        catch (Exception e) {
            log.error("用户登录-login-异常：", e);
            return null;
        }
    }
    @Override
   public int updateUserInfo(UpdateUserInfoDto updateUserInfoDto){
        try{
            log.info("修改用户信息-updateUserInfo-入参：{}",JSON.toJSONString(updateUserInfoDto));
            Assert.isTrue(updateUserInfoDto!=null,"入参不能为空");
            Assert.isTrue(updateUserInfoDto.getId()!=null,"id不能为空");
            Assert.isTrue(StringUtils.isNotBlank(updateUserInfoDto.getUsername()),"用户名不能为空");
            //先查询用户
            User user=userDao.selectByPrimaryKey(updateUserInfoDto.getId());
            if(user==null){
                log.error("查不到对应的用户信息:{}",updateUserInfoDto.getId());
                return -1;
            }
            //校验
            cn.hutool.core.lang.Assert.isTrue(updateUserInfoDto.getUsername().equals(user.getUsername()),"不是相同的用户，无法操作");
            //修改用户信息
            User user1=buildUser(updateUserInfoDto);

            int count=userDao.updateByPrimaryKeySelective(user1);

            log.info("修改用户信息-updateUserInfo-出参：{}",count);
            return count;
        }catch (Exception e){
            log.error("修改用户信息-updateUserInfo-异常",e);
            return -1;
        }
    }
    @Override
    public int deleteUser(UpdateUserInfoDto updateUserInfoDto) {
        try {
            log.info("删除用户-deleteUser-入参：{}", JSON.toJSONString(updateUserInfoDto));
            //入参校验
            Assert.isTrue(updateUserInfoDto != null, "入参不能为空");
            Assert.isTrue(updateUserInfoDto.getId() != null, "id不能为空");
            Assert.isTrue(StringUtils.isNotBlank(updateUserInfoDto.getUsername()), "用户名不能为空");
            UserExample userExample = new UserExample();
            UserExample.Criteria criteria = userExample.createCriteria();
            criteria.andIdEqualTo(updateUserInfoDto.getId());
            criteria.andIsDeleteEqualTo(0);
            userExample.setLimit(1);
            //先查询
            List<User> users = userDao.selectByExample(userExample);
            if (CollectionUtils.isEmpty(users)) {
                log.info("用户不存在：{}", updateUserInfoDto.getId());
                return -1;
            }
            User userfromDb = users.get(0);
            Assert.isTrue(updateUserInfoDto.getUsername().equals(userfromDb.getUsername()), "不是相同的用户，无法操作");
            userfromDb.setIsDelete(1);//逻辑删除
            int count = userDao.updateByPrimaryKeySelective(userfromDb);
            log.info("删除用户-deleteUser-出参：{}", count);
            return count;
        }catch(Exception e){
                log.error("删除用户-deleteUser-异常：", e);
                return -1;
            }
    }


    private User buildUser(UpdateUserInfoDto updateUserInfoDto) {
        User user =new User();
        user.setId(updateUserInfoDto.getId());
        user.setUsername(updateUserInfoDto.getUsername());
        if(StringUtils.isNotBlank(updateUserInfoDto.getEmail())){
            user.setEmail(updateUserInfoDto.getEmail());
        }
        if(StringUtils.isNotBlank(updateUserInfoDto.getPhone())){
            user.setPhone(updateUserInfoDto.getPhone());
        }
        if(StringUtils.isNotBlank(updateUserInfoDto.getPassword())){
            user.setPassword(updateUserInfoDto.getPassword());
        }
        if(updateUserInfoDto.getDateOfBirth()!= null){
            user.setDateOfBirth(updateUserInfoDto.getDateOfBirth());
        }
        if(updateUserInfoDto.getStatus()!= null){
            user.setStatus(updateUserInfoDto.getStatus());
        }
        return user;
    }

    @Override
    public boolean isUsernameExists(String username) //检查字段username
    {
        if (StringUtils.isBlank(username)) {
            return false;
        }
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();// Criteria 查询sql条件
        criteria.andUsernameEqualTo(username);
        criteria.andIsDeleteEqualTo(0);
        List<User> users = userDao.selectByExample(userExample);
        return !CollectionUtils.isEmpty(users);
    }

    @Override
    public boolean isPhoneExists(String phone) {
        if (StringUtils.isBlank(phone)) {
            return false;
        }
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        criteria.andPhoneEqualTo(phone);
        criteria.andIsDeleteEqualTo(0);
        List<User> users = userDao.selectByExample(userExample);
        return !CollectionUtils.isEmpty(users);
    }
    @Override
    public boolean isEmailExists(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        criteria.andEmailEqualTo(email);
        criteria.andIsDeleteEqualTo(0);
        List<User> users = userDao.selectByExample(userExample);
        return !CollectionUtils.isEmpty(users);
    }



}
