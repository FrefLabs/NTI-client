package NTI;


import java.io.FileWriter;
import java.io.IOException;
import org.json.JSONObject;
import java.sql.*;

public class Registro {
    
    private static final String URL = "jdbc:mariadb://br1.aguilucho.ar:25579/NTI";
    private static final String USUARIO = "nti";
    private static final String PASSWORD = "NTISystem070104!";
    
    public boolean actualizarCofig(String mon, boolean sfx, boolean rdr, int idModelo, String estiloGrafica) {
        boolean result = false;

        JSONObject config = new JSONObject();
        config.put("moneda", mon);
        config.put("efectos_sonido", sfx);
        config.put("red_refinamiento", rdr);
        config.put("modelo", idModelo); // <-- NUEVO
        config.put("estilo_grafica", estiloGrafica); // <-- NUEVO
        
        // (Guardamos los valores eliminados por compatibilidad)
        config.put("modo_oscuro", true);
        config.put("idioma", "Español (ES)");

        try (FileWriter file = new FileWriter("config.json")) {
            file.write(config.toString(4)); // El '4' lo indenta
            file.flush();
            result = true;
        } catch (IOException ex) {
            ex.printStackTrace();
            result = false;
        }
        return result;
    }
    
    public boolean cargaDatosN(Tupla noticia, String simbolo) {
        String sql = "{CALL InsertarDatosNoticia(?, ?, ?, ?, ?)}"; // 5 params

        try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, noticia.getUrl());
            stmt.setString(2, noticia.getTitulo());
            stmt.setDate(3, java.sql.Date.valueOf(noticia.getFecha())); // YYYY-MM-DD
            stmt.setString(4, noticia.getFuente());
            stmt.setString(5, simbolo); // (Parámetro nuevo)

            boolean hasResult = stmt.execute();
            
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    if (rs.next()) {
                        String estado = rs.getString("Estado");
                        if (estado.equals("EXITO")) {
                            System.out.println("Noticia insertada: " + noticia.getTitulo());
                            return true;
                        } else {
                            System.err.println("Error al insertar noticia (SP): " + rs.getString("Mensaje"));
                            return false;
                        }
                    }
                }
            }
            return false; // Si el SP no devolvió un ResultSet de estado

        } catch (SQLException e) {
            System.err.println("Error de SQL al insertar noticia '" + noticia.getTitulo() + "': " + e.getMessage());
            return false;
        }
    }
    
    public boolean guardarPartida(String nombre, int puntaje, int ronda, java.util.Date fecha, String modo, String simbolo) {
        
        String sql = "{CALL InsertarDatosPartida(?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, nombre);
            stmt.setInt(2, puntaje);
            stmt.setInt(3, ronda);
            stmt.setDate(4, new java.sql.Date(fecha.getTime()));
            stmt.setString(5, modo);
            stmt.setString(6, simbolo);

            boolean hasResult = stmt.execute();
            
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    if (rs.next()) {
                        String estado = rs.getString("Estado");
                        if (estado.equals("EXITO")) {
                            System.out.println("Partida guardada exitosamente. ID: " + rs.getInt("IdPartida"));
                            return true;
                        } else {
                            System.err.println("Error al guardar partida (SP): " + rs.getString("Mensaje"));
                            return false;
                        }
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error de SQL al guardar partida: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
}