package com.shanghaiwind.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanghaiwind.admin.entity.OperationLog;
import com.shanghaiwind.admin.service.OperationLogService;
import com.shanghaiwind.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/operation-logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    /**
     * 分页查询操作日志
     */
    @GetMapping
    public R<IPage<OperationLog>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String empNo,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(empNo != null, OperationLog::getEmpNo, empNo);
        wrapper.eq(module != null, OperationLog::getModule, module);
        wrapper.eq(result != null, OperationLog::getResult, result);
        wrapper.ge(startTime != null, OperationLog::getTime, startTime);
        wrapper.le(endTime != null, OperationLog::getTime, endTime);
        wrapper.orderByDesc(OperationLog::getTime);

        IPage<OperationLog> page = operationLogService.page(new Page<>(pageNum, pageSize), wrapper);
        return R.ok(page);
    }
}
