package com.daisobook.shop.booksearch.BooksSearch.service.book.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.*;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookGroupReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.*;
import com.daisobook.shop.booksearch.BooksSearch.dto.service.ImagesReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.*;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.*;
import com.daisobook.shop.booksearch.BooksSearch.repository.BookAuthorRepository;
import com.daisobook.shop.booksearch.BooksSearch.repository.BookCategoryRepository;
import com.daisobook.shop.booksearch.BooksSearch.repository.BookRepository;
import com.daisobook.shop.booksearch.BooksSearch.repository.BookTagRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.author.AuthorService;
import com.daisobook.shop.booksearch.BooksSearch.service.book.BookService;
import com.daisobook.shop.booksearch.BooksSearch.service.category.CategoryService;
import com.daisobook.shop.booksearch.BooksSearch.service.image.impl.BookImageServiceImpl;
import com.daisobook.shop.booksearch.BooksSearch.service.publisher.PublisherService;
import com.daisobook.shop.booksearch.BooksSearch.service.tag.TagService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

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

    private final ObjectMapper objectMapper;

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
        List<BookImage> bookImages = imageService.addBookImage(reqDTO, fileMap);
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

                List<List<BookImage>> newImagesList = imageService.addBookImages(imagesReqDTOsForProcess);

                for (int i = 0; i < books.size(); i++) {
                    Book book = books.get(i);
                    newImagesList.get(i).forEach(bookImage -> bookImage.setBook(book));
                }

                count = 0;
            }
        }
    }

    @Override
    @Transactional
    public BookRespDTO findBookById(long bookId) {
        validateExistsById(bookId);

        Book book = bookRepository.findBookById(bookId);
        log.debug("도서ID로 조회 성공 - ISBN: {}, Title: {}, Author: {}", book.getIsbn(), book.getTitle(),
                book.getBookAuthors().stream()
                        .map(ba -> ba.getAuthor().getName() + ba.getRole().getName())
                        .toList());

        return createdBookRespDTO(book);
    }

    @Override
    @Transactional
    public BookRespDTO findBookByIsbn(String isbn) {
        validateExistsByIsbn(isbn);

        Book book = bookRepository.findBookByIsbn(isbn);
        log.debug("도로ISBN으로 조회 성공 - ISBN: {}, Title: {}, Author: {}", book.getIsbn(), book.getTitle(),
                book.getBookAuthors().stream()
                        .map(ba -> ba.getAuthor().getName() + ba.getRole().getName())
                        .toList());

        return createdBookRespDTO(book);
    }

    private BookRespDTO createdBookRespDTO(Book book){
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

        return new BookRespDTO(book.getId(), book.getIsbn(), book.getTitle(), book.getIndex(), book.getDescription(),
                book.getBookAuthors().stream()
                        .map(ba ->
                                new AuthorRespDTO(ba.getAuthor() != null ? ba.getAuthor().getId() : null,
                                        ba.getAuthor() != null ? ba.getAuthor().getName() : null,
                                        ba.getRole() != null ? ba.getRole().getId() : null,
                                        ba.getRole() != null ? ba.getRole().getName() : null))
                        .toList(),
                book.getPublisher().getName(), book.getPublicationDate(), book.getPrice(), book.isPackaging(), book.getStock(), book.getStatus(),
                imageRespDTOS, book.getVolumeNo(), categoryRespDTOS, tagRespDTOS);
    }

    @Override
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
    public List<BookListRespDTO> getBooksByIdIn(List<Long> bookIds) {
        List<Book> bookList = bookRepository.findBooksByIdIn(bookIds);
        if(bookList == null || bookList.isEmpty()){
            return List.of();
        }
        return createdBookListRespDTOs(bookList);
    }

    private List<BookRespDTO> createdBookRespDTOs(List<Book> books){
        List<BookRespDTO> bookRespDTOS = new ArrayList<>();
        for(Book book:books) {
            bookRespDTOS.add(createdBookRespDTO(book));
        }
        return bookRespDTOS;
    }

    private BookListRespDTO createdBooksRespDTO(Book book){
        List<ImageRespDTO> imageRespDTOS = book.getBookImages().stream()
                .map(bi -> new ImageRespDTO(bi.getNo(), bi.getPath(), bi.getImageType()))
                .toList();

        return new BookListRespDTO(book.getId(), book.getTitle(),
                book.getBookAuthors().stream()
                        .map(ba ->
                                new AuthorRespDTO(ba.getAuthor() != null ? ba.getAuthor().getId() : null,
                                        ba.getAuthor() != null ? ba.getAuthor().getName() : null,
                                        ba.getRole() != null ? ba.getRole().getId() : null,
                                        ba.getRole() != null ? ba.getRole().getName() : null))
                        .toList(),
                book.getPublisher().getName(), book.getPublicationDate(), book.getPrice(), book.getStatus(),
                imageRespDTOS, book.getVolumeNo());
    }

    private List<BookListRespDTO> createdBookListRespDTOs(List<Book> books){
        List<BookListRespDTO> bookListRespDTOs = new ArrayList<>();
        for(Book book:books) {
            bookListRespDTOs.add(createdBooksRespDTO(book));
        }
        return bookListRespDTOs;
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
        //TODO 여기 작가 수정 좀 해 (미래의 나한톄 전하는 메시지)
//        if(!bookReqDTO.author().equals(book.getBookAuthors().stream().map(BookAuthor::getAuthor).toList().toString())){
////            book.setAuthor(bookReqDTO.author());
//        }
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
            List<BookImage> bookImageList = imageService.updateBookImage(reqDTO, fileMap);
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
}
