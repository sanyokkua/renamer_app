# Java Developer Skill

## Purpose

This skill enables AI agents to write high-quality Java code that follows modern best practices (Java 21+ / LTS through Java 25), is clean,
maintainable, testable, and well-documented. The focus is on **writing code that humans can understand**, avoiding common pitfalls, and producing
documentation that communicates **what code does and how to use it** — not marketing language or implementation noise.

---

## Core Philosophy

### 1. Code Quality Principles

**Be clear, not clever:**

- ✅ Code that any developer can read and understand
- ✅ Simple, straightforward solutions to problems
- ✅ Self-documenting names and structure
- ❌ "Clever" one-liners that require mental gymnastics
- ❌ Complexity as a badge of honor

**Write for the next maintainer:**

- Code is read far more often than it is written
- The next maintainer may be you in 6 months
- Every design decision should reduce cognitive load

**Immutability by default:**

- Make fields `final` unless mutation is required
- Use Records for data carriers
- Prefer immutable collections where possible
- Mutable state is the source of most bugs

### 2. Documentation Philosophy

**Document the WHAT and WHY, not the HOW:**

- ✅ What the code does (its purpose and contract)
- ✅ Why constraints or design decisions exist
- ✅ How to use the API (parameters, returns, exceptions)
- ❌ Implementation details (unless critical for understanding)
- ❌ Subjective praise ("robust", "fast", "powerful", "elegant")

**Javadoc serves:**

- API users who need to know how to call methods
- Future maintainers who need to understand intent
- Tools (IDEs, javadoc generator) that produce reference docs
- Quality tools (SonarQube, Checkstyle) that verify compliance

---

## Modern Java Language Features (Java 21+)

### Records (Prefer over Lombok for DTOs)

**Use Java Records for immutable data carriers:**

```java
// ✅ GOOD: Clean, immutable, auto-generated equals/hashCode/toString
public record UserResponse(
    Long id,
    String username,
    String email,
    LocalDateTime createdAt
) {}

// ✅ GOOD: Record with validation in compact constructor
public record EmailAddress(String value) {
    public EmailAddress {
        if (value == null || !value.contains("@")) {
            throw new IllegalArgumentException("Invalid email: " + value);
        }
    }
}
```

**When to use Lombok instead:**

- Mutable state required (JPA entities with setters)
- Builder pattern for complex objects (`@Builder`)
- Logging annotation (`@Slf4j`)

**Avoid `@Data` on JPA entities** — it generates `equals/hashCode` that cause performance issues with lazy loading.

### Var (Type Inference)

**Use `var` when the type is obvious from the right-hand side:**

```java
// ✅ GOOD: Type is clear from constructor
var users = new ArrayList<User>();
var config = new HashMap<String, Object>();
var stream = list.stream();

// ❌ BAD: Type is unclear
var result = service.process(data);  // What type is result?
var x = 10;  // Is this int? Could need long
```

### Pattern Matching

**Use pattern matching for switch and instanceof:**

```java
// ✅ GOOD: Pattern matching switch (Java 21+)
String describe(Object obj) {
    return switch (obj) {
        case Integer i when i > 100 -> "Large integer: " + i;
        case Integer i -> "Small integer: " + i;
        case String s when s.isBlank() -> "Empty string";
        case String s -> "String: " + s;
        case null -> "Null value";
        default -> "Unknown: " + obj.getClass().getSimpleName();
    };
}

// ✅ GOOD: Pattern matching instanceof (eliminates casting)
if (shape instanceof Circle c) {
    return Math.PI * c.radius() * c.radius();
}

// ❌ BAD: Old-style instanceof with manual cast
if (shape instanceof Circle) {
    Circle c = (Circle) shape;
    return Math.PI * c.radius() * c.radius();
}
```

### Text Blocks

**Use text blocks for multiline strings (SQL, JSON, HTML):**

```java
// ✅ GOOD: Readable SQL
String sql = """
                SELECT u.id, u.username, u.email
                FROM users u
                WHERE u.status = 'ACTIVE'
                  AND u.created_at > :since
                ORDER BY u.created_at DESC
                """;

// ✅ GOOD: Readable JSON
String json = """
        {
            "name": "%s",
            "email": "%s",
            "active": true
        }
        """.formatted(name, email);
```

### Sealed Classes

**Use sealed classes to define restricted type hierarchies:**

