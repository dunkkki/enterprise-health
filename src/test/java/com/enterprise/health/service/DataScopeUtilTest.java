package com.enterprise.health.service;

import cn.dev33.satoken.stp.StpUtil;
import com.enterprise.health.common.util.DataScopeUtil;
import com.enterprise.health.entity.Role;
import com.enterprise.health.entity.User;
import com.enterprise.health.mapper.RoleMapper;
import com.enterprise.health.mapper.UserMapper;
import com.enterprise.health.mapper.UserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataScopeUtilTest {

    @Mock private UserMapper userMapper;
    @Mock private UserRoleMapper userRoleMapper;
    @Mock private RoleMapper roleMapper;
    @InjectMocks private DataScopeUtil dataScopeUtil;

    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        // StpUtil is not mockable via Mockito (static), but getLoginIdAsLong()
        // will be mocked at the integration test level. These tests validate
        // the scope logic once loginId is resolved.
    }

    @Test
    void getDataScope_shouldReturnMinScope() {
        try (MockedStatic<StpUtil> stpMock = mockStatic(StpUtil.class)) {
            stpMock.when(StpUtil::getLoginIdAsLong).thenReturn(userId);
            when(userRoleMapper.selectRoleIdsByUserId(userId)).thenReturn(List.of(1L, 2L));

            Role admin = new Role();
            admin.setDataScope(0);
            Role leader = new Role();
            leader.setDataScope(1);
            when(roleMapper.selectBatchIds(anyList())).thenReturn(List.of(admin, leader));

            assertEquals(0, dataScopeUtil.getDataScope());
        }
    }

    @Test
    void getDataScope_noRoles_shouldReturn2() {
        try (MockedStatic<StpUtil> stpMock = mockStatic(StpUtil.class)) {
            stpMock.when(StpUtil::getLoginIdAsLong).thenReturn(userId);
            when(userRoleMapper.selectRoleIdsByUserId(userId)).thenReturn(List.of());

            assertEquals(2, dataScopeUtil.getDataScope());
        }
    }

    @Test
    void getCurrentDeptId_shouldReturnDeptId() {
        try (MockedStatic<StpUtil> stpMock = mockStatic(StpUtil.class)) {
            stpMock.when(StpUtil::getLoginIdAsLong).thenReturn(userId);
            User user = new User();
            user.setDeptId(3L);
            when(userMapper.selectById(userId)).thenReturn(user);

            assertEquals(3L, dataScopeUtil.getCurrentDeptId());
        }
    }

    @Test
    void getCurrentDeptId_userNotFound_shouldReturnNull() {
        try (MockedStatic<StpUtil> stpMock = mockStatic(StpUtil.class)) {
            stpMock.when(StpUtil::getLoginIdAsLong).thenReturn(userId);
            when(userMapper.selectById(userId)).thenReturn(null);

            assertEquals(null, dataScopeUtil.getCurrentDeptId());
        }
    }
}
