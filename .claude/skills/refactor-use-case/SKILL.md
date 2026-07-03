---
name: refactor-use-case
description: Refactoriza un use case del backend (com.huly.backend.domain.useCase.*) a la estructura SOLID objetivo del proyecto, dejando execute() como una lista de pasos, moviendo reglas/validaciones de dominio al model y la duplicación transversal a servicios compartidos, y sus tests al estilo Arrange-Act-Assert con helpers privados, nombres camelCase y @DisplayName en español; sin cambiar el comportamiento y manteniendo los tests verdes. Usar cuando se pida "refactorizar este use case", "dejar SOLID", "extraer a private methods/al model", limpiar un execute() con lógica inline, o "refactorizar/limpiar los tests" (extraer when/then y validaciones a métodos privados, camelCase, DisplayName).
---

# Refactor de Use Cases (estilo SOLID del proyecto)

Lleva un use case de `com.huly.backend.domain.useCase.*` a la **forma objetivo**:
`execute()` se lee como los pasos del caso de uso; las reglas de dominio viven en el
model; la duplicación transversal vive en servicios. **El comportamiento no cambia y
los tests existentes deben seguir pasando sin modificación de sus assertions.**

## Forma objetivo de `execute()`

`execute()` solo orquesta. Cada línea es una de estas tres cosas:

1. Una llamada a un **método privado** del use case (`resolveX`, `loadX`, `validateX`,
   `applyX`, `registerX`).
2. Una llamada a un **método del model** (regla de dominio pura).
3. Una llamada a un **servicio colaborador** (lógica compartida / con I/O).

No debe quedar en `execute()`: validaciones inline con `throw`, matemática de dominio,
cadenas `repository.findX().filter(...).isPresent()`, ni armado de varios valores.

Ejemplo de referencia ya hecho —
`backend/src/main/java/com/huly/backend/domain/useCase/dailyReward/ClaimDailyRewardUseCase.java`:

```java
DailyClaimState state = resolveClaimState(userId, today);   // privado: fetch + validación
List<DailyReward> cycle = loadConfiguredCycle();            // privado: fetch + guard
ClaimedReward reward = resolveReward(state, today, cycle, userId); // privado -> record
applyClaim(userId, today, reward);                          // privado: efectos/writes
return mapper.toResponse(reward.coins(), reward.cycleDay(), reward.newStreak());
```

## Dónde va cada cosa (regla de decisión)

- **Función pura del estado de dominio, sin I/O** → al **model**.
  - Si es un `record` (estado), agregale un predicado: ej. `DailyClaimState.claimedOn(today)`.
  - Si es una utilidad de dominio sin estado (clase `final` con constructor privado y
    estáticos, ej. `DailyRewardCycle`), agregá una función estática: `coinsForDay(...)`,
    `currentStreak(...)`, `progress(...)`.
  - Si deriva **varios** valores, devolvé un **value object del model** (record propio,
    ej. `CycleProgress`).
- **Toca un repositorio/servicio, es específico de este use case y se usa una sola vez**
  → **método privado** del use case. Si arma varios valores para downstream, devolvé un
  **record privado** (ej. `ClaimedReward`).
- **La misma lógica aparece en ≥2 lugares** (compruébalo con grep antes de decidir) →
  **servicio `@Service`** en `com.huly.backend.domain.service.*`, inyectado en el use case.
  Ejemplos ya creados: `PlanService.hasActivePlan(userId, asOf)`,
  `UserActivityService.registerActivity(userId, today)`.

Para mantener los servicios agnósticos del tiempo y los tests deterministas: el use case
mantiene su `Clock` y le pasa `Instant.now(clock)` / `LocalDate.now(clock)` al servicio
(no inyectes el `Clock` en el servicio).

## Procedimiento

1. **Leer**: el use case, sus clases de model y su test (`src/test/.../<UseCase>Test.java`).
2. **Buscar duplicación** antes de extraer, para decidir model vs privado vs servicio:
   ```
   # ¿la cadena de "plan activo" / la lógica candidata está en otros lados?
   ```
   Usá Grep sobre `backend/src/main/java/com/huly/backend`.
3. **Cambiar en orden de dependencia**: model → servicios nuevos → use case → `*UseCaseConfig` → tests.
4. **Wiring**: si agregaste/quitaste dependencias del use case, actualizá el `@Bean` en
   `com.huly.backend.infrastructure.config.useCase.<Area>UseCaseConfig`. El orden de los
   argumentos del constructor debe coincidir con el orden de los campos (por
   `@RequiredArgsConstructor`). Los `@Service` se inyectan solos como parámetros del `@Bean`.
5. **Tests**:
   - Los tests del use case no deben cambiar sus assertions; solo adaptá la construcción
     (mocks). Si una dependencia pasó a servicio, mockeá el servicio
     (`when(planService.hasActivePlan(eq(USER_ID), any())).thenReturn(true)`) en lugar del repo.
   - Agregá tests unitarios para **toda** regla movida al model/servicio (mismo estilo:
     JUnit5 + AssertJ + Mockito, un caso por comportamiento).
