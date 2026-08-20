package com.enterprise.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.health.common.exception.BusinessException;
import com.enterprise.health.entity.Department;
import com.enterprise.health.entity.User;
import com.enterprise.health.mapper.DepartmentMapper;
import com.enterprise.health.mapper.UserMapper;
import com.enterprise.health.service.DeptService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class DeptServiceImpl implements DeptService {

    private final DepartmentMapper deptMapper;
    private final UserMapper userMapper;

    public DeptServiceImpl(DepartmentMapper deptMapper, UserMapper userMapper) {
        this.deptMapper = deptMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<Map<String, Object>> tree() {
        List<Department> all = deptMapper.selectList(new LambdaQueryWrapper<Department>()
                .eq(Department::getStatus, 1)
                .orderByAsc(Department::getSortOrder));
        return all.stream()
                .filter(d -> d.getParentId() == 0)
                .map(root -> buildNode(root, all))
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildNode(Department dept, List<Department> all) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", dept.getId().toString());
        node.put("name", dept.getName());
        node.put("parentId", dept.getParentId().toString());
        node.put("sortOrder", dept.getSortOrder());
        List<Map<String, Object>> children = all.stream()
                .filter(d -> d.getParentId() != null && d.getParentId().equals(dept.getId()))
                .map(d -> buildNode(d, all))
                .collect(Collectors.toList());
        node.put("children", children);
        return node;
    }

    @Override
    public Department create(Department dept) {
        dept.setStatus(1);
        deptMapper.insert(dept);
        return dept;
    }

    @Override
    public Department update(Long id, Department dept) {
        Department existing = deptMapper.selectById(id);
        if (existing == null) throw new BusinessException(400, "部门不存在");
        if (dept.getName() != null) existing.setName(dept.getName());
        if (dept.getParentId() != null) existing.setParentId(dept.getParentId());
        if (dept.getSortOrder() != null) existing.setSortOrder(dept.getSortOrder());
        deptMapper.updateById(existing);
        return existing;
    }

    @Override
    public void delete(Long id) {
        if (deptMapper.selectCount(new LambdaQueryWrapper<Department>().eq(Department::getParentId, id)) > 0)
            throw new BusinessException(400, "该部门下有子部门，无法删除");
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getDeptId, id)) > 0)
            throw new BusinessException(400, "该部门下有员工，无法删除");
        deptMapper.deleteById(id);
    }
}
