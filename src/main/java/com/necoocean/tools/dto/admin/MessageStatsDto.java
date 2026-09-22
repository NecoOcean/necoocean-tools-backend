package com.necoocean.tools.dto.admin;

import java.util.List;

/**
 * 反馈统计。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class MessageStatsDto {

    private final List<MessageToolStatsDto> list;

    /**
     * @param list 各工具统计
     */
    public MessageStatsDto(List<MessageToolStatsDto> list) {
        this.list = list == null ? List.of() : List.copyOf(list);
    }

    /** @return 统计列表 */
    public List<MessageToolStatsDto> getList() {
        return list;
    }
        /**
     * 简短文本。
     *
     * @return 文本
     */
    @Override
    public String toString() {
        return "MessageStatsDto{size=" + list.size() + '}';
    }
}