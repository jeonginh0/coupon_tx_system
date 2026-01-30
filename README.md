## 📖 프로젝트 개요

- **목표**
    - 객체지향적인 코드 설계와 트랜잭션 이해도 확인
- **해결해야 할 문제**
    - 쿠폰은 정해진 수량만 발급 가능
    - 발급 가능 시간이 제한됨
    - 다수의 사용자가 동시에 발급 요청
    - 동일 사용자의 중복 발급 방지
    - 동시성 상황에서도 초과 발급 없이 정합성 유지
- **주요기능**
    - 동시에 여러 요청 발생 시 쿠폰이 정확히 N개만 발급되도록 보장


---

## 🙋‍♂️ 역할

- 백엔드 개발
- DB 설계, 시스템 아키텍처 설계

---

## 🔨 기술 스택

- **백엔드 (Back-End)**
    - **`Spring Boot`**
    - **`PostgreSQL`** (데이터베이스)


---

## 💻 프로젝트 구조

```json
coupon
 ├─ controller
 │   └─ CouponController
 ├─ service
 │   └─ CouponIssueService
 │   └─ CouponIssueTransactionalService
 ├─ entity
 │   ├─ Coupon
 │   ├─ CouponStock
 │   └─ CouponIssue
 ├─ repository
 │   ├─ CouponRepository
 │   ├─ CouponStockRepository
 │   └─ CouponIssueRepository
 └─ test
     └─ CouponIssueServiceTest
```

- **계층 역할**
    - **Controller:** 요청 수신 및 전달
    - **Service:** 쿠폰 발급 UseCase + Transaction
    - **Entity:** 비즈니스 규칙과 상태 관리
    - **Repository:** 데이터 접근
    - **Test:** 동시성 및 정합성 검증

---

## 📃 코드 설명

1. **핵심 도메인**
    - Coupon (발급 정책)

      `coupon.validateIssueableTime(now);`

        - 쿠폰 발급 가능 시간을 스스로 검증
        - 시간 정책을 서비스가 아닌 도메인 책임으로 설계
    - CouponStock (한정 자원 관리)

      `@Lock(LockModeType.PESSIMISTIC_WRITE)`

        - 한정 수량 자원에 비관적 락 적용
        - 동시에 접근 시 하나의 트랜잭션만 재고 수정 가능
    - CouponIssue (중복 발급 방지)

      `@Table(uniqueConstraints = @UniqueConstraint(colunmNames = {”userId”, “couponId”}))`

        - DB 레벨에서 동일 유저의 중복 발급 차단
        - 애플리케이션 로직보다 DB를 최종 방어선으로 사용
2. **쿠폰 발급 트랜잭션 흐름**

    ```json
    1. 쿠폰 발급 가능 시간 검증
    2. 재고 수량에 비관적 락 적용
    3. 재고 감소
    4. 발급 이력 저장
    ```

    - 하나의 트랜잭션에서 처리
    - 중간 실패 시 전체 롤백
    - 동시 요청 환경에서도 초과 발급 방지
3. **테스트**
    - **초기 데이터 세팅 (100개 한정 수량의 쿠폰 발행)**

        ```java
        @BeforeEach
        void setUp() {
            Coupon coupon = new Coupon("선착순 쿠폰", LocalDateTime.now(), LocalDateTime.now().plusMinutes(5));
            couponRepository.save(coupon);
        
        		couponStockRepository.save(new CouponStock(coupon.getId(), 100));
        
            couponId = coupon.getId();
        }
        ```

    - **동일 유저 쿠폰 중복 발급 불가 테스트 (성공)**

        ```java
        @Test
        public void 같은_유저는_중복_발급_불가() {
            couponIssueService.issueCoupon(1L, couponId);
        
            assertThatThrownBy(() -> couponIssueService.issueCoupon(1L, couponId))
                .isInstanceOf(Exception.class);
        
            assertThat(couponIssueRepository.count()).isEqualTo(1);
        }
        ```

        - 특정 쿠폰에 한해서 유저당 하나의 쿠폰만 발급가능
    - **발급가능한 특정 재고보다 초과 발급 불가 테스트(성공)**

        ```java
        @Test
        void 동시에_1000명이_요청해도_500개만_발급가능() {
            int threadCount = 1000;
            int stock = 500;
        
            ExecutorService executorService = Executors.newFixedThreadPool(32);
            CountDownLatch latch = new CountDownLatch(threadCount);
        
            for (int i = 0; i < threadCount; i++) {
        		    final long userId = i + 1;
                executorService.submit(() -> {
        		        try {
        				        couponIssueService.issueCoupon(userId, couponId);
        		        } catch (Exception e) {
        						} finally {
        				        latch.countDown();
        		        }
        		    });
            }
        
            latch.await();
        
        	  long issuedCount = couponIssueRepository.count();
            assertThat(issuedCount).isEqualTo(stock);
        }
        ```

        - 동시에 많은 요청이 와도 DB row락이 순차 처리를 진행하여 초과발급을 막고 결과가 항상 동일함을 검증하는 테스트

