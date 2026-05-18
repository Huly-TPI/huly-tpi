# Guía de Contribución

## Ramas

```
tipo/SCRUM-XXX-descripcion-corta
```

| Tipo | Cuándo |
|------|--------|
| `feature` | Nueva funcionalidad |
| `fix` | Bug |
| `hotfix` | Urgencia en producción |
| `chore` | Config, dependencias |

```bash
✅ feature/SCRUM-88-respiracion-guiada
❌ Feature/SCRUM-88-Respiración-Guiada   # mayúsculas y tildes
❌ feature-SCRUM-88-respiracion          # guion en vez de /
```

---

## Commits

```
SCRUM-XXX: Descripcion del cambio
```

```bash
✅ SCRUM-88: agregar animacion de temporizador
✅ SCRUM-12: Corregir null pointer en validacion de token
✅ SCRUM-33: Setup CI github actions
```

---

## Código

**Java**
- Clases `PascalCase`, métodos y variables `camelCase`, constantes `UPPER_SNAKE_CASE`
- Lógica de negocio solo en `service/`, nunca en `controller/`
- Métodos de máximo 30 líneas

**React**
- Componentes `PascalCase`, hooks `useCamelCase`, todo lo demás `camelCase`
- Llamadas a la API solo en `services/`, nunca directo en componentes
- Un componente por archivo

---

## Flujo Git

```bash
git checkout dev && git pull
git checkout -b feature/SCRUM-XXX-nombre
# ... commits ...
git rebase origin/dev
git push origin feature/SCRUM-XXX-nombre
# Abrir PR → develop
```

**Ramas base:** `main` (producción) ← `stg` (staging) ← `dev` (integración) ← tus ramas

---

## Pull Requests

- Título: `[SCRUM-88] Descripcion del cambio`
- Mínimo 1 aprobación, el autor no se aprueba a sí mismo
- Resolver todos los comentarios antes de mergear
- Usar **Squash and merge**
