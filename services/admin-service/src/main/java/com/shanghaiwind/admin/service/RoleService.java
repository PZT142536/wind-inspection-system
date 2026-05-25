package com.shanghaiwind.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shanghaiwind.admin.dto.RoleDTO;
import com.shanghaiwind.admin.entity.Role;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService extends IService<Role> {

    /**
     * 分页查询角色
     */
    IPage<Role> pageQuery(Integer pageNum, Integer pageSize, String keyword);

    /**
     * 创建角色
     */
    Role create(RoleDTO dto, Long createdBy);

    /**
     * 更新角色
     */
    Role update(Long id, RoleDTO dto);

    /**
     * 删除角色
     */
    void delete(Long id);

    /**
     * 修改角色权限
     */
    void changePermissions(Long roleId, List<Long> permissionIds);

    /**
     * 获取角色的权限ID列表
     */
    List<Long> getPermissionIds(Long roleId);

    /**
     * 根据用户ID查询角色列表
     */
    List<Role> getByUserId(Long userId);
}
