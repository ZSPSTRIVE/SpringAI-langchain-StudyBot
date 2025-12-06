package com.qasystem.common.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果类 - 所有API接口的标准返回格式
 * 
 * 🎯 作用说明：
 * 封装所有API接口的响应数据,提供统一的返回格式
 * 就像快递包裹的标准格式:有状态码、消息、具体内容和时间戳
 * 
 * 📦 响应格式：
 * {
 *   "code": 200,              // 状态码(200成功,500失败)
 *   "message": "操作成功",    // 提示消息
 *   "data": {...},           // 实际数据
 *   "timestamp": 1642345678  // 时间戳
 * }
 * 
 * 💡 使用场景：
 * 1. 所有Controller方法的返回值
 * 2. 成功时返回Result.success(data)
 * 3. 失败时返回Result.error(message)
 * 4. 前端统一处理响应
 * 
 * 🔧 泛型说明：
 * <T> 代表data字段的类型,可以是任何类型
 * - Result<User>: data是User对象
 * - Result<List<Question>>: data是问题列表
 * - Result<Void>: 没有返回数据
 * 
 * @author QA System Team
 * @version 1.0
 */
@Data
public class Result<T> implements Serializable {
    
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    public Result(Integer code, String message, T data) {
        this();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "success");
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(500, message);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message);
    }
}

