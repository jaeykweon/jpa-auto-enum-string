# jpa-auto-enum-string

[![Maven Central](https://img.shields.io/maven-central/v/io.github.jaeykweon/jpa-auto-enum-string-core)](https://central.sonatype.com/artifact/io.github.jaeykweon/jpa-auto-enum-string-core)

**JPA enum 필드마다 `@Enumerated(EnumType.STRING)`을 반복해서 달아야 하는 번거로움을 없애줍니다.**

JPA에서 enum을 DB에 저장할 때 `EnumType.STRING`은 사실상 표준입니다.

기본값인 `ORDINAL`은 enum 값을 정수(`0`, `1`, `2`, ...)로 저장하는데, enum 중간에 값을 추가하거나 순서가 바뀌는 순간 기존 데이터가 아무런 오류 없이 오염됩니다.

`EnumType.STRING`은 enum 이름을 그대로 저장하기 때문에, 값을 추가하거나 순서를 바꿔도 DB 데이터에 영향이 없습니다.

문제는 이 설정을 enum 필드마다 하나하나 직접 선언해야 한다는 점입니다.

이 라이브러리는 그 작업을 자동으로 처리하여 엔티티 코드를 깔끔하게 유지합니다.

<table>
<tr>
<th>이 라이브러리 없이</th>
<th>이 라이브러리 사용 시</th>
</tr>
<tr>
<td>

```java
@Entity
public class Order {
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType;
}
```

</td>
<td>

```java
@Entity
public class Order {
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private DeliveryType deliveryType;
}
```

</td>
</tr>
</table>

## 동작 방식

앱 시작 시 Hibernate 초기화 과정에 개입합니다.

설정된 패키지의 엔티티 클래스를 스캔해 `@Enumerated` 어노테이션이 없는 enum 필드를 찾고, STRING 타입으로 등록합니다.

각 필드에 `@Enumerated(EnumType.STRING)`을 직접 붙인 것과 동일한 결과입니다.

`@Embeddable` 컴포넌트 내부의 enum 필드, `@MappedSuperclass`에서 상속된 필드, `@ElementCollection` 필드의 enum element, 그리고 `@ElementCollection` element로 사용되는 `@Embeddable` 타입 내부의 enum 필드도 처리합니다.

시작 시 적용된 필드 목록이 로그로 출력됩니다:

```
[jpa-auto-enum-string] Applied STRING mapping to 2 enum field(s): Order.status, Order.paymentMethod
```

**필드 매핑에 실패하면 해당 필드를 명시한 에러 메시지와 함께 앱 시작이 중단됩니다:**

```
IllegalStateException: [jpa-auto-enum-string] Failed to apply STRING mapping to Order.status
```

## 요구 사항

- Java 8+
- Hibernate 5.3+

Spring Boot starter를 사용할 때만 Spring Boot가 필요합니다:

| Starter                                     | Spring Boot | Hibernate | 통합 테스트                                                      |
|---------------------------------------------|-------------|-----------|-------------------------------------------------------------|
| `jpa-auto-enum-string-spring-boot2-starter` | 2.1 – 2.7   | 5.3.x     | [integration-spring-boot2](tests/integration-spring-boot2/) |
| `jpa-auto-enum-string-spring-boot3-starter` | 3.x         | 6.x       | [integration-spring-boot3](tests/integration-spring-boot3/) |
| `jpa-auto-enum-string-spring-boot4-starter` | 4.x         | 7.x       | [integration-spring-boot4](tests/integration-spring-boot4/) |

Spring Boot 없이도 사용할 수 있습니다 — [Spring Boot 없이 사용하기](#spring-boot-없이-사용하기)를 참고하세요.

새로운 Spring Boot 및 Hibernate 버전이 출시되면 지원을 추가할 예정입니다.

## 시작하기

### Spring Boot

사용 중인 Spring Boot 버전에 맞는 starter를 추가하세요:

**Spring Boot 2.x**

```gradle
implementation 'io.github.jaeykweon:jpa-auto-enum-string-spring-boot2-starter:{version}'
```

```xml
<dependency>
    <groupId>io.github.jaeykweon</groupId>
    <artifactId>jpa-auto-enum-string-spring-boot2-starter</artifactId>
    <version>{version}</version>
</dependency>
```

**Spring Boot 3.x**

```gradle
implementation 'io.github.jaeykweon:jpa-auto-enum-string-spring-boot3-starter:{version}'
```

```xml
<dependency>
    <groupId>io.github.jaeykweon</groupId>
    <artifactId>jpa-auto-enum-string-spring-boot3-starter</artifactId>
    <version>{version}</version>
</dependency>
```

**Spring Boot 4.x**

```gradle
implementation 'io.github.jaeykweon:jpa-auto-enum-string-spring-boot4-starter:{version}'
```

```xml
<dependency>
    <groupId>io.github.jaeykweon</groupId>
    <artifactId>jpa-auto-enum-string-spring-boot4-starter</artifactId>
    <version>{version}</version>
</dependency>
```

별도 설정 없이 바로 동작합니다. `@SpringBootApplication` 클래스의 패키지와 하위 패키지를 자동으로 스캔합니다.

> ⚠️ **기존 프로젝트에 추가하려는 경우** 진행 전에 [주의사항](#-기존-프로젝트에-추가할-때-주의하세요)을 반드시 읽어보세요.

`base-packages` 설정은 엔티티가 `@SpringBootApplication` 패키지 밖에 있을 때만 필요합니다. 주로 엔티티가 별도 Gradle/Maven 모듈에 있는 경우입니다:

```yaml
jpa:
  auto-enum-string:
    base-packages: com.example.myapp
```

멀티 모듈 프로젝트라면 전체 설정 예시를 [examples/multi-module](examples/multi-module/app/)에서 확인하세요.

설정된 패키지 하위의 엔티티에만 적용됩니다. 서드파티 라이브러리의 엔티티는 건드리지 않습니다.

잘못된 버전의 starter를 추가한 경우(예: Spring Boot 3에 SB2 starter 사용), Hibernate integrator 인터페이스 불일치로 앱 시작이 실패합니다. 올바른 starter는 [요구 사항](#요구-사항) 표를 확인하세요.

## 적용 제외 (Opting out)

아래 조건 중 하나라도 해당하는 필드는 건너뜁니다:

- `@Enumerated`가 있으면 — 선언된 매핑을 그대로 사용
- `@Convert`가 있으면 — 커스텀 컨버터를 우선
- Hibernate `@Type`이 있으면 — 커스텀 타입 매핑을 우선
- `@Transient` 또는 Java `transient` 키워드 — 영속 필드가 아니므로 건너뜀

```java
@Entity
public class Order {
    private OrderStatus status;          // auto: stored as STRING

    @Enumerated(EnumType.ORDINAL)
    private LegacyStatus legacyStatus;   // explicit: stays as ORDINAL

    @Convert(converter = StatusConverter.class)
    private OrderStatus converted;       // custom converter: not overridden

    @Transient
    private OrderStatus tempStatus;      // transient: skipped
}
```

`@Embeddable` 컴포넌트, `@ElementCollection` 필드, 그리고 `@ElementCollection` element로 사용되는 `@Embeddable` 타입 내부의 enum 필드에서도 동일하게 적용됩니다.

```java
@Embeddable
public class ShippingInfo {
    private OrderStatus status;          // auto: stored as STRING

    @Enumerated(EnumType.ORDINAL)
    private OrderStatus legacyStatus;    // explicit: stays as ORDINAL
}
```

```java
@Entity
public class Order {
    @ElementCollection
    private Set<OrderStatus> statuses;            // auto: elements stored as STRING

    @ElementCollection
    @Enumerated(EnumType.ORDINAL)
    private Set<OrderStatus> legacyStatuses;      // explicit: elements stay as ORDINAL
}
```

```java
@Embeddable
public class StatusEntry {
    private OrderStatus status;           // auto: stored as STRING

    @Enumerated(EnumType.ORDINAL)
    private OrderStatus legacyStatus;     // explicit: stays as ORDINAL
}

@Entity
public class Order {
    @ElementCollection
    private Set<StatusEntry> entries;     // embeddable 내부 enum 필드도 자동 처리
}
```

## ⚠️ 기존 프로젝트에 추가할 때 주의하세요

이 라이브러리는 Hibernate가 enum 필드를 읽고 쓰는 방식을 바꿉니다.

DB의 enum 필드가 이미 문자열로 저장되어 있다면 마이그레이션 없이 바로 추가해도 됩니다.

정수(`0`, `1`, `2`)로 저장된 enum 필드가 있다면, 라이브러리를 추가하는 순간 런타임 매핑 오류가 발생하고 기존 레코드를 읽을 수 없게 됩니다.

**마이그레이션 없이 추가해도 괜찮은 경우:**

- 새 프로젝트를 시작하는 경우
- DB의 모든 enum 필드가 이미 문자열로 저장되어 있는 경우 (일반적인 케이스)

**마이그레이션이 필요한 경우:**

1. DB에 정수로 저장된 enum 필드가 있는지 확인
2. 정수 값을 문자열로 마이그레이션 (`0` → `'PENDING'`, `1` → `'COMPLETED'`, ...)
3. 그 다음 이 라이브러리를 추가

**라이브러리를 제거하기 전에, 모든 enum 필드에 `@Enumerated(EnumType.STRING)`을 먼저 명시적으로 추가하세요.**

enum 값이 문자열로 저장된 상태에서 어노테이션 없이 라이브러리를 제거하면, Hibernate가 `ORDINAL`로 돌아가면서 기존 데이터를 읽지 못하게 됩니다:

```
org.springframework.dao.DataIntegrityViolationException:
  Could not extract column from JDBC ResultSet
  [Data conversion error converting "CONFIRMED"]
```

## Spring Boot 없이 사용하기

> ⚠️ **직접 설정할 때는 반드시 `basePackages`를 지정하세요.** 
> 
> 지정하지 않으면 서드파티 라이브러리의 엔티티를 포함해 탐지되는 모든 엔티티 클래스가 스캔됩니다. 자신의 엔티티가 있는 루트 패키지를 지정해 스캔 범위를 제한하세요.

### Hibernate 5.3+

```gradle
implementation 'io.github.jaeykweon:jpa-auto-enum-string-hibernate5-adapter:{version}'
```

```java
AutoEnumStringConfig config = AutoEnumStringConfig.builder()
    .basePackages("com.example.myapp")  // 필수: 자신의 엔티티 루트 패키지
    .build();

BootstrapServiceRegistry bootstrapRegistry = new BootstrapServiceRegistryBuilder()
    .applyIntegrator(new Hibernate5EnumStringIntegrator(config))
    .build();
```

`bootstrapRegistry`를 `StandardServiceRegistryBuilder`에 전달해 `SessionFactory`를 구성하세요.
전체 설정 예시는 [examples/hibernate5-manual](examples/hibernate5-manual/)을 참고하세요.

### Hibernate 6 / 7

```gradle
implementation 'io.github.jaeykweon:jpa-auto-enum-string-hibernate6-adapter:{version}'
```

```java
AutoEnumStringConfig config = AutoEnumStringConfig.builder()
    .basePackages("com.example.myapp")  // 필수: 자신의 엔티티 루트 패키지
    .build();

BootstrapServiceRegistry bootstrapRegistry = new BootstrapServiceRegistryBuilder()
    .applyIntegrator(new Hibernate6EnumStringIntegrator(config))
    .build();
```

`bootstrapRegistry`를 `StandardServiceRegistryBuilder`에 전달해 `SessionFactory`를 구성하세요.
전체 설정 예시는 [examples/hibernate6-manual](examples/hibernate6-manual/)을 참고하세요.

## FAQ

**`@Embeddable`, `@MappedSuperclass`, `@ElementCollection` 지원이 실제로 동작하는지 어떻게 확인할 수 있나요?**

각 기능은 실제 데이터베이스(H2)를 대상으로 하는 통합 테스트로 검증되어 있습니다(Spring Boot 3):

- [`@Embeddable`](tests/integration-spring-boot3/src/test/java/io/github/jaeykweon/jpaautoenumstring/integration/EmbeddableIntegrationTest.java) — embeddable 컴포넌트 내부의 enum 필드
- [`@MappedSuperclass`](tests/integration-spring-boot3/src/test/java/io/github/jaeykweon/jpaautoenumstring/integration/InheritanceIntegrationTest.java) — mapped superclass에서 상속된 enum 필드
- [`@ElementCollection` (plain enum)](tests/integration-spring-boot3/src/test/java/io/github/jaeykweon/jpaautoenumstring/integration/ElementCollectionTest.java) — 컬렉션 필드의 enum element
- [`@ElementCollection` (embeddable element)](tests/integration-spring-boot3/src/test/java/io/github/jaeykweon/jpaautoenumstring/integration/ElementCollectionEmbeddableTest.java) — 컬렉션 element로 사용되는 embeddable 내부의 enum 필드

동일한 테스트가 [Spring Boot 2](tests/integration-spring-boot2/src/test/java/io/github/jaeykweon/jpaautoenumstring/integration/)와 [Spring Boot 4](tests/integration-spring-boot4/src/test/java/io/github/jaeykweon/jpaautoenumstring/integration/)에도 있습니다.

**Lombok과 같이 써도 되나요?**

네. `@Builder`, `@NoArgsConstructor`, `@Getter` 등 Lombok 어노테이션과 충돌 없이 사용할 수 있습니다.
이 라이브러리는 필드에서 직접 어노테이션을 읽기 때문에, Lombok이 생성하는 메서드와는 전혀 관련이 없습니다.

**`AttributeConverter`로 해결하면 되지 않나요?**

크게 두 가지 방법을 생각해볼 수 있습니다. enum 타입마다 `AttributeConverter`를 하나씩 만들어 `@Converter(autoApply = true)`를 적용하는 방법, 또는 모든 enum을 처리하는 제네릭 컨버터 하나를 만드는 방법입니다.

타입별 컨버터 방식은 enum 타입마다 클래스를 직접 작성하거나, 어노테이션 프로세서로 자동 생성해야 합니다. 어느 쪽이든 새로운 enum 타입이 생길 때마다 대응하는 컨버터가 존재해야 하며, 이 부담은 프로젝트의 enum 수에 비례해 커지고, 프로젝트가 커질수록 관리 부담도 늘어납니다.

제네릭 컨버터 방식은 이 API로는 불가능합니다. `convertToEntityAttribute(String dbData)`는 타입 정보를 받지 않기 때문에, 어떤 enum 클래스로 변환해야 하는지 알 수 없습니다.

**왜 어노테이션 프로세서(컴파일 타임)로 만들지 않았나요?**

표준 Java 어노테이션 프로세서 API(`javax.annotation.processing`)는 새로운 소스 파일 생성만 지원하고, 기존 클래스를 수정할 수 없습니다.

컴파일 타임에 기존 엔티티 클래스에 `@Enumerated(STRING)`을 주입하려면 AST 조작이 필요한데, 이는 JDK 내부 비공개 API인 `com.sun.tools.javac`을 써야 한다는 의미입니다.

Lombok이 이 방식을 사용합니다. 이 내부 API는 JDK 버전마다 예고 없이 변경될 수 있어, 장기적인 유지보수 부담이 따릅니다.

Hibernate 자체가 런타임에 동작하는 만큼, Hibernate 레이어에서 동작하는 라이브러리에는 런타임 통합이 가장 자연스러운 선택입니다.

**Hibernate 최소 버전이 왜 5.0이 아니라 5.3인가요?**

Hibernate 5.0/5.1은 Java 8 타입 지원이 코어에 포함되지 않아 별도의 `hibernate-java8` 의존성이 필요했습니다.

또한 이 시기는 Spring Boot 1.4/1.5 시대로, 이 라이브러리의 Spring Boot 최소 지원 버전인 2.1보다 이전입니다.

Spring Boot 2.1이 Hibernate 5.3을 사용하므로, 지원 환경에서 만날 수 있는 가장 낮은 버전이 자연스럽게 5.3이 됩니다.

**SB2 starter의 Spring Boot 최소 버전이 왜 2.1인가요?**

이 라이브러리는 `hibernate.integrator_provider` 프로퍼티로 Hibernate `Integrator`를 등록합니다. Spring Boot에서는 이 프로퍼티를 `EntityManagerFactory` 생성 전에 세팅해야 합니다.

Spring Boot 2.1에서 도입된 `HibernatePropertiesCustomizer`가 바로 그 타이밍에 실행되는 콜백입니다. Spring Boot 2.0에는 이 콜백이 없어서 자동 설정으로 integrator를 등록할 깔끔한 방법이 없었습니다.

Spring Boot 없이 어댑터를 직접 사용하는 경우에는 Spring Boot 버전에 관계없이 Hibernate 5.3+, 6, 7 어느 환경에서도 사용 가능합니다.

**Hibernate의 `hibernate.type.prefer_native_enum_types` 프로퍼티로도 해결되지 않나요?**

이 프로퍼티는 enum을 문자열(VARCHAR)로 저장하는 게 아닙니다.

PostgreSQL의 `CREATE TYPE` enum처럼 DB의 네이티브 ENUM 컬럼 타입을 활성화하는 것으로, 완전히 다른 저장 전략입니다.

또한 명시적인 `@Enumerated(ORDINAL)` 어노테이션도 무시합니다. 의도적으로 ORDINAL로 매핑한 필드가 네이티브 ENUM 타입으로 변경되어, 기존 데이터의 의미가 조용히 바뀔 수 있습니다.

Hibernate 6과 7 모두에서 `@Incubating`(실험적) 상태이며, Hibernate 6.5에서 도입되어 Hibernate 5나 그 이전 버전의 Hibernate 6 환경에서는 사용할 수 없습니다. 

이 라이브러리는 지원하는 모든 Hibernate 버전에서 동작합니다.

## 알려진 제한 사항

### Property-based access (getter에 JPA 어노테이션을 선언하는 방식)

이 라이브러리는 opt-out 어노테이션(`@Enumerated`, `@Convert`, `@Type`)을 **필드**에서만 읽습니다. getter 메서드는 확인하지 않습니다.

`@Id` 등 JPA 어노테이션을 필드가 아닌 getter에 선언하는 property-based access 방식을 사용하면, getter에 붙인 opt-out 어노테이션이 감지되지 않아 의도치 않게 STRING 매핑이 적용될 수 있습니다.

필드에 `@Id`를 선언하는 field-based access(Spring Boot 관례)는 완전히 지원됩니다.

Property-based access는 Spring Boot 프로젝트에서 거의 사용되지 않아 현재는 지원하지 않습니다. 필요하신 경우 [이슈](https://github.com/jaeykweon/jpa-auto-enum-string/issues)에 등록해 주세요.

### `@Convert` 필드 + Hibernate 7 + 테스트 환경의 H2

Spring Boot 4(Hibernate 7)에서 H2 인메모리 테스트 DB를 쓸 때, `@Convert`가 붙은 enum 필드가 있는 테이블에 INSERT하면 아래 오류가 발생할 수 있습니다:

```
Check constraint invalid: "CONSTRAINT_N: ..."
```

Hibernate 7은 `@Convert` 필드에 대해 컨버터 출력 값 기반의 `CHECK` 제약 조건을 생성합니다(예: `CHECK (status IN ('P','C','S','X'))`). H2 2.4.x에 이 형식의 제약 조건을 평가하지 못하는 버그가 있어 INSERT가 실패합니다.

H2 측의 회귀 버그([H2 issue #4302](https://github.com/h2database/h2database/issues/4302))이며, [H2 PR #4311](https://github.com/h2database/h2database/pull/4311)에서 수정되었습니다(2025년 11월 병합, H2 2.4.240 기준 미릴리즈).

**이 라이브러리와는 무관합니다** — 라이브러리 없이도 동일한 오류가 발생합니다. `@Convert` 필드는 이 라이브러리의 처리 대상에서 명시적으로 제외됩니다([적용 제외](#적용-제외-opting-out) 참고).

**H2 수정 버전이 릴리즈되기 전까지의 임시 방편:**

- 테스트 의존성의 H2를 2.3.x로 다운그레이드
- Testcontainers로 실제 DB(예: PostgreSQL)를 띄워 통합 테스트 수행

## 라이선스

[MIT](LICENSE)

## 이슈

버그 리포트, 질문, 그 외 어떤 의견이든 [GitHub Issues](https://github.com/jaeykweon/jpa-auto-enum-string/issues)에 남겨주세요.
