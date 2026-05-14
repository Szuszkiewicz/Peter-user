package com.Peter.Param;

import com.Peter.enums.RolesEnums;
import lombok.Data;

import java.util.Date;

@Data
public class UserParam {
    /**
     * 主键id
     */
    private Long id;
    /**
     * 用户名
     */
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
    private  Byte status;
    /**
     * 用户生日
     */
    private Date dateOfBirth;
    /**
     * 角色
     * @see RolesEnums
     */
    private Byte role;
}
