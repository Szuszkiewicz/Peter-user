package com.Peter.Param;

import com.Peter.enums.ResultCodeEnums;

public class Result {
    private String code;
    private String message;
    private Object data;
    private Result(Object data) {
        this.data = data;
    }

    public Result() {
    }

    public static Result success() {
        Result tResult = new Result();
        tResult.setCode(ResultCodeEnums.SUCCESS.code);
        tResult.setMessage(ResultCodeEnums.SUCCESS.msg);
        return tResult;
    }

    public static Result success(Object data) {
        Result tResult = new Result (data);
        tResult.setCode(ResultCodeEnums.SUCCESS.code);
        tResult.setMessage(ResultCodeEnums.SUCCESS.msg);
        return tResult;
    }

    public static Result error() {
        Result tResult = new Result();
        tResult.setCode(ResultCodeEnums.SYSTEM_ERROR.code);
        tResult.setMessage(ResultCodeEnums.SYSTEM_ERROR.msg);
        return tResult;
    }

    public static Result error(String code, String message) {
        Result tResult = new Result();
        tResult.setCode(code);
        tResult.setMessage(message);
        return tResult;
    }

    public static Result error(ResultCodeEnums resultCodeEnum) {
        Result tResult = new Result();
        tResult.setCode(resultCodeEnum.code);
        tResult.setMessage(resultCodeEnum.msg);
        return tResult;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
