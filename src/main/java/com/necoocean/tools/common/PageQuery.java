package com.necoocean.tools.common;

/**
 * 页码分页参数。页码从 1 开始，每页默认 20 条，超过 100 条按 100 截断。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
public final class PageQuery {

    /** 默认页码。 */
    public static final int DEFAULT_PAGE = 1;

    /** 默认每页条数。 */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /** 每页条数上限。 */
    public static final int MAX_PAGE_SIZE = 100;

    private final int page;

    private final int pageSize;

    private PageQuery(int page, int pageSize) {
        this.page = page;
        this.pageSize = pageSize;
    }

    /**
     * 把请求参数收成合法分页。空值、小于 1 的页码和小于 1 的页大小回到默认值。
     *
     * @param page     页码，可以为 null
     * @param pageSize 每页条数，可以为 null
     * @return 归一化后的分页参数
     */
    public static PageQuery of(Integer page, Integer pageSize) {
        int normalizedPage = DEFAULT_PAGE;
        if (page != null && page.intValue() >= DEFAULT_PAGE) {
            normalizedPage = page.intValue();
        }
        int normalizedSize = DEFAULT_PAGE_SIZE;
        if (pageSize != null && pageSize.intValue() >= DEFAULT_PAGE) {
            normalizedSize = pageSize.intValue();
        }
        if (normalizedSize > MAX_PAGE_SIZE) {
            normalizedSize = MAX_PAGE_SIZE;
        }
        return new PageQuery(normalizedPage, normalizedSize);
    }

    /**
     * 计算总页数。总数或页大小不是正数时返回 0。
     *
     * @param total    总条数
     * @param pageSize 每页条数
     * @return 总页数
     */
    public static int totalPages(long total, int pageSize) {
        if (total <= 0L || pageSize <= 0) {
            return 0;
        }
        long pages = (total + pageSize - 1L) / pageSize;
        if (pages > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) pages;
    }

    /**
     * 当前页码。
     *
     * @return 从 1 开始的页码
     */
    public int getPage() {
        return page;
    }

    /**
     * 每页条数。
     *
     * @return 已经截断过的页大小
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * SQL 偏移量。
     *
     * @return 从 0 开始的偏移
     */
    public long offset() {
        return (long) (page - DEFAULT_PAGE) * pageSize;
    }

    /**
     * 只输出页码和页大小。
     *
     * @return 分页参数文本
     */
    @Override
    public String toString() {
        return "PageQuery{page=" + page + ", pageSize=" + pageSize + '}';
    }
}
