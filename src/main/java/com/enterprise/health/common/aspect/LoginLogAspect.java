package com.enterprise.health.common.aspect;

import com.enterprise.health.common.exception.BusinessException;
import com.enterprise.health.domain.LoginDTO;
import com.enterprise.health.entity.LoginLog;
import com.enterprise.health.mapper.LoginLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
public class LoginLogAspect {

    private final LoginLogMapper loginLogMapper;

    public LoginLogAspect(LoginLogMapper loginLogMapper) {
        this.loginLogMapper = loginLogMapper;
    }

    @Around("execution(* com.enterprise.health.controller.AuthController.login(..))")
    public Object aroundLogin(ProceedingJoinPoint pjp) throws Throwable {
        String username = extractUsername(pjp.getArgs());

        try {
            Object result = pjp.proceed();
            record(username, 1, null);
            return result;
        } catch (BusinessException e) {
            record(username, 0, e.getMessage());
            throw e;
        }
    }

    private String extractUsername(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof LoginDTO dto) {
                return dto.username();
            }
        }
        return "unknown";
    }

    private void record(String username, int status, String failReason) {
        LoginLog log = new LoginLog();
        log.setUsername(username);
        log.setStatus(status);
        log.setFailReason(failReason);
        log.setIp(getClientIp());
        log.setUserAgent(getUserAgent());
        log.setCreatedAt(LocalDateTime.now());
        loginLogMapper.insert(log);
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                String forwarded = req.getHeader("X-Forwarded-For");
                if (forwarded != null && !forwarded.isEmpty())
                    return forwarded.split(",")[0].trim();
                return req.getRemoteAddr();
            }
        } catch (Exception ignored) {}
        return "unknown";
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return attrs.getRequest().getHeader("User-Agent");
            }
        } catch (Exception ignored) {}
        return "unknown";
    }
}
