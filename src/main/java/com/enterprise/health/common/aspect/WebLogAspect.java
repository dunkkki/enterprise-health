package com.enterprise.health.common.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.Aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

//标记这是切面类
@Aspect
//交给Spring容器管理，才能生效
@Component

public class WebLogAspect {
    //日志对象，打印控制台日志
    private static final Logger log = LoggerFactory.getLogger(WebLogAspect.class);
    //定义切点，拦截所有controller包下的全部类的所有方法
    @Pointcut("execution(* com.enterprise.health.controller.*.*(..))")
    public void controllerPointCut() {}
    //环绕通知，绑定上面的切点
    @Around("controllerPointCut()")

    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        log.info("准备执行接口：{}", methodName);
        try {
            // 执行目标接口
            Object result = joinPoint.proceed();
            long time = System.currentTimeMillis() - start;
            log.info("接口{}执行成功，耗时：{}ms", methodName, time);
            return result;
        } catch (Throwable e) {
            long time = System.currentTimeMillis() - start;
            log.error("接口{}执行失败，耗时：{}ms", methodName, time, e);
            // 把异常抛出，交给全局异常处理器处理返回前端
            throw e;
        }
    }
}
