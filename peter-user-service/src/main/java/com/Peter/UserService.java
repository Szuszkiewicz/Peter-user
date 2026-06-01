package com.Peter;

import com.Peter.dto.*;
import com.Peter.entity.User;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface UserService {

    int register(RegisterUserDto registerUserDto);

    UserInfoDto login(String username, String password );

    int updateUserInfo(UpdateUserInfoDto updateUserInfoDto);

    int deleteUser(UpdateUserInfoDto updateUserInfoDto);

    boolean isUsernameExists(String username);

    boolean isPhoneExists(String phone);

    boolean isEmailExists(String email);

    List<UserInfoDto> queryUserInfoByParam(QueryUserInfoDto queryUserInfoDto);

    PageInfo<User> queryUserListByPage(QueryUserInfoDto queryUserInfoDto, Integer pageNum, Integer pageSize);

    boolean sendVerificationCode(String email);

    int updateUserPassword(ResetPasswordDto resetPasswordDto);
}
