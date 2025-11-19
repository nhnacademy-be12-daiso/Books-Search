package com.daisobook.shop.booksearch.BooksSearch.service.like.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.LikeReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.LikeRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.Like;
import com.daisobook.shop.booksearch.BooksSearch.repository.BookRepository;
import com.daisobook.shop.booksearch.BooksSearch.repository.LikeRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final BookRepository bookRepository;

    @Override
    public void exist(long likeId) {
        if(!likeRepository.existsLikeById(likeId)){
            throw new RuntimeException("존재하지 않는 좋아요 입니다.");
        }
    }

    @Override
    @Transactional
    public void createLike(LikeReqDTO likeReqDTO) {
        if(likeRepository.existsLikeByBookIdAndUserId(likeReqDTO.bookId(), likeReqDTO.userId())){
            throw new RuntimeException("이미 존재하는 좋아요 입니다.");
        }

        Like newLike = new Like(bookRepository.findBookById(likeReqDTO.bookId()),
                likeReqDTO.userId(), likeReqDTO.title(), likeReqDTO.imageUrl());

        likeRepository.save(newLike);
    }

    @Override
    @Transactional
    public List<LikeRespDTO> getLikeList(long userId) {
        List<Like> likes = likeRepository.findAllByUserId(userId);

        return likes.stream()
                .map(l -> new LikeRespDTO(l.getBook().getId(), l.getUserId(), l.getTitle(), l.getImageUrl()))
                .toList();
    }

    @Override
    public int likeCount(long bookId) {
        return likeRepository.countAllByBook_Id(bookId);
    }

    @Override
    public boolean likeCheck(long bookId, long userId) {
        return likeRepository.existsLikeByBook_IdAndUserId(bookId, userId);
    }

    @Override
    @Transactional
    public void updateLike(long likeId) {
        exist(likeId);

        Like like = likeRepository.findLikeById(likeId);
    }

    @Override
    @Transactional
    public void deleteLike(long likeId) {
        exist(likeId);

        Like like = likeRepository.findLikeById(likeId);

        likeRepository.delete(like);
    }


}
