package com.necoocean.tools.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.necoocean.tools.common.BizException;
import com.necoocean.tools.common.ErrorCode;
import com.necoocean.tools.common.PageQuery;
import com.necoocean.tools.common.PageResult;
import com.necoocean.tools.domain.entity.EntityTimestamps;
import com.necoocean.tools.domain.entity.ResourceFile;
import com.necoocean.tools.domain.entity.Tool;
import com.necoocean.tools.domain.entity.ToolCategory;
import com.necoocean.tools.domain.repository.ResourceFileRepository;
import com.necoocean.tools.domain.repository.ToolCategoryRepository;
import com.necoocean.tools.domain.repository.ToolRepository;
import com.necoocean.tools.dto.admin.IdResponse;
import com.necoocean.tools.dto.admin.StorageUsageDto;
import com.necoocean.tools.dto.admin.StorageVersionUsageDto;
import com.necoocean.tools.dto.admin.ToolAdminDto;
import com.necoocean.tools.dto.admin.ToolCreateRequest;
import com.necoocean.tools.dto.admin.ToolStatusRequest;
import com.necoocean.tools.dto.admin.ToolUpdateRequest;
import com.necoocean.tools.dto.publicapi.CategoryDto;
import com.necoocean.tools.service.cos.CosObjectStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 后台工具管理。删除时级联清库并删 COS 前缀（含 covers/）。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Service
public class ToolAdminService {

    private static final Logger logger = LoggerFactory.getLogger(ToolAdminService.class);

    /** 搜索关键字上限。 */
    public static final int MAX_KEYWORD_LENGTH = 50;

    private static final int NAME_MAX = 100;

    private static final int SLUG_MAX = 120;

    private static final int SUMMARY_MAX = 200;

    private static final int PLATFORMS_MAX = 100;

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    private static final String LIKE_ESCAPE = "\\";

    private final ToolRepository tools;

    private final ToolCategoryRepository categories;

    private final ResourceFileRepository resourceFiles;

    private final CosObjectStore cosObjectStore;

    /**
     * @param tools          工具
     * @param categories     分类
     * @param resourceFiles  资源文件
     * @param cosObjectStore 对象存储
     */
    public ToolAdminService(ToolRepository tools, ToolCategoryRepository categories,
            ResourceFileRepository resourceFiles, CosObjectStore cosObjectStore) {
        this.tools = tools;
        this.categories = categories;
        this.resourceFiles = resourceFiles;
        this.cosObjectStore = cosObjectStore;
    }

