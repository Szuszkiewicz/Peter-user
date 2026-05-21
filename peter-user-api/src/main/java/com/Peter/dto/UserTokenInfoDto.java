package com.Peter.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UserTokenInfoDto {
    private Long id;

    private String username;

    private String password;

    private String email;

    private Date createdAt;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 1:正常，2：冻结,3:注销
     */
    private Byte status;

    /**
     * 用户生日
     */
    private Date dateOfBirth;

    /**
     * 1：普通用户，2：管理员
     */
    private Byte role;

    private Date updatedAt;

    /**
     * 0:正常，1：已删除
     */
    private Integer isDelete;

    private String avatar;

}
