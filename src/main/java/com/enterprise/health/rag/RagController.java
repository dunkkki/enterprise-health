package com.enterprise.health.rag;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.enterprise.health.common.exception.BusinessException;
import com.enterprise.health.common.result.Result;
import com.enterprise.health.common.util.DataScopeUtil;
import com.enterprise.health.entity.ExamRecord;
import com.enterprise.health.entity.ExamResultItem;
import com.enterprise.health.entity.User;
import com.enterprise.health.mapper.ExamRecordMapper;
import com.enterprise.health.mapper.ExamResultItemMapper;
import com.enterprise.health.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;
    private final ExamRecordMapper recordMapper;
    private final ExamResultItemMapper resultItemMapper;
    private final UserMapper userMapper;
    private final DataScopeUtil dataScopeUtil;

    public RagController(RagService ragService,
                         ExamRecordMapper recordMapper,
                         ExamResultItemMapper resultItemMapper,
                         UserMapper userMapper,
                         DataScopeUtil dataScopeUtil) {
        this.ragService = ragService;
        this.recordMapper = recordMapper;
        this.resultItemMapper = resultItemMapper;
        this.userMapper = userMapper;
        this.dataScopeUtil = dataScopeUtil;
    }

    // ============ 嵌入一：体检健康建议 ============
    // 前端在体检详情页调用，拿到异常指标后自动生成建议
    @GetMapping("/health-advice")
    public Result<Map<String, Object>> healthAdvice(@RequestParam Long recordId) {
        // 数据权限校验：员工只能看自己的体检记录，leader 只能看本部门的
        ExamRecord record = recordMapper.selectById(recordId);
        if (record == null) throw new BusinessException(400, "记录不存在");
        checkViewPermission(record);

        // 查异常指标
        List<ExamResultItem> items = resultItemMapper.selectList(
                new LambdaQueryWrapper<ExamResultItem>()
                        .eq(ExamResultItem::getRecordId, recordId)
                        .ne(ExamResultItem::getIsAbnormal, 0));

        if (items.isEmpty()) {
            return Result.ok(Map.of("advice", "本次体检各项指标均在正常范围内，请继续保持良好的生活习惯。",
                    "sources", List.of()));
        }

        // 拼查询文本
        String query = "用户体检发现以下异常指标：";
        for (ExamResultItem item : items) {
            query += "指标ID" + item.getPackageItemId() + "异常(" + item.getItemValue() + "); ";
        }
        query += "请给出综合健康建议。";

        Map<String, Object> result = ragService.ask(query, 3);
        return Result.ok(result);
    }

    // ============ 嵌入二：干预方案建议 ============
    // HR 在评估结果页点击按钮，根据风险等级和命中规则生成干预建议
    @PostMapping("/suggest-plan")
    @SaCheckPermission("risk:assess")
    public Result<Map<String, Object>> suggestPlan(@RequestBody Map<String, Object> body) {
        String riskLevel = (String) body.getOrDefault("riskLevel", "");
        Object score = body.getOrDefault("totalScore", 0);
        @SuppressWarnings("unchecked")
        List<String> hitRules = (List<String>) body.getOrDefault("hitRules", List.of());

        String query = "员工风险评估结果：风险等级" + riskLevel
                + "，总分" + score + "，命中规则：" + String.join("、", hitRules)
                + "。请设计具体的干预方案，包括干预类型、周期、步骤。";

        Map<String, Object> result = ragService.ask(query, 3);
        return Result.ok(result);
    }

    // 数据权限：scope=0 全量，scope=1 本部门，scope=2 仅本人
    private void checkViewPermission(ExamRecord record) {
        int scope = dataScopeUtil.getDataScope();
        if (scope == 0) return;
        if (scope == 2) {
            if (!record.getUserId().equals(StpUtil.getLoginIdAsLong())) {
                throw new BusinessException(403, "无权查看他人体检记录");
            }
            return;
        }
        // scope == 1：本部门
        User owner = userMapper.selectById(record.getUserId());
        Long deptId = dataScopeUtil.getCurrentDeptId();
        if (owner == null || owner.getDeptId() == null || !owner.getDeptId().equals(deptId)) {
            throw new BusinessException(403, "无权查看其他部门的体检记录");
        }
    }
}
