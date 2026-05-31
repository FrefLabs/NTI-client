<p align="center">
<img src="https://raw.githubusercontent.com/FrefLabs/NTI-client/master/src/img/IconoAplicativo.png" width="380" alt="N.T.I. Logo">
</p>

<h2 align="center">NTI-client</h2>
<p align="center">Interfaz gráfica de escritorio - NeuroFref Trading Intelligence</p>

<p align="center">
<img src="https://img.shields.io/badge/Java-8-orange?style=flat-square&logo=java">
<img src="https://img.shields.io/badge/GUI-Swing-1565c0?style=flat-square">
<img src="https://img.shields.io/badge/Estado-Finalizado-2e7d32?style=flat-square">
</p>

---

## El sistema

N.T.I. (NeuroFref Trading Intelligence) es una plataforma que entrena redes neuronales para predecir el precio de cierre de acciones del mercado bursátil. El sistema completo tiene cinco componentes:

- **NTI-client** (este repo) - interfaz de escritorio para el usuario
- **NTI-server** - servidor REST central que coordina todo
- **NTI-gateway** - gateway Python que ejecuta los scripts de datos financieros
- **NTI-train** - proceso de entrenamiento distribuido que corre en dispositivos disponibles
- **NTI-node** - nodos de cómputo que se conectan al gateway vía WebSocket para ejecutar scripts de datos

El cliente se comunica únicamente con NTI-server vía HTTP.

