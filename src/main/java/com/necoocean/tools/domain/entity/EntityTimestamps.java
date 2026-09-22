package com.necoocean.tools.domain.entity;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * 数据层时间。与接口约定一致，按东八区墙钟生成，不带时区偏移。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public final class EntityTimestamps {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private EntityTimestamps() {
    }

    /**
     * 当前东八区时间，截到秒，和库里的 DATETIME 对齐。
     *
     * @return 不含纳秒的本地时间
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(SHANGHAI).withNano(0);
    }

    /**
     * 把库里的东八区墙钟转成接口使用的带偏移时间。
     *
     * @param value 墙钟时间，可以为 null
     * @return 带 +08:00 的时间；入参为 null 时返回 null
     */
    public static OffsetDateTime toOffset(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atZone(SHANGHAI).toOffsetDateTime();
    }
}
