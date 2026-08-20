package com.enterprise.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.health.common.exception.BusinessException;
import com.enterprise.health.entity.Menu;
import com.enterprise.health.entity.RoleMenu;
import com.enterprise.health.mapper.MenuMapper;
import com.enterprise.health.mapper.RoleMenuMapper;
import com.enterprise.health.service.MenuService;
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
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;

    public MenuServiceImpl(MenuMapper menuMapper, RoleMenuMapper roleMenuMapper) {
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    @Override
    public List<Map<String, Object>> tree() {
        List<Menu> all = menuMapper.selectList(new LambdaQueryWrapper<Menu>()
                .orderByAsc(Menu::getSortOrder));
        return all.stream()
                .filter(m -> m.getParentId() == 0)
                .map(root -> buildNode(root, all))
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildNode(Menu menu, List<Menu> all) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", menu.getId().toString());
        node.put("name", menu.getName());
        node.put("parentId", menu.getParentId().toString());
        node.put("path", menu.getPath());
        node.put("icon", menu.getIcon());
        node.put("sortOrder", menu.getSortOrder());
        node.put("type", menu.getType());
        node.put("permission", menu.getPermission());
        node.put("visible", menu.getVisible());
        List<Map<String, Object>> children = all.stream()
                .filter(m -> m.getParentId() != null && m.getParentId().equals(menu.getId()))
                .map(m -> buildNode(m, all))
                .collect(Collectors.toList());
        node.put("children", children);
        return node;
    }

    @Override
    public Menu create(Menu menu) {
        menuMapper.insert(menu);
        return menu;
    }

    @Override
    public Menu update(Long id, Menu update) {
        Menu existing = menuMapper.selectById(id);
        if (existing == null) throw new BusinessException(400, "菜单不存在");
        if (update.getName() != null) existing.setName(update.getName());
        if (update.getPath() != null) existing.setPath(update.getPath());
        if (update.getIcon() != null) existing.setIcon(update.getIcon());
        if (update.getSortOrder() != null) existing.setSortOrder(update.getSortOrder());
        if (update.getPermission() != null) existing.setPermission(update.getPermission());
        if (update.getVisible() != null) existing.setVisible(update.getVisible());
        if (update.getParentId() != null) existing.setParentId(update.getParentId());
        if (update.getType() != null) existing.setType(update.getType());
        menuMapper.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (menuMapper.selectCount(new LambdaQueryWrapper<Menu>().eq(Menu::getParentId, id)) > 0)
            throw new BusinessException(400, "该菜单下有子菜单，无法删除");
        menuMapper.deleteById(id);
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getMenuId, id));
    }
}
