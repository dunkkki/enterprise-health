package com.enterprise.health.service;

import cn.dev33.satoken.stp.StpUtil;
import com.enterprise.health.common.exception.BusinessException;
import com.enterprise.health.entity.LoginLog;
import com.enterprise.health.entity.Menu;
import com.enterprise.health.entity.Role;
import com.enterprise.health.entity.User;
import com.enterprise.health.mapper.LoginLogMapper;
import com.enterprise.health.mapper.MenuMapper;
import com.enterprise.health.mapper.RoleMapper;
import com.enterprise.health.mapper.RoleMenuMapper;
import com.enterprise.health.mapper.UserMapper;
import com.enterprise.health.mapper.UserRoleMapper;
import com.enterprise.health.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthLoginLogTest {

    @Mock private UserMapper userMapper;
    @Mock private UserRoleMapper userRoleMapper;
    @Mock private RoleMenuMapper roleMenuMapper;
    @Mock private MenuMapper menuMapper;
    @Mock private RoleMapper roleMapper;
    @Mock private LoginLogMapper loginLogMapper;

    private AuthServiceImpl authService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userMapper, userRoleMapper, roleMenuMapper,
                menuMapper, roleMapper, loginLogMapper);
    }

    private User buildUser(String username, String rawPassword) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setPassword(encoder.encode(rawPassword));
        user.setStatus(1);
        return user;
    }

    @Test
    void login_success_shouldWriteStatus1Log() throws Exception {
        when(userMapper.selectOne(any())).thenReturn(buildUser("emp01", "123456"));
        when(userRoleMapper.selectRoleIdsByUserId(1L)).thenReturn(List.of(1L));
        when(roleMapper.selectBatchIds(anyList())).thenReturn(List.of());
        when(roleMenuMapper.selectMenuIdsByRoleIds(anyList())).thenReturn(List.of());

        try (MockedStatic<StpUtil> stpMock = mockStatic(StpUtil.class)) {
            // login() 是 void，静默放行；只 stub getTokenValue
            stpMock.when(StpUtil::getTokenValue).thenReturn("test-token");

            authService.login("emp01", "123456");
        }

        ArgumentCaptor<LoginLog> captor = ArgumentCaptor.forClass(LoginLog.class);
        verify(loginLogMapper).insert(captor.capture());
        LoginLog log = captor.getValue();
        assertEquals("emp01", log.getUsername());
        assertEquals(1, log.getStatus());
    }

    @Test
    void login_wrongPassword_shouldWriteStatus0Log() {
        when(userMapper.selectOne(any())).thenReturn(buildUser("emp01", "123456"));

        try (MockedStatic<StpUtil> stpMock = mockStatic(StpUtil.class)) {
            assertThrows(BusinessException.class, () -> authService.login("emp01", "wrong"));
        }

        ArgumentCaptor<LoginLog> captor = ArgumentCaptor.forClass(LoginLog.class);
        verify(loginLogMapper).insert(captor.capture());
        LoginLog log = captor.getValue();
        assertEquals("emp01", log.getUsername());
        assertEquals(0, log.getStatus());
        assertEquals("密码错误", log.getFailReason());
    }

    @Test
    void login_userNotFound_shouldWriteStatus0Log() {
        when(userMapper.selectOne(any())).thenReturn(null);

        try (MockedStatic<StpUtil> stpMock = mockStatic(StpUtil.class)) {
            assertThrows(BusinessException.class, () -> authService.login("nobody", "x"));
        }

        ArgumentCaptor<LoginLog> captor = ArgumentCaptor.forClass(LoginLog.class);
        verify(loginLogMapper).insert(captor.capture());
        LoginLog log = captor.getValue();
        assertEquals("nobody", log.getUsername());
        assertEquals(0, log.getStatus());
        assertEquals("用户不存在", log.getFailReason());
    }
}
