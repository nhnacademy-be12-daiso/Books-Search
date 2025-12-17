package com.daisobook.shop.booksearch.BooksSearch.service.like.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookIdProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.LikeRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.like.Like;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.book.NotFoundBook;
import com.daisobook.shop.booksearch.BooksSearch.repository.like.LikeRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;

    @Override
    public void createLike(long userId, Book book) {
        if(likeRepository.existsLikeByBookIdAndUserId(book.getId(), userId)){
            throw new RuntimeException("이미 존재하는 좋아요 입니다.");
        }

        if(book == null){
            log.error("존재하지 않은 도서 좋아요 시도 - 도서Id:{}", book.getId());
            throw new NotFoundBook("존재하지 않는 도서의 좋아요 시도입니다.");
        }

        Like newLike = new Like(book, userId);

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
                .map(l -> new LikeRespDTO(l.getId() ,l.getBook().getId(), l.getUserId(),
                        l.getBook().getTitle(), l.getBook().getBookImages().getFirst().getPath()))
                .toList();
    }

    @Override
    public int likeCount(long bookId) {
        return likeRepository.countAllByBook_Id(bookId);
    }

    @Override
    public boolean likeCheck(long bookId, Long userId) {
        if(userId == null){
            return false;
        }
        return likeRepository.existsLikeByBook_IdAndUserId(bookId, userId);
    }

    @Override
    public Set<Long> getLikeByUserIdAndBookIds(Long userId, List<Long> bookIds){
        List<BookIdProjection> likeList = likeRepository.getLikeByUserIdAndBookIdIn(userId, bookIds);

        return likeList.stream()
                .map(BookIdProjection::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public List<Like> getBookIsLike(Long userId, List<Book> books) {
        if(userId == null){
            return List.of();
        }

        return likeRepository.findAllByUserIdAndBookIn(userId, books);
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
    public void deleteLike(long userId, Book book) {
        if(book == null){
            log.error("존재하지 않은 도서 좋아요 취소 - 도서Id:{}", book.getId());
            throw new NotFoundBook("존재하지 않는 도서의 좋아요 취소입니다.");
        }

        likeRepository.deleteLikeByBookAndUserId(book, userId);
    }


}
