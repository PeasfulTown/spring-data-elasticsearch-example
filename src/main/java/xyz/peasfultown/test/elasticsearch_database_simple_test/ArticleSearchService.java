package xyz.peasfultown.test.elasticsearch_database_simple_test;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleSearchService {
    private final ElasticsearchOperations ops;

    public List<Article> searchArticles(ArticleSearchRequest request) {
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        if (request.getTextQuery() != null && !request.getTextQuery().isBlank()) {
            boolBuilder.must(m -> m
                    .bool(b -> b
                            .should(s -> s.match(mt -> mt
                                    .field("title")
                                    .query(request.getTextQuery())
                                    .fuzziness("AUTO")))
                            .should(s -> s.match(md -> md
                                    .field("description")
                                    .query(request.getTextQuery())
                                    .fuzziness("AUTO")))
                    )
            );
        }

        // strict search: in order to search for authors have to type the exact author
        // name, case sensitive, full name as it is in the database, have to set the entity
        // field to FieldType.Keyword
//        if (request.getAuthors() != null && !request.getAuthors().isEmpty()) {
//            for (String author : request.getAuthors()) {
//                if (author != null && !author.isBlank()) {
//                    boolBuilder.filter(f -> f
//                            .nested(n -> n
//                                    .path("authors")
//                                    .query(nq -> nq
//                                            .term(t -> t
//                                                    .field("authors.name")
//                                                    .value(author)))));
//                }
//            }
//        }

        if (request.getAuthors() != null && !request.getAuthors().isEmpty()) {
            for (String author : request.getAuthors()) {
                if (author != null && !author.isBlank()) {
                    boolBuilder.filter(f -> f
                            .nested(n -> n
                                    .path("authors")
                                    .query(nq -> nq
                                            .match(m -> m
                                                    .field("authors.name")
                                                    .query(author)
                                                    .fuzziness("AUTO")))));
                }
            }
        }

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            for (String tag : request.getTags()) {
                if (tag != null && !tag.isBlank()) {
                    boolBuilder.filter(f -> f
                            .nested(n -> n
                                    .path("tags")
                                    .query(nq -> nq
                                            .match(m -> m
                                                    .field("tags.name")
                                                    .query(tag)))));
                }
            }
        }

        if (request.getTimeFilter() != null) {
            Instant cutoff = switch (request.getTimeFilter()) {
                case PAST_24_HOURS -> Instant.now().minus(1, ChronoUnit.DAYS);
                case PAST_WEEK -> Instant.now().minus(7, ChronoUnit.DAYS);
                case PAST_MONTH -> Instant.now().minus(31, ChronoUnit.DAYS);
                case PAST_6_MONTHS -> Instant.now().minus(183, ChronoUnit.DAYS);
                case PAST_YEAR -> Instant.now().minus(366, ChronoUnit.DAYS);
                default -> null;
            };

            if (cutoff != null) {
                boolBuilder.filter(f -> f
                        .range(r -> r
                                .date(d -> d
                                        .field("createdAt")
                                        .gte(cutoff.toString()))));
            }
        }

        if (request.getAdditionalProperties() != null && !request.getAdditionalProperties().isEmpty()) {
            for (Map.Entry<String, Object> prop : request.getAdditionalProperties().entrySet()) {
                if (prop.getKey() != null && !prop.getKey().isBlank()) {
                    Object value = prop.getValue();
                    if (Objects.nonNull(value)) {
                        String fieldPath = "additionalProperties." + prop.getKey();
                        if (value instanceof String) {
                            boolBuilder.filter(f -> f
                                    .term(t -> t
                                            .field(fieldPath)
                                            .value((String) value)));
                        } else if (value instanceof Integer) {
                            boolBuilder.filter(f -> f
                                    .term(t -> t
                                            .field(fieldPath)
                                            .value(Integer.valueOf(value.toString()))));
                        } else if (value instanceof Map<?, ?>) {
                            Map<String, Object> innerMap = (Map<String, Object>) value;
                            log.info("dynamic query map object detected: {}", innerMap);
                            Object innerValue = innerMap.get("value");
                            switch (QueryType.valueOf(innerMap.get("queryType").toString())) {
                                case RANGE -> {
                                    if (!(innerValue instanceof List<?>) || ((List<?>) innerValue).isEmpty()) {
                                        log.error("unable to cast value object to list when dynamic query type is RANGE");
                                        log.error("value: {}", innerMap.get("value"));
                                        throw new IllegalArgumentException("unable to cast value object when query type is RANGE, value should be a list of 2 values");
                                    }

                                    List<?> innerValues = (List<?>) innerMap.get("value");
                                    if (innerValues.get(0) instanceof Integer) {
                                        boolBuilder.filter(f -> f
                                                .range(r -> r
                                                        .number(n -> n
                                                                .field(fieldPath)
                                                                .gte(((Number) innerValues.get(0)).doubleValue())
                                                                .lte(((Number) innerValues.get(1)).doubleValue()))));
                                    }
                                }
                                case NOT_EQUALS -> {
                                    if (innerValue instanceof String) {
                                        boolBuilder
                                                .must(m -> m
                                                        .exists(e -> e
                                                                .field(fieldPath)))
                                                .mustNot(m -> m
                                                .term(t -> t
                                                        .field(fieldPath)
                                                        .value(innerValue.toString())));
                                    } else if (innerValue instanceof Integer) {
                                        boolBuilder
                                                .must(m -> m
                                                        .exists(e -> e
                                                                .field(fieldPath)))
                                                .mustNot(m -> m
                                                .term(t -> t
                                                        .field(fieldPath)
                                                        .value((Integer) innerValue)));
                                    }
                                }
                                case GREATER_THAN_OR_EQUALS -> {
                                    if (innerValue instanceof Number) {
                                        boolBuilder.filter(f -> f
                                                .range(r -> r
                                                        .number(n -> n
                                                                .field(fieldPath)
                                                                .gte(((Number) innerValue).doubleValue()))));
                                    }
                                }
                                case LESSER_THAN_OR_EQUALS -> {
                                    if (innerValue instanceof Number) {
                                        boolBuilder.filter(f -> f
                                                .range(r -> r
                                                        .number(n -> n
                                                                .field(fieldPath)
                                                                .lte(((Number) innerValue).doubleValue()))));
                                    }
                                }
                                default -> throw new IllegalArgumentException("well");
                            }
                        } else {
                            log.warn("unable to determine value type for key {}", prop.getKey());
                        }

                    }
                }
            }
        }

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(Query.of(q -> q.bool(boolBuilder.build())))
                .build();

        SearchHits<Article> searchHits = ops.search(nativeQuery, Article.class);
        return searchHits.stream()
                .map(SearchHit::getContent)
                .toList();
    }
}
