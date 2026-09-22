package com.necoocean.tools.service;

import java.util.ArrayList;
import java.util.List;

import com.necoocean.tools.common.BizException;
import com.necoocean.tools.common.ErrorCode;
import com.necoocean.tools.common.PageQuery;
import com.necoocean.tools.common.PageResult;
import com.necoocean.tools.domain.entity.EntityTimestamps;
import com.necoocean.tools.domain.entity.Message;
import com.necoocean.tools.domain.entity.MessageReply;
import com.necoocean.tools.domain.entity.ReleaseNote;
import com.necoocean.tools.domain.entity.ResourceFile;
import com.necoocean.tools.domain.entity.SiteSetting;
import com.necoocean.tools.domain.entity.Tool;
import com.necoocean.tools.domain.repository.MessageReplyRepository;
import com.necoocean.tools.domain.repository.MessageRepository;
import com.necoocean.tools.domain.repository.ReleaseNoteRepository;
import com.necoocean.tools.domain.repository.ResourceFileRepository;
import com.necoocean.tools.domain.repository.SiteSettingRepository;
import com.necoocean.tools.domain.repository.ToolRepository;
import com.necoocean.tools.dto.publicapi.CategoryDto;
import com.necoocean.tools.dto.publicapi.MessagePublicDto;
import com.necoocean.tools.dto.publicapi.MessageReplyPublicDto;
import com.necoocean.tools.dto.publicapi.PublicListDto;
import com.necoocean.tools.dto.publicapi.ReleaseNotePublicDto;
import com.necoocean.tools.dto.publicapi.ResourceFilePublicDto;
import com.necoocean.tools.dto.publicapi.SiteInfoDto;
import com.necoocean.tools.dto.publicapi.ToolCardDto;
import com.necoocean.tools.dto.publicapi.ToolDetailDto;
import com.necoocean.tools.service.cos.CosObjectStore;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 公开读。只装配白名单字段；下载经 COS 预签名 302。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
@Service
public class PublicReadService {

    /** 搜索关键字上限。对应附录 C 的 C-01。 */
    public static final int MAX_KEYWORD_LENGTH = 50;

    /** 留言轮询间隔。附录 C 已定稿，不进站点配置。 */
    public static final int MESSAGE_POLL_INTERVAL_SECONDS = 30;

    private static final String DOWNLOAD_PREFIX = "/download/";

    private static final String LIKE_ESCAPE = "\\";

    private final ToolRepository toolRepository;

    private final ReleaseNoteRepository releaseNoteRepository;

    private final ResourceFileRepository resourceFileRepository;

    private final MessageRepository messageRepository;

    private final MessageReplyRepository messageReplyRepository;

    private final SiteSettingRepository siteSettingRepository;

    private final CosObjectStore cosObjectStore;

    private final ResourceFileStatusService resourceFileStatusService;

    /**
     * @param toolRepository             工具
     * @param releaseNoteRepository      更新日志
     * @param resourceFileRepository     资源文件
     * @param messageRepository          留言
     * @param messageReplyRepository     回复
     * @param siteSettingRepository      站点配置
     * @param cosObjectStore             对象存储
     * @param resourceFileStatusService  状态短事务
     */
    public PublicReadService(ToolRepository toolRepository, ReleaseNoteRepository releaseNoteRepository,
            ResourceFileRepository resourceFileRepository, MessageRepository messageRepository,
            MessageReplyRepository messageReplyRepository, SiteSettingRepository siteSettingRepository,
            CosObjectStore cosObjectStore, ResourceFileStatusService resourceFileStatusService) {
        this.toolRepository = toolRepository;
        this.releaseNoteRepository = releaseNoteRepository;
        this.resourceFileRepository = resourceFileRepository;
        this.messageRepository = messageRepository;
        this.messageReplyRepository = messageReplyRepository;
        this.siteSettingRepository = siteSettingRepository;
        this.cosObjectStore = cosObjectStore;
        this.resourceFileStatusService = resourceFileStatusService;
    }

