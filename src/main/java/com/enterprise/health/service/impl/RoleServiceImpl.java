package com.enterprise.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.health.common.exception.BusinessException;
import com.enterprise.health.entity.Role;
import com.enterprise.health.entity.RoleMenu;
import com.enterprise.health.entity.UserRole;
import com.enterprise.health.entity.Menu;
import com.enterprise.health.mapper.RoleMapper;
import com.enterprise.health.mapper.UserRoleMapper;
import com.enterprise.health.mapper.RoleMenuMapper;
import com.enterprise.health.mapper.MenuMapper;
import com.enterprise.health.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;
    private final UserRoleMapper userRoleMapper;

    public RoleServiceImpl(RoleMapper roleMapper, RoleMenuMapper roleMenuMapper,
                           MenuMapper menuMapper, UserRoleMapper userRoleMapper) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.menuMapper = menuMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public List<Role> list() {
        return roleMapper.selectList(null);
    }

    @Override
    public Role create(Role role) {
        roleMapper.insert(role);
        return role;
    }

    @Override
    public Role update(Long id, Role update) {
        Role existing = roleMapper.selectById(id);
        if (existing == null) throw new BusinessException(400, "角色不存在");
        if (update.getName() != null) existing.setName(update.getName());
        if (update.getDescription() != null) existing.setDescription(update.getDescription());
        if (update.getDataScope() != null) existing.setDataScope(update.getDataScope());
        roleMapper.updateById(existing);
        return existing;
    }

    @Override
    public void delete(Long id) {
        if (roleMapper.selectById(id) == null) throw new BusinessException(400, "角色不存在");
        long userCount = userRoleMapper.selectCount(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id));
        if (userCount > 0) throw new BusinessException(400, "该角色下还有 " + userCount + " 个用户，请先移除用户再删除角色");
        roleMapper.deleteById(id);
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, id));
    }

    @Override
    public Map<String, Object> getMenus(Long roleId) {
        List<Long> menuIds = roleMenuMapper.selectMenuIdsByRoleIds(List.of(roleId));
        Map<String, Object> result = new HashMap<>();
        result.put("roleId", roleId.toString());
        result.put("menuIds", menuIds.stream().map(String::valueOf).collect(Collectors.toList()));
        return result;
    }

    @Override
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId));
        for (Long menuId : menuIds) {
            RoleMenu rm = new RoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            roleMenuMapper.insert(rm);
        }
    }
}
