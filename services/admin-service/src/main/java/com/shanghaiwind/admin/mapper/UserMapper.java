package com.shanghaiwind.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanghaiwind.admin.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据工号查询用户
     */
    @Select("SELECT * FROM sys_user WHERE emp_no = #{empNo}")
    User selectByEmpNo(@Param("empNo") String empNo);

    /**
     * 根据角色ID查询用户列表
     */
    @Select("SELECT u.* FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.id = ur.user_id " +
            "WHERE ur.role_id = #{roleId}")
    List<User> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询用户的角色ID列表
     */
    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 删除用户角色关联
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    void deleteUserRoleByUserId(@Param("userId") Long userId);

    /**
     * 插入用户角色关联
     */
    @Insert("INSERT INTO sys_user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
