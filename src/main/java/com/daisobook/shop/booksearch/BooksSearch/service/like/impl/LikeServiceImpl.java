package com.daisobook.shop.booksearch.BooksSearch.service.like.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.DiscountValueListData;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookIdProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.LikeBookListProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.LikeListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.LikeRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.like.Like;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.book.NotFoundBook;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.like.ExistedLike;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.mapper.FailObjectMapper;
import com.daisobook.shop.booksearch.BooksSearch.mapper.like.LikeMapper;
import com.daisobook.shop.booksearch.BooksSearch.repository.like.LikeRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.book.impl.BookCoreService;
import com.daisobook.shop.booksearch.BooksSearch.service.like.LikeService;
import com.daisobook.shop.booksearch.BooksSearch.service.policy.DiscountPolicyService;
import com.fasterxml.jackson.core.JsonProcessingException;
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

//    private final BookCoreService bookService;
    private final DiscountPolicyService discountPolicyService;
    private final LikeMapper likeMapper;

    @Override
    @Transactional
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
            log.error("존재하지 않은 도서 좋아요 취소 - 도서Id:{}", book.getId());
            throw new NotFoundBook("존재하지 않는 도서의 좋아요 취소입니다.");
        }

        likeRepository.deleteLikeByBookAndUserId(book, userId);
    }

//    //v2
//    @Override
//    @Transactional
//    public void addLike(long bookId, long userId){
//        if(likeRepository.existsLikeByBookIdAndUserId(bookId, userId)){
//            log.warn("[좋아요 생성] 이미 존재하는 좋아요 입니다");
//            throw new ExistedLike("[좋아요 생성] 이미 존재하는 좋아요 입니다");
//        }
//
//        Book book = bookService.getBook_Id(bookId);
//
//        if(book == null){
//            log.error("[좋아요 생성] 존재하지 않은 도서 좋아요 시도 - 도서Id:{}", bookId);
//            throw new NotFoundBook("[좋아요 생성] 존재하지 않는 도서의 좋아요 시도입니다.");
//        }
//
//        Like newLike = new Like(book, userId);
//
//        likeRepository.save(newLike);
//    }
//
//    @Override
//    @Transactional
//    public void deleteLike(long bookId, long likeId, long userId){
//        Like like = likeRepository.findLikeByBook_IdAndIdAndUserId(bookId, likeId, userId);
//        if(like == null){
//            log.warn("[좋아요 삭제] 존재하지 않는 좋아요 입니다");
//            throw new ExistedLike("[좋아요 삭제] 존재하지 않는 좋아요 입니다");
//        }
//
//        likeRepository.delete(like);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<LikeListRespDTO> getMyLikeList(long userId){
//        List<LikeBookListProjection> likeList = likeRepository.getAllByUserId(userId);
//
//        Map<Long, List<DiscountValueListData>> discountPolicyByDataMap = null;
//        try {
//            discountPolicyByDataMap = discountPolicyService.getDiscountPolicyByDataMap(likeList.stream()
//                    .map(LikeBookListProjection::getBookId)
//                    .toList());
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//
//        List<LikeListRespDTO> listRespDTOList = null;
//        try {
//            listRespDTOList = likeMapper.toLikeListRespDTOList(likeList, discountPolicyByDataMap);
//        } catch (JsonProcessingException e) {
//            throw new FailObjectMapper(e.getMessage());
//        }
//        return listRespDTOList;
//    }

}
