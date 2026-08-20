package com.enterprise.health.common.config.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;

/**
 * 字段级脱敏序列化器，配合 @Sensitive 注解使用。
 * 根据字段名/值自动识别手机号、身份证、邮箱、密码等敏感类型做脱敏。
 */
public class SensitiveSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private final String fieldName;

    public SensitiveSerializer() {
        this.fieldName = "";
    }

    public SensitiveSerializer(String fieldName) {
        this.fieldName = fieldName == null ? "" : fieldName;
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        String name = property != null && property.getName() != null ? property.getName() : "";
        return new SensitiveSerializer(name);
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null || value.isBlank()) {
            gen.writeString(value);
            return;
        }
        gen.writeString(mask(value));
    }

    private String mask(String value) {
        String v = value.trim();
        // 手机号：11 位数字，保留前 3 后 4
        if (v.matches("^1\\d{10}$")) {
            return v.substring(0, 3) + "****" + v.substring(7);
        }
        // 身份证：18 位，保留前 4 后 4
        if (v.matches("^\\d{17}[\\dXx]$") || v.matches("^\\d{15}$")) {
            return v.substring(0, 4) + "**********" + v.substring(14);
        }
        // 邮箱：保留首字母 + @ 域名
        if (v.contains("@") && !v.startsWith("@")) {
            int at = v.indexOf('@');
            String local = v.substring(0, at);
            String domain = v.substring(at);
            if (local.length() > 1) {
                return local.charAt(0) + "***" + domain;
            }
            return "***" + domain;
        }
        // 密码等其它：一律打码（保留首尾各 1 位）
        if (v.length() <= 2) return "****";
        return v.charAt(0) + "*".repeat(Math.min(6, v.length() - 2)) + v.charAt(v.length() - 1);
    }
}
