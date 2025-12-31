package com.daisobook.shop.booksearch.BooksSearch.search.component;

import com.daisobook.shop.booksearch.BooksSearch.entity.ImageType;
import com.daisobook.shop.booksearch.BooksSearch.entity.author.Author;
import com.daisobook.shop.booksearch.BooksSearch.entity.author.BookAuthor;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.BookImage;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Status;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.BookCategory;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.Category;
import com.daisobook.shop.booksearch.BooksSearch.entity.publisher.Publisher;
import com.daisobook.shop.booksearch.BooksSearch.search.domain.RabbitBook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class BookSearchPayloadMapperTest {

    private final BookSearchPayloadMapper mapper = new BookSearchPayloadMapper();

    private static void setId(Object entity, long idFieldValue) {
        ReflectionTestUtils.setField(entity, "id", idFieldValue);
    }

    @Nested
    @DisplayName("toRabbitBook 정상 매핑 테스트")
    class NormalMapping {

        @Test
        @DisplayName("toRabbitBook: Book의 모든 필드를 RabbitBook으로 올바르게 매핑한다")
        void toRabbitBook_shouldMapAllFieldsCorrectly() {
            // given
            Book book = new Book();
            setId(book, 123L);

            book.setIsbn("978-1-1111-1111-1");
            book.setTitle("테스트 제목");
            book.setDescription("설명");
            book.setPublicationDate(LocalDate.of(2025, 12, 1));
            book.setPrice(15000L);
            book.setStatus(Status.ON_SALE);

            Publisher publisher = new Publisher("한빛");
            setId(publisher, 77L);
            book.setPublisher(publisher);

            Author a1 = new Author("작가1");
            Author a2 = new Author("작가2");
            BookAuthor ba1 = new BookAuthor(book, a1);
            BookAuthor ba2 = new BookAuthor(book, a2);
            book.setBookAuthors(List.of(ba1, ba2));

            Category c1 = new Category(1001L,"카테고리1", 2);
            Category c2 = new Category(1002L,"카테고리2", 2);
            setId(c1, 10L);
            setId(c2, 20L);

            BookCategory bc1 = new BookCategory(book, c1);
            BookCategory bc2 = new BookCategory(book, c2);
            book.setBookCategories(new ArrayList<>(List.of(bc1, bc2)));

            BookImage img = new BookImage(0, "https://img/cover.jpg", ImageType.COVER);
            book.setBookImages(new ArrayList<>(List.of(img)));

            // when
            RabbitBook rb = mapper.toRabbitBook(book);

            // then
            assertThat(rb.getId()).as("bookId 매핑").isEqualTo(123L);
            assertThat(rb.getIsbn()).as("isbn 매핑").isEqualTo("978-1-1111-1111-1");
            assertThat(rb.getTitle()).as("title 매핑").isEqualTo("테스트 제목");
            assertThat(rb.getDescription()).as("description 매핑").isEqualTo("설명");
            assertThat(rb.getPubDate()).as("publicationDate 매핑").isEqualTo(LocalDate.of(2025, 12, 1));
            assertThat(rb.getPrice()).as("price(Long) -> int 매핑").isEqualTo(15000);

            assertThat(rb.getPublisher()).as("publisher name 매핑").isEqualTo("한빛");
            assertThat(rb.getPublisherId()).as("publisher id 매핑").isEqualTo(77L);

            assertThat(rb.getAuthor()).as("author join(쉼표) 매핑")
                    .isEqualTo("작가1, 작가2");

            assertThat(rb.getCategories()).as("category name 리스트 매핑")
                    .containsExactly("카테고리1", "카테고리2");

            assertThat(rb.getCategoryId()).as("마지막 category id 매핑(getLast)") // Java 21 List#getLast
                    .isEqualTo(20L);

            assertThat(rb.getImageUrl()).as("bookImages[0].path 매핑")
                    .isEqualTo("https://img/cover.jpg");
        }
    }

    @Nested
    @DisplayName("toRabbitBook null 및 빈 값 처리 테스트")
    class NullAndEmptyHandling {

        @Test
        @DisplayName("toRabbitBook: publisher가 null일 때 빈 문자열 및 null로 매핑하고, 빈 컬렉션 처리")
        void toRabbitBook_whenPublisherIsNull_shouldMapPublisherAsEmpty_andPublisherIdAsNull() {
            // given
            Book book = new Book();
            setId(book, 1L);
            book.setIsbn("ISBN");
            book.setTitle("T");
            book.setDescription("D");
            book.setPublicationDate(LocalDate.of(2025, 1, 1));
            book.setPrice(1L);
            book.setStatus(Status.ON_SALE);

            book.setBookAuthors(List.of()); // empty -> author should be ""
            book.setBookImages(new ArrayList<>()); // empty -> imageUrl should be ""

            Category c = new Category(1000L,"C", 2);
            setId(c, 9L);
            book.setBookCategories(new ArrayList<>(List.of(new BookCategory(book, c))));

            // when
            RabbitBook rb = mapper.toRabbitBook(book);

            // then
            assertThat(rb.getPublisher()).as("publisher가 null이면 빈 문자열").isEqualTo("");
            assertThat(rb.getPublisherId()).as("publisher가 null이면 publisherId는 null").isNull();
            assertThat(rb.getAuthor()).as("bookAuthors가 비어있으면 author는 빈 문자열").isEqualTo("");
            assertThat(rb.getImageUrl()).as("bookImages가 비어있으면 imageUrl은 빈 문자열").isEqualTo("");
            assertThat(rb.getCategoryId()).as("카테고리 1개일 때도 categoryId는 정상").isEqualTo(9L);
        }

        @Test
        @DisplayName("toRabbitBook: bookCategories가 비어있을 때 예외 발생")
        void toRabbitBook_whenBookCategoriesIsEmpty_shouldThrow() {
            // given
            Book book = new Book();
            setId(book, 1L);
            book.setIsbn("ISBN");
            book.setTitle("T");
            book.setDescription("D");
            book.setPublicationDate(LocalDate.of(2025, 1, 1));
            book.setPrice(1L);
            book.setStatus(Status.ON_SALE);

            book.setBookAuthors(List.of());
            book.setBookCategories(new ArrayList<>()); // mapper에서 getLast() 호출 -> 예외 기대
            book.setBookImages(new ArrayList<>());

            // when / then
            assertThatThrownBy(() -> mapper.toRabbitBook(book))
                    .as("bookCategories가 비어있으면 getLast()에서 예외가 발생해야 함 (원인 파악 용이)")
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("toRabbitBook: price가 Long 범위를 벗어나면 ArithmeticException 발생")
        void toRabbitBook_whenPriceOverflowsInt_shouldThrowArithmeticException() {
            // given
            Book book = new Book();
            setId(book, 1L);
            book.setIsbn("ISBN");
            book.setTitle("T");
            book.setDescription("D");
            book.setPublicationDate(LocalDate.of(2025, 1, 1));
            book.setPrice((long) Integer.MAX_VALUE + 1L);
            book.setStatus(Status.ON_SALE);

            Category c = new Category(1000L,"C", 2);
            setId(c, 9L);
            book.setBookCategories(new ArrayList<>(List.of(new BookCategory(book, c))));
            book.setBookAuthors(List.of());
            book.setBookImages(new ArrayList<>());

            // when / then
            assertThatThrownBy(() -> mapper.toRabbitBook(book))
                    .as("Math.toIntExact overflow 시 ArithmeticException이어야 함")
                    .isInstanceOf(ArithmeticException.class);
        }
    }
}
