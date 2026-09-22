package com.necoocean.tools.config;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.sql.internal.DdlTypeImpl;
import org.hibernate.type.descriptor.sql.spi.DdlTypeRegistry;

/**
 * 让 Hibernate 的类型预期和 Flyway 脚本一致：DATETIME 不带小数秒，长文本是 TEXT，开关量是 TINYINT。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class MysqlToolsDialect extends MySQLDialect {

    /** 本机安装的 MySQL 主版本。 */
    private static final int MYSQL_MAJOR = 8;

    /** 本机安装的 MySQL 次版本。 */
    private static final int MYSQL_MINOR = 4;

    /** 与脚本中的 DATETIME 一致，不保留小数秒。 */
    private static final int DATETIME_PRECISION = 0;

    /**
     * 按本机安装的 MySQL 8.4 注册类型。
     */
    public MysqlToolsDialect() {
        super(DatabaseVersion.make(MYSQL_MAJOR, MYSQL_MINOR));
    }

    /**
     * 脚本里的时间列是 DATETIME，不是 DATETIME(6)。
     *
     * @return 0，表示不保留小数秒
     */
    @Override
    public int getDefaultTimestampPrecision() {
        return DATETIME_PRECISION;
    }

    @Override
    protected void registerColumnTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        super.registerColumnTypes(typeContributions, serviceRegistry);
        DdlTypeRegistry ddlTypeRegistry = typeContributions.getTypeConfiguration().getDdlTypeRegistry();
        ddlTypeRegistry.addDescriptor(new DdlTypeImpl(SqlTypes.LONGVARCHAR, "text", this));
        ddlTypeRegistry.addDescriptor(new DdlTypeImpl(SqlTypes.LONG32VARCHAR, "text", this));
        ddlTypeRegistry.addDescriptor(new DdlTypeImpl(SqlTypes.CLOB, "text", this));
        ddlTypeRegistry.addDescriptor(new DdlTypeImpl(SqlTypes.TINYINT, "tinyint", this));
    }
}
