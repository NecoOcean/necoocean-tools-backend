package com.necoocean.tools.domain.repository;

import java.util.Optional;

import com.necoocean.tools.domain.entity.AdminUser;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 管理员账号存储。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public interface AdminUserRepository extends JpaRepository<AdminUser, Integer> {

    /**
     * 按登录账号查找管理员。
     *
     * @param username 登录账号
     * @return 管理员；不存在时为空
     */
    Optional<AdminUser> findByUsername(String username);
}
