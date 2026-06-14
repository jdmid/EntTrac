package com.enttrac.backend.repository;

import com.enttrac.backend.model.item.GameItem;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class GameRepository implements MediaRepository<GameItem> {

    private static final String TABLE_NAME = "EntTrac";
    private static final String USER_PK = "USER#default";

    private final DynamoDbTable<GameItem> table;

    public GameRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(GameItem.class));
    }

    @Override
    public void save(GameItem item) {
        table.putItem(item);
    }

    @Override
    public GameItem findById(String gameId) {
        Key key = Key.builder()
                .partitionValue(USER_PK)
                .sortValue("GAME#IGDB#" + gameId)
                .build();
        return table.getItem(key);
    }

    @Override
    public List<GameItem> findAll() {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder()
                        .partitionValue(USER_PK)
                        .build());

        return table.query(queryConditional)
                .items()
                .stream()
                .filter(item -> item.getSk().startsWith("GAME#"))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String gameId) {
        Key key = Key.builder()
                .partitionValue(USER_PK)
                .sortValue("GAME#IGDB#" + gameId)
                .build();
        table.deleteItem(key);
    }
}
