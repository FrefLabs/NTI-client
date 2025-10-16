package prototipo;

import java.sql.*;
import java.util.Vector;

public class Lectura {

    // Datos de conexión a la BD
    private static final String URL = "jdbc:mariadb://br1.aguilucho.ar:25579/NTI";
    private static final String USUARIO = "nti";
    private static final String PASSWORD = "NTISystem070104!";

    public Vector<Empresa> obtenerEmpresasDesdeBD() {
        Vector<Empresa> empresas = new Vector<>();

        // Llamado al Stored Procedure
        String storedProc = "{CALL ObtenerDatosEmpresa()}";

        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;

        try {
            // Carga de Driver
            Class.forName("org.mariadb.jdbc.Driver");

            // Conectar con BD
            conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);

            // Procedure
            stmt = conn.prepareCall(storedProc);

            // exec
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
            // Cerrar
            try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }

        return empresas;
    }
}
