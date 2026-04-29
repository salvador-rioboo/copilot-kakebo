# Kakebo Finance - Implementation Plan & Progress

**Last Updated**: 2026-04-29
**Current Status**: Phase 3 Complete - Proceeding to Phase 4
**Overall Test Suite**: 46/46 tests passing ✅
**Build Status**: SUCCESS ✅

---

## Project Overview

**Kakebo** is a personal expense management application using the Japanese Kakebo method.

- **Stack**: Java 25 + Spring Boot 3.2.4 + Thymeleaf + Tailwind CSS + H2 (embedded)
- **Architecture**: REST API + Frontend MVC (3-layer: Controller → Service → Repository)
- **Target**: Income management, fixed/variable expenses, savings visualization with clear budgeting

---

## Completed Phases ✅

### Phase 1: Controller Integration Tests (COMPLETED)
**Goal**: Fix broken controller integration tests after Java/Mockito incompatibility issues

**Implementation**:
- Migrated from unit tests with mocks → integration tests with real H2 database
- Strategy: `@SpringBootTest` + `@AutoConfigureMockMvc` + real `@Autowired` repositories
- Result: 20 controller tests passing (Expense 7, Income 8, Dashboard 5)

**Key Decisions**:
- Removed Mockito @MockBean approach (incompatible with Java 25)
- Uses real in-memory H2 database for all tests
- Each test class calls `deleteAll()` in `@BeforeEach` for isolation

**Files Modified**:
- `src/test/java/com/kakebo/controller/ExpenseControllerIntegrationTest.java`
- `src/test/java/com/kakebo/controller/IncomeControllerIntegrationTest.java`
- `src/test/java/com/kakebo/controller/DashboardControllerIntegrationTest.java`

---

### Phase 2: GlobalExceptionHandler (COMPLETED)
**Goal**: Implement centralized exception handling for REST API

**Implementation**:
- Created `@ControllerAdvice` handler in `src/main/java/com/kakebo/exception/GlobalExceptionHandler.java`
- Catches domain exceptions and converts to HTTP responses:
  - `ResourceNotFoundException` → 404 Not Found
  - `InvalidExpenseException` → 400 Bad Request
  - `InvalidIncomeException` → 400 Bad Request
  - `MethodArgumentNotValidException` → 400 Bad Request (with field errors)
  - Generic `Exception` → 500 Internal Server Error

**Result**: All controller tests now pass because exceptions are properly handled

**Files Created**:
- `src/main/java/com/kakebo/exception/GlobalExceptionHandler.java`

---

### Phase 3: Thymeleaf Template Validation (COMPLETED)
**Goal**: Add client-side JavaScript validation to expense and income entry forms

**Implementation**:

#### expenses.html Enhancements
- ✅ Form ID: `expenseForm` for JS targeting
- ✅ Real-time validation via blur/change events:
  - **Description**: 3-100 characters, required
  - **Amount**: positive number (> 0), decimal support
  - **Category**: required selection
  - **Date**: not in future, not older than 1 year
- ✅ Error messages displayed inline under each field
- ✅ Border color changes to red on error
- ✅ Submit button disabled until all validations pass
- ✅ Date field defaults to today with max constraint

#### incomes.html Enhancements
- Same validation pattern as expenses.html
- Fields: Description, Amount, Type, Date
- Form ID: `incomeForm`

**JavaScript Features**:
- Validation object with rules for each field
- `showError()` function to display/clear error messages
- `validateForm()` function to check all fields
- Real-time validation on blur/change events
- Form submit prevention if validation fails
- Auto-fill date field with today's date (max: today)

**UX Improvements**:
- Cursor pointer on checkboxes for better accessibility
- Smooth transitions on input focus
- Descriptive placeholders
- Clear required field indicators (red asterisks)

**Files Modified**:
- `src/main/resources/templates/expenses.html`
- `src/main/resources/templates/incomes.html`

---

## Current Test Suite Status

### Service Tests (26 tests) ✅
| Test Class | Count | Status |
|-----------|-------|--------|
| `ExpenseServiceTest` | 7 | ✅ PASS |
| `IncomeServiceTest` | 14 | ✅ PASS |
| `DashboardServiceTest` | 5 | ✅ PASS |

