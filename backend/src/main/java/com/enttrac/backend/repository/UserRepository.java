package com.enttrac.backend.repository;

import com.enttrac.backend.model.item.RefreshTokenItem;
import com.enttrac.backend.model.item.UserProfileItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;

@Repository
public class UserRepository {

    private final DynamoDbTable<UserProfileItem> profileTable;
    private final DynamoDbTable<RefreshTokenItem> refreshTable;

    public UserRepository(DynamoDbEnhancedClient enhancedClient,
                          @Value("${dynamodb.table-name:EntTrac}") String tableName) {
        this.profileTable = enhancedClient.table(tableName, TableSchema.fromBean(UserProfileItem.class));
        this.refreshTable = enhancedClient.table(tableName, TableSchema.fromBean(RefreshTokenItem.class));
    }

    public static String pkFor(String provider, String providerId) {
        return "USER#" + provider + "#" + providerId;
    }

    public Optional<UserProfileItem> findProfile(String userId) {
        Key key = Key.builder().partitionValue(userId).sortValue("PROFILE").build();
        return Optional.ofNullable(profileTable.getItem(key));
    }

    public void saveProfile(UserProfileItem profile) {
        profileTable.putItem(profile);
    }

    public void saveRefreshToken(RefreshTokenItem token) {
        refreshTable.putItem(token);
    }

    public Optional<RefreshTokenItem> findRefreshToken(String userId, String tokenId) {
        Key key = Key.builder().partitionValue(userId).sortValue("REFRESH#" + tokenId).build();
        return Optional.ofNullable(refreshTable.getItem(key));
    }

    public void deleteRefreshToken(String userId, String tokenId) {
        Key key = Key.builder().partitionValue(userId).sortValue("REFRESH#" + tokenId).build();
        refreshTable.deleteItem(key);
    }
}
