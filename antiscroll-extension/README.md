# Pausa digital - Chrome Extension

Esta extensión es parte del proyecto Huly TPI y está diseñada para fomentar el bienestar digital mediante pausas activas.

## Características

- **Seguimiento de actividad:** Monitorea el tiempo activo de navegación y scrolls por dominio.
- **Pausa Inteligente:** Muestra un modal después de un intervalo configurable de actividad.
- **Privacidad:** Solo se registran dominios, no URLs completas.
- **Sincronización:** Obtiene configuraciones y envía métricas al backend de Huly.

## Requisitos

- Node.js (v18 o superior)
- npm o yarn

## Instalación y Desarrollo

1. Entra en el directorio de la extensión:
   ```bash
   cd antiscroll-extension
   ```

2. Instala las dependencias:
   ```bash
   npm install
   ```

3. Ejecuta el modo desarrollo:
   ```bash
   npm run dev
   ```

4. Para compilar la versión de producción:
   ```bash
   npm run build
   ```

## Cómo cargar en Chrome

1. Abre Chrome y ve a `chrome://extensions/`.
2. Activa el **Modo de desarrollador** (esquina superior derecha).
3. Haz clic en **Cargar descomprimida** (Load unpacked).
4. Selecciona la carpeta `antiscroll-extension/dist` (después de ejecutar `build`) o la carpeta raíz `antiscroll-extension` si estás usando el modo `dev`.

## Estructura del Proyecto

- `src/background`: Maneja el temporizador central, alarmas y comunicación con la API.
- `src/content`: Inyecta el modal en las páginas visitadas usando Shadow DOM.
- `src/popup`: Interfaz de usuario para configurar la extensión.
- `src/shared`: Tipos, constantes, utilidades de almacenamiento y cliente API.

## API Integration

La extensión espera los siguientes endpoints en el backend:

- `GET /api/extension/settings`: Retorna la configuración de la extensión.
- `POST /api/extension/metrics`: Recibe un array de métricas por dominio.
