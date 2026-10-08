package com.zyx.consultant.knowledge;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.scoring.ScoringModel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 教学用重排模型：按查询词在片段中的覆盖比例打分。
 *
 * <p>它不是云端重排模型，只是为了让你先看懂 LangChain4j
 * ReRankingContentAggregator 如何接收 ScoringModel。
 * 生产项目可以替换为真实的 Cross-Encoder 或云端 Reranker。</p>
 */
public class KeywordOverlapScoringModel implements ScoringModel {

    @Override
    public Response<List<Double>> scoreAll(
            List<TextSegment> segments,
            String query) {
        Set<String> queryTerms = terms(query);
        List<Double> scores = segments.stream()
                .map(segment -> score(segment.text(), queryTerms))
                .toList();
        return Response.from(scores);
    }

    /**
     * 计算片段覆盖了多少查询词。
     */
    private double score(String text, Set<String> queryTerms) {
        if (queryTerms.isEmpty()) {
            return 0.0;
        }
        Set<String> textTerms = terms(text);
        long matched = queryTerms.stream()
                .filter(textTerms::contains)
                .count();
        return (double) matched / queryTerms.size();
    }

    private Set<String> terms(String text) {
        Set<String> terms = Arrays.stream(text.toLowerCase(Locale.ROOT)
                        .split("[\\s，。！？、：；,.!?;:]+"))
                .filter(term -> !term.isBlank())
                .collect(java.util.stream.Collectors.toCollection(HashSet::new));

        // 中文通常没有空格分词，因此额外加入相邻二字片段，避免简单分词完全匹配不到。
        for (String term : new HashSet<>(terms)) {
            if (term.length() > 1) {
                for (int index = 0; index < term.length() - 1; index++) {
                    terms.add(term.substring(index, index + 2));
                }
            }
        }
        return terms;
    }
}