    /**
     * 已上架工具卡片。分类和关键字同时过滤。
     *
     * @param categoryId 分类，空表示不限
     * @param keyword    名称或简介包含匹配，空表示不限
     * @param page       页码
     * @param pageSize   每页条数
     * @return 分页卡片
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResult<ToolCardDto> listTools(Integer categoryId, String keyword, Integer page, Integer pageSize) {
        PageQuery query = PageQuery.of(page, pageSize);
        Page<Tool> tools = toolRepository.findPublished(Integer.valueOf(Tool.STATUS_PUBLISHED),
                normalizeCategoryId(categoryId), normalizeKeyword(keyword),
                PageRequest.of(query.getPage() - 1, query.getPageSize()));
        List<ToolCardDto> cards = new ArrayList<ToolCardDto>(tools.getContent().size());
        for (Tool tool : tools.getContent()) {
            cards.add(toCard(tool));
        }
        return PageResult.of(cards, query.getPage(), query.getPageSize(), tools.getTotalElements());
    }

    /**
     * 已上架工具详情。不存在或已下架都是 40401。
     *
     * @param slug 专题页标识
     * @return 详情
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ToolDetailDto getTool(String slug) {
        return toDetail(requirePublished(slug));
    }

    /**
     * 某工具的更新日志。
     *
     * @param slug     专题页标识
     * @param page     页码
     * @param pageSize 每页条数
     * @return 分页日志
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResult<ReleaseNotePublicDto> listReleaseNotes(String slug, Integer page, Integer pageSize) {
        Tool tool = requirePublished(slug);
        PageQuery query = PageQuery.of(page, pageSize);
        Page<ReleaseNote> notes = releaseNoteRepository.findPageByToolId(tool.getId(),
                PageRequest.of(query.getPage() - 1, query.getPageSize()));
        List<ReleaseNotePublicDto> items = new ArrayList<ReleaseNotePublicDto>(notes.getContent().size());
        for (ReleaseNote note : notes.getContent()) {
            items.add(new ReleaseNotePublicDto(note.getId(), note.getVersion(), note.getContent(),
                    EntityTimestamps.toOffset(note.getReleasedAt())));
        }
        return PageResult.of(items, query.getPage(), query.getPageSize(), notes.getTotalElements());
    }

    /**
     * 某工具的文件清单。不含对象键。
     *
     * @param slug 专题页标识
     * @return 文件列表
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PublicListDto<ResourceFilePublicDto> listFiles(String slug) {
        Tool tool = requirePublished(slug);
        List<ResourceFile> files = resourceFileRepository.findByToolIdForPublic(tool.getId(),
                Integer.valueOf(ResourceFile.OBJECT_STATUS_READY));
        List<ResourceFilePublicDto> items = new ArrayList<ResourceFilePublicDto>(files.size());
        for (ResourceFile file : files) {
            items.add(toFile(file));
        }
        return new PublicListDto<ResourceFilePublicDto>(items);
    }

    /**
     * 已通过审核的留言。非 BUG 不返回系统、版本和复现步骤。
     *
     * @param slug     专题页标识
     * @param page     页码
     * @param pageSize 每页条数
     * @return 分页留言
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResult<MessagePublicDto> listMessages(String slug, Integer page, Integer pageSize) {
        Tool tool = requirePublished(slug);
        PageQuery query = PageQuery.of(page, pageSize);
        Page<Message> messages = messageRepository.findApprovedByToolId(tool.getId(),
                Integer.valueOf(Message.AUDIT_APPROVED), PageRequest.of(query.getPage() - 1, query.getPageSize()));
        List<MessagePublicDto> items = new ArrayList<MessagePublicDto>(messages.getContent().size());
        for (Message message : messages.getContent()) {
            items.add(toMessage(message));
        }
        return PageResult.of(items, query.getPage(), query.getPageSize(), messages.getTotalElements());
    }

    /**
     * 站点公开信息。
     *
     * @return 站点信息
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public SiteInfoDto getSiteInfo() {
        return new SiteInfoDto(setting(SiteSetting.SITE_NAME), setting(SiteSetting.HOME_INTRO),
                setting(SiteSetting.COPYRIGHT), setting(SiteSetting.ABOUT_CONTENT), setting(SiteSetting.ICP_NUMBER),
                Boolean.valueOf(SiteSetting.BOARD_ON.equals(setting(SiteSetting.MESSAGE_BOARD_ENABLED))),
                Integer.valueOf(MESSAGE_POLL_INTERVAL_SECONDS));
    }

    /**
     * 校验文件并签发下载地址。对象缺失时标记异常并返回 40405。
     *
     * @param fileId 文件主键
     * @return 预签名下载 URL
     */
    @Transactional(rollbackFor = Exception.class)
    public String prepareDownload(Integer fileId) {
        ResourceFile file = resourceFileRepository.findById(fileId).orElse(null);
        if (file == null || !Integer.valueOf(Tool.STATUS_PUBLISHED).equals(file.getTool().getStatus())) {
            throw new BizException(ErrorCode.FILE_NOT_FOUND);
        }
        Integer status = file.getObjectStatus();
        if (!Integer.valueOf(ResourceFile.OBJECT_STATUS_READY).equals(status)
                && !Integer.valueOf(ResourceFile.OBJECT_STATUS_ABNORMAL).equals(status)) {
            throw new BizException(ErrorCode.FILE_NOT_FOUND);
        }
        if (!cosObjectStore.objectExists(file.getObjectKey())) {
            resourceFileStatusService.markAbnormal(file.getId());
            throw new BizException(ErrorCode.OBJECT_MISSING);
        }
        if (Integer.valueOf(ResourceFile.OBJECT_STATUS_ABNORMAL).equals(status)) {
            file.setObjectStatus(Integer.valueOf(ResourceFile.OBJECT_STATUS_READY));
        }
        int count = file.getDownloadCount() == null ? 0 : file.getDownloadCount().intValue();
        file.setDownloadCount(Integer.valueOf(count + 1));
        resourceFileRepository.saveAndFlush(file);
        return cosObjectStore.generateDownloadUrl(file.getObjectKey(), file.getDisplayName());
    }

