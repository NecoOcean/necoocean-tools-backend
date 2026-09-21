package com.necoocean.tools.logging;

/**
 * 访问日志里使用的追踪常量。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
public final class TraceConstants {

    /** MDC 中的追踪键。 */
    public static final String MDC_KEY = "traceId";

    /** 响应头里的追踪标识。 */
    public static final String RESPONSE_HEADER = "X-Trace-Id";

    /** 接受外部传入追踪号的最短长度。 */
    public static final int MIN_LENGTH = 8;

    /** 接受外部传入追踪号的最长长度，用来挡住日志注入。 */
    public static final int MAX_LENGTH = 64;

    private TraceConstants() {
    }
}
