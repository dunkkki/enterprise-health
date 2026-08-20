package com.enterprise.health.common.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.enterprise.health.entity.OperationLog;
import com.enterprise.health.mapper.OperationLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
public class OperationLogAspect {

    private final OperationLogMapper logMapper;
    private final ObjectMapper objectMapper;

    public OperationLogAspect(OperationLogMapper logMapper, ObjectMapper objectMapper) {
        this.logMapper = logMapper;
        this.objectMapper = objectMapper;
    }

    @Pointcut("execution(* com.enterprise.health.controller..*(..)) && " +
              "(@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.DeleteMapping))")
    public void writeOperation() {}

    @Around("writeOperation()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = pjp.proceed();
        long duration = System.currentTimeMillis() - start;

        try {
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            Method method = signature.getMethod();
            String httpMethod = "";
            if (method.isAnnotationPresent(org.springframework.web.bind.annotation.PostMapping.class))
                httpMethod = "POST";
            else if (method.isAnnotationPresent(org.springframework.web.bind.annotation.PutMapping.class))
                httpMethod = "PUT";
            else if (method.isAnnotationPresent(org.springframework.web.bind.annotation.DeleteMapping.class))
                httpMethod = "DELETE";

            String url = "";
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                url = req.getRequestURI();
            }

            OperationLog log = new OperationLog();
            if (StpUtil.isLogin()) {
                log.setUserId(StpUtil.getLoginIdAsLong());
                Object loginId = StpUtil.getLoginId();
                log.setUsername(loginId != null ? loginId.toString() : "");
            }
            log.setModule(getModule(url));
            log.setAction(signature.getMethod().getName());
            log.setDescription(pjp.getTarget().getClass().getSimpleName() + "." + signature.getMethod().getName());
            log.setRequestMethod(httpMethod);
            log.setRequestUrl(url);
            String requestParams = objectMapper.writeValueAsString(pjp.getArgs());
            for (Object arg : pjp.getArgs()) {
                if (arg == null) continue;
                for (java.lang.reflect.Field field : arg.getClass().getDeclaredFields()) {
                    if
                    (field.isAnnotationPresent(com.enterprise.health.common.annotation.Sensitive.class)) {
                        field.setAccessible(true);
                        Object originalValue = field.get(arg);   // 先记下原值
                        field.set(arg, "***");                     // 临时改成 ***
                        requestParams = objectMapper.writeValueAsString(pjp.getArgs());  // 序列化是 ***
                        field.set(arg, originalValue);            // 改回去，业务逻辑不受影响
                    }
                }
            }
            log.setRequestParams(truncate(requestParams, 500));
            log.setIp(getClientIp());
            log.setDuration((int) duration);
            log.setCreatedAt(LocalDateTime.now());
            logMapper.insert(log);
        } catch (Exception ignored) {
            // logging should never break business flow
        }

        return result;
    }

    private String getModule(String url) {
        if (url == null) return "未知";
        if (url.contains("/depts")) return "部门管理";
        if (url.contains("/users")) return "用户管理";
        if (url.contains("/roles")) return "角色管理";
        if (url.contains("/menus")) return "菜单管理";
        if (url.contains("/packages")) return "体检套餐";
        if (url.contains("/schedules")) return "体检排期";
        if (url.contains("/records")) return "体检记录";
        if (url.contains("/risk-rules")) return "风险规则";
        if (url.contains("/risk-results")) return "风险评估";
        if (url.contains("/interventions")) return "干预管理";
        if (url.contains("/follow-ups")) return "随访记录";
        return "其他";
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
        return "";
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }
}