    private Tool requirePublished(String slug) {
        Tool tool = toolRepository.findBySlug(slug).orElse(null);
        if (tool == null || !Integer.valueOf(Tool.STATUS_PUBLISHED).equals(tool.getStatus())) {
            throw new BizException(ErrorCode.TOOL_NOT_FOUND);
        }
        return tool;
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

    private ToolCardDto toCard(Tool tool) {
        return new ToolCardDto(tool.getId(), tool.getName(), tool.getSlug(), tool.getSummary(), null,
                new CategoryDto(tool.getCategory().getId(), tool.getCategory().getName()),
                splitPlatforms(tool.getPlatforms()), tool.getLatestVersion(),
                EntityTimestamps.toOffset(tool.getUpdatedAt()));
    }

    private ToolDetailDto toDetail(Tool tool) {
        return new ToolDetailDto(tool.getId(), tool.getName(), tool.getSlug(), tool.getSummary(), null,
                new CategoryDto(tool.getCategory().getId(), tool.getCategory().getName()),
                splitPlatforms(tool.getPlatforms()), tool.getTutorial(), tool.getLatestVersion(), tool.getRepoUrl(),
                tool.getWebUrl(), tool.getDevelopedAt(), EntityTimestamps.toOffset(tool.getCreatedAt()),
                EntityTimestamps.toOffset(tool.getUpdatedAt()));
    }

    private ResourceFilePublicDto toFile(ResourceFile file) {
        boolean latest = Integer.valueOf(ResourceFile.LATEST).equals(file.getLatest());
        return new ResourceFilePublicDto(file.getId(), file.getVersion(), file.getDisplayName(), file.getFileSize(),
                file.getSha256(), file.getPlatform(), Boolean.valueOf(latest), DOWNLOAD_PREFIX + file.getId(),
                EntityTimestamps.toOffset(file.getCreatedAt()));
    }

    private MessagePublicDto toMessage(Message message) {
        boolean bug = Integer.valueOf(Message.CATEGORY_BUG).equals(message.getCategory());
        boolean pinned = Integer.valueOf(Message.PINNED).equals(message.getPinned());
        return new MessagePublicDto(message.getId(), message.getNickname(), message.getCategory(),
                categoryText(message.getCategory()), message.getContent(), bug ? message.getOsPlatform() : null,
                bug ? message.getToolVersion() : null, bug ? message.getReproSteps() : null, Boolean.valueOf(pinned),
                EntityTimestamps.toOffset(message.getCreatedAt()), repliesOf(message.getId()));
    }

    private List<MessageReplyPublicDto> repliesOf(Integer messageId) {
        List<MessageReply> replies = messageReplyRepository.findByMessageIdOrderByCreatedAtAsc(messageId);
        List<MessageReplyPublicDto> items = new ArrayList<MessageReplyPublicDto>(replies.size());
        for (MessageReply reply : replies) {
            items.add(new MessageReplyPublicDto(reply.getId(), reply.getContent(),
                    EntityTimestamps.toOffset(reply.getCreatedAt())));
        }
        return items;
    }

    private String setting(String key) {
        SiteSetting setting = siteSettingRepository.findById(key).orElse(null);
        if (setting == null) {
            return null;
        }
        return setting.getValue();
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

    private static String categoryText(Integer category) {
        if (Integer.valueOf(Message.CATEGORY_EXPERIENCE).equals(category)) {
            return "使用体验";
        }
        if (Integer.valueOf(Message.CATEGORY_SUGGESTION).equals(category)) {
            return "功能建议";
        }
        if (Integer.valueOf(Message.CATEGORY_BUG).equals(category)) {
            return "BUG报错";
        }
        if (Integer.valueOf(Message.CATEGORY_QUESTION).equals(category)) {
            return "问题咨询";
        }
        return "";
    }
}
