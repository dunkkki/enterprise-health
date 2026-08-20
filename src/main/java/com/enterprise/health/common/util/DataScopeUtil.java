package com.enterprise.health.common.util;

import cn.dev33.satoken.stp.StpUtil;
import com.enterprise.health.entity.Role;
import com.enterprise.health.entity.User;
import com.enterprise.health.mapper.RoleMapper;
import com.enterprise.health.mapper.UserMapper;
import com.enterprise.health.mapper.UserRoleMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataScopeUtil {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    public DataScopeUtil(UserMapper userMapper, UserRoleMapper userRoleMapper, RoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
    }

    public int getDataScope() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) return 2;
        return roleMapper.selectBatchIds(roleIds).stream()
                .mapToInt(Role::getDataScope)
                .min().orElse(2);
    }

    public Long getCurrentDeptId() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        return user != null ? user.getDeptId() : null;
    }
}
