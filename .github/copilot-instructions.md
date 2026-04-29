# GitHub Copilot Instructions - Kakebo Finance

## Project Overview

**Kakebo** is a personal expense management application based on the Japanese method of the same name.
- **Stack**: Java 25 + Spring Boot + Thymeleaf + Tailwind CSS + H2 (embedded DB)
- **Architecture**: REST API + Frontend MVC
- **Objective**: Provide income management, fixed/variable expenses and savings with clear visualization

---

## SOLID Principles

### 1. Single Responsibility Principle (SRP)
- Each class has ONE unique and well-defined responsibility
- **Layer separation**:
  - `Controller`: HTTP handling and input validation
  - `Service`: Pure business logic
  - `Repository`: Data access (Spring Data JPA)
  - `Entity`: Database mapping
  - `DTO`: Data transfer between layers

**Example**: `ExpenseService` only handles expense logic, not persistence or HTTP.

### 2. Open/Closed Principle (OCP)
- Classes open for extension, closed for modification
- **Use interfaces and abstractions** for components that may vary:
  - `SavingsCalculatorStrategy` for different calculation strategies
  - `AlertNotifier` for different notification channels
- New expense categories = configuration, not code

### 3. Liskov Substitution Principle (LSP)
- Subclasses can replace their parent classes without breaking functionality
- If you use `List<Expense>` with fixed and variable expenses: both must fulfill the contract

### 4. Interface Segregation Principle (ISP)
- Specific interfaces, not monolithic
- ❌ `IExpenseManager` that does everything
- ✅ `IExpenseCreator`, `IExpenseFinder`, `IExpenseUpdater` separated

### 5. Dependency Injection Principle (DIP)
- Injection via **Spring `@Autowired`, `@Service`, `@Repository`**
- Never instantiate services directly: `new ExpenseService()` ❌
- Testability: use `@MockBean` in tests

---

## Code Best Practices

### Backend (Java + Spring Boot)

#### 1. Naming Conventions
- **Entity classes**: `Expense`, `Income`, `User` (nouns, singular)
- **Services**: `ExpenseService`, `IncomeService` (suffix `Service`)
- **Repositories**: `ExpenseRepository` (suffix `Repository`)
- **Controllers**: `ExpenseController` (suffix `Controller`)
- **Boolean variables**: `isFixed`, `isRecurring`, `hasAlert` (prefix `is`, `has`)
- **Private methods**: `calculateMonthlyTotal()` (verb + noun)

#### 2. Class Structure
```java
@Entity
@Table(name = "expenses")
public class Expense {
    // 1. Private attributes
    @Id
    private Long id;
    private BigDecimal amount;

    // 2. Constructor with mandatory parameters
    public Expense(String description, BigDecimal amount, Category category) { }

    // 3. Getters/Setters
    public BigDecimal getAmount() { }

    // 4. Business logic methods
    public boolean isRecurring() { }
}
```

#### 3. Services
```java
@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ExpenseValidator validator;

    // Constructor for injection
    public ExpenseService(ExpenseRepository repo, ExpenseValidator validator) {
        this.expenseRepository = repo;
        this.validator = validator;
    }

    public Expense create(CreateExpenseDTO dto) {
        // 1. Validate
        validator.validate(dto);
        // 2. Convert DTO → Entity
        Expense expense = dto.toExpense();
        // 3. Execute logic
        expense.assignCategoryAutomatically();
        // 4. Persist
        return expenseRepository.save(expense);
    }
}
```

#### 4. Controllers
```java
@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponseDTO> create(@Valid @RequestBody CreateExpenseDTO dto) {
        Expense expense = expenseService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ExpenseResponseDTO.from(expense));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> getById(@PathVariable Long id) {
        return expenseService.getById(id)
            .map(e -> ResponseEntity.ok(ExpenseResponseDTO.from(e)))
            .orElse(ResponseEntity.notFound().build());
    }
}
```

#### 5. DTOs (Data Transfer Objects)
- Use for **input**: `@Valid`, JSR-380 validation
- Use for **output**: Conversion from entities
```java
public record CreateExpenseDTO(
    @NotBlank(message = "Description required")
    String description,

    @Positive(message = "Amount must be positive")
    BigDecimal amount,

    @NotNull
    Category category
) { }

public record ExpenseResponseDTO(
    Long id,
    String description,
    BigDecimal amount
) {
    public static ExpenseResponseDTO from(Expense expense) {
        return new ExpenseResponseDTO(expense.getId(), expense.getDescription(), expense.getAmount());
    }
}
```

#### 6. Validation
- Input validation in DTOs with `@NotNull`, `@Positive`, etc.
- Custom exceptions:
```java
public class InvalidExpenseException extends RuntimeException {
    public InvalidExpenseException(String message) {
        super(message);
    }
}
```

