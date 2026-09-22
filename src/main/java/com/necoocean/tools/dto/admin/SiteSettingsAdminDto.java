package com.necoocean.tools.dto.admin;

/**
 * 后台站点设置。接口字段名与库键做映射。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class SiteSettingsAdminDto {

    private final String siteTitle;
    private final String homeIntro;
    private final String copyright;
    private final String aboutContent;
    private final String privacyContent;
    private final String icpNumber;
    private final String announcement;
    private final Boolean messageEnabled;
    private final Integer messagePollInterval;
    private final String messageAuditMode;
    private final String messageKeywords;

    /**
     * @param siteTitle            站点标题
     * @param homeIntro            首页简介
     * @param copyright            版权
     * @param aboutContent         关于我
     * @param privacyContent       隐私政策
     * @param icpNumber            备案号
     * @param announcement         公告
     * @param messageEnabled       留言开关
     * @param messagePollInterval  轮询秒数，固定 30
     * @param messageAuditMode     审核模式
     * @param messageKeywords      关键词
     */
    public SiteSettingsAdminDto(String siteTitle, String homeIntro, String copyright, String aboutContent,
            String privacyContent, String icpNumber, String announcement, Boolean messageEnabled,
            Integer messagePollInterval, String messageAuditMode, String messageKeywords) {
        this.siteTitle = siteTitle;
        this.homeIntro = homeIntro;
        this.copyright = copyright;
        this.aboutContent = aboutContent;
        this.privacyContent = privacyContent;
        this.icpNumber = icpNumber;
        this.announcement = announcement;
        this.messageEnabled = messageEnabled;
        this.messagePollInterval = messagePollInterval;
        this.messageAuditMode = messageAuditMode;
        this.messageKeywords = messageKeywords;
    }

    /** @return 站点标题 */
    public String getSiteTitle() {
        return siteTitle;
    }
    /** @return 首页简介 */
    public String getHomeIntro() {
        return homeIntro;
    }
    /** @return 版权 */
    public String getCopyright() {
        return copyright;
    }
    /** @return 关于我 */
    public String getAboutContent() {
        return aboutContent;
    }
    /** @return 隐私政策 */
    public String getPrivacyContent() {
        return privacyContent;
    }
    /** @return 备案号 */
    public String getIcpNumber() {
        return icpNumber;
    }
    /** @return 公告 */
    public String getAnnouncement() {
        return announcement;
    }
    /** @return 留言开关 */
    public Boolean getMessageEnabled() {
        return messageEnabled;
    }
    /** @return 轮询秒数 */
    public Integer getMessagePollInterval() {
        return messagePollInterval;
    }
    /** @return 审核模式 */
    public String getMessageAuditMode() {
        return messageAuditMode;
    }
    /** @return 关键词 */
    public String getMessageKeywords() {
        return messageKeywords;
    }
        /**
     * 简短文本。
     *
     * @return 文本
     */
    @Override
    public String toString() {
        return "SiteSettingsAdminDto{messageEnabled=" + messageEnabled + '}';
    }
}