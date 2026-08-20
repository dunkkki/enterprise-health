package com.enterprise.health.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.enterprise.health.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final MenuMapper menuMapper;
    private final RoleMapper roleMapper;
    private final LoginLogMapper loginLogMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(UserMapper userMapper, UserRoleMapper userRoleMapper,
                           RoleMenuMapper roleMenuMapper, MenuMapper menuMapper,
                           RoleMapper roleMapper, LoginLogMapper loginLogMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.menuMapper = menuMapper;
        this.roleMapper = roleMapper;
        this.loginLogMapper = loginLogMapper;
    }

    @Override
    public Map<String, Object> login(String username, String password) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (user == null) {
            recordLoginLog(username, 0, "用户不存在");
            throw new BusinessException(400, "用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            recordLoginLog(username, 0, "账号禁用");
            throw new BusinessException(400, "账号已被禁用");
        }
        if (!encoder.matches(password, user.getPassword())) {
            recordLoginLog(username, 0, "密码错误");
            throw new BusinessException(400, "用户名或密码错误");
        }

        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();

        recordLoginLog(username, 1, null);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", buildUserInfo(user));
        result.put("menus", buildMenuTree(user.getId()));
        return result;
    }

    // 写入登录日志，日志记录失败不影响登录流程
    private void recordLoginLog(String username, int status, String failReason) {
        try {
            LoginLog log = new LoginLog();
            log.setUsername(username);
            log.setStatus(status);
            log.setFailReason(failReason);
            log.setIp(getClientIp());
            log.setUserAgent(getUserAgent());
            log.setCreatedAt(LocalDateTime.now());
            loginLogMapper.insert(log);
        } catch (Exception e) {
            // 忽略日志写入异常，不阻断登录
        }
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return "";
            HttpServletRequest request = attrs.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
            if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
            return ip != null ? ip : "";
        } catch (Exception e) {
            return "";
        }
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return "";
            return attrs.getRequest().getHeader("User-Agent");
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public Map<String, Object> me(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(400, "用户不存在");
        Map<String, Object> result = new HashMap<>();
        result.put("user", buildUserInfo(user));
        result.put("menus", buildMenuTree(userId));
        return result;
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (!encoder.matches(oldPassword, user.getPassword()))
            throw new BusinessException(400, "原密码错误");
        user.setPassword(encoder.encode(newPassword));
        userMapper.updateById(user);
    }

    private Map<String, Object> buildUserInfo(User u) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", u.getId().toString());
        info.put("username", u.getUsername());
        info.put("realName", u.getRealName());
        info.put("employeeNo", u.getEmployeeNo());
        info.put("gender", u.getGender());
        info.put("phone", u.getPhone());
        info.put("email", u.getEmail());
        info.put("deptId", u.getDeptId() != null ? u.getDeptId().toString() : null);
        info.put("position", u.getPosition());

        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(u.getId());
        List<String> roles = roleIds.isEmpty() ? java.util.Collections.emptyList()
                : roleMapper.selectBatchIds(roleIds).stream()
                .map(Role::getCode).toList();
        info.put("roles", roles);

        return info;
    }

    private List<Map<String, Object>> buildMenuTree(Long userId) {
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) return Collections.emptyList();

        List<Long> menuIds = roleMenuMapper.selectMenuIdsByRoleIds(roleIds);
        if (menuIds.isEmpty()) return Collections.emptyList();

        List<Menu> allMenus = menuMapper.selectBatchIds(menuIds).stream()
                .filter(m -> m.getVisible() == 1 && m.getType() != 2)
                .sorted(Comparator.comparingInt(m -> m.getSortOrder() != null ? m.getSortOrder() : 0))
                .collect(Collectors.toList());

        return allMenus.stream()
                .filter(m -> m.getParentId() == 0)
                .map(dir -> {
                    Map<String, Object> node = new HashMap<>();
                    node.put("id", dir.getId().toString());
                    node.put("name", dir.getName());
                    node.put("path", dir.getPath());
                    node.put("icon", dir.getIcon());
                    node.put("parentId", "0");
                    List<Map<String, Object>> children = allMenus.stream()
                            .filter(m -> m.getParentId() != null && m.getParentId().equals(dir.getId()))
                            .map(sub -> {
                                Map<String, Object> subNode = new HashMap<>();
                                subNode.put("id", sub.getId().toString());
                                subNode.put("name", sub.getName());
                                subNode.put("path", sub.getPath());
                                subNode.put("icon", sub.getIcon());
                                subNode.put("parentId", sub.getParentId().toString());
                                return subNode;
                            }).collect(Collectors.toList());
                    node.put("children", children.isEmpty() ? null : children);
                    return node;
                }).collect(Collectors.toList());
    }
}