### Controller Tests (20 tests) ✅
| Test Class | Count | Status |
|-----------|-------|--------|
| `ExpenseControllerIntegrationTest` | 7 | ✅ PASS |
| `IncomeControllerIntegrationTest` | 8 | ✅ PASS |
| `DashboardControllerIntegrationTest` | 5 | ✅ PASS |

**Total**: 46/46 tests passing
**Build Time**: ~9-14 seconds
**Architecture**: Integration tests with real H2 database (no mocks)

---

## In-Progress: Phase 4 (NOT YET STARTED)

### Phase 4: Repository Tests & End-to-End Tests

**Goal**: Add `@DataJpaTest` tests for repositories and comprehensive end-to-end tests

#### 4a. Repository Layer Tests
**Purpose**: Test JPA query methods and data persistence logic in isolation

**Planned Tests**:

1. **ExpenseRepositoryTest** (target: 6-8 tests)
   - `testFindByMonthAndYearReturnsExpenses()`
   - `testFindByMonthYearAndCategoryReturnsFiltered()`
   - `testSumByMonthAndYearReturnsTotal()`
   - `testFindAllByMonthAndYearOrderByDateDesc()`
   - `testDeleteByIdRemovesExpense()`

2. **IncomeRepositoryTest** (target: 5-7 tests)
   - `testFindByMonthAndYearReturnsIncomes()`
   - `testFindByMonthYearAndTypeReturnsFiltered()`
   - `testSumByMonthAndTypeReturnsTotal()`
   - `testFindAllByMonthAndYearOrderByDateDesc()`

**Implementation Strategy**:
- Use `@DataJpaTest` annotation (loads only JPA layer)
- H2 in-memory database (same as service tests)
- Autowire repository directly
- Test custom `@Query` methods with YEAR/MONTH functions

**Files to Create**:
- `src/test/java/com/kakebo/repository/ExpenseRepositoryTest.java`
- `src/test/java/com/kakebo/repository/IncomeRepositoryTest.java`

#### 4b. End-to-End Tests
**Purpose**: Simulate complete user flows (create → query → update → delete)

**Planned E2E Tests** (target: 3-5 scenarios):

1. **ExpenseE2ETest**
   - Create expense → Retrieve by ID → Update → Delete → Verify deletion
   - Create multiple → Filter by month/category → Validate sums

2. **IncomeE2ETest**
   - Create income → Filter by type → Update amount → Delete
   - Create multiple → Calculate monthly total → Verify persistence

3. **DashboardE2ETest**
   - Create income + expenses → Get monthly summary
   - Verify budget calculations (availableMoney, plannedSavings)
   - Test alert logic when expenses exceed threshold

**Implementation Strategy**:
- Use full `@SpringBootTest` context
- Each test = complete flow (setup → action → verify)
- Database cleanup in `@AfterEach`
- No API calls (direct service/repository usage)

**Files to Create**:
- `src/test/java/com/kakebo/e2e/ExpenseE2ETest.java`
- `src/test/java/com/kakebo/e2e/IncomeE2ETest.java`
- `src/test/java/com/kakebo/e2e/DashboardE2ETest.java`

**Expected Test Count**: +10-15 new tests
**Target Result**: Total 56-61 tests, all passing

---

## In-Progress: Phase 5 (NOT YET STARTED)

### Phase 5: Coverage Validation & Final Checks

**Goal**: Ensure code coverage ≥80% and validate all endpoints

#### 5a. Coverage Analysis
**Tools**: JaCoCo Maven Plugin

**Steps**:
1. Add JaCoCo plugin to `pom.xml` (if not present)
2. Run: `mvn clean test jacoco:report`
3. Generate coverage report: `target/site/jacoco/index.html`
4. Target: ≥80% overall coverage, focus on:
   - Service layer (logic validation)
   - Repository layer (query methods)
   - Exception handling (GlobalExceptionHandler)

**Expected Gaps** (if any):
- Template rendering (Thymeleaf not covered by unit tests)
- Edge cases in error handling

