package com.necoocean.tools.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 数据导出审计。记录操作人、时间与是否含邮箱。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Entity
@Table(name = "export_audit_logs")
public class ExportAuditLog {

    /** 不含邮箱。 */
    public static final int EMAIL_EXCLUDED = 0;

    /** 含邮箱。 */
    public static final int EMAIL_INCLUDED = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String operator;

    @Column(name = "export_type", nullable = false, length = 32)
    private String exportType;

    @Column(name = "export_format", nullable = false, length = 16)
    private String exportFormat;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "include_email", nullable = false)
    private Integer includeEmail;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void fillCreatedAt() {
        if (createdAt == null) {
            createdAt = EntityTimestamps.now();
        }
    }

    /**
     * @return 主键
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id 主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return 操作人
     */
    public String getOperator() {
        return operator;
    }

    /**
     * @param operator 操作人
     */
    public void setOperator(String operator) {
        this.operator = operator;
    }

    /**
     * @return 导出类型
     */
    public String getExportType() {
        return exportType;
    }

    /**
     * @param exportType 导出类型
     */
    public void setExportType(String exportType) {
        this.exportType = exportType;
    }

    /**
     * @return 导出格式
     */
    public String getExportFormat() {
        return exportFormat;
    }

    /**
     * @param exportFormat 导出格式
     */
    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }

    /**
     * @return 是否含邮箱
     */
    public Integer getIncludeEmail() {
        return includeEmail;
    }

    /**
     * @param includeEmail 是否含邮箱
     */
    public void setIncludeEmail(Integer includeEmail) {
        this.includeEmail = includeEmail;
    }

    /**
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt 创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ExportAuditLog{id=" + id + ", operator=" + operator + '}';
    }
}
