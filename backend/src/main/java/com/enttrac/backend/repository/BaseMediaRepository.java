package com.enttrac.backend.repository;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseMediaRepository<T> {

    private final DynamoDbTable<T> table;
    private final String typePrefix;

    protected BaseMediaRepository(DynamoDbTable<T> table, String typePrefix) {
        this.table = table;
        this.typePrefix = typePrefix;
    }

    /** Builds the exact sort key for one item, e.g. "BOOK#OPENLIBRARY#123". */
    protected abstract String buildSortKey(String id);

    public void save(T item) {
        table.putItem(item);
    }

    public T findById(String userId, String id) {
        Key key = Key.builder()
                .partitionValue(userId)
                .sortValue(buildSortKey(id))
                .build();
        return table.getItem(key);
    }

    public List<T> findAll(String userId) {
        QueryConditional queryConditional = QueryConditional.sortBeginsWith(
                Key.builder()
                        .partitionValue(userId)
                        .sortValue(typePrefix)
                        .build());
        return table.query(queryConditional)
                .items()
                .stream()
                .collect(Collectors.toList());
    }

    public void delete(String userId, String id) {
        Key key = Key.builder()
                .partitionValue(userId)
                .sortValue(buildSortKey(id))
                .build();
        table.deleteItem(key);
    }
}
