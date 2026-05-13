package xyz.peasfultown.test.elasticsearch_database_simple_test;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
public class ArticleSearchRequest {
    private String textQuery;
    private List<String> authors;
    private List<String> tags;
    private TimeFilterEnum timeFilter;
    private Map<String, Object> additionalProperties;
}
