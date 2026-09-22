package com.necoocean.tools.service;

import java.util.ArrayList;
import java.util.List;

import com.necoocean.tools.common.BizException;
import com.necoocean.tools.common.ErrorCode;
import com.necoocean.tools.domain.entity.ToolCategory;
import com.necoocean.tools.domain.repository.ToolCategoryRepository;
import com.necoocean.tools.domain.repository.ToolRepository;
import com.necoocean.tools.dto.admin.CategoryAdminDto;
import com.necoocean.tools.dto.admin.CategoryWriteRequest;
import com.necoocean.tools.dto.admin.IdResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 后台分类管理。删除策略 A：有工具则拒绝；「其他工具」不可删。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Service
public class CategoryAdminService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryAdminService.class);

    private static final int NAME_MAX = 50;

    private final ToolCategoryRepository categories;

    private final ToolRepository tools;

    /**
     * @param categories 分类
     * @param tools      工具
     */
    public CategoryAdminService(ToolCategoryRepository categories, ToolRepository tools) {
        this.categories = categories;
        this.tools = tools;
    }

    /**
     * 分类列表，含工具数。
     *
     * @return 分类列表
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<CategoryAdminDto> list() {
        List<ToolCategory> rows = categories.findAllOrdered();
        List<CategoryAdminDto> items = new ArrayList<CategoryAdminDto>(rows.size());
        for (ToolCategory category : rows) {
            long count = tools.countByCategoryId(category.getId());
            items.add(new CategoryAdminDto(category.getId(), category.getName(), category.getSortOrder(),
                    Long.valueOf(count)));
        }
        return List.copyOf(items);
    }

    /**
     * 新增分类。名称冲突返回 40907。
     *
     * @param request 请求体
     * @return 新主键
     */
    @Transactional(rollbackFor = Exception.class)
    public IdResponse create(CategoryWriteRequest request) {
        if (request == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        String name = requireName(request.getName());
        if (categories.existsByName(name)) {
            throw new BizException(ErrorCode.CATEGORY_NAME_CONFLICT);
        }
        ToolCategory category = new ToolCategory();
        category.setName(name);
        category.setSortOrder(resolveSortOrder(request.getSortOrder()));
        categories.saveAndFlush(category);
        logger.info("category created, id={}", category.getId());
        return new IdResponse(category.getId());
    }

    /**
     * 编辑分类。
     *
     * @param id      主键
     * @param request 请求体
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Integer id, CategoryWriteRequest request) {
        ToolCategory category = requireCategory(id);
        if (request == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        if (StringUtils.hasText(request.getName())) {
            String name = requireName(request.getName());
            if (!name.equals(category.getName()) && categories.existsByName(name)) {
                throw new BizException(ErrorCode.CATEGORY_NAME_CONFLICT);
            }
            category.setName(name);
        }
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        categories.saveAndFlush(category);
        logger.info("category updated, id={}", id);
    }

    /**
     * 删除分类。有工具 40905；系统预置 40906。
     *
     * @param id 主键
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id) {
        ToolCategory category = requireCategory(id);
        if (Integer.valueOf(ToolCategory.FALLBACK_ID).equals(category.getId())
                || ToolCategory.FALLBACK_NAME.equals(category.getName())) {
            throw new BizException(ErrorCode.CATEGORY_PROTECTED);
        }
        if (tools.countByCategoryId(category.getId()) > 0L) {
            throw new BizException(ErrorCode.CATEGORY_NOT_EMPTY);
        }
        categories.delete(category);
        logger.info("category deleted, id={}", id);
    }

    private ToolCategory requireCategory(Integer id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return categories.findById(id).orElseThrow(() -> new BizException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private Integer resolveSortOrder(Integer sortOrder) {
        if (sortOrder != null) {
            return sortOrder;
        }
        Integer max = categories.findMaxSortOrder().orElse(Integer.valueOf(0));
        return Integer.valueOf(max.intValue() + 1);
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
}
