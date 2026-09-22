package com.necoocean.tools.dto.admin;

/**
 * C-29 完成登记。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class UploadCompleteRequest {

    private String sha256;

    private Long fileSize;

    /**
     * @return 64 位小写十六进制
     */
    public String getSha256() {
        return sha256;
    }

    /**
     * @param sha256 校验值
     */
    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    /**
     * @return 实际字节数
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * @param fileSize 字节数
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
}
