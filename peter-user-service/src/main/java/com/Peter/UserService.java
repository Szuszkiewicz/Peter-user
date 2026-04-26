package com.Peter;

import com.Peter.dto.RegisterUserDto;
import com.Peter.dto.UpdateUserInfoDto;
import com.Peter.dto.UserInfoDto;

public interface UserService {

    int register(RegisterUserDto registerUserDto);

    UserInfoDto login(String username, String password );

    int updateUserInfo(UpdateUserInfoDto updateUserInfoDto);

    int deleteUser(UpdateUserInfoDto updateUserInfoDto);

}
