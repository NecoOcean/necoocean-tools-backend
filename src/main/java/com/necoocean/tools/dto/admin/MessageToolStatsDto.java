package com.necoocean.tools.dto.admin;

import com.necoocean.tools.dto.publicapi.CategoryDto;

/**
 * 单工具留言统计。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class MessageToolStatsDto {

    private final CategoryDto tool;
    private final Long messageCount;
    private final Long bugCount;
    private final Long suggestionCount;

    /**
     * @param tool            工具
     * @param messageCount    留言总数
     * @param bugCount        BUG 数
     * @param suggestionCount 建议数
     */
    public MessageToolStatsDto(CategoryDto tool, Long messageCount, Long bugCount, Long suggestionCount) {
        this.tool = tool;
        this.messageCount = messageCount;
        this.bugCount = bugCount;
        this.suggestionCount = suggestionCount;
    }

    /** @return 工具 */
    public CategoryDto getTool() {
        return tool;
    }
    /** @return 留言总数 */
    public Long getMessageCount() {
        return messageCount;
    }
    /** @return BUG 数 */
    public Long getBugCount() {
        return bugCount;
    }
    /** @return 建议数 */
    public Long getSuggestionCount() {
        return suggestionCount;
    }
        /**
     * 简短文本。
     *
     * @return 文本
     */
    @Override
    public String toString() {
        return "MessageToolStatsDto{messageCount=" + messageCount + '}';
    }
}