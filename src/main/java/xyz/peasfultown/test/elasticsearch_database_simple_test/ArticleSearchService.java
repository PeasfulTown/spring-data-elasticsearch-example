package xyz.peasfultown.test.elasticsearch_database_simple_test;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

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

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(Query.of(q -> q.bool(boolBuilder.build())))
                .build();

        SearchHits<Article> searchHits = ops.search(nativeQuery, Article.class);
        return searchHits.stream()
                .map(SearchHit::getContent)
                .toList();
    }
}
