package NTI;

import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

public class Formato {

    public Vector<Tupla> formatDatosN(String key) {
        Vector<Tupla> listaNoticias = new Vector<>();

        try {
            // Fecha del sistema
            Date hoy = new Date();

            // Fecha de hace 5 días
            Calendar cal = Calendar.getInstance();
            cal.setTime(hoy);
            cal.add(Calendar.DAY_OF_MONTH, -5);
            Date hace5Dias = cal.getTime();

            // Formatear fechas a YYYY-MM-DD para la BD
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String fechaHoy = sdf.format(hoy);
            String fechaInicio = sdf.format(hace5Dias);

            // URL con rango de 5 días
            String urlStr = "https://finnhub.io/api/v1/company-news?symbol=KO&from="
                    + fechaInicio + "&to=" + fechaHoy + "&token=" + key;

            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            JSONArray noticias = new JSONArray(response.toString());

            // Solo tomar las dos primeras noticias
            int limite = Math.min(2, noticias.length());
            for (int i = 0; i < limite; i++) {
                JSONObject obj = noticias.getJSONObject(i);
                String titulo = obj.optString("headline", "Sin título");
                String fuente = obj.optString("source", "Desconocida");
                String urlNoticia = obj.optString("url", "");

                // Guardar la fecha del sistema en la Tupla
                listaNoticias.add(new Tupla(titulo, fuente, urlNoticia, fechaHoy));
            }

        } catch (Exception e) {
            System.out.println("Error al obtener noticias: " + e.getMessage());
        }

        return listaNoticias;
    }
}
