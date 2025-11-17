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
    private static final String APP_CONFIG_FILE = "config.json";

    public String obtenerSimboloPorIDModelo(int idModelo) {
        String sql = "{CALL ObtenerSimboloPorIDModelo(?)}";
        String simbolo = "KO"; // Fallback por si el ID no es válido

        try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
                CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, idModelo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    simbolo = rs.getString("Simbolo");
                } else {
                    System.err.println("ADVERTENCIA: No se encontró Símbolo para IDModelo " + idModelo + ". Usando 'KO'.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error de SQL al obtener símbolo: " + e.getMessage());
        }
        return simbolo;
    }
    
    public Vector<Empresa> obtenerEmpresasDesdeBD(String simbolo) {
        Vector<Empresa> empresas = new Vector<>();
        String storedProc = "{CALL ObtenerDatosEmpresaSimbolo(?)}";
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName("org.mariadb.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
            stmt = conn.prepareCall(storedProc);
            stmt.setString(1, simbolo); // (Usa el parámetro)
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
    
    // (El resto de Lectura.java permanece como lo enviaste)
    
    public String[] obtenerConfig() {
        // [0]moneda, [1]sfx, [2]rdr, [3]modelo(ID), [4]estilo_grafica
        String[] valores = new String[5]; 
        
        // --- Valores por defecto ---
        valores[0] = "US Dolar (USD)"; // moneda
        valores[1] = "true";           // efectos_sonido (SFX)
        valores[2] = "true";           // red_refinamiento (RDR)
        valores[3] = "29";             // modelo (IDModelo default 29)
        valores[4] = "lineal";         // estilo_grafica

        try (BufferedReader br = new BufferedReader(new FileReader(APP_CONFIG_FILE))) {
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea);
            }
            
            JSONObject obj = new JSONObject(sb.toString());
            
            // Leemos del JSON, si no existe, usamos el valor por defecto
            valores[0] = obj.optString("moneda", valores[0]);
            valores[1] = String.valueOf(obj.optBoolean("efectos_sonido", true)); // <-- SFX está en el índice 1
            valores[2] = String.valueOf(obj.optBoolean("red_refinamiento", true)); // <-- RDR está en el índice 2
            valores[3] = String.valueOf(obj.optInt("modelo", 29));
            valores[4] = obj.optString("estilo_grafica", valores[4]);

        } catch (IOException e) {
            System.err.println("ADVERTENCIA: No se encontró 'config.json'. Usando valores por defecto.");
        } catch (org.json.JSONException e) {
            System.err.println("Error de formato en 'config.json'. Usando valores por defecto.");
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

    
    public Vector<Tupla> obtenerDatosN(java.util.Date fecha, String simbolo) {
        Vector<Tupla> noticias = new Vector<>();
        String sql = "{CALL ObtenerNoticiasPorFecha(?, ?)}"; 
        java.sql.Date fechaSQL = new java.sql.Date(fecha.getTime());

        try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setDate(1, fechaSQL);
            stmt.setString(2, simbolo); // <-- Parámetro nuevo
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.getMetaData().getColumnName(1).equals("Estado")) {
                    rs.next();
                    System.out.println("Respuesta de ObtenerNoticias: " + rs.getString("Mensaje"));
                    return noticias; 
                }
                while (rs.next()) {
                    noticias.add(new Tupla(
                        rs.getString("Titular"),
                        rs.getString("Fuente"),
                        rs.getString("URL"),
                        fechaSQL.toString()
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return noticias;
    }

    
    public Map<String, Vector<String[]>> obtenerRankings() {
        Map<String, Vector<String[]>> rankings = new HashMap<>();
        String sql = "{CALL ObtenerRankings()}";

        try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
             CallableStatement stmt = conn.prepareCall(sql)) {

            boolean hasResultSet = stmt.execute();

            if (hasResultSet) {
                try (ResultSet rsEndless = stmt.getResultSet()) {
                    Vector<String[]> endlessList = new Vector<>();
                    while (rsEndless.next()) {
                        if (rsEndless.getMetaData().getColumnCount() == 2 && rsEndless.getString(1).equals("ERROR")) {
                            System.err.println("Error del SP al obtener rankings: " + rsEndless.getString(2));
                            return rankings;
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
        String sql = "{CALL ObtenerTop3Modelos(?, ?)}";

        try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, idModeloAExcluir);
            stmt.setInt(2, limite);

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
        String sql = "{CALL ObtenerModeloInfoResumida(?)}";

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

    public Map<String, Object> getDetallesModelo(int idModelo) {
        Map<String, Object> detalles = null;
        String sql = "{CALL ObtenerModeloDetallado(?)}";

        try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
                CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, idModelo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    detalles = new HashMap<>();
                    // IDs y Nombres
                    detalles.put("IDModelo", rs.getInt("IDModelo"));
                    detalles.put("Simbolo", rs.getString("Simbolo"));

                    // --- (INICIO DE LA CORRECCIÓN) ---
                    // (Se elimina la descripción de la empresa)
                    // detalles.put("E_Descripcion", rs.getString("E_Descripcion")); 
                    // (Se añade la nueva columna 'Features')
                    detalles.put("Features", rs.getString("Features"));
                    // --- (FIN DE LA CORRECCIÓN) ---

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
    
    public Map<String, Object> obtenerDatosActualesModelo(int idModelo, java.util.Date fecha) {
        Map<String, Object> datos = null;
        String sql = "{CALL ObtenerDatosActualesModelo(?, ?)}";
        java.sql.Date fechaSQL = new java.sql.Date(fecha.getTime());

        try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
                CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, idModelo);
            stmt.setDate(2, fechaSQL); // (Usa la fecha actual)

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    datos = new HashMap<>();
                    datos.put("MAE_A", rs.getDouble("MAE_A"));
                    datos.put("Precision", rs.getFloat("Precision"));
                    datos.put("TENDENCIA", rs.getFloat("TENDENCIA"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error de SQL al obtener datos actuales: " + e.getMessage());
            e.printStackTrace();
        }
        return datos; // Devuelve null si no se encuentra
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
        String sql = "{CALL BuscarModelos(?)}"; // (Asumo que el SP se llama 'BuscarModelos')

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
    
    /* (Estos son los métodos que añadimos en nuestra conversación) */
    
    public Vector<Map<String, Object>> getDatosParaJuego(String simbolo) {
        Vector<Map<String, Object>> datosJuego = new Vector<>();
        String sql = "{CALL ObtenerDatosJuego(?)}";

        try (Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setString(1, simbolo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> dia = new HashMap<>();
                    dia.put("Fecha", rs.getDate("DA_Fecha"));
                    dia.put("Abierto", rs.getDouble("ValorAbierto"));
                    dia.put("Alto", rs.getDouble("ValorAlto"));
                    dia.put("Bajo", rs.getDouble("ValorBajo"));
                    dia.put("Cerrado", rs.getDouble("ValorCerrado"));
                    dia.put("Volumen", rs.getDouble("Volumen"));
                    datosJuego.add(dia);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error de SQL al obtener datos del juego: " + e.getMessage());
            e.printStackTrace();
        }
        return datosJuego;
    }
}