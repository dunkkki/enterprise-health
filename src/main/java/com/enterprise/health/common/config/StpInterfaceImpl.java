package com.enterprise.health.common.config;

import cn.dev33.satoken.stp.StpInterface;
import com.enterprise.health.entity.Role;
import com.enterprise.health.entity.Menu;
import com.enterprise.health.mapper.RoleMapper;
import com.enterprise.health.mapper.UserRoleMapper;
import com.enterprise.health.mapper.RoleMenuMapper;
import com.enterprise.health.mapper.MenuMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StpInterfaceImpl implements StpInterface {

    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;

    public StpInterfaceImpl(UserRoleMapper userRoleMapper, RoleMapper roleMapper,
                            RoleMenuMapper roleMenuMapper, MenuMapper menuMapper) {
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.menuMapper = menuMapper;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) return new ArrayList<>();
        List<Long> menuIds = roleMenuMapper.selectMenuIdsByRoleIds(roleIds);
        if (menuIds.isEmpty()) return new ArrayList<>();
        return menuMapper.selectBatchIds(menuIds).stream()
                .map(Menu::getPermission)
                .filter(p -> p != null && !p.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) return new ArrayList<>();
        return roleMapper.selectBatchIds(roleIds).stream()
                .map(Role::getCode)
                .collect(Collectors.toList());
    }
}
