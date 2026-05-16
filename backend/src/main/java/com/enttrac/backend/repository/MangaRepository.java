package com.enttrac.backend.repository;

import com.enttrac.backend.model.MangaItem;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MangaRepository {

    private static final String TABLE_NAME = "EntTrac";
    private static final String USER_PK = "USER#default";

    private final DynamoDbTable<MangaItem> table;

    public MangaRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(MangaItem.class));
    }

    public void save(MangaItem item) {
        table.putItem(item);
    }

    public MangaItem findById(String mangaId) {
        Key key = Key.builder()
                .partitionValue(USER_PK)
                .sortValue("MANGA#MANGADEX#" + mangaId)
                .build();
        return table.getItem(key);
    }

    public List<MangaItem> findAll() {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder()
                        .partitionValue(USER_PK)
                        .build());
        return table.query(queryConditional)
                .items()
                .stream()
                .collect(Collectors.toList());
    }

    public void delete(String mangaId) {
        Key key = Key.builder()
                .partitionValue(USER_PK)
                .sortValue("MANGA#MANGADEX#" + mangaId)
                .build();
        table.deleteItem(key);
    }
}
