package com.enttrac.backend.repository;

import com.enttrac.backend.model.item.TvItem;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TvRepository {

    private static final String TABLE_NAME = "EntTrac";
    private static final String USER_PK = "USER#default";

    private final DynamoDbTable<TvItem> table;

    public TvRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(TvItem.class));
    }

    public void save(TvItem item) {
        table.putItem(item);
    }

    public TvItem findById(String tvId) {
        Key key = Key.builder()
                .partitionValue(USER_PK)
                .sortValue("TV#TMDB#" + tvId)
                .build();
        return table.getItem(key);
    }

    public List<TvItem> findAll() {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder()
                        .partitionValue(USER_PK)
                        .build());

        return table.query(queryConditional)
                .items()
                .stream()
                .filter(item -> item.getSk().startsWith("TV#"))
                .collect(Collectors.toList());
    }

    public void delete(String tvId) {
        Key key = Key.builder()
                .partitionValue(USER_PK)
                .sortValue("TV#TMDB#" + tvId)
                .build();
        table.deleteItem(key);
    }
}