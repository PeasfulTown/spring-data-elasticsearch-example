package xyz.peasfultown.test.elasticsearch_database_simple_test;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
    @Field(type = FieldType.Keyword)
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @Field(type = FieldType.Text)
    private String body;

    @Field(type = FieldType.Keyword)
    private String authorId;
}