#### 5b. Endpoint Validation Against SPEC.md
**Checklist**:
- ✅ GET `/` (index page)
- ✅ GET `/expenses` (expenses page)
- ✅ GET `/incomes` (incomes page)
- ✅ GET `/api/expenses` (list all)
- ✅ GET `/api/expenses/{id}` (get by ID)
- ✅ POST `/api/expenses` (create)
- ✅ PUT `/api/expenses/{id}` (update)
- ✅ DELETE `/api/expenses/{id}` (delete)
- ✅ GET `/api/expenses?month=4&year=2026` (filter by month/year)
- ✅ GET `/api/incomes` (list all)
- ✅ GET `/api/incomes/{id}` (get by ID)
- ✅ POST `/api/incomes` (create)
- ✅ PUT `/api/incomes/{id}` (update)
- ✅ DELETE `/api/incomes/{id}` (delete)
- ✅ GET `/api/dashboard/summary` (monthly summary)
- ✅ GET `/api/dashboard/summary/current` (current month)
- ✅ GET `/api/dashboard/alerts` (budget alerts)

#### 5c. Build & Package Validation
**Steps**:
1. Run `mvn clean package` (includes tests)
2. Verify JAR created: `target/kakebo-finance-1.0.0.jar`
3. Test JAR execution: `java -jar target/kakebo-finance-1.0.0.jar`
4. Verify application starts on `http://localhost:8080`

**Expected Output**:
- Full test suite passes (46+ tests)
- JAR packaged successfully
- Application boots without errors

**Files to Check/Update**:
- `pom.xml` (JaCoCo plugin)
- `SPEC.md` (verify all endpoints documented)
- `README.md` (update with final status)

---

## Architecture Summary

### Three-Layer Pattern (SOLID Compliant)
```
┌─────────────────────────────────┐
│ Controller Layer (HTTP)          │
│ - ExpenseController              │
│ - IncomeController               │
│ - DashboardController            │
└──────────────┬────────────────────┘
               ↓
┌─────────────────────────────────┐
│ Service Layer (Business Logic)   │
│ - ExpenseService                 │
│ - IncomeService                  │
│ - DashboardService               │
└──────────────┬────────────────────┘
               ↓
┌─────────────────────────────────┐
│ Repository Layer (Data Access)   │
│ - ExpenseRepository              │
│ - IncomeRepository               │
│ - Spring Data JPA                │
└─────────────────────────────────┘
               ↓
        H2 In-Memory DB
```

### Technology Stack
- **Backend**: Java 25 LTS, Spring Boot 3.2.4, Spring Data JPA, Hibernate 6.4.4
- **Frontend**: Thymeleaf, Tailwind CSS, Vanilla JavaScript
- **Database**: H2 (embedded, auto-configured)
- **Testing**: JUnit 5, Spring Test, Mockito (integration tests, no mocks for Spring beans)
- **Build**: Maven 3.11.0, JaCoCo (coverage)

---

## Key Technical Decisions

### 1. **Java Version: Java 25 → Java 21 (LTS)**
- **Reason**: Mockito/ByteBuddy incompatibility with Java 25
- **Result**: Downgraded to Java 21 for library compatibility
- **Config**: `<java.version>21</java.version>` in pom.xml

### 2. **Integration Tests vs Unit Tests**
- **Decision**: Use integration tests with real H2 database (no mocks for repositories)
- **Reason**: Avoids Mockito limitations with Spring Data JPA interfaces
- **Benefit**: More reliable, tests real behavior vs mock assumptions

### 3. **GlobalExceptionHandler Pattern**
- **Decision**: Centralized exception handling via `@ControllerAdvice`
- **Benefit**: Consistent HTTP status codes, cleaner controllers

### 4. **Client-Side Validation in Templates**
- **Decision**: JavaScript validation + HTML5 constraints
- **Benefit**: Immediate user feedback, reduced server load

---

## Enum Reference

### Category (Expenses)
```java
SURVIVAL        // Essentials (rent, food, utilities)
ENTERTAINMENT   // Treats (movies, dining)
CULTURE         // Education (courses, books)
EXTRAS          // Unexpected expenses
```

### IncomeType
```java
PRINCIPAL       // Main salary/income
EXTRA           // Freelance, gifts, refunds
```

---

## Next Steps (To Continue)

