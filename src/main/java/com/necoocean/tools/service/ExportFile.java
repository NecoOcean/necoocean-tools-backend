package com.necoocean.tools.service;

/**
 * 导出文件结果。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public final class ExportFile {

    private final byte[] body;

    private final String contentType;

    private final String filename;

    /**
     * @param body        文件内容
     * @param contentType MIME
     * @param filename    下载文件名
     */
    public ExportFile(byte[] body, String contentType, String filename) {
        this.body = body == null ? new byte[0] : body.clone();
        this.contentType = contentType;
        this.filename = filename;
    }

    /**
     * @return 内容
     */
    public byte[] getBody() {
        return body.clone();
    }

    /**
     * @return MIME
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @return 文件名
     */
    public String getFilename() {
        return filename;
    }
}
