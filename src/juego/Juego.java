package juego;

import NTI.Formato; 
import java.util.Random;

public class Juego {
    private String nombreJugador;
    private String modoJuego; // "Endless" o "Tira Y Afloje"

    private int puntosJugador;
    private int puntosIA;
    private int rondaActual;

    private String fecha;
    private double openHoy; 
    private double cierreAyer;
    private double altoAyer;
    private double bajoAyer;
    private long volumenAyer;
    
    private double cierreHoy;
    private double cierreManana; 

    public boolean ingresarNombre(String nombre) {
        if (Formato.validarNombre(nombre)) {
            this.nombreJugador = nombre;
            return true;
        }
        return false;
    }

    public void ingresarModo(String modo) {
        this.modoJuego = modo;
    }

    public void setPuntos() {
        this.puntosJugador = 1000;
        this.puntosIA = 1000;
        this.rondaActual = 0; 
    }

    public void generarNuevaRonda() {
        Random r = new Random();
        this.rondaActual++; 

        this.cierreAyer = 100 + r.nextDouble() * 100;
        this.altoAyer = this.cierreAyer + (r.nextDouble() * 5); 
        this.bajoAyer = this.cierreAyer - (r.nextDouble() * 5); 
        this.volumenAyer = 1_000_000 + r.nextInt(10_000_000); 
        this.openHoy = this.cierreAyer + (r.nextDouble() * 4 - 2); 
        this.cierreHoy = this.openHoy + (r.nextDouble() * 10 - 5); 
        this.cierreManana = this.cierreHoy + (r.nextDouble() * 10 - 5); 
        this.fecha = String.format("2025-10-%02d", (1 + r.nextInt(28))); 
    }

    public String calcularResultadoRonda(int errorJugador, int errorIA, double prediccionJugador) {
        boolean aciertoDireccion = (prediccionJugador > this.cierreAyer && this.cierreHoy > this.cierreAyer) ||
                                  (prediccionJugador < this.cierreAyer && this.cierreHoy < this.cierreAyer) ||
                                  (prediccionJugador == this.cierreAyer && this.cierreHoy == this.cierreAyer);
        int puntosBonus = 0;
        if (aciertoDireccion) {
            puntosBonus = 10;
            this.puntosJugador += puntosBonus;
            this.puntosIA -= puntosBonus;
        }
        int puntosTransferidos = 0;
        String ganadorRonda;
        if (errorJugador < errorIA) {
            puntosTransferidos = errorIA; 
            this.puntosJugador += puntosTransferidos;
            this.puntosIA -= puntosTransferidos;
            ganadorRonda = this.nombreJugador;
        } else if (errorIA < errorJugador) {
            puntosTransferidos = errorJugador; 
            this.puntosIA += puntosTransferidos;
            this.puntosJugador -= puntosTransferidos;
            ganadorRonda = "IA";
        } else {
            ganadorRonda = "Empate";
        }
        if (this.puntosJugador < 0) this.puntosJugador = 0;
        if (this.puntosIA < 0) this.puntosIA = 0;
        StringBuilder resumen = new StringBuilder();
        resumen.append(String.format("Ganador de la ronda: %s\n", ganadorRonda));
        if (ganadorRonda.equals(this.nombreJugador)) {
            resumen.append(String.format("¡Ganas %d puntos (el error de la IA)!\n", puntosTransferidos));
        } else if (ganadorRonda.equals("IA")) {
            resumen.append(String.format("Pierdes %d puntos (tu error).\n", puntosTransferidos));
        }
        if (aciertoDireccion) {
            resumen.append(String.format("¡Bonus por dirección! +%d puntos", puntosBonus));
        }
        return resumen.toString();
    }
    
    public String calcularResultadoEndless(int errorJugador, int errorIA, double prediccionJugador) {
        boolean aciertoDireccion = (prediccionJugador > this.cierreAyer && this.cierreHoy > this.cierreAyer) ||
                                  (prediccionJugador < this.cierreAyer && this.cierreHoy < this.cierreAyer) ||
                                  (prediccionJugador == this.cierreAyer && this.cierreHoy == this.cierreAyer);
        int gananciaBonus = 0;
        if (aciertoDireccion) {
            gananciaBonus = 10;
            this.puntosJugador += gananciaBonus; 
        }
        int puntosTransferidos = 0;
        String ganadorRonda;
        if (errorJugador < errorIA) {
            puntosTransferidos = errorIA; 
            this.puntosJugador += puntosTransferidos;
            ganadorRonda = this.nombreJugador;
        } else if (errorIA < errorJugador) {
            puntosTransferidos = errorJugador; 
            this.puntosJugador -= puntosTransferidos;
            ganadorRonda = "IA";
        } else {
            ganadorRonda = "Empate";
        }
        if (this.puntosJugador < 0) this.puntosJugador = 0;
        StringBuilder resumen = new StringBuilder();
        resumen.append(String.format("Ganador de la ronda: %s\n", ganadorRonda));
        if (ganadorRonda.equals(this.nombreJugador)) {
            resumen.append(String.format("¡Sumas %d puntos (el error de la IA)!\n", puntosTransferidos));
        } else if (ganadorRonda.equals("IA")) {
            resumen.append(String.format("Pierdes %d puntos (tu error).\n", puntosTransferidos));
        }
        if (aciertoDireccion) {
            resumen.append(String.format("¡Bonus por dirección! +%d puntos", gananciaBonus));
        }
        return resumen.toString();
    }
    
    public boolean isJuegoTerminado() {
        return this.puntosJugador <= 0 || this.puntosIA <= 0;
    }
    
    public String getGanadorFinal() {
        if (this.puntosJugador <= 0) return "IA";
        if (this.puntosIA <= 0) return this.nombreJugador;
        return "Nadie";
    }

    // --- Getters (usados para guardar la partida) ---
    public String getNombreJugador() { return nombreJugador; }
    public String getModoJuego() { return modoJuego; }
    public int getPuntosJugador() { return puntosJugador; }
    public int getRondaActual() { return rondaActual; }
    
    // (Getters no usados para guardar)
    public int getPuntosIA() { return puntosIA; }
    public String getFecha() { return fecha; }
    public double getOpenHoy() { return openHoy; }
    public double getCierreAyer() { return cierreAyer; }
    public double getAltoAyer() { return altoAyer; }
    public double getBajoAyer() { return bajoAyer; }
    public long getVolumenAyer() { return volumenAyer; }
    public double getCierreHoy() { return cierreHoy; }
    public double getCierreManana() { return cierreManana; }
}