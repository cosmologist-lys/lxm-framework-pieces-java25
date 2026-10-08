package com.lxm.framework.web.jsonresult;

import com.lxm.framework.common.web.JsonResultInterface;
import com.lxm.framework.common.web.Result;
import com.lxm.framework.web.jsonresult.annotation.JsonResultFilter;
import com.lxm.framework.web.jsonresult.filter.JsonResultProvider;

import java.util.List;
import java.util.Map;

/**
 * @author twenty2
 */
public class JsonResult<T> implements JsonResultInterface<T> {

    private static final long serialVersionUID = 6398711247548069496L;

    private JsonResultProvider jsonResultProvider = null;

    private int code = 0;

    private String message = "";

    private T data;

    private String sign;

    private JsonResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> JsonResult<T> json() {
        return json(0, "ok");
    }

    public static <T> JsonResult<T> json(int code, String message) {
        return json(code, message, null);
    }

    public static <T> JsonResult<T> json(int code, String message, T data) {
        return new JsonResult<>(code, message, data);
    }

    public static JsonResult<Map<String, Object>> json(Map<String, Object> source) {
        Map<String, Object> map = new java.util.LinkedHashMap<>(source);
        int code = 0;
        if (map.containsKey("code")) {
            code = ((Number) map.get("code")).intValue();
            map.remove("code");
        }
        String message = "";
        if (map.containsKey("message")) {
            message = (String) map.get("message");
            map.remove("message");
        }
        return json(code, message, map);
    }

    public static <T> JsonResult<T> dataTable(T t) {
        return json(0, "", t);
    }

    public static JsonResult<List<?>> select2(int code, List<?> list) {
        return json(code, "", list);
    }

    public static JsonResult<List<?>> select2(List<?> list) {
        return select2(0, list);
    }

    public static <T> JsonResult<T> of(Result<T> result) {
        return json(result.getCode(), result.getMessage(), result.getReturnValue());
    }

    public static JsonResult<? extends Map<String, Object>> ofMap(
            Result<? extends Map<String, Object>> result) {
        return json(result.getCode(), result.getMessage(), result.getReturnValue());
    }

    public void include(Class<?> type, String[] fields) {
        chkProviderIsNull();
        this.jsonResultProvider.include(type, fields);
    }

    public void exclude(Class<?> type, String[] fields) {
        chkProviderIsNull();
        this.jsonResultProvider.exclude(type, fields);
    }

    public void filter(JsonResultFilter jsonResultFilter) {
        if (jsonResultFilter == null) {
            return;
        }
        if (jsonResultFilter.include().length != 0) {
            chkProviderIsNull();
            this.jsonResultProvider.include(jsonResultFilter.type(), jsonResultFilter.include());
        } else if (jsonResultFilter.exclude().length != 0) {
            chkProviderIsNull();
            this.jsonResultProvider.exclude(jsonResultFilter.type(), jsonResultFilter.exclude());
        }
    }

    private void chkProviderIsNull() {
        if (this.jsonResultProvider == null) {
            this.jsonResultProvider = new JsonResultProvider();
        }
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public JsonResultProvider getJsonResultProvider() {
        return jsonResultProvider;
    }

    public void setJsonResultProvider(JsonResultProvider jsonResultProvider) {
        this.jsonResultProvider = jsonResultProvider;
    }
}
