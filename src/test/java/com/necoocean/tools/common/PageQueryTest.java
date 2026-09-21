package com.necoocean.tools.common;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 分页参数的边界：默认值、上限截断和总页数。
 *
 * @author NecoOcean
 * @date 2026/09/21
 */
class PageQueryTest {

    @Test
    void emptyInputUsesDefaults() {
        PageQuery query = PageQuery.of(null, null);
        assertThat(query.getPage()).isEqualTo(PageQuery.DEFAULT_PAGE);
        assertThat(query.getPageSize()).isEqualTo(PageQuery.DEFAULT_PAGE_SIZE);
        assertThat(query.offset()).isZero();
    }

    @Test
    void pageSizeAboveLimitIsCapped() {
        PageQuery query = PageQuery.of(0, 101);
        assertThat(query.getPage()).isEqualTo(PageQuery.DEFAULT_PAGE);
        assertThat(query.getPageSize()).isEqualTo(PageQuery.MAX_PAGE_SIZE);
    }

    @Test
    void nonPositivePageSizeFallsBackToDefault() {
        PageQuery query = PageQuery.of(3, 0);
        assertThat(query.getPage()).isEqualTo(3);
        assertThat(query.getPageSize()).isEqualTo(PageQuery.DEFAULT_PAGE_SIZE);
        assertThat(query.offset()).isEqualTo(40L);
    }

    @Test
    void totalPagesHandlesEdges() {
        assertThat(PageQuery.totalPages(0L, 20)).isZero();
        assertThat(PageQuery.totalPages(-1L, 20)).isZero();
        assertThat(PageQuery.totalPages(20L, 0)).isZero();
        assertThat(PageQuery.totalPages(20L, 20)).isEqualTo(1);
        assertThat(PageQuery.totalPages(21L, 20)).isEqualTo(2);
        assertThat(PageQuery.totalPages(Long.MAX_VALUE, 1)).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void nullItemsBecomeEmptyList() {
        PageResult<String> result = PageResult.of(null, 1, 20, 0L);
        assertThat(result.getList()).isEmpty();
        assertThat(result.getPagination().getTotalPages()).isZero();
        assertThat(result.toString()).contains("size=0");
    }

    @Test
    void pageResultCopiesItems() {
        PageResult<String> result = PageResult.of(List.of("apk"), 1, 20, 21L);
        assertThat(result.getList()).containsExactly("apk");
        assertThat(result.getPagination().getPageSize()).isEqualTo(20);
        assertThat(result.getPagination().getTotal()).isEqualTo(21L);
        assertThat(result.getPagination().getTotalPages()).isEqualTo(2);
        assertThat(result.getPagination().toString()).contains("totalPages=2");
    }
}
