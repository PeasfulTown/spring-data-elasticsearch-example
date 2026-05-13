# Getting started

Clone repo and go into project directory:

```
git clone git@github.com:PeasfulTown/spring-data-elasticsearch-search-example.git
cd spring-data-elasticsearch-search-example
```

Grant execution permission to `mvnw` and compile spring project into jar:

```sh
chmod +x ./mvnw
./mvnw package -DskipTests
```

Build and run docker containers:

```
docker compose up --build -d
```

Kibana is commented out in `docker-compose.yaml` since it's not needed for this
program to work but you can uncomment it before running the above command if you
need Kibana for some further testing if need be.

Some dummy data is already inserted into elasticsearch so you can start
searching immediately:

```
curl -X POST http://localhost:8080/search \
    -H "Content-Type: application/json" \
    -d '{ "description": "security", "authors": [ "kim" ], "tags": [ "web", "backend" ], "timeFilter": "PAST_YEAR" }'
```

This returns the JSON:

```json
[
    {
        "id": "ccff7b05-48ab-4a63-aa34-89ed95aa7ec7",
        "title": "Securing Your Web Applications",
        "description": "Essential security practices to protect your apps against OWASP Top 10 vulnerabilities",
        "authors": [
            {
                "name": "David Kim"
            }
        ],
        "tags": [
            {
                "name": "Web Development"
            },
            {
                "name": "Backend"
            }
        ],
        "createdAt": "2026-04-05T07:20:00Z"
    }
]
```

The [ArticleSearchRequest](./src/main/java/xyz/peasfultown/test/elasticsearch_database_simple_test/ArticleSearchRequest.java)
describes the request body.

Fuzzy search is enabled for the author name, so you can query an author name
like:

```json
{
    "authors": [
        "kjm"
    ]
}
```

And it will return results for author "David Kim".

Fuzzy search is not enabled for the tags, so you can test to see the difference
between fuzzy and non-fuzzy search.
