package juego;

import NTI.Formato; // (NUEVO) Importa Formato desde el paquete principal
import java.util.Random;

public class Juego {
    private String nombreJugador;
    private String modoJuego; // "Endless" o "Normal"

    private int puntosJugador;
    private int puntosIA;
    private int rondaActual;

    private String fecha;
    private double openHoy; 
    private double cierreAyer;
    private double altoAyer;
    private double bajoAyer;
    private long volumenAyer;
    
    private double cierreHoy; // RESPUESTA SECRETA
    private double cierreManana; // PISTA ADICIONAL

    public boolean ingresarNombre(String nombre) {
        // (MODIFICADO) Llama a Formato (asume que está en NTI)
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

        // 1. Genera el Día 1 (Ayer)
        this.cierreAyer = 100 + r.nextDouble() * 100; // Un valor entre 100 y 200
        this.altoAyer = this.cierreAyer + (r.nextDouble() * 5); // Un poco más alto
        this.bajoAyer = this.cierreAyer - (r.nextDouble() * 5); // Un poco más bajo
        this.volumenAyer = 1_000_000 + r.nextInt(10_000_000); 

        // 2. Genera el Día 2 (Hoy) - Esta es la respuesta
        this.openHoy = this.cierreAyer + (r.nextDouble() * 4 - 2); // +/- $2
        this.cierreHoy = this.openHoy + (r.nextDouble() * 10 - 5); // +/- $5 respecto al open

        // 3. Genera el Día 3 (Mañana) - Esta es la pista
        this.cierreManana = this.cierreHoy + (r.nextDouble() * 10 - 5); // +/- $5

        this.fecha = String.format("2025-10-%02d", (1 + r.nextInt(28))); // Fecha del Día 2 (Hoy)
    }

    /**
     * Calcula el resultado de la ronda TIRA Y AFLOJE.
     * Lógica: Ganas el error de la IA, o pierdes tu propio error.
     */
    public String calcularResultadoRonda(int errorJugador, int errorIA, double prediccionJugador) {
        
        // 1. Revisar acierto de dirección
        boolean aciertoDireccion = (prediccionJugador > this.cierreAyer && this.cierreHoy > this.cierreAyer) ||
                                  (prediccionJugador < this.cierreAyer && this.cierreHoy < this.cierreAyer) ||
                                  (prediccionJugador == this.cierreAyer && this.cierreHoy == this.cierreAyer);

        int puntosBonus = 0;
        if (aciertoDireccion) {
            puntosBonus = 10;
            this.puntosJugador += puntosBonus;
            this.puntosIA -= puntosBonus;
        }

        // 2. Transferencia de puntos
        int puntosTransferidos = 0;
        String ganadorRonda;

        if (errorJugador < errorIA) {
            // Gana Jugador
            puntosTransferidos = errorIA; // Gana el error GRANDE de la IA
            this.puntosJugador += puntosTransferidos;
            this.puntosIA -= puntosTransferidos;
            ganadorRonda = this.nombreJugador;

        } else if (errorIA < errorJugador) {
            // Gana IA
            puntosTransferidos = errorJugador; // Pierde su propio error GRANDE
            this.puntosIA += puntosTransferidos;
            this.puntosJugador -= puntosTransferidos;
            ganadorRonda = "IA";
        
        } else {
            // Empate
            ganadorRonda = "Empate";
        }
        
        if (this.puntosJugador < 0) this.puntosJugador = 0;
        if (this.puntosIA < 0) this.puntosIA = 0;

        // 4. Crear el resumen
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
    
    /**
     * Calcula el resultado de la ronda para el modo ENDLESS.
     * Lógica: Ganas el error de la IA, o pierdes tu propio error.
     */
    public String calcularResultadoEndless(int errorJugador, int errorIA, double prediccionJugador) {
        
        // 1. Revisar acierto de dirección
        boolean aciertoDireccion = (prediccionJugador > this.cierreAyer && this.cierreHoy > this.cierreAyer) ||
                                  (prediccionJugador < this.cierreAyer && this.cierreHoy < this.cierreAyer) ||
                                  (prediccionJugador == this.cierreAyer && this.cierreHoy == this.cierreAyer);

        int gananciaBonus = 0;
        if (aciertoDireccion) {
            gananciaBonus = 10;
            this.puntosJugador += gananciaBonus; // Suma el bonus
        }

        // 2. Transferencia de puntos
        int puntosTransferidos = 0;
        String ganadorRonda;

        if (errorJugador < errorIA) {
            // Gana Jugador
            puntosTransferidos = errorIA; // Gana el error GRANDE de la IA
            this.puntosJugador += puntosTransferidos;
            ganadorRonda = this.nombreJugador;

        } else if (errorIA < errorJugador) {
            // "Gana" IA (Jugador pierde)
            puntosTransferidos = errorJugador; // Pierde su propio error GRANDE
            this.puntosJugador -= puntosTransferidos;
            ganadorRonda = "IA";
        
        } else {
            // Empate
            ganadorRonda = "Empate";
        }
        
        if (this.puntosJugador < 0) this.puntosJugador = 0;

        // 4. Crear el resumen
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

    // --- Getters ---
    public String getNombreJugador() { return nombreJugador; }
    public String getModoJuego() { return modoJuego; }
    public int getPuntosJugador() { return puntosJugador; }
    public int getPuntosIA() { return puntosIA; }
    public int getRondaActual() { return rondaActual; }
    public String getFecha() { return fecha; }
    public double getOpenHoy() { return openHoy; }
    public double getCierreAyer() { return cierreAyer; }
    public double getAltoAyer() { return altoAyer; }
    public double getBajoAyer() { return bajoAyer; }
    public long getVolumenAyer() { return volumenAyer; }
    public double getCierreHoy() { return cierreHoy; } // La respuesta
    public double getCierreManana() { return cierreManana; } // La pista
}