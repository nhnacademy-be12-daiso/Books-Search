package com.daisobook.shop.booksearch.books_search.service.publisher;

import com.daisobook.shop.booksearch.books_search.entity.book.Book;
import com.daisobook.shop.booksearch.books_search.entity.publisher.Publisher;
import com.daisobook.shop.booksearch.books_search.repository.publisher.PublisherRepository;
import com.daisobook.shop.booksearch.books_search.service.publisher.impl.PublisherV2ServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PublisherServiceTest {

    @MockitoBean
    @Mock
    private PublisherRepository publisherRepository;

    @InjectMocks
    private PublisherV2ServiceImpl publisherService;

    @Test
    @DisplayName("assignPublisherToBook: 퍼블리셔가 없으면 새로 생성하고 저장함")
    void assignPublisherToBook_createsAndSavesWhenPublisherNotFound() {
        Book book = mock(Book.class);
        String publisherName = "NewPub";

        when(publisherRepository.findPublisherByName(publisherName)).thenReturn(null);

        publisherService.assignPublisherToBook(book, publisherName);

        ArgumentCaptor<Publisher> captor = ArgumentCaptor.forClass(Publisher.class);
        verify(publisherRepository, times(1)).save(captor.capture());
        Publisher saved = captor.getValue();
        assertNotNull(saved);
        assertEquals(publisherName, saved.getName());
        // 책이 publisher의 리스트에 추가되었고 book.setPublisher 호출 검증
        assertTrue(saved.getBookList().contains(book));
        verify(book, times(1)).setPublisher(saved);
    }

    @Test
    @DisplayName("assignPublisherToBook: 기존 퍼블리셔가 있으면 새로 저장하지 않고 사용함")
    void assignPublisherToBook_usesExistingPublisherWithoutSaving() {
        Book book = mock(Book.class);
        String publisherName = "ExistPub";

        Publisher exist = mock(Publisher.class);
        // 기존 퍼블리셔에 실제 리스트를 반환하도록 stub
        List<Book> bookList = new ArrayList<>();
        when(exist.getBookList()).thenReturn(bookList);
        when(publisherRepository.findPublisherByName(publisherName)).thenReturn(exist);

        publisherService.assignPublisherToBook(book, publisherName);

        verify(publisherRepository, never()).save(any());
        assertTrue(bookList.contains(book));
        verify(book, times(1)).setPublisher(exist);
    }

    @Test
    @DisplayName("assignPublisherToBooks: 기존 퍼블리셔와 새 퍼블리셔가 섞여 있을 때, 새 퍼블리셔는 생성 및 저장됨")
    void assignPublisherToBooks_mixedExistingAndNew_publishersCreatedAndSaved() {
        Book b1 = mock(Book.class);
        Book b2 = mock(Book.class);
        when(b1.getIsbn()).thenReturn("isbn1");
        when(b2.getIsbn()).thenReturn("isbn2");

        Map<String, Book> bookMap = new HashMap<>();
        bookMap.put("isbn1", b1);
        bookMap.put("isbn2", b2);

        Map<String, String> publisherNameMap = new HashMap<>();
        publisherNameMap.put("isbn1", "ExistPub");
        publisherNameMap.put("isbn2", "NewPub");

        Publisher exist = mock(Publisher.class);
        List<Book> existList = new ArrayList<>();
        when(exist.getBookList()).thenReturn(existList);
        when(exist.getName()).thenReturn("ExistPub");

        // repository returns only existing publisher; NewPub will be created
        when(publisherRepository.findAllByNameIn(anyList())).thenReturn(List.of(exist));

        publisherService.assignPublisherToBooks(bookMap, publisherNameMap);

        // verify saveAll called for new publishers
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(publisherRepository, times(1)).saveAll(captor.capture());
        List savedList = captor.getValue();
        assertEquals(1, savedList.size());
        Object maybePub = savedList.get(0);
        assertInstanceOf(Publisher.class, maybePub);
        Publisher newPub = (Publisher) maybePub;
        assertEquals("NewPub", newPub.getName());
        assertTrue(newPub.getBookList().contains(b2));

        // existing publisher got b1 added
        assertTrue(existList.contains(b1));
        verify(b1, times(1)).setPublisher(exist);
        verify(b2, times(1)).setPublisher(newPub);
    }

    @Test
    @DisplayName("updatePublisherOfBook: 동일한 이름이면 아무 작업도 하지 않음")
    void updatePublisherOfBook_noopWhenSameName() {
        Book book = mock(Book.class);
        Publisher pre = mock(Publisher.class);
        when(pre.getName()).thenReturn("SameName");
        when(book.getPublisher()).thenReturn(pre);

        publisherService.updatePublisherOfBook(book, "SameName");

        verify(publisherRepository, never()).findPublisherByName(anyString());
        verify(publisherRepository, never()).save(any());
        verify(book, never()).setPublisher(any());
    }

    @Test
    @DisplayName("updatePublisherOfBook: 퍼블리셔가 변경될 때, 기존 퍼블리셔에서 제거하고 새 퍼블리셔에 추가함")
    void updatePublisherOfBook_createsNewPublisherAndSaves_whenNotFound() {
        Book book = mock(Book.class);
        Publisher pre = mock(Publisher.class);
        List<Book> preList = new ArrayList<>();
        preList.add(book);
        when(pre.getBookList()).thenReturn(preList);
        when(pre.getName()).thenReturn("OldPub");
        when(book.getPublisher()).thenReturn(pre);

        when(publisherRepository.findPublisherByName("NewPub")).thenReturn(null);

        publisherService.updatePublisherOfBook(book, "NewPub");

        // pre에서 제거됨
        assertFalse(preList.contains(book));
        // 새 퍼블리셔 저장됨
        ArgumentCaptor<Publisher> captor = ArgumentCaptor.forClass(Publisher.class);
        verify(publisherRepository, times(1)).save(captor.capture());
        Publisher saved = captor.getValue();
        assertEquals("NewPub", saved.getName());
        assertTrue(saved.getBookList().contains(book));
        verify(book, times(1)).setPublisher(saved);
    }

    @Test
    @DisplayName("deletePublisherOfBook: 퍼블리셔가 null인 경우와 존재하는 경우 모두 처리함")
    void deletePublisherOfBook_handlesNullPublisherAndRemovesWhenPresent() {
        Book book1 = mock(Book.class);
        when(book1.getPublisher()).thenReturn(null);

        publisherService.deletePublisherOfBook(book1);
        verify(book1, times(1)).setPublisher(null);

        Book book2 = mock(Book.class);
        Publisher pub = mock(Publisher.class);
        List<Book> list = new ArrayList<>();
        list.add(book2);
        when(pub.getBookList()).thenReturn(list);
        when(book2.getPublisher()).thenReturn(pub);

        publisherService.deletePublisherOfBook(book2);
        assertFalse(list.contains(book2));
        verify(book2, times(1)).setPublisher(null);
    }
}
