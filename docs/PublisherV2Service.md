# PublisherV2Service

`PublisherV2Service`는 도서와 출판사 간의 연관 관계를 관리하는 서비스입니다. 출판사 정보의 중복 생성을 방지하고, 도서 등록 및 수정 시 효율적으로 관계를 설정합니다.

## 주요 기능

1.  **출판사 자동 생성 및 할당 (`assignPublisherToBook`)**
    *   도서 등록 시 입력된 출판사 이름으로 기존 출판사를 조회합니다.
    *   **존재할 경우**: 해당 출판사 엔티티를 도서에 할당합니다.
    *   **존재하지 않을 경우**: 새로운 출판사 엔티티를 생성하여 저장하고 도서에 할당합니다.
    *   이를 통해 동일한 출판사 이름에 대해 중복 데이터가 생성되는 것을 방지합니다.

2.  **대량 도서 출판사 일괄 할당 (`assignPublisherToBooks`)**
    *   여러 권의 도서를 한 번에 등록할 때(예: 엑셀 업로드), 각 도서별로 출판사를 조회하면 N번의 쿼리가 발생합니다.
    *   이를 방지하기 위해 입력된 모든 출판사 이름을 수집하여 `IN` 절을 사용해 한 번에 조회합니다.
    *   조회된 출판사 맵(`Map<Name, Publisher>`)을 활용하여 메모리 상에서 매핑하고, 없는 출판사만 모아서 `saveAll`로 일괄 저장합니다.

3.  **출판사 정보 수정 (`updatePublisherOfBook`)**
    *   도서의 출판사가 변경될 경우, 기존 출판사와의 관계를 끊고 새로운 출판사와 연결합니다.
    *   변경하려는 출판사가 DB에 없으면 새로 생성합니다.
    *   JPA의 변경 감지(Dirty Checking)를 활용하여 엔티티 상태를 업데이트합니다.

## 사용 예시

```java
@Service
@RequiredArgsConstructor
public class BookCoreService {
    private final PublisherV2Service publisherService;

    @Transactional
    public void registerBook(Book book, String publisherName) {
        // 도서 저장 전 출판사 관계 설정
        publisherService.assignPublisherToBook(book, publisherName);
        
        bookRepository.save(book);
    }
    
    @Transactional
    public void registerBooks(List<Book> books, Map<String, String> publisherMap) {
        // 대량 등록 시 일괄 처리
        publisherService.assignPublisherToBooks(books, publisherMap);
        
        bookRepository.saveAll(books);
    }
}
```

## 구성 요소

*   **PublisherRepository**: 출판사 엔티티(`Publisher`)에 대한 DB 접근을 담당합니다. `findPublisherByName`, `findAllByNameIn` 등의 메서드를 제공합니다.

## 흐름도 (단일 할당)

1.  출판사 이름으로 DB 조회 (`findPublisherByName`).
2.  결과가 `null`이면 `new Publisher(name)` 생성.
3.  `publisher.getBookList().add(book)` 및 `book.setPublisher(publisher)`로 양방향 관계 설정.
4.  새로 생성된 출판사라면 `repository.save(publisher)` 호출.
