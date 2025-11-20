package com.daisobook.shop.booksearch.BooksSearch.service.like.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.LikeReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.LikeRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.Like;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.NotFoundBook;
import com.daisobook.shop.booksearch.BooksSearch.repository.LikeRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.book.BookService;
import com.daisobook.shop.booksearch.BooksSearch.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final BookService bookService;

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

        Book book = bookService.getBookById(likeReqDTO.bookId());

        if(book == null){
            log.error("존재하지 않은 도서 좋아요 시도 - 도서Id:{}", likeReqDTO.bookId());
            throw new NotFoundBook("존재하지 않는 도서의 좋아요 시도입니다.");
        }

        Like newLike = new Like(book, likeReqDTO.userId());

        likeRepository.save(newLike);
    }

    @Override
    @Transactional
    public List<LikeRespDTO> getLikeList(long userId) {
        List<Like> likes = likeRepository.findAllByUserId(userId);

        if(likes == null){
            return List.of();
        }

        return likes.stream()
                .map(l -> new LikeRespDTO(l.getId() ,l.getBook().getId(), l.getUserId(), l.getBook().getTitle(), l.getBook().getImageUrl()))
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

    //없어도 될것같다
//    @Override
//    @Transactional
//    public void updateLike(long likeId) {
//        exist(likeId);
//
//        Like like = likeRepository.findLikeById(likeId);
//    }

    @Override
    @Transactional
    public void deleteLike(long likeId) {
        exist(likeId);

        likeRepository.deleteLikeById(likeId);
    }


}
