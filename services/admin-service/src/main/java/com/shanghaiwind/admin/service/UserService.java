package com.shanghaiwind.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shanghaiwind.admin.entity.User;
import com.shanghaiwind.admin.dto.UserDTO;
import com.shanghaiwind.admin.dto.UserQuery;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 根据工号查询用户
     */
    User getByEmpNo(String empNo);

    /**
     * 分页查询用户
     */
    IPage<User> pageQuery(UserQuery query);

    /**
     * 创建用户
     */
    User create(UserDTO dto);

    /**
     * 更新用户
     */
    User update(Long id, UserDTO dto);

    /**
     * 删除用户
     */
    void delete(Long id);

    /**
     * 修改用户角色
     */
    void changeRoles(Long userId, List<Long> roleIds);

    /**
     * 获取用户的角色ID列表
     */
    List<Long> getRoleIds(Long userId);

    /**
     * 获取用户的权限编码列表
     */
    List<String> getPermissionCodes(Long userId);

    /**
     * 锁定用户
     */
    void lock(Long userId, int minutes);

    /**
     * 解锁用户
     */
    void unlock(Long userId);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置密码
     */
    void resetPassword(Long userId, String newPassword);
}
