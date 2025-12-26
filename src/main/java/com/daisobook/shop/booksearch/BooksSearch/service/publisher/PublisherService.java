//package com.daisobook.shop.booksearch.BooksSearch.service.publisher;
//
//import com.daisobook.shop.booksearch.BooksSearch.dto.request.PublisherReqDTO;
//import com.daisobook.shop.booksearch.BooksSearch.dto.response.PublisherRespDTO;
//import com.daisobook.shop.booksearch.BooksSearch.entity.publisher.Publisher;
//
//import java.util.List;
//
//public interface PublisherService {
//    void validateExistsById(long publisherId);
//    void validateExistsByName(String publisherName);
//    void validateNotExistsByName(String publisherName);
//    void registerPublisher(PublisherReqDTO publisherReqDTO);
//    void registerPublishers(List<PublisherReqDTO> publisherReqDTO);
//    PublisherRespDTO findPublisherById(long publisherId);
//    PublisherRespDTO findPublisherByName(String publisherName);
//    List<PublisherRespDTO> findPublisher();
//    void updatePublisher(long publisherId, PublisherReqDTO publisherReqDTO);
//    void deletePublisher(long publisherId);
//
//    //book 서비스에서 사용하는 메서드
//    Publisher getPublisherRegisterBook(String publisherName);
//    Publisher getPublisherByName(String publisherName);
//}
