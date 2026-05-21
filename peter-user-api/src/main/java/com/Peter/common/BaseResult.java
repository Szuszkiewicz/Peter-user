package com.Peter.common;

import com.Peter.enums.ResultCodeEnums;
import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

@Data
public class BaseResult {
    /**
     * 响应码
     */
    private Integer code;
    /**
     * 是否成功
     * true: 成功
     * false: 失败
     */
    private Boolean success;
    /**
     * message
     */
    private String msg;
    /**
     * 具体的返回值
     */
    private T data;

    public BaseResult(Integer code, Boolean success, String msg, T data) {
        this.code = code;
        this.success = success;
        this.msg = msg;
        this.data = data;
    }
    public static BaseResult error() {
        BaseResult tResult = new BaseResult();
        tResult.setCode(Integer.valueOf(ResultCodeEnums.SYSTEM_ERROR.code));
        tResult.setMsg(ResultCodeEnums.SYSTEM_ERROR.msg);
        return tResult;
    }
    public static BaseResult error(String code, String msg) {
        BaseResult tResult = new BaseResult();
        tResult.setCode(Integer.valueOf(code));
        tResult.setMsg(msg);
        return tResult;
    }

    public BaseResult() {
    }
}
