package com.example.pojo.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 后端统一返回结果
 * 与前端API接口保持一致：code=0表示成功，非0表示失败
 * @param <T>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private Integer code; //编码：0成功，非0失败
    private String message; //消息信息
    private T data; //数据

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("操作成功");
        return result;
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(1);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败响应（自定义错误码）
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 参数错误
     */
    public static <T> Result<T> badRequest(String message) {
        Result<T> result = new Result<>();
        result.setCode(400);
        result.setMessage(message);
        return result;
    }

    /**
     * 未授权
     */
    public static <T> Result<T> unauthorized(String message) {
        Result<T> result = new Result<>();
        result.setCode(401);
        result.setMessage(message);
        return result;
    }

    /**
     * 禁止访问
     */
    public static <T> Result<T> forbidden(String message) {
        Result<T> result = new Result<>();
        result.setCode(403);
        result.setMessage(message);
        return result;
    }

    /**
     * 资源不存在
     */
    public static <T> Result<T> notFound(String message) {
        Result<T> result = new Result<>();
        result.setCode(404);
        result.setMessage(message);
        return result;
    }

    /**
     * 服务器内部错误
     */
    public static <T> Result<T> serverError(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

}
