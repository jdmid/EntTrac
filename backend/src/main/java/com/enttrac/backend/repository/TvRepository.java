package com.enttrac.backend.repository;

import com.enttrac.backend.model.item.TvItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class TvRepository extends BaseMediaRepository<TvItem> {

    public TvRepository(DynamoDbEnhancedClient enhancedClient,
                        @Value("${dynamodb.table-name:EntTrac}") String tableName) {
        super(enhancedClient.table(tableName, TableSchema.fromBean(TvItem.class)), "TV#");
    }

    @Override
    protected String buildSortKey(String tvId) {
        return "TV#TMDB#" + tvId;
    }
}