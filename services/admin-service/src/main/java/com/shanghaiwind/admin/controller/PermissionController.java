package com.shanghaiwind.admin.controller;

import com.shanghaiwind.admin.entity.Permission;
import com.shanghaiwind.admin.service.PermissionService;
import com.shanghaiwind.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制器
 */
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 获取权限树
     */
    @GetMapping("/tree")
    public R<List<Permission>> tree() {
        return R.ok(permissionService.getTree());
    }

    /**
     * 获取所有权限
     */
    @GetMapping
    public R<List<Permission>> list() {
        return R.ok(permissionService.list());
    }

    /**
     * 根据角色ID查询权限列表
     */
    @GetMapping("/role/{roleId}")
    public R<List<Permission>> getByRoleId(@PathVariable Long roleId) {
        return R.ok(permissionService.getByRoleId(roleId));
    }
}
