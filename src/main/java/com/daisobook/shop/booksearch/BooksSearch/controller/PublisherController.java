package com.daisobook.shop.booksearch.BooksSearch.controller;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.PublisherReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.PublisherRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.service.publisher.PublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/books/publishers")
public class PublisherController {
    private final PublisherService publisherService;

    @PostMapping
    public ResponseEntity addPublisher(@RequestBody PublisherReqDTO publisherReqDTO){
        publisherService.registerPublisher(publisherReqDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/batch")
    public ResponseEntity addPublishers(@RequestBody List<PublisherReqDTO> publisherReqDTOs){
        publisherService.registerPublishers(publisherReqDTOs);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{publisherId}")
    public PublisherRespDTO getPublisherById(@PathVariable("publisherId") long publisherId){
        return publisherService.findPublisherById(publisherId);
    }

    @GetMapping("/name-search/{publisherName}")
    public PublisherRespDTO getPublisherByName(@PathVariable("publisherName") String publisherName){
        return publisherService.findPublisherByName(publisherName);
    }

    @GetMapping
    public List<PublisherRespDTO> getPublishers(){
        return publisherService.findPublisher();
    }

    @PatchMapping("/{publisherId}")
    public ResponseEntity updatePublisher(@PathVariable("publisherId") long publisherId, @RequestBody PublisherReqDTO publisherReqDTO){
        publisherService.updatePublisher(publisherId, publisherReqDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{publisherId}")
    public ResponseEntity deletePublisher(@PathVariable("publisherId") long publisherId){
        publisherService.deletePublisher(publisherId);
        return ResponseEntity.ok().build();
    }
}
