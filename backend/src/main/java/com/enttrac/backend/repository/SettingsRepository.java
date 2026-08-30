package com.enttrac.backend.repository;

import com.enttrac.backend.model.item.SettingsItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;

@Repository
public class SettingsRepository {

    private final DynamoDbTable<SettingsItem> table;

    public SettingsRepository(DynamoDbEnhancedClient enhancedClient,
                              @Value("${dynamodb.table-name:EntTrac}") String tableName) {
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(SettingsItem.class));
    }

    public Optional<SettingsItem> find(String userId) {
        Key key = Key.builder().partitionValue(userId).sortValue("SETTINGS").build();
        return Optional.ofNullable(table.getItem(key));
    }

    public void save(SettingsItem item) {
        table.putItem(item);
    }
}
