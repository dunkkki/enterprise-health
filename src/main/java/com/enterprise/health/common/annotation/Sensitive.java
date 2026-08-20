package com.enterprise.health.common.annotation;

import com.enterprise.health.common.config.serializer.SensitiveSerializer;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
//字段级别的注解
//贴在字段上，而不是方法上
@Target(ElementType.FIELD)
//运行时有效
@Retention(RetentionPolicy.RUNTIME)
//组合 Jackson 注解：标了 @Sensitive 的字段自动走脱敏序列化器
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveSerializer.class)
public @interface Sensitive {
}
