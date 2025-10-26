/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package NTI;


import java.io.FileWriter;
import java.io.IOException;
import org.json.JSONObject;
import java.sql.*;

public class Registro {
    
    private static final String URL = "jdbc:mariadb://br1.aguilucho.ar:25579/NTI";
    private static final String USUARIO = "nti";
    private static final String PASSWORD = "NTISystem070104!";
    
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
    
    public boolean cargaDatosN(Tupla noticia) {
        String sql = "{CALL InsertarDatosNoticia(?, ?, ?, ?)}";

        try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, noticia.getUrl());
            stmt.setString(2, noticia.getTitulo());
            stmt.setDate(3, java.sql.Date.valueOf(noticia.getFecha())); // YYYY-MM-DD
            stmt.setString(4, noticia.getFuente());

            stmt.execute();
            System.out.println("Noticia insertada: " + noticia.getTitulo());
            return true;

        } catch (SQLException e) {
            System.err.println("Error al insertar noticia '" + noticia.getTitulo() + "': " + e.getMessage());
            return false;
        }
    }
}
