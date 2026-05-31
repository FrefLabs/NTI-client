<p align="center">
  <img src="src/img/NTI-LOGO-FINAL.svg" width="380" alt="N.T.I. Logo">
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

N.T.I. (NeuroFref Trading Intelligence) es una plataforma que entrena redes neuronales para predecir el precio de cierre de acciones del mercado bursátil. El sistema completo tiene cuatro componentes:

- **NTI-client** (este repo) - interfaz de escritorio para el usuario
- **NTI-server** - servidor REST central que coordina todo
- **NTI-gateway** - gateway Python que ejecuta los scripts de datos financieros
- **NTI-train** - proceso de entrenamiento distribuido que corre en dispositivos disponibles

El cliente se comunica únicamente con NTI-server vía HTTP.

> [!IMPORTANT]
>  Para una explicación completa de la arquitectura distribuida, el flujo de entrenamiento y la red neuronal, ver el [README de la organización](https://github.com/FrefLabs).

> [!CAUTION]
> Los modelos son una herramienta de análisis de referencia. No constituyen asesoramiento financiero.

---

## NTI-client

Es la interfaz con la que interactúa el usuario (llamado "Accionista" en la documentación del proyecto). Permite consultar datos de mercado en tiempo real, explorar modelos entrenados, ver predicciones históricas, crear nuevos modelos y participar en un modo de juego educativo.

Toda la UI está construida con Java Swing sobre un layout de `CardLayout` con sidebar fijo. No hay framework web ni dependencias de red más allá de las llamadas al servidor y las APIs externas.

---

## Pantallas

### Inicio

![Inicio](screenshots/Pantalla%20de%20Inicio.png)

Dashboard principal. Columna izquierda: gráfico de velas (JFreeChart) con selector de período (1D / 3D / 5D / 7D) y métricas de mercado fundamentales (previous close, open, bid/ask, volumen, market cap, P/E, EPS, etc.). Columna derecha: precio actual del ticker con cambio porcentual, recomendación diaria (COMPRAR / MANTENER / VENDER), precio de cierre predicho por el modelo activo, descripción de la empresa, ranking de los 3 modelos más precisos para el ticker, y feed de noticias recientes vía Finnhub.

### Modelos

![Modelos](screenshots/Pantalla%20de%20Modelos.png)

Lista de todos los modelos disponibles con buscador en tiempo real por ticker. Muestra el modelo actualmente seleccionado con su precisión, MAE y rango de entrenamiento. El botón "+ Crear Modelo" abre el formulario de creación.

### Detalle de modelo

![Detalle](screenshots/Pantalla%20de%20Modelo%20Seleccionado%20(Modelos).png)

Al hacer clic en un modelo se muestra: las features usadas en el entrenamiento, métricas sobre el período de entrenamiento (MSE, RMSE, MAE, R², max/min error, percentil 90, precisión), métricas desde la fecha fin hasta hoy (error promedio, precisión y precisión de tendencia para medir performance real post-entrenamiento), y los hiperparámetros usados (arquitectura, funciones, learning rate, max error, épocas).

### Historial

![Historial](screenshots/Pantalla%20de%20Historial.png)

Predicciones día a día del modelo activo: fecha, apertura, máximo, mínimo, cierre real, cierre predicho y diferencia en dólares.

### Ajustes

![Ajustes](screenshots/Pantalla%20de%20Ajustes.png)

Configuración de moneda (con conversión en tiempo real vía ExchangeRate API), red de refinamiento y efectos de sonido.

---

## Estructura del proyecto

```
NTI-client/
├── src/
│   ├── NTI/
│   │   ├── NTI.java                         # Clase principal, ventana, CardLayout
│   │   ├── PanelInicio.java                 # Dashboard
│   │   ├── PanelModelos.java                # Gestión y creación de modelos
│   │   ├── PanelHistorial.java              # Historial de predicciones
│   │   ├── PanelAjustes.java                # Configuración del usuario
│   │   ├── Lectura.java                     # Acceso a datos (DB + servidor)
│   │   ├── Registro.java                    # Escritura en base de datos
│   │   ├── Api.java                         # Integración con APIs externas
│   │   ├── Formato.java                     # Formateo de respuestas de API
│   │   ├── Modelo.java                      # Lógica de modelos y predicciones
│   │   ├── Entorno.java                     # Lectura/escritura de configuración
│   │   ├── Accion.java                      # Datos de la acción seleccionada
│   │   ├── Empresa.java                     # Datos de empresa
│   │   ├── Noticia.java                     # Noticias financieras
│   │   ├── Fuentes.java                     # Carga de tipografía Inter
│   │   ├── Tupla.java                       # Estructura de datos auxiliar
│   │   ├── RoundedBorder.java               # Borde redondeado personalizado
│   │   └── TransparentRoundedBorder.java
│   └── fonts/                               # Familia tipográfica Inter (variable)
├── lib/
│   ├── jfreechart-1.0.19.jar                # Gráficos de velas
│   ├── jcommon-1.0.23.jar
│   ├── mariadb-java-client-3.5.6.jar        # Conexión a base de datos
│   └── json-20250517.jar                    # Parseo de respuestas JSON
├── screenshots/                             # Capturas para el README
├── config.json                              # Preferencias del usuario
├── api_config.json                          # Claves de APIs externas
└── build.xml                                # Build con Apache Ant
```

---

## Configuración

**`config.json`** - preferencias del usuario, se sobreescribe desde la pantalla de Ajustes:
```json
{
    "red_refinamiento": true,
    "modo_oscuro": true,
    "moneda": "US Dolar (USD)",
    "idioma": "Español (ES)",
    "efectos_sonido": true
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
| JFreeChart | 1.0.19 | Gráfico de velas candlestick |
| MariaDB Java Client | 3.5.6 | Conexión a base de datos |
| org.json | 20250517 | Parseo de JSON del servidor |

---

## Equipo - Fref Labs

Desarrollado por Federico Battistello, Luca Guarna, Nicolás Pereira, Franco Perfetti y Juan Sirota  
