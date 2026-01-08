# Query Optimization & Architecture

도서 쇼핑몰의 특성상 복잡한 연관 관계와 대량의 데이터 조회가 빈번하게 발생합니다. 이를 효율적으로 처리하기 위해 JPA의 기능을 최대한 활용하고, 구조적인 문제를 해결하기 위한 다양한 전략을 적용했습니다.

## 1. 데이터 조회 및 다중 연관 관계 최적화

### 문제 상황 (Problem)
*   **다중 일대다(One-to-Many) 관계**: 도서(`Book`) 엔티티는 태그(`Tag`), 이미지(`BookImage`), 카테고리(`Category`), 저자(`Author`) 등 다수의 일대다 관계를 가집니다.
*   **Fetch Join의 한계**: JPA에서 2개 이상의 컬렉션을 동시에 `Fetch Join`하면 `MultipleBagFetchException`이 발생하거나, 데이터가 카테시안 곱(Cartesian Product)으로 뻥튀기되어 메모리 오류를 유발할 수 있습니다.
*   **N+1 문제**: 지연 로딩(Lazy Loading)으로 설정된 연관 엔티티를 조회할 때, 도서 수만큼 추가 쿼리가 발생하는 N+1 문제가 발생합니다.

### 해결 방안 (Solution)

#### 1. `@BatchSize` 활용
*   연관된 엔티티나 컬렉션을 조회할 때, 지정된 크기만큼 `IN` 쿼리로 묶어서 조회하도록 설정했습니다.
*   이를 통해 N번 실행될 쿼리를 `N / BatchSize` 번으로 획기적으로 줄였습니다.
*   **적용 대상**: `Book`, `Tag`, `Author`, `Category`, `Review`, `Publisher` 등 주요 엔티티 전반에 `@BatchSize(size = 100)` 적용.

```java
@Entity
public class Book {
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private List<BookImage> bookImages = new ArrayList<>();
    
    // ...
}
```

#### 2. Projection Interface & Native Query
*   엔티티 전체를 영속성 컨텍스트에 올리는 것은 메모리 비용이 큽니다.
*   단순 조회용 데이터(목록, 통계 등)는 필요한 필드만 정의한 **Projection Interface**를 사용하여 조회 성능을 최적화했습니다.
*   복잡한 집계나 특정 DB 함수가 필요한 경우 **Native Query**와 매핑하여 사용했습니다.
*   **주요 Projection**: `BookListProjection`, `BookDetailProjection`, `BookAdminProjection` 등.

## 2. 순환 참조 해결 및 계층 분리 (Facade Pattern)

### 문제 상황 (Problem)
*   도서 등록 로직은 `BookService`, `ImageService`, `TagService`, `PublisherService`, `AladinApiService` 등 여러 서비스가 서로를 참조해야 하는 복잡한 구조를 가집니다.
*   이로 인해 서비스 간의 **순환 참조(Circular Dependency)**가 발생하여 애플리케이션 구동이 실패하거나, 결합도가 지나치게 높아지는 문제가 있었습니다.

### 해결 방안 (Solution)

#### Facade Pattern 도입 (`BookFacade`)
*   **계층 분리**: 개별 비즈니스 로직은 각 Service(`BookCoreService`, `ImageService` 등)에 위임하고, 이들을 조합하여 흐름을 제어하는 역할은 `BookFacade`가 전담하도록 분리했습니다.
*   **응집도 향상**: 도서 등록, 수정, 조회와 같은 복합적인 워크플로우를 Facade 레이어에서 오케스트레이션(Orchestration)하여, 하위 서비스들은 서로를 알 필요 없이 본연의 기능에만 집중할 수 있게 되었습니다.

```java
@Service
@RequiredArgsConstructor
public class BookFacade {
    private final BookCoreService bookCoreService;
    private final ImageService imageService;
    private final TagV2Service tagService;
    // ...

    @Transactional
    public void registerBook(BookReqV2DTO request) {
        // 1. 도서 기본 정보 저장 (Core)
        Book book = bookCoreService.registerBook(...);
        
        // 2. 이미지 처리 (Image)
        imageService.addBookImage(book, ...);
        
        // 3. 태그 연결 (Tag)
        tagService.assignTagsToBook(book, ...);
        
        // 각 서비스는 서로를 참조하지 않음
    }
}
```
