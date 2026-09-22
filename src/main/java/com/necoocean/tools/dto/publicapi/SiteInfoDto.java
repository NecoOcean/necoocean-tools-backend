package com.necoocean.tools.dto.publicapi;

/**
 * 站点公开信息。轮询间隔固定为 30 秒，不从后台配置读取。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class SiteInfoDto {

    private final String siteTitle;

    private final String homeIntro;

    private final String copyright;

    private final String aboutContent;

    private final String icpNumber;

    private final Boolean messageEnabled;

    private final Integer messagePollInterval;

    /**
     * @param siteTitle           站点标题，可以为 null
     * @param homeIntro           首页简介，可以为 null
     * @param copyright           版权，可以为 null
     * @param aboutContent        关于我，可以为 null
     * @param icpNumber           备案号，未填写时为 null
     * @param messageEnabled      留言板是否开放
     * @param messagePollInterval 轮询秒数
     */
    public SiteInfoDto(String siteTitle, String homeIntro, String copyright, String aboutContent, String icpNumber,
            Boolean messageEnabled, Integer messagePollInterval) {
        this.siteTitle = siteTitle;
        this.homeIntro = homeIntro;
        this.copyright = copyright;
        this.aboutContent = aboutContent;
        this.icpNumber = icpNumber;
        this.messageEnabled = messageEnabled;
        this.messagePollInterval = messagePollInterval;
    }

    /**
     * 站点标题。对应配置键 site_name。
     *
     * @return 标题，可以为 null
     */
    public String getSiteTitle() {
        return siteTitle;
    }

    /**
     * 首页简介。
     *
     * @return 简介，可以为 null
     */
    public String getHomeIntro() {
        return homeIntro;
    }

    /**
     * 版权文案。
     *
     * @return 文案，可以为 null
     */
    public String getCopyright() {
        return copyright;
    }

    /**
     * 关于我。
     *
     * @return Markdown，可以为 null
     */
    public String getAboutContent() {
        return aboutContent;
    }

    /**
     * 备案号。
     *
     * @return 备案号，未填写时为 null
     */
    public String getIcpNumber() {
        return icpNumber;
    }

    /**
     * 留言板是否接受新留言。已有留言仍可查询。
     *
     * @return true 表示开放
     */
    public Boolean getMessageEnabled() {
        return messageEnabled;
    }

    /**
     * 留言列表轮询间隔。
     *
     * @return 秒
     */
    public Integer getMessagePollInterval() {
        return messagePollInterval;
    }

    /**
     * 不输出正文。
     *
     * @return 简短文本
     */
    @Override
    public String toString() {
        return "SiteInfoDto{messageEnabled=" + messageEnabled + '}';
    }
}
