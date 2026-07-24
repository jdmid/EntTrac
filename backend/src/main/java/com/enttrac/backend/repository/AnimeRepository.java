package com.enttrac.backend.repository;

import com.enttrac.backend.model.item.AnimeItem;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class AnimeRepository extends BaseMediaRepository<AnimeItem> {

    private static final String TABLE_NAME = "EntTrac";

    public AnimeRepository(DynamoDbEnhancedClient enhancedClient) {
        super(enhancedClient.table(TABLE_NAME, TableSchema.fromBean(AnimeItem.class)), "ANIME#");
    }

    @Override
    protected String buildSortKey(String animeId) {
        return "ANIME#ANILIST#" + animeId;
    }
}