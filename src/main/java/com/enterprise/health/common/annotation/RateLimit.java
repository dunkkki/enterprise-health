package com.enterprise.health.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
//贴在方法上
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
//自定义注解关键字：@interface
public @interface RateLimit {
    //时间窗口内最大请求次数，默认10词
    int maxRequests() default 10;
    //统计时间范围，单位秒，默认60秒
    int timeWindow() default 60;
}