```java
// ✅ GOOD: Exhaustive switch possible, clear API contract
public sealed interface Shape permits Circle, Rectangle, Triangle {}

public record Circle(double radius) implements Shape {}
public record Rectangle(double width, double height) implements Shape {}
public record Triangle(double base, double height) implements Shape {}

// Compiler ensures all cases are handled
double area(Shape shape) {
    return switch (shape) {
        case Circle c -> Math.PI * c.radius() * c.radius();
        case Rectangle r -> r.width() * r.height();
        case Triangle t -> 0.5 * t.base() * t.height();
    };
}
```

---

## Concurrency (Modern Era)

### Virtual Threads (Project Loom)

**Virtual Threads are the standard for I/O-bound tasks in Java 21+:**

```java
// ✅ GOOD: Virtual thread per task executor
try(var executor = Executors.newVirtualThreadPerTaskExecutor()){
List<Future<String>> futures = urls.stream()
                                   .map(url -> executor.submit(() -> fetchData(url)))
                                   .toList();
    
    for(
Future<String> future :futures){

processResult(future.get());
        }
        }

// ✅ GOOD: Direct virtual thread creation
        Thread.

startVirtualThread(() ->{

// I/O-bound work here
processRequest(request);
});
```

**Virtual Thread Rules:**

- ✅ Use for I/O-bound tasks (HTTP requests, database calls, file I/O)
- ✅ Write "blocking" code that scales like async code
- ❌ DO NOT pool virtual threads (they are cheap to create)
- ❌ Avoid for CPU-bound computation (use platform threads)

### Structured Concurrency

**Use `StructuredTaskScope` for coordinated concurrent tasks:**

```java
// ✅ GOOD: Structured concurrency (Preview in 21, standard soon)
try(var scope = new StructuredTaskScope.ShutdownOnFailure()){
Subtask<User> userTask = scope.fork(() -> fetchUser(userId));
Subtask<List<Order>> ordersTask = scope.fork(() -> fetchOrders(userId));
    
    scope.

join();           // Wait for both
    scope.

throwIfFailed();  // Propagate exceptions
    
    return new

UserWithOrders(userTask.get(),ordersTask.

get());
        }
```

### Thread Safety

**Choose the right synchronization mechanism:**

| Scenario                  | Use                             |
|---------------------------|---------------------------------|
| Global cache/shared state | `ConcurrentHashMap`             |
| Read-heavy lists          | `CopyOnWriteArrayList`          |
| Producer-consumer         | `BlockingQueue` implementations |
| Fine-grained locking      | `ReentrantLock`                 |
| High-read/low-write       | `StampedLock` (optimistic)      |

**Avoid `synchronized` on methods** — it locks the entire object.

### Scoped Values (Replacing ThreadLocal)

**Use Scoped Values for request context (Java 21+ preview):**

```java
// ✅ GOOD: Scoped values (efficient with virtual threads)
private static final ScopedValue<RequestContext> CONTEXT = ScopedValue.newInstance();

void handleRequest(Request request) {
    ScopedValue.runWhere(CONTEXT, new RequestContext(request), () -> {
        // Context available to all called methods
        processRequest();
    });
}

// ❌ AVOID: ThreadLocal (expensive with virtual threads)
private static final ThreadLocal<RequestContext> CONTEXT = new ThreadLocal<>();
```

---

## Code Structure and Organization

### Project Structure

**Follow Maven/Gradle standard layout:**

```
src/
├── main/
│   ├── java/
│   │   └── com/company/project/
│   │       ├── controller/      # REST controllers
│   │       ├── service/         # Business logic
│   │       ├── repository/      # Data access
│   │       ├── model/           # Domain entities
│   │       ├── dto/             # Data transfer objects
│   │       ├── config/          # Configuration classes
│   │       ├── exception/       # Custom exceptions
│   │       └── util/            # Utility classes
│   └── resources/
│       ├── application.yml
│       └── db/migration/        # Flyway/Liquibase
└── test/
    ├── java/                    # Test classes (mirror main structure)
    └── resources/               # Test configuration
```

**Alternative: Feature-based packaging (for larger codebases):**

```
src/main/java/com/company/project/
├── user/
│   ├── UserController.java
│   ├── UserService.java
│   ├── UserRepository.java
│   └── User.java
├── order/
│   ├── OrderController.java
│   ├── OrderService.java
│   └── ...
└── common/
    ├── exception/
    └── config/
```

### Source File Structure

**Order elements consistently within each class:**

