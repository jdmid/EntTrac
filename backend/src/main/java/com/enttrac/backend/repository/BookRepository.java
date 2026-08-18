package com.enttrac.backend.repository;

import com.enttrac.backend.model.item.BookItem;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class BookRepository extends BaseMediaRepository<BookItem> {

    private static final String TABLE_NAME = "EntTrac";

    public BookRepository(DynamoDbEnhancedClient enhancedClient) {
        super(enhancedClient.table(TABLE_NAME, TableSchema.fromBean(BookItem.class)), "BOOK#");
    }

    @Override
    protected String buildSortKey(String bookId) {
        return "BOOK#OPENLIBRARY#" + bookId;
    }
}
