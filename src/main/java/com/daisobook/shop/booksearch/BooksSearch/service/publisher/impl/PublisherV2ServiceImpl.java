package com.daisobook.shop.booksearch.BooksSearch.service.publisher.impl;

import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.publisher.Publisher;
import com.daisobook.shop.booksearch.BooksSearch.repository.publisher.PublisherRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.publisher.PublisherV2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PublisherV2ServiceImpl implements PublisherV2Service {
    private PublisherRepository publisherRepository;

    @Override
    @Transactional
    public void assignPublisherToBook(Book book, String publisherName) {
        Publisher publisher = publisherRepository.findPublisherByName(publisherName);

        boolean isNull = false;
        if(publisher == null){
            publisher = new Publisher(publisherName);
            isNull = true;
        }

        publisher.getBookList().add(book);
        book.setPublisher(publisher);

        if(isNull){
            publisherRepository.save(publisher);
        }
    }

    @Override
    public void assignPublisherToBooks(Map<String, Book> bookMap, Map<String, String> publisherNameMap) {
        Map<String, Publisher> publisherMap = publisherRepository.findAllByNameIn(publisherNameMap.values().stream()
                        .toList()).stream()
                .collect(Collectors.toMap(Publisher::getName, p -> p));

        List<Publisher> savePublisher = new ArrayList<>();
        for(Book book: bookMap.values()){
            String publisherName = publisherNameMap.get(book.getIsbn());
            Publisher publisher = publisherMap.get(publisherName);

            if(publisher == null){
                publisher = new Publisher(publisherName);
                savePublisher.add(publisher);
            }

            publisher.getBookList().add(book);
            book.setPublisher(publisher);
        }

        if(!savePublisher.isEmpty()){
            publisherRepository.saveAll(savePublisher);
        }
    }
}
