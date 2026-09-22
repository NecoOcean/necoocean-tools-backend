package com.necoocean.tools.dto.admin;

/**
 * 仅返回新建资源主键。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class IdResponse {

    private final Integer id;

    /**
     * @param id 主键
     */
    public IdResponse(Integer id) {
        this.id = id;
    }

    /**
     * 主键。
     *
     * @return 主键
     */
    public Integer getId() {
        return id;
    }
        /**
     * 简短文本。
     *
     * @return 文本
     */
    @Override
    public String toString() {
        return "IdResponse{id=" + id + '}';
    }
}