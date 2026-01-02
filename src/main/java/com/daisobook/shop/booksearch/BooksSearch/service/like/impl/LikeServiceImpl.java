package com.daisobook.shop.booksearch.BooksSearch.service.like.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookIdProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.LikeBookListProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.LikeRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.like.Like;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.book.NotFoundBook;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.like.ExistedLike;
import com.daisobook.shop.booksearch.BooksSearch.repository.like.LikeRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;

    @Override
    @Transactional
    public void existLike(long bookId, long userId){
        if(likeRepository.existsLikeByBookIdAndUserId(bookId, userId)){
            throw new RuntimeException("이미 존재하는 좋아요 입니다.");
        }
    }

    @Override
    @Transactional
    public void createLike(long userId, Book book) {
        if(book == null){
            log.error("존재하지 않은 도서 좋아요 시도");
            throw new NotFoundBook("존재하지 않는 도서의 좋아요 시도입니다.");
        }
        existLike(book.getId(), userId);

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
    @Transactional
    public int likeCount(long bookId) {
        return likeRepository.countAllByBook_Id(bookId);
    }

    @Override
    @Transactional
    public boolean likeCheck(long bookId, Long userId) {
        if(userId == null){
            return false;
        }
        return likeRepository.existsLikeByBook_IdAndUserId(bookId, userId);
    }

    @Override
    public Set<Long> getLikeByUserIdAndBookIds(Long userId, List<Long> bookIds){
        if(userId == null){
            return null;
        }

        List<BookIdProjection> likeList = likeRepository.getLikeByUserIdAndBookIdIn(userId, bookIds);
        if(likeList == null || likeList.isEmpty()){
            return null;
        }

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
            log.error("존재하지 않은 도서 좋아요 취소 - book is null, userId: {}", userId);
            throw new NotFoundBook("존재하지 않는 도서의 좋아요 취소입니다.");
        }

        likeRepository.deleteLikeByBookAndUserId(book, userId);
    }

    //v2
    @Override
    @Transactional
    public void saveLike(Like like){
        likeRepository.save(like);
    }

    @Override
    @Transactional
    public void deleteLike(long bookId, long userId){
        Like like = likeRepository.findLikeByBook_IdAndUserId(bookId, userId);
        if(like == null){
            log.warn("[좋아요 삭제] 존재하지 않는 좋아요 입니다");
            throw new ExistedLike("[좋아요 삭제] 존재하지 않는 좋아요 입니다");
        }

        likeRepository.delete(like);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LikeBookListProjection> getMyLikeList(long userId, Pageable pageable){
        return likeRepository.getAllByUserId(userId);
    }

}
