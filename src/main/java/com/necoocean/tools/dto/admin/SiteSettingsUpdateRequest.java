package com.necoocean.tools.dto.admin;

/**
 * 站点设置部分更新。字段 setter 被调用即视为客户端显式传入，null 表示清空。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class SiteSettingsUpdateRequest {

    private String siteTitle;
    private boolean siteTitlePresent;
    private String homeIntro;
    private boolean homeIntroPresent;
    private String copyright;
    private boolean copyrightPresent;
    private String aboutContent;
    private boolean aboutContentPresent;
    private String privacyContent;
    private boolean privacyContentPresent;
    private String icpNumber;
    private boolean icpNumberPresent;
    private String announcement;
    private boolean announcementPresent;
    private Boolean messageEnabled;
    private boolean messageEnabledPresent;
    private String messageAuditMode;
    private boolean messageAuditModePresent;
    private String messageKeywords;
    private boolean messageKeywordsPresent;

    /** @return 站点标题 */
    public String getSiteTitle() {
        return siteTitle;
    }
    /** @param siteTitle 站点标题，可以为 null */
    public void setSiteTitle(String siteTitle) {
        this.siteTitlePresent = true;
        this.siteTitle = siteTitle;
    }
    /** @return 是否传入 site_title */
    public boolean isSiteTitlePresent() {
        return siteTitlePresent;
    }
    /** @return 首页简介 */
    public String getHomeIntro() {
        return homeIntro;
    }
    /** @param homeIntro 首页简介 */
    public void setHomeIntro(String homeIntro) {
        this.homeIntroPresent = true;
        this.homeIntro = homeIntro;
    }
    /** @return 是否传入 home_intro */
    public boolean isHomeIntroPresent() {
        return homeIntroPresent;
    }
    /** @return 版权 */
    public String getCopyright() {
        return copyright;
    }
    /** @param copyright 版权 */
    public void setCopyright(String copyright) {
        this.copyrightPresent = true;
        this.copyright = copyright;
    }
    /** @return 是否传入 copyright */
    public boolean isCopyrightPresent() {
        return copyrightPresent;
    }
    /** @return 关于我 */
    public String getAboutContent() {
        return aboutContent;
    }
    /** @param aboutContent 关于我 */
    public void setAboutContent(String aboutContent) {
        this.aboutContentPresent = true;
        this.aboutContent = aboutContent;
    }
    /** @return 是否传入 about_content */
    public boolean isAboutContentPresent() {
        return aboutContentPresent;
    }
    /** @return 隐私政策 */
    public String getPrivacyContent() {
        return privacyContent;
    }
    /** @param privacyContent 隐私政策 */
    public void setPrivacyContent(String privacyContent) {
        this.privacyContentPresent = true;
        this.privacyContent = privacyContent;
    }
    /** @return 是否传入 privacy_content */
    public boolean isPrivacyContentPresent() {
        return privacyContentPresent;
    }
    /** @return 备案号 */
    public String getIcpNumber() {
        return icpNumber;
    }
    /** @param icpNumber 备案号 */
    public void setIcpNumber(String icpNumber) {
        this.icpNumberPresent = true;
        this.icpNumber = icpNumber;
    }
    /** @return 是否传入 icp_number */
    public boolean isIcpNumberPresent() {
        return icpNumberPresent;
    }
    /** @return 公告 */
    public String getAnnouncement() {
        return announcement;
    }
    /** @param announcement 公告 */
    public void setAnnouncement(String announcement) {
        this.announcementPresent = true;
        this.announcement = announcement;
    }
    /** @return 是否传入 announcement */
    public boolean isAnnouncementPresent() {
        return announcementPresent;
    }
    /** @return 留言开关 */
    public Boolean getMessageEnabled() {
        return messageEnabled;
    }
    /** @param messageEnabled 留言开关 */
    public void setMessageEnabled(Boolean messageEnabled) {
        this.messageEnabledPresent = true;
        this.messageEnabled = messageEnabled;
    }
    /** @return 是否传入 message_enabled */
    public boolean isMessageEnabledPresent() {
        return messageEnabledPresent;
    }
    /** @return 审核模式 */
    public String getMessageAuditMode() {
        return messageAuditMode;
    }
    /** @param messageAuditMode 审核模式 */
    public void setMessageAuditMode(String messageAuditMode) {
        this.messageAuditModePresent = true;
        this.messageAuditMode = messageAuditMode;
    }
    /** @return 是否传入 message_audit_mode */
    public boolean isMessageAuditModePresent() {
        return messageAuditModePresent;
    }
    /** @return 关键词 */
    public String getMessageKeywords() {
        return messageKeywords;
    }
    /** @param messageKeywords 关键词 */
    public void setMessageKeywords(String messageKeywords) {
        this.messageKeywordsPresent = true;
        this.messageKeywords = messageKeywords;
    }
    /** @return 是否传入 message_keywords */
    public boolean isMessageKeywordsPresent() {
        return messageKeywordsPresent;
    }
        /**
     * 简短文本。
     *
     * @return 文本
     */
    @Override
    public String toString() {
        return "SiteSettingsUpdateRequest{}";
    }
}