---

## ❗️ 트러블 슈팅

1. **동시 요청 시 한정수량(500 개)가 아닌 모든 인원(1000 명)에게 발급 (낙관적 락 적용 시)**
    - **원인:** 낙관적 락만으로는 테스트 환경에서 충돌이 발생하지 않았으며 모든 트랜잭션이 순차적으로 않았으며 모든 트랜잭션이 순차적으로 UPDATE가 성공
    - **해결**
        - 한정 자원 특성에 맞게 비관적 락 적용
        - 읽는 순간부터 row 락을 걸어 충돌 원천 차단

---

## 🎯 회고

- **처음 목표**
    - 동시에 많은 사용자가 요청하더라도 정해진 수량만 정확히 발급되는 선착순 쿠폰 발급 시스템을 구현하고자 하였습니다.
    - 단순히 기능을 구현하는 것을 넘어서, 동시성 문제를 코드와 테스트로 직접 경험해보고 트랜잭션과 락이 실제로 어떻게 동작하는지 이해하고자 하였습니다.
- **잘한 점**
    - 낙관적 락과 비관적 락에 대한 차이점을 이해하여 사용해야 하는 상황을 완벽하게 재현했다는 점입니다.
    - 낙관적 락을 적용했을 때 테스트 결과가 기대와 다르게 나왔고, 이를 단순한 구현 실수가 아니라 해당 문제에서 낙관적 락이 적합한지 고민했습니다. 이 과정에서 낙관적 락과 비관적 락의 역할 차이, 선착순 문제에서 순서와 정합성의 의미, 테스트에서 무엇을 검증해야 하는지 도출할 수 있었습니다.
    - 동시 요청 상황을 가정한 테스트를 직접 작성하면서 동시성 문제를 이론이 아니라 코드와 테스트로 확인한 경험도 큰 성과라고 생각합니다.
- **아쉬운 점**
    - 초기 OCR 전처리 규칙이 부족하여 불필요한 재작업이 너무 많았습니다.
    - UI/UX 설계에 충분한 시간을 투자하지 못해, 초반 MVP 버전은 사용자가 직관적으로 이해하기 어려운 부분이 있었지만, 캡스톤 디자인 수업 시간에 발표를 통해 피드백을 얻게 되어 디자인 구성을 마무리할 수 있었습니다.
    - AI 답변의 신뢰성을 높이기 위해 법률 데이터셋을 더 정교하게 구축할 시간이 부족했음.
- **배운 점**
    - AI 서비스는 **데이터 전처리 품질**이 전체 성능을 좌우한다는 점.
    - 풀스택 개발을 직접 경험하면서, 백엔드·프론트엔드·인프라까지 전체 개발 프로세스를 통합적으로 바라보는 시각.
- **마무리**
    - 이번 프로젝트를 통해 OCR·AI 챗봇을 결합한 실서비스를 구현하면서, 단순 기능 구현을 넘어 **데이터 처리와 사용자 경험 개선**이 중요함을 실감했습니다.
    - 향후에는 법률 전문 변호사와 협력하여 법률 데이터셋을 고도화하고, UI/UX를 강화해 서비스 완성도를 높이고 싶습니다.