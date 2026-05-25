package com.shanghaiwind.admin.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanghaiwind.admin.dto.UserDTO;
import com.shanghaiwind.admin.dto.UserQuery;
import com.shanghaiwind.admin.entity.User;
import com.shanghaiwind.admin.mapper.PermissionMapper;
import com.shanghaiwind.admin.mapper.UserMapper;
import com.shanghaiwind.admin.service.UserService;
import com.shanghaiwind.common.exception.BusinessException;
import com.shanghaiwind.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final PermissionMapper permissionMapper;

    @Override
    public User getByEmpNo(String empNo) {
        return userMapper.selectByEmpNo(empNo);
    }

    @Override
    public IPage<User> pageQuery(UserQuery query) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(query.getKeyword() != null, User::getEmpNo, query.getKeyword())
                .or()
                .like(query.getKeyword() != null, User::getName, query.getKeyword());
        wrapper.eq(query.getStatus() != null, User::getStatus, query.getStatus());
        wrapper.orderByDesc(User::getCreatedAt);
        return page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    @Override
    @Transactional
    public User create(UserDTO dto) {
        // 检查工号是否已存在
        User existing = userMapper.selectByEmpNo(dto.getEmpNo());
        if (existing != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "工号已存在");
        }

        User user = new User();
        user.setEmpNo(dto.getEmpNo());
        user.setName(dto.getName());
        user.setDept(dto.getDept());
        user.setSource("LOCAL");
        user.setStatus(1);

        // 生成密码
        String salt = UUID.randomUUID().toString().replace("-", "");
        String passwordHash = DigestUtil.sha256Hex(dto.getPassword() + salt);
        user.setPasswordHash(passwordHash);
        user.setSalt(salt);

        save(user);
        return user;
    }

    @Override
    @Transactional
    public User update(Long id, UserDTO dto) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }

        user.setName(dto.getName());
        user.setDept(dto.getDept());
        updateById(user);
        return user;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }
        if ("ADMIN001".equals(user.getEmpNo())) {
            throw new BusinessException("不能删除管理员账户");
        }
        removeById(id);
    }

    @Override
    @Transactional
    public void changeRoles(Long userId, List<Long> roleIds) {
        // 先删除现有关联
        userMapper.deleteUserRoleByUserId(userId);
        // 再添加新关联
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                userMapper.insertUserRole(userId, roleId);
            }
        }
    }

    @Override
    public List<Long> getRoleIds(Long userId) {
        return userMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    public List<String> getPermissionCodes(Long userId) {
        return permissionMapper.selectByUserId(userId).stream()
                .map(p -> p.getCode())
                .toList();
    }

    @Override
    public void lock(Long userId, int minutes) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }
        user.setLockedUntil(LocalDateTime.now().plusMinutes(minutes));
        updateById(user);
    }

    @Override
    public void unlock(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }
        user.setLockedUntil(null);
        updateById(user);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }

        // 验证旧密码
        String oldHash = DigestUtil.sha256Hex(oldPassword + user.getSalt());
        if (!oldHash.equals(user.getPasswordHash())) {
            throw new BusinessException("旧密码不正确");
        }

        // 设置新密码
        String newSalt = UUID.randomUUID().toString().replace("-", "");
        String newHash = DigestUtil.sha256Hex(newPassword + newSalt);
        user.setPasswordHash(newHash);
        user.setSalt(newSalt);
        updateById(user);
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }

        String newSalt = UUID.randomUUID().toString().replace("-", "");
        String newHash = DigestUtil.sha256Hex(newPassword + newSalt);
        user.setPasswordHash(newHash);
        user.setSalt(newSalt);
        updateById(user);
    }
}
