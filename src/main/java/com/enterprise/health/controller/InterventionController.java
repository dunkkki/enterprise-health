package com.enterprise.health.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.enterprise.health.common.result.Result;
import com.enterprise.health.entity.InterventionParticipant;
import com.enterprise.health.mapper.InterventionParticipantMapper;
import com.enterprise.health.service.InterventionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/interventions")
public class InterventionController {

    private final InterventionService interventionService;
    private final InterventionParticipantMapper participantMapper;

    public InterventionController(InterventionService interventionService,
                                   InterventionParticipantMapper participantMapper) {
        this.interventionService = interventionService;
        this.participantMapper = participantMapper;
    }

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String type,
                          @RequestParam(required = false) Integer status) {
        return Result.ok(interventionService.list(page, size, type, status));
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.ok(interventionService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("intervention:create")
    public Result<?> create(@RequestBody com.enterprise.health.entity.InterventionPlan plan) {
        return Result.ok(interventionService.create(plan));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("intervention:update")
    public Result<?> update(@PathVariable Long id,
                            @RequestBody com.enterprise.health.entity.InterventionPlan plan) {
        return Result.ok(interventionService.update(id, plan));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("intervention:delete")
    public Result<?> delete(@PathVariable Long id) {
        interventionService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    @SaCheckPermission("intervention:update")
    public Result<?> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        interventionService.changeStatus(id, status);
        return Result.ok();
    }

    @GetMapping("/{id}/participants")
    public Result<?> participants(@PathVariable Long id) {
        List<InterventionParticipant> list = participantMapper.selectList(
                new LambdaQueryWrapper<InterventionParticipant>()
                        .eq(InterventionParticipant::getPlanId, id));
        return Result.ok(list);
    }

    @PostMapping("/{id}/participants")
    @SaCheckPermission("intervention:update")
    public Result<?> addParticipants(@PathVariable Long id, @RequestBody List<Long> userIds) {
        for (Long uid : userIds) {
            if (participantMapper.selectCount(new LambdaQueryWrapper<InterventionParticipant>()
                    .eq(InterventionParticipant::getPlanId, id)
                    .eq(InterventionParticipant::getUserId, uid)) == 0) {
                InterventionParticipant p = new InterventionParticipant();
                p.setPlanId(id);
                p.setUserId(uid);
                p.setStatus(0);
                p.setJoinedAt(LocalDateTime.now());
                participantMapper.insert(p);
            }
        }
        return Result.ok();
    }

    @PutMapping("/{id}/participants/{uid}")
    @SaCheckPermission("intervention:update")
    public Result<?> updateParticipant(@PathVariable Long id, @PathVariable Long uid,
                                        @RequestParam Integer status) {
        InterventionParticipant p = participantMapper.selectOne(
                new LambdaQueryWrapper<InterventionParticipant>()
                        .eq(InterventionParticipant::getPlanId, id)
                        .eq(InterventionParticipant::getUserId, uid));
        if (p == null) return Result.fail(400, "参与者不存在");
        p.setStatus(status);
        if (status == 2) p.setCompletedAt(LocalDateTime.now());
        participantMapper.updateById(p);
        return Result.ok();
    }
}
