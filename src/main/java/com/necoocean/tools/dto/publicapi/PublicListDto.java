package com.necoocean.tools.dto.publicapi;

import java.util.List;

/**
 * 不分页的公开列表。文件清单使用这个形状。
 *
 * @param <T> 元素类型
 * @author NecoOcean
 * @date 2026/09/22
 */
public class PublicListDto<T> {

    private final List<T> list;

    /**
     * @param list 列表，null 会变成空列表
     */
    public PublicListDto(List<T> list) {
        this.list = list == null ? List.of() : List.copyOf(list);
    }

    /**
     * 当前列表。
     *
     * @return 不为 null 的列表
     */
    public List<T> getList() {
        return list;
    }

    /**
     * 只输出条数。
     *
     * @return 简短文本
     */
    @Override
    public String toString() {
        return "PublicListDto{size=" + list.size() + '}';
    }
}
