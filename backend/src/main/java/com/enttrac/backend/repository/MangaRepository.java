package com.enttrac.backend.repository;

import com.enttrac.backend.model.item.MangaItem;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class MangaRepository extends BaseMediaRepository<MangaItem> {

    private static final String TABLE_NAME = "EntTrac";

    public MangaRepository(DynamoDbEnhancedClient enhancedClient) {
        super(enhancedClient.table(TABLE_NAME, TableSchema.fromBean(MangaItem.class)), "MANGA#");
    }

    @Override
    protected String buildSortKey(String mangaId) {
        return "MANGA#MANGADEX#" + mangaId;
    }
}
