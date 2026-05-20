package com.enttrac.backend.repository;

import com.enttrac.backend.model.item.MovieItem;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MovieRepository {

    private static final String TABLE_NAME = "EntTrac";
    private static final String USER_PK = "USER#default";

    private final DynamoDbTable<MovieItem> table;

    public MovieRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(MovieItem.class));
    }

    public void save(MovieItem item) {
        table.putItem(item);
    }

    public MovieItem findById(String movieId) {
        Key key = Key.builder()
                .partitionValue(USER_PK)
                .sortValue("MOVIE#TMDB#" + movieId)
                .build();
        return table.getItem(key);
    }

    public List<MovieItem> findAll() {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder()
                        .partitionValue(USER_PK)
                        .build());

        return table.query(queryConditional)
                .items()
                .stream()
                .filter(item -> item.getSk().startsWith("MOVIE#"))
                .collect(Collectors.toList());
    }

    public void delete(String movieId) {
        Key key = Key.builder()
                .partitionValue(USER_PK)
                .sortValue("MOVIE#TMDB#" + movieId)
                .build();
        table.deleteItem(key);
    }
}
