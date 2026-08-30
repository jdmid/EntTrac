package com.enttrac.backend.repository;

import com.enttrac.backend.model.item.MangaItem;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class MangaRepository extends BaseMediaRepository<MangaItem> {

    public MangaRepository(DynamoDbEnhancedClient enhancedClient,
                           @Value("${dynamodb.table-name:EntTrac}") String tableName) {
        super(enhancedClient.table(tableName, TableSchema.fromBean(MangaItem.class)), "MANGA#");
    }

    @Override
    protected String buildSortKey(String mangaId) {
        return "MANGA#MANGADEX#" + mangaId;
    }
}
