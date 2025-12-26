//package com.daisobook.shop.booksearch.BooksSearch.service.publisher.impl;
//
//import com.daisobook.shop.booksearch.BooksSearch.dto.request.PublisherReqDTO;
//import com.daisobook.shop.booksearch.BooksSearch.dto.response.PublisherRespDTO;
//import com.daisobook.shop.booksearch.BooksSearch.entity.publisher.Publisher;
//import com.daisobook.shop.booksearch.BooksSearch.exception.custom.publisher.CannotChangedPublisher;
//import com.daisobook.shop.booksearch.BooksSearch.exception.custom.publisher.DuplicatedPublisher;
//import com.daisobook.shop.booksearch.BooksSearch.exception.custom.publisher.NotFoundPublisherId;
//import com.daisobook.shop.booksearch.BooksSearch.exception.custom.publisher.NotFoundPublisherName;
//import com.daisobook.shop.booksearch.BooksSearch.repository.publisher.PublisherRepository;
//import com.daisobook.shop.booksearch.BooksSearch.service.publisher.PublisherService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Slf4j
//@RequiredArgsConstructor
//@Service
//public class PublisherServiceImpl implements PublisherService {
//    private final PublisherRepository publisherRepository;
//
//    @Override
//    public void validateExistsById(long publisherId) {
//        if(!publisherRepository.existsById(publisherId)){
//            log.error("해당 출판사ID가 존재하지 않습니다 - 출판사ID:{}", publisherId);
//            throw new NotFoundPublisherId("해당 출판사ID가 존재하지 않습니다");
//        }
//    }
//
//    @Override
//    public void validateExistsByName(String publisherName) {
//        if(!publisherRepository.existsPublisherByName(publisherName)){
//            log.error("해당 출판사 이름이 존재하지 않습니다 - 출판사 Name:{}", publisherName);
//            throw new NotFoundPublisherName("해당 출판사 이름이 존재하지 않습니다");
//        }
//    }
//
//    @Override
//    public void validateNotExistsByName(String publisherName) {
//        if(publisherRepository.existsPublisherByName(publisherName)){
//            log.error("해당 출판사 이름이 이미 존재합니다 - 출판사 Name:{}", publisherName);
//            throw new DuplicatedPublisher("해당 출판사 이름이 이미 존재합니다");
//        }
//    }
//
//    @Override
//    @Transactional
//    public void registerPublisher(PublisherReqDTO publisherReqDTO) {
//        validateNotExistsByName(publisherReqDTO.name());
//
//        Publisher newPublisher = Publisher.create(publisherReqDTO);
//
//        publisherRepository.save(newPublisher);
//        log.debug("출판사 저장 - ID: {}, Name: {}", newPublisher.getId(), newPublisher.getName());
//    }
//
//    @Override
//    @Transactional
//    public void registerPublishers(List<PublisherReqDTO> publisherReqDTOs) {
//        Set<String> publisherNames = publisherRepository.findAll().stream()
//                .map(Publisher::getName)
//                .collect(Collectors.toSet());
//
//        for(PublisherReqDTO p: publisherReqDTOs){
//            if(publisherNames.contains(p.name())){
//                log.error("데이터베이스에 출판사 이름이 이미 존재합니다 - 출판사 Name:{}", p.name());
//                continue;
//            }
//
//            Publisher newPublisher = Publisher.create(p);
//
//            publisherRepository.save(newPublisher);
//            log.debug("출판사 저장(여러개 출판사 저장중) - ID: {}, Name: {}", newPublisher.getId(), newPublisher.getName());
//        }
//    }
//
//    @Override
//    @Transactional
//    public PublisherRespDTO findPublisherById(long publisherId) {
//        validateExistsById(publisherId);
//
//        Publisher publisher = publisherRepository.findPublisherById(publisherId);
//        return new PublisherRespDTO(publisher.getId(), publisher.getName());
//    }
//
//    @Override
//    @Transactional
//    public PublisherRespDTO findPublisherByName(String publisherName) {
//        validateExistsByName(publisherName);
//
//        Publisher publisher = publisherRepository.findPublisherByName(publisherName);
//        return new PublisherRespDTO(publisher.getId(), publisher.getName());
//    }
//
//    @Override
//    @Transactional
//    public List<PublisherRespDTO> findPublisher() {
//        List<Publisher> publishers = publisherRepository.findAll();
//        if(publishers.isEmpty()){
//           return List.of();
//        }
//
//        return publishers.stream()
//                .map(p -> new PublisherRespDTO(p.getId(), p.getName()))
//                .toList();
//    }
//
//    @Override
//    @Transactional
//    public void updatePublisher(long publisherId, PublisherReqDTO publisherReqDTO) {
//        validateExistsById(publisherId);
//        validateNotExistsByName(publisherReqDTO.name());
//
//        Publisher publisher = publisherRepository.findPublisherById(publisherId);
//        publisher.setName(publisherReqDTO.name());
//    }
//
//    @Override
//    @Transactional
//    public void deletePublisher(long publisherId) {
//        Publisher publisher = publisherRepository.findPublisherById(publisherId);
//
//        if(publisher.getBookList() != null && !publisher.getBookList().isEmpty()){
//            log.error("해당 출판사와 관계가 있는 도서가 존재하여 삭제 불가 - 관련 도서 수:{}", publisher.getBookList().size());
//            throw new CannotChangedPublisher("해당 출판사와 관계가 있는 도서가 존재하여 삭제 불가");
//        }
//
//        publisherRepository.delete(publisher);
//    }
//
//    // book 서비스에서 사용하는 메서드
//
//    @Override
//    public Publisher getPublisherRegisterBook(String publisherName) {
//        Publisher publisher = publisherRepository.findPublisherByName(publisherName);
//        if(publisher == null){
//            publisher = new Publisher(publisherName);
//            publisherRepository.save(publisher);
//        }
//
//        return publisher;
//    }
//
//    @Override
//    public Publisher getPublisherByName(String publisherName){
//        return publisherRepository.findPublisherByName(publisherName);
//    }
//}
