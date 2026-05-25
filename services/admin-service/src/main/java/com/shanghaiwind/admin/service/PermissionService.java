package com.shanghaiwind.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shanghaiwind.admin.entity.Permission;

import java.util.List;

/**
 * 权限服务接口
 */
public interface PermissionService extends IService<Permission> {

    /**
     * 获取权限树
     */
    List<Permission> getTree();

    /**
     * 根据角色ID查询权限列表
     */
    List<Permission> getByRoleId(Long roleId);

    /**
     * 根据用户ID查询权限列表
     */
    List<Permission> getByUserId(Long userId);
}
