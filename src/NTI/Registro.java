package NTI;


import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Map;

public class Registro {
    
    private static final String URL_DB = "jdbc:mariadb://br1.aguilucho.ar:25579/NTI";
    private static final String USUARIO = "nti";
    private static final String PASSWORD = "NTISystem070104!";
    private static final String APP_CONFIG_FILE = "config.json";
    private static final String BASE_API_URL = "https://nti-api.aguilucho.ar/api";

    private boolean postRequest(String endpoint, String jsonPayload) throws IOException {
        URL url = new URL(BASE_API_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            // Read the response to check for "exito"
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse.optBoolean("exito", false);
            }
        } else {
            // Log error stream for debugging
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.err.println("API POST Error for " + endpoint + ": HTTP " + responseCode + " - " + response.toString());
            }
            return false;
        }
    }

    private boolean postRequestWithOkCheck(String endpoint, String jsonPayload) throws IOException {
        URL url = new URL(BASE_API_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse.optBoolean("ok", false);
            }
        } else {
            String errorMessage = "Error " + responseCode;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                // Intenta parsear el mensaje de error específico de la API
                JSONObject errorJson = new JSONObject(response.toString());
                errorMessage = errorJson.optString("mensaje", "No se pudo obtener el mensaje de error del servidor.");
            } catch (Exception e) {
                // Si no se puede parsear el JSON de error, se usa el código de error
                System.err.println("No se pudo parsear el stream de error: " + e.getMessage());
            }
            throw new IOException(errorMessage);
        }
    }
    
    public boolean actualizarCofig(String mon, boolean sfx, boolean rdr, int idModelo, String estiloGrafica, int volumen) {
        JSONObject obj = new JSONObject();
        obj.put("moneda", mon);
        obj.put("efectos_sonido", sfx);
        obj.put("red_refinamiento", rdr);
        obj.put("modelo", idModelo);
        obj.put("estilo_grafica", estiloGrafica);
        obj.put("volumen", volumen);

        try (FileWriter file = new FileWriter(APP_CONFIG_FILE)) {
            file.write(obj.toString(4));
            file.flush();
            return true;
        } catch (IOException e) {
            System.err.println("Error al escribir 'config.json': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean guardarPartida(String nombre, int puntaje, int ronda, java.util.Date fecha, String modo, String simbolo) {
        
        JSONObject partidaJson = new JSONObject();
        partidaJson.put("nombre", nombre);
        partidaJson.put("puntaje", puntaje);
        partidaJson.put("ronda", ronda);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        partidaJson.put("fecha", sdf.format(fecha));
        
        partidaJson.put("modo", modo);
        String tickerFinal = (simbolo == null || simbolo.isEmpty()) ? "KO" : simbolo;
        partidaJson.put("ticker", tickerFinal);
        
        try {
            boolean success = postRequest("/game/save-score", partidaJson.toString());
            if (success) {
                System.out.println("Partida guardada exitosamente via API.");
            } else {
                System.err.println("Fallo al guardar partida via API." + simbolo);
            }
            return success;
        } catch (IOException e) {
            System.err.println("Error de IO al guardar partida via API: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    //NO INCLUIR TODAVIA
    public boolean solicitarEntrenamientoModelo(Map<String, Object> parametros) {
        JSONObject modeloJson = new JSONObject();
        try {
            // Asegurarse de que los nombres de las claves coincidan con la API
            modeloJson.put("Ticker", parametros.get("Ticker"));
            modeloJson.put("Arquitectura", parametros.get("Arquitectura"));
            modeloJson.put("Funciones", parametros.get("Funciones"));
            modeloJson.put("LearningRate", parametros.get("LearningRate"));
            modeloJson.put("MaxError", parametros.get("MaxError"));
            modeloJson.put("MaxIter", parametros.get("MaxIter"));
            modeloJson.put("Features", parametros.get("Features"));
            modeloJson.put("FechaInicio", parametros.get("FechaInicio"));
            modeloJson.put("FechaFin", parametros.get("FechaFin"));

            String endpoint = "/models";
            System.out.println("DEBUG: Enviando JSON al endpoint " + endpoint + ":");
            System.out.println(modeloJson.toString(4)); 

            return postRequest(endpoint, modeloJson.toString());
        } catch (Exception e) {
            System.err.println("Error al solicitar entrenamiento de modelo via API: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