    /**
     * 工具列表。含下架。
     *
     * @param status     上下架，可选
     * @param categoryId 分类，可选
     * @param keyword    关键字，可选
     * @param page       页码
     * @param pageSize   每页条数
     * @return 分页结果
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResult<ToolAdminDto> list(Integer status, Integer categoryId, String keyword, Integer page,
            Integer pageSize) {
        PageQuery query = PageQuery.of(page, pageSize);
        Page<Tool> rows = tools.findAdmin(normalizeStatusFilter(status), normalizeCategoryId(categoryId),
                normalizeKeyword(keyword), PageRequest.of(query.getPage() - 1, query.getPageSize()));
        List<ToolAdminDto> items = new ArrayList<ToolAdminDto>(rows.getContent().size());
        for (Tool tool : rows.getContent()) {
            items.add(toDto(tool));
        }
        return PageResult.of(items, query.getPage(), query.getPageSize(), rows.getTotalElements());
    }

    /**
     * 工具详情。
     *
     * @param id 主键
     * @return 详情
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ToolAdminDto get(Integer id) {
        return toDto(requireTool(id));
    }

    /**
     * 新建工具。
     *
     * @param request 请求体
     * @return 新主键
     */
    @Transactional(rollbackFor = Exception.class)
    public IdResponse create(ToolCreateRequest request) {
        if (request == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        String slug = requireSlug(request.getSlug());
        if (tools.existsBySlug(slug)) {
            throw new BizException(ErrorCode.SLUG_CONFLICT);
        }
        Tool tool = new Tool();
        tool.setName(requireName(request.getName()));
        tool.setSlug(slug);
        tool.setSummary(requireSummary(request.getSummary()));
        tool.setCategory(requireCategory(request.getCategoryId()));
        tool.setPlatforms(joinPlatforms(request.getPlatforms()));
        tool.setTutorial(blankToNull(request.getTutorial()));
        tool.setRepoUrl(blankToNull(request.getRepoUrl()));
        tool.setWebUrl(blankToNull(request.getWebUrl()));
        tool.setDevelopedAt(request.getDevelopedAt());
        tool.setStatus(resolveCreateStatus(request.getStatus()));
        tools.saveAndFlush(tool);
        logger.info("tool created, id={}, slug={}", tool.getId(), tool.getSlug());
        return new IdResponse(tool.getId());
    }

    /**
     * 编辑工具。slug 不可改。
     *
     * @param id      主键
     * @param request 请求体
     * @return 详情
     */
    @Transactional(rollbackFor = Exception.class)
    public ToolAdminDto update(Integer id, ToolUpdateRequest request) {
        Tool tool = requireTool(id);
        if (request == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        if (StringUtils.hasText(request.getSlug()) && !request.getSlug().trim().equals(tool.getSlug())) {
            throw new BizException(ErrorCode.SLUG_IMMUTABLE);
        }
        tool.setName(requireName(request.getName()));
        tool.setSummary(requireSummary(request.getSummary()));
        tool.setCategory(requireCategory(request.getCategoryId()));
        tool.setPlatforms(joinPlatforms(request.getPlatforms()));
        tool.setTutorial(blankToNull(request.getTutorial()));
        tool.setRepoUrl(blankToNull(request.getRepoUrl()));
        tool.setWebUrl(blankToNull(request.getWebUrl()));
        if (request.isDevelopedAtPresent()) {
            tool.setDevelopedAt(request.getDevelopedAt());
        }
        tools.saveAndFlush(tool);
        logger.info("tool updated, id={}", id);
        return toDto(tool);
    }

    /**
     * 上下架。
     *
     * @param id      主键
     * @param request 状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void patchStatus(Integer id, ToolStatusRequest request) {
        Tool tool = requireTool(id);
        if (request == null || request.getStatus() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        int status = request.getStatus().intValue();
        if (status != Tool.STATUS_PUBLISHED && status != Tool.STATUS_UNPUBLISHED) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        tool.setStatus(Integer.valueOf(status));
        tools.saveAndFlush(tool);
        logger.info("tool status patched, id={}, status={}", id, status);
    }

    /**
     * 删除工具。先删 COS 前缀，再删库（级联元数据）。
     *
     * @param id 主键
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id) {
        Tool tool = requireTool(id);
        cosObjectStore.deleteByPrefix(tool.getId() + "/");
        cosObjectStore.deleteByPrefix("covers/" + tool.getId() + "/");
        tools.delete(tool);
        logger.info("tool deleted, id={}", id);
    }

    /**
     * 存储占用。汇总库内 file_size，并统计异常文件数。
     *
     * @param id 工具主键
     * @return 占用汇总
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public StorageUsageDto storageUsage(Integer id) {
        requireTool(id);
        List<ResourceFile> files = resourceFiles.findByToolIdOrderByCreatedAtDesc(id);
        Map<String, long[]> grouped = new LinkedHashMap<String, long[]>();
        long total = 0L;
        for (ResourceFile file : files) {
            long size = file.getFileSize() == null ? 0L : file.getFileSize().longValue();
            total += size;
            long[] bucket = grouped.computeIfAbsent(file.getVersion(), key -> new long[2]);
            bucket[0] += size;
            bucket[1] += 1L;
        }
        List<StorageVersionUsageDto> byVersion = new ArrayList<StorageVersionUsageDto>(grouped.size());
        for (Map.Entry<String, long[]> entry : grouped.entrySet()) {
            byVersion.add(new StorageVersionUsageDto(entry.getKey(), Long.valueOf(entry.getValue()[0]),
                    Long.valueOf(entry.getValue()[1])));
        }
        long abnormal = resourceFiles.countByToolIdAndObjectStatus(id,
                Integer.valueOf(ResourceFile.OBJECT_STATUS_ABNORMAL));
        return new StorageUsageDto(Long.valueOf(total), Long.valueOf(files.size()), Long.valueOf(abnormal), byVersion);
    }

    private Tool requireTool(Integer id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return tools.findById(id).orElseThrow(() -> new BizException(ErrorCode.TOOL_NOT_FOUND));
    }

    private ToolCategory requireCategory(Integer categoryId) {
        if (categoryId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return categories.findById(categoryId).orElseThrow(() -> new BizException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private Integer normalizeStatusFilter(Integer status) {
        if (status == null) {
            return null;
        }
        if (status.intValue() != Tool.STATUS_PUBLISHED && status.intValue() != Tool.STATUS_UNPUBLISHED) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return status;
    }

    private Integer normalizeCategoryId(Integer categoryId) {
        if (categoryId == null) {
            return null;
        }
        if (categoryId.intValue() < PageQuery.DEFAULT_PAGE) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return categoryId;
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        String trimmed = keyword.trim();
        if (trimmed.length() > MAX_KEYWORD_LENGTH) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return trimmed.replace(LIKE_ESCAPE, LIKE_ESCAPE + LIKE_ESCAPE).replace("%", LIKE_ESCAPE + "%")
                .replace("_", LIKE_ESCAPE + "_");
    }

    private static Integer resolveCreateStatus(Integer status) {
        if (status == null) {
            return Integer.valueOf(Tool.STATUS_UNPUBLISHED);
        }
        if (status.intValue() != Tool.STATUS_PUBLISHED && status.intValue() != Tool.STATUS_UNPUBLISHED) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return status;
    }

    private static String requireName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty() || trimmed.length() > NAME_MAX) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return trimmed;
    }

    private static String requireSlug(String slug) {
        if (!StringUtils.hasText(slug)) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        String trimmed = slug.trim();
        if (trimmed.length() > SLUG_MAX || !SLUG_PATTERN.matcher(trimmed).matches()) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return trimmed;
    }

    private static String requireSummary(String summary) {
        if (!StringUtils.hasText(summary)) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        String trimmed = summary.trim();
        if (trimmed.isEmpty() || trimmed.length() > SUMMARY_MAX) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return trimmed;
    }

    private static String joinPlatforms(List<String> platforms) {
        if (platforms == null || platforms.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        StringBuilder joined = new StringBuilder();
        for (String platform : platforms) {
            if (!StringUtils.hasText(platform)) {
                continue;
            }
            if (joined.length() > 0) {
                joined.append(',');
            }
            joined.append(platform.trim());
        }
        if (joined.length() == 0 || joined.length() > PLATFORMS_MAX) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return joined.toString();
    }

    private static List<String> splitPlatforms(String raw) {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        String[] parts = raw.split(",");
        List<String> platforms = new ArrayList<String>(parts.length);
        for (String part : parts) {
            if (StringUtils.hasText(part)) {
                platforms.add(part.trim());
            }
        }
        return List.copyOf(platforms);
    }

    private static String blankToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private ToolAdminDto toDto(Tool tool) {
        return new ToolAdminDto(tool.getId(), tool.getName(), tool.getSlug(), tool.getSummary(), null,
                new CategoryDto(tool.getCategory().getId(), tool.getCategory().getName()),
                splitPlatforms(tool.getPlatforms()), tool.getTutorial(), tool.getLatestVersion(), tool.getRepoUrl(),
                tool.getWebUrl(), tool.getStatus(), tool.getDevelopedAt(),
                EntityTimestamps.toOffset(tool.getCreatedAt()), EntityTimestamps.toOffset(tool.getUpdatedAt()));
    }
}
