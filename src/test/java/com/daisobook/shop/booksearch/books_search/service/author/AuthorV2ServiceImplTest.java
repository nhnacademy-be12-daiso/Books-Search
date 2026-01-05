package com.daisobook.shop.booksearch.books_search.service.author;

import com.daisobook.shop.booksearch.books_search.dto.projection.RoleNameProjection;
import com.daisobook.shop.booksearch.books_search.dto.request.AuthorReqDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.RoleNameListRespDTO;
import com.daisobook.shop.booksearch.books_search.entity.author.Author;
import com.daisobook.shop.booksearch.books_search.entity.author.BookAuthor;
import com.daisobook.shop.booksearch.books_search.entity.author.Role;
import com.daisobook.shop.booksearch.books_search.entity.book.Book;
import com.daisobook.shop.booksearch.books_search.repository.author.AuthorRepository;
import com.daisobook.shop.booksearch.books_search.repository.author.BookAuthorRepository;
import com.daisobook.shop.booksearch.books_search.repository.author.RoleRepository;
import com.daisobook.shop.booksearch.books_search.service.author.impl.AuthorV2ServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorV2ServiceImplTest {

    @Mock
    AuthorRepository authorRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    BookAuthorRepository bookAuthorRepository;

    @InjectMocks
    AuthorV2ServiceImpl authorV2Service;

    // Helpers
    private static Book newBook(String isbn) {
        Book book = BeanUtils.instantiateClass(Book.class);
        ReflectionTestUtils.setField(book, "isbn", isbn);
        if (book.getBookAuthors() == null) {
            ReflectionTestUtils.setField(book, "bookAuthors", new ArrayList<BookAuthor>());
        }
        return book;
    }

    private static Author newAuthor(String name) {
        Author author = new Author(name);
        if (author.getBookAuthors() == null) {
            ReflectionTestUtils.setField(author, "bookAuthors", new ArrayList<BookAuthor>());
        }
        return author;
    }

    private static Role newRole(String name, long id) {
        Role role = BeanUtils.instantiateClass(Role.class);
        ReflectionTestUtils.setField(role, "name", name);
        ReflectionTestUtils.setField(role, "id", id);
        if (role.getBookAuthors() == null) {
            ReflectionTestUtils.setField(role, "bookAuthors", new ArrayList<BookAuthor>());
        }
        return role;
    }

    private static BookAuthor newBookAuthor(Book book, Author author, Role roleOrNull, long id) {
        BookAuthor ba = new BookAuthor(book, author);
        ReflectionTestUtils.setField(ba, "id", id);
        if (roleOrNull != null) {
            ba.setRole(roleOrNull);
        }
        return ba;
    }

    private static AuthorReqDTO req(String authorName, String roleName) {
        return new AuthorReqDTO(authorName, roleName);
    }

    // Tests

    @Test
    @DisplayName("assignAuthorsToBook: null/empty 요청이면 아무 repo 호출 안함")
    void assignAuthorsToBook_whenNullOrEmpty_thenNoRepoCall() {
        Book book = newBook("ISBN-1");

        authorV2Service.assignAuthorsToBook(book, null);
        authorV2Service.assignAuthorsToBook(book, Collections.emptyList());

        verifyNoInteractions(authorRepository, roleRepository, bookAuthorRepository);
        assertThat(book.getBookAuthors()).isEmpty();
    }

    @Test
    @DisplayName("assignAuthorsToBook: 기존 Author/Role 재사용")
    void assignAuthorsToBook_existingAuthorAndRole() {
        Book book = newBook("ISBN-1");
        Author alice = newAuthor("Alice");
        Role writer = newRole("WRITER", 10L);

        when(authorRepository.findAllByNameIn(anyList())).thenReturn(List.of(alice));
        when(roleRepository.findAllByNameIn(anyList())).thenReturn(List.of(writer));
        when(bookAuthorRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        authorV2Service.assignAuthorsToBook(book, List.of(req("Alice", "WRITER")));

        verify(authorRepository, never()).saveAll(anyList());
        verify(bookAuthorRepository, times(1)).saveAll(anyList());

        assertThat(book.getBookAuthors()).hasSize(1);
        BookAuthor ba = book.getBookAuthors().getFirst();
        assertThat(ba.getAuthor().getName()).isEqualTo("Alice");
        assertThat(ba.getRole()).isNotNull();
        assertThat(ba.getRole().getName()).isEqualTo("WRITER");
        assertThat(alice.getBookAuthors()).contains(ba);
        assertThat(writer.getBookAuthors()).contains(ba);
    }

    @Test
    @DisplayName("assignAuthorsToBook: 새작가 저장, role 없으면 null")
    void assignAuthorsToBook_newAuthor_roleMissingOrNull() {
        Book book = newBook("ISBN-1");
        when(authorRepository.findAllByNameIn(anyList())).thenReturn(Collections.emptyList());
        when(roleRepository.findAllByNameIn(anyList())).thenReturn(Collections.emptyList());
        when(authorRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookAuthorRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        authorV2Service.assignAuthorsToBook(book, List.of(req("NewGuy", null)));

        verify(authorRepository, times(1)).saveAll(anyList());
        verify(bookAuthorRepository, times(1)).saveAll(anyList());

        assertThat(book.getBookAuthors()).hasSize(1);
        BookAuthor ba = book.getBookAuthors().getFirst();
        assertThat(ba.getAuthor().getName()).isEqualTo("NewGuy");
        assertThat(ba.getRole()).isNull();
    }

    @Test
    @DisplayName("assignAuthorsToBooks: 여러 권 일괄 처리 - 기존 재사용, 없는 것만 저장")
    void assignAuthorsToBooks_bulkMixExistingAndNew() {
        Book b1 = newBook("ISBN-1");
        Book b2 = newBook("ISBN-2");
        Map<String, Book> bookMap = new LinkedHashMap<>();
        bookMap.put(b1.getIsbn(), b1);
        bookMap.put(b2.getIsbn(), b2);

        Map<String, List<AuthorReqDTO>> authorListMap = new HashMap<>();
        authorListMap.put("ISBN-1", List.of(req("Alice", "WRITER")));
        authorListMap.put("ISBN-2", List.of(req("Bob", "WRITER"), req("Charlie", null)));

        Author alice = newAuthor("Alice");
        Role writer = newRole("WRITER", 10L);

        when(authorRepository.findAllByNameIn(anyList())).thenReturn(List.of(alice));
        when(roleRepository.findAllByNameIn(anyList())).thenReturn(List.of(writer));
        when(authorRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookAuthorRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        authorV2Service.assignAuthorsToBooks(bookMap, authorListMap);

        verify(authorRepository, times(1)).saveAll(anyList());
        verify(bookAuthorRepository, times(1)).saveAll(anyList());

        assertThat(b1.getBookAuthors()).hasSize(1);
        assertThat(b2.getBookAuthors()).hasSize(2);
    }

    @Test
    @DisplayName("updateAuthorOfBook: 요청에 없는 작가는 삭제하고 관계 정리")
    void updateAuthorOfBook_deleteRemovedAuthors() {
        Book book = newBook("ISBN-1");

        Author a1 = newAuthor("Alice");
        Author a2 = newAuthor("Bob");
        Role r1 = newRole("WRITER", 10L);

        BookAuthor ba1 = newBookAuthor(book, a1, r1, 101L);
        BookAuthor ba2 = newBookAuthor(book, a2, null, 202L);

        book.getBookAuthors().addAll(List.of(ba1, ba2));
        a1.getBookAuthors().add(ba1);
        a2.getBookAuthors().add(ba2);
        r1.getBookAuthors().add(ba1);

        when(roleRepository.findAllByNameIn(anyList())).thenReturn(List.of(r1));
        when(authorRepository.findAllByNameIn(anyList())).thenReturn(List.of(a1, a2));
        doNothing().when(bookAuthorRepository).deleteAllById(anyList());

        authorV2Service.updateAuthorOfBook(book, List.of(req("Alice", "WRITER")));

        verify(bookAuthorRepository, times(1)).deleteAllById(anyList());
        assertThat(book.getBookAuthors()).hasSize(1);
        assertThat(book.getBookAuthors().getFirst().getAuthor().getName()).isEqualTo("Alice");
        assertThat(a2.getBookAuthors()).doesNotContain(ba2);
    }

    @Test
    @DisplayName("updateAuthorOfBook: role 변경 시 기존 role에서 제거하고 새 role에 추가")
    void updateAuthorOfBook_changeRole() {
        Book book = newBook("ISBN-1");
        Author a1 = newAuthor("Alice");
        Role oldRole = newRole("WRITER", 10L);
        Role newRole = newRole("TRANSLATOR", 20L);

        BookAuthor ba1 = newBookAuthor(book, a1, oldRole, 101L);
        book.getBookAuthors().add(ba1);
        a1.getBookAuthors().add(ba1);
        oldRole.getBookAuthors().add(ba1);

        when(roleRepository.findAllByNameIn(anyList())).thenReturn(List.of(newRole));
        when(authorRepository.findAllByNameIn(anyList())).thenReturn(List.of(a1));

        authorV2Service.updateAuthorOfBook(book, List.of(req("Alice", "TRANSLATOR")));

        assertThat(ba1.getRole()).isNotNull();
        assertThat(ba1.getRole().getName()).isEqualTo("TRANSLATOR");
        assertThat(oldRole.getBookAuthors()).doesNotContain(ba1);
        assertThat(newRole.getBookAuthors()).contains(ba1);
    }

    @Test
    @DisplayName("updateAuthorOfBook: 기존 role이 있는데 요청 role이 null이면 role 제거")
    void updateAuthorOfBook_removeRole_whenRequestRoleIsNull() {
        Book book = newBook("ISBN-1");
        Author a1 = newAuthor("Alice");
        Role oldRole = newRole("WRITER", 10L);

        BookAuthor ba1 = newBookAuthor(book, a1, oldRole, 101L);
        book.getBookAuthors().add(ba1);
        a1.getBookAuthors().add(ba1);
        oldRole.getBookAuthors().add(ba1);

        when(roleRepository.findAllByNameIn(anyList())).thenReturn(Collections.emptyList());
        when(authorRepository.findAllByNameIn(anyList())).thenReturn(List.of(a1));

        authorV2Service.updateAuthorOfBook(book, List.of(req("Alice", null)));

        assertThat(ba1.getRole()).isNull();
        assertThat(oldRole.getBookAuthors()).doesNotContain(ba1);
    }

    @Test
    @DisplayName("updateAuthorOfBook: 새 작가 요청이면 저장 및 BookAuthor 추가")
    void updateAuthorOfBook_addNewAuthor() {
        Book book = newBook("ISBN-1");
        when(roleRepository.findAllByNameIn(anyList())).thenReturn(Collections.emptyList());
        when(authorRepository.findAllByNameIn(anyList())).thenReturn(Collections.emptyList());
        when(authorRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookAuthorRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        authorV2Service.updateAuthorOfBook(book, List.of(req("NewGuy", null)));

        verify(authorRepository, times(1)).saveAll(anyList());
        verify(bookAuthorRepository, times(1)).saveAll(anyList());

        assertThat(book.getBookAuthors()).hasSize(1);
        assertThat(book.getBookAuthors().getFirst().getAuthor().getName()).isEqualTo("NewGuy");
    }

    @Test
    @DisplayName("deleteAuthorOfBook: 모든 관계 정리 후 deleteAll 호출")
    void deleteAuthorOfBook_clearAllRelationsAndDelete() {
        Book book = newBook("ISBN-1");
        Author a1 = newAuthor("Alice");
        Author a2 = newAuthor("Bob");
        Role r1 = newRole("WRITER", 10L);

        BookAuthor ba1 = newBookAuthor(book, a1, r1, 101L);
        BookAuthor ba2 = newBookAuthor(book, a2, null, 202L);

        book.getBookAuthors().addAll(List.of(ba1, ba2));
        a1.getBookAuthors().add(ba1);
        a2.getBookAuthors().add(ba2);
        r1.getBookAuthors().add(ba1);

        doNothing().when(bookAuthorRepository).deleteAll(anyList());

        authorV2Service.deleteAuthorOfBook(book);

        verify(bookAuthorRepository, times(1)).deleteAll(anyList());
        assertThat(book.getBookAuthors()).isEmpty();
        assertThat(a1.getBookAuthors()).doesNotContain(ba1);
        assertThat(a2.getBookAuthors()).doesNotContain(ba2);
        assertThat(r1.getBookAuthors()).doesNotContain(ba1);
    }


    @Test
    @DisplayName("getRoleNameList: projection을 dto로 변환")
    void getRoleNameList_returnsRoleNames() {
        RoleNameProjection p1 = () -> "WRITER";
        RoleNameProjection p2 = () -> "TRANSLATOR";

        when(roleRepository.getAllRoleName()).thenReturn(List.of(p1, p2));

        RoleNameListRespDTO resp = authorV2Service.getRoleNameList();

        assertThat(resp.roleNames()).containsExactly("WRITER", "TRANSLATOR");
    }
}
