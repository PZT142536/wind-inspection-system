package com.shanghaiwind.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanghaiwind.admin.entity.Permission;
import com.shanghaiwind.admin.mapper.PermissionMapper;
import com.shanghaiwind.admin.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限服务实现
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    private final PermissionMapper permissionMapper;

    @Override
    public List<Permission> getTree() {
        // 查询所有权限
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Permission::getSortOrder);
        List<Permission> allPermissions = list(wrapper);

        // 构建树结构
        return buildTree(allPermissions, null);
    }

    @Override
    public List<Permission> getByRoleId(Long roleId) {
        return permissionMapper.selectByRoleId(roleId);
    }

    @Override
    public List<Permission> getByUserId(Long userId) {
        return permissionMapper.selectByUserId(userId);
    }

    /**
     * 递归构建树结构
     */
    private List<Permission> buildTree(List<Permission> allPermissions, Long parentId) {
        Map<Long, List<Permission>> groupByParent = allPermissions.stream()
                .collect(Collectors.groupingBy(p -> p.getParentId() == null ? 0L : p.getParentId()));

        return buildChildren(groupByParent, parentId == null ? 0L : parentId);
    }

    private List<Permission> buildChildren(Map<Long, List<Permission>> groupByParent, Long parentId) {
        List<Permission> children = groupByParent.getOrDefault(parentId, new ArrayList<>());
        for (Permission child : children) {
            List<Permission> subChildren = buildChildren(groupByParent, child.getId());
            // 这里可以添加children属性，但Permission实体暂未定义
            // 实际项目中需要在Permission实体中添加 @TableField(exist = false) private List<Permission> children;
        }
        return children;
    }
}
