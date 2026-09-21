package com.necoocean.tools.common;

/**
 * 附录 C 的错误码。0 与 30 个业务码来自错误码表；{@link #ROUTE_NOT_FOUND} 只用于未匹配路由，避免返回 HTML。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
public enum ErrorCode {

    /** 成功。 */
    SUCCESS(0, 200, "ok"),

    /** 未登录或会话失效。 */
    UNAUTHORIZED(40100, 401, "请先登录。"),

    /** 无操作权限。 */
    FORBIDDEN(40300, 403, "无操作权限。"),

    /** 原密码错误。 */
    PASSWORD_INCORRECT(40301, 403, "原密码不正确。"),

    /** CSRF Token 缺失或无效。 */
    CSRF_INVALID(40302, 403, "请求校验失败，请刷新页面后重试。"),

    /** 未匹配到路由。不属于 30 个业务错误码。 */
    ROUTE_NOT_FOUND(40400, 404, "请求的资源不存在。"),

    /** 工具不存在或已下架。 */
    TOOL_NOT_FOUND(40401, 404, "该工具不存在或已下架。"),

    /** 留言不存在。 */
    MESSAGE_NOT_FOUND(40402, 404, "该留言不存在。"),

    /** 分类不存在。 */
    CATEGORY_NOT_FOUND(40403, 404, "该分类不存在。"),

    /** 文件不存在或工具已下架。 */
    FILE_NOT_FOUND(40404, 404, "文件不存在或已下架。"),

    /** 对象存储中文件缺失。 */
    OBJECT_MISSING(40405, 404, "文件暂时不可用，请稍后再试。"),

    /** 版本号重复。 */
    VERSION_CONFLICT(40901, 409, "该版本号已存在。"),

    /** slug 重复。 */
    SLUG_CONFLICT(40902, 409, "该 URL 标识已被占用。"),

    /** slug 创建后不可修改。 */
    SLUG_IMMUTABLE(40903, 409, "URL 标识创建后不可修改。"),

    /** 留言功能已关闭。 */
    MESSAGE_DISABLED(40904, 409, "留言功能暂时关闭。"),

    /** 分类下仍有工具，无法删除。 */
    CATEGORY_NOT_EMPTY(40905, 409, "该分类下仍有工具，请先转移或下架。"),

    /** 系统预置分类不可删除。 */
    CATEGORY_PROTECTED(40906, 409, "该分类为系统预置，不可删除。"),

    /** 分类名称重复。 */
    CATEGORY_NAME_CONFLICT(40907, 409, "该分类名称已存在。"),

    /** 留言已审核，不可重复审核。 */
    MESSAGE_ALREADY_AUDITED(40908, 409, "该留言已审核。"),

    /** 通用参数错误。 */
    PARAM_INVALID(42200, 422, "提交内容有误，请检查后重试。"),

    /** 邮箱格式错误。 */
    EMAIL_INVALID(42201, 422, "邮箱格式不正确，可留空。"),

    /** 昵称为空或超长。 */
    NICKNAME_INVALID(42202, 422, "昵称不能超过 50 个字符。"),

    /** 留言分类缺失或非法。 */
    MESSAGE_CATEGORY_INVALID(42203, 422, "请选择留言分类。"),

    /** 留言内容为空或超长。 */
    CONTENT_INVALID(42204, 422, "留言内容不能为空，且不超过 2000 字。"),

    /** BUG 附加字段超长。 */
    BUG_FIELD_INVALID(42205, 422, "填写内容过长，请精简后重试。"),

    /** 文件类型不允许。 */
    FILE_TYPE_REJECTED(42206, 422, "该类型文件不允许上传。"),

    /** 文件超过上限。 */
    FILE_TOO_LARGE(42207, 422, "文件大小超过 200 MB 上限。"),

    /** 封面图格式或大小不合规。 */
    COVER_INVALID(42208, 422, "封面图需为 JPG/PNG/WebP，且不超过 2 MB。"),

    /** sha256 格式不合规。 */
    SHA256_INVALID(42209, 422, "文件校验值格式有误，请重新上传。"),

    /** 留言提交过于频繁。 */
    MESSAGE_RATE_LIMITED(42901, 429, "提交过于频繁，请稍后再试。"),

    /** 登录失败次数过多。 */
    LOGIN_RATE_LIMITED(42902, 429, "登录失败次数过多，请 15 分钟后再试。"),

    /** 服务端内部错误。对外不返回异常细节。 */
    INTERNAL_ERROR(50000, 500, "服务繁忙，请稍后再试。");

    private final int code;

    private final int httpStatus;

    private final String message;

    ErrorCode(int code, int httpStatus, String message) {
        CodeLayout.check(code, httpStatus);
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    /**
     * 业务错误码。0 表示成功。
     *
     * @return 错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * 与错误码对应的 HTTP 状态。
     *
     * @return HTTP 状态码
     */
    public int getHttpStatus() {
        return httpStatus;
    }

    /**
     * 可直接展示给用户的文案。
     *
     * @return 用户可见文案
     */
    public String getMessage() {
        return message;
    }

    /**
     * 错误码分段规则。单独成类，是为了在枚举常量初始化之前完成常量赋值。
     */
    private static final class CodeLayout {

        /** 业务码除以该数等于对应的 HTTP 状态。 */
        private static final int HTTP_STATUS_DIVISOR = 100;

        private CodeLayout() {
        }

        private static void check(int code, int httpStatus) {
            if (code != 0 && code / HTTP_STATUS_DIVISOR != httpStatus) {
                throw new IllegalArgumentException("error code and http status mismatch: " + code);
            }
        }
    }
}
