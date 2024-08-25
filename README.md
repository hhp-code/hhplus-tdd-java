##  테스트 주도개발 연습 프로젝트

### 1. 개발 과정

#### 1.1 테스트 및 메인 패키지 구성

##### 1.1.1 컨트롤러 테스트 작성 (실패 -> 성공)
- `PointService` 메서드 구현 및 모킹
- `when`, `thenReturn`, `verify`를 활용한 메서드 호출 테스트
- `MockMvc`를 통한 `UserPoint` 반환 테스트

##### 1.1.2 서비스 테스트 작성
- 다양한 시나리오에 대한 테스트 케이스 작성
    - 예외 처리, 정상 동작, 경계값 테스트 등

##### 1.1.3 서비스 클래스 구현 및 패키지 이동

##### 1.1.4 Service와 Repository 분리
- `PointRepository` 인터페이스 및 구현체 생성
- 서비스 테스트 재실행 및 검증

### 2. 개선 과정

#### 2.1 컨트롤러 테스트 개선
- `@ParameterizedTest`와 `@MethodSource`를 활용한 입력값 검증

#### 2.2 동시성 고려사항
1. 동시성 이슈 조사
    - 레이스 컨디션, 데드락, 로스트 업데이트 등
2. 동시성 테스트 구현
    - `countDownLatch`를 활용한 테스트
3. 동시성 문제 해결 방안
    - `ConcurrentLinkedQueue` 및 `AtomicLong` 활용
4. 개선된 로직 테스트 및 검증

#### 2.3 서비스 테스트 실패 원인 분석 및 해결
- `CompletableFuture`를 활용한 비동기 처리 개선

### 3. 마무리

#### 3.1 개선 필요 사항
1. DTO 미사용 문제
2. 응답 객체 개선
    - `ResponseEntity` 활용
    - 컨트롤러 어드바이스를 통한 예외 처리

### 주요 기술 스택
- Spring Boot
- JUnit
- Mockito
- Concurrent 프로그래밍 (ConcurrentLinkedQueue, AtomicLong)
- CompletableFuture

### 학습 포인트
- TDD 방식의 개발 프로세스
- 동시성 이슈 해결 방법
- 테스트 코드 작성 및 리팩토링
- 서비스 계층과 레포지토리 계층의 분리

이 프로젝트를 통해 TDD 기반의 개발 방식과 동시성 문제 해결에 대한 실질적인 경험을 쌓을 수 있었습니다[1][2][3].

