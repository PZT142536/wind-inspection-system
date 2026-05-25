package com.shanghaiwind.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanghaiwind.admin.entity.OperationLog;
import com.shanghaiwind.admin.mapper.OperationLogMapper;
import com.shanghaiwind.admin.service.OperationLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作日志服务实现
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public void recordLog(Long userId, String empNo, String name, String module, String action, String detail, String ip, String result, String errorMsg) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setEmpNo(empNo);
        log.setName(name);
        log.setModule(module);
        log.setAction(action);
        log.setDetail(detail);
        log.setIp(ip);
        log.setTime(LocalDateTime.now());
        log.setResult(result);
        log.setErrorMsg(errorMsg);
        save(log);
    }
}
