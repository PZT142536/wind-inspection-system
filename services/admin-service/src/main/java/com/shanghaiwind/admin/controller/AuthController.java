package com.shanghaiwind.admin.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.wf.captcha.SpecCaptcha;
import com.shanghaiwind.admin.dto.LoginDTO;
import com.shanghaiwind.admin.entity.User;
import com.shanghaiwind.admin.service.LoginLogService;
import com.shanghaiwind.admin.service.UserService;
import com.shanghaiwind.common.exception.BusinessException;
import com.shanghaiwind.common.result.R;
import com.shanghaiwind.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final LoginLogService loginLogService;
    private final StringRedisTemplate redisTemplate;

    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final String LOGIN_FAIL_PREFIX = "login:fail:";
    private static final int MAX_FAIL_COUNT = 5;
    private static final int LOCK_MINUTES = 10;

    /**
     * 获取验证码
     */
    @GetMapping("/captcha")
    public R<Map<String, String>> captcha() {
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 5);
        String key = UUID.randomUUID().toString().replace("-", "");
        String code = captcha.text().toLowerCase();

        // 存储验证码，5分钟过期
        redisTemplate.opsForValue().set(CAPTCHA_PREFIX + key, code, 5, TimeUnit.MINUTES);

        Map<String, String> result = new HashMap<>();
        result.put("key", key);
        result.put("image", captcha.toBase64());
        return R.ok(result);
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public R<SaTokenInfo> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        // 校验验证码
        String captchaKey = CAPTCHA_PREFIX + dto.getCaptchaKey();
        String cachedCaptcha = redisTemplate.opsForValue().get(captchaKey);
        if (cachedCaptcha == null) {
            throw new BusinessException(ResultCode.CAPTCHA_EXPIRED);
        }
        if (!cachedCaptcha.equals(dto.getCaptcha().toLowerCase())) {
            throw new BusinessException(ResultCode.CAPTCHA_ERROR);
        }
        redisTemplate.delete(captchaKey);

        // 查询用户
        User user = userService.getByEmpNo(dto.getEmpNo());
        if (user == null) {
            loginLogService.recordLogin(dto.getEmpNo(), null, request, false, "用户不存在");
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        // 检查账户状态
        if (user.getStatus() == 0) {
            loginLogService.recordLogin(dto.getEmpNo(), user.getId(), request, false, "账户已禁用");
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 检查是否锁定
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            loginLogService.recordLogin(dto.getEmpNo(), user.getId(), request, false, "账户已锁定");
            throw new BusinessException(ResultCode.ACCOUNT_LOCKED);
        }

        // 校验密码
        String passwordHash = DigestUtil.sha256Hex(dto.getPassword() + user.getSalt());
        if (!passwordHash.equals(user.getPasswordHash())) {
            // 记录失败次数
            String failKey = LOGIN_FAIL_PREFIX + dto.getEmpNo();
            Long failCount = redisTemplate.opsForValue().increment(failKey);
            redisTemplate.expire(failKey, LOCK_MINUTES, TimeUnit.MINUTES);

            if (failCount != null && failCount >= MAX_FAIL_COUNT) {
                userService.lock(user.getId(), LOCK_MINUTES);
                redisTemplate.delete(failKey);
                loginLogService.recordLogin(dto.getEmpNo(), user.getId(), request, false, "密码错误，账户已锁定");
                throw new BusinessException(ResultCode.ACCOUNT_LOCKED);
            }

            loginLogService.recordLogin(dto.getEmpNo(), user.getId(), request, false, "密码错误");
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        // 清除失败次数
        redisTemplate.delete(LOGIN_FAIL_PREFIX + dto.getEmpNo());

        // 登录
        StpUtil.login(user.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        // 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(getClientIp(request));
        user.setLockedUntil(null);
        userService.updateById(user);

        // 记录登录日志
        loginLogService.recordLogin(dto.getEmpNo(), user.getId(), request, true, null);

        return R.ok(tokenInfo);
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public R<Void> logout() {
        StpUtil.logout();
        return R.ok();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public R<Map<String, Object>> getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }

        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("empNo", user.getEmpNo());
        info.put("name", user.getName());
        info.put("dept", user.getDept());
        info.put("roles", userService.getRoleIds(userId));
        info.put("permissions", userService.getPermissionCodes(userId));
        return R.ok(info);
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
}
