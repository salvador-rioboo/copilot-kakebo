# Instrucciones para GitHub Copilot - Kakebo Finance

## Visión General del Proyecto

**Kakebo** es una aplicación de gestión de gastos personales basada en el método japonés homónimo.
- **Stack**: Java 25 + Spring Boot + Thymeleaf + Tailwind CSS + H2 (BD embebida)
- **Arquitectura**: REST API + Frontend MVC
- **Objetivo**: Proporcionar gestión de ingresos, gastos fijos/variables y ahorro con visualización clara

---

## Principios SOLID

### 1. Single Responsibility Principle (SRP)
- Cada clase tiene UNA responsabilidad única y bien definida
- **Separación de capas**:
  - `Controller`: Manejo de HTTP y validación de entrada
  - `Service`: Lógica de negocio pura
  - `Repository`: Acceso a datos (Spring Data JPA)
  - `Entity`: Mapeo a base de datos
  - `DTO`: Transferencia de datos entre capas

**Ejemplo**: `GastoService` solo maneja lógica de gastos, no persistencia ni HTTP.

### 2. Open/Closed Principle (OCP)
- Clases abiertas para extensión, cerradas para modificación
- **Usar interfaces y abstracciones** para componentes que pueden variar:
  - `CalculadoraAhorroStrategy` para diferentes estrategias de cálculo
  - `NotificadorAlerta` para diferentes canales de notificación
- Nuevas categorías de gastos = configuración, no código

### 3. Liskov Substitution Principle (LSP)
- Subclases pueden reemplazar a sus clases padre sin romper funcionalidad
- Si usas `List<Gasto>` con gastos fijos y variables: ambos deben cumplir el contrato

### 4. Interface Segregation Principle (ISP)
- Interfaces específicas, no monolíticas
- ❌ `IGastoManager` que todo lo hace
- ✅ `ICreadorGasto`, `IBuscadorGasto`, `IActualizadorGasto` separadas

### 5. Dependency Injection Principle (DIP)
- Inyección mediante **Spring `@Autowired`, `@Service`, `@Repository`**
- Nunca instanciar servicios directamente: `new GastoService()` ❌
- Testabilidad: usar `@MockBean` en tests

---

## Buenas Prácticas de Código

### Backend (Java + Spring Boot)

#### 1. Nomenclatura
- **Clases entidad**: `Gasto`, `Ingreso`, `Usuario` (sustantivos, singular)
- **Servicios**: `GastoService`, `IngresoService` (sufijo `Service`)
- **Repositorios**: `GastoRepository` (sufijo `Repository`)
- **Controladores**: `GastoController` (sufijo `Controller`)
- **Variables booleanas**: `esFijo`, `esRecurrente`, `tieneAlerta` (prefijo `es`, `tiene`)
- **Métodos privados**: `calcularTotalMensual()` (verbo + sustantivo)

#### 2. Estructura de Clases
```java
@Entity
@Table(name = "gastos")
public class Gasto {
    // 1. Atributos privados
    @Id
    private Long id;
    private BigDecimal cantidad;

    // 2. Constructor con parámetros obligatorios
    public Gasto(String descripcion, BigDecimal cantidad, Categoria categoria) { }

    // 3. Getters/Setters
    public BigDecimal getCantidad() { }

    // 4. Métodos de lógica de negocio
    public boolean esRecurrente() { }
}
```

#### 3. Servicios
```java
@Service
public class GastoService {
    private final GastoRepository gastoRepository;
    private final ValidadorGasto validador;

    // Constructor para inyección
    public GastoService(GastoRepository repo, ValidadorGasto validador) {
        this.gastoRepository = repo;
        this.validador = validador;
    }

    public Gasto crear(CrearGastoDTO dto) {
        // 1. Validar
        validador.validar(dto);
        // 2. Convertir DTO → Entidad
        Gasto gasto = dto.aGasto();
        // 3. Ejecutar lógica
        gasto.asignarCategoriaAutomatica();
        // 4. Persistir
        return gastoRepository.save(gasto);
    }
}
```

#### 4. Controladores
```java
@RestController
@RequestMapping("/api/gastos")
public class GastoController {
    private final GastoService gastoService;

    @PostMapping
    public ResponseEntity<GastoResponseDTO> crear(@Valid @RequestBody CrearGastoDTO dto) {
        Gasto gasto = gastoService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(GastoResponseDTO.de(gasto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GastoResponseDTO> obtener(@PathVariable Long id) {
        return gastoService.obtenerPorId(id)
            .map(g -> ResponseEntity.ok(GastoResponseDTO.de(g)))
            .orElse(ResponseEntity.notFound().build());
    }
}
```

#### 5. DTOs (Data Transfer Objects)
- Usar para **entrada**: `@Valid`, validaciones JSR-380
- Usar para **salida**: Conversión desde entidades
```java
public record CrearGastoDTO(
    @NotBlank(message = "Descripción requerida")
    String descripcion,

    @Positive(message = "Cantidad debe ser positiva")
    BigDecimal cantidad,

    @NotNull
    Categoria categoria
) { }

public record GastoResponseDTO(
    Long id,
    String descripcion,
    BigDecimal cantidad
) {
    public static GastoResponseDTO de(Gasto gasto) {
        return new GastoResponseDTO(gasto.getId(), gasto.getDescripcion(), gasto.getCantidad());
    }
}
```

#### 6. Validaciones
- Validación de entrada en DTOs con `@NotNull`, `@Positive`, etc.
- Excepciones personalizadas:
```java
public class GastoInvalidoException extends RuntimeException {
    public GastoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
```

