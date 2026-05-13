package xyz.peasfultown.test.elasticsearch_database_simple_test;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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

    @Field(type = FieldType.Date)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
