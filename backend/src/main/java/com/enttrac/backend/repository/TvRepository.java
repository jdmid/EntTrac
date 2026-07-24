package com.enttrac.backend.repository;

import com.enttrac.backend.model.item.TvItem;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class TvRepository extends BaseMediaRepository<TvItem> {

    private static final String TABLE_NAME = "EntTrac";

    public TvRepository(DynamoDbEnhancedClient enhancedClient) {
        super(enhancedClient.table(TABLE_NAME, TableSchema.fromBean(TvItem.class)), "TV#");
    }

    @Override
    protected String buildSortKey(String tvId) {
        return "TV#TMDB#" + tvId;
    }
}