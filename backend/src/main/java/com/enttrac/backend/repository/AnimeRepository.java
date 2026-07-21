package com.enttrac.backend.repository;

import com.enttrac.backend.model.item.AnimeItem;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AnimeRepository implements MediaRepository<AnimeItem> {

    private static final String TABLE_NAME = "EntTrac";

    private final DynamoDbTable<AnimeItem> table;

    public AnimeRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(AnimeItem.class));
    }

    public void save(AnimeItem item) {
        table.putItem(item);
    }

    public AnimeItem findById(String userId, String animeId) {
        Key key = Key.builder()
                .partitionValue(userId)
                .sortValue("ANIME#ANILIST#" + animeId)
                .build();
        return table.getItem(key);
    }

    public List<AnimeItem> findAll(String userId) {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder()
                        .partitionValue(userId)
                        .build());

        return table.query(queryConditional)
                .items()
                .stream()
                .filter(item -> item.getSk().startsWith("ANIME#"))
                .collect(Collectors.toList());
    }

    public void delete(String userId, String animeId) {
        Key key = Key.builder()
                .partitionValue(userId)
                .sortValue("ANIME#ANILIST#" + animeId)
                .build();
        table.deleteItem(key);
    }
}