1. Static fields (constants first, then static variables)
2. Instance fields
3. Constructors
4. Public methods
5. Package-private methods
6. Protected methods
7. Private methods
8. Inner classes/enums

```java
public class UserService {
    
    // 1. Static fields
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final int MAX_RESULTS = 100;
    
    // 2. Instance fields
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    // 3. Constructors
    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }
    
    // 4. Public methods
    public User findById(Long id) { ... }
    
    public User create(CreateUserRequest request) { ... }
    
    // 5-7. Private/helper methods
    private void validateEmail(String email) { ... }
}
```

### Method Length and Complexity

**Keep methods short and focused:**

- Target: 10-20 lines per method
- Maximum: fits on one screen (roughly 30-40 lines)
- If longer, extract helper methods

**Keep classes focused:**

- Single responsibility
- If a class has many unrelated methods, split it
- Typical class: 100-300 lines (warning sign above 500)

---

## Naming Conventions

### Standard Java Conventions

| Element         | Convention                   | Example                              |
|-----------------|------------------------------|--------------------------------------|
| Classes         | PascalCase, nouns            | `UserService`, `OrderValidator`      |
| Interfaces      | PascalCase, adjectives/nouns | `Serializable`, `UserRepository`     |
| Methods         | camelCase, verbs             | `findById()`, `calculateTotal()`     |
| Variables       | camelCase, nouns             | `userName`, `orderCount`             |
| Constants       | SCREAMING_SNAKE_CASE         | `MAX_RETRY_COUNT`, `DEFAULT_TIMEOUT` |
| Packages        | lowercase, no underscores    | `com.company.userservice`            |
| Type Parameters | Single uppercase letter      | `<T>`, `<K, V>`, `<E>`               |

### Meaningful Names

**Names should explain intent without comments:**

```java
// ❌ BAD: Abbreviations and unclear names
int d; // elapsed time in days
List<User> list1;

void calc();

// ✅ GOOD: Self-documenting names
int elapsedTimeInDays;
List<User> activeUsers;

void calculateMonthlyRevenue();
```

**Include units in variable names when relevant:**

```java
// ❌ BAD: What unit is this?
int timeout;
long fileSize;

// ✅ GOOD: Units are clear
int timeoutInMillis;
long fileSizeInBytes;
Duration connectionTimeout;  // Or use Duration type
```

**Method names should describe the action:**

```java
// ❌ BAD: Vague names
User get(Long id);

void process(Order order);

boolean check(String email);

// ✅ GOOD: Action is clear
User findById(Long id);

void submitForProcessing(Order order);

boolean isValidEmail(String email);
```

---

## Object-Oriented Design

### SOLID Principles

**S — Single Responsibility:**

```java
// ❌ BAD: Class does too many things
public class UserManager {
    public User create(UserDto dto) { ... }
    public void sendWelcomeEmail(User user) { ... }
    public String generateReport(List<User> users) { ... }
    public void exportToCsv(List<User> users) { ... }
}

// ✅ GOOD: Separate responsibilities
public class UserService {
    public User create(UserDto dto) { ... }
}

public class UserNotificationService {
    public void sendWelcomeEmail(User user) { ... }
}

public class UserReportGenerator {
    public String generateReport(List<User> users) { ... }
}
```

**O — Open/Closed (open for extension, closed for modification):**

```java
// ✅ GOOD: New payment types don't require modifying existing code
public interface PaymentProcessor {
    void process(Payment payment);
}

public class CreditCardProcessor implements PaymentProcessor { ... }
public class PayPalProcessor implements PaymentProcessor { ... }
// Add new processor without changing existing ones
public class CryptoProcessor implements PaymentProcessor { ... }
```

**L — Liskov Substitution:**

```java
// ❌ BAD: Square violates Rectangle's contract
public class Rectangle {
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
}

public class Square extends Rectangle {
    @Override
    public void setWidth(int width) {
        this.width = width;
        this.height = width;  // Breaks expectations!
    }
}

// ✅ GOOD: Use composition or separate hierarchies
public sealed interface Shape permits Rectangle, Square { ... }
```

**I — Interface Segregation:**

```java
// ❌ BAD: Fat interface forces unnecessary implementations
public interface Worker {
    void work();
    void eat();
    void sleep();
}

// ✅ GOOD: Focused interfaces
public interface Workable {
    void work();
}

public interface Feedable {
    void eat();
}
```

**D — Dependency Inversion:**

