package com.enterprise.health.service;

import com.enterprise.health.entity.FollowUpRecord;
import java.util.Map;

public interface FollowUpService {
    Map<String, Object> list(int page, int size, Long planId, Long userId);
    FollowUpRecord create(FollowUpRecord record);
    FollowUpRecord update(Long id, FollowUpRecord update);
}