> [!IMPORTANT]
> Para una explicación completa de la arquitectura distribuida, el flujo de entrenamiento y la red neuronal, ver el [README de la organización](https://github.com/FrefLabs).

> [!CAUTION]
> Los modelos son una herramienta de análisis de referencia. No constituyen asesoramiento financiero.

---

## NTI-client

Es la interfaz con la que interactúa el usuario (llamado "Accionista" en la documentación del proyecto). Permite consultar datos de mercado en tiempo real, explorar modelos entrenados, ver predicciones históricas, crear nuevos modelos y participar en un modo de juego educativo.

Toda la UI está construida con Java Swing sobre un layout de `CardLayout` con sidebar de navegación fijo. Todo el acceso a datos pasa exclusivamente por la API REST de NTI-server; no hay conexiones directas a base de datos ni dependencia de APIs de terceros desde el cliente.

---

## Pantallas

### Inicio

![Inicio](screenshots/Pantalla%20de%20Inicio.png)

Dashboard principal. Gráfico de línea/customizable (JFreeChart) con selector de estilo de gráfica (Línea / Línea y Puntos / Solo Puntos) y datos de los últimos 7 días. Panel de datos de la empresa seleccionada. Precio actual del ticker con actualización automática, recomendación diaria (COMPRAR / MANTENER / VENDER), precio de cierre predicho por el modelo activo, y feed de noticias recientes. El tipo de gráfica es configurable desde Ajustes.

### Modelos

![Modelos](screenshots/Pantalla%20de%20Modelos.png)

Lista de modelos disponibles con buscador en tiempo real. Muestra el modelo actualmente seleccionado con su precisión, error promedio y rango de entrenamiento. El botón "+ Crear Modelo" abre el formulario de creación con campos para acción, fechas, arquitectura, funciones de activación, learning rate, max error y max iteraciones, con panel de features personalizable.

### Detalle de modelo

![Detalle](screenshots/Pantalla%20de%20Modelo%20Seleccionado%20(Modelos).png)

Al hacer clic en un modelo se accede a la vista detalle: features usadas en el entrenamiento, métricas sobre el período de entrenamiento (MSE, RMSE, MAE, R², max/min error, percentil 90, precisión), métricas post-entrenamiento (error promedio, precisión y precisión de tendencia), e hiperparámetros usados (arquitectura, funciones, learning rate, max error, épocas). Navegación con botón "Volver" a la lista.

### Historial

![Historial](screenshots/Pantalla%20de%20Historial.png)

Predicciones día a día del modelo activo: fecha, apertura, máximo, mínimo, cierre real, cierre predicho y diferencia en dólares. Vista con lista scrolleable y botón "Ver Detalles".

### Ajustes

![Ajustes](screenshots/Pantalla%20de%20Ajustes.png)

Configuración de moneda (lista dinámica obtenida del servidor), red de refinamiento, efectos de sonido (SFX), y volumen de música con slider de 0 a 100%.

### Juego

Modo de juego educativo "Tira y Afloje" donde el jugador predice si el precio de una acción subirá o bajará, usando datos reales del mercado. Incluye sistema de puntos, rondas, rankings y efectos de sonido. El volumen de la música de fondo se controla desde Ajustes.

---

## Estructura del proyecto

```
NTI-client/
├── src/
│   ├── NTI/
│   │   ├── NTI.java                  # Clase principal, ventana, CardLayout + sidebar
│   │   ├── PanelInicio.java          # Dashboard con gráficos y datos de mercado
│   │   ├── PanelModelos.java         # Gestión, búsqueda y creación de modelos
│   │   ├── PanelHistorial.java       # Historial de predicciones
│   │   ├── PanelAjustes.java         # Configuración del usuario
│   │   ├── AudioManager.java         # Sistema de audio (singleton, SFX y música)
│   │   ├── Lectura.java             # Cliente HTTP contra NTI-server API
│   │   ├── Registro.java            # Escritura de datos vía API REST
│   │   ├── Api.java                 # Configuración de API y obtención de clave
│   │   ├── Formato.java             # Formateo de respuestas y datos
│   │   ├── Modelo.java              # Lógica de modelos y predicciones
│   │   ├── Entorno.java             # Configuración del entorno (carga/guarda JSON)
│   │   ├── Accion.java              # Datos de la acción seleccionada
│   │   ├── Empresa.java             # Datos de empresa
│   │   ├── Noticia.java             # Noticias financieras
│   │   ├── Fuentes.java             # Carga de tipografía Inter
│   │   ├── Tupla.java               # Estructura de datos auxiliar
│   │   ├── RoundedBorder.java       # Borde redondeado personalizado
│   │   └── TransparentRoundedBorder.java
│   ├── juego/
│   │   ├── Juego.java               # Lógica del juego (rondas, puntos, rankings)
│   │   └── PanelJuego.java          # UI del modo de juego
│   ├── fonts/                        # Familia tipográfica Inter (variable)
│   ├── audios/                       # Efectos de sonido y música (WAV)
│   └── img/                          # Íconos e imágenes de la interfaz
├── screenshots/                      # Capturas de pantalla para el README
├── lib/
│   ├── jfreechart-1.0.19.jar        # Gráficos financieros
│   ├── jcommon-1.0.23.jar
│   └── json-20250517.jar            # Parseo de respuestas JSON
├── config.json                       # Preferencias del usuario
├── api_config.json                   # Claves de APIs externas
└── build.xml                         # Build con Apache Ant
```

---

## Configuración

**`config.json`** — preferencias del usuario, se sobreescribe desde la pantalla de Ajustes:
```json
{
  "red_refinamiento": true,
  "volumen": 30,
  "moneda": "USD",
  "efectos_sonido": true,
  "modelo": 29,
  "estilo_grafica": "lineal"
}
```

---

## Compilar y ejecutar

**Requisitos:** JDK 8, Apache Ant. NTI-server debe estar corriendo antes de iniciar el cliente.

```bash
ant compile
ant run
```

O abrir el proyecto en NetBeans y ejecutar con F6.

---

## Dependencias

| Librería | Versión | Uso |
|---|---|---|
| JFreeChart | 1.0.19 | Gráficos financieros (línea, puntos) |
| org.json | 20250517 | Parseo de JSON del servidor |

---

## Equipo - Fref Labs

Desarrollado por Federico Battistello, Luca Guarna, Nicolás Pereira, Franco Perfetti y Juan Sirota
