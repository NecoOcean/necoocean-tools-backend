package com.necoocean.tools.web.publicapi;

import com.necoocean.tools.common.ApiResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 存活探测。不属于附录 C 的业务接口，只确认进程返回 JSON 信封。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
@RestController
@RequestMapping("/api/v1/public")
public class HealthController {

    /**
     * 返回进程存活状态。
     *
     * @return 统一信封
     */
    @GetMapping("/health")
    public ApiResponse<HealthStatus> health() {
        return ApiResponse.success(new HealthStatus(HealthStatus.STATUS_UP));
    }
}
