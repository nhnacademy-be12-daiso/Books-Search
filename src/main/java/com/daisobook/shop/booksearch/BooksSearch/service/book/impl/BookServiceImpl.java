package com.daisobook.shop.booksearch.BooksSearch.service.book.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.AddBookReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.AddCategoryReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.AddTagReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.BookRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.CategoryRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.TagRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.*;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.DuplicatedBookISBN;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.NotFoundBookCategory;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.NotFoundBookISBN;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.NotFoundBookId;
import com.daisobook.shop.booksearch.BooksSearch.repository.*;
import com.daisobook.shop.booksearch.BooksSearch.service.book.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookCategoryRepository bookCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final BookTagRepository bookTagRepository;

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
            throw new DuplicatedBookISBN("이미 존재하는 ISBN 입니다.");
        }
    }

    @Override
    @Transactional
    public void assignCategoriesToBook(Book book, List<AddCategoryReqDTO> categories) {

        for(AddCategoryReqDTO c: categories) {

            Category category = categoryRepository.findCategoryByName(c.categoryName());

            if(category == null){
                log.error("존재하지 않는 카테고리 도서 등록 시도 - category name :{}", c.categoryName());
                throw new NotFoundBookCategory("존재하지 않는 카테고리 입니다");
            }

            BookCategory bookCategory = new BookCategory(book, category);

            category.getBookCategories().add(bookCategory);
            book.getBookCategories().add(bookCategory);

            bookCategoryRepository.save(bookCategory);
        }
    }

    @Override
    @Transactional
    public void assignTagsToBook(Book book, List<AddTagReqDTO> tags) {

        for(AddTagReqDTO t: tags){

            Tag tag = tagRepository.findTagByName(t.tagName());
            if(tag == null){
                tag = new Tag(t.tagName());
            }

            BookTag bookTag = new BookTag(book, tag);

            tag.getBookTags().add(bookTag);
            book.getBookTags().add(bookTag);

            tagRepository.save(tag);
            bookTagRepository.save(bookTag);
        }
    }

    @Override
    @Transactional
    public void registerBook(AddBookReqDTO addBookReqDTO) {
        validateNotExistsByIsbn(addBookReqDTO.isbn());

        Book newBook = Book.create(addBookReqDTO);

        assignCategoriesToBook(newBook, addBookReqDTO.categories());
        assignTagsToBook(newBook, addBookReqDTO.tags());

        bookRepository.save(newBook);
    }

    //초기 데이터 넣기 가공된 올바른 데이터라고 가정
    @Override
    @Transactional
    public void registerBooks(List<AddBookReqDTO> addBookReqDTOS) {
        for(AddBookReqDTO b: addBookReqDTOS){
            Book newBook = Book.create(b);

            assignCategoriesToBook(newBook, b.categories());
            assignTagsToBook(newBook, b.tags());

            bookRepository.save(newBook);
        }
    }

    @Override
    @Transactional
    public BookRespDTO findBookById(long id) {
        validateExistsById(id);

        Book book = bookRepository.findBookById(id);

        return createdBookRespDTO(book);
    }

    @Override
    @Transactional
    public BookRespDTO findBookByIsbn(String isbn) {
        validateExistsByIsbn(isbn);

        Book book = bookRepository.findBookByIsbn(isbn);

        return createdBookRespDTO(book);
    }

    private BookRespDTO createdBookRespDTO(Book book){
        List<Category> categories = new ArrayList<>();
        List<Tag> tags = new ArrayList<>();

        for(BookCategory c: book.getBookCategories()){
            categories.add(categoryRepository.findCategoryById(c.getCategory().getId()));
        }
        for(BookTag t: book.getBookTags()){
            tags.add(tagRepository.findTagById(t.getTag().getId()));
        }

        List<CategoryRespDTO> categoryRespDTOS = categories.stream().map(c -> new CategoryRespDTO(c.getId(), c.getName(), c.getDeep())).toList();
        List<TagRespDTO> tagRespDTOS = tags.stream().map(t -> new TagRespDTO(t.getId(), t.getName())).toList();

        return new BookRespDTO(book.getId(), book.getIsbn(), book.getTitle(), book.getIndex(), book.getDescription(), book.getAuthor(),
                book.getPublisher(), book.getPublicationDate(), book.getPrice(), book.isPackaging(), book.getStock(), book.getStatus(),
                categoryRespDTOS, tagRespDTOS);
    }

    @Override
    public List<BookRespDTO> findBooksByCategory(String categoryName) {
        long categoryId = categoryRepository.findCategoryByName(categoryName).getId();
        long bookId = bookCategoryRepository.findAllById(categoryId).getBook().getId();

        return createdBookRespDTOs(bookRepository.findAllById(bookId));
    }

    @Override
    public List<BookRespDTO> findBooksByTag(String tagName) {
        long tagId = tagRepository.findTagByName(tagName).getId();
        long bookId = bookTagRepository.findAllById(tagId).getBook().getId();

        return createdBookRespDTOs(bookRepository.findAllById(bookId));
    }

    @Override
    public List<BookRespDTO> findBooksByAuthor(String author) {
        return createdBookRespDTOs(bookRepository.findAllByAuthor(author));
    }

    @Override
    public List<BookRespDTO> findBooksByPublisher(String publisher) {
        return createdBookRespDTOs(bookRepository.findAllByPublisher(publisher));
    }

    private List<BookRespDTO> createdBookRespDTOs(List<Book> books){
        List<Category> categories = new ArrayList<>();
        List<Tag> tags = new ArrayList<>();

        for(Book b: books){
            for(BookCategory c: b.getBookCategories()){
                categories.add(categoryRepository.findCategoryById(c.getCategory().getId()));
            }

            for(BookTag t: b.getBookTags()){
                tags.add(tagRepository.findTagById(t.getTag().getId()));
            }
        }

        return books.stream()
                .map(b -> new BookRespDTO(b.getId(), b.getIsbn(), b.getTitle(), b.getIndex(), b.getDescription(), b.getAuthor(),
                        b.getPublisher(), b.getPublicationDate(), b.getPrice(), b.isPackaging(), b.getStock(), b.getStatus(),
                        categories.stream().map(c -> new CategoryRespDTO(c.getId(), c.getName(), c.getDeep())).toList(),
                        tags.stream().map(t -> new TagRespDTO(t.getId(), t.getName())).toList()))
                .toList();
    }
}
