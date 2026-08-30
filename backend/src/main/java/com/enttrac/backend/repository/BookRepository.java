package com.enttrac.backend.repository;

import com.enttrac.backend.model.item.BookItem;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class BookRepository extends BaseMediaRepository<BookItem> {

    public BookRepository(DynamoDbEnhancedClient enhancedClient,
                          @Value("${dynamodb.table-name:EntTrac}") String tableName) {
        super(enhancedClient.table(tableName, TableSchema.fromBean(BookItem.class)), "BOOK#");
    }

    @Override
    protected String buildSortKey(String bookId) {
        return "BOOK#OPENLIBRARY#" + bookId;
    }
}
