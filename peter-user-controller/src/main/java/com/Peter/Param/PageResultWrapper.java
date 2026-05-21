package com.Peter.Param;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class PageResultWrapper<T> implements Serializable {
    private boolean success;
    private String errorCode;
    private String message;
    private Integer pageSize;
    private Integer pageIndex;
    private Integer total;
    private Integer totalPage;
    private List<T> data;
    private Map extendInfo;

    public  PageResultWrapper() {
    }

    public static <T> PageResultWrapper<T> absent(){
        PageResultWrapper<T> result = new PageResultWrapper<>();
        result.setSuccess(true);
        result.setData(new ArrayList<>());
        result.setTotal(0);
        result.setTotalPage(0);
        result.setMessage("暂无数据");
        return result;
    }

    public static <T>PageResultWrapper<T> page(List<T> data, Integer total, Integer pageIndex, Integer pageSize) {
        PageResultWrapper<T> ret=new PageResultWrapper();
        ret.data=data;
        ret.success=true;
        ret.total=total;
        ret.pageIndex=pageIndex;
        ret.pageSize=pageSize;
        return ret;
    }

    /**
     * 添加扩展信息
     */
    public PageResultWrapper<T> addExtendInfo(String key, Object value) {
        if (this.extendInfo == null) {
            this.extendInfo = new java.util.HashMap<>();
        }
        this.extendInfo.put(key, value);
        return this;
    }

    /**
     * 判断是否有数据
     */
    public boolean hasData() {
        return data != null && !data.isEmpty();
    }

    /**
     * 判断是否有上一页
     */
    public boolean hasPrevious() {
        return pageIndex > 1;
    }

    /**
     * 判断是否有下一页
     */
    public boolean hasNext() {
        return pageIndex < totalPage;
    }
}
