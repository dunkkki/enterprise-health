package com.enterprise.health.rag;

import dev.langchain4j.model.chat.ChatLanguageModel;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final ChatLanguageModel chatModel;
    // 内存文档库：每个 Chunk 是一段文字 + 文档名
    private final List<Chunk> chunks = new ArrayList<>();

    public RagService(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
    }

    // ============ 启动时加载知识库 ============
    @PostConstruct
    public void initKnowledgeBase() {
        String[] files = {"体检指标解读.md", "高血压防治指南.md", "糖尿病预防指南.md",
                          "企业健康干预方案.md", "健康饮食建议.md"};
        int total = 0;
        for (String fileName : files) {
            try {
                String content = loadResource("rag/" + fileName);
                List<String> parts = splitText(content, 500);
                for (int i = 0; i < parts.size(); i++) {
                    chunks.add(new Chunk(parts.get(i), fileName, i));
                }
                total += parts.size();
                log.info("知识库加载完成: {} → {} 段", fileName, parts.size());
            } catch (Exception e) {
                log.warn("跳过文件 {}: {}", fileName, e.getMessage());
            }
        }
        log.info("知识库初始化完成，共 {} 段，{} 个文档", total, files.length);
    }

    // ============ 核心方法：检索 + 生成 ============
    public Map<String, Object> ask(String query, int maxResults) {
        long start = System.currentTimeMillis();

        // ① 关键词检索 — 用词频重叠度打分
        List<Chunk> top = search(query, maxResults);
        long searchMs = System.currentTimeMillis() - start;

        // ② 没找到 → 不瞎编
        if (top.isEmpty()) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("advice", "抱歉，知识库中暂未找到与您问题相关的健康知识。");
            result.put("sources", List.of());
            return result;
        }

        // ③ 拼参考资料
        List<String> sources = new ArrayList<>();
        StringBuilder context = new StringBuilder();
        for (Chunk c : top) {
            sources.add(c.docName);
            context.append(c.text).append("\n\n");
        }

        // ④ 调大模型生成
        String prompt = """
                你是一个企业健康管理助手。请严格根据以下参考资料回答用户的问题。
                如果参考资料中没有相关信息，直接说"未找到相关知识"，不要编造。

                参考资料：
                %s

                用户问题：%s
                请用简洁的中文回答，控制在 300 字以内。
                """.formatted(context.toString(), query);

        String answer = chatModel.generate(prompt);
        long genMs = System.currentTimeMillis() - start - searchMs;

        log.info("RAG 完成: 检索={}ms, 生成={}ms, 命中{}/{}条",
                searchMs, genMs, top.size(), chunks.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("advice", answer);
        result.put("sources", sources.stream().distinct().toList());
        return result;
    }

    // ============ 关键词检索算法 ============
    private List<Chunk> search(String query, int topK) {
        // 1. 把问题拆成关键词（去掉常见停用词）
        String[] stopWords = {"的","了","在","是","我","有","和","就","不","人","都","一","一个",
                              "上","也","很","到","说","要","去","你","会","着","没有","看","好",
                              "自己","这","他","她","它","们","那","些","什么","怎么","如何",
                              "进行","需要","包括","对于","通过","可以","应该","能够","是否","哪些","他的","她的","请","该","等","及","或","为","与","从","被","把","向","对",
                              "然后","所以","因为","如果","虽然","但是","而且","以及","关于","按照","除了","的话","就是","还是","或者","不过","没有","这个","那个","这些","那些"};
        String[] words = query.split("[，,。.！!？?；;：:\\s、]+");
        List<String> keywords = new ArrayList<>();
        for (String w : words) {
            String kw = w.trim().toLowerCase();
            if (kw.length() < 2) continue;
            boolean isStop = false;
            for (String sw : stopWords) {
                if (sw.equals(kw)) { isStop = true; break; }
            }
            if (!isStop) keywords.add(kw);
        }

        // 2. 计算每个文档块的关键词命中分
        List<Pair> scored = new ArrayList<>();
        for (Chunk chunk : chunks) {
            String text = chunk.text.toLowerCase();
            int score = 0;
            for (String kw : keywords) {
                int count = countOccurrences(text, kw);
                if (count > 0) score += count * kw.length(); // 长词权重更高
            }
            if (score > 0) {
                scored.add(new Pair(chunk, score));
            }
        }

        // 3. 按分数排序，取 top K
        scored.sort((a, b) -> Integer.compare(b.score, a.score));
        List<Chunk> result = new ArrayList<>();
        for (int i = 0; i < Math.min(topK, scored.size()); i++) {
            result.add(scored.get(i).chunk);
        }
        return result;
    }

    // 统计子串出现次数
    private int countOccurrences(String text, String keyword) {
        int count = 0, idx = 0;
        while ((idx = text.indexOf(keyword, idx)) != -1) {
            count++;
            idx += keyword.length();
        }
        return count;
    }

    // ============ 文本切片 ============
    private List<String> splitText(String content, int maxLen) {
        List<String> parts = new ArrayList<>();
        String[] paragraphs = content.split("\n\n");
        StringBuilder buf = new StringBuilder();
        for (String p : paragraphs) {
            String t = p.trim();
            if (t.isEmpty()) continue;
            // 只跳过一级标题，保留 ## 二级标题
            if (t.startsWith("# ") && !t.startsWith("## ")) continue;
            if (buf.length() + t.length() > maxLen && !buf.isEmpty()) {
                parts.add(buf.toString().trim());
                buf.setLength(0);
            }
            buf.append(t).append("\n");
        }
        if (!buf.isEmpty()) parts.add(buf.toString().trim());
        return parts;
    }

    private String loadResource(String path) throws Exception {
        ClassPathResource r = new ClassPathResource(path);
        try (InputStream is = r.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    // 内部类：文档块
    record Chunk(String text, String docName, int index) {}
    // 内部类：检索打分
    private record Pair(Chunk chunk, int score) {}
}
