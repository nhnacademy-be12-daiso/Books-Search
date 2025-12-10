package com.daisobook.shop.booksearch.BooksSearch.service.tag;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.TagReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.TagRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.tag.BookTag;
import com.daisobook.shop.booksearch.BooksSearch.entity.tag.Tag;

import java.util.List;

public interface TagService {
    void validateNotExistsByName(String tagName);
    void validateExistsById(long tagId);
    void validateExistsByName(String tagName);
    void registerTag(TagReqDTO tagReqDTO);
    void registerTags(List<TagReqDTO> tagReqDTOList);
    TagRespDTO getTagById(long tagId);
    TagRespDTO getTagByName(String name);
    List<TagRespDTO> getTags();
    void updateTag(long tagId, TagReqDTO tagReqDTO);
    void deleteTag(long tagId);

    // book 서비스에서 사용하는 메서드
    Tag findTagByName(String tagName);
    List<TagRespDTO> findAllByIdIn(List<Long> tagId);
    List<Tag> findAllByNameIn(List<String> tagName);
    List<Tag> findAllByBookTags(List<BookTag> bookTags);
    List<Tag> getAllByIdIn(List<Long> tagId);
}