```java
// ❌ BAD: Depends on concrete implementation
public class OrderService {
    private final MySqlOrderRepository repository = new MySqlOrderRepository();
}

// ✅ GOOD: Depends on abstraction, injected via constructor
public class OrderService {
    private final OrderRepository repository;
    
    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }
}
```

### Interfaces vs Abstract Classes

**Interfaces (preferred default):**

- Define **capabilities** or **contracts**
- Use `default` methods for optional behavior
- Multiple inheritance allowed

**Abstract Classes (use sparingly):**

- When sharing **state** (fields) between subclasses
- When enforcing a template method pattern
- When the "is-a" relationship is truly hierarchical

### Composition over Inheritance

**Prefer composition for code reuse:**

```java
// ❌ BAD: Inheritance for code reuse
public class GamingComputer extends BasicComputer {
    // Tightly coupled, hard to change
}

// ✅ GOOD: Composition
public class Computer {
    private final Processor processor;
    private final Memory memory;
    private final GraphicsCard graphics;
    
    public Computer(Processor processor, Memory memory, GraphicsCard graphics) {
        this.processor = processor;
        this.memory = memory;
        this.graphics = graphics;
    }
}
```

---

## Streams and Functional Programming

### When to Use Streams

**Use Streams for:**

- Data processing pipelines (filter, map, reduce)
- Declarative, readable transformations
- Parallel processing (`parallelStream()`)

**Use traditional loops for:**

- Simple iterations
- Index-based access needed
- Performance-critical "hot paths"
- Early exit with complex conditions

### Stream Best Practices

```java
// ✅ GOOD: Clear, readable stream pipeline
List<String> activeUserEmails = users.stream()
                                     .filter(User::isActive)
                                     .filter(user -> user.getCreatedAt().isAfter(cutoffDate))
                                     .map(User::getEmail)
                                     .sorted()
                                     .toList();

// ❌ BAD: "God Stream" - too long, hard to debug
var result = orders.stream()
                   .filter(o -> o.getStatus() == Status.PENDING)
                   .filter(o -> o.getCreatedAt().isAfter(lastWeek))
                   .map(o -> new OrderDto(o.getId(), o.getCustomerId(), o.getTotal()))
                   .filter(dto -> dto.total().compareTo(minAmount) > 0)
                   .sorted(Comparator.comparing(OrderDto::total).reversed())
                   .limit(100)
                   .map(dto -> enrichWithCustomerData(dto))
                   .filter(dto -> dto.customer().isActive())
                   .collect(Collectors.groupingBy(OrderDto::customerId));

// ✅ GOOD: Break into meaningful steps
var pendingOrders = filterPendingOrdersSince(orders, lastWeek);
var qualifiedOrders = filterByMinimumAmount(pendingOrders, minAmount);
var enrichedOrders = enrichWithCustomerData(qualifiedOrders);
var result = groupByCustomer(enrichedOrders);
```

### Use Standard Functional Interfaces

**Prefer `java.util.function` interfaces:**

| Interface             | Signature     | Use Case               |
|-----------------------|---------------|------------------------|
| `Predicate<T>`        | `T → boolean` | Filtering              |
| `Function<T, R>`      | `T → R`       | Transformation         |
| `Consumer<T>`         | `T → void`    | Side effects           |
| `Supplier<T>`         | `() → T`      | Lazy creation          |
| `BiFunction<T, U, R>` | `(T, U) → R`  | Two-arg transformation |

```java
// ✅ GOOD: Use standard interfaces
public List<User> findUsers(Predicate<User> filter) {
    return users.stream()
        .filter(filter)
        .toList();
}

// Usage
var adults = findUsers(user -> user.getAge() >= 18);
var active = findUsers(User::isActive);
```

---

## Exception Handling

### Exception Design

**Use the right exception type:**

| Type                | When to Use                                    |
|---------------------|------------------------------------------------|
| Checked exceptions  | Recoverable conditions caller should handle    |
| Unchecked (Runtime) | Programming errors, unrecoverable conditions   |
| Custom exceptions   | Domain-specific errors with meaningful context |

**Create custom exceptions with context:**

```java
// ✅ GOOD: Custom exception with context
public class UserNotFoundException extends RuntimeException {
    private final Long userId;
    
    public UserNotFoundException(Long userId) {
        super("User not found with ID: " + userId);
        this.userId = userId;
    }
    
    public Long getUserId() {
        return userId;
    }
}

// Usage
public User findById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));
}
```

### Exception Handling Best Practices

