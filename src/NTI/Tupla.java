package NTI;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Tupla {
    private String titulo;
    private String fuente;
    private String url;
    private String fecha;

    public Tupla(String titulo, String fuente, String url, String fecha) {
        this.titulo = titulo;
        this.fuente = fuente;
        this.url = url;
        this.fecha = formatearFecha(fecha);
    }

    private String formatearFecha(String fechaOriginal) {
        if (fechaOriginal == null || fechaOriginal.isEmpty()) return "0000-00-00";

        if (fechaOriginal.contains("T")) {
            fechaOriginal = fechaOriginal.substring(0, 10);
        }

        try {
            SimpleDateFormat entrada = new SimpleDateFormat("yyyy-MM-dd");
            Date date = entrada.parse(fechaOriginal);
            SimpleDateFormat salida = new SimpleDateFormat("yyyy-MM-dd");
            return salida.format(date);
        } catch (ParseException e) {
            return "0000-00-00";
        }
    }

    public String getTitulo() { return titulo; }
    public String getFuente() { return fuente; }
    public String getUrl() { return url; }
    public String getFecha() { return fecha; }

    @Override
    public String toString() {
        return "Título: " + titulo + "\nFuente: " + fuente + "\nURL: " + url + "\nFecha: " + fecha;
    }
}