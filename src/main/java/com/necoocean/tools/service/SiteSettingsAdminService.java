package com.necoocean.tools.service;

import java.util.Optional;

import com.necoocean.tools.common.BizException;
import com.necoocean.tools.common.ErrorCode;
import com.necoocean.tools.domain.entity.SiteSetting;
import com.necoocean.tools.domain.repository.SiteSettingRepository;
import com.necoocean.tools.dto.admin.SiteSettingsAdminDto;
import com.necoocean.tools.dto.admin.SiteSettingsUpdateRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 后台站点设置。site_title↔site_name，message_enabled↔message_board_enabled；
 * message_poll_interval 固定 30，不入库。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Service
public class SiteSettingsAdminService {

    private static final Logger logger = LoggerFactory.getLogger(SiteSettingsAdminService.class);

    private final SiteSettingRepository settings;

    /**
     * @param settings 站点配置
     */
    public SiteSettingsAdminService(SiteSettingRepository settings) {
        this.settings = settings;
    }

    /**
     * 读取全量设置。
     *
     * @return 设置
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public SiteSettingsAdminDto get() {
        return new SiteSettingsAdminDto(value(SiteSetting.SITE_NAME), value(SiteSetting.HOME_INTRO),
                value(SiteSetting.COPYRIGHT), value(SiteSetting.ABOUT_CONTENT), value(SiteSetting.PRIVACY_CONTENT),
                value(SiteSetting.ICP_NUMBER), value(SiteSetting.ANNOUNCEMENT),
                Boolean.valueOf(SiteSetting.BOARD_ON.equals(value(SiteSetting.MESSAGE_BOARD_ENABLED))),
                Integer.valueOf(PublicReadService.MESSAGE_POLL_INTERVAL_SECONDS),
                value(SiteSetting.MESSAGE_AUDIT_MODE), value(SiteSetting.MESSAGE_KEYWORDS));
    }

    /**
     * 部分更新设置。忽略 message_poll_interval。
     *
     * @param request 请求体
     * @return 更新后的全量设置
     */
    @Transactional(rollbackFor = Exception.class)
    public SiteSettingsAdminDto update(SiteSettingsUpdateRequest request) {
        if (request == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        if (request.isSiteTitlePresent()) {
            write(SiteSetting.SITE_NAME, blankToNull(request.getSiteTitle()));
        }
        if (request.isHomeIntroPresent()) {
            write(SiteSetting.HOME_INTRO, blankToNull(request.getHomeIntro()));
        }
        if (request.isCopyrightPresent()) {
            write(SiteSetting.COPYRIGHT, blankToNull(request.getCopyright()));
        }
        if (request.isAboutContentPresent()) {
            write(SiteSetting.ABOUT_CONTENT, blankToNull(request.getAboutContent()));
        }
        if (request.isPrivacyContentPresent()) {
            write(SiteSetting.PRIVACY_CONTENT, blankToNull(request.getPrivacyContent()));
        }
        if (request.isIcpNumberPresent()) {
            write(SiteSetting.ICP_NUMBER, blankToNull(request.getIcpNumber()));
        }
        if (request.isAnnouncementPresent()) {
            write(SiteSetting.ANNOUNCEMENT, blankToNull(request.getAnnouncement()));
        }
        if (request.isMessageEnabledPresent()) {
            if (request.getMessageEnabled() == null) {
                throw new BizException(ErrorCode.PARAM_INVALID);
            }
            write(SiteSetting.MESSAGE_BOARD_ENABLED,
                    request.getMessageEnabled().booleanValue() ? SiteSetting.BOARD_ON : SiteSetting.BOARD_OFF);
        }
        if (request.isMessageAuditModePresent()) {
            write(SiteSetting.MESSAGE_AUDIT_MODE, requireAuditMode(request.getMessageAuditMode()));
        }
        if (request.isMessageKeywordsPresent()) {
            write(SiteSetting.MESSAGE_KEYWORDS, blankToNull(request.getMessageKeywords()));
        }
        logger.info("site settings updated");
        return get();
    }

    private void write(String key, String value) {
        Optional<SiteSetting> existing = settings.findById(key);
        SiteSetting setting = existing.orElseGet(() -> {
            SiteSetting created = new SiteSetting();
            created.setKey(key);
            return created;
        });
        setting.setValue(value);
        settings.saveAndFlush(setting);
    }

    private String value(String key) {
        SiteSetting setting = settings.findById(key).orElse(null);
        if (setting == null) {
            return null;
        }
        return setting.getValue();
    }

    private static String requireAuditMode(String mode) {
        if (!StringUtils.hasText(mode)) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        String trimmed = mode.trim();
        if (!SiteSetting.AUDIT_MODE_POST.equals(trimmed) && !SiteSetting.AUDIT_MODE_PRE.equals(trimmed)) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return trimmed;
    }

    private static String blankToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
