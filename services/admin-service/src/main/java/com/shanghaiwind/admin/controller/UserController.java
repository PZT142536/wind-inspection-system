package com.shanghaiwind.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shanghaiwind.admin.dto.UserDTO;
import com.shanghaiwind.admin.dto.UserQuery;
import com.shanghaiwind.admin.entity.User;
import com.shanghaiwind.admin.service.UserService;
import com.shanghaiwind.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户
     */
    @GetMapping
    public R<IPage<User>> page(UserQuery query) {
        return R.ok(userService.pageQuery(query));
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public R<User> detail(@PathVariable Long id) {
        return R.ok(userService.getById(id));
    }

    /**
     * 创建用户
     */
    @PostMapping
    public R<User> create(@Valid @RequestBody UserDTO dto) {
        return R.ok(userService.create(dto));
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public R<User> update(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        return R.ok(userService.update(id, dto));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return R.ok();
    }

    /**
     * 修改用户角色
     */
    @PutMapping("/{id}/roles")
    public R<Void> changeRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userService.changeRoles(id, roleIds);
        return R.ok();
    }

    /**
     * 获取用户角色ID列表
     */
    @GetMapping("/{id}/roles")
    public R<List<Long>> getRoleIds(@PathVariable Long id) {
        return R.ok(userService.getRoleIds(id));
    }

    /**
     * 锁定用户
     */
    @PutMapping("/{id}/lock")
    public R<Void> lock(@PathVariable Long id, @RequestParam(defaultValue = "10") int minutes) {
        userService.lock(id, minutes);
        return R.ok();
    }

    /**
     * 解锁用户
     */
    @PutMapping("/{id}/unlock")
    public R<Void> unlock(@PathVariable Long id) {
        userService.unlock(id);
        return R.ok();
    }

    /**
     * 重置密码
     */
    @PutMapping("/{id}/reset-password")
    public R<Void> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return R.ok();
    }
}
