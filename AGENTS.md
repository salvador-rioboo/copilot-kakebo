# AGENTS.md - Kakebo Agent Guidelines

Quick reference for AI agents working on Kakebo. **Read `.github/copilot-instructions.md` for comprehensive guidelines.**

## Critical Architecture Pattern

**Three-layer separation is non-negotiable**:
```
Controller (HTTP) → Service (Business Logic) → Repository (DB)
                         ↓
                       DTOs (validation)
```

- Never mix concerns: Controllers handle HTTP only, Services own business logic, Repositories access DB
- Services are stateless and testable in isolation
- Use `@Transactional` on Service methods that modify multiple entities

## DTO Mandatory Pattern

**All input/output must use DTOs**:
```java
// Input: DTO with @Valid JSR-380 annotations
@PostMapping
public ResponseEntity<GastoResponseDTO> crear(@Valid @RequestBody CrearGastoDTO dto)

// Output: Static factory method for conversion
GastoResponseDTO.de(gasto)  // Never expose Entity directly
```

Violations: Accepting/returning raw Entities in REST endpoints.

## Dependency Injection Rule

- **Always** use constructor injection in `@Service`, `@Repository`, `@Controller`
- **Never** instantiate: `new GastoService()` ❌
- Use Spring-managed beans exclusively for testability

## Naming Conventions (Non-Negotiable)

| Component | Pattern | Example |
|-----------|---------|---------|
| Entity | Noun, singular | `Gasto`, `Ingreso` |
| Service | `*Service` | `GastoService` |
| Repository | `*Repository` | `GastoRepository` |
| Controller | `*Controller` | `GastoController` |
| DTO (input) | `Crear*DTO` | `CrearGastoDTO` |
| DTO (output) | `*ResponseDTO` | `GastoResponseDTO` |
| Boolean variable | `es*/tiene*` | `esFijo`, `tieneAlerta` |

## Class Size Constraints

- **Max 200 lines per class** (refactor if exceeding)
- **Max 20 parameters per method** (use DTO if more needed)
- One class per file (except small Records)

## Frontend Integration (Thymeleaf + Tailwind)

- Always use `xmlns:th="http://www.thymeleaf.org"` in template root
- Navigation flows defined in `SPEC.md` cases of use (home → variables → register → back)
- Tailwind utilities only: `bg-blue-600`, `text-gray-500` (no custom CSS)
- Minimal aesthetic: limited color palette, consistent spacing (`p-2`, `p-4`, `mb-4`)

## Testing Strategy

**Unit tests** (Mockito): Service classes in isolation
```java
@ExtendWith(MockitoExtension.class)
class GastoServiceTest {
    @Mock GastoRepository repo;
    @InjectMocks GastoService service;
}
```

**Integration tests** (@SpringBootTest): Full Spring context, Controller→Repository
- Minimum coverage target: **≥80%**

## Build & Development Commands

```bash
# Build with Maven
mvn clean package

# Run tests
mvn test

# Integration tests
mvn verify
```

Java 25 LTS compatibility required; Spring Boot must support Java 25.

## Exception Handling

- Create project-specific exception classes (e.g., `GastoInvalidoException`)
- Use `@ExceptionHandler` in Controller for REST error responses
- Never expose stack traces in production

## SOLID Reference

Detailed explanations in `.github/copilot-instructions.md`. Quick summary:
- **SRP**: Layer separation + single responsibility per class
- **OCP**: Use interfaces/strategies for extension (e.g., `CalculadoraAhorroStrategy`)
- **LSP**: Subclasses respect parent contracts
- **ISP**: Segregated interfaces, not god interfaces
- **DIP**: Depend on abstractions, inject dependencies

## Key Files

- `SPEC.md` - Functional requirements & navigation flows
- `ARCHITECTURE.md` - Tech stack rationale & decisions
- `.github/copilot-instructions.md` - Comprehensive coding standards
- `pom.xml` - Maven configuration (verify Java 25 compatibility)

## Common Violations to Avoid

❌ Accepting raw `Gasto` entity in `@PostMapping` endpoint
❌ Business logic in Controller
❌ Hardcoded category lists (use `Categoria` enum)
❌ Direct service instantiation
❌ Classes exceeding 200 lines without refactoring
❌ Missing `@Transactional` on multi-entity operations
❌ Thymeleaf without namespace declaration
❌ Custom CSS instead of Tailwind utilities

---

**Last Updated**: 2026-04-29 | **For comprehensive guidelines, see `.github/copilot-instructions.md`**
