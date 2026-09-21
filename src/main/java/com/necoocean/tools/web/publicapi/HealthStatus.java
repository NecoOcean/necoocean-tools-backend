package com.necoocean.tools.web.publicapi;

import java.util.Objects;

/**
 * 存活探测的数据体。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
public final class HealthStatus {

    /** 进程可以接受请求。 */
    public static final String STATUS_UP = "up";

    private final String status;

    /**
     * 创建存活状态。
     *
     * @param status 状态值，不能为空
     */
    public HealthStatus(String status) {
        this.status = Objects.requireNonNull(status, "status");
    }

    /**
     * 状态值。
     *
     * @return 状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "HealthStatus{status=" + status + '}';
    }
}
