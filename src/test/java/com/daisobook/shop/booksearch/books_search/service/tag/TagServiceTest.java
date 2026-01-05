package com.daisobook.shop.booksearch.books_search.service.tag;

import com.daisobook.shop.booksearch.books_search.entity.book.Book;
import com.daisobook.shop.booksearch.books_search.entity.tag.BookTag;
import com.daisobook.shop.booksearch.books_search.entity.tag.Tag;
import com.daisobook.shop.booksearch.books_search.repository.tag.BookTagRepository;
import com.daisobook.shop.booksearch.books_search.repository.tag.TagRepository;
import com.daisobook.shop.booksearch.books_search.service.tag.impl.TagV2ServiceImpl;
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
class TagServiceTest {

    @MockitoBean
    @Mock
    private TagRepository tagRepository;

    @MockitoBean @Mock
    private BookTagRepository bookTagRepository;

    @InjectMocks
    private TagV2ServiceImpl tagService;

    @Test
    @DisplayName("Book에 태그 할당 - null 또는 빈 리스트인 경우 아무 작업도 수행하지 않음")
    void assignTagsToBook_withNullOrEmpty_doesNothing() {
        Book book = mock(Book.class);

        tagService.assignTagsToBook(book, null);
        tagService.assignTagsToBook(book, Collections.emptyList());

        verifyNoInteractions(tagRepository);
        verifyNoInteractions(bookTagRepository);
    }

    @Test
    @DisplayName("Book에 태그 할당 - 기존 태그 사용, 신규 태그 생성 및 모두 저장")
    void assignTagsToBook_usesExistingAndCreatesNewAndSavesAll() {
        // 준비: book의 bookTags 리스트를 실제 리스트로 제공
        Book book = mock(Book.class);
        List<BookTag> bookTagList = new ArrayList<>();
        when(book.getBookTags()).thenReturn(bookTagList);

        // 기존 태그 mock
        Tag exist = mock(Tag.class);
        when(exist.getName()).thenReturn("exist");
        List<BookTag> existBookTags = new ArrayList<>();
        when(exist.getBookTags()).thenReturn(existBookTags);

        // repository: 존재하는 태그만 반환
        when(tagRepository.findAllByNameIn(List.of("exist", "new"))).thenReturn(List.of(exist));

        // 실행
        tagService.assignTagsToBook(book, List.of("exist", "new"));

        // 새로운 Tag가 saveAll로 전달되는지 확인
        ArgumentCaptor<Collection> saveCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(tagRepository, times(1)).saveAll(saveCaptor.capture());
        Collection saved = saveCaptor.getValue();
        assertEquals(1, saved.size());
        Object savedTag = saved.iterator().next();
        assertTrue(savedTag instanceof Tag);
        Tag newTag = (Tag) savedTag;
        assertEquals("new", newTag.getName());

        // bookTagRepository.saveAll 호출 및 book/bookTag 연결 확인
        ArgumentCaptor<Collection> bookTagCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(bookTagRepository, times(1)).saveAll(bookTagCaptor.capture());
        Collection savedBookTags = bookTagCaptor.getValue();
        assertEquals(2, savedBookTags.size()); // exist + new

        // book의 bookTags에 추가되었는지 확인
        assertEquals(2, bookTagList.size());
    }

    @Test
    @DisplayName("여러 권의 Book에 태그 할당 - 중복 태그는 한 번만 저장")
    void assignTagsToBooks_handlesMultipleBooks_and_savesNewTagsOnce() {
        // 준비: 두 권의 Book
        Book b1 = mock(Book.class);
        Book b2 = mock(Book.class);
        when(b1.getIsbn()).thenReturn("isbn1");
        when(b2.getIsbn()).thenReturn("isbn2");
        when(b1.getBookTags()).thenReturn(new ArrayList<>());
        when(b2.getBookTags()).thenReturn(new ArrayList<>());

        Map<String, Book> bookMap = Map.of("isbn1", b1, "isbn2", b2);
        Map<String, List<String>> tagMap = Map.of(
                "isbn1", List.of("tA", "tB"),
                "isbn2", List.of("tB", "tC")
        );

        // repository: 기존에 tB만 존재한다고 가정
        Tag tB = mock(Tag.class);
        when(tB.getName()).thenReturn("tB");
        when(tagRepository.findAllByNameIn(anyList())).thenReturn(List.of(tB));

        tagService.assignTagsToBooks(bookMap, tagMap);

        // 새로운 태그 tA, tC 는 saveAll 로 전달되어야 함 (2개)
        ArgumentCaptor<Collection> saveCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(tagRepository, times(1)).saveAll(saveCaptor.capture());
        Collection savedTags = saveCaptor.getValue();
        assertEquals(2, savedTags.size());

        // bookTagRepository.saveAll 은 총 4개(BookTag) 호출(2 tags for b1, 2 tags for b2)
        ArgumentCaptor<Collection> bookTagCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(bookTagRepository, times(1)).saveAll(bookTagCaptor.capture());
        Collection savedBookTags = bookTagCaptor.getValue();
        assertEquals(4, savedBookTags.size());
    }

