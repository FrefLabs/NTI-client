package NTI;

import java.util.Random;
import java.util.Map;
import java.util.Vector;

public class Modelo {

    // prediccion con cuenta matematica
    public double hacerPrediccion(double cierreAyer, double cierreManana) {
        Random r = new Random();
        
        double min = Math.min(cierreAyer, cierreManana);
        double max = Math.max(cierreAyer, cierreManana);
        
        // Devuelve un valor aleatorio dentro de ese rango
        return min + (max - min) * r.nextDouble();
    }
    
    private Lectura lectura;
    private int IDModeloSeleccionado = 29;
    
    public Modelo() {
        
    }
    
    public Modelo(Lectura lectura) {
        this.lectura = lectura;
    }
    
    public int getIDModeloSeleccionado() {
        return this.IDModeloSeleccionado;
    }
    
    public void setIDModeloSeleccionado(int id) {
        this.IDModeloSeleccionado = id;
    }
    
    public Vector<Map<String, Object>> obtenerTop3ParaVista() {
        if (this.lectura == null);

        return lectura.getModelosRecomendados(0);
    }
    
    public Vector<Map<String, Object>> obtenerRecomendados() {
        if (this.lectura == null) {
            /* ... error ... */ }

        // Excluir el que ya está seleccionado
        return lectura.getModelosRecomendados(this.IDModeloSeleccionado);
    }

// Método para el panel "Modelo Seleccionado"
    public Map<String, Object> obtenerInfoSeleccionado() {
        if (this.lectura == null) {
            /* ... error ... */ }
        return lectura.getInfoModeloResumida(this.IDModeloSeleccionado);
    }

// Método para el panel "Detalle de Modelo"
    public Map<String, Object> obtenerDetalles(int idModelo) {
        if (this.lectura == null) {
            /* ... error ... */ }
        return lectura.getDetallesModelo(idModelo);
    }
}