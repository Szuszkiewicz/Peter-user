package com.Peter.Param;

import lombok.Data;

@Data
public class ResetPasswordParam {
    /**
     * 邮箱
     */
    private String email;
    /**
     * 验证码
     */
    private String VerificationCode;
    /**
     * 新密码
     */
    private String newPassword;
    /**
     * 确认密码
     */
    private String confirmPassword;
}
