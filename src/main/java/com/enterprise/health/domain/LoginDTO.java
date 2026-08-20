package com.enterprise.health.domain;

import com.enterprise.health.common.annotation.Sensitive;

public record LoginDTO(String username, @Sensitive String password) {}
