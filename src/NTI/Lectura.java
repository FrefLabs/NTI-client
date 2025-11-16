package NTI;

import java.sql.*;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap; 
import java.util.Map;   
import org.json.JSONObject;


public class Lectura {

    // Datos de conexión a la BD
    private static final String URL = "jdbc:mariadb://br1.aguilucho.ar:25579/NTI";
    private static final String USUARIO = "nti";
    private static final String PASSWORD = "NTISystem070104!";
    private static final String CONFIG_FILE = "api_config.json";

    public Vector<Empresa> obtenerEmpresasDesdeBD() {
        Vector<Empresa> empresas = new Vector<>();
        String storedProc = "{CALL ObtenerDatosEmpresaSimbolo(?)}";
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName("org.mariadb.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
            stmt = conn.prepareCall(storedProc);
            stmt.setString(1, "KO"); 
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
            try { if (rs != null) rs.close(); } catch (SQLException ignored) { }
            try { if (stmt != null) stmt.close(); } catch (SQLException ignored) { }
            try { if (conn != null) conn.close(); } catch (SQLException ignored) { }
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
            return null; 
        }
        return valores;
    }

    private String extraerValores(String json, String clave) {
        String busqueda = "\"" + clave + "\":";
        int index = json.indexOf(busqueda);
        if (index == -1) {
            return null;
        }
        String resto = json.substring(index + busqueda.length()).trim();
        if (resto.startsWith("true")) {
            return "true";
        }
        if (resto.startsWith("false")) {
            return "false";
        }
        if (resto.startsWith("\"")) {
            int fin = resto.indexOf("\"", 1);
            if (fin != -1) {
                return resto.substring(1, fin);
            }
        }
        return null;
    }
    
