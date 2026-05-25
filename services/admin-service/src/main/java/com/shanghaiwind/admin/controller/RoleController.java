package com.shanghaiwind.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shanghaiwind.admin.dto.RoleDTO;
import com.shanghaiwind.admin.entity.Role;
import com.shanghaiwind.admin.service.RoleService;
import com.shanghaiwind.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 分页查询角色
     */
    @GetMapping
    public R<IPage<Role>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return R.ok(roleService.pageQuery(pageNum, pageSize, keyword));
    }

    /**
     * 获取所有角色（下拉选择用）
     */
    @GetMapping("/all")
    public R<List<Role>> listAll() {
        return R.ok(roleService.list());
    }

    /**
     * 获取角色详情
     */
    @GetMapping("/{id}")
    public R<Role> detail(@PathVariable Long id) {
        return R.ok(roleService.getById(id));
    }

    /**
     * 创建角色
     */
    @PostMapping
    public R<Role> create(@Valid @RequestBody RoleDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(roleService.create(dto, userId));
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    public R<Role> update(@PathVariable Long id, @Valid @RequestBody RoleDTO dto) {
        return R.ok(roleService.update(id, dto));
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return R.ok();
    }

    /**
     * 修改角色权限
     */
    @PutMapping("/{id}/permissions")
    public R<Void> changePermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        roleService.changePermissions(id, permissionIds);
        return R.ok();
    }

    /**
     * 获取角色权限ID列表
     */
    @GetMapping("/{id}/permissions")
    public R<List<Long>> getPermissionIds(@PathVariable Long id) {
        return R.ok(roleService.getPermissionIds(id));
    }
}
