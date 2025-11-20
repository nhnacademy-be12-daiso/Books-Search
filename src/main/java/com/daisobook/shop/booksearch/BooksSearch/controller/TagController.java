package com.daisobook.shop.booksearch.BooksSearch.controller;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.TagReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.TagRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.service.tag.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/tags")
public class TagController {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity addCategory(@RequestBody TagReqDTO tagReqDTO){
        tagService.registerTag(tagReqDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/batch")
    public ResponseEntity addCategories(@RequestBody List<TagReqDTO>tagReqDTOs){
        tagService.registerTags(tagReqDTOs);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{tagId}")
    public TagRespDTO getCategoryById(@PathVariable("tagId") long tagId){
        return tagService.getTagById(tagId);
    }

    @GetMapping("/name-search/{tagName}")
    public TagRespDTO getCategoryByName(@PathVariable("tagName") String tagName){
        return tagService.getTagByName(tagName);
    }

    @GetMapping
    public List<TagRespDTO> getCategories(){
        return tagService.getTags();
    }

    @PatchMapping("/{tagId}")
    public ResponseEntity updateCategory(@PathVariable("tagId") long tagId,
                                         @RequestBody TagReqDTO tagReqDTO){
        tagService.updateTag(tagId,tagReqDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity deleteCategory(@PathVariable("tagId") long tagId){
        tagService.deleteTag(tagId);
        return ResponseEntity.ok().build();
    }

}
