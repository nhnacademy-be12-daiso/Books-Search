package com.daisobook.shop.booksearch.BooksSearch.service.book.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.coupon.response.BookCategoryResponse;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.DiscountValueProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.*;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookGroupReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.order.BookOrderDetailRequest;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.review.ReviewReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.*;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.HomeBookListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.BookResponse;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.BookReviewResponse;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.OrderBooksInfoRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.service.ImagesReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.*;
import com.daisobook.shop.booksearch.BooksSearch.entity.author.Author;
import com.daisobook.shop.booksearch.BooksSearch.entity.author.BookAuthor;
import com.daisobook.shop.booksearch.BooksSearch.entity.author.Role;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.BookImage;
import com.daisobook.shop.booksearch.BooksSearch.entity.policy.DiscountPolicy;
import com.daisobook.shop.booksearch.BooksSearch.entity.policy.DiscountType;
import com.daisobook.shop.booksearch.BooksSearch.entity.publisher.Publisher;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.BookCategory;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.Category;
import com.daisobook.shop.booksearch.BooksSearch.entity.review.Review;
import com.daisobook.shop.booksearch.BooksSearch.entity.tag.BookTag;
import com.daisobook.shop.booksearch.BooksSearch.entity.tag.Tag;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.book.DuplicatedBook;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.book.NotFoundBook;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.book.NotFoundBookISBN;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.book.NotFoundBookId;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.category.NotFoundCategoryName;
import com.daisobook.shop.booksearch.BooksSearch.mapper.book.BookMapper;
import com.daisobook.shop.booksearch.BooksSearch.repository.*;
import com.daisobook.shop.booksearch.BooksSearch.repository.author.BookAuthorRepository;
import com.daisobook.shop.booksearch.BooksSearch.repository.book.BookRepository;
import com.daisobook.shop.booksearch.BooksSearch.repository.category.BookCategoryRepository;
import com.daisobook.shop.booksearch.BooksSearch.repository.tag.BookTagRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.author.AuthorService;
import com.daisobook.shop.booksearch.BooksSearch.service.book.BookService;
import com.daisobook.shop.booksearch.BooksSearch.service.category.CategoryService;
import com.daisobook.shop.booksearch.BooksSearch.service.image.impl.BookImageServiceImpl;
import com.daisobook.shop.booksearch.BooksSearch.service.like.LikeService;
import com.daisobook.shop.booksearch.BooksSearch.service.policy.DiscountPolicyService;
import com.daisobook.shop.booksearch.BooksSearch.service.publisher.PublisherService;
import com.daisobook.shop.booksearch.BooksSearch.service.review.ReviewService;
import com.daisobook.shop.booksearch.BooksSearch.service.tag.TagService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

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

    private final ObjectMapper objectMapper;
    private final BookMapper bookMapper;

    @Override
    public BookGroupReqDTO parsing(BookMetadataReqDTO dto) throws JsonProcessingException {
        if(dto == null){
            throw new RuntimeException("null");
        }

        final int MAX_FILE_COUNT = 5;

        BookReqDTO metadata = objectMapper.readValue(dto.metadata(), BookReqDTO.class);
        Map<String, MultipartFile> files = new HashMap<>();
        Class<?> clazz = dto.getClass();

        for(int i = 0; i < MAX_FILE_COUNT; i++) {
            String key = "image%d".formatted(i);
            try {
                // DTO에서 필드를 찾아 접근 권한 설정
                Field field = clazz.getDeclaredField(key);
                field.setAccessible(true);

                // DTO 인스턴스에서 해당 필드의 값(MultipartFile) 추출
                MultipartFile file = (MultipartFile) field.get(dto);

                // 파일이 비어있지 않은 경우에만 Map에 추가 (Key는 "image0", "image1"...)
                if (file != null && !file.isEmpty()) {
                    files.put(key, file);
                }
            } catch (NoSuchFieldException e) {
                // 필드가 없으면 종료
                break;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return new BookGroupReqDTO(metadata, files);
    }

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

    @Override
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

    @Override
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

    @Override
    public void assignImages(Book book, List<ImageMetadataReqDTO> dto, Map<String, MultipartFile> fileMap) {
        ImagesReqDTO reqDTO = new ImagesReqDTO(book.getId(), dto);
        List<BookImage> bookImages = imageService.addBookImage(book, reqDTO, fileMap);
        bookImages.forEach(bi -> bi.setBook(book));
        book.setBookImages(bookImages);
    }

    @Override
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

    //초기 데이터 넣기 가공된 올바른 데이터라고 가정
    @Override
    @Transactional
    public void registerBooks(List<BookReqDTO> bookReqDTOS) {

        Set<String> existingIsbns = bookRepository.findAllByIsbnIn(
                        bookReqDTOS.stream().map(BookReqDTO::isbn).toList())
                .stream().map(Book::getIsbn)
                .collect(Collectors.toSet());

        int count = 0;
        List<Book> addBooks = new ArrayList<>();
        List<List<ImageMetadataReqDTO>> imageMetadataReqDTOs = new ArrayList<>();
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
            assignAuthorToBook(newBook, b.authorReqDTOList());
            log.debug("도서 저장(여러개 도서 저장중) - ISBN: {}, Title: {}, Author: {}", newBook.getIsbn(), newBook.getTitle(),
                    newBook.getBookAuthors().stream()
                            .map(ba -> ba.getAuthor().getName() + ba.getRole().getName())
                            .toList());

            addBooks.add(newBook);
            imageMetadataReqDTOs.add(b.imageMetadataReqDTOList());
            count++;
            if(count >= MAX_SIZE){
                List<Book> books = bookRepository.saveAll(addBooks);
                List<ImagesReqDTO> imagesReqDTOsForProcess = new ArrayList<>();
                for(int i = 0; i < books.size(); i++){
                    imagesReqDTOsForProcess.add(new ImagesReqDTO(books.get(i).getId(), imageMetadataReqDTOs.get(i)));
                }

                Map<Long, List<BookImage>> newImagesListMap = imageService.addBookImages(null, imagesReqDTOsForProcess);

                for (Book book: books) {
                    newImagesListMap.get(book.getId()).forEach(bookImage -> bookImage.setBook(book));
                }

                count = 0;
            }
        }
    }

    @Override
    @Transactional
    public BookRespDTO findBookById(long bookId, Long userId) {
        validateExistsById(bookId);

        Book book = bookRepository.findBookById(bookId);
        log.debug("도서ID로 조회 성공 - ISBN: {}, Title: {}, Author: {}", book.getIsbn(), book.getTitle(),
                book.getBookAuthors().stream()
                        .map(ba -> ba.getAuthor().getName() + ba.getRole().getName())
                        .toList());

        return createdBookRespDTO(book, userId, bookDiscountPrice(book));
    }

    @Override
    @Transactional
    public BookRespDTO findBookByIsbn(String isbn, Long userId) {
        validateExistsByIsbn(isbn);

        Book book = bookRepository.findBookByIsbn(isbn);
        log.debug("도서ISBN으로 조회 성공 - ISBN: {}, Title: {}, Author: {}", book.getIsbn(), book.getTitle(),
                book.getBookAuthors().stream()
                        .map(ba -> ba.getAuthor().getName() + ba.getRole().getName())
                        .toList());

        return createdBookRespDTO(book, userId, bookDiscountPrice(book));
    }

    private BookRespDTO createdBookRespDTO(Book book, Long userId, Long discountPrice){
//        List<CategoryRespDTO> categoryRespDTOS = categoryService.getCategoryDTOsByIds(book.getBookCategories().stream()
//                .map(bc -> bc.getCategory().getId())
//                .toList());

        List<CategoryRespDTO> categoryRespDTOS = book.getBookCategories().stream()
                .map(BookCategory::getCategory)
                .map(c -> new CategoryRespDTO(c.getId(), c.getName(), c.getDeep(),
                        c.getPreCategory() != null ? c.getPreCategory().getId() : null,
                        c.getPreCategory() != null ? c.getPreCategory().getName() : null))
                .toList();

//        List<TagRespDTO> tagRespDTOS = tagService.findAllByIdIn(book.getBookTags().stream()
//                .map(bt -> bt.getTag().getId())
//                .toList());

        List<TagRespDTO> tagRespDTOS = book.getBookTags().stream()
                .map(BookTag::getTag)
                .map(t -> new TagRespDTO(t.getId(), t.getName()))
                .toList();

        List<ImageRespDTO> imageRespDTOS = book.getBookImages().stream()
                .map(bi -> new ImageRespDTO(bi.getNo(), bi.getPath(), bi.getImageType()))
                .toList();

        int count = likeService.likeCount(book.getId());
        boolean check = likeService.likeCheck(book.getId(), userId);

        List<ReviewRespDTO> reviews = reviewService.getReviewsByBookId(book.getId());

        BigDecimal i = discountPrice != null && book.getPrice() != null ? BigDecimal.valueOf((1.0 - (double) discountPrice / book.getPrice()) * 100.0): null;

        return new BookRespDTO(book.getId(), book.getIsbn(), book.getTitle(), book.getIndex(), book.getDescription(),
                book.getBookAuthors().stream()
                        .map(ba ->
                                new AuthorRespDTO(ba.getAuthor() != null ? ba.getAuthor().getId() : null,
                                        ba.getAuthor() != null ? ba.getAuthor().getName() : null,
                                        ba.getRole() != null ? ba.getRole().getId() : null,
                                        ba.getRole() != null ? ba.getRole().getName() : null))
                        .toList(),
                book.getPublisher().getName(), book.getPublicationDate(), book.getPrice(), i != null ? i.setScale(2, RoundingMode.DOWN) : null,
                discountPrice, book.isPackaging(), book.getStock(), book.getStatus(), imageRespDTOS, book.getVolumeNo(), categoryRespDTOS,
                tagRespDTOS, count, check, reviews);
    }

    @Override
    @Transactional
    public List<BookRespDTO> findBooks(String categoryName, String tagName, String authorName, String publisherName) {
        if(categoryName != null){
            Category category = categoryService.findCategoryByName(categoryName);
            List<Book> bookList = category.getBookCategories().stream()
                    .map(BookCategory::getBook)
                    .toList();

            return createdBookRespDTOs(bookList);
        } else if (tagName != null) {
            Tag tag = tagService.findTagByName(tagName);
            List<Book> bookList = tag.getBookTags().stream()
                    .map(BookTag::getBook)
                    .toList();

            return createdBookRespDTOs(bookList);
        } else if (authorName != null) {
            Author author = authorService.findAuthorByName(authorName);
            List<Book> bookList = author.getBookAuthors().stream()
                    .map(BookAuthor::getBook)
                    .toList();

            return createdBookRespDTOs(bookList);
        } else if (publisherName != null) {
            Publisher publisher = publisherService.getPublisherByName(publisherName);
            List<Book> bookList = publisher.getBookList();

            return createdBookRespDTOs(bookList);
        }

        return List.of();
    }

    @Override
    @Transactional
    public List<BookListRespDTO> getBooksByIdIn(List<Long> bookIds) {
        List<Book> bookList = bookRepository.findAllByIdIn(bookIds);
        if(bookList == null || bookList.isEmpty()){
            return List.of();
        }
        return createdBookListRespDTOs(bookList, null);
    }

    @Override
    @Transactional
    public OrderBooksInfoRespDTO findBooksByIdIn(List<Long> bookIds) {
        List<Book> bookList = bookRepository.findAllByIdIn(bookIds);
        if(bookList == null || bookList.isEmpty()){
            return null;
        }

        Map<Long, Long> discountPriceMap = new HashMap<>();
        for(Book book: bookList){
            Long discountPrice = bookDiscountPrice(book);
            discountPriceMap.put(book.getId(), discountPrice);
        }

        return bookMapper.toOrderBookInfoRespDTOList(bookList, discountPriceMap);
    }

    @Override
    @Transactional
    public HomeBookListRespDTO getHomeBookLists(Long userId) {
        Map<String, List<BookListRespDTO>> bookLists = new HashMap<>();

        String newReleases = "newReleases"; //신간도서
        String bookOfTheWeek = "bookOfTheWeek"; //금주 추천 도서

        //신간 도서
        Sort sort = Sort.by(Sort.Direction.DESC, "publicationDate");
        List<Book> bookList = bookRepository.findAllByPublicationDateAfterOrderByPublicationDateDesc(
                LocalDate.now().minusMonths(240), sort, Limit.of(10));

        if(bookList == null || bookList.isEmpty()){
            bookLists.put(newReleases, List.of());
        } else {
            bookLists.put(newReleases, createdBookListRespDTOs(bookList, userId));
        }

        //금주 추천 도서
        sort = Sort.by(Sort.Direction.ASC, "no");
        List<BookOfTheWeek> bookOfTheWeeks = bookOfTheWeekRepository.findAllByIsActiveOrderByAppliedDateDesc(
                true, sort, Limit.of(10));

        if(bookOfTheWeeks == null || bookOfTheWeeks.isEmpty()){
            bookLists.put(newReleases, List.of());
        } else {
            Map<Long, Book> bookMap = bookRepository.findAllByIdIn(bookOfTheWeeks.stream()
                        .map(BookOfTheWeek::getBookId)
                        .toList()).stream()
                    .collect(Collectors.toMap(Book::getId, b -> b));

            List<Book> bookList1 = bookOfTheWeeks.stream()
                    .map(bo -> bookMap.get(bo.getBookId()))
                    .filter(Objects::nonNull)
                    .toList();

            bookLists.put(bookOfTheWeek, createdBookListRespDTOs(bookList1, userId));
        }

        return new HomeBookListRespDTO(bookLists);
    }

    private List<BookRespDTO> createdBookRespDTOs(List<Book> books){
        List<BookRespDTO> bookRespDTOS = new ArrayList<>();
        for(Book book:books) {
            bookRespDTOS.add(createdBookRespDTO(book, null, bookDiscountPrice(book)));
        }
        return bookRespDTOS;
    }

    private BookListRespDTO createdBooksRespDTO(Book book, Long discount, Boolean isLike){
        List<ImageRespDTO> imageRespDTOS = book.getBookImages().stream()
                .map(bi -> new ImageRespDTO(bi.getNo(), bi.getPath(), bi.getImageType()))
                .toList();

        List<CategoryRespDTO> categoryRespDTOS = book.getBookCategories().stream()
                .map(BookCategory::getCategory)
                .map(c -> new CategoryRespDTO(c.getId(), c.getName(), c.getDeep(),
                        c.getPreCategory() != null ? c.getPreCategory().getId() : null,
                        c.getPreCategory() != null ? c.getPreCategory().getName() : null))
                .toList();

        List<TagRespDTO> tagRespDTOS = book.getBookTags().stream()
                .map(BookTag::getTag)
                .map(t -> new TagRespDTO(t.getId(), t.getName()))
                .toList();

        BigDecimal i = discount != null && book.getPrice() != null ? BigDecimal.valueOf ((1.0 - (double) discount / book.getPrice()) * 100.0): null;

        return new BookListRespDTO(book.getId(), book.getIsbn(), book.getTitle(),
                book.getBookAuthors().stream()
                        .map(ba ->
                                new AuthorRespDTO(ba.getAuthor() != null ? ba.getAuthor().getId() : null,
                                        ba.getAuthor() != null ? ba.getAuthor().getName() : null,
                                        ba.getRole() != null ? ba.getRole().getId() : null,
                                        ba.getRole() != null ? ba.getRole().getName() : null))
                        .toList(),
                book.getPublisher().getName(), book.getPublicationDate(), book.getPrice(), i != null ? i.setScale(2, RoundingMode.DOWN) : null,
                discount, book.getStatus(), imageRespDTOS, categoryRespDTOS, tagRespDTOS,book.getVolumeNo(), book.isPackaging(), isLike);
    }

    private List<BookListRespDTO> createdBookListRespDTOs(List<Book> books, Long userId){
        List<BookListRespDTO> bookListRespDTOs = new ArrayList<>();
        Set<Long> isLikes = likeService.getBookIsLike(userId, books).stream()
                .map(l -> l.getBook().getId())
                .collect(Collectors.toSet());

        for(Book book:books) {
            Boolean isLike = isLikes.contains(book.getId());
            bookListRespDTOs.add(createdBooksRespDTO(book, bookDiscountPrice(book), isLike));
        }
        return bookListRespDTOs;
    }

    @Override
    @Transactional
    public List<BookReviewResponse> getBooksByIdIn_ReviewId(long userId, List<BookOrderDetailRequest> bookOrderDetailRequests) {
        Map<Long, Book> bookMap = bookRepository.findAllByIdIn(bookOrderDetailRequests.stream()
                    .map(BookOrderDetailRequest::bookId)
                    .toList()).stream()
                .collect(Collectors.toMap(Book::getId, b->b));

        List<BookReviewResponse> bookReviewResponses = new ArrayList<>();
        for(BookOrderDetailRequest bod: bookOrderDetailRequests){
            if(!bookMap.containsKey(bod.bookId())){
                log.warn("존재하지 않는 도서입니다");
                continue;
            }
            Book book = bookMap.get(bod.bookId());
            BookResponse bookResponse = new BookResponse(book.getId(), book.getTitle(),
                    book.getBookImages().stream()
                            .map(bi ->
                                    new ImageRespDTO(bi.getNo(), bi.getPath(), bi.getImageType()))
                            .toList());

            Review review = reviewService.findReviewByUserIdAndBookIdAndOrderDetailId(userId, bod.bookId(), bod.orderDetailId());

            bookReviewResponses.add(new BookReviewResponse(bookResponse, bod.orderDetailId(), review != null ? review.getId() : null));
        }
        return bookReviewResponses;
    }

    @Override
    @Transactional
    public void updateBook(long bookId, BookReqDTO bookReqDTO, Map<String, MultipartFile> fileMap) {
        Book book = getBook_IdOrISBN(bookId, bookReqDTO.isbn(), "수정");

        //book 기본 필드값 수정사항
        if(!bookReqDTO.isbn().equals(book.getIsbn())){
            book.setIsbn(bookReqDTO.isbn());
        }
        if(!bookReqDTO.title().equals(book.getTitle())){
            book.setTitle(bookReqDTO.title());
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

        //작가 수정사항 확인
        Set<String> updateAuthorNames = bookReqDTO.authorReqDTOList().stream()
                .map(AuthorReqDTO::authorName)
                .collect(Collectors.toSet());

        List<BookAuthor> bookAuthorsToDelete = book.getBookAuthors().stream()
                .filter(ba -> !updateAuthorNames.contains(ba.getAuthor().getName())) // 요청 목록에 없는 작가만 필터링
                .toList();

        if (!bookAuthorsToDelete.isEmpty()) {
            bookAuthorsToDelete.forEach(ba -> {
                ba.getAuthor().getBookAuthors().remove(ba); // Author 측 관계 제거
                if (ba.getRole() != null) {
                    ba.getRole().getBookAuthors().remove(ba); // Role 측 관계 제거
                }
            });

            book.getBookAuthors().removeAll(bookAuthorsToDelete);

            bookAuthorRepository.deleteAllById(bookAuthorsToDelete.stream()
                    .map(BookAuthor::getId)
                    .toList());
        }

        Map<String, BookAuthor> existingBookAuthorMap = book.getBookAuthors().stream()
                .collect(Collectors.toMap(
                        ba -> ba.getAuthor().getName(),
                        ba -> ba
                ));

        for (AuthorReqDTO a : bookReqDTO.authorReqDTOList()) {
            Role role = authorService.findRoleByName(a.roleName());

            BookAuthor existingBookAuthor = existingBookAuthorMap.get(a.authorName());

            if (existingBookAuthor != null) { // 관계가 있으면 명칭이 변경이 있는지만 확인
                Role currentRole = existingBookAuthor.getRole();

                if ((role == null && currentRole != null) || //명칭이 null -> notNull
                        (role != null && currentRole == null) || //명칭이 notNull -> null
                        (role != null && currentRole != null && role.getId() != currentRole.getId())) { //명칭이 변경

                    if (currentRole != null) {
                        currentRole.getBookAuthors().remove(existingBookAuthor);
                    }

                    existingBookAuthor.setRole(role);
                    if (role != null) {
                        role.getBookAuthors().add(existingBookAuthor);
                    }
                }
            } else { // 관계가 없으면 새로운 작가를 등록
                Author author = authorService.findAuthorByName(a.authorName());
                if(author == null){
                    author = new Author(a.authorName());
                }

                BookAuthor newBookAuthor = new BookAuthor(book, author);

                if (role != null) {
                    newBookAuthor.setRole(role);
                    role.getBookAuthors().add(newBookAuthor);
                }

                book.getBookAuthors().add(newBookAuthor);
                author.getBookAuthors().add(newBookAuthor);
            }
        }

        //이미지 수정사항 확인
        List<BookImage> bookImages = book.getBookImages();
        List<ImageMetadataReqDTO> imageMetadataReqDTOs = bookReqDTO.imageMetadataReqDTOList();
        Map<Integer, String> bookImageUrl = bookImages.stream().collect(Collectors.toMap(BookImage::getNo, BookImage::getPath));
        Map<Integer, String> metadataReqDTOUrl = imageMetadataReqDTOs.stream().collect(Collectors.toMap(ImageMetadataReqDTO::sequence, ImageMetadataReqDTO::dataUrl));

        boolean imageChangeDetected = false;
        for(int i = 0; i < 5; i++){
            boolean pre = bookImageUrl.containsKey(i);
            boolean update = metadataReqDTOUrl.containsKey(i);

            if (imageMetadataReqDTOs.size() > i)
                if(
                    imageMetadataReqDTOs.get(i) != null &&
                    imageMetadataReqDTOs.get(i).fileKey() != null &&
                    fileMap.containsKey(imageMetadataReqDTOs.get(i).fileKey())) {

                imageChangeDetected = true;
                break;
            }

            if(pre && update) {
                if (!bookImageUrl.get(i).equals(metadataReqDTOUrl.get(i))) {
                    imageChangeDetected = true;
                    break;
                }
            } else if (pre || update) {
                imageChangeDetected = true;
                break;
            }
        }
        if(imageChangeDetected){
            ImagesReqDTO reqDTO = new ImagesReqDTO(book.getId(), imageMetadataReqDTOs);
            List<BookImage> bookImageList = imageService.updateBookImage(null, reqDTO, fileMap);
            book.setBookImages(bookImageList);
            if(bookImageList != null) {
                bookImageList.forEach(bi -> bi.setBook(book));
            }
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
                .map(BookCategory::getId)
                .toList());
        bookTagRepository.deleteBookTagsByIdIn(bookTags.stream()
                .map(BookTag::getId)
                .toList());

        book.getPublisher().getBookList().remove(book);

        List<BookAuthor> bookAuthors = book.getBookAuthors();
        List<Author> authors = bookAuthors.stream().map(BookAuthor::getAuthor).toList();
        List<Role> roles = bookAuthors.stream().map(BookAuthor::getRole).toList();

        authors.forEach(a -> a.getBookAuthors().removeAll(bookAuthors));
        roles.forEach(r -> r.getBookAuthors().removeAll(bookAuthors)); //이게 null인 경우

        bookAuthorRepository.deleteAllByIdIn(bookAuthors.stream()
                .map(BookAuthor::getId)
                .toList());

        log.debug("도서 제거 - ISBN: {}, Title: {}, Author: {}", book.getIsbn(), book.getTitle(),
                book.getBookAuthors().stream()
                        .map(b -> b.getAuthor().getName() + b.getRole().getName())
                        .toList());
        bookRepository.delete(book);
    }

    @Override
    @Transactional
    public void addLike(long bookId, long userId) {
        Book book = bookRepository.findBookById(bookId);
        likeService.createLike(userId, book);
    }

    @Override
    @Transactional
    public void deleteLike(long booId, long userId) {
        Book book = bookRepository.findBookById(booId);
        likeService.deleteLike(userId, book);
    }

    @Override
    @Transactional
    public void registerReview(ReviewReqDTO reviewReqDTO, Map<String, MultipartFile> fileMap) {
        Book book = bookRepository.findBookById(reviewReqDTO.bookId());
        reviewService.registerReview(reviewReqDTO, fileMap, book);
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
    public Book getBookById(long bookId) {
        return bookRepository.findBookById(bookId);
    }

    private Long bookDiscountPrice(Book book){
        List<Long> categoryIdlist = book.getBookCategories().stream().map(BookCategory::getCategory).map(Category::getId).toList();
        List<DiscountValueProjection> discountPolicyList = discountPolicyService.getDiscountValueByBookIdOrCategoryIdsOrPublisherId(categoryIdlist, book.getPublisher().getId(), book.getId());

        if(discountPolicyList == null || discountPolicyList.isEmpty()){
            return null;
        }

        long discountPercentage = 0;
        long discountFixedAmount = 0;
        for(DiscountValueProjection dp: discountPolicyList){
            if(DiscountType.PERCENTAGE.equals(dp.getDiscountType())){
                discountPercentage += dp.getValue();
            } else if(DiscountType.FIXED_AMOUNT.equals(dp.getDiscountType())){
                discountFixedAmount += dp.getValue();
            }
        }

        Long price = book.getPrice();
        if(price == null){
            log.warn("해당 도서의 가격이 존재하지 않습니다 - bookId:{}", book.getId());
            return null;
        }
        if(discountPercentage > 0){
            price = (long) (price * (1 - discountPercentage / 100.0));
        }
        if(discountFixedAmount > 0){
            price = price - discountFixedAmount;
        }

        return price < 0 ? 0: price;
    }

    @Override
    public BookCategoryResponse bookcategory(long bookId) {
        // 1. 책에 연결된 카테고리 정보 조회
        List<BookCategory> allByBookId = bookCategoryRepository.findAllByBook_Id(bookId);

        if (allByBookId.isEmpty()) {
            return new BookCategoryResponse(bookId, null, null);
        }

        // 변수 초기화
        Long firstCategoryId = null;  // 1단계 (10% 쿠폰용)
        Long secondCategoryId = null; // 2단계 (15% 쿠폰용)

        // 2. 조회된 리스트를 순회
        for (BookCategory bc : allByBookId) {
            Category category = bc.getCategory();
            if (category == null) continue;

            int deep = category.getDeep(); // 단계 확인

            if (deep == 1) {
                // 1단계인 경우
                firstCategoryId = category.getId();

            } else if (deep == 2) {
                // 2단계인 경우 -> 본인은 2단계, 부모는 1단계
                secondCategoryId = category.getId();
                if (firstCategoryId == null && category.getPreCategory() != null) {
                    firstCategoryId = category.getPreCategory().getId();
                }

            } else if (deep == 3) {
                // 3단계인 경우 -> 부모가 2단계, 조부모가 1단계
                // 3단계 카테고리 자체는 쿠폰 정책이 없으므로 ID 저장 안 함(혹은 필요하면 저장)

                // 1. 부모(2단계) 찾기
                Category parent = category.getPreCategory();
                if (parent != null) {
                    secondCategoryId = parent.getId(); // 2단계 ID 확보

                    // 2. 조부모(1단계) 찾기
                    Category grandParent = parent.getPreCategory();
                    if (firstCategoryId == null && grandParent != null) {
                        firstCategoryId = grandParent.getId(); // 1단계 ID 확보
                    }
                }
            }
        }

        // 로그 확인 (개발 중 디버깅용)
        if (firstCategoryId == null && secondCategoryId == null) {
            log.warn("책 ID {}에 대해 적용 가능한 1, 2단계 카테고리를 찾지 못했습니다.", bookId);
        }

        return new BookCategoryResponse(bookId, firstCategoryId, secondCategoryId);
    }
}
