package com.shanghaiwind.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shanghaiwind.admin.entity.LoginLog;
import com.shanghaiwind.admin.service.LoginLogService;
import com.shanghaiwind.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 登录日志控制器
 */
@RestController
@RequestMapping("/login-logs")
@RequiredArgsConstructor
public class LoginLogController {

    private final LoginLogService loginLogService;

    /**
     * 分页查询登录日志
     */
    @GetMapping
    public R<IPage<LoginLog>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String empNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String source) {
        return R.ok(loginLogService.pageQuery(pageNum, pageSize, empNo, status, source));
    }
}
