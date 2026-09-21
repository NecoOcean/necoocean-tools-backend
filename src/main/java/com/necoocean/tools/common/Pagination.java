package com.necoocean.tools.common;

/**
 * 分页元数据。字段在 JSON 中输出为 page、page_size、total、total_pages。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
public final class Pagination {

    private final int page;

    private final int pageSize;

    private final long total;

    private final int totalPages;

    /**
     * 创建分页元数据。
     *
     * @param page       当前页
     * @param pageSize   每页条数
     * @param total      总条数
     * @param totalPages 总页数
     */
    public Pagination(int page, int pageSize, long total, int totalPages) {
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
        this.totalPages = totalPages;
    }

    /**
     * 当前页。
     *
     * @return 页码
     */
    public int getPage() {
        return page;
    }

    /**
     * 每页条数。序列化后字段名为 page_size。
     *
     * @return 页大小
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * 总条数。
     *
     * @return 总条数
     */
    public long getTotal() {
        return total;
    }

    /**
     * 总页数。序列化后字段名为 total_pages。
     *
     * @return 总页数
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Pagination{page=" + page + ", pageSize=" + pageSize
                + ", total=" + total + ", totalPages=" + totalPages + '}';
    }
}
