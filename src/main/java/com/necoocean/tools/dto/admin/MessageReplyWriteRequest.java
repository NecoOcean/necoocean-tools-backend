package com.necoocean.tools.dto.admin;

/**
 * 管理员回复请求。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class MessageReplyWriteRequest {

    private String content;

    /**
     * 回复正文。
     *
     * @return 正文
     */
    public String getContent() {
        return content;
    }
    /**
     * 写入回复正文。
     *
     * @param content 正文
     */
    public void setContent(String content) {
        this.content = content;
    }

        /**
     * 简短文本。
     *
     * @return 文本
     */
    @Override
    public String toString() {
        return "MessageReplyWriteRequest{hasContent=" + (content != null) + '}';
    }
}