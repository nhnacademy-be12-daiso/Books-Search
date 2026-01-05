package com.daisobook.shop.booksearch.books_search.service.tag.impl;

import com.daisobook.shop.booksearch.books_search.entity.book.Book;
import com.daisobook.shop.booksearch.books_search.entity.tag.BookTag;
import com.daisobook.shop.booksearch.books_search.entity.tag.Tag;
import com.daisobook.shop.booksearch.books_search.repository.tag.BookTagRepository;
import com.daisobook.shop.booksearch.books_search.repository.tag.TagRepository;
import com.daisobook.shop.booksearch.books_search.service.tag.TagV2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class TagV2ServiceImpl implements TagV2Service {
    private final TagRepository tagRepository;
    private final BookTagRepository bookTagRepository;

    @Override
    @Transactional
    public void assignTagsToBook(Book book, List<String> tagNameList) {
        if(tagNameList == null || tagNameList.isEmpty()){
            log.warn("[도서 등록] 해당 Tage Name List가 비어있습니다.");
            return;
        }

        Map<String, Tag> tagMap = tagRepository.findAllByNameIn(tagNameList).stream()
                .collect(Collectors.toMap(Tag::getName, tag -> tag));

        List<Tag> saveTags = new ArrayList<>();
        List<BookTag> bookTags = new ArrayList<>();
        for(String tagName: tagNameList){
            Tag tag;
            if(tagMap.containsKey(tagName)){
                tag = tagMap.get(tagName);
            } else {
                tag = new Tag(tagName);
                saveTags.add(tag);
            }

            BookTag newBookTag = new BookTag(book, tag);

            tag.getBookTags().add(newBookTag);
            book.getBookTags().add(newBookTag);

            bookTags.add(newBookTag);
        }

        if(!saveTags.isEmpty()){
            tagRepository.saveAll(saveTags);
        }
        bookTagRepository.saveAll(bookTags);
    }

    @Override
    @Transactional
    public void assignTagsToBooks(Map<String, Book> bookMap, Map<String, List<String>> tagNameListMap) {
        Map<String, Tag> tagMap = tagRepository.findAllByNameIn(tagNameListMap.values().stream()
                        .flatMap(Collection::stream)
                        .toList()).stream()
                .collect(Collectors.toMap(Tag::getName, tag -> tag));

        List<Tag> saveTags = new ArrayList<>();
        List<BookTag> bookTags = new ArrayList<>();
        for(Book book: bookMap.values()){
            List<String> tagNameList = tagNameListMap.get(book.getIsbn());
            for(String tagName: tagNameList){
                Tag tag;
                if(tagMap.containsKey(tagName)){
                    tag = tagMap.get(tagName);
                } else {
                    tag = new Tag(tagName);
                    tagMap.put(tag.getName(), tag);
                    saveTags.add(tag);
                }

                BookTag newBookTag = new BookTag(book, tag);

                tag.getBookTags().add(newBookTag);
                book.getBookTags().add(newBookTag);

                bookTags.add(newBookTag);
            }
        }

        if(!saveTags.isEmpty()){
            tagRepository.saveAll(saveTags);
        }
        bookTagRepository.saveAll(bookTags);
    }

    @Override
    @Transactional
    public void updateTagOfBook(Book book, List<String> tagNameList) {
        List<BookTag> preBookTags = book.getBookTags();
        List<Tag> preTags = preBookTags.stream().map(BookTag::getTag).toList();

        Set<String> updateTagNames = new HashSet<>(tagNameList);

        List<Long> tagIdsToDelete = preTags.stream()
                .filter(preTag -> !updateTagNames.contains(preTag.getName()))
                .map(Tag::getId)
                .toList();

        if (!tagIdsToDelete.isEmpty()) {
            List<BookTag> bookTagsToDelete = bookTagRepository.findAllByBook_IdAndTag_IdIn(book.getId(), tagIdsToDelete);

            book.getBookTags().removeAll(bookTagsToDelete);
            bookTagRepository.deleteAllById(bookTagsToDelete.stream().map(BookTag::getId).toList());
        }

        Set<String> preTagNames = preTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        List<String> assignTags = tagNameList.stream()
                .filter(updateTagName -> !preTagNames.contains(updateTagName))
                .toList();

        if (!assignTags.isEmpty()) {
            assignTagsToBook(book, assignTags);
        }
    }

    @Override
    @Transactional
    public void deleteTagOfBook(Book book) {
        List<BookTag> bookTags = book.getBookTags();
        List<Tag> tags = bookTags.stream().map(BookTag::getTag).toList();

        if(!tags.isEmpty()){
            tags.forEach(t -> t.getBookTags().removeAll(bookTags));
        }

        book.getBookTags().removeAll(bookTags);

        bookTagRepository.deleteAll(bookTags);
    }
}