1. **Phase 4a - Repository Tests**:
   - Create `ExpenseRepositoryTest.java` with @DataJpaTest
   - Create `IncomeRepositoryTest.java` with @DataJpaTest
   - Run: `mvn -Dtest='*RepositoryTest' test`
   - Target: +6-10 tests

2. **Phase 4b - End-to-End Tests**:
   - Create `ExpenseE2ETest.java`, `IncomeE2ETest.java`, `DashboardE2ETest.java`
   - Each test simulates complete user flow
   - Target: +3-5 scenarios (9-15 test methods)

3. **Phase 5 - Coverage & Validation**:
   - Run `mvn clean test jacoco:report`
   - Verify coverage ≥80%
   - Final endpoint validation
   - Package & test JAR execution

---

## File Structure Summary

```
src/
├── main/
│   ├── java/com/kakebo/
│   │   ├── KakeboApplication.java
│   │   ├── controller/
│   │   │   ├── DashboardController.java
│   │   │   ├── ExpenseController.java
│   │   │   ├── IncomeController.java
│   │   │   └── ViewController.java
│   │   ├── dto/
│   │   │   ├── CreateExpenseDTO.java
│   │   │   ├── CreateIncomeDTO.java
│   │   │   ├── DashboardSummaryDTO.java
│   │   │   ├── ExpenseResponseDTO.java
│   │   │   └── IncomeResponseDTO.java
│   │   ├── entity/
│   │   │   ├── Category.java
│   │   │   ├── Expense.java
│   │   │   ├── Income.java
│   │   │   └── IncomeType.java
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java ✅
│   │   │   ├── InvalidExpenseException.java
│   │   │   ├── InvalidIncomeException.java
│   │   │   └── ResourceNotFoundException.java
│   │   ├── repository/
│   │   │   ├── ExpenseRepository.java
│   │   │   └── IncomeRepository.java
│   │   └── service/
│   │       ├── DashboardService.java
│   │       ├── ExpenseService.java
│   │       └── IncomeService.java
│   └── resources/
│       ├── application.properties
│       └── templates/
│           ├── expenses.html ✅
│           ├── incomes.html ✅
│           └── index.html
└── test/
    └── java/com/kakebo/
        ├── service/ ✅
        │   ├── ExpenseServiceTest.java (7 tests)
        │   ├── IncomeServiceTest.java (14 tests)
        │   └── DashboardServiceTest.java (5 tests)
        ├── controller/ ✅
        │   ├── ExpenseControllerIntegrationTest.java (7 tests)
        │   ├── IncomeControllerIntegrationTest.java (8 tests)
        │   └── DashboardControllerIntegrationTest.java (5 tests)
        ├── repository/ (TO CREATE - Phase 4)
        │   ├── ExpenseRepositoryTest.java
        │   └── IncomeRepositoryTest.java
        └── e2e/ (TO CREATE - Phase 4)
            ├── ExpenseE2ETest.java
            ├── IncomeE2ETest.java
            └── DashboardE2ETest.java
```

---

## Build Commands Reference

```bash
# Compile project
mvn clean compile

# Run all tests
mvn test

# Run specific test class
mvn -Dtest='ExpenseServiceTest' test

# Run test pattern (e.g., all repository tests)
mvn -Dtest='*RepositoryTest' test

# Generate test coverage report
mvn clean test jacoco:report

# Package application
mvn clean package

# Run application
java -jar target/kakebo-finance-1.0.0.jar

# View test results
mvn surefire-report:report
```

---

## Status Summary

| Phase | Task | Status | Tests | Build |
|-------|------|--------|-------|-------|
| 1 | Controller Integration Tests | ✅ DONE | 20/20 | SUCCESS |
| 2 | GlobalExceptionHandler | ✅ DONE | 46/46 | SUCCESS |
| 3 | Template Validation | ✅ DONE | 46/46 | SUCCESS |
| 4a | Repository Tests | ⏳ NEXT | - | - |
| 4b | End-to-End Tests | ⏳ NEXT | - | - |
| 5 | Coverage & Validation | ⏳ FINAL | - | - |

**Overall Progress**: 3/5 phases complete (60%)
**Current Test Coverage**: 46 tests, all passing
**Timeline**: Target completion Phases 4-5 in next session
