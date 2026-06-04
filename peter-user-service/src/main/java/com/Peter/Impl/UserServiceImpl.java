package com.Peter.Impl;

import cn.hutool.core.lang.Validator;
import com.Peter.common.Constants;
import com.Peter.dto.*;
import com.Peter.enums.RolesEnums;
import com.Peter.UserService;
import com.Peter.dao.UserDao;
import com.Peter.entity.User;
import com.Peter.entity.UserExample;
import com.Peter.utils.PasswordUtils;
import com.Peter.utils.TokenUtils;
import com.alibaba.fastjson2.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;
    @Value("${ip}")
    private String ip;
    @Value("${server.port:8489}")
    private String port;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public int register(RegisterUserDto registerUserDto) {
        try {

            log.info("注册用户-register-入参：{}", JSON.toJSONString(registerUserDto));
            if(!registerUserDto.getIsAgreeContract()){
                log.warn("注册失败，请先同意用户协议");
                return -1;
            }
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
                //生成 token
                UserTokenInfoDto userTokenInfoDto = new UserTokenInfoDto();
                BeanUtils.copyProperties(userInfoDto, userTokenInfoDto);
                userTokenInfoDto.setPassword(null);
                String tokenData= JSON.toJSONString(userTokenInfoDto);
                String token= TokenUtils.createToken(tokenData,password);
                userInfoDto.setToken(token);//token作为前端后续请求后端的入参
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
    public boolean sendVerificationCode(String email){
        try{

            log.info("发送验证码-sendVerificationCode-入参：{}",email);
            Assert.isTrue(StringUtils.isNotBlank(email),"邮箱不能为空");
            Assert.isTrue(Validator.isEmail(email),"邮箱格式不正确");
            if(!isEmailExists( email)){
                log.warn("邮箱不存在：{}",email);
                return false;
            }
            //生成6位验证码
            String verificationCode= String.format("%06d", new Random().nextInt(new Random().nextInt(100,291),new Random().nextInt(305,999756)));
            //储存到redis
            String redisKey="Verification_Code:"+ email;
            redisTemplate.opsForValue().set(redisKey,verificationCode,5, TimeUnit.MINUTES);
            //发送邮件
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setFrom(fromEmail);
            simpleMailMessage.setTo(email);
            simpleMailMessage.setSubject("输入此临时验证码以重置密码");
            simpleMailMessage.setText("您的验证码为："+verificationCode+"\n\n验证码5分钟内有效，请勿泄露给其他人,如果并非您本人尝试修改密码，请忽略此电子邮件");

            javaMailSender.send(simpleMailMessage);
            log.info("发送验证码-sendVerificationCode-出参：{}",true);
            return true;
        }catch (Exception e){
            log.error("发送验证码-sendVerificationCode-异常",e);
            return false;
        }
    }
    @Override
    public int updateUserPassword(ResetPasswordDto resetPasswordDto){
        try{
            log.info("重置密码-updateUserPassword-入参：{}",JSON.toJSONString(resetPasswordDto));
            Assert.isTrue(resetPasswordDto!=null,"入参不能为空");
            Assert.isTrue(StringUtils.isNotBlank(resetPasswordDto.getEmail()),"邮箱不能为空");
            Assert.isTrue(Validator.isEmail(resetPasswordDto.getEmail()),"邮箱格式不正确");
            Assert.isTrue(isEmailExists(resetPasswordDto.getEmail()),"邮箱不存在");
            Assert.isTrue(StringUtils.isNotBlank(resetPasswordDto.getVerificationCode()),"验证码不能为空");
            Assert.isTrue(StringUtils.isNotBlank(resetPasswordDto.getNewPassword()),"新密码不能为空");
            Assert.isTrue(StringUtils.isNotBlank(resetPasswordDto.getConfirmPassword()),"确认密码不能为空");
            Assert.isTrue(Objects.equals(resetPasswordDto.getConfirmPassword(), resetPasswordDto.getNewPassword()),"两次输入的密码不一致");
            //从redis获取验证码校验
            String redisKey="Verification_Code:"+ resetPasswordDto.getEmail();
            String storedCode=redisTemplate.opsForValue().get(redisKey);
            if(!StringUtils.isNotBlank(storedCode)){
                log.warn("验证码已过期：{}",resetPasswordDto.getEmail());
                return -1;
            }
            if(!storedCode.equals(resetPasswordDto.getVerificationCode())){
                log.warn("验证码错误：{}",resetPasswordDto.getEmail());
                return -1;
            }
            //查询用户
            UserExample userExample = new UserExample();
            UserExample.Criteria criteria = userExample.createCriteria();
            criteria.andEmailEqualTo(resetPasswordDto.getEmail());
            criteria.andIsDeleteEqualTo(0);
            List<User> users = userDao.selectByExample(userExample);
            if(CollectionUtils.isEmpty(users)){
                log.warn("用户不存在：{}",resetPasswordDto.getEmail());
                return -1;
            }
            User user=users.get(0);
            //更新密码
            user.setPassword(PasswordUtils.passwordWithMd5(resetPasswordDto.getNewPassword()));
            //删除验证码
            redisTemplate.delete(redisKey);
            int count=userDao.updateByPrimaryKeySelective(user);
            log.info("重置密码-updateUserPassword-出参：{}",count);
            return count;
        }catch (Exception e){
            log.error("重置密码-updateUserPassword-异常",e);
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
    @Override
    public List<UserInfoDto> queryUserInfoByParam(QueryUserInfoDto queryUserInfoDto) {
        try {
            log.info("查询用户信息-deleteUser-入参：{}", JSON.toJSONString(queryUserInfoDto));

            UserExample userExample=getUserExample(queryUserInfoDto);

            List<User> users=userDao.selectByExample(userExample);

            List<UserInfoDto> userInfoDtos=buildUserInfoDtoList(users);
            log.info("查询用户信息-deleteUser-出参：{}", JSON.toJSONString(queryUserInfoDto));
            return userInfoDtos;
        }catch(Exception e){
            log.error("查询用户信息-deleteUser-异常：", e);
            return new ArrayList<>();
        }
    }
    @Override
    public PageInfo<User> queryUserListByPage(QueryUserInfoDto queryUserInfoDto, Integer pageNum, Integer pageSize){
        //可以直接查用户信息
        PageHelper.startPage(pageNum,pageSize);
        UserExample userExample=getUserExample(queryUserInfoDto, true);
        //额外生成一条MySQL语句
        List<User> users=userDao.selectByExample(userExample);
        return PageInfo.of(users);

    }



    private List<UserInfoDto> buildUserInfoDtoList(List<User> users) {
        if(CollectionUtils.isEmpty(users)){
            return new ArrayList<>();
        }
        List<UserInfoDto> userInfoDtos=new ArrayList<>();
        for(int i=0;i<users.size();i++){
            User user=users.get(i);
            UserInfoDto userInfoDto=new UserInfoDto();
            BeanUtils.copyProperties(user, userInfoDto);
            userInfoDtos.add(userInfoDto);
        }
        return userInfoDtos;
    }

    private UserExample getUserExample(QueryUserInfoDto queryUserInfoDto) {
        return getUserExample(queryUserInfoDto,false);
    }
    private UserExample getUserExample(QueryUserInfoDto queryUserInfoDto,boolean noLimit) {
        UserExample userExample=new UserExample();
        UserExample.Criteria criteria=userExample.createCriteria();
        if(!noLimit){
            userExample.setLimit(1000);
        }
        if(queryUserInfoDto.getId()!=null){
            criteria.andIdEqualTo(queryUserInfoDto.getId());
        }
        if(StringUtils.isNotBlank(queryUserInfoDto.getUsername())){
            criteria.andUsernameLike("%"+queryUserInfoDto.getUsername()+"%");
        }
        criteria.andIsDeleteEqualTo(0);
        return  userExample;
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
