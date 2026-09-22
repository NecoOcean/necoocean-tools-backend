package com.necoocean.tools.dto.admin;

/**
 * C-28 申请上传凭证。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class UploadTicketRequest {

    private String displayName;

    private String ext;

    private Long fileSize;

    private String version;

    private String platform;

    /**
     * @return 原始文件名
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName 原始文件名
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return 扩展名，可带点
     */
    public String getExt() {
        return ext;
    }

    /**
     * @param ext 扩展名
     */
    public void setExt(String ext) {
        this.ext = ext;
    }

    /**
     * @return 声明字节数
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * @param fileSize 声明字节数
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * @return 版本号
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version 版本号
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return 平台
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * @param platform 平台
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
