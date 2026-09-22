package com.necoocean.tools.domain.repository;

import com.necoocean.tools.domain.entity.SiteSetting;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 站点配置存储。主键是配置键。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public interface SiteSettingRepository extends JpaRepository<SiteSetting, String> {
}
