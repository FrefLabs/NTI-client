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
        if (this.lectura == null) {
            /* ... error ... */ }

        // El panel de inicio (panelInicio) llama con LÍMITE 3
        return lectura.getModelosRecomendados(this.IDModeloSeleccionado, 3);
    }
    
    public Vector<Map<String, Object>> obtenerRecomendados() {
        if (this.lectura == null) {
            /* ... error ... */ }

        // El panel de modelos (panelModelos) llama con LÍMITE 10
        return lectura.getModelosRecomendados(this.IDModeloSeleccionado, 10);
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
    
    public Vector<Map<String, Object>> obtenerHistorialModelo() {
        if (this.lectura == null) { 
            System.err.println("Lectura no inicializada en Modelo");
            return new Vector<>(); 
        }
        return lectura.getHistorialModelo(this.IDModeloSeleccionado);
    }
    
    public Vector<Map<String, Object>> buscarModelos(String busqueda) {
        if (this.lectura == null) { 
            System.err.println("Lectura no inicializada en Modelo");
            return new Vector<>(); 
        }
        return lectura.buscarModelosEnBD(busqueda);
    }
}
