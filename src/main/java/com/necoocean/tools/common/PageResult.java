package com.necoocean.tools.common;

import java.util.List;

/**
 * 分页响应体。结构为 list 加 pagination，列表为空时返回空集合而不是 null。
 *
 * @param <T> 列表元素类型
 * @author NecoOcean
 * @date 2026/09/21
 */
public final class PageResult<T> {

    private final List<T> list;

    private final Pagination pagination;

    private PageResult(List<T> list, Pagination pagination) {
        this.list = list;
        this.pagination = pagination;
    }

    /**
     * 组装一页数据。
     *
     * @param items    当前页记录，null 会变成空列表
     * @param page     当前页
     * @param pageSize 每页条数
     * @param total    总条数
     * @param <T>      元素类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> items, int page, int pageSize, long total) {
        List<T> safeItems = items == null ? List.of() : List.copyOf(items);
        Pagination pageMeta = new Pagination(page, pageSize, total, PageQuery.totalPages(total, pageSize));
        return new PageResult<T>(safeItems, pageMeta);
    }

    /**
     * 当前页记录。
     *
     * @return 不为 null 的列表
     */
    public List<T> getList() {
        return list;
    }

    /**
     * 分页元数据。
     *
     * @return 分页信息
     */
    public Pagination getPagination() {
        return pagination;
    }

    /**
     * 只输出条数，不输出列表内容。
     *
     * @return 分页结果文本
     */
    @Override
    public String toString() {
        return "PageResult{size=" + list.size() + ", pagination=" + pagination + '}';
    }
}
