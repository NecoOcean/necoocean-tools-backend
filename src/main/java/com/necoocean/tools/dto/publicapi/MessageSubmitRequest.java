package com.necoocean.tools.dto.publicapi;

/**
 * 访客提交留言的请求体。非 BUG 类的条件字段由服务层丢弃。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class MessageSubmitRequest {

    private String nickname;

    private Integer category;

    private String content;

    private String contactEmail;

    private String osPlatform;

    private String toolVersion;

    private String reproSteps;

    /**
     * 昵称。空时服务层改用「匿名」。
     *
     * @return 昵称，可以为 null
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * @param nickname 昵称
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 留言分类，1 到 4。
     *
     * @return 分类，可以为 null
     */
    public Integer getCategory() {
        return category;
    }

    /**
     * @param category 分类
     */
    public void setCategory(Integer category) {
        this.category = category;
    }

    /**
     * 留言正文。
     *
     * @return 正文，可以为 null
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content 正文
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 联系邮箱，选填。
     *
     * @return 邮箱，可以为 null
     */
    public String getContactEmail() {
        return contactEmail;
    }

    /**
     * @param contactEmail 邮箱
     */
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    /**
     * 操作系统，仅 BUG 类保留。
     *
     * @return 操作系统，可以为 null
     */
    public String getOsPlatform() {
        return osPlatform;
    }

    /**
     * @param osPlatform 操作系统
     */
    public void setOsPlatform(String osPlatform) {
        this.osPlatform = osPlatform;
    }

    /**
     * 工具版本，仅 BUG 类保留。
     *
     * @return 版本，可以为 null
     */
    public String getToolVersion() {
        return toolVersion;
    }

    /**
     * @param toolVersion 版本
     */
    public void setToolVersion(String toolVersion) {
        this.toolVersion = toolVersion;
    }

    /**
     * 复现步骤，仅 BUG 类保留。
     *
     * @return 步骤，可以为 null
     */
    public String getReproSteps() {
        return reproSteps;
    }

    /**
     * @param reproSteps 步骤
     */
    public void setReproSteps(String reproSteps) {
        this.reproSteps = reproSteps;
    }
}
