package com.daisobook.shop.booksearch.BooksSearch.service.book.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.*;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.BookRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.CategoryRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.TagRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.*;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.*;
import com.daisobook.shop.booksearch.BooksSearch.repository.*;
import com.daisobook.shop.booksearch.BooksSearch.service.book.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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

            Category category = categoryRepository.findCategoryByNameAndDeep(c.categoryName(), c.deep());

            if(category == null){
                log.error("존재하지 않는 카테고리 도서 등록 시도 - category name: {}, deep: {}", c.categoryName(), c.deep());
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
        log.debug("도서 저장 - ISBN: {}, Title: {}, Author: {}", newBook.getIsbn(), newBook.getTitle(), newBook.getAuthor());
    }

    //초기 데이터 넣기 가공된 올바른 데이터라고 가정
    @Override
    @Transactional
    public void registerBooks(List<AddBookReqDTO> addBookReqDTOS) {
        List<String> isbns = bookRepository.findAllByIsbnIn(addBookReqDTOS.stream().map(AddBookReqDTO::isbn).toList())
                .stream().map(Book::getIsbn).toList();

        boolean nullCheck = CollectionUtils.isEmpty(isbns);

        for(AddBookReqDTO b: addBookReqDTOS){
            if(!nullCheck){
                boolean index = false;
                for(String isbn: isbns){
                    if(isbn.equals(b.isbn())){
                        index = true;
                        break;
                    }
                }

                if(index){
                    log.error("데이터베이스에 있는 ISBN을 등록 시도 - ISBN: {}", b.isbn());
                    continue;
                }
            }

            Book newBook = Book.create(b);

            assignCategoriesToBook(newBook, b.categories());
            assignTagsToBook(newBook, b.tags());

            bookRepository.save(newBook);
            log.debug("도서 저장(여러개 도서 저장중) - ISBN: {}, Title: {}, Author: {}", newBook.getIsbn(), newBook.getTitle(), newBook.getAuthor());
        }
    }

    @Override
    @Transactional
    public BookRespDTO findBookById(long id) {
        validateExistsById(id);

        Book book = bookRepository.findBookById(id);
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
    @Transactional
    public List<BookRespDTO> findBooksByCategory(String categoryName) {
        long categoryId = categoryRepository.findCategoryByName(categoryName).getId();
        long bookId = bookCategoryRepository.findAllById(categoryId).getBook().getId();

        return createdBookRespDTOs(bookRepository.findAllById(bookId));
    }

    @Override
    @Transactional
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
    @Transactional
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

    @Override
    @Transactional
    public void updateBook(UpdateBookReqDTO updateBookReqDTO) {
        Book book = getBook_IdOrISBN(updateBookReqDTO.id(), updateBookReqDTO.isbn(), "수정");

        //book 기본 필드값 수정사항
        if(!updateBookReqDTO.isbn().equals(book.getIsbn())){
            book.setIsbn(updateBookReqDTO.isbn());
        }
        if(!updateBookReqDTO.title().equals(book.getTitle())){
            book.setTitle(updateBookReqDTO.title());
        }
        if(!updateBookReqDTO.author().equals(book.getAuthor())){
            book.setAuthor(updateBookReqDTO.author());
        }
        if(!updateBookReqDTO.index().equals(book.getIndex())){
            book.setIndex(updateBookReqDTO.index());
        }
        if(!updateBookReqDTO.description().equals(book.getDescription())){
            book.setDescription(updateBookReqDTO.description());
        }
        if(!updateBookReqDTO.publisher().equals(book.getPublisher())){
            book.setPublisher(updateBookReqDTO.publisher());
        }
        if(!updateBookReqDTO.publicationDate().equals(book.getPublicationDate())){
            book.setPublicationDate(updateBookReqDTO.publicationDate());
        }
        if(updateBookReqDTO.price() != book.getPrice()){
            book.setPrice(updateBookReqDTO.price());
        }
        if(updateBookReqDTO.isPackaging() != book.isPackaging()){
            book.setPackaging(updateBookReqDTO.isPackaging());
        }
        if(!updateBookReqDTO.stock().equals(book.getStock())){
            book.setStock(updateBookReqDTO.stock());
        }
        if(!updateBookReqDTO.status().equals(book.getStatus())){
            book.setStatus(updateBookReqDTO.status());
        }

        //연결된 부분 수정
        List<BookCategory> updateBookCategories = bookCategoryRepository.findAllByBook_Id(updateBookReqDTO.id());
        List<BookTag> updateBookTags = bookTagRepository.findAllByBook_Id(updateBookReqDTO.id());

        List<BookCategory> preBookCategories = bookCategoryRepository.findAllByBook_Id(book.getId());
        List<BookTag> preBookTags = bookTagRepository.findAllByBook_Id(book.getId());

        for(BookCategory updateBC: updateBookCategories){
            for(BookCategory prBC: preBookCategories){

            }
        }

        for(BookTag updateBT: updateBookTags){
            for(BookTag preBT: preBookTags){

            }
        }
    }

    @Override
    @Transactional
    public void deleteBook(DeleteBookReqDTO deleteBookReqDTO) {
        Book book = getBook_IdOrISBN(deleteBookReqDTO.id(), deleteBookReqDTO.isbn(), "삭제");

        List<BookCategory> bookCategories = bookCategoryRepository.findAllByBook_Id(book.getId());
        List<BookTag> bookTags = bookTagRepository.findAllByBook_Id(book.getId());

        List<Long> bookCategoriesId =bookCategories.stream().map(bc -> bc.getCategory().getId()).toList();
        List<Long> bookTagsId = bookTags.stream().map(bt -> bt.getTag().getId()).toList();

        List<Category> categories = categoryRepository.findAllByIdIn(bookCategoriesId);
        List<Tag> tags = tagRepository.findAllByIdIn(bookTagsId);

        for(Category c: categories){
            c.getBookCategories().removeAll(bookCategories);
        }
        for(Tag t: tags){
            t.getBookTags().removeAll(bookTags);
        }

        bookCategoryRepository.deleteBookCategoriesByIdIn(bookCategoriesId);
        bookTagRepository.deleteBookTagsByIdIn(bookTagsId);

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
}