```java
// ❌ BAD: Empty catch block
try{
processFile(path);
}catch(
IOException e){
        // Silent failure - debugging nightmare
        }

// ❌ BAD: Catching Exception (too broad)
        try{

processData(data);
}catch(
Exception e){
        log.

error("Error",e);
}

// ✅ GOOD: Specific exception, meaningful handling
        try{

processFile(path);
}catch(
FileNotFoundException e){
        log.

warn("File not found: {}, using defaults",path);
    return

loadDefaults();
}catch(
IOException e){
        throw new

DataProcessingException("Failed to process file: "+path, e);
}
```

**Always use try-with-resources:**

```java
// ✅ GOOD: Resources automatically closed
try(var reader = Files.newBufferedReader(path);
var writer = Files.newBufferedWriter(outputPath)){
String line;
    while((line =reader.

readLine())!=null){
        writer.

write(processLine(line));
        }
        }
```

### Global Exception Handling (Spring)

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        log.warn("User not found: {}", ex.getUserId());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("USER_NOT_FOUND", ex.getMessage()));
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ConstraintViolationException ex) {
        var errors = ex.getConstraintViolations().stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .toList();
        return ResponseEntity
            .badRequest()
            .body(new ErrorResponse("VALIDATION_ERROR", errors.toString()));
    }
}

public record ErrorResponse(String code, String message) {}
```

---

## Null Safety

### Avoid Returning Null

```java
// ❌ BAD: Returns null
public User findByEmail(String email) {
    return userRepository.findByEmail(email);  // May return null
}

// ✅ GOOD: Return Optional
public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
}

// ✅ GOOD: Return empty collection instead of null
public List<Order> findOrdersByUser(Long userId) {
    var orders = orderRepository.findByUserId(userId);
    return orders != null ? orders : List.of();
}
```

### Use Optional Correctly

```java
// ✅ GOOD: Optional usage
Optional<User> userOpt = findByEmail(email);

// Get with default
User user = userOpt.orElse(defaultUser);

// Get or throw
User user = userOpt.orElseThrow(() -> new UserNotFoundException(email));

// Transform if present
String name = userOpt.map(User::getName).orElse("Unknown");

// ❌ BAD: Don't use Optional for fields or parameters
public class User {
    private Optional<String> middleName;  // DON'T
}

public void process(Optional<String> input) {  // DON'T
}

// ✅ GOOD: Use @Nullable annotation instead for fields
public class User {
    @Nullable
    private String middleName;
}
```

### Null Annotations

**Use `@Nullable` and `@NonNull` annotations:**

```java
import org.springframework.lang.Nullable;
import org.springframework.lang.NonNull;

public class UserService {
    
    @Nullable
    public User findByEmail(@NonNull String email) {
        Objects.requireNonNull(email, "Email must not be null");
        return userRepository.findByEmail(email);
    }
}
```

---

## Spring Boot Best Practices

### Dependency Injection

**Always use constructor injection:**

```java
// ✅ GOOD: Constructor injection (immutable, testable)
@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }
}

// With Lombok
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;
}

// ❌ BAD: Field injection (hard to test, hides dependencies)
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
}
```

### Controller Layer

**Controllers handle routing only:**

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        log.info("Fetching user with id: {}", id);
        return ResponseEntity.ok(userService.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        log.info("Creating user with email: {}", request.email());
        var user = userService.create(request);
        return ResponseEntity
            .created(URI.create("/api/v1/users/" + user.id()))
            .body(user);
    }
}
```

### Service Layer

**Services contain business logic:**

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        log.info("Creating user: {}", request.email());
        
        validateEmailNotTaken(request.email());
        
        var user = User.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .name(request.name())
            .build();
        
        var saved = userRepository.save(user);
        emailService.sendWelcomeEmail(saved);
        
        return toResponse(saved);
    }
    
    private void validateEmailNotTaken(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
    }
}
```

### Configuration

**Use `@ConfigurationProperties` for type-safe config:**

```java
// ✅ GOOD: Type-safe configuration
@ConfigurationProperties(prefix = "app.email")
public record EmailProperties(
    String fromAddress,
    String smtpHost,
    int smtpPort,
    Duration timeout
) {}

// application.yml
app:
  email:
    from-address: noreply@example.com
    smtp-host: smtp.example.com
    smtp-port: 587
    timeout: 30s

// Enable in main class
@EnableConfigurationProperties(EmailProperties.class)
@SpringBootApplication
public class Application { ... }

