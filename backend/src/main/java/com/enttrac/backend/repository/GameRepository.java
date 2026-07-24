package com.enttrac.backend.repository;

import com.enttrac.backend.model.item.GameItem;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class GameRepository extends BaseMediaRepository<GameItem> {

    private static final String TABLE_NAME = "EntTrac";

    public GameRepository(DynamoDbEnhancedClient enhancedClient) {
        super(enhancedClient.table(TABLE_NAME, TableSchema.fromBean(GameItem.class)), "GAME#");
    }

    @Override
    protected String buildSortKey(String gameId) {
        return "GAME#IGDB#" + gameId;
    }
}