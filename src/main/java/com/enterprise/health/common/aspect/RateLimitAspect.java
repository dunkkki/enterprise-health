package com.enterprise.health.common.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.enterprise.health.common.annotation.RateLimit;
import com.enterprise.health.common.exception.RateLimitException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 接口限流切面
 * 基于AOP+Redis实现接口访问频次限制，搭配@RateLimit注解使用
 * 可区分登录用户/匿名访客统计访问次数，超出阈值拒绝请求
 */
@Aspect          // 标识当前类为AOP切面类
@Component       // 交给Spring容器管理，切面才能生效
public class RateLimitAspect {

    // Redis操作模板，用来读写缓存、做访问次数计数
    private final StringRedisTemplate redis;

    // 构造函数注入Redis模板（Spring构造注入推荐写法）
    public RateLimitAspect(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /**
     * 环绕通知：拦截所有添加了@RateLimit注解的接口方法
     * @param pjp 连接点对象，代表被拦截的目标接口方法
     * @param rateLimit 捕获方法上的限流注解，读取配置的次数、时间参数
     * @return 接口正常执行后的返回结果
     * @throws Throwable 超限抛出异常，由全局异常处理器统一返回提示
     */
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {
        // 1. 获取访问者唯一标识：已登录取用户ID，未登录标记为匿名用户
        String userId = StpUtil.isLogin() ? String.valueOf(StpUtil.getLoginIdAsLong()) : "anonymous";

        // 2. 拼接Redis存储key：前缀+用户标识+方法名，不同用户、不同接口分开计数
        String key = "rate_limit:" + userId + ":" + pjp.getSignature().getName();

        // 3. Redis自增：每访问一次，key对应数值+1；key不存在则默认初始化为1
        Long count = redis.opsForValue().increment(key);

        // 4. 第一次访问时，给key设置过期时间（时间窗口），到期自动清空计数
        if (count == 1) {
            redis.expire(key, rateLimit.timeWindow(), TimeUnit.SECONDS);
        }

        // 5. 判断：访问次数超过设定最大阈值，抛出异常拒绝访问
        if (count > rateLimit.maxRequests()) {
            throw new RateLimitException("请求太频繁，请稍后再试");
        }

        // 6. 次数未超限，放行，执行目标接口业务代码
        return pjp.proceed();
    }
}