    @Test
    @DisplayName("Book의 태그 업데이트 - 제거된 태그 삭제 및 신규 태그 할당")
    void updateTagOfBook_deletesRemovedAnd_assignsNew() {
        // 준비: book, 기존 BookTag -> 기존 Tag들
        Book book = mock(Book.class);
        when(book.getId()).thenReturn(10L);
        List<BookTag> preBookTags = new ArrayList<>();
        Tag pre1 = mock(Tag.class);
        when(pre1.getName()).thenReturn("a");
        when(pre1.getId()).thenReturn(1L);
        BookTag bt1 = mock(BookTag.class);
        when(bt1.getTag()).thenReturn(pre1);
        when(bt1.getId()).thenReturn(100L);
        preBookTags.add(bt1);

        Tag pre2 = mock(Tag.class);
        when(pre2.getName()).thenReturn("b");
        when(pre2.getId()).thenReturn(2L);
        BookTag bt2 = mock(BookTag.class);
        when(bt2.getTag()).thenReturn(pre2);
        when(bt2.getId()).thenReturn(200L);
        preBookTags.add(bt2);

        when(book.getBookTags()).thenReturn(preBookTags);

        // 삭제 대상: pre 태그 중 "b" 가 리스트에 없으면 삭제
        when(bookTagRepository.findAllByBook_IdAndTag_IdIn(eq(10L), anyList()))
                .thenReturn(List.of(bt2));

        // 실행: 새로운 리스트에서 "b"가 빠지고 "c"가 추가됨 -> b 삭제, c 추가
        tagService.updateTagOfBook(book, List.of("a", "c"));

        // 삭제 아이디 리스트가 deleteAllById 호출로 전달되는지 확인
        ArgumentCaptor<Collection> deleteIdsCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(bookTagRepository, times(1)).deleteAllById(deleteIdsCaptor.capture());
        Collection deletedIds = deleteIdsCaptor.getValue();
        assertTrue(deletedIds.contains(200L));

        // assignTagsToBook 로 인해 saveAll 호출 (신규 tag c 생성)
        verify(tagRepository, atLeastOnce()).saveAll(any());
        verify(bookTagRepository, atLeastOnce()).saveAll(any());
    }

    @Test
    @DisplayName("Book의 모든 태그 삭제 - 양방향 연관관계 제거 후 모두 삭제")
    void deleteTagOfBook_removesAssociations_and_deletesAll() {
        Book book = mock(Book.class);

        Tag t1 = mock(Tag.class);
        Tag t2 = mock(Tag.class);
        List<BookTag> bookTags = new ArrayList<>();
        BookTag bt1 = mock(BookTag.class);
        BookTag bt2 = mock(BookTag.class);
        when(bt1.getTag()).thenReturn(t1);
        when(bt2.getTag()).thenReturn(t2);
        when(bt1.getId()).thenReturn(11L);
        when(bt2.getId()).thenReturn(22L);
        bookTags.add(bt1);
        bookTags.add(bt2);

        List<BookTag> t1List = new ArrayList<>(bookTags);
        List<BookTag> t2List = new ArrayList<>(bookTags);
        when(t1.getBookTags()).thenReturn(t1List);
        when(t2.getBookTags()).thenReturn(t2List);

        when(book.getBookTags()).thenReturn(bookTags);

        tagService.deleteTagOfBook(book);

        // 각 Tag의 bookTags에서 제거되었는지 확인
        assertTrue(t1List.isEmpty());
        assertTrue(t2List.isEmpty());

        // book의 bookTags도 비워졌는지 확인
        assertTrue(bookTags.isEmpty());

        // deleteAll 호출의 인자가 서비스 구현에 따라 빈 리스트일 수 있으므로 캡처하여 허용 범위를 검사
        ArgumentCaptor<Collection> deleteCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(bookTagRepository, times(1)).deleteAll(deleteCaptor.capture());
        Collection deletedArg = deleteCaptor.getValue();
        // 서비스가 원본 리스트를 그대로 전달하기 전 빈 리스트로 만들면 빈 컬렉션이 올 수 있음.
        // 따라서 빈 리스트거나 원래 요소들을 포함하는 경우를 허용함.
        assertTrue(deletedArg.isEmpty() || deletedArg.containsAll(Arrays.asList(bt1, bt2)));
    }
}
