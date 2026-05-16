package xyz.peasfultown.test.elasticsearch_database_simple_test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.ScriptType;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Arrays.asList;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class ElasticsearchDatabaseSimpleTestApplication {
    private final ElasticsearchOperations ops;
    private final ArticleRepository repo;

    public static void main(String[] args) {
        SpringApplication.run(ElasticsearchDatabaseSimpleTestApplication.class, args);
    }

    @Bean
    public CommandLineRunner initialize() {
        return args -> {
            // wipe old index
            ops.indexOps(Article.class).delete();
            // create the empty index shell
            ops.indexOps(Article.class).create();
            // extract and register @Field mappings (nested, keyword, etc.)
            ops.indexOps(Article.class).putMapping();

            List<Article> articles = asList(
                    Article.builder()
                            .title("Mastering Async/Await in JavaScript")
                            .description("Learn how to write cleaner asynchronous code using modern JavaScript syntax and error handling patterns")
                            .authors(asList(
                                    new Author("Alex Mercer"),
                                    new Author("Elena Rostova")
                            ))
                            .tags(asList(
                                    new Tag("JavaScript"),
                                    new Tag("Web Development"),
                                    new Tag("Programming")
                            ))
                            .createdAt(Instant.parse("2026-01-15T09:30:00Z"))
                            .additionalProperties(Map.of(
                                    "followers", 1200,
                                    "wordCount", 3000,
                                    "publisher", "Google"

                            ))
                            .build(),
                    Article.builder()
                            .title("A Beginner's Guide to Containerization")
                            .description("An introduction to Docker concepts, images, containers, and how to simplify deployment workflows")
                            .authors(asList(
                                    new Author("Marcus Vance")
                            ))
                            .tags(asList(
                                    new Tag("DevOps"),
                                    new Tag("Docker"),
                                    new Tag("Cloud")
                            ))
                            .createdAt(Instant.parse("2026-02-02T14:15:22Z"))
                            .build(),
                    Article.builder()
                            .title("Designing Accessible User Interfaces")
                            .description("Practical tips and WCAG guidelines to ensure your web applications are usable for everyone")
                            .authors(asList(
                                    new Author("Sarah Jenkins"),
                                    new Author("Chloe Tan")
                            ))
                            .tags(asList(
                                    new Tag("UI/UX"),
                                    new Tag("Accessibility"),
                                    new Tag("Design")
                            ))
                            .additionalProperties(Map.of(
                                    "subscribers", 1000,
                                    "publisher", "Baeldung",
                                    "wordCount", 9000

                            ))
                            .createdAt(Instant.parse("2026-02-18T11:05:00Z"))
                            .build(),
                    Article.builder()
                            .title("Understanding Database Indexes")
                            .description("Deep dive into B-Trees and execution plans to optimize slow database queries and improve speed")
                            .authors(asList(
                                    new Author("David Kim")
                            ))
                            .tags(asList(
                                    new Tag("Databases"),
                                    new Tag("SQL"),
                                    new Tag("Backend")
                            ))
                            .createdAt(Instant.parse("2026-04-30T08:00:00Z"))
                            .build(),
                    Article.builder()
                            .title("Understanding Database Indexes")
                            .description("Deep dive into B-Trees and execution plans to optimize slow database queries and improve speed")
                            .authors(asList(
                                    new Author("David Kim")
                            ))
                            .tags(asList(
                                    new Tag("Databases"),
                                    new Tag("SQL"),
                                    new Tag("Backend")
                            ))
                            .createdAt(Instant.parse("2026-05-01T08:00:00Z"))
                            .build(),
                    Article.builder()
                            .title("Remote Work Productivity Hacks")
                            .description("Discover the best tools, routines, and boundaries to maintain high performance while working from home")
                            .authors(asList(
                                    new Author("Emily Watson"),
                                    new Author("Liam O'Connor")
                            ))
                            .tags(asList(
                                    new Tag("Productivity"),
                                    new Tag("Remote Work"),
                                    new Tag("Lifestyle")
                            ))
                            .additionalProperties(Map.of(
                                    "publisher", "AWS",
                                    "followers", 1300
                            ))
                            .createdAt(Instant.parse("2026-03-29T08:00:00Z"))
                            .build(),
                    Article.builder()
                            .title("Securing Your Web Applications")
                            .description("Essential security practices to protect your apps against OWASP Top 10 vulnerabilities")
                            .authors(asList(
                                    new Author("David Kim")
                            ))
                            .tags(asList(
                                    new Tag("Web Development"),
                                    new Tag("Backend")
                            ))
                            .createdAt(Instant.parse("2026-04-05T07:20:00Z"))
                            .build(),
                    Article.builder()
                            .title("The Rise of Serverless Architecture")
                            .description("Exploring the pros and cons of cloud functions and pay-per-use computing models")
                            .authors(asList(
                                    new Author("Sophia Martinez")
                            ))
                            .tags(asList(
                                    new Tag("Cloud"),
                                    new Tag("Serverless")
                            ))
                            .additionalProperties(Map.of(
                                    "publisher", "AWS",
                                    "followers", 4000
                            ))
                            .createdAt(Instant.parse("2026-03-01T08:00:00Z"))
                            .build()
                    );

            repo.saveAll(articles);
            log.info("Saved articles");

            // add comments

            String commenter1 = UUID.randomUUID().toString();
            String commenter2 = UUID.randomUUID().toString();
            String commenter3 = UUID.randomUUID().toString();

            addComment(articles.get(0).getId().toString(),
                    Comment.builder()
                            .body("Test comment!")
                            .authorId(commenter1)
                    .build());
            addComment(articles.get(0).getId().toString(),
                    Comment.builder()
                            .body("Another test comment!")
                            .authorId(commenter2)
                            .build());
            addComment(articles.get(1).getId().toString(),
                    Comment.builder()
                            .body("The best test comment!")
                            .authorId(commenter2)
                            .build());
            addComment(articles.get(1).getId().toString(),
                    Comment.builder()
                            .body("The worst comment!")
                            .authorId(commenter3)
                            .build());
        };
    }

    private void addComment(String articleId, Comment comment) {
        String script = "if (ctx._source.comments == null) { ctx._source.comments = [] }" +
                "ctx._source.comments.add(params.comment)";
        Map<String, Object> params = Map.of("comment", comment);
        UpdateQuery updateQuery = UpdateQuery.builder(articleId)
                .withScriptType(ScriptType.INLINE)
                .withScript(script)
                .withParams(params)
                .build();

        ops.update(updateQuery, IndexCoordinates.of("blog"));
    }
}
