# Léxico

Cuaderno de lectura y retención de vocabulario en inglés.

Diseñado para lectores de libros, prensa y guiones que buscan capturar términos desconocidos al instante y consolidarlos en la memoria a largo plazo mediante repetición espaciada, sin distracciones ni mecánicas de juego infantil.

---

## Enfoque

La mayoría de aplicaciones de idiomas recurren a la gamificación excesiva, animaciones innecesarias y ejercicios triviales. Léxico está pensado como una libreta sobria de estudio personal:

- **Estética editorial:** Tipografía serif cuidada (Newsreader) para títulos literarios y sans-serif (Inter) para lectura cómoda, con modos en papel cálido y tinta oscura.
- **Retención espaciada:** Las palabras no se repiten al azar; el sistema calcula cuándo toca repasar cada término según lo fácil o difícil que te resulte recordarlo. Si fallas una palabra, vuelve a programarse de inmediato para el día siguiente.
- **Captura sin fricción:** Añade cualquier término mientras lees en pocos segundos usando el portapapeles y el autocompletado de definiciones y frases de ejemplo.

---

## Qué puedes hacer con la app

### Captura rápida mientras lees
- **Pegado directo:** Botón rápido para volcar la última palabra copiada en el portapapeles del teléfono.
- **Autocompletado en un toque:** Rellena automáticamente la traducción en español, pronunciación fonética y un ejemplo literario sin tener que escribirlo todo a mano.

### Modos de práctica
- **Producción activa:** Escribe la palabra en inglés a partir de su significado en español, con opción de pedir la letra inicial como pista si te bloqueas.
- **Contexto:** Deduce la palabra que falta completando el hueco en frases de obras literarias reales (*Dune*, *1984*, *The Great Gatsby*, etc.).
- **Sprint:** Empareja contrarreloj términos y significados para agilizar el reconocimiento visual.

### Biblioteca y organización
- Búsqueda en tiempo real por palabra en inglés o por su traducción.
- Filtros por fuente de lectura (libros, artículos, series) y por estado de aprendizaje (pendientes para hoy, en progreso o dominadas).
- Consulta del estado de cada término, su nivel de retención y la fecha del próximo repaso.

### Recordatorios diarios
- Notificación diaria a la hora que elijas. Solo te avisa si tienes repasos pendientes y aún no has abierto la app durante el día.

---

## Instalación

### Instalar el archivo en un teléfono Android
1. Copia el archivo instalador `app/build/outputs/apk/debug/app-debug.apk` a tu teléfono (mediante cable, Google Drive, Telegram, etc.).
2. Ábrelo en el móvil para instalarlo. Si Android te lo pide, permite la instalación desde esa fuente.

### Compilar el proyecto
Requiere Android Studio y JDK 17 o superior.

```bash
./gradlew assembleDebug
```

El archivo generado estará listo en `app/build/outputs/apk/debug/app-debug.apk`.
