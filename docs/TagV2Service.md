# TagV2Service

`TagV2Service`는 도서와 태그 간의 다대다(N:M) 관계를 관리하는 서비스입니다. 태그의 중복 생성을 방지하고, 도서 등록 및 수정 시 효율적으로 관계를 설정합니다.

## 주요 기능

1.  **태그 자동 생성 및 할당 (`assignTagsToBook`)**
    *   도서 등록 시 입력된 태그 이름 목록으로 기존 태그를 조회합니다.
    *   **존재할 경우**: 해당 태그 엔티티를 사용하여 `BookTag` 연결 엔티티를 생성합니다.
    *   **존재하지 않을 경우**: 새로운 태그 엔티티를 생성하여 저장하고 `BookTag`를 생성합니다.
    *   `BookTag` 엔티티를 통해 도서와 태그 간의 다대다 관계를 매핑합니다.

2.  **대량 도서 태그 일괄 할당 (`assignTagsToBooks`)**
    *   여러 권의 도서를 한 번에 등록할 때, 모든 도서의 태그 이름을 수집하여 한 번의 쿼리로 조회합니다.
    *   조회된 태그 맵(`Map<Name, Tag>`)을 활용하여 메모리 상에서 매핑하고, 없는 태그만 모아서 `saveAll`로 일괄 저장합니다.
    *   `BookTag` 연결 엔티티들도 리스트에 모아 `saveAll`로 한 번에 저장하여 성능을 최적화합니다.

3.  **태그 정보 수정 (`updateTagOfBook`)**
    *   도서의 태그 목록이 변경될 경우, 기존 태그와 비교하여 추가/삭제 작업을 수행합니다.
    *   **삭제**: 기존 태그 중 새 목록에 없는 태그와의 연결(`BookTag`)을 삭제합니다.
    *   **추가**: 새 목록 중 기존에 없던 태그를 새로 할당(`assignTagsToBook`)합니다.
    *   불필요한 전체 삭제 후 재생성을 피하고, 변경된 부분만 효율적으로 업데이트합니다.

## 사용 예시

```java
@Service
@RequiredArgsConstructor
public class BookCoreService {
    private final TagV2Service tagService;

    @Transactional
    public void registerBook(Book book, List<String> tagNames) {
        // 도서 저장
        bookRepository.save(book);
        
        // 태그 관계 설정
        tagService.assignTagsToBook(book, tagNames);
    }
    
    @Transactional
    public void updateBook(Book book, List<String> newTagNames) {
        // 태그 정보 업데이트
        tagService.updateTagOfBook(book, newTagNames);
    }
}
```

## 구성 요소

*   **TagRepository**: 태그 엔티티(`Tag`)에 대한 DB 접근을 담당합니다.
*   **BookTagRepository**: 도서와 태그의 연결 엔티티(`BookTag`)에 대한 DB 접근을 담당합니다.

## 흐름도 (태그 수정)

1.  현재 도서에 연결된 태그 목록 조회 (`preTags`).
2.  업데이트할 태그 목록(`updateTagNames`)과 비교.
3.  **삭제 대상 식별**: `preTags`에는 있지만 `updateTagNames`에는 없는 태그 -> `BookTag` 삭제.
4.  **추가 대상 식별**: `updateTagNames`에는 있지만 `preTags`에는 없는 태그 -> `assignTagsToBook` 호출하여 연결 추가.
