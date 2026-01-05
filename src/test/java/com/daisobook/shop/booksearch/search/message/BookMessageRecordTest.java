package com.daisobook.shop.booksearch.search.message;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookMessageRecordTest {

    @Test
    @DisplayName("BookDeleteMessage 모든 필드 보존 테스트")
    void bookDeleteMessage_shouldKeepAllFields() {
        BookDeleteMessage msg = new BookDeleteMessage("rid", "isbn", 1L, "reason");

        assertThat(msg.requestId()).as("requestId 저장").isEqualTo("rid");
        assertThat(msg.isbn()).as("isbn 저장").isEqualTo("isbn");
        assertThat(msg.ts()).as("ts 저장").isEqualTo(1L);
        assertThat(msg.reason()).as("reason 저장").isEqualTo("reason");
    }

    @Test
    @DisplayName("BookUpsertMessage 모든 필드 보존 테스트")
    void bookUpsertMessage_shouldKeepAllFields() {
        BookUpsertMessage.BookPayload payload = new BookUpsertMessage.BookPayload(
                "isbn",
                2L,
                "title",
                "author",
                "publisher",
                "desc",
                LocalDate.of(2025, 12, 28),
                100,
                List.of("c1", "c2"),
                "img",
                3L,
                4L
        );

        BookUpsertMessage msg = new BookUpsertMessage("rid", payload, 5L, "reason");

        assertThat(msg.requestId()).as("requestId 저장").isEqualTo("rid");
        assertThat(msg.book()).as("payload 저장").isSameAs(payload);
        assertThat(msg.ts()).as("ts 저장").isEqualTo(5L);
        assertThat(msg.reason()).as("reason 저장").isEqualTo("reason");

        assertThat(msg.book().categories()).as("payload 내부 필드").containsExactly("c1", "c2");
        assertThat(msg.book().pubDate()).as("payload 내부 LocalDate").isEqualTo(LocalDate.of(2025, 12, 28));
    }
}
