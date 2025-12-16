package com.daisobook.shop.booksearch.BooksSearch.service.tag.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.TagReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.TagRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.tag.BookTag;
import com.daisobook.shop.booksearch.BooksSearch.entity.tag.Tag;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.tag.CannotChangedTag;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.tag.DuplicatedTag;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.tag.NotFoundTagId;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.tag.NotFoundTagName;
import com.daisobook.shop.booksearch.BooksSearch.repository.tag.BookTagRepository;
import com.daisobook.shop.booksearch.BooksSearch.repository.tag.TagRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.tag.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final BookTagRepository bookTagRepository;

    @Override
    public void validateNotExistsByName(String tagName) {
        if(tagRepository.existsTagByName(tagName)){
            log.error("이미 존재하는 테그이름 입니다 - 태그Name:{}", tagName);
            throw new DuplicatedTag("이미 존재하는 테그이름입니다.");
        }
    }

    @Override
    public void validateExistsById(long tagId) {
        if(!tagRepository.existsTagById(tagId)){
            log.error("존재하지 않은 태그ID 입니다 - 태그ID:{}", tagId);
            throw new NotFoundTagId("존재하지 않은 태그ID 입니다");
        }
    }

    @Override
    public void validateExistsByName(String tagName) {
        if(!tagRepository.existsTagByName(tagName)){
            log.error("존재하지 않은 테그이름 입니다 - 태그Name:{}", tagName);
            throw new DuplicatedTag("존재하지 않은 테그이름입니다.");
        }
    }

    @Override
    @Transactional
    public void registerTag(TagReqDTO tagReqDTO) {
        validateNotExistsByName(tagReqDTO.tagName());

        tagRepository.save(Tag.create(tagReqDTO));
    }

    @Override
    @Transactional
    public void registerTags(List<TagReqDTO> tagReqDTOList) {
        Set<String> tags = tagRepository.findAllByNameIn(tagReqDTOList.stream()
                        .map(TagReqDTO::tagName)
                        .toList()).stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        for(TagReqDTO t: tagReqDTOList) {
            if (tags.contains(t.tagName())){
                log.error("이미 존재하는 테그 등록 시도 - 테그 이름: {}", t.tagName());
                continue;
            }

            tagRepository.save(Tag.create(t));
        }
    }

    @Override
    @Transactional
    public TagRespDTO getTagById(long tagId) {
        validateExistsById(tagId);

        Tag tag = tagRepository.findTagById(tagId);

        return new TagRespDTO(tag.getId(), tag.getName());
    }

    @Override
    public TagRespDTO getTagByName(String name) {
        validateExistsByName(name);

        Tag tag = tagRepository.findTagByName(name);

        return new TagRespDTO(tag.getId(), tag.getName());
    }

    @Override
    public List<TagRespDTO> getTags() {
        List<Tag> tags = tagRepository.findAll();

        return tags.stream()
                .map(t -> new TagRespDTO(t.getId(), t.getName()))
                .toList();
    }

    @Override
    @Transactional
    public void updateTag(long tagId, TagReqDTO tagReqDTO) {
        validateExistsById(tagId);
        validateNotExistsByName(tagReqDTO.tagName());

        Tag tag = tagRepository.findTagById(tagId);
         tag.setName(tagReqDTO.tagName());
    }

    @Override
    @Transactional
    public void deleteTag(long tagId) {
        if(bookTagRepository.existsByTag_Id(tagId)){
            log.error("해당 테그에 관계가 존재하여 삭제 불가 - tagID:{}", tagId);
            throw new CannotChangedTag("해당 테그에 관계가 존재하여 삭제 불가");
        }

        tagRepository.deleteById(tagId);
    }

    //book 서비스에서 사용하는 메서드

    @Override
    public Tag findTagByName(String tagName) {
        Tag tag = tagRepository.findTagByName(tagName);
        if(tag == null){
            tag = new Tag(tagName);
        }

        tagRepository.save(tag);
        return tag;
    }

    @Override
    public List<TagRespDTO> findAllByIdIn(List<Long> tagId) {
        List<Tag> tags = getAllByIdIn(tagId);

        return tags.stream()
                .map(t -> new TagRespDTO(t.getId(), t.getName()))
                .toList();
    }

    @Override
    public List<Tag> findAllByNameIn(List<String> tagName){
        return tagRepository.findAllByNameIn(tagName);
    }

    @Override
    public List<Tag> findAllByBookTags(List<BookTag> bookTags) {
        return tagRepository.findAllByBookTags(bookTags);
    }

    @Override
    public List<Tag> getAllByIdIn(List<Long> tagId) {
        return tagRepository.findAllByIdIn(tagId);
    }
}
