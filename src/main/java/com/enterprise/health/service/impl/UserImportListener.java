package com.enterprise.health.service.impl;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.enterprise.health.domain.UserImportDTO;
import com.enterprise.health.entity.User;
import com.enterprise.health.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.concurrent.atomic.AtomicInteger;

public class UserImportListener implements ReadListener<UserImportDTO> {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder encoder;
    private final AtomicInteger count;

    public UserImportListener(UserMapper userMapper, BCryptPasswordEncoder encoder, AtomicInteger count) {
        this.userMapper = userMapper;
        this.encoder = encoder;
        this.count = count;
    }

    @Override
    public void invoke(UserImportDTO dto, AnalysisContext context) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setRealName(dto.getRealName());
        user.setEmployeeNo(dto.getEmployeeNo());
        user.setGender("女".equals(dto.getGender()) ? 0 : 1);
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setDeptId(dto.getDeptId());
        user.setPosition(dto.getPosition());
        user.setPassword(encoder.encode("123456"));
        user.setStatus(1);
        userMapper.insert(user);
        count.incrementAndGet();
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {}
}
