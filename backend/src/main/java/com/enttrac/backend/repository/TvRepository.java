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
public class TvRepository implements MediaRepository<TvItem> {

    private static final String TABLE_NAME = "EntTrac";

    private final DynamoDbTable<TvItem> table;

    public TvRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(TvItem.class));
    }

    public void save(TvItem item) {
        table.putItem(item);
    }

    public TvItem findById(String userId, String tvId) {
        Key key = Key.builder()
                .partitionValue(userId)
                .sortValue("TV#TMDB#" + tvId)
                .build();
        return table.getItem(key);
    }

    public List<TvItem> findAll(String userId) {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder()
                        .partitionValue(userId)
                        .build());

        return table.query(queryConditional)
                .items()
                .stream()
                .filter(item -> item.getSk().startsWith("TV#"))
                .collect(Collectors.toList());
    }

    public void delete(String userId, String tvId) {
        Key key = Key.builder()
                .partitionValue(userId)
                .sortValue("TV#TMDB#" + tvId)
                .build();
        table.deleteItem(key);
    }
}