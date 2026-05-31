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

    
    public static boolean validarNombre(String nombre) {
        // Verifica longitud
        if (nombre == null || nombre.length() < 3 || nombre.length() > 10) {
            return false;
        }
        // Verifica solo letras
        for (int i = 0; i < nombre.length(); i++) {
            char c = nombre.charAt(i);
            if (!Character.isLetter(c)) {
                return false;
            }
        }
        return true;
    }
}
