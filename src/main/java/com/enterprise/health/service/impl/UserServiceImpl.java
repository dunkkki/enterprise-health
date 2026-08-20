package com.enterprise.health.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.health.common.exception.BusinessException;
import com.enterprise.health.common.util.DataScopeUtil;
import com.enterprise.health.domain.UserImportDTO;
import com.enterprise.health.entity.User;
import com.enterprise.health.mapper.UserMapper;
import com.enterprise.health.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final DataScopeUtil dataScopeUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserServiceImpl(UserMapper userMapper, DataScopeUtil dataScopeUtil) {
        this.userMapper = userMapper;
        this.dataScopeUtil = dataScopeUtil;
    }

    @Override
    public Map<String, Object> list(int page, int size, String keyword, Long deptId) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(User::getUsername, keyword).or().like(User::getRealName, keyword).or().like(User::getEmployeeNo, keyword));
        }
        if (deptId != null) qw.eq(User::getDeptId, deptId);

        int scope = dataScopeUtil.getDataScope();
        if (scope == 1) qw.eq(User::getDeptId, dataScopeUtil.getCurrentDeptId());
        if (scope == 2) qw.eq(User::getId, StpUtil.getLoginIdAsLong());

        qw.orderByDesc(User::getCreatedAt);
        Page<User> result = userMapper.selectPage(new Page<>(page, size), qw);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        return data;
    }

    @Override
    public User create(User user) {
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername())) > 0)
            throw new BusinessException(400, "用户名已存在");
        user.setPassword(encoder.encode(user.getPassword() != null ? user.getPassword() : "123456"));
        user.setStatus(1);
        userMapper.insert(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public User update(Long id, User update) {
        User existing = userMapper.selectById(id);
        if (existing == null) throw new BusinessException(400, "用户不存在");
        if (update.getRealName() != null) existing.setRealName(update.getRealName());
        if (update.getEmployeeNo() != null) existing.setEmployeeNo(update.getEmployeeNo());
        if (update.getGender() != null) existing.setGender(update.getGender());
        if (update.getPhone() != null) existing.setPhone(update.getPhone());
        if (update.getEmail() != null) existing.setEmail(update.getEmail());
        if (update.getDeptId() != null) existing.setDeptId(update.getDeptId());
        if (update.getPosition() != null) existing.setPosition(update.getPosition());
        if (update.getHireDate() != null) existing.setHireDate(update.getHireDate());
        if (update.getBirthDate() != null) existing.setBirthDate(update.getBirthDate());
        userMapper.updateById(existing);
        return existing;
    }

    @Override
    public void delete(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(400, "用户不存在");
        userMapper.deleteById(id);
    }

    @Override
    public void toggleStatus(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(400, "用户不存在");
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        userMapper.updateById(user);
    }

    @Override
    public int importExcel(InputStream inputStream) {
        AtomicInteger count = new AtomicInteger(0);
        EasyExcel.read(inputStream, UserImportDTO.class, new UserImportListener(userMapper, encoder, count)).sheet().doRead();
        return count.get();
    }
}
