package xyz.peasfultown.test.elasticsearch_database_simple_test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    @Field(type = FieldType.Text)
    private String name;
}
