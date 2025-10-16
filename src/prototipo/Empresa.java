package prototipo;

import java.util.Vector;

public class Empresa {
    private String nombreEmpresa;
    private String descripcion;

    public Empresa() {
    }

    // Constructor con variables
    public Empresa(String nombreEmpresa, String descripcion) {
        this.nombreEmpresa = nombreEmpresa;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Vector<Empresa> obtenerEmpresasDesdeBD() {
        Lectura lectura = new Lectura();
        return lectura.obtenerEmpresasDesdeBD();
    }
}
