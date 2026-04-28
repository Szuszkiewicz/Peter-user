package com.Peter.dto;

import lombok.Data;

import java.util.Date;
@Data
public class RegisterUserDto {
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 用户状态
     * 1:正常，2：冻结,3:注销
     */
    private  Integer status;
    /**
     * 用户生日
     */
    private Date dateOfBirth;
    /**
     * 角色
     */
    private Byte role;
}
