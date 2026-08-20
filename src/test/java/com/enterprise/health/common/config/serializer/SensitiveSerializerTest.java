package com.enterprise.health.common.config.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.enterprise.health.common.annotation.Sensitive;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensitiveSerializerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static class User {
        @Sensitive public String phone;
        @Sensitive public String email;
        @Sensitive public String idCard;
        @Sensitive public String password;
        public String normal;

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getIdCard() { return idCard; }
        public void setIdCard(String idCard) { this.idCard = idCard; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getNormal() { return normal; }
        public void setNormal(String normal) { this.normal = normal; }
    }

    private String serialize(String field, String value) throws Exception {
        User u = new User();
        switch (field) {
            case "phone": u.setPhone(value); break;
            case "email": u.setEmail(value); break;
            case "idCard": u.setIdCard(value); break;
            case "password": u.setPassword(value); break;
            case "normal": u.setNormal(value); break;
        }
        return MAPPER.writeValueAsString(u);
    }

    @Test
    void phone_shouldBeMasked() throws Exception {
        String json = serialize("phone", "13812340001");
        assertTrue(json.contains("\"phone\":\"138****0001\""), "期望 138****0001，实际: " + json);
    }

    @Test
    void email_shouldBeMasked() throws Exception {
        String json = serialize("email", "zhangsan@company.com");
        assertTrue(json.contains("\"email\":\"z***@company.com\""), "期望 z***@company.com，实际: " + json);
    }

    @Test
    void idCard_shouldBeMasked() throws Exception {
        String json = serialize("idCard", "110101199001011234");
        assertTrue(json.contains("\"idCard\":\"1101**********1234\""), "期望 1101**********1234，实际: " + json);
    }

    @Test
    void password_shouldBeMasked() throws Exception {
        String json = serialize("password", "$2a$10$abcdefghijklmn");
        assertTrue(json.contains("\"password\":\"$******n\""), "期望打码，实际: " + json);
    }

    @Test
    void nullValue_shouldNotThrow() throws Exception {
        User u = new User();
        u.setPhone(null);
        assertEquals("{\"phone\":null,\"email\":null,\"idCard\":null,\"password\":null,\"normal\":null}",
                MAPPER.writeValueAsString(u));
    }
}
