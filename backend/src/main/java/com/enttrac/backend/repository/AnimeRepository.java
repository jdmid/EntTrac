package com.enttrac.backend.repository;

import com.enttrac.backend.model.AnimeItem;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AnimeRepository {

    private static final String TABLE_NAME = "EntTrac";
    private static final String USER_PK = "USER#default";

    private final DynamoDbTable<AnimeItem> table;

    public AnimeRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(AnimeItem.class));
    }

    public void save(AnimeItem item) {
        table.putItem(item);
    }

    public AnimeItem findById(String animeId) {
        Key key = Key.builder()
                .partitionValue(USER_PK)
                .sortValue("ANIME#JIKAN#" + animeId)
                .build();
        return table.getItem(key);
    }

    public List<AnimeItem> findAll() {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder()
                        .partitionValue(USER_PK)
                        .build());

        return table.query(queryConditional)
                .items()
                .stream()
                .filter(item -> item.getSk().startsWith("ANIME#"))
                .collect(Collectors.toList());
    }

    public void delete(String animeId) {
        Key key = Key.builder()
                .partitionValue(USER_PK)
                .sortValue("ANIME#JIKAN#" + animeId)
                .build();
        table.deleteItem(key);
    }
}