package com.daisobook.shop.booksearch.BooksSearch.service.book.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.AuthorReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.CategoryReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.TagReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.service.ImagesReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.author.Author;
import com.daisobook.shop.booksearch.BooksSearch.entity.author.BookAuthor;
import com.daisobook.shop.booksearch.BooksSearch.entity.author.Role;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.BookImage;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.BookCategory;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.Category;
import com.daisobook.shop.booksearch.BooksSearch.entity.tag.BookTag;
import com.daisobook.shop.booksearch.BooksSearch.entity.tag.Tag;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.book.DuplicatedBook;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.book.NotFoundBookISBN;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.book.NotFoundBookId;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.category.NotFoundCategoryName;
import com.daisobook.shop.booksearch.BooksSearch.repository.BookOfTheWeekRepository;
import com.daisobook.shop.booksearch.BooksSearch.repository.author.BookAuthorRepository;
import com.daisobook.shop.booksearch.BooksSearch.repository.book.BookRepository;
import com.daisobook.shop.booksearch.BooksSearch.repository.category.BookCategoryRepository;
import com.daisobook.shop.booksearch.BooksSearch.repository.tag.BookTagRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.author.AuthorService;
import com.daisobook.shop.booksearch.BooksSearch.service.category.CategoryService;
import com.daisobook.shop.booksearch.BooksSearch.service.image.impl.BookImageServiceImpl;
import com.daisobook.shop.booksearch.BooksSearch.service.like.LikeService;
import com.daisobook.shop.booksearch.BooksSearch.service.policy.DiscountPolicyService;
import com.daisobook.shop.booksearch.BooksSearch.service.publisher.PublisherService;
import com.daisobook.shop.booksearch.BooksSearch.service.review.ReviewService;
import com.daisobook.shop.booksearch.BooksSearch.service.tag.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookCoreService {
    private final LikeService likeService;
    private final ReviewService reviewService;

    @Value("${app.batch.size}")
    private int BATCH_SIZE;

    private final int MAX_SIZE = 1000;

    private final BookRepository bookRepository;
    private final BookCategoryRepository bookCategoryRepository;
    private final CategoryService categoryService;

    private final BookTagRepository bookTagRepository;
    private final TagService tagService;

    private final PublisherService publisherService;

    private final BookAuthorRepository bookAuthorRepository;
    private final AuthorService authorService;

    private final BookImageServiceImpl imageService;

    private final BookOfTheWeekRepository bookOfTheWeekRepository;
    private final DiscountPolicyService discountPolicyService;

    @Transactional
    public void validateExistsById(long bookId) {
        if(!bookRepository.existsBookById(bookId)) {
            log.error("존재하지 않은 도서ID: {}", bookId);
            throw new NotFoundBookId("존재하지 않은 도서ID 입니다.");
        }
    }

    @Transactional
    public void validateExistsByIsbn(String isbn) {
        if(!bookRepository.existsBookByIsbn(isbn)) {
            log.error("존재하지 않은 ISBN: {}", isbn);
            throw new NotFoundBookISBN("존재하지 않은 ISBN 입니다.");
        }
    }

    @Transactional
    public void validateNotExistsByIsbn(String isbn) {
        if(bookRepository.existsBookByIsbn(isbn)) {
            log.error("이미 존재하는 ISBN: {}", isbn);
            throw new DuplicatedBook("이미 존재하는 ISBN 입니다.");
        }
    }

    @Transactional
    public void assignCategoriesToBook(Book book, List<CategoryReqDTO> categories) {
        if(categories == null || categories.isEmpty()){
            return;
        }

        Map<String, Category> categoryMap = categoryService.findCategoriesByNamesAndDeeps(categories.stream()
                                .map(CategoryReqDTO::categoryName)
                                .toList(),
                        categories.stream()
                                .map(CategoryReqDTO::deep).
                                toList()).stream()
                .collect(Collectors.toMap(Category::getName, category -> category));

        for(CategoryReqDTO c: categories) {
//            Category category = categoryService.findValidCategoryByNameAndDeep(c.categoryName(), c.deep());
            Category category = categoryMap.get(c.categoryName());
            if(category == null){
                log.error("존재하지 않는 카테고리입니다 - 요청한 카테고리:{}", c.categoryName());
                throw new NotFoundCategoryName("존재하지 않는 카테고리입니다");
            }

            BookCategory bookCategory = new BookCategory(book, category);

            category.getBookCategories().add(bookCategory);
            book.getBookCategories().add(bookCategory);

//            bookCategoryRepository.save(bookCategory);
        }
    }

    @Transactional
    public void assignTagsToBook(Book book, List<String> tagNames) {

        Map<String, Tag> tagMap = tagService.findAllByNameIn(tagNames).stream()
                .collect(Collectors.toMap(Tag::getName, tag -> tag));

        for(String t: tagNames){
//            Tag tag = tagService.findTagByName(t);
            Tag tag = tagMap.get(t);
            if(tag == null){
                tag = new Tag(t);
            }

            BookTag bookTag = new BookTag(book, tag);

            tag.getBookTags().add(bookTag);
            book.getBookTags().add(bookTag);

//            bookTagRepository.save(bookTag);
        }
    }

    public void assignAuthorToBook(Book book, List<AuthorReqDTO> authorReqDTOs) {
        Map<String, Author> authorMap = authorService.findAuthorsByNameIn(authorReqDTOs.stream()
                        .map(AuthorReqDTO::authorName)
                        .toList()).stream()
                .collect(Collectors.toMap(Author::getName, author -> author));

        Map<String, Role> roleMap = authorService.findRolesByNameIn(authorReqDTOs.stream()
                        .map(AuthorReqDTO::roleName)
                        .toList()).stream()
                .collect(Collectors.toMap(Role::getName, role -> role));

        for(AuthorReqDTO a: authorReqDTOs){
            Author author = authorMap.get(a.authorName());
            if(author == null){
                author = new Author(a.authorName());
            }

            Role role = roleMap.get(a.roleName());

            BookAuthor newBookAuthor = new BookAuthor(book, author);
            book.getBookAuthors().add(newBookAuthor);
            author.getBookAuthors().add(newBookAuthor);

            if(role != null){
                newBookAuthor.setRole(role);
                role.getBookAuthors().add(newBookAuthor);
            }
        }
    }

    public void assignImages(Book book, List<ImageMetadataReqDTO> dto, Map<String, MultipartFile> fileMap) {
        ImagesReqDTO reqDTO = new ImagesReqDTO(book.getId(), dto);
        List<BookImage> bookImages = imageService.addBookImage(reqDTO, fileMap);
        bookImages.forEach(bi -> bi.setBook(book));
        book.setBookImages(bookImages);
    }

    @Transactional
    public void registerBook(BookReqDTO bookReqDTO, Map<String, MultipartFile> fileMap) {
        validateNotExistsByIsbn(bookReqDTO.isbn());

        Book newBook = Book.create(bookReqDTO, publisherService.getPublisherRegisterBook(bookReqDTO.publisher()));

        assignCategoriesToBook(newBook, bookReqDTO.categories());
        if(bookReqDTO.tags() != null) {
            assignTagsToBook(newBook, bookReqDTO.tags().stream()
                    .map(TagReqDTO::tagName)
                    .toList());
        }

        bookRepository.save(newBook);
        assignImages(newBook, bookReqDTO.imageMetadataReqDTOList(), fileMap);
        assignAuthorToBook(newBook, bookReqDTO.authorReqDTOList());
        log.debug("도서 저장 - ISBN: {}, Title: {}, Author: {}", newBook.getIsbn(), newBook.getTitle(),
                newBook.getBookAuthors().stream()
                        .map(ba -> ba.getAuthor().getName() + ba.getRole().getName())
                        .toList());
    }

//    @Transactional
//    public void registerBook(Book book) {
//        validateNotExistsByIsbn(bookReqDTO.isbn());
//
//        Book newBook = Book.create(bookReqDTO, publisherService.getPublisherRegisterBook(bookReqDTO.publisher()));
//
//        assignCategoriesToBook(newBook, bookReqDTO.categories());
//        if(bookReqDTO.tags() != null) {
//            assignTagsToBook(newBook, bookReqDTO.tags().stream()
//                    .map(TagReqDTO::tagName)
//                    .toList());
//        }
//
//        bookRepository.save(newBook);
//        assignImages(newBook, bookReqDTO.imageMetadataReqDTOList(), fileMap);
//        assignAuthorToBook(newBook, bookReqDTO.authorReqDTOList());
//        log.debug("도서 저장 - ISBN: {}, Title: {}, Author: {}", newBook.getIsbn(), newBook.getTitle(),
//                newBook.getBookAuthors().stream()
//                        .map(ba -> ba.getAuthor().getName() + ba.getRole().getName())
//                        .toList());
//    }
}
