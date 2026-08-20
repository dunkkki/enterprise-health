package com.enterprise.health.service;

import com.enterprise.health.entity.Menu;
import java.util.List;
import java.util.Map;

public interface MenuService {
    List<Map<String, Object>> tree();
    Menu create(Menu menu);
    Menu update(Long id, Menu menu);
    void delete(Long id);
}
