package com.necoocean.tools.common;

import java.util.Objects;

/**
 * 统一响应信封。失败时 data 为 null，成功文案固定为 ok。
 *
 * @param <T> data 的类型
 * @author NecoOcean
 * @date 2026/09/21
 */
public final class ApiResponse<T> {

    private final int code;

    private final String message;

    private final T data;

    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功信封。
     *
     * @param data 业务数据，允许为 null
     * @param <T>  data 的类型
     * @return code 为 0 的信封
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }

    /**
     * 失败信封。文案取错误码上的标准文案，不接受调用方改写。
     *
     * @param errorCode 错误码，不能为空
     * @param <T>       data 的类型
     * @return data 为 null 的信封
     */
    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        Objects.requireNonNull(errorCode, "errorCode");
        return new ApiResponse<T>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 业务错误码。
     *
     * @return 错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * 面向用户的文案。
     *
     * @return 文案
     */
    public String getMessage() {
        return message;
    }

    /**
     * 业务数据。
     *
     * @return 成功时的数据；失败时为 null
     */
    public T getData() {
        return data;
    }

    /**
     * 不输出 data 内容，避免日志里带出邮箱或留言正文。
     *
     * @return 不含 data 内容的文本
     */
    @Override
    public String toString() {
        return "ApiResponse{code=" + code + ", message='" + message + "', hasData=" + (data != null) + '}';
    }
}
