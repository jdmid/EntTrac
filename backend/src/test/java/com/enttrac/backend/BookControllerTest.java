package com.enttrac.backend;

import com.enttrac.backend.auth.AuthFilter;
import com.enttrac.backend.auth.CurrentUserIdArgumentResolver;
import com.enttrac.backend.auth.JwtService;
import com.enttrac.backend.config.WebMvcConfig;
import com.enttrac.backend.controller.BookController;
import com.enttrac.backend.model.item.BookItem;
import com.enttrac.backend.model.result.BookSearchResult;
import com.enttrac.backend.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static com.enttrac.backend.auth.AuthTestSupport.TEST_USER_ID;
import static com.enttrac.backend.auth.AuthTestSupport.accessTokenCookie;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@Import({AuthFilter.class, CurrentUserIdArgumentResolver.class, WebMvcConfig.class, JwtService.class})
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void search_ShouldReturnResults() throws Exception {
        BookSearchResult result = BookSearchResult.builder()
                .id("OL27448W")
                .title("The Lord of the Rings")
                .build();

        when(bookService.search("tolkien")).thenReturn(List.of(result));

        mockMvc.perform(get("/api/books/search")
                        .cookie(accessTokenCookie(jwtService))
                        .param("q", "tolkien"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("The Lord of the Rings"));
    }

    @Test
    void getLibrary_ShouldReturnItems() throws Exception {
        BookItem item = new BookItem();
        item.setBookId("OL27448W");
        item.setTitle("The Lord of the Rings");

        when(bookService.getLibrary(TEST_USER_ID)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/books/library")
                        .cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value("OL27448W"));
    }

    @Test
    void getBook_ShouldReturnItemWhenFound() throws Exception {
        BookItem item = new BookItem();
        item.setBookId("OL27448W");
        item.setTitle("The Lord of the Rings");

        when(bookService.getBook(TEST_USER_ID,"OL27448W")).thenReturn(item);

        mockMvc.perform(get("/api/books/library/OL27448W")
                        .cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value("OL27448W"));
    }

    @Test
    void getBook_ShouldReturn404WhenNotFound() throws Exception {
        when(bookService.getBook(TEST_USER_ID,"notreal")).thenReturn(null);

        mockMvc.perform(get("/api/books/library/notreal").cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addToLibrary_ShouldReturnSavedItem() throws Exception {
        BookItem item = new BookItem();
        item.setBookId("OL27448W");
        item.setTitle("The Lord of the Rings");
        item.setStatus("PLANNED");

        when(bookService.addToLibrary(eq(TEST_USER_ID) ,any())).thenReturn(item);

        mockMvc.perform(post("/api/books/library")
                        .cookie(accessTokenCookie(jwtService))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value("OL27448W"));
    }

    @Test
    void updateProgress_ShouldReturnUpdatedItem() throws Exception {
        BookItem item = new BookItem();
        item.setBookId("OL27448W");
        item.setCurrentChapter(10);
        item.setCurrentPage(250);

        when(bookService.updateProgress(TEST_USER_ID,"OL27448W", 10, 250)).thenReturn(item);

        mockMvc.perform(patch("/api/books/library/OL27448W/progress")
                        .cookie(accessTokenCookie(jwtService))
                        .param("currentChapter", "10")
                        .param("currentPage", "250"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentChapter").value(10))
                .andExpect(jsonPath("$.currentPage").value(250));
    }

    @Test
    void updateProgress_ShouldAcceptChapterOnly() throws Exception {
        BookItem item = new BookItem();
        item.setBookId("OL27448W");
        item.setCurrentChapter(10);

        when(bookService.updateProgress(TEST_USER_ID,"OL27448W", 10, null)).thenReturn(item);

        mockMvc.perform(patch("/api/books/library/OL27448W/progress")
                        .cookie(accessTokenCookie(jwtService))
                        .param("currentChapter", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentChapter").value(10));
    }

    @Test
    void updateProgress_ShouldAcceptPageOnly() throws Exception {
        BookItem item = new BookItem();
        item.setBookId("OL27448W");
        item.setCurrentPage(250);

        when(bookService.updateProgress(TEST_USER_ID,"OL27448W", null, 250)).thenReturn(item);

        mockMvc.perform(patch("/api/books/library/OL27448W/progress")
                        .cookie(accessTokenCookie(jwtService))
                        .param("currentPage", "250"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(250));
    }

    @Test
    void updateScore_ShouldReturnUpdatedItem() throws Exception {
        BookItem item = new BookItem();
        item.setBookId("OL27448W");
        item.setScore(9);

        when(bookService.updateScore(TEST_USER_ID,"OL27448W", 9)).thenReturn(item);

        mockMvc.perform(patch("/api/books/library/OL27448W/score")
                        .cookie(accessTokenCookie(jwtService))
                        .param("score", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(9));
    }

    @Test
    void updateScore_ShouldReturn400WhenOutOfRange() throws Exception {
        mockMvc.perform(patch("/api/books/library/OL27448W/score")
                        .cookie(accessTokenCookie(jwtService))
                        .param("score", "11"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_ShouldReturnUpdatedItem() throws Exception {
        BookItem item = new BookItem();
        item.setBookId("OL27448W");
        item.setStatus("FINISHED");

        when(bookService.updateStatus(TEST_USER_ID,"OL27448W", "FINISHED")).thenReturn(item);

        mockMvc.perform(patch("/api/books/library/OL27448W/status")
                        .cookie(accessTokenCookie(jwtService))
                        .param("status", "FINISHED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED"));
    }

    @Test
    void updateStatus_ShouldReturn400WhenInvalid() throws Exception {
        mockMvc.perform(patch("/api/books/library/OL27448W/status")
                        .cookie(accessTokenCookie(jwtService))
                        .param("status", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateNotes_ShouldReturnUpdatedItem() throws Exception {
        BookItem item = new BookItem();
        item.setBookId("OL27448W");

        when(bookService.updateNotes(TEST_USER_ID,"OL27448W", "Great book")).thenReturn(item);

        mockMvc.perform(patch("/api/books/library/OL27448W/notes")
                        .cookie(accessTokenCookie(jwtService))
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Great book"))
                .andExpect(status().isOk());
    }

    @Test
    void updateNotes_ShouldHandleNullBody() throws Exception {
        BookItem item = new BookItem();
        item.setBookId("OL27448W");

        when(bookService.updateNotes(TEST_USER_ID,"OL27448W", "")).thenReturn(item);

        mockMvc.perform(patch("/api/books/library/OL27448W/notes")
                        .cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isOk());
    }

    @Test
    void getDetails_ShouldReturnResultWhenFound() throws Exception {
        BookSearchResult result = BookSearchResult.builder()
                .id("OL27448W")
                .title("The Lord of the Rings")
                .description("A fantasy epic.")
                .build();

        when(bookService.getDetails("OL27448W")).thenReturn(result);

        mockMvc.perform(get("/api/books/details/OL27448W")
                        .cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("The Lord of the Rings"))
                .andExpect(jsonPath("$.description").value("A fantasy epic."));
    }

    @Test
    void getDetails_ShouldReturn404WhenNotFound() throws Exception {
        when(bookService.getDetails("notreal")).thenReturn(null);

        mockMvc.perform(get("/api/books/details/notreal")
                        .cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeFromLibrary_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/books/library/OL27448W")
                        .cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).removeFromLibrary(TEST_USER_ID,"OL27448W");
    }

    @Test
    void getWorksByAuthor_ShouldReturnResults() throws Exception {
        BookSearchResult result = BookSearchResult.builder()
                .id("OL45804W")
                .title("The Hobbit")
                .build();

        when(bookService.getWorksByAuthor("OL26320A")).thenReturn(List.of(result));

        mockMvc.perform(get("/api/books/creator/OL26320A")
                        .cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("The Hobbit"));
    }

    @Test
    void searchAuthors_ShouldReturnResults() throws Exception {
        when(bookService.searchAuthors("tolkien"))
                .thenReturn(List.of(Map.of("id", "OL26320A", "name", "J.R.R. Tolkien")));

        mockMvc.perform(get("/api/books/author-search").param("name", "tolkien")
                        .cookie(accessTokenCookie(jwtService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("J.R.R. Tolkien"));
    }
}