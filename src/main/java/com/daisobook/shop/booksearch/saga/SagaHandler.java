package com.daisobook.shop.booksearch.saga;

import com.daisobook.shop.booksearch.entity.book.Book;
import com.daisobook.shop.booksearch.entity.book.Status;
import com.daisobook.shop.booksearch.exception.custom.saga.BookOutOfStockException;
import com.daisobook.shop.booksearch.books_search.saga.event.*;
import com.daisobook.shop.booksearch.saga.event.*;
import com.daisobook.shop.booksearch.service.book.impl.BookCoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class SagaHandler {

    private final SagaTestService testService;
    private final SagaReplyService replyService;
    private final BookCoreService bookCoreService;

    @Transactional
    public void onMessage(SagaEvent event) {
        event.accept(this);
    }
    // ---> 이렇게 관문을 열어놔야 더블 디스패치 가능

    // 주문 전용 핸들러
    public void handleEvent(OrderConfirmedEvent event) {

        boolean isSuccess = true; // 성공 여부
        String reason = null; // 실패시 사유

        try {
            Map<Long, Integer> bookIntMap = event.getBookList();
            List<Long> bookIdList = bookIntMap.keySet().stream().toList();
            Map<Long, Book> books = bookCoreService.getBookByIdIn(bookIdList).stream()
                    .collect(Collectors.toMap(Book::getId, book -> book));

            for (Long bookId : bookIdList) {
                Integer n = bookIntMap.get(bookId);
                if (n <= 0) {
                    log.error("[주문 saga:재고 감소] 수량이 정보 오류");
                    reason = "STOCK_ERROR";
                    throw new BookOutOfStockException("[주문 saga:재고 감소] 수량이 정보 오류");
                }

                if (!books.containsKey(bookId)) {
                    log.error("[주문 saga:재고 감소] 해당하는 도서를 찾지 못했습니다 - 도서ID:{}", bookId);
                    reason = "NOT_FOUND";
                    throw new BookOutOfStockException("[주문 saga:재고 감소] 해당하는 도서를 찾지 못했습니다");
                }

                Book book = books.get(bookId);
                if (!book.getStatus().equals(Status.ON_SALE)) {
                    log.error("[주문 saga:재고 감소] 해당하는 도서가 판매 중이 아닙니다 - 도서ID:{}, 도서상태:{}", book.getId(), book.getStatus());
                    reason = "NOT_SELLING";
                    throw new BookOutOfStockException("[주문 saga:재고 감소] 해당하는 도서가 판매 중이 아닙니다");
                }

                int stock = book.getStock();
                if (stock < n) {
                    log.error("[주문 saga:재고 감소] 해당하는 도서 재고가 부족합니다 - 도서ID:{}, 도서 재고:{}, 주무 수량:{}", bookId, stock, n);
                    reason = "OUT_OF_STOCK";
                    throw new BookOutOfStockException("[주문 saga:재고 감소] 해당하는 도서 재고가 부족합니다");
                }

                book.setStock(stock - n);
                if (book.getStock() == 0) {
                    book.setStatus(Status.SOLD_OUT);
                }
            }

            log.error("[Book API] 재고 차감 성공 - Order : {}", event.getOrderId());

        } catch(BookOutOfStockException e) { // 재고 부족 비즈니스 예외
            log.error("[Book API] 비즈니스 오류로 인한 차감 실패 - Order : {}", event.getOrderId());
            isSuccess = false;
            throw e; // 롤백
        } catch(Exception e) {
            log.error("[Book API] 예상치 못한 시스템 에러 발생 - Order : {}", event.getOrderId(), e);
            isSuccess = false;
            reason = "SYSTEM_ERROR";
            throw e; // 롤백
        }
        // 이렇게 예외 범위를 넓게 해놔야 무슨 에러가 터져도 finally 문이 실행됨
        finally {

            // 성공했든 실패했든 답장은 해야함
            SagaReply reply = new SagaReply(
                    event.getEventId(),
                    event.getOrderId(),
                    "BOOK",
                    isSuccess,
                    reason
            );

            // 응답 메시지 전송
            replyService.send(event, reply, SagaTopic.REPLY_RK);
        }
    }

    // Refund 전용 핸들러
    public void handleEvent(OrderRefundEvent event) {
        boolean isSuccess = true; // 성공 여부
        String reason = null; // 실패시 사유

        try {
            /** TODO 반품 시 원상 복구 로직 작성
             * OrderRefundEvent 확인하셔서 다시 재고 채워주는 로직 작성해주시면 됩니다.
             */
            if (event.getQuantity() <= 0) {
                log.error("[주문 saga:반품] 수량 정보 오류 - 수량:{}", event.getQuantity());
                throw new BookOutOfStockException("[주문 saga:반품] 수량 정보 오류");
            }

            Book book = bookCoreService.getBook_Id(event.getBookId());
            if (book == null) {
                log.error("[주문 saga:반품] 해당하는 도서를 찾을 수 없습니다 - 도서ID:{}", event.getBookId());
                throw new BookOutOfStockException("[주문 saga:반품] 해당하는 도서를 찾을 수 없습니다");
            }

            book.setStock(book.getStock() + event.getQuantity().intValue());

        } catch (BookOutOfStockException e) { // 재고 부족 비즈니스 예외
            log.info("[Book API] 재고 부족으로 인한 차감 실패 - Order : {}", event.getOrderId());
            isSuccess = false;
            reason = "OUT_OF_STOCK";
            throw e; // 롤백
        } catch (Exception e) {
            log.info("[Book API] 예상치 못한 시스템 에러 발생 - Order : {}", event.getOrderId(), e);
            isSuccess = false;
            reason = "SYSTEM_ERROR";
            throw e; // 롤백
        }
        // 이렇게 예외 범위를 넓게 해놔야 무슨 에러가 터져도 finally 문이 실행됨
        finally {

            // 성공했든 실패했든 답장은 해야함
            SagaReply reply = new SagaReply(
                    event.getEventId(),
                    event.getOrderId(),
                    "BOOK",
                    isSuccess,
                    reason
            );
            replyService.send(event, reply, SagaTopic.REPLY_RK);
        }
    }

    // 보상 전용 핸들러
    public void handleEvent(OrderCompensateEvent event) {

        boolean isSuccess = true; // 성공 여부
        String reason = null; // 실패시 사유

        SagaEvent originalEvent = event.getOriginalEvent();

        try {
            // OrderConfirmedEvent일 경우 보상 트랜잭션
            if(originalEvent instanceof OrderConfirmedEvent confirmedEvent) {
                Map<Long, Integer> bookIntMap = confirmedEvent.getBookList();
                List<Long> bookIdList = bookIntMap.keySet().stream().toList();
                Map<Long, Book> books = bookCoreService.getBookByIdIn(bookIdList).stream()
                        .collect(Collectors.toMap(Book::getId, book -> book));

                for (Long bookId : bookIdList) {
                    Integer n = bookIntMap.get(bookId);
                    if (n <= 0) {
                        log.error("[주문 saga:재고 복구] 수량이 정보 오류");
                        reason = "STOCK_ERROR";
                        throw new BookOutOfStockException("[주문 saga:재고 복구] 수량이 정보 오류");
                    }

                    if (!books.containsKey(bookId)) {
                        log.error("[주문 saga:재고 복구] 해당하는 도서를 찾지 못했습니다 - 도서ID:{}", bookId);
                        reason = "NOT_FOUND";
                        throw new BookOutOfStockException("[주문 saga:재고 복구] 해당하는 도서를 찾지 못했습니다");
                    }

                    Book book = books.get(bookId);

                    book.setStock(book.getStock() + n);
                    if (book.getStatus().equals(Status.SOLD_OUT)) {
                        book.setStatus(Status.ON_SALE);
                    }
                }
                log.error("[Book API] 재고 보상 성공 - Order : {}", event.getOrderId());
            }

            if(originalEvent instanceof OrderRefundEvent refundEvent) {
                // TODO refund 의 보상 로직 작성
            }

        } catch(Exception e) {
            log.error("[Book API] 예상치 못한 시스템 에러 발생 - Order : {}", event.getOrderId(), e);
            isSuccess = false;
            // TODO 재시도 로직
            /**
             * 보상 자체가 실패했을때 재시도 로직을 구현할 필요가 있음
             * 비즈니스 로직의 문제가 아니라 기술적 문제라면 재시도하는게 맞음
             * 그냥 무지성으로 롤백해버리면 안됨
             */
        }
        finally {
            // 성공했든 실패했든 답장은 해야함
            SagaReply reply = new SagaReply(
                    event.getEventId(),
                    event.getOrderId(),
                    "BOOK",
                    isSuccess,
                    reason
            );

            // 응답 메시지 전송
            replyService.send(event, reply, SagaTopic.REPLY_COMPENSATION_RK);
        }
    }

}

