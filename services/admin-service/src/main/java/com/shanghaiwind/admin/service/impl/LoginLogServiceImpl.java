package com.shanghaiwind.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanghaiwind.admin.entity.LoginLog;
import com.shanghaiwind.admin.mapper.LoginLogMapper;
import com.shanghaiwind.admin.service.LoginLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 登录日志服务实现
 */
@Service
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLog> implements LoginLogService {

    @Override
    public void recordLogin(String empNo, Long userId, HttpServletRequest request, boolean success, String failReason) {
        LoginLog log = new LoginLog();
        log.setEmpNo(empNo);
        log.setUserId(userId);
        log.setIp(getClientIp(request));
        log.setSource(detectSource(request));
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setLoginTime(LocalDateTime.now());
        log.setStatus(success ? 1 : 0);
        log.setFailReason(failReason);
        save(log);
    }

    @Override
    public IPage<LoginLog> pageQuery(Integer pageNum, Integer pageSize, String empNo, Integer status, String source) {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(empNo != null, LoginLog::getEmpNo, empNo);
        wrapper.eq(status != null, LoginLog::getStatus, status);
        wrapper.eq(source != null, LoginLog::getSource, source);
        wrapper.orderByDesc(LoginLog::getLoginTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }

    private String detectSource(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            userAgent = userAgent.toLowerCase();
            if (userAgent.contains("android") || userAgent.contains("iphone") || userAgent.contains("harmonyos")) {
                return "APP";
            }
        }
        return "WEB";
    }
}
