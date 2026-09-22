package com.necoocean.tools.dto.admin;

import com.necoocean.tools.security.AuthConstants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改口令请求。toString 不输出新旧口令。
 *
 * @author NecoOcean
 * @date 2026/09/22
 */
public class PasswordChangeRequest {

    @NotBlank
    private String oldPassword;

    @NotBlank
    @Size(min = AuthConstants.MIN_PASSWORD_LENGTH, max = AuthConstants.MAX_PASSWORD_LENGTH)
    private String newPassword;

    /**
     * 旧口令。
     *
     * @return 旧口令
     */
    public String getOldPassword() {
        return oldPassword;
    }
    /**
     * 写入旧口令。
     *
     * @param oldPassword 旧口令
     */
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    /**
     * 新口令。长度 8 到 72。
     *
     * @return 新口令
     */
    public String getNewPassword() {
        return newPassword;
    }
    /**
     * 写入新口令。
     *
     * @param newPassword 新口令
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    /**
     * 不输出任何口令。
     *
     * @return 简短文本
     */
    @Override
    public String toString() {
        return "PasswordChangeRequest{}";
    }
}
