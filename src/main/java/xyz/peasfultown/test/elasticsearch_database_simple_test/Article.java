package xyz.peasfultown.test.elasticsearch_database_simple_test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.*;

@Document(indexName = "blog", createIndex = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Article {
    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Nested)
    private List<Author> authors;

    @Field(type = FieldType.Nested)
    private List<Tag> tags;

    @Field(type = FieldType.Nested)
    private List<Comment> comments;

    @Field(type = FieldType.Date)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Map<String, Object> additionalProperties = new HashMap<>();
}