// ❌ BAD: Scattered @Value annotations
@Value("${app.email.from-address}")
private String fromAddress;

@Value("${app.email.smtp-host}")
private String smtpHost;
```

### Request/Response DTOs

**Use Records for DTOs:**

```java
// Request DTO with validation
public record CreateUserRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password,
    
    @NotBlank(message = "Name is required")
    String name
) {}

// Response DTO
public record UserResponse(
    Long id,
    String email,
    String name,
    LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getCreatedAt()
        );
    }
}
```

---

## Testing

### Test Structure

**Follow the AAA pattern (Arrange, Act, Assert):**

```java
@Test
void createUser_WithValidData_ReturnsCreatedUser() {
    // Arrange
    var request = new CreateUserRequest("test@example.com", "password123", "Test User");
    when(userRepository.existsByEmail(any())).thenReturn(false);
    when(userRepository.save(any())).thenAnswer(inv -> {
        User user = inv.getArgument(0);
        return user.toBuilder().id(1L).build();
    });
    
    // Act
    var result = userService.create(request);
    
    // Assert
    assertThat(result.email()).isEqualTo("test@example.com");
    assertThat(result.id()).isNotNull();
    verify(emailService).sendWelcomeEmail(any());
}
```

### Test Naming

**Use descriptive test names:**

```java
// ✅ GOOD: Describes scenario and expected outcome
@Test
void findById_WhenUserExists_ReturnsUser() { }

@Test
void findById_WhenUserNotFound_ThrowsException() { }

@Test
void create_WithDuplicateEmail_ThrowsEmailAlreadyExistsException() { }

// ❌ BAD: Unclear what is being tested
@Test
void testFindById() { }

@Test
void test1() { }
```

### Use AssertJ

```java
// ✅ GOOD: Fluent, readable assertions
assertThat(users).

hasSize(3);

assertThat(users).

extracting(User::getEmail).

contains("test@example.com");

assertThat(result).

isNotNull();

assertThat(exception).

hasMessageContaining("not found");

// ❌ BAD: JUnit assertions (less readable)
assertEquals(3,users.size());

assertTrue(users.stream().

anyMatch(u ->u.

getEmail().

equals("test@example.com")));
```

### Integration Tests with Testcontainers

```java
@SpringBootTest
@Testcontainers
class UserRepositoryIT {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void save_PersistsUserToDatabase() {
        var user = User.builder()
            .email("test@example.com")
            .name("Test User")
            .build();
        
        var saved = userRepository.save(user);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(userRepository.findById(saved.getId())).isPresent();
    }
}
```

---

## Javadoc Documentation

### Documentation Requirements

**Document all public API:**

- Public classes and interfaces
- Public methods and constructors
- Public fields and constants

**DO NOT document:**

- Private methods (use inline comments if needed)
- Obvious getters/setters
- Implementation details

### Javadoc Format

```java
/**
 * Manages user accounts and authentication.
 *
 * <p>This service handles user creation, retrieval, and authentication.
 * All user modifications are transactional.
 *
 * @author Development Team
 * @since 1.0
 * @see UserRepository
 */
@Service
public class UserService {
    
    /**
     * Creates a new user account.
     *
     * <p>The email must be unique across all users. The password will be
     * encoded before storage. A welcome email is sent upon successful creation.
     *
     * @param request the user creation request containing email, password, and name
     * @return the created user response with generated ID
     * @throws EmailAlreadyExistsException if a user with the given email exists
     * @throws IllegalArgumentException if any required field is null or empty
     */
    @Transactional
    public UserResponse create(CreateUserRequest request) { ... }
    
    /**
     * Finds a user by their unique identifier.
     *
     * @param id the user's unique identifier, must not be null
     * @return the user response
     * @throws UserNotFoundException if no user exists with the given ID
     */
    public UserResponse findById(Long id) { ... }
}
```

### Javadoc Anti-Patterns

```java
// ❌ BAD: Marketing language
/**
 * Powerful and efficient user management system that robustly handles
 * all user operations with blazing fast performance.
 */

// ❌ BAD: Restates the method name
/**
 * Gets the user by ID.
 * @param id the ID
 * @return the user
 */
public User getUserById(Long id) { }

// ❌ BAD: Documents implementation
/**
 * Uses HashMap internally to cache results, iterates through the list
 * using a for loop, and calls StringBuilder.append() for each element.
 */