6. **Compilar y correr** los tests afectados (ver abajo). Verificar exit 0 y los conteos
   en `target/surefire-reports/*.txt`.

## Anti-patrones (NO hacer)

- Un método por cada write o por cada línea: eso es fragmentar, no es SOLID.
- Crear interfaces para los use cases cuando hay una sola implementación (YAGNI).
- Mover I/O (repositorios) dentro del model.
- Cambiar el comportamiento o relajar assertions para que "entre" la extracción.
- Inyectar `Clock` en los servicios de dominio nuevos (pasá el instante por parámetro).

## Refactor de los tests del use case (estilo Arrange-Act-Assert)

Cada `@Test` queda en tres bloques (arrange / act / assert) de pocas líneas; toda la
mecánica (stubs, ejecución, asserts y verifies) se extrae a **métodos privados** agrupados
al final de la clase, bajo comentarios `// --- arrange ---`, `// --- act ---`, `// --- assert ---`.

- **Arrange** → métodos `givenX(...)` que encapsulan los `when(...).thenReturn(...)`. Uno por
  colaborador/escenario: `givenClaimState(streak, lastClaimDate)`, `givenConfiguredCycle()`,
  `givenCycle(cycle)`, `givenNoConfiguredCycle()`, `givenActivePlan()`.
- **Act** → un método que ejecuta el use case: `claim()` / `status()` →
  `useCase.execute(new XRequest(USER_ID))`. Permite además `assertThatThrownBy(this::claim)`.
- **Assert** → métodos `thenX(...)` que agrupan los `assertThat` y `verify`:
  `thenClaimed(result, day, coins, streak)` (respuesta + efectos), `thenNothingWasClaimed()`
  (los `verify(..., never())`), `thenStatusWas(...)`, `thenPlanBonus(...)`.

Convenciones obligatorias:

- Nombres de los `@Test` en **camelCase** sin guiones bajos:
  `executeShouldCreditDayOneWhenFirstClaim` (NO `execute_shouldCreditDayOne_whenFirstClaim`).
- `@DisplayName` en **español** describiendo el comportamiento, ej.
  `@DisplayName("Acredita el Día 1 cuando es el primer reclamo")`
  (import `org.junit.jupiter.api.DisplayName`).
- Dejar comentarios solo en los casos no obvios (wrap de ciclo, config con huecos, etc.).
- Cuidar el **strict stubbing** de Mockito: cada `givenX()` que dejes en un test tiene que
  usarlo el camino de `execute` de ese test, o salta `UnnecessaryStubbingException`
  (ej.: en el test de "ya reclamó hoy" no stubbees el ciclo, porque corta antes).

Resultado típico:

```java
@Test
@DisplayName("Avanza la racha cuando el reclamo es consecutivo")
void executeShouldAdvanceStreakWhenClaimIsConsecutive() {
    givenClaimState(3, TODAY.minusDays(1));
    givenConfiguredCycle();

    ClaimDailyRewardResponse result = claim();

    thenClaimed(result, 4, 25, 4);
}
```

Plantilla ya hecha: `ClaimDailyRewardUseCaseTest`, `GetDailyRewardStatusUseCaseTest`.

## Build & test (entorno de esta máquina)



Surefire matchea `-Dtest=NombreSimple` en todos los paquetes. Los resúmenes quedan en
`backend/target/surefire-reports/*.txt` (`Tests run: N, Failures: 0, Errors: 0`).

## Ejemplo completo de referencia

El módulo de daily reward ya está refactorizado a esta forma — úsalo como plantilla:

- Use cases: `domain/useCase/dailyReward/ClaimDailyRewardUseCase.java`, `GetDailyRewardStatusUseCase.java`
- Reglas en model: `domain/model/dailyReward/DailyClaimState.java` (`claimedOn`),
  `DailyRewardCycle.java` (`coinsForDay`, `currentStreak`, `progress`), `CycleProgress.java`
- Servicios compartidos: `domain/service/payment/PlanService.java` (`hasActivePlan`),
  `domain/service/user/UserActivityService.java` (`registerActivity`)
- Wiring: `infrastructure/config/useCase/DailyRewardUseCaseConfig.java`
- Tests: `*UseCaseTest`, `DailyRewardCycleTest`, `PlanServiceTest`, `UserActivityServiceTest`

### Duplicación pendiente de migrar (follow-up, mismo patrón)

La cadena "plan activo" sigue inline en `GetCurrentMembershipUseCase`, `BuyStoreItemUseCase`,
`CreatePaymentPreferenceUseCase`, `ChatQuotaService`, `ListBackofficeUsersUseCase`; y
`LoginUseCase` tiene su propio `registerActivity` idéntico. Destino ya disponible:
`PlanService.hasActivePlan` y `UserActivityService.registerActivity`.
