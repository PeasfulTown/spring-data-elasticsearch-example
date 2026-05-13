package xyz.peasfultown.test.elasticsearch_database_simple_test;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleSearchService service;

    @PostMapping("/search")
    public List<Article> search(@RequestBody ArticleSearchRequest req) {
        return service.searchArticles(req);
    }
}