#### 7. Transactions
```java
@Transactional  // For operations that modify multiple entities
public void transferExpense(Long sourceId, Long targetId) {
    Expense source = expenseRepository.findById(sourceId).orElseThrow();
    Expense target = expenseRepository.findById(targetId).orElseThrow();
    // operations...
}
```

### Frontend (Thymeleaf + Tailwind)

#### 1. Naming Conventions
- **Templates**: `expense-list.html`, `income-detail.html` (kebab-case)
- **Thymeleaf variables**: `th:text="${expense.description}"` (camelCase)
- **CSS classes**: `bg-blue-600`, `text-danger`, `btn-primary` (Tailwind)

#### 2. Structure
```html
<!-- expense-list.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="en">
<head>
    <title>Expenses - Kakebo</title>
    <link rel="stylesheet" th:href="@{/css/tailwind.min.css}">
</head>
<body class="bg-gray-50">
    <div class="container mx-auto p-4">
        <!-- Minimalist header -->
        <h1 class="text-3xl font-bold mb-6">My Expenses</h1>

        <!-- Safe iteration with Thymeleaf -->
        <div th:if="${expenses.isEmpty()}" class="text-gray-500">
            No expenses registered.
        </div>
        <ul class="space-y-2">
            <li th:each="expense : ${expenses}" class="p-4 border-l-4 border-blue-600">
                <span th:text="${expense.description}"></span>
                <span class="text-gray-500" th:text="'$' + ${expense.amount}"></span>
            </li>
        </ul>
    </div>
</body>
</html>
```

#### 3. Forms
```html
<form th:action="@{/expenses}" th:object="${expenseForm}" method="POST" class="space-y-4">
    <div>
        <label for="description" class="block font-semibold">Description</label>
        <input type="text" id="description" th:field="*{description}"
               class="w-full p-2 border rounded-lg"
               th:classappend="${#fields.hasErrors('description') ? 'border-red-500' : ''}">
        <span th:if="${#fields.hasErrors('description')}"
              th:errors="*{description}" class="text-red-500 text-sm"></span>
    </div>
</form>
```

#### 4. Minimalist Styles (Tailwind)
- Limited colors: blue (`blue-600`), gray (`gray-50`), red for errors
- Consistent padding/margin: `p-2`, `p-4`, `mb-4`
- Responsive breakpoints: `md:`, `lg:` for mobile-first

---

## Testing

### Unit Tests (JUnit 5 + Mockito)
```java
@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {
    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    void createValidExpense() {
        // Arrange
        CreateExpenseDTO dto = new CreateExpenseDTO("Food", new BigDecimal("25.50"), Category.SURVIVAL);
        Expense expectedExpense = dto.toExpense();
        when(expenseRepository.save(any())).thenReturn(expectedExpense);

        // Act
        Expense result = expenseService.create(dto);

        // Assert
        assertEquals("Food", result.getDescription());
        verify(expenseRepository, times(1)).save(any());
    }

    @Test
    void createExpenseWithNegativeAmountThrowsException() {
        CreateExpenseDTO dto = new CreateExpenseDTO("Food", new BigDecimal("-10"), Category.SURVIVAL);

        assertThrows(InvalidExpenseException.class, () -> expenseService.create(dto));
    }
}
```

### Integration Tests
```java
@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void getExpensesReturns200() throws Exception {
        mockMvc.perform(get("/api/expenses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }
}
```

---

## Code Standards

### Commits
```
[FEAT] Add validation for negative amount
[FIX] Fix monthly savings calculation
[REFACTOR] Extract validation logic to service
[TEST] Increase ExpenseService coverage to 95%
[DOCS] Document POST /expenses endpoint
```

### Files
- **One class per file** (except small Records)
- **Maximum 200 lines per class** (refactor if exceeding)
- **Maximum 20 parameters per method** (use DTO if exceeding)

### Documentation
```java
/**
 * Calculates the total expenses for a category in the current month.
 *
 * @param category the category to filter
 * @return the total in BigDecimal
 * @throws CategoryNullException if category is null
 */
public BigDecimal getTotalByCategory(Category category) {
    // ...
}
```

---

## Development Workflow

1. **Branch**: `feature/short-name` or `fix/description`
2. **Code**: Comply with SOLID, pass linting, coverage ≥ 80%
3. **Test**: Unit + Integration tests
4. **Commit**: Clear message with prefix
5. **Pull Request**: Description, issue link, pass CI/CD

---

## Key Resources

- **Specification**: `SPEC.md` - Functional requirements
- **Architecture**: `ARCHITECTURE.md` - Stack and technical decisions
- **Agent Guidelines**: `AGENTS.md` - AI agent reference
- **Spring Boot Documentation**: https://spring.io/projects/spring-boot
- **Tailwind CSS**: https://tailwindcss.com/docs
