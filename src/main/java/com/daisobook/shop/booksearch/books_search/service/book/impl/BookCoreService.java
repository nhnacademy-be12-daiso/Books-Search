package com.daisobook.shop.booksearch.books_search.service.book.impl;

import com.daisobook.shop.booksearch.books_search.dto.BookUpdateData;
import com.daisobook.shop.booksearch.books_search.dto.projection.*;
import com.daisobook.shop.booksearch.books_search.dto.request.AuthorReqDTO;
import com.daisobook.shop.booksearch.books_search.entity.book.Book;
import com.daisobook.shop.booksearch.books_search.entity.book.Status;
import com.daisobook.shop.booksearch.books_search.exception.custom.book.DuplicatedBook;
import com.daisobook.shop.booksearch.books_search.exception.custom.book.NotFoundBookISBN;
import com.daisobook.shop.booksearch.books_search.exception.custom.book.NotFoundBookId;
import com.daisobook.shop.booksearch.books_search.repository.BookOfTheWeekRepository;
import com.daisobook.shop.booksearch.books_search.repository.book.BookRepository;
import com.daisobook.shop.booksearch.books_search.search.component.BookSearchSyncPublisher;
import com.daisobook.shop.booksearch.books_search.service.author.AuthorV2Service;
import com.daisobook.shop.booksearch.books_search.service.category.CategoryV2Service;
import com.daisobook.shop.booksearch.books_search.service.publisher.PublisherV2Service;
import com.daisobook.shop.booksearch.books_search.service.tag.TagV2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookCoreService {
    private final BookRepository bookRepository;

    private final CategoryV2Service categoryService;
    private final TagV2Service tagService;
    private final AuthorV2Service authorService;
    private final PublisherV2Service publisherService;

    private final BookOfTheWeekRepository bookOfTheWeekRepository;
    private final BookSearchSyncPublisher bookSearchSyncPublisher;

    @Transactional(readOnly = true)
    public void validateExistsById(long bookId) {
        if(!bookRepository.existsBookById(bookId)) {
            log.error("존재하지 않은 도서ID: {}", bookId);
            throw new NotFoundBookId("존재하지 않은 도서ID 입니다.");
        }
    }

    @Transactional(readOnly = true)
    public void validateExistsByIsbn(String isbn) {
        if(!bookRepository.existsBookByIsbn(isbn)) {
            log.error("존재하지 않은 ISBN: {}", isbn);
            throw new NotFoundBookISBN("존재하지 않은 ISBN 입니다.");
        }
    }

    @Transactional(readOnly = true)
    public void validateNotExistsByIsbn(String isbn) {
        if(bookRepository.existsBookByIsbn(isbn)) {
            log.error("이미 존재하는 ISBN: {}", isbn);
            throw new DuplicatedBook("이미 존재하는 ISBN 입니다.");
        }
    }

    @Transactional
    public Book registerBook(Book book, Long categoryId, List<String> tagNameList, List<AuthorReqDTO> authorReqDTOList,
                             String publisherName) {

        bookRepository.save(book);
        categoryService.assignCategoriesToBook(book, categoryId);
        tagService.assignTagsToBook(book, tagNameList);
        authorService.assignAuthorsToBook(book, authorReqDTOList);
        publisherService.assignPublisherToBook(book, publisherName);

        log.debug("도서 저장 - ISBN: {}, Title: {}, Author: {}", book.getIsbn(), book.getTitle(),
                book.getBookAuthors().stream()
                        .map(ba -> ba.getAuthor().getName() + ba.getRole().getName())
                        .toList());

        return book;
    }

    @Transactional(readOnly = true)
    public Set<BookIsbnProjection> getExistsByIsbn(List<String> isbns){
        List<BookIsbnProjection> isIsbns = bookRepository.findBooksByIsbnIn(isbns);
        if(isIsbns == null || isIsbns.isEmpty()){
            return null;
        }

        return new HashSet<>(isIsbns);
    }

    @Transactional
    public Map<String, Book> registerBooks(Map<String, Book> bookMap, Map<String, Long> categoryIdMap, Map<String, List<String>> tagNameListMap,
                                           Map<String, List<AuthorReqDTO>> authorListMap, Map<String, String> publisherNameMap){
        categoryService.assignCategoriesToBooks(bookMap, categoryIdMap);
        tagService.assignTagsToBooks(bookMap, tagNameListMap);
        authorService.assignAuthorsToBooks(bookMap, authorListMap);
        publisherService.assignPublisherToBooks(bookMap, publisherNameMap);

        bookRepository.saveAll(bookMap.values());

        log.debug("도서 저장 수 - {}권", bookMap.size());

        return bookMap;
    }

    @Transactional
    public Book getBook_Id(long bookId){
        Book book = bookRepository.findBookById(bookId);
        if(book == null){
            log.error("해당하는 도서를 찾지 못하였습니다 - 도서ID: {}", bookId);
            return null;
        }

        return book;
    }

    @Transactional
    public Book updateBookByData(Book book, BookUpdateData updateCheckDTO){

        //book 엔티티 필드 값 체크
        if(updateCheckDTO.title() != null &&
                !book.getTitle().equals(updateCheckDTO.title())){
            book.setTitle(updateCheckDTO.title());
        }
        if(updateCheckDTO.index() != null &&
                !Objects.equals(book.getIndex(), updateCheckDTO.index())){
            book.setIndex(updateCheckDTO.index());
        }
        if(updateCheckDTO.description() != null &&
                !Objects.equals(book.getDescription(), updateCheckDTO.description())){
            book.setDescription(updateCheckDTO.description());
        }
        if(updateCheckDTO.publicationDate() != null &&
                !book.getPublicationDate().equals(updateCheckDTO.publicationDate())){
            book.setPublicationDate(updateCheckDTO.publicationDate());
        }
        if(updateCheckDTO.price() != null &&
                !book.getPrice().equals(updateCheckDTO.price())){
            book.setPrice(updateCheckDTO.price());
        }
        if(updateCheckDTO.isPackaging() != null &&
                book.isPackaging() != updateCheckDTO.isPackaging()){
            book.setPackaging(updateCheckDTO.isPackaging());
        }
        if(updateCheckDTO.stock() != null &&
                !book.getStock().equals(updateCheckDTO.stock())){
            book.setStock(updateCheckDTO.stock());
        }
        if(updateCheckDTO.status() != null &&
                !book.getStatus().equals(updateCheckDTO.status())){
            book.setStatus(updateCheckDTO.status());
        }
        if(updateCheckDTO.volumeNo() != null &&
                !Objects.equals(book.getVolumeNo(), updateCheckDTO.volumeNo())){
            book.setVolumeNo(updateCheckDTO.volumeNo());
        }
        if(updateCheckDTO.isDeleted() != null &&
                book.isDeleted() != updateCheckDTO.isDeleted()){
            book.setDeleted(updateCheckDTO.isDeleted());
            bookSearchSyncPublisher.publishDeleteAfterCommit(book.getIsbn(), "BOOK_DELETE");

        }

        //book엔티티와 관련된 엔티티 체크
        authorService.updateAuthorOfBook(book, updateCheckDTO.author());
        categoryService.updateCategoryOfBook(book, updateCheckDTO.category());
        tagService.updateTagOfBook(book, updateCheckDTO.tag());
        publisherService.updatePublisherOfBook(book, updateCheckDTO.publisher());

        return book;
    }

    @Transactional
    public Book deleteBookByData(Book book){

        authorService.deleteAuthorOfBook(book);
        categoryService.deleteCategoryOfBook(book);
        tagService.deleteTagOfBook(book);
        publisherService.deletePublisherOfBook(book);

        return book;
    }

    @Transactional
    public void deleteBook(Book book){
        bookRepository.delete(book);
    }

    @Transactional(readOnly = true)
    public long getBookIdByIsbn(String isbn){
        if(isbn == null || isbn.isBlank()){
            log.error("isbn에 비어있습니다.");
            throw new NotFoundBookISBN("isbn에 비어있습니다");
        }

        BookIdProjection bookId = bookRepository.getBookId(isbn);
        if(bookId == null){
            log.error("[도서 삭제] 해당 isbn으로 해당 도서를 찾지 못했습니다 - ISBN:{}", isbn);
            throw new NotFoundBookISBN("[도서 삭제] 해당 isbn으로 해당 도서를 찾지 못했습니다");
        }

        return bookId.getId();
    }

    @Transactional(readOnly = true)
    public BookDetailProjection getBookDetail_Id(long bookId){
        BookDetailProjection detail = bookRepository.getBookDetailById(bookId, false);
        if(detail == null){
            log.error("[도서 조회] 해당하는 도서를 찾지 못하였습니다 - 도서ID: {}", bookId);
            throw new NotFoundBookId("[도서 조회] 해당하는 도서를 찾지 못하였습니다");
        }

        return detail;
    }

    @Transactional(readOnly = true)
    public BookUpdateViewProjection getBookUpdateView_Id(long bookId){
        BookUpdateViewProjection updateView = bookRepository.getBookUpdateView(bookId);
        if(updateView == null){
            log.error("[수정 도서 조회] 해당하는 도서를 찾지 못하였습니다 - 도서ID: {}", bookId);
            throw new NotFoundBookId("[수정 도서 조회] 해당하는 도서를 찾지 못하였습니다");
        }

        return updateView;
    }

    @Transactional(readOnly = true)
    public List<Long> getBookIdsFromBookOfTheWeek(Pageable pageable, Integer limit){
        if(limit == null) {
            limit = 10;
        }

        if(pageable == null) {
            pageable = PageRequest.of(0, limit);
        }
        List<BookIdProjection> bookId = bookOfTheWeekRepository.getBookId(pageable);

        return bookId.stream()
                .map(BookIdProjection::getId)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Long> getBookIdsOfNewReleases(Pageable pageable, LocalDate startDate, Integer limit){
        if(startDate == null){
            startDate = LocalDate.now().minusMonths(6);
        }
        if(limit == null){
            limit = 10;
        }

        if(pageable == null) {
            pageable = PageRequest.of(0, limit);
        }
        List<BookIdProjection> bookId = bookRepository.getBookIdByNewReleases(startDate, pageable);

        return bookId.stream()
                .map(BookIdProjection::getId)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookListProjection> getBookByIds(List<Long> bookIds, boolean includeDeleted){
        return bookRepository.getBookListBy(bookIds, includeDeleted);
    }

    @Transactional(readOnly = true)
    public List<BookInfoListProjection> getBookInfoListByInd(List<Long> bookIds, boolean includeDeleted){
        return bookRepository.getBookInfoListBy(bookIds, includeDeleted);
    }

    @Transactional(readOnly = true)
    public List<BookSummeryProjection> getBookSummeryByIds(List<Long> bookIds){
        return bookRepository.getBookSummeryByIdIn(bookIds);
    }

    @Transactional
    public List<Book> getBookByIdIn(List<Long> bookIds){
        return bookRepository.findAllByIdIn(bookIds);
    }

    @Transactional
    public Boolean existIsbn(String isbn){
        return bookRepository.existsBookByIsbn(isbn);
    }

    @Transactional
    public Page<BookAdminProjection> getBookAdminProjectionPage(Pageable pageable){
        return bookRepository.getBookAdminProjection(pageable);
    }

    @Transactional
    public Long getCountAll(){
        return bookRepository.count();
    }

    @Transactional
    public Long getCountByStatus(Status status){
        return bookRepository.countAllByStatus(status);
    }

    @Transactional
    public Page<BookListProjection> getBookByCategoryIdList(Pageable pageable, List<Long> categoryIds){
        return bookRepository.getBookByCategoryIdIn(pageable, categoryIds, false);
    }
}
