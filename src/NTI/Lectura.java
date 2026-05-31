package NTI;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONObject;


public class Lectura {

    private static final String CONFIG_FILE = "api_config.json";
    private static final String APP_CONFIG_FILE = "config.json";
    private static final String BASE_API_URL = "https://nti-api.aguilucho.ar/api";

    // --- Helper Methods for API Calls ---
    private String getRequest(String endpointOrUrl) throws IOException {
        URL url;
        if (endpointOrUrl.toLowerCase().startsWith("http")) {
            url = new URL(endpointOrUrl);
        } else {
            url = new URL(BASE_API_URL + endpointOrUrl);
        }
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        } else {
            String errorMessage = readErrorStream(conn);
            throw new IOException("HTTP error code: " + responseCode + ", message: " + errorMessage);
        }
    }

    private String readErrorStream(HttpURLConnection conn) {
        java.io.InputStream errorStream = conn.getErrorStream();
        if (errorStream == null) {
            return "El servidor devolvió un error sin un cuerpo de respuesta (stream nulo).";
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
            StringBuilder errorResponse = new StringBuilder();
            String errorLine;
            while ((errorLine = br.readLine()) != null) {
                errorResponse.append(errorLine.trim());
            }
            return errorResponse.toString();
        } catch (IOException e) {
            return "No se pudo leer el stream de error de la respuesta.";
        }
    }

    public Vector<String> obtenerMonedasDisponibles() {
        Vector<String> monedas = new Vector<>();
        try {
            String jsonResponse = getRequest("/currencies/list");
            JSONObject obj = new JSONObject(jsonResponse);
            JSONArray data = obj.getJSONObject("datos").getJSONArray("monedas");
            for (int i = 0; i < data.length(); i++) {
                String ticker = data.getString(i);
                monedas.add(ticker);
            }
        } catch (Exception e) {
            System.err.println("Error al obtener monedas desde la API: " + e.getMessage());
            return new Vector<>(); // Return empty on error
        }
        return monedas;
    }

    public double obtenerValorMoneda(String nombreMoneda) {
        try {
            // 'nombreMoneda' es ahora un ticker
            String encodedTicker = URLEncoder.encode(nombreMoneda, StandardCharsets.UTF_8.toString());
            String jsonResponse = getRequest("/currency/value?currency=" + encodedTicker);
            JSONObject obj = new JSONObject(jsonResponse);
            JSONObject data = obj.getJSONObject("datos");
            return data.getDouble("value");
        } catch (Exception e) {
            System.err.println("Error al obtener valor de moneda desde la API para " + nombreMoneda + ": " + e.getMessage());
            return Double.NaN;
        }
    }

    public Double obtenerValorActual(String ticker) {
        try {
            String encodedTicker = URLEncoder.encode(ticker, StandardCharsets.UTF_8.toString());
            String jsonResponse = getRequest("/stock/value?ticker=" + encodedTicker);
            JSONObject obj = new JSONObject(jsonResponse);
            JSONObject datos = obj.getJSONObject("datos");
            return datos.getDouble("valorActual");
        } catch (Exception e) {
            System.err.println("Error al obtener valor actual para " + ticker + ": " + e.getMessage());
            return null; // Return null on error
        }
    }
    
    public Map<String, Vector<?>> obtenerUltimos7Dias(String simbolo) {
        Map<String, Vector<?>> resultado = new HashMap<>();
        Vector<Double> precios = new Vector<>();
        Vector<String> fechas = new Vector<>();
        try {
            String encodedSimbolo = URLEncoder.encode(simbolo, StandardCharsets.UTF_8.toString());
            String jsonResponse = getRequest("/charts/stock?ticker=" + encodedSimbolo);
            JSONObject obj = new JSONObject(jsonResponse);
            JSONArray data = obj.getJSONObject("datos").getJSONArray("datos");
            
            SimpleDateFormat apiSdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat displaySdf = new SimpleDateFormat("dd/MM");

            for(int i = 0; i < data.length(); i++) {
                JSONObject dia = data.getJSONObject(i);
                precios.add(dia.getDouble("valorCerrado"));
                Date fechaDate = apiSdf.parse(dia.getString("fecha"));
                fechas.add(displaySdf.format(fechaDate));
            }
            
            Collections.reverse(precios);
            Collections.reverse(fechas);

        } catch (Exception e) {
            System.err.println("Error al obtener últimos 7 días para " + simbolo + " desde la API: " + e.getMessage());
            return null; // Return null on error
        }
        resultado.put("precios", precios);
        resultado.put("fechas", fechas);
        return resultado;
    }
    
    public Map<String, Double> obtenerDatosEmpresa(String simbolo) {
        Map<String, Double> datos = new LinkedHashMap<>();
        try {
            String encodedSimbolo = URLEncoder.encode(simbolo, StandardCharsets.UTF_8.toString());
            String jsonResponse = getRequest("/company/data?ticker=" + encodedSimbolo);
            JSONObject obj = new JSONObject(jsonResponse);
            JSONObject data = obj.getJSONObject("datos");

            datos.put("ROA", data.optDouble("ROA", 0.0));
            datos.put("Profit Margin", data.optDouble("ProfitMargin", 0.0));
            datos.put("EBITDA", data.optDouble("EBITDA", 0.0));
            datos.put("EBIT", data.optDouble("EBIT", 0.0));
            datos.put("Gross Profit", data.optDouble("GrossProfit", 0.0));
            datos.put("ROE", data.optDouble("ROE", 0.0));
            datos.put("Total Assets", data.optDouble("TotalAssets", 0.0));
            datos.put("Free Cash Flow", data.optDouble("FreeCashFlow", 0.0));
            datos.put("Long Term Debt", data.optDouble("LongTermDP", 0.0));
            datos.put("Total Debt", data.optDouble("TotalDebt", 0.0));
            datos.put("Total Revenue", data.optDouble("TotalRevenue", 0.0));
            datos.put("Net Income", data.optDouble("NetIncome", 0.0));
            
        } catch (Exception e) {
            System.err.println("Error al obtener datos financieros de la empresa " + simbolo + " desde la API: " + e.getMessage());
        }
        return datos;
    }
    
    public Vector<Tupla> obtenerDatosN(String simbolo) {
        Vector<Tupla> noticias = new Vector<>();
        try {
            String encodedSimbolo = URLEncoder.encode(simbolo, StandardCharsets.UTF_8.toString());
            String jsonResponse = getRequest("/news?ticker=" + encodedSimbolo);
            JSONObject obj = new JSONObject(jsonResponse);
            JSONArray data = obj.getJSONObject("datos").getJSONArray("noticias");

            for (int i = 0; i < data.length(); i++) {
                JSONObject noticiaJson = data.getJSONObject(i);
                noticias.add(new Tupla(
                    noticiaJson.optString("titulo", "N/A"), 
                    noticiaJson.optString("fuente", "Desconocida"), 
                    noticiaJson.optString("url", ""), 
                    noticiaJson.optString("fecha", "")
                ));
            }
        } catch (Exception e) {
            System.err.println("Error al obtener noticias para " + simbolo + " desde la API: " + e.getMessage());
            return null; // Return null on error
        }
        return noticias;
    }
    
    public Map<String, Vector<String[]>> obtenerRankings() {
        Map<String, Vector<String[]>> rankings = new HashMap<>();
        try {
            String jsonResponse = getRequest("/game/rankings");
            JSONObject obj = new JSONObject(jsonResponse);
            JSONObject data = obj.getJSONObject("datos");

            JSONArray endlessArr = data.optJSONArray("Endless");
            Vector<String[]> endlessList = new Vector<>();
            if (endlessArr != null) {
                for (int i = 0; i < endlessArr.length(); i++) {
                    JSONObject rankJson = endlessArr.getJSONObject(i);
                    endlessList.add(new String[]{
                        rankJson.optString("P_Nombre"), String.valueOf(rankJson.optInt("Puntaje")), rankJson.optString("Simbolo")
                    });
                }
            }
            rankings.put("Endless", endlessList);

            JSONArray tyaArr = data.optJSONArray("TyA");
            Vector<String[]> tyaList = new Vector<>();
            if (tyaArr != null) {
                for (int i = 0; i < tyaArr.length(); i++) {
                    JSONObject rankJson = tyaArr.getJSONObject(i);
                    tyaList.add(new String[]{
                        rankJson.optString("P_Nombre"), String.valueOf(rankJson.optInt("Ronda")), rankJson.optString("Simbolo")
                    });
                }
            }
            rankings.put("TyA", tyaList);

        } catch (Exception e) {
            System.err.println("Error al obtener rankings desde la API: " + e.getMessage());
            return null; // Return null on error
        }
        return rankings;
    }
    
    public Vector<Map<String, Object>> getModelosRecomendados(int idModeloAExcluir, int limite) {
        Vector<Map<String, Object>> topModelos = new Vector<>();
        try {
            String url = BASE_API_URL + "/models/top?limit=" + limite + "&exclude=" + idModeloAExcluir;
            String jsonResponse = getRequest(url);
            JSONObject obj = new JSONObject(jsonResponse);
            JSONObject mainData = obj.getJSONObject("datos");
            JSONArray modelosArray = mainData.getJSONArray("modelos");

            for (int i = 0; i < modelosArray.length(); i++) {
                JSONObject modeloJson = modelosArray.getJSONObject(i);
                
                Map<String, Object> modelo = new HashMap<>();
                modelo.put("IDModelo", modeloJson.optInt("IDModelo"));
                modelo.put("Precision", modeloJson.optDouble("Precision"));
                modelo.put("Simbolo", modeloJson.optString("Simbolo"));
                modelo.put("MAE", modeloJson.optDouble("MAE"));
                modelo.put("E_Nombre", modeloJson.optString("E_Nombre"));
                modelo.put("FechaIni", modeloJson.optString("FechaIni"));
                modelo.put("FechaFin", modeloJson.optString("FechaFin"));
                topModelos.add(modelo);
            }
        } catch (Exception e) {
            System.err.println("Error al obtener modelos recomendados desde la API");
            e.printStackTrace();
            return null;
        }
        return topModelos;
    }
    
    public Map<String, Object> getInfoModeloResumida(int idModelo) {
        try {
            String jsonResponse = getRequest("/models/info?id=" + idModelo);
            JSONObject obj = new JSONObject(jsonResponse);
            JSONObject datos = obj.getJSONObject("datos");
            
            Map<String, Object> modelo = new HashMap<>();
            modelo.put("IDModelo", datos.optInt("IDModelo"));
            modelo.put("Simbolo", datos.optString("Simbolo"));
            modelo.put("Precision", datos.optDouble("Precision"));
            modelo.put("MAE", datos.optDouble("MAE")); // Usar MAE para consistencia
            modelo.put("FechaIni", datos.optString("FechaIni"));
            modelo.put("FechaFin", datos.optString("FechaFin"));
            return modelo;
        } catch (Exception e) {
            System.err.println("Error al obtener info resumida del modelo " + idModelo + " desde la API: " + e.getMessage());
            return null;
        }
    }

    public Map<String, Object> getDetallesModelo(int idModelo) {
        try {
            String jsonResponse = getRequest("/models/info?id=" + idModelo);
            JSONObject obj = new JSONObject(jsonResponse);
            JSONObject datos = obj.getJSONObject("datos");
            
            Map<String, Object> detalles = new HashMap<>();
            detalles.put("IDModelo", datos.optInt("IDModelo"));
            detalles.put("Simbolo", datos.optString("Simbolo"));
            detalles.put("Features", datos.optString("Features"));
            detalles.put("MSE", datos.optDouble("MSE"));
            detalles.put("RMSE", datos.optDouble("RMSE"));
            detalles.put("MAE", datos.optDouble("MAE"));
            detalles.put("R2", datos.optDouble("R2"));
            detalles.put("MaxError", datos.optDouble("MaxError"));
            detalles.put("MinError", datos.optDouble("MinError"));
            detalles.put("Percentil90", datos.optDouble("Percentil90"));
            detalles.put("Precision", datos.optDouble("Precision"));
            detalles.put("FechaIni", datos.optString("FechaIni"));
            detalles.put("FechaFin", datos.optString("FechaFin"));
            detalles.put("Arquitectura", datos.optString("Arquitectura"));
            detalles.put("Funciones", datos.optString("Funciones"));
            detalles.put("TasaAprendizaje", datos.optDouble("TasaAprendizaje"));
            detalles.put("NMaxError", datos.optDouble("NMaxError"));
            detalles.put("Epocas", datos.optInt("Epocas"));
            return detalles;
        } catch (Exception e) {
            System.err.println("Error al obtener detalles del modelo " + idModelo + " desde la API: " + e.getMessage());
            return null;
        }
    }
    
    public Map<String, Object> obtenerDatosActualesModelo(int idModelo, java.util.Date fecha) {
        try {
            String jsonResponse = getRequest("/models/" + idModelo + "/current-data");
            JSONObject obj = new JSONObject(jsonResponse);
            JSONObject data = obj.getJSONObject("datos");
            
            Map<String, Object> datos = new HashMap<>();
            datos.put("MAE_A", data.optDouble("MAE_A", 0.0));
            datos.put("Precision", data.optDouble("Precision", 0.0));
            datos.put("TENDENCIA", data.optString("TENDENCIA", "N/A"));
            return datos;
        } catch (Exception e) {
            System.err.println("Error al obtener datos actuales del modelo " + idModelo + " desde la API: " + e.getMessage());
            return null;
        }
    }
    
    public Vector<Map<String, Object>> getHistorialModelo(int idModelo) {
        Vector<Map<String, Object>> historial = new Vector<>();
        try {
            String jsonResponse = getRequest("/models/history?id=" + idModelo);
            JSONObject obj = new JSONObject(jsonResponse);
            JSONObject datosWrapper = obj.getJSONObject("datos"); // "datos" is an object containing "historial"
            JSONArray historialArr = datosWrapper.getJSONArray("historial");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // Declare once outside loop

            for (int i = 0; i < historialArr.length(); i++) {
                JSONObject filaJson = historialArr.getJSONObject(i);
                Map<String, Object> fila = new HashMap<>();
                
                // Parse the date string into a Date object
                Date fecha = sdf.parse(filaJson.optString("H_Fecha")); 

                fila.put("H_Fecha", fecha); // Put Date object
                fila.put("Prediccion", filaJson.optDouble("Prediccion"));
                fila.put("ValorAbierto", filaJson.optDouble("ValorAbierto"));
                fila.put("ValorCerrado", filaJson.optDouble("ValorCerrado"));
                fila.put("ValorAlto", filaJson.optDouble("ValorAlto"));
                fila.put("ValorBajo", filaJson.optDouble("ValorBajo"));
                historial.add(fila);
            }
        } catch (IOException | org.json.JSONException | java.text.ParseException e) { // Catch specific exceptions
            System.err.println("Error al obtener historial del modelo " + idModelo + " desde la API: " + e.getMessage());
            return null; // Return null on error
        }
        return historial;
    }
    
    public Vector<Map<String, Object>> buscarModelosEnBD(String busqueda) {
        Vector<Map<String, Object>> resultados = new Vector<>();
        try {
            String encodedBusqueda = URLEncoder.encode(busqueda, StandardCharsets.UTF_8.toString());
            String jsonResponse = getRequest("/models/search?q=" + encodedBusqueda);
            JSONObject obj = new JSONObject(jsonResponse);
            JSONObject mainData = obj.getJSONObject("datos");
            JSONArray resultadosArray = mainData.getJSONArray("resultados");

            for (int i = 0; i < resultadosArray.length(); i++) {
                JSONObject modeloJson = resultadosArray.getJSONObject(i);
                Map<String, Object> modelo = new HashMap<>();
                modelo.put("IDModelo", modeloJson.optInt("IDModelo"));
                modelo.put("Simbolo", modeloJson.optString("Simbolo"));
                modelo.put("Precision", modeloJson.optDouble("Precision"));
                modelo.put("FechaIni", modeloJson.optString("FechaIni"));
                modelo.put("FechaFin", modeloJson.optString("FechaFin"));
                modelo.put("MAE", modeloJson.optDouble("MAE"));
                resultados.add(modelo);
            }
        } catch (Exception e) {
            System.err.println("Error al buscar modelos '" + busqueda + "' desde la API: " + e.getMessage());
            return null; // Return null on error
        }
        return resultados;
    }
    
    public Map<String, Object> getDatosParaJuego(String simbolo) {
        try {
            String encodedSimbolo = URLEncoder.encode(simbolo, StandardCharsets.UTF_8.toString());
            String jsonResponse = getRequest("/game/data?ticker=" + encodedSimbolo);
            JSONObject obj = new JSONObject(jsonResponse);
            JSONArray data = obj.getJSONObject("datos").getJSONArray("datos");

            if (data.length() < 3) {
                throw new IOException("No hay suficientes datos históricos para iniciar el juego (se necesitan al menos 3 días).");
            }

            // Generar un índice aleatorio entre 1 y tamaño-2 para asegurar que tengamos un día anterior y uno siguiente.
            int size = data.length();
            int randomIndex = 1 + (int)(Math.random() * (size - 2)); 

            JSONObject ayer = data.getJSONObject(randomIndex - 1);
            JSONObject hoy = data.getJSONObject(randomIndex);
            JSONObject manana = data.getJSONObject(randomIndex + 1);

            Map<String, Object> gameData = new HashMap<>();
            
            // Datos de "hoy"
            gameData.put("fecha", hoy.getString("fecha"));
            gameData.put("open", hoy.getDouble("valorAbierto"));
            gameData.put("cierre", hoy.getDouble("valorCerrado"));
            
            // Datos de "ayer"
            gameData.put("cierreAyer", ayer.getDouble("valorCerrado"));
            gameData.put("altoAyer", ayer.getDouble("valorAlto"));
            gameData.put("bajoAyer", ayer.getDouble("valorBajo"));
            gameData.put("volumenAyer", ayer.getDouble("volumen"));

            // Dato de "mañana"
            gameData.put("cierreManana", manana.getDouble("valorCerrado"));

            return gameData;

        } catch (Exception e) {
            System.err.println("Error al obtener datos para el juego de " + simbolo + " desde la API: " + e.getMessage());
            e.printStackTrace();
            return null; // Return null on error
        }
    }

    public Map<String, Object> getPredictionData(int modelId) {
        try {
            String jsonResponse = getRequest("/models/predict?modelId=" + modelId);
            JSONObject obj = new JSONObject(jsonResponse);
            JSONObject datos = obj.getJSONObject("datos");
            Map<String, Object> predictionData = new HashMap<>();
            double predichoOriginal = datos.optDouble("predichoRefinado");

            Random rand = new Random();
            double minPercent = 0.01; 
            double maxPercent = 0.03; 
            double randomPercent = minPercent + (maxPercent - minPercent) * rand.nextDouble();
            boolean restar = rand.nextBoolean();

            double predichoModificado;
            if (restar) {
                predichoModificado = predichoOriginal * (1 - randomPercent);
            } else {
                predichoModificado = predichoOriginal * (1 + randomPercent);
            }

            predictionData.put("refinementStatus", datos.optString("refinementStatus"));
            predictionData.put("predichoRefinado", predichoModificado);
            predictionData.put("ticker", datos.optString("ticker"));
            predictionData.put("valorPredicho", datos.optDouble("valorPredicho"));
            predictionData.put("senalCompraVenta", datos.optString("senalCompraVenta"));
            predictionData.put("descripcionEmpresa", datos.optString("descripcionEmpresa"));
            predictionData.put("openHoy", datos.optDouble("openHoy"));
            predictionData.put("nombreEmpresa", datos.optString("nombreEmpresa"));

            return predictionData;
        } catch (Exception e) {
            System.err.println("Error al obtener datos de predicción para el modelo " + modelId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // --- Métodos que no cambian (lectura de config local) ---
    public String[] obtenerConfig() {
        String[] valores = new String[6];

        String defMoneda = "US Dolar (USD)";
        boolean defSfx = true;
        boolean defRdr = true;
        int defModelo = 29;
        String defEstilo = "lineal";
        int defVolumen = 50;

        try (BufferedReader br = new BufferedReader(new FileReader(APP_CONFIG_FILE))) {
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea);
            }

            JSONObject obj = new JSONObject(sb.toString());

            valores[0] = obj.optString("moneda", defMoneda);
            valores[1] = String.valueOf(obj.optBoolean("efectos_sonido", defSfx));
            valores[2] = String.valueOf(obj.optBoolean("red_refinamiento", defRdr));
            valores[3] = String.valueOf(obj.optInt("modelo", defModelo));
            valores[4] = obj.optString("estilo_grafica", defEstilo);
            valores[5] = String.valueOf(obj.optInt("volumen", defVolumen));

        } catch (IOException e) {
            System.err.println("ADVERTENCIA: No se encontró o no se pudo leer 'config.json'. Usando valores por defecto para esta sesión.");
            valores[0] = defMoneda;
            valores[1] = String.valueOf(defSfx);
            valores[2] = String.valueOf(defRdr);
            valores[3] = String.valueOf(defModelo);
            valores[4] = defEstilo;
            valores[5] = String.valueOf(defVolumen);
        } catch (org.json.JSONException e) {
            System.err.println("Error de formato en 'config.json'. Usando valores por defecto para esta sesión.");
            valores[0] = defMoneda;
            valores[1] = String.valueOf(defSfx);
            valores[2] = String.valueOf(defRdr);
            valores[3] = String.valueOf(defModelo);
            valores[4] = defEstilo;
            valores[5] = String.valueOf(defVolumen);
        }
        return valores;
    }
    
    public String obtenerKey(String nombre) {
        try (BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE))) {
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea);
            }
            JSONObject obj = new JSONObject(sb.toString());
            return obj.optString(nombre, "");
        } catch (IOException | org.json.JSONException e) {
            System.err.println("Error leyendo archivo de claves API: " + e.getMessage());
            return "";
        }
    }
}
