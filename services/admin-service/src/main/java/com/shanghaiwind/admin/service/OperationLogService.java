package com.shanghaiwind.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shanghaiwind.admin.entity.OperationLog;

/**
 * 操作日志服务接口
 */
public interface OperationLogService extends IService<OperationLog> {

    /**
     * 记录操作日志
     */
    void recordLog(Long userId, String empNo, String name, String module, String action, String detail, String ip, String result, String errorMsg);
}
