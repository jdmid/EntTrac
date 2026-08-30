package com.enttrac.backend.repository;

import com.enttrac.backend.model.item.GameItem;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class GameRepository extends BaseMediaRepository<GameItem> {

    public GameRepository(DynamoDbEnhancedClient enhancedClient,
                          @Value("${dynamodb.table-name:EntTrac}") String tableName) {
        super(enhancedClient.table(tableName, TableSchema.fromBean(GameItem.class)), "GAME#");
    }

    @Override
    protected String buildSortKey(String gameId) {
        return "GAME#IGDB#" + gameId;
    }
}