package com.shanghaiwind.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shanghaiwind.admin.entity.LoginLog;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 登录日志服务接口
 */
public interface LoginLogService extends IService<LoginLog> {

    /**
     * 记录登录日志
     */
    void recordLogin(String empNo, Long userId, HttpServletRequest request, boolean success, String failReason);

    /**
     * 分页查询登录日志
     */
    IPage<LoginLog> pageQuery(Integer pageNum, Integer pageSize, String empNo, Integer status, String source);
}
