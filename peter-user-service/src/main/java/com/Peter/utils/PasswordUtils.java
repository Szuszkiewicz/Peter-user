package com.Peter.utils;

import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.util.DigestUtils;

public class PasswordUtils {
    public static String passwordWithMd5(String password) {
        return DigestUtil.md5Hex(password);
    }
}
