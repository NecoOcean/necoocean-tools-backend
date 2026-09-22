package com.necoocean.tools.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import com.necoocean.tools.common.BizException;
import com.necoocean.tools.common.ErrorCode;
import com.necoocean.tools.common.PageQuery;
import com.necoocean.tools.common.PageResult;
import com.necoocean.tools.domain.entity.EntityTimestamps;
import com.necoocean.tools.domain.entity.ReleaseNote;
import com.necoocean.tools.domain.entity.Tool;
import com.necoocean.tools.domain.repository.ReleaseNoteRepository;
import com.necoocean.tools.domain.repository.ToolRepository;
import com.necoocean.tools.dto.admin.IdResponse;
import com.necoocean.tools.dto.admin.ReleaseNoteAdminDto;
import com.necoocean.tools.dto.admin.ReleaseNoteWriteRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 后台更新日志管理。同工具版本号冲突返回 40901。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Service
public class ReleaseNoteAdminService {

    private static final Logger logger = LoggerFactory.getLogger(ReleaseNoteAdminService.class);

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private static final int VERSION_MAX = 32;

    private final ReleaseNoteRepository releaseNotes;

    private final ToolRepository tools;

    /**
     * @param releaseNotes 更新日志
     * @param tools        工具
     */
    public ReleaseNoteAdminService(ReleaseNoteRepository releaseNotes, ToolRepository tools) {
        this.releaseNotes = releaseNotes;
        this.tools = tools;
    }

    /**
     * 某工具的更新日志列表。
     *
     * @param toolId   工具主键
     * @param page     页码
     * @param pageSize 每页条数
     * @return 分页日志
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResult<ReleaseNoteAdminDto> list(Integer toolId, Integer page, Integer pageSize) {
        requireTool(toolId);
        PageQuery query = PageQuery.of(page, pageSize);
        Page<ReleaseNote> rows = releaseNotes.findPageByToolId(toolId,
                PageRequest.of(query.getPage() - 1, query.getPageSize()));
        List<ReleaseNoteAdminDto> items = new ArrayList<ReleaseNoteAdminDto>(rows.getContent().size());
        for (ReleaseNote note : rows.getContent()) {
            items.add(toDto(note));
        }
        return PageResult.of(items, query.getPage(), query.getPageSize(), rows.getTotalElements());
    }

    /**
     * 新增更新日志。
     *
     * @param toolId  工具主键
     * @param request 请求体
     * @return 新主键
     */
    @Transactional(rollbackFor = Exception.class)
    public IdResponse create(Integer toolId, ReleaseNoteWriteRequest request) {
        Tool tool = requireTool(toolId);
        if (request == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        String version = requireVersion(request.getVersion());
        if (releaseNotes.existsByToolIdAndVersion(toolId, version)) {
            throw new BizException(ErrorCode.VERSION_CONFLICT);
        }
        ReleaseNote note = new ReleaseNote();
        note.setTool(tool);
        note.setVersion(version);
        note.setContent(requireContent(request.getContent()));
        note.setReleasedAt(toLocal(request.getReleasedAt()));
        releaseNotes.saveAndFlush(note);
        logger.info("release note created, id={}, toolId={}", note.getId(), toolId);
        return new IdResponse(note.getId());
    }

    /**
     * 编辑更新日志。
     *
     * @param id      日志主键
     * @param request 请求体
     * @return 详情
     */
    @Transactional(rollbackFor = Exception.class)
    public ReleaseNoteAdminDto update(Integer id, ReleaseNoteWriteRequest request) {
        ReleaseNote note = requireNote(id);
        if (request == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        String version = requireVersion(request.getVersion());
        if (!version.equals(note.getVersion())
                && releaseNotes.existsByToolIdAndVersion(note.getTool().getId(), version)) {
            throw new BizException(ErrorCode.VERSION_CONFLICT);
        }
        note.setVersion(version);
        note.setContent(requireContent(request.getContent()));
        note.setReleasedAt(toLocal(request.getReleasedAt()));
        releaseNotes.saveAndFlush(note);
        logger.info("release note updated, id={}", id);
        return toDto(note);
    }

    /**
     * 删除更新日志。
     *
     * @param id 日志主键
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id) {
        ReleaseNote note = requireNote(id);
        releaseNotes.delete(note);
        logger.info("release note deleted, id={}", id);
    }

    private Tool requireTool(Integer toolId) {
        if (toolId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return tools.findById(toolId).orElseThrow(() -> new BizException(ErrorCode.TOOL_NOT_FOUND));
    }

    private ReleaseNote requireNote(Integer id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return releaseNotes.findById(id).orElseThrow(() -> new BizException(ErrorCode.TOOL_NOT_FOUND));
    }

    private static String requireVersion(String version) {
        if (!StringUtils.hasText(version)) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        String trimmed = version.trim();
        if (trimmed.isEmpty() || trimmed.length() > VERSION_MAX) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return trimmed;
    }

    private static String requireContent(String content) {
        if (!StringUtils.hasText(content)) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        return content.trim();
    }

    private static LocalDateTime toLocal(OffsetDateTime value) {
        if (value == null) {
            return EntityTimestamps.now();
        }
        return value.atZoneSameInstant(SHANGHAI).toLocalDateTime().withNano(0);
    }

    private static ReleaseNoteAdminDto toDto(ReleaseNote note) {
        return new ReleaseNoteAdminDto(note.getId(), note.getVersion(), note.getContent(),
                EntityTimestamps.toOffset(note.getReleasedAt()));
    }
}
