package com.Peter;

import lombok.Getter;

public enum UserStatusEnums {
    NORMAL(1, "正常"),
    FROZEN(2, "冻结"),
    DESTROY(3, "注销");
    private Integer code;

    private String message;

    UserStatusEnums(Integer code, String message){
        this.code = code;
        this.message = message;
    }
    public int getCode(){
        return code;
    }
    public String getMessage(){
        return message;
    }
}
