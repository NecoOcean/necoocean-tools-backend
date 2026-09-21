package com.necoocean.tools.common;

import java.util.Objects;

/**
 * 可预期的业务失败。使用运行时异常，让事务能回滚，也不拿异常做正常分支判断。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;

    /**
     * 用标准错误码构造业务异常。
     *
     * @param errorCode 错误码，不能为空
     */
    public BizException(ErrorCode errorCode) {
        super(requireErrorCode(errorCode).getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 保留触发原因，便于日志打印完整因果链。
     *
     * @param errorCode 错误码，不能为空
     * @param cause     原因，不能为空
     */
    public BizException(ErrorCode errorCode, Throwable cause) {
        super(requireErrorCode(errorCode).getMessage(), Objects.requireNonNull(cause, "cause"));
        this.errorCode = errorCode;
    }

    /**
     * 业务错误码。
     *
     * @return 错误码
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    private static ErrorCode requireErrorCode(ErrorCode errorCode) {
        return Objects.requireNonNull(errorCode, "errorCode");
    }
}
