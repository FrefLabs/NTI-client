package NTI;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.text.DecimalFormat;

/**
 * (NUEVA CLASE - BASADA EN TU CÓDIGO)
 * Encapsula todo el contenido de la pantalla "Inicio".
 */
public class PanelInicio extends JPanel {

    // --- Referencia a NTI y sus componentes ---
    private NTI nti; 

    // --- Componentes de la UI (Tus variables) ---
    private JPanel listaValores;
    private JLabel lblFallbackModelos;
    private JPanel panelDescripcion;
    private JPanel panelNoticias;
    
    /**
     * Constructor de PanelInicio.
     */
    public PanelInicio(NTI nti) {
        // 1. Guardar Referencia
        this.nti = nti;

        // 2. Configurar este Panel (Tu layout original)
        this.setLayout(new BorderLayout());
        this.setBackground(nti.fondo);

        // 3. Construir la UI (Tu código original)
        initUI();
    }

    /**
     * (TU CÓDIGO ORIGINAL) Construye la UI del panel de inicio.
     */
    private void initUI() {
        // ------------------------------ PANEL CENTRAL ------------------------------------
        JPanel centro = new JPanel(new BorderLayout(10, 10));
        centro.setPreferredSize(new Dimension(300, nti.getHeight()));
        centro.setBackground(nti.fondo);
        centro.setBorder(new EmptyBorder(30, 20, 30, 0));

        JPanel bar = new JPanel(new BorderLayout(10, 10));
        bar.setBorder(new RoundedBorder(20, nti.bordeDorado, 3));
        bar.setBackground(nti.fondoPanel);
        bar.setPreferredSize(new Dimension(300, 40));
        centro.add(bar, BorderLayout.NORTH);

        ChartPanel chartPanel = crearGraficoConSegmentos();
        chartPanel.setBackground(nti.fondoPanel);
        chartPanel.setBorder(new RoundedBorder(20, nti.bordeDorado, 3));
        centro.add(chartPanel, BorderLayout.CENTER);

        JPanel datos = crearPanelDatos();
        datos.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, nti.bordeDorado, 3),
                new EmptyBorder(10, 15, 10, 15)
        ));
        centro.add(datos, BorderLayout.SOUTH);
        this.add(centro, BorderLayout.CENTER);

        // ------------------------- PANEL DERECHO --------------------------------------
        JPanel derecha = new JPanel();
        derecha.setLayout(new BoxLayout(derecha, BoxLayout.Y_AXIS));
        derecha.setPreferredSize(new Dimension(450, nti.getHeight()));
        derecha.setBackground(nti.fondo);
        derecha.setBorder(new EmptyBorder(30, 20, 30, 20));

        // --- Recomendación
        JPanel recomendacion = new JPanel();
        recomendacion.setBackground(nti.fondoPanel);
        recomendacion.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, nti.bordeDorado, 3),
                new EmptyBorder(10, 10, 10, 10)
        ));
        recomendacion.setLayout(new BoxLayout(recomendacion, BoxLayout.Y_AXIS));
        recomendacion.setPreferredSize(new Dimension(450, 180));
        recomendacion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        recomendacion.setAlignmentX(Component.LEFT_ALIGNMENT);
        derecha.add(recomendacion);
        derecha.add(Box.createVerticalStrut(15));

        // --- Descripción de empresas
        panelDescripcion = new JPanel(); // (Asignado a variable de clase)
        panelDescripcion.setBackground(nti.fondoPanel);
        panelDescripcion.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, nti.bordeDorado, 3),
                new EmptyBorder(12, 15, 10, 10)
        ));
        panelDescripcion.setLayout(new BoxLayout(panelDescripcion, BoxLayout.Y_AXIS));
        panelDescripcion.setPreferredSize(new Dimension(450, 150));
        panelDescripcion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        panelDescripcion.setAlignmentX(Component.LEFT_ALIGNMENT);
        derecha.add(panelDescripcion);
        derecha.add(Box.createVerticalStrut(15));
        
        // --- Lista de Modelos mas precisos ---
        listaValores = new JPanel(); // (Asignado a variable de clase)
        listaValores.setLayout(new BoxLayout(listaValores, BoxLayout.Y_AXIS));
        listaValores.setBackground(nti.fondoPanel);
        // (Borde se aplica en el contenedor de abajo)

        JPanel panelTitulo = new JPanel(new BorderLayout());
        panelTitulo.setOpaque(false);
        panelTitulo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        Font fuenteRegular14 = Fuentes.getRegular(12f);
        String cssFuente = String.format("font-family:'%s'; font-size:%dpx; font-weight:normal;",
                                         fuenteRegular14.getFamily(),
                                         fuenteRegular14.getSize());
        JLabel lblTituloModelos = new JLabel(String.format(
            "<html><span style=\"%s color:%s;\">Modelos más </span><span style=\"%s color:%s;\">precisos</span></html>",
            cssFuente, nti.letraStr, cssFuente, nti.bordedoradoStr
        ));
        lblTituloModelos.setBorder(new EmptyBorder(0, 5, 10, 5));
        panelTitulo.add(lblTituloModelos, BorderLayout.WEST);
        listaValores.add(panelTitulo); // (Añade el título)

        lblFallbackModelos = new JLabel("Aún no hay modelos disponibles", SwingConstants.CENTER);
        lblFallbackModelos.setForeground(nti.letra.darker());
        lblFallbackModelos.setFont(Fuentes.getRegular(16f));
        lblFallbackModelos.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblFallbackModelos.setVisible(false);
        listaValores.add(lblFallbackModelos); // (Añade el fallback, se ocultará en la carga)
        
        JPanel contenedorLista = new JPanel(new BorderLayout());
        contenedorLista.setBackground(nti.fondoPanel);
        contenedorLista.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(20, nti.bordeDorado, 3),
            new EmptyBorder(10, 10, 10, 10)
        ));
        contenedorLista.setPreferredSize(new Dimension(450, 160));
        contenedorLista.setMaximumSize(new Dimension(Integer.MAX_VALUE, 245));
        contenedorLista.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenedorLista.add(listaValores, BorderLayout.CENTER);
        derecha.add(contenedorLista);
        derecha.add(Box.createVerticalStrut(15));

        // --- Noticias
        panelNoticias = new JPanel(); // (Asignado a variable de clase)
        panelNoticias.setLayout(new BoxLayout(panelNoticias, BoxLayout.Y_AXIS));
        panelNoticias.setBackground(nti.fondoPanel);
        panelNoticias.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, nti.bordeDorado, 3),
                new EmptyBorder(20, 5, 10, 15)
        ));
        panelNoticias.setAlignmentX(Component.LEFT_ALIGNMENT);
        derecha.add(panelNoticias);
        
        this.add(derecha, BorderLayout.EAST);
    }

    /**
     * (TU CÓDIGO ORIGINAL) Carga los datos de Empresas y Noticias.
     * (Ahora es 'cargarDatosIniciales' para diferenciarla de la carga de modelos)
     */
    public void cargarDatosIniciales() {
        // Carga síncrona, tal como en tu original
        Vector<Empresa> empresas = nti.emp.obtenerEmpresasDesdeBD();
        for (Empresa e : empresas) {
            JLabel label = new JLabel("<html>"
                    + "<span style='color:" + nti.bordedoradoStr + "; font-weight:bold;'>"
                    + e.getNombreEmpresa()
                    + "</span> "
                    + "<span style='color:" + nti.letraStr + ";'>"
                    + e.getDescripcion()
                    + "</span>"
                    + "</html>");
            label.setForeground(Color.WHITE);
            panelDescripcion.add(label);
            panelDescripcion.add(Box.createVerticalStrut(8));
        }

        Vector<Tupla> datnot = nti.not.getDatos();
        for (Tupla noticia : datnot) {
            JPanel panelNoticia = crearPanelNoticia(noticia); // (Llama al helper)
            panelNoticias.add(panelNoticia);
            panelNoticias.add(Box.createVerticalStrut(10));
        }
        
        // Carga los modelos (esto SÍ es en un hilo)
        cargarTopModelos();
    }

    /**
     * (TU MÉTODO ORIGINAL) Carga el Top 3 de modelos.
     */
    private void cargarTopModelos() {
        new Thread(() -> {
            Vector<Map<String, Object>> modelos = nti.modelo.obtenerTop3ParaVista();
            SwingUtilities.invokeLater(() -> {
                Component titulo = listaValores.getComponent(0); // Guarda el título
                listaValores.removeAll();
                listaValores.add(titulo); // Lo vuelve a añadir

                if (modelos == null || modelos.isEmpty()) {
                    lblFallbackModelos.setVisible(true);
                    lblFallbackModelos.setBorder(new EmptyBorder(0, 5, 0, 5));
                    // (ya está añadido, solo se hace visible)
                } else {
                    lblFallbackModelos.setVisible(false);
                    int rank = 1;
                    for (Map<String, Object> m : modelos) {
                        JPanel panelModelo = crearPanelUnModelo(
                                rank++,
                                (String) m.get("Simbolo"),
                                (String) m.get("E_Nombre"),
                                (Double) m.get("MAE"),
                                (Double) m.get("Precision")
                        );
                        listaValores.add(panelModelo);
                    }
                    listaValores.add(Box.createVerticalGlue());
                }
                listaValores.revalidate();
                listaValores.repaint();
            });
        }).start();
    }

    /**
     * (TU MÉTODO ORIGINAL) Helper para panel de 1 modelo.
     */
    private JPanel crearPanelUnModelo(int rank, String simbolo, String nombreEmpresa, double mae, double precision) {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 5, 0, 5));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel lblRank = new JLabel(rank + ". " + simbolo);
        lblRank.setForeground(nti.letra);
        lblRank.setFont(Fuentes.getBlack(14f));
        lblRank.setVerticalAlignment(SwingConstants.BOTTOM);
        panel.add(lblRank);

        panel.add(new JLabel("")); 

        JLabel lblNombre = new JLabel(nombreEmpresa);
        lblNombre.setForeground(nti.letra.darker());
        lblNombre.setFont(Fuentes.getRegular(12f));
        lblNombre.setVerticalAlignment(SwingConstants.TOP);
        panel.add(lblNombre);

        String maeTexto = (mae > 0) ? "± " + nti.maeFormat.format(mae) : nti.maeFormat.format(mae);
        String precisionTexto = nti.percentFormat.format(precision);
        JLabel lblStats = new JLabel(maeTexto + " - " + precisionTexto);
        lblStats.setForeground(nti.bordeDorado);
        lblStats.setFont(Fuentes.getBold(12f));
        lblStats.setHorizontalAlignment(SwingConstants.RIGHT);
        lblStats.setVerticalAlignment(SwingConstants.TOP);
        panel.add(lblStats);

        return panel;
    }

    /**
     * (TU MÉTODO ORIGINAL) Helper para panel de noticia.
     */
    private JPanel crearPanelNoticia(Tupla noticia) {
        JPanel panelNoticia = new JPanel(new BorderLayout());
        panelNoticia.setBackground(nti.fondoPanel);
        panelNoticia.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        JPanel textoPanel = new JPanel();
        textoPanel.setLayout(new BoxLayout(textoPanel, BoxLayout.Y_AXIS));
        textoPanel.setBackground(nti.fondoPanel);
        JTextArea title = new JTextArea(noticia.getTitulo());
        title.setWrapStyleWord(true);
        title.setLineWrap(true);
        title.setOpaque(false);
        title.setEditable(false);
        title.setFocusable(false);
        title.setForeground(Color.WHITE);
        title.setFont(Fuentes.getBlack(14f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setMaximumSize(new Dimension(450, Short.MAX_VALUE));
        JPanel fuentePanel = new JPanel(new BorderLayout());
        fuentePanel.setBackground(nti.fondoPanel);
        JLabel fuenteLabel = new JLabel(noticia.getFuente());
        fuenteLabel.setForeground(nti.bordeDorado);
        fuenteLabel.setFont(Fuentes.getBlack(12f));
        fuenteLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fuentePanel.add(fuenteLabel, BorderLayout.EAST);
        textoPanel.add(title);
        textoPanel.add(Box.createVerticalStrut(3));
        textoPanel.add(fuentePanel);
        panelNoticia.add(textoPanel, BorderLayout.CENTER);
        return panelNoticia;
    }

    /**
     * (TU MÉTODO ORIGINAL) Helper para crear el gráfico.
     */
    private ChartPanel crearGraficoConSegmentos() {
        double[] precios = {20, 30, 58, 45, 40, 50, 65, 39, 49, 28, 57};
        XYSeriesCollection dataset = new XYSeriesCollection();
        for (int i = 0; i < precios.length - 1; i++) {
            XYSeries segmento = new XYSeries("Segmento " + i);
            segmento.add(i, precios[i]);
            segmento.add(i + 1, precios[i + 1]);
            dataset.addSeries(segmento);
        }
        JFreeChart chart = ChartFactory.createXYLineChart(
                "", "Tiempo", "Precio", dataset,
                PlotOrientation.VERTICAL, false, false, false
        );
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(nti.fondoPanel);
        plot.setDomainGridlinePaint(new Color(60, 60, 60));
        plot.setRangeGridlinePaint(new Color(60, 60, 60));
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setBaseShapesVisible(false);
        for (int i = 0; i < precios.length - 1; i++) {
            if (precios[i + 1] > precios[i]) {
                renderer.setSeriesPaint(i, new Color(0, 255, 0));
            } else {
                renderer.setSeriesPaint(i, new Color(255, 0, 0));
            }
        }
        plot.setRenderer(renderer);
        plot.getDomainAxis().setTickLabelPaint(nti.letra);
        plot.getRangeAxis().setTickLabelPaint(nti.letra);
        plot.getDomainAxis().setLabelPaint(nti.letra);
        plot.getRangeAxis().setLabelPaint(nti.letra);
        chart.setBackgroundPaint(nti.fondoPanel);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(nti.fondoPanel);
        chartPanel.setOpaque(true);
        chartPanel.setBorder(BorderFactory.createEmptyBorder());
        return chartPanel;
    }

    /**
     * (TU MÉTODO ORIGINAL) Helper para leer datos del archivo.
     */
    private Map<String, String> leerDatosDesdeArchivo() {
        Map<String, String> datos = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(nti.ARCHIVO_DATOS))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split("=", 2);
                if (partes.length == 2) {
                    String clave = partes[0].trim().replace("=", "");
                    String valor = partes[1].trim();
                    datos.put(clave, valor);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo de datos: " + e.getMessage());
            e.printStackTrace();
        }
        return datos;
    }

    /**
     * (TU MÉTODO ORIGINAL) Helper para crear el panel de datos.
     */
    private JPanel crearPanelDatos() {
        JPanel datosPanel = new JPanel();
        datosPanel.setBorder(new RoundedBorder(20, nti.bordeDorado, 3));
        datosPanel.setBackground(nti.fondoPanel);
        datosPanel.setPreferredSize(new Dimension(600, 180));
        datosPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        datosPanel.setLayout(new GridLayout(8, 2, 15, 5));
        Map<String, String> datos = leerDatosDesdeArchivo();
        for (Map.Entry<String, String> entry : datos.entrySet()) {
            JPanel panelDato = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            panelDato.setBackground(Color.decode("#060521"));
            JLabel labelClave = new JLabel(entry.getKey() + ":");
            labelClave.setForeground(Color.WHITE);
            labelClave.setFont(new Font("Arial", Font.PLAIN, 12));
            JLabel labelValor = new JLabel(entry.getValue());
            labelValor.setForeground(Color.decode("#D4AF37"));
            labelValor.setFont(new Font("Arial", Font.BOLD, 12));
            panelDato.add(labelClave);
            panelDato.add(Box.createRigidArea(new Dimension(5, 0)));
            panelDato.add(labelValor);
            datosPanel.add(panelDato);
        }
        return datosPanel;
    }
}