package com.enttrac.backend.repository;

import com.enttrac.backend.model.item.BookItem;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class BookRepository implements MediaRepository<BookItem> {

    private static final String TABLE_NAME = "EntTrac";

    private final DynamoDbTable<BookItem> table;

    public BookRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(BookItem.class));
    }

    @Override
    public void save(BookItem item) {
        table.putItem(item);
    }

    @Override
    public BookItem findById(String userId, String bookId) {
        Key key = Key.builder()
                .partitionValue(userId)
                .sortValue("BOOK#OPENLIBRARY#" + bookId)
                .build();
        return table.getItem(key);
    }

    @Override
    public List<BookItem> findAll(String userId) {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder()
                        .partitionValue(userId)
                        .build());

        return table.query(queryConditional)
                .items()
                .stream()
                .filter(item -> item.getSk().startsWith("BOOK#"))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String userId, String bookId) {
        Key key = Key.builder()
                .partitionValue(userId)
                .sortValue("BOOK#OPENLIBRARY#" + bookId)
                .build();
        table.deleteItem(key);
    }
}
