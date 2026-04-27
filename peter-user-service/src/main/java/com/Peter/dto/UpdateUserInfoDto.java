package com.Peter.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UpdateUserInfoDto {
    /**
     * 用户id
     */
    private Long id;
    /**
     * 用户名
     */
    private  String username;
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
     * 生日
     */
    private Date dateOfBirth;
    /**
     * 1:正常，2：冻结,3:注销
     */
    private Byte status;

}