#### 7. Transacciones
```java
@Transactional  // Para operaciones que modifican múltiples entidades
public void transferirGasto(Long idOrigen, Long idDestino) {
    Gasto origen = gastoRepository.findById(idOrigen).orElseThrow();
    Gasto destino = gastoRepository.findById(idDestino).orElseThrow();
    // operaciones...
}
```

### Frontend (Thymeleaf + Tailwind)

#### 1. Nomenclatura
- **Plantillas**: `gasto-list.html`, `ingreso-detail.html` (kebab-case)
- **Variables Thymeleaf**: `th:text="${gasto.descripcion}"` (camelCase)
- **Clases CSS**: `bg-blue-600`, `text-danger`, `btn-primary` (Tailwind)

#### 2. Estructura
```html
<!-- gasto-list.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="es">
<head>
    <title>Gastos - Kakebo</title>
    <link rel="stylesheet" th:href="@{/css/tailwind.min.css}">
</head>
<body class="bg-gray-50">
    <div class="container mx-auto p-4">
        <!-- Header mínimalista -->
        <h1 class="text-3xl font-bold mb-6">Mis Gastos</h1>

        <!-- Iteración segura con Thymeleaf -->
        <div th:if="${gastos.isEmpty()}" class="text-gray-500">
            No hay gastos registrados.
        </div>
        <ul class="space-y-2">
            <li th:each="gasto : ${gastos}" class="p-4 border-l-4 border-blue-600">
                <span th:text="${gasto.descripcion}"></span>
                <span class="text-gray-500" th:text="'$' + ${gasto.cantidad}"></span>
            </li>
        </ul>
    </div>
</body>
</html>
```

#### 3. Formularios
```html
<form th:action="@{/gastos}" th:object="${gastoForm}" method="POST" class="space-y-4">
    <div>
        <label for="descripcion" class="block font-semibold">Descripción</label>
        <input type="text" id="descripcion" th:field="*{descripcion}"
               class="w-full p-2 border rounded-lg"
               th:classappend="${#fields.hasErrors('descripcion') ? 'border-red-500' : ''}">
        <span th:if="${#fields.hasErrors('descripcion')}"
              th:errors="*{descripcion}" class="text-red-500 text-sm"></span>
    </div>
</form>
```

#### 4. Estilos Minimalistas (Tailwind)
- Colores limitados: azul (`blue-600`), gris (`gray-50`), rojo para errores
- Padding/Margin consistente: `p-2`, `p-4`, `mb-4`
- Breakpoints responsive: `md:`, `lg:` para mobile-first

---

## Testing

### Unit Tests (JUnit 5 + Mockito)
```java
@ExtendWith(MockitoExtension.class)
class GastoServiceTest {
    @Mock
    private GastoRepository gastoRepository;

    @InjectMocks
    private GastoService gastoService;

    @Test
    void crearGastoValido() {
        // Arrange
        CrearGastoDTO dto = new CrearGastoDTO("Comida", new BigDecimal("25.50"), Categoria.SUPERVIVENCIA);
        Gasto gastoEsperado = dto.aGasto();
        when(gastoRepository.save(any())).thenReturn(gastoEsperado);

        // Act
        Gasto resultado = gastoService.crear(dto);

        // Assert
        assertEquals("Comida", resultado.getDescripcion());
        verify(gastoRepository, times(1)).save(any());
    }

    @Test
    void crearGastoConCantidadNegativaLanzaExcepcion() {
        CrearGastoDTO dto = new CrearGastoDTO("Comida", new BigDecimal("-10"), Categoria.SUPERVIVENCIA);

        assertThrows(GastoInvalidoException.class, () -> gastoService.crear(dto));
    }
}
```

### Integration Tests
```java
@SpringBootTest
@AutoConfigureMockMvc
class GastoControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void obtenerGastosDevuelve200() throws Exception {
        mockMvc.perform(get("/api/gastos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }
}
```

---

## Estándares de Código

### Commits
```
[FEAT] Agregar validación de cantidad negativa
[FIX] Corregir cálculo de ahorro mensual
[REFACTOR] Extraer lógica de validación a servicio
[TEST] Aumentar cobertura de GastoService a 95%
[DOCS] Documentar endpoint POST /gastos
```

### Archivos
- **Una clase por archivo** (excepto Records pequeños)
- **Máximo 200 líneas por clase** (refactorizar si supera)
- **Máximo 20 parámetros por método** (usar DTO si supera)

### Documentación
```java
/**
 * Calcula el total de gastos para una categoría en el mes actual.
 *
 * @param categoria la categoría a filtrar
 * @return el total en BigDecimal
 * @throws CategoriaNulaException si la categoría es null
 */
public BigDecimal obtenerTotalPorCategoria(Categoria categoria) {
    // ...
}
```

---

## Flujo de Desarrollo

1. **Branch**: `feature/nombre-corto` o `fix/descripcion`
2. **Código**: Cumplir SOLID, pasar linting, cobertura ≥ 80%
3. **Test**: Unit + Integration tests
4. **Commit**: Mensaje claro con prefijo
5. **Pull Request**: Descripción, enlace a issue, pasar CI/CD

---

## Recursos Clave

- **Especificación**: `SPEC.md` - Requisitos funcionales
- **Arquitectura**: `ARCHITECTURE.md` - Stack y decisiones técnicas
- **Documentación Spring**: https://spring.io/projects/spring-boot
- **Tailwind CSS**: https://tailwindcss.com/docs
