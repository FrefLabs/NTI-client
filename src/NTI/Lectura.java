package NTI;

import java.sql.*;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Lectura {

    // Datos de conexión a la BD
    private static final String URL = "jdbc:mariadb://br1.aguilucho.ar:25579/NTI";
    private static final String USUARIO = "nti";
    private static final String PASSWORD = "NTISystem070104!";

    public Vector<Empresa> obtenerEmpresasDesdeBD() {
        Vector<Empresa> empresas = new Vector<>();

        // Llamado al Stored Procedure
        String storedProc = "{CALL ObtenerDatosEmpresaSimbolo(?)}";

        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;

        try {
            // Cargar Driver
            Class.forName("org.mariadb.jdbc.Driver");

            // Conectar con BD
            conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);

            // Preparar llamado al procedimiento
            stmt = conn.prepareCall(storedProc);
            stmt.setString(1, "KO"); 

            // Ejecutar
            rs = stmt.executeQuery();

            while (rs.next()) {
                String nombre = rs.getString("e_nombre");
                String descripcion = rs.getString("e_descripcion");

                Empresa empresa = new Empresa(nombre, descripcion);
                empresas.add(empresa);
            }

        } catch (ClassNotFoundException e) {
            System.err.println("Error: no se encontró el driver de MariaDB. Asegúrate de tener el JAR en el classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error al obtener datos de la empresa: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cerrar recursos
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }

        return empresas;
    }
    
    public String[] obtenerConfig() {
        String[] valores = new String[5]; // moneda, idioma, sfx, modo, red

        try (BufferedReader br = new BufferedReader(new FileReader("config.json"))) {
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea);
            }

            String json = sb.toString();

            valores[0] = extraerValores(json, "moneda");           // moneda
            valores[1] = extraerValores(json, "idioma");           // idioma
            valores[2] = extraerValores(json, "efectos_sonido");   // sfx
            valores[3] = extraerValores(json, "modo_oscuro");      // modo oscuro
            valores[4] = extraerValores(json, "red_refinamiento"); // red

        } catch (IOException e) {
            e.printStackTrace();
            return null; // o un arreglo vacío según prefieras
        }

        return valores;
    }

    private String extraerValores(String json, String clave) {
        String busqueda = "\"" + clave + "\":";
        int index = json.indexOf(busqueda);
        if (index == -1) {
            return null;
        }

        // agarra valor
        String resto = json.substring(index + busqueda.length()).trim();

        // Si es booleano
        if (resto.startsWith("true")) {
            return "true";
        }
        if (resto.startsWith("false")) {
            return "false";
        }

        // Si es string 
        if (resto.startsWith("\"")) {
            int fin = resto.indexOf("\"", 1);
            if (fin != -1) {
                return resto.substring(1, fin);
            }
        }

        return null;
    }
}
