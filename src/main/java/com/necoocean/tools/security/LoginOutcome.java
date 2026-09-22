package com.necoocean.tools.security;

/**
 * 登录尝试的结果。口令比对和失败计数在同一事务里完成后再交给接口层决定错误码。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public enum LoginOutcome {

    /** 口令正确，且当前不在锁定期。 */
    SUCCESS,

    /** 账号不存在或口令错误。对外使用同一个错误码。 */
    BAD_CREDENTIALS,

    /** 连续失败已达上限，锁定期未满。 */
    LOCKED
}