// ✅ GOOD: Describes contract and behavior
/**
 * Retrieves a user by their unique identifier.
 *
 * @param id the user's unique identifier, must be positive
 * @return the user with the specified ID
 * @throws UserNotFoundException if no user exists with the given ID
 * @throws IllegalArgumentException if id is null or not positive
 */
public User findById(Long id) { }
```

### Inline Comments

**Use inline comments ONLY for:**

- Explaining **why** something non-obvious is done
- Documenting workarounds or trade-offs
- Clarifying complex algorithms

```java
// ❌ BAD: States the obvious
counter++; // increment counter

// ❌ BAD: Describes what code does
// loop through users and filter active ones
for (User user : users) {
    if (user.isActive()) {
        activeUsers.add(user);
    }
}

// ✅ GOOD: Explains why
// Use insertion sort for small arrays (< 10 elements) as it outperforms
// quicksort due to lower overhead
if (array.length < 10) {
    insertionSort(array);
}

// ✅ GOOD: Documents a workaround
// Work around JPA limitation: batch inserts require identity generation to be disabled
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE)
private Long id;
```

---

## Code Smells to Avoid

### Bloaters

| Smell                              | Solution                     |
|------------------------------------|------------------------------|
| Long Method (> 20-30 lines)        | Extract helper methods       |
| Large Class                        | Split by responsibility      |
| Long Parameter List (> 3-4 params) | Use parameter objects        |
| Primitive Obsession                | Create value objects/records |

### Object-Orientation Abusers

| Smell           | Solution                 |
|-----------------|--------------------------|
| Switch on type  | Use polymorphism         |
| Refused Bequest | Use composition instead  |
| Temporary Field | Pass as method parameter |

### Change Preventers

| Smell                | Solution                             |
|----------------------|--------------------------------------|
| Divergent Change     | Split class by concern               |
| Shotgun Surgery      | Consolidate related code             |
| Parallel Inheritance | Merge hierarchies or use composition |

### Dispensables

| Smell                  | Solution                    |
|------------------------|-----------------------------|
| Dead Code              | Delete it                   |
| Duplicate Code         | Extract common method/class |
| Lazy Class             | Inline or merge             |
| Speculative Generality | Remove unused abstractions  |

### Couplers

| Smell          | Solution                           |
|----------------|------------------------------------|
| Feature Envy   | Move method to the class it envies |
| Message Chains | Introduce intermediate methods     |
| Middle Man     | Remove unnecessary delegation      |

---

## Libraries and Ecosystem

### Recommended Libraries

| Category            | Library                      | Purpose                       |
|---------------------|------------------------------|-------------------------------|
| JSON                | Jackson                      | Serialization/deserialization |
| Mapping             | MapStruct                    | DTO mapping (compile-time)    |
| Validation          | Hibernate Validator          | Bean validation               |
| Logging             | SLF4J + Logback              | Logging facade                |
| Utilities           | Apache Commons Lang3         | String/Object utilities       |
| Testing             | JUnit 5 + AssertJ + Mockito  | Unit testing                  |
| Integration Testing | Testcontainers               | Docker-based tests            |
| Resilience          | Resilience4j                 | Circuit breakers, retries     |
| HTTP Client         | Java HttpClient or WebClient | HTTP calls                    |

### Libraries to Avoid

| Avoid               | Reason                   | Use Instead         |
|---------------------|--------------------------|---------------------|
| `java.util.Date`    | Mutable, poorly designed | `java.time.*`       |
| `java.io.File`      | Limited API              | `java.nio.file.*`   |
| ModelMapper         | Reflection-based, slow   | MapStruct           |
| Apache HttpClient 4 | Outdated                 | Java 11+ HttpClient |

---

## Logging

### Logging Best Practices

```java
// ✅ GOOD: Use SLF4J with parameterized messages
private static final Logger log = LoggerFactory.getLogger(UserService.class);

public User create(CreateUserRequest request) {
    log.info("Creating user with email: {}", request.email());
    
    try {
        var user = userRepository.save(toEntity(request));
        log.debug("User created with ID: {}", user.getId());
        return user;
    } catch (DataIntegrityViolationException e) {
        log.error("Failed to create user with email: {}", request.email(), e);
        throw new EmailAlreadyExistsException(request.email());
    }
}

// ❌ BAD: String concatenation (evaluated even if log level disabled)
log.debug("Processing user: " + user.toString());

