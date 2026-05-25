package com.shanghaiwind.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanghaiwind.admin.dto.RoleDTO;
import com.shanghaiwind.admin.entity.Role;
import com.shanghaiwind.admin.mapper.RoleMapper;
import com.shanghaiwind.admin.service.RoleService;
import com.shanghaiwind.common.exception.BusinessException;
import com.shanghaiwind.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色服务实现
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleMapper roleMapper;

    @Override
    public IPage<Role> pageQuery(Integer pageNum, Integer pageSize, String keyword) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(keyword != null, Role::getName, keyword)
                .or()
                .like(keyword != null, Role::getCode, keyword);
        wrapper.orderByDesc(Role::getCreatedAt);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional
    public Role create(RoleDTO dto, Long createdBy) {
        // 检查编码是否已存在
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getCode, dto.getCode());
        if (count(wrapper) > 0) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "角色编码已存在");
        }

        Role role = new Role();
        role.setCode(dto.getCode());
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        role.setCreatedBy(createdBy);
        save(role);
        return role;
    }

    @Override
    @Transactional
    public Role update(Long id, RoleDTO dto) {
        Role role = getById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "角色不存在");
        }

        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        updateById(role);
        return role;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Role role = getById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "角色不存在");
        }
        if ("ADMIN".equals(role.getCode())) {
            throw new BusinessException("不能删除管理员角色");
        }
        removeById(id);
    }

    @Override
    @Transactional
    public void changePermissions(Long roleId, List<Long> permissionIds) {
        Role role = getById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "角色不存在");
        }

        // 先删除现有关联
        roleMapper.deleteRolePermissionByRoleId(roleId);
        // 再添加新关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                roleMapper.insertRolePermission(roleId, permissionId);
            }
        }
    }

    @Override
    public List<Long> getPermissionIds(Long roleId) {
        return roleMapper.selectPermissionIdsByRoleId(roleId);
    }

    @Override
    public List<Role> getByUserId(Long userId) {
        return roleMapper.selectByUserId(userId);
    }
}
