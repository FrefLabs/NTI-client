/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package NTI;


import java.io.FileWriter;
import java.io.IOException;
import org.json.JSONObject;

public class Registro {
    public boolean actualizarCofig(String mon, String idm, boolean sfx, boolean modo, boolean rdr) {
        boolean result = false;

        // Crear objeto JSON con la estructura deseada
        JSONObject config = new JSONObject();
        config.put("moneda", mon);
        config.put("idioma", idm);
        config.put("efectos_sonido", sfx);
        config.put("modo_oscuro", modo);
        config.put("red_refinamiento", rdr);

        // Intentar escribir en el archivo /config.json
        try (FileWriter file = new FileWriter("config.json")) { // ruta 
            file.write(config.toString(4));
            file.flush();
            result = true;
        } catch (IOException ex) {
            ex.printStackTrace();
            result = false;
        }

        return result;
    }
}