    public String obtenerKey(String nombre) {
        try (BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE))) {
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea);
            }
            JSONObject obj = new JSONObject(sb.toString());
            if (!obj.has(nombre)) {
                System.err.println("No se encontró la clave para la API: " + nombre);
                return "";
            }
            return obj.getString(nombre);
        } catch (IOException e) {
            System.err.println("Error leyendo archivo JSON: " + e.getMessage());
            return "";
        }
    }

    
    public Vector<Tupla> obtenerDatosN(Date fecha) {
        Vector<Tupla> lista = new Vector<>();
        String sql = "{CALL ObtenerNoticiasPorFecha(?)}";

        try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
             CallableStatement stmt = conn.prepareCall(sql)) {

            java.sql.Date sqlDate = new java.sql.Date(fecha.getTime());
            stmt.setDate(1, sqlDate);

            boolean tieneResultados = stmt.execute();

            if (tieneResultados) {
                try (ResultSet rs = stmt.getResultSet()) {
                    ResultSetMetaData meta = rs.getMetaData();
                    boolean columnasValidas = false;
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        String col = meta.getColumnLabel(i);
                        if (col.equalsIgnoreCase("URL") || col.equalsIgnoreCase("Titular") || col.equalsIgnoreCase("Fuente")) {
                            columnasValidas = true;
                            break;
                        }
                    }
                    if (!columnasValidas) {
                        System.out.println("No se obtuvieron noticias.");
                        return lista; // vector vacío
                    }
                    while (rs.next()) {
                        String url = rs.getString("URL");
                        String titular = rs.getString("Titular");
                        String fuente = rs.getString("Fuente");
                        String fechaStr = new SimpleDateFormat("yyyy-MM-dd").format(fecha);
                        lista.add(new Tupla(titular, fuente, url, fechaStr));
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener noticias por fecha: " + e.getMessage());
        }
        return lista; 
    }

    
    public Map<String, Vector<String[]>> obtenerRankings() {
        Map<String, Vector<String[]>> rankings = new HashMap<>();
        String sql = "{CALL ObtenerRankings()}";

        try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
             CallableStatement stmt = conn.prepareCall(sql)) {

            boolean hasResultSet = stmt.execute();

            // --- Leer el primer ResultSet (Endless) ---
            if (hasResultSet) {
                try (ResultSet rsEndless = stmt.getResultSet()) {
                    Vector<String[]> endlessList = new Vector<>();
                    while (rsEndless.next()) {
                        // Verificar si el SP devolvió un error
                        if (rsEndless.getMetaData().getColumnCount() == 2 && rsEndless.getString(1).equals("ERROR")) {
                            System.err.println("Error del SP al obtener rankings: " + rsEndless.getString(2));
                            return rankings; // Devuelve mapa vacío
                        }
                        
                        String[] fila = new String[3];
                        fila[0] = rsEndless.getString("P_Nombre");
                        fila[1] = String.valueOf(rsEndless.getInt("Puntaje"));
                        fila[2] = rsEndless.getString("Simbolo");
                        endlessList.add(fila);
                    }
                    rankings.put("Endless", endlessList);
                }
            }

            // --- Leer el segundo ResultSet (Tira y Afloje) ---
            if (stmt.getMoreResults()) {
                try (ResultSet rsTyA = stmt.getResultSet()) {
                    Vector<String[]> tyaList = new Vector<>();
                    while (rsTyA.next()) {
                        String[] fila = new String[3];
                        fila[0] = rsTyA.getString("P_Nombre");
                        fila[1] = String.valueOf(rsTyA.getInt("Ronda"));
                        fila[2] = rsTyA.getString("Simbolo");
                        tyaList.add(fila);
                    }
                    rankings.put("TyA", tyaList);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error de SQL al obtener rankings: " + e.getMessage());
            e.printStackTrace();
        }

        return rankings;
    }
    
    public Vector<Map<String, Object>> getModelosRecomendados(int idModeloAExcluir, int limite) {
        Vector<Map<String, Object>> topModelos = new Vector<>();

        // (MODIFICADO) 2. Añadir '?' a la llamada SQL
        String sql = "{CALL ObtenerTop3Modelos(?, ?)}";

        try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
                CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, idModeloAExcluir);
            stmt.setInt(2, limite); // (NUEVO) 3. Settear el parámetro

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> modelo = new HashMap<>();
                    modelo.put("IDModelo", rs.getInt("IDModelo"));
                    modelo.put("Precision", rs.getDouble("Precision"));
                    modelo.put("Simbolo", rs.getString("Simbolo"));
                    modelo.put("MAE", rs.getDouble("MAE"));
                    modelo.put("E_Nombre", rs.getString("E_Nombre"));
                    modelo.put("FechaIni", rs.getDate("FechaIni"));
                    modelo.put("FechaFin", rs.getDate("FechaFin"));
                    topModelos.add(modelo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error de SQL al obtener top 3 modelos: " + e.getMessage());
            e.printStackTrace();
        }
        return topModelos;
    }
    
    public Map<String, Object> getInfoModeloResumida(int idModelo) {
        Map<String, Object> modelo = null;
        String sql = "{CALL GetModeloInfoResumida(?)}";

        try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
                CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, idModelo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    modelo = new HashMap<>();
                    modelo.put("IDModelo", rs.getInt("IDModelo"));
                    modelo.put("Simbolo", rs.getString("Simbolo"));
                    modelo.put("Precision", rs.getDouble("Precision"));
                    modelo.put("PromedioError", rs.getDouble("PromedioError"));
                    modelo.put("FechaIni", rs.getDate("FechaIni"));
                    modelo.put("FechaFin", rs.getDate("FechaFin"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error de SQL al obtener info resumida: " + e.getMessage());
            e.printStackTrace();
        }
        return modelo; // Devuelve null si no se encuentra
    }

// Método para 'GetModeloDetallado'
    public Map<String, Object> getDetallesModelo(int idModelo) {
        Map<String, Object> detalles = null;
        String sql = "{CALL GetModeloDetallado(?)}";

        try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
                CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, idModelo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    detalles = new HashMap<>();
                    // IDs y Nombres
                    detalles.put("IDModelo", rs.getInt("IDModelo"));
                    detalles.put("Simbolo", rs.getString("Simbolo"));
                    detalles.put("E_Descripcion", rs.getString("E_Descripcion"));
                    // Resultados
                    detalles.put("MSE", rs.getDouble("MSE"));
                    detalles.put("RMSE", rs.getDouble("RMSE"));
                    detalles.put("MAE", rs.getDouble("MAE"));
                    detalles.put("R2", rs.getDouble("R2"));
                    detalles.put("MaxError", rs.getDouble("MaxError"));
                    detalles.put("MinError", rs.getDouble("MinError"));
                    detalles.put("Percentil90", rs.getDouble("Percentil90"));
                    detalles.put("Precision", rs.getDouble("Precision"));
                    // Parámetros
                    detalles.put("FechaIni", rs.getDate("FechaIni"));
                    detalles.put("FechaFin", rs.getDate("FechaFin"));
                    detalles.put("Arquitectura", rs.getString("Arquitectura"));
                    detalles.put("Funciones", rs.getString("Funciones"));
                    detalles.put("TasaAprendizaje", rs.getDouble("TasaAprendizaje"));
                    detalles.put("NMaxError", rs.getDouble("NMaxError"));
                    detalles.put("Epocas", rs.getInt("Epocas"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error de SQL al obtener detalles de modelo: " + e.getMessage());
            e.printStackTrace();
        }
        return detalles;
    }
    
    public Vector<Map<String, Object>> getHistorialModelo(int idModelo) {
        Vector<Map<String, Object>> historial = new Vector<>();
        String sql = "{CALL ObtenerHistorialModelo(?)}";

        try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, idModelo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new HashMap<>();
                    fila.put("H_Fecha", rs.getDate("H_Fecha"));
                    fila.put("Prediccion", rs.getDouble("Prediccion"));
                    fila.put("ValorAbierto", rs.getDouble("ValorAbierto"));
                    fila.put("ValorCerrado", rs.getDouble("ValorCerrado"));
                    fila.put("ValorAlto", rs.getDouble("ValorAlto"));
                    fila.put("ValorBajo", rs.getDouble("ValorBajo"));
                    historial.add(fila);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error de SQL al obtener historial del modelo: " + e.getMessage());
            e.printStackTrace();
        }
        return historial;
    }
    
    public Vector<Map<String, Object>> buscarModelosEnBD(String busqueda) {
        Vector<Map<String, Object>> resultados = new Vector<>();
        String sql = "{CALL BuscarModelos(?)}";

        try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setString(1, busqueda);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> modelo = new HashMap<>();
                    modelo.put("IDModelo", rs.getInt("IDModelo"));
                    modelo.put("Simbolo", rs.getString("Simbolo"));
                    modelo.put("Precision", rs.getDouble("Precision"));
                    modelo.put("FechaIni", rs.getDate("FechaIni"));
                    modelo.put("FechaFin", rs.getDate("FechaFin"));
                    modelo.put("MAE", rs.getDouble("MAE"));
                    resultados.add(modelo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error de SQL al buscar modelos: " + e.getMessage());
            e.printStackTrace();
        }
        return resultados;
    }
}