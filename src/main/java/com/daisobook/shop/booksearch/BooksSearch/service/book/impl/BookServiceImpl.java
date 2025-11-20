package com.daisobook.shop.booksearch.BooksSearch.service.book.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.*;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.BookRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.CategoryRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.TagRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.*;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.*;
import com.daisobook.shop.booksearch.BooksSearch.repository.*;
import com.daisobook.shop.booksearch.BooksSearch.service.book.BookService;
import com.daisobook.shop.booksearch.BooksSearch.service.category.CategoryService;
import com.daisobook.shop.booksearch.BooksSearch.service.publisher.PublisherService;
import com.daisobook.shop.booksearch.BooksSearch.service.tag.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookCategoryRepository bookCategoryRepository;
    private final CategoryService categoryService;
    private final BookTagRepository bookTagRepository;
    private final TagService tagService;
    private final PublisherService publisherService;

    @Override
    public void validateExistsById(long bookId) {
        if(!bookRepository.existsBookById(bookId)) {
            log.error("존재하지 않은 도서ID: {}", bookId);
            throw new NotFoundBookId("존재하지 않은 도서ID 입니다.");
        }
    }

    @Override
    public void validateExistsByIsbn(String isbn) {
        if(!bookRepository.existsBookByIsbn(isbn)) {
            log.error("존재하지 않은 ISBN: {}", isbn);
            throw new NotFoundBookISBN("존재하지 않은 ISBN 입니다.");
        }
    }

    @Override
    public void validateNotExistsByIsbn(String isbn) {
        if(bookRepository.existsBookByIsbn(isbn)) {
            log.error("이미 존재하는 ISBN: {}", isbn);
            throw new DuplicatedBook("이미 존재하는 ISBN 입니다.");
        }
    }

    @Override
    @Transactional
    public void assignCategoriesToBook(Book book, List<CategoryReqDTO> categories) {
        if(categories == null || categories.isEmpty()){
            return;
        }

        for(CategoryReqDTO c: categories) {
            Category category = categoryService.findValidCategoryByNameAndDeep(c.categoryName(), c.deep());

            BookCategory bookCategory = new BookCategory(book, category);

            category.getBookCategories().add(bookCategory);
            book.getBookCategories().add(bookCategory);

            bookCategoryRepository.save(bookCategory);
        }
    }

    @Override
    @Transactional
    public void assignTagsToBook(Book book, List<String> tagNames) {

        for(String t: tagNames){
            Tag tag = tagService.findTagByName(t);

            BookTag bookTag = new BookTag(book, tag);

            tag.getBookTags().add(bookTag);
            book.getBookTags().add(bookTag);

            bookTagRepository.save(bookTag);
        }
    }

    @Override
    @Transactional
    public void registerBook(BookReqDTO bookReqDTO) {
        validateNotExistsByIsbn(bookReqDTO.isbn());

        Book newBook = Book.create(bookReqDTO, publisherService.getPublisherRegisterBook(bookReqDTO.publisher()));

        assignCategoriesToBook(newBook, bookReqDTO.categories());
        if(bookReqDTO.tags() != null) {
            assignTagsToBook(newBook, bookReqDTO.tags().stream()
                    .map(TagReqDTO::tagName)
                    .toList());
        }

        bookRepository.save(newBook);
        log.debug("도서 저장 - ISBN: {}, Title: {}, Author: {}", newBook.getIsbn(), newBook.getTitle(), newBook.getAuthor());
    }

    //초기 데이터 넣기 가공된 올바른 데이터라고 가정
    @Override
    @Transactional
    public void registerBooks(List<BookReqDTO> bookReqDTOS) {

        Set<String> existingIsbns = bookRepository.findAllByIsbnIn(
                        bookReqDTOS.stream().map(BookReqDTO::isbn).toList())
                .stream().map(Book::getIsbn)
                .collect(Collectors.toSet());

        for(BookReqDTO b: bookReqDTOS){
            if (existingIsbns.contains(b.isbn())) {
                log.error("데이터베이스에 있는 ISBN을 등록 시도 - ISBN: {}", b.isbn());
                continue;
            }

            Book newBook = Book.create(b, publisherService.getPublisherRegisterBook(b.publisher()));

            assignCategoriesToBook(newBook, b.categories());
            assignTagsToBook(newBook, b.tags().stream()
                    .map(TagReqDTO::tagName)
                    .toList());

            bookRepository.save(newBook);
            log.debug("도서 저장(여러개 도서 저장중) - ISBN: {}, Title: {}, Author: {}", newBook.getIsbn(), newBook.getTitle(), newBook.getAuthor());
        }
    }

    @Override
    @Transactional
    public BookRespDTO findBookById(long bookId) {
        validateExistsById(bookId);

        Book book = bookRepository.findBookById(bookId);
        log.debug("도서ID로 조회 성공 - ISBN: {}, Title: {}, Author: {}", book.getIsbn(), book.getTitle(), book.getAuthor());

        return createdBookRespDTO(book);
    }

    @Override
    @Transactional
    public BookRespDTO findBookByIsbn(String isbn) {
        validateExistsByIsbn(isbn);

        Book book = bookRepository.findBookByIsbn(isbn);
        log.debug("도로ISBN으로 조회 성공 - ISBN: {}, Title: {}, Author: {}", book.getIsbn(), book.getTitle(), book.getAuthor());

        return createdBookRespDTO(book);
    }

    private BookRespDTO createdBookRespDTO(Book book){
//        List<CategoryRespDTO> categoryRespDTOS = categoryService.getCategoryDTOsByIds(book.getBookCategories().stream()
//                .map(bc -> bc.getCategory().getId())
//                .toList());

        List<CategoryRespDTO> categoryRespDTOS = book.getBookCategories().stream()
                .map(BookCategory::getCategory)
                .map(c -> new CategoryRespDTO(c.getId(), c.getName(), c.getDeep(), c.getPreCategory().getName()))
                .toList();

//        List<TagRespDTO> tagRespDTOS = tagService.findAllByIdIn(book.getBookTags().stream()
//                .map(bt -> bt.getTag().getId())
//                .toList());

        List<TagRespDTO> tagRespDTOS = book.getBookTags().stream()
                .map(BookTag::getTag)
                .map(t -> new TagRespDTO(t.getId(), t.getName()))
                .toList();

        return new BookRespDTO(book.getId(), book.getIsbn(), book.getTitle(), book.getIndex(), book.getDescription(), book.getAuthor(),
                book.getPublisher().getName(), book.getPublicationDate(), book.getPrice(), book.isPackaging(), book.getStock(), book.getStatus(),
                book.getImageUrl(), book.getVolumeNo(), categoryRespDTOS, tagRespDTOS);
    }

    @Override
    public List<BookRespDTO> findBooks(String categoryName, String tagName, String author, String publisher) {
        if(categoryName != null){
            return createdBookRespDTOs(bookRepository.findBooksByCategoryName(categoryName));
        } else if (tagName != null) {
            return createdBookRespDTOs(bookRepository.findBooksByTagName(tagName));
        } else if (author != null) {
            return createdBookRespDTOs(bookRepository.findAllByAuthor(author));
        } else if (publisher != null) {
            return createdBookRespDTOs(bookRepository.findAllByPublisher_Name(publisher));
        }

        return List.of();
    }

    private List<BookRespDTO> createdBookRespDTOs(List<Book> books){
        List<BookRespDTO> bookRespDTOS = new ArrayList<>();
        for(Book book:books) {
            bookRespDTOS.add(createdBookRespDTO(book));
        }
        return bookRespDTOS;
    }

    @Override
    @Transactional
    public void updateBook(long bookId, BookReqDTO bookReqDTO) {
        Book book = getBook_IdOrISBN(bookId, bookReqDTO.isbn(), "수정");

        //book 기본 필드값 수정사항
        if(!bookReqDTO.isbn().equals(book.getIsbn())){
            book.setIsbn(bookReqDTO.isbn());
        }
        if(!bookReqDTO.title().equals(book.getTitle())){
            book.setTitle(bookReqDTO.title());
        }
        if(!bookReqDTO.author().equals(book.getAuthor())){
            book.setAuthor(bookReqDTO.author());
        }
        if(!bookReqDTO.index().equals(book.getIndex())){
            book.setIndex(bookReqDTO.index());
        }
        if(!bookReqDTO.description().equals(book.getDescription())){
            book.setDescription(bookReqDTO.description());
        }
        if(!bookReqDTO.publisher().equals(book.getPublisher().getName())){
            book.setPublisher(publisherService.getPublisherRegisterBook(bookReqDTO.publisher()));
        }
        if(!bookReqDTO.publicationDate().equals(book.getPublicationDate())){
            book.setPublicationDate(bookReqDTO.publicationDate());
        }
        if(!bookReqDTO.price().equals(book.getPrice())){
            book.setPrice(bookReqDTO.price());
        }
        if(bookReqDTO.isPackaging() != book.isPackaging()){
            book.setPackaging(bookReqDTO.isPackaging());
        }
        if(!bookReqDTO.stock().equals(book.getStock())){
            book.setStock(bookReqDTO.stock());
        }
        if(!bookReqDTO.status().equals(book.getStatus())){
            book.setStatus(bookReqDTO.status());
        }

        //연결된 부분 수정
//        List<BookCategory> preBookCategories = bookCategoryRepository.findAllByBook_Id(book.getId());
        List<BookCategory> preBookCategories = book.getBookCategories();

        List<Category> updateCategories = categoryService.findCategoriesByNamesAndDeeps(bookReqDTO.categories().stream().map(CategoryReqDTO::categoryName).toList(),
                bookReqDTO.categories().stream().map(CategoryReqDTO::deep).toList());

//        List<Category> preCategories = categoryService.findAllByBookCategories(preBookCategories);
        List<Category> preCategories = preBookCategories.stream().map(BookCategory::getCategory).toList();

        for(Category updateC: updateCategories){
            for(Category preC: preCategories){
                if(updateC.getDeep() == preC.getDeep()){
                    if(updateC.getId() == preC.getId()){
                        continue;
                    }

                    log.debug("변경사항 - 이전 카테고리 - ID: {}, Name: {}, Deep: {}, preCategory: {}", preC.getId(), preC.getName(), preC.getDeep(), preC.getPreCategory().getName());
                    log.debug("변경사항 - 변경 카테고리 - ID: {}, Name: {}, Deep: {}, preCategory: {}", updateC.getId(), updateC.getName(), updateC.getDeep(), updateC.getPreCategory().getName());

                    for(BookCategory bc: preBookCategories){
                        if(bc.getCategory().getId() == preC.getId()){
                            preC.getBookCategories().removeIf(preBc -> preBc.getId() == bc.getId());
                            bc.setCategory(updateC);
                            updateC.getBookCategories().add(bc);
                            break;
                        }
                    }
                    break;
                }
            }
        }

//        List<BookTag> preBookTags = bookTagRepository.findAllByBook_Id(book.getId());
        List<BookTag> preBookTags = book.getBookTags();
//        List<Tag> preTags = tagService.findAllByBookTags(preBookTags);
        List<Tag> preTags = preBookTags.stream().map(BookTag::getTag).toList();

        Set<String> updateTagNames = bookReqDTO.tags().stream()
                .map(TagReqDTO::tagName)
                .collect(Collectors.toSet());

        List<Long> tagIdsToDelete = preTags.stream()
                .filter(preTag -> !updateTagNames.contains(preTag.getName()))
                .map(Tag::getId)
                .toList();

        if (!tagIdsToDelete.isEmpty()) {
            List<BookTag> bookTagsToDelete = bookTagRepository.findAllByBook_IdAndTag_IdIn(book.getId(), tagIdsToDelete);

            book.getBookTags().removeAll(bookTagsToDelete);
            //대량 삭제시 InBatch 유용 하지만 수동으로 flush와 clear가 필요
//            bookTagRepository.deleteAllInBatch(bookTagsToDelete);
            bookTagRepository.deleteAllById(bookTagsToDelete.stream().map(BookTag::getId).toList());
        }

        Set<String> preTagNames = preTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        List<String> assignTags = bookReqDTO.tags().stream()
                .map(TagReqDTO::tagName)
                .filter(updateTagName -> !preTagNames.contains(updateTagName))
                .toList();

        if (!assignTags.isEmpty()) {
            assignTagsToBook(book, assignTags);
        }
    }

    @Override
    @Transactional
    public void deleteBook(DeleteBookReqDTO deleteBookReqDTO) {
        Book book = getBook_IdOrISBN(deleteBookReqDTO.id(), deleteBookReqDTO.isbn(), "삭제");

//        List<BookCategory> bookCategories = bookCategoryRepository.findAllByBook_Id(book.getId());
        List<BookCategory> bookCategories = book.getBookCategories();
//        List<BookTag> bookTags = bookTagRepository.findAllByBook_Id(book.getId());
        List<BookTag> bookTags = book.getBookTags();

//        List<Long> bookCategoriesId =bookCategories.stream().map(bc -> bc.getCategory().getId()).toList();
//        List<Long> bookTagsId = bookTags.stream().map(bt -> bt.getTag().getId()).toList();

//        List<Category> categories = categoryService.findCategoriesByIds(bookCategoriesId);
        List<Category> categories = bookCategories.stream().map(BookCategory::getCategory).toList();
//        List<Tag> tags = tagService.getAllByIdIn(bookTagsId);
        List<Tag> tags = bookTags.stream().map(BookTag::getTag).toList();

//        for(Category c: categories){
//            c.getBookCategories().removeAll(bookCategories);
//        }
//        for(Tag t: tags){
//            t.getBookTags().removeAll(bookTags);
//        }
        categories.forEach(c -> c.getBookCategories().removeAll(bookCategories));
        tags.forEach(t -> t.getBookTags().removeAll(bookTags));

//        bookCategoryRepository.deleteBookCategoriesByIdIn(bookCategoriesId);
//        bookTagRepository.deleteBookTagsByIdIn(bookTagsId);
        bookCategoryRepository.deleteBookCategoriesByIdIn(bookCategories.stream()
                .map(bc -> bc.getCategory().getId())
                .toList());
        bookTagRepository.deleteBookTagsByIdIn(bookTags.stream()
                .map(bt -> bt.getTag().getId())
                .toList());

        book.getPublisher().getBookList().remove(book);

        bookRepository.delete(book);
        log.debug("도서 제거 - ISBN: {}, Title: {}, Author: {}", book.getIsbn(), book.getTitle(), book.getAuthor());
    }

    private Book getBook_IdOrISBN(long id, String isbn, String methodName){
        Book book = null;

        if(id != 0){
            validateExistsById(id);
            book = bookRepository.findBookById(id);
        } else if(isbn != null){
            validateExistsByIsbn(isbn);
            book = bookRepository.findBookByIsbn(isbn);
        }

        if(book == null){
            log.error("{} 할 도서 찾기 실패 - ID: {}, ISBN: {}", methodName, id, isbn);
            throw new NotFoundBook(methodName + " 할 도서을 찾지 못 했습니다.");
        }

        return book;
    }

    @Override
    public List<Book> getBooksByUser(List<Long> bookIds) {
        return bookTagRepository.findAllByIdIn(bookIds);
    }

    @Override
    public Book getBookById(long bookId) {
        return bookRepository.findBookById(bookId);
    }
}
