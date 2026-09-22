package com.necoocean.tools.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

/**
 * 资源文件元数据。二进制在对象存储，不入库。同一工具至多一条最新版本。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Entity
@Table(name = "resource_files")
public class ResourceFile {

    /** 当前推荐版本。同一工具只能有一条。 */
    public static final int LATEST = 1;

    /** 不是当前推荐版本。 */
    public static final int NOT_LATEST = 0;

    /** 新文件的下载次数。一期只累加，不展示。 */
    public static final int DEFAULT_DOWNLOAD_COUNT = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tool_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_resource_files_tool"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tool tool;

    @Column(nullable = false, length = 32)
    private String version;

    @Column(name = "display_name", nullable = false, length = 255)
    private String displayName;

    @Column(name = "object_key", nullable = false, length = 255)
    private String objectKey;

    @Column(nullable = false, length = 16)
    private String ext;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 64)
    private String sha256;

    @Column(length = 20)
    private String platform;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "is_latest", nullable = false)
    private Integer latest;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "download_count", nullable = false)
    private Integer downloadCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void fillDefaults() {
        if (downloadCount == null) {
            downloadCount = DEFAULT_DOWNLOAD_COUNT;
        }
        if (createdAt == null) {
            createdAt = EntityTimestamps.now();
        }
    }

    /**
     * 主键。
     *
     * @return 主键，未持久化时为 null
     */
    public Integer getId() {
        return id;
    }

    /**
     * 写入主键。仅供持久化框架回填。
     *
     * @param id 主键
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 所属工具。
     *
     * @return 工具
     */
    public Tool getTool() {
        return tool;
    }

    /**
     * 写入所属工具。
     *
     * @param tool 工具
     */
    public void setTool(Tool tool) {
        this.tool = tool;
    }

    /**
     * 所属版本号。
     *
     * @return 版本号
     */
    public String getVersion() {
        return version;
    }

    /**
     * 写入所属版本号。
     *
     * @param version 版本号
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 原始文件名。只用于下载响应头。
     *
     * @return 文件名
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 写入原始文件名。
     *
     * @param displayName 文件名
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 对象键。不含桶名和域名。
     *
     * @return 对象键
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * 写入对象键。
     *
     * @param objectKey 对象键
     */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /**
     * 扩展名，不含点。
     *
     * @return 扩展名
     */
    public String getExt() {
        return ext;
    }

    /**
     * 写入扩展名。
     *
     * @param ext 扩展名
     */
    public void setExt(String ext) {
        this.ext = ext;
    }

    /**
     * 文件大小，单位字节。
     *
     * @return 字节数
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * 写入文件大小。
     *
     * @param fileSize 字节数
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * SHA-256，小写十六进制。
     *
     * @return 64 位校验值
     */
    public String getSha256() {
        return sha256;
    }

    /**
     * 写入 SHA-256。
     *
     * @param sha256 64 位小写十六进制
     */
    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    /**
     * 该文件适用的平台。
     *
     * @return Windows、Android、Mac、通用之一，未填时为 null
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * 写入适用平台。
     *
     * @param platform 平台，可以为 null
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    /**
     * 是否为当前推荐版本。
     *
     * @return 1 是，0 否
     */
    public Integer getLatest() {
        return latest;
    }

    /**
     * 写入是否为当前推荐版本。
     *
     * @param latest 1 是，0 否
     */
    public void setLatest(Integer latest) {
        this.latest = latest;
    }

    /**
     * 下载次数。
     *
     * @return 累计次数
     */
    public Integer getDownloadCount() {
        return downloadCount;
    }

    /**
     * 写入下载次数。为空时在插入前记为 0。
     *
     * @param downloadCount 次数，可以为 null
     */
    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    /**
     * 上传时间。
     *
     * @return 东八区墙钟时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 写入上传时间。为空时在插入前自动补上。
     *
     * @param createdAt 上传时间，可以为 null
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 只输出主键和对象键，不输出文件正文。
     *
     * @return 简短文本
     */
    @Override
    public String toString() {
        return "ResourceFile{id=" + id + ", objectKey=" + objectKey + '}';
    }
}
