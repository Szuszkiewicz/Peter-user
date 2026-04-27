package com.Peter;

public enum RolesEnums {
    USER(1, "普通用户"),
    ADMIN(2, "管理员"),
    ;
    int code;
    String Message;
    RolesEnums(int code,String Message){
        this.code=code;
        this.Message=Message;
    }
    public int getCode() {
        return code;
    }
    public String getMessage() {
        return Message;
    }
}