// ❌ BAD: Using System.out
System.out.println("User created");
```

### Log Levels

| Level | Use For                                                       |
|-------|---------------------------------------------------------------|
| ERROR | Unexpected failures requiring investigation                   |
| WARN  | Recoverable issues, degraded functionality                    |
| INFO  | Significant business events (request received, order created) |
| DEBUG | Detailed flow for debugging                                   |
| TRACE | Very detailed debugging (rarely used)                         |

---

## Build Tools

### Gradle (Kotlin DSL) - Recommended for New Projects

```kotlin
// build.gradle.kts
plugins {
    java
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:postgresql")
}
```

### Maven - Stable and Widely Used

```xml
<!-- pom.xml -->
<properties>
    <java.version>21</java.version>
    <mapstruct.version>1.5.5.Final</mapstruct.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!-- Other dependencies -->
</dependencies>
```

---

## Quality Tools

### Static Analysis

| Tool                | Purpose                      |
|---------------------|------------------------------|
| SonarQube/SonarLint | Comprehensive code quality   |
| Checkstyle          | Style/formatting enforcement |
| SpotBugs            | Bug detection                |
| PMD                 | Code quality rules           |
| Error Prone         | Compile-time bug detection   |

### IDE Configuration

**IntelliJ IDEA recommended settings:**

- Enable "Optimize imports on the fly"
- Enable "Add unambiguous imports on the fly"
- Configure code style to team standard
- Enable Checkstyle/SonarLint plugins

---

## Agent Workflow

When writing Java code, follow this process:

### Step 1: Understand Requirements

- Clarify the problem being solved
- Identify inputs, outputs, and constraints
- Determine integration points

### Step 2: Design Structure

- Identify classes and their responsibilities (single responsibility)
- Define interfaces for abstractions
- Plan package structure

### Step 3: Write Code

- Start with public API (interfaces, method signatures)
- Use modern Java features (Records, var, pattern matching)
- Apply SOLID principles
- Keep methods short and focused
- Use meaningful names

### Step 4: Handle Edge Cases

- Validate inputs
- Handle null values appropriately
- Document and handle exceptions
- Consider concurrency implications

### Step 5: Add Documentation

- Write Javadoc for public API
- Focus on WHAT and WHY, not HOW
- Document parameters, returns, exceptions
- Add examples for complex APIs

### Step 6: Verify Quality

- [ ] Methods are short (< 20 lines)
- [ ] Classes have single responsibility
- [ ] No code smells
- [ ] Proper exception handling
- [ ] Null-safe design
- [ ] Meaningful names throughout
- [ ] Javadoc on public API
- [ ] No marketing language in docs

---

## Quick Reference Checklist

### Before Marking Code Complete:

**Structure & Design**

- [ ] Classes have single responsibility
- [ ] Methods are short and focused (< 20 lines)
- [ ] Proper package organization
- [ ] Constructor injection used (not field injection)
- [ ] Composition preferred over inheritance

**Modern Java**

- [ ] Records used for DTOs and value objects
- [ ] `var` used where type is obvious
- [ ] Pattern matching used where applicable
- [ ] Text blocks for multiline strings
- [ ] `java.time` for dates (not `java.util.Date`)
- [ ] `java.nio.file` for file operations

**Code Quality**

- [ ] Meaningful names for all identifiers
- [ ] No magic numbers (use constants)
- [ ] No code duplication
- [ ] No empty catch blocks
- [ ] Proper exception handling
- [ ] Try-with-resources for closeable resources

**Null Safety**

- [ ] Optional returned instead of null (where appropriate)
- [ ] `@Nullable`/`@NonNull` annotations used
- [ ] Empty collections returned instead of null

**Documentation**

- [ ] Javadoc on all public classes and methods
- [ ] Parameters, returns, exceptions documented
- [ ] No marketing language
- [ ] Inline comments only for "why"

**Testing Considerations**

- [ ] Code is testable (dependencies injectable)
- [ ] No static mutable state
- [ ] Clear input/output contracts

---

## Summary

**The best Java code is:**

1. **Simple** — Solves the problem without unnecessary complexity
2. **Readable** — Any developer can understand it quickly
3. **Maintainable** — Easy to modify without breaking things
4. **Testable** — Dependencies are injectable, contracts are clear
5. **Well-documented** — Javadoc explains what and why, not how

**Remember:**

- Clear code > clever code
- Immutability by default
- Composition over inheritance
- Document the contract, not the implementation
- Modern Java features reduce boilerplate — use them
