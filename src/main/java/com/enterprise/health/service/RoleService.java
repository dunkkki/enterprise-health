package com.enterprise.health.service;

import com.enterprise.health.entity.Role;
import java.util.List;
import java.util.Map;

public interface RoleService {
    List<Role> list();
    Role create(Role role);
    Role update(Long id, Role role);
    void delete(Long id);
    Map<String, Object> getMenus(Long roleId);
    void assignMenus(Long roleId, List<Long> menuIds);
}
