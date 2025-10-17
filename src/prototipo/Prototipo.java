package prototipo;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.sql.*;

public class Prototipo extends JFrame {

    // Archivo de datos
    private static final String ARCHIVO_DATOS = "datos.txt";
    Color bordeDorado = Color.decode("#D4AF37");
    Color fondo = Color.decode("#030614");
    Color fondoPanel = Color.decode("#060521");
    Color letra = Color.white;
    
    String bordedoradoStr = "rgb(" + bordeDorado.getRed() + "," + bordeDorado.getGreen() + "," + bordeDorado.getBlue() + ")";
    String letraStr = "rgb(" + letra.getRed() + "," + letra.getGreen() + "," + letra.getBlue() + ")";

    
    Empresa emp = new Empresa();
    Noticia not = new Noticia();

    public Prototipo() {
        setTitle("NeuroFref Trading Intelligence - 1.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1298, 763);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(fondo); // fondo general

        // -------------------------------    
        // SIDEBAR - TODAS LAS PAGINAS 
        // -------------------------------  
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(200, getHeight()));
        sidebar.setLayout(new BorderLayout());
        sidebar.setBackground(new Color(10, 10, 10));

        // ------------- Panel Título) ---------------------------
        JPanel panelSuperior = new JPanel();
        panelSuperior.setPreferredSize(new Dimension(200, 100));
        panelSuperior.setBackground(new Color(10, 10, 10));
        panelSuperior.setBorder(new EmptyBorder(20, 0, 0, 0)); // margen superior

        // Cargar imagen original
        ImageIcon logoIconOriginal = new ImageIcon(getClass().getResource("/Images/Logo.png"));

        // Escalar imagen a un tamaño más pequeño (excede el tamaño)
        ImageIcon logoIconEscalado = escalarImagen(logoIconOriginal, 160, 90); // ajustable (parametro)

        // Crear JLabel con la imagen escalada
        JLabel logoLabel = new JLabel(logoIconEscalado);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoLabel.setVerticalAlignment(SwingConstants.CENTER);


        // JLabel imagen  

        panelSuperior.setLayout(new BorderLayout());
        panelSuperior.add(logoLabel, BorderLayout.CENTER);

        // ----------------------- Panel Central (Botones principales)--------------------
        JPanel panelCentral = new JPanel();
        panelCentral.setBackground(new Color(10, 10, 10));
        panelCentral.setLayout(new BoxLayout(panelCentral, BoxLayout.Y_AXIS));
        panelCentral.setBorder(new EmptyBorder(5, 20, 20, 20));

        JButton btnInicio = crearBoton("Inicio");
        JButton btnModelos = crearBoton("Modelos");
        JButton btnHistorial = crearBoton("Historial");
        JButton btnJuego = crearBoton("Juego");

        JButton[] botones = {btnInicio, btnModelos, btnHistorial, btnJuego};

        // Agregar botones con separación de 10px
        for (int i = 0; i < botones.length; i++) {
            panelCentral.add(botones[i]);
            if (i < botones.length - 1) {
                panelCentral.add(Box.createRigidArea(new Dimension(0, 20)));
            }
        }

        // ----- Panel Inferior (Botón Ajustes) -----
        JPanel panelInferior = new JPanel();
        panelInferior.setBackground(new Color(10, 10, 10));
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));
        panelInferior.setBorder(new EmptyBorder(10, 20, 20, 20));

        JButton btnAjustes = crearBoton("Ajustes");
        panelInferior.add(btnAjustes);

        // Añadir secciones al sidebar
        sidebar.add(panelSuperior, BorderLayout.NORTH);
        sidebar.add(panelCentral, BorderLayout.CENTER);
        sidebar.add(panelInferior, BorderLayout.SOUTH);

        add(sidebar, BorderLayout.WEST);

        //--------------------------------
        //PANELES PARA LA PAGINA DE INICIO
        //--------------------------------
        
        // ------------------------------ PANEL CENTRAL ------------------------------------
        JPanel centro = new JPanel(new BorderLayout(10, 10));
        centro.setPreferredSize(new Dimension(300, getHeight()));
        centro.setBackground(fondo);
        centro.setBorder(new EmptyBorder(30, 20, 30, 0));

        // Barra superior en centro
        JPanel bar = new JPanel(new BorderLayout(10, 10));
        bar.setBorder(new RoundedBorder(20, bordeDorado, 3));
        bar.setBackground(fondo);
        bar.setPreferredSize(new Dimension(300, 40));
        centro.add(bar, BorderLayout.NORTH);

        // Gráfico dinámico de valores de accion IMPORTATNE
        ChartPanel chartPanel = crearGraficoConSegmentos();
        chartPanel.setBorder(new RoundedBorder(20, bordeDorado, 3));
        centro.add(chartPanel, BorderLayout.CENTER);

        // Datos en parte inferior
        JPanel datos = crearPanelDatos();
        datos.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(20, bordeDorado, 3),
            new EmptyBorder(10, 15, 10, 15)
        ));
        centro.add(datos, BorderLayout.SOUTH);

        add(centro, BorderLayout.CENTER);

        //  ------------------------- PANEL DERECHO --------------------------------------
        JPanel derecha = new JPanel();
        derecha.setLayout(new BoxLayout(derecha, BoxLayout.Y_AXIS));
        derecha.setPreferredSize(new Dimension(450, getHeight()));
        derecha.setBackground(fondo);
        derecha.setBorder(new EmptyBorder(30, 20, 30, 20));

        // --- Recomendacion - Panel de Valores IMPORTANTE
        JPanel recomendacion = new JPanel();
        recomendacion.setBackground(fondoPanel);
        recomendacion.setBorder(new RoundedBorder(20, bordeDorado, 3));
        recomendacion.setPreferredSize(new Dimension(450, 180));
        recomendacion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        recomendacion.setAlignmentX(Component.LEFT_ALIGNMENT);
        derecha.add(recomendacion);
        derecha.add(Box.createVerticalStrut(20));

        JPanel descripcion = new JPanel();
        descripcion.setBackground(fondoPanel);
        descripcion.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, bordeDorado, 3),
                new EmptyBorder(10, 10, 10, 10)
        ));
        descripcion.setLayout(new BoxLayout(descripcion, BoxLayout.Y_AXIS));
        descripcion.setPreferredSize(new Dimension(450, 130));
        descripcion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        descripcion.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Obtencion de datos de Empresa
        Vector<Empresa> empresas = emp.obtenerEmpresasDesdeBD();

        for (Empresa e : empresas) {
            // Creamos un JLabel con HTML para diferenciar colores
            JLabel label = new JLabel("<html>"
            + "<span style='color:" + bordedoradoStr + "; font-weight:bold;'>"
            + e.getNombreEmpresa()
            + "</span> "
            + "<span style='color:" + letraStr + ";'>"
            + e.getDescripcion()
            + "</span>"
            + "</html>");

            label.setForeground(Color.WHITE);
            descripcion.add(label);
            descripcion.add(Box.createVerticalStrut(8));
        }

        derecha.add(descripcion);
        derecha.add(Box.createVerticalStrut(20));

        // --- ListaValores
        JPanel listaValores = new JPanel();
        listaValores.setBackground(fondoPanel);
        listaValores.setBorder(new RoundedBorder(20, bordeDorado, 3));
        listaValores.setPreferredSize(new Dimension(450, 260));
        listaValores.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        listaValores.setAlignmentX(Component.LEFT_ALIGNMENT);
        derecha.add(listaValores);
        derecha.add(Box.createVerticalStrut(20));

        // --- Noticias
        JPanel noticias = new JPanel();
        noticias.setLayout(new BoxLayout(noticias, BoxLayout.Y_AXIS));
        noticias.setBackground(fondoPanel);

        // Borde dorado con margen interno de 10px
        noticias.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, bordeDorado, 3), // Borde externo redondeado
                new EmptyBorder(10, 10, 10, 10) // Margen interno: top, left, bottom, right
        ));

        noticias.setPreferredSize(new Dimension(450, 170));
        noticias.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        noticias.setAlignmentX(Component.LEFT_ALIGNMENT);
        derecha.add(noticias);

        // Obtener noticias desde el archivo usando tu función
        Vector<String[]> datnot = not.obtenerDatosNoticias();

        // Recorrer las noticias y mostrarlas
        for (int i = 0; i < datnot.size(); i++) {
            String[] noticia = datnot.get(i);
            if (noticia.length < 3) {
                continue; // Ignorar líneas mal formateadas
            }
            // Panel individual para cada noticia con layout horizontal
            JPanel panelNoticia = new JPanel();
            panelNoticia.setLayout(new BoxLayout(panelNoticia, BoxLayout.X_AXIS));
            panelNoticia.setBackground(fondoPanel);
            panelNoticia.setBorder(new EmptyBorder(5, 10, 5, 10)); // márgenes

            // Seleccionar imagen según la noticia
            String rutaImagen = (i == 0) ? "/Images/Coca.png" : "/Images/CostaCofee.png";
            ImageIcon icono = new ImageIcon(getClass().getResource(rutaImagen));
            JLabel imagenLabel = new JLabel(icono);
            imagenLabel.setBorder(new EmptyBorder(0, 0, 0, 10)); // margen entre imagen y texto

            // Panel para texto (vertical)
            JPanel textoPanel = new JPanel();
            textoPanel.setLayout(new BoxLayout(textoPanel, BoxLayout.Y_AXIS));
            textoPanel.setBackground(fondoPanel);

            JLabel title = new JLabel(noticia[1]);
            title.setForeground(Color.WHITE);
            title.setFont(Fuentes.getBlack(14f));
            //! CAMBIAR FONT, NO ESTA BIEN OCNFIG, CAMBIAR A TEXT AREa.
            
            JLabel fuente = new JLabel("📎 " + noticia[0]);
            fuente.setForeground(bordeDorado); // amarillo

            JLabel link = new JLabel(noticia[2]);
            link.setForeground(Color.CYAN); // opcional

            textoPanel.add(title);
            textoPanel.add(Box.createVerticalStrut(5));
            textoPanel.add(fuente);
            // textoPanel.add(link); // opcional

            // Agregar imagen y texto al panel de noticia
            panelNoticia.add(imagenLabel);
            panelNoticia.add(textoPanel);

            // Agregar el panel de noticia al contenedor principal
            noticias.add(panelNoticia);

            // Espacio entre noticias
            noticias.add(Box.createVerticalStrut(10));
        }

        add(derecha, BorderLayout.EAST);
        setVisible(true);
        
// ---------------------------------------------------
// PANELES PARA LA PAGINA MODULO
// ---------------------------------------------------
        
        // Panel principal que ocupa el espacio del centro y derecha
        JPanel panelModelos = new JPanel();
        panelModelos.setLayout(new BorderLayout(20, 20));
        panelModelos.setBackground(fondo);
        panelModelos.setBorder(new EmptyBorder(30, 20, 30, 20));
        panelModelos.setVisible(false); // oculto por defecto
        add(panelModelos, BorderLayout.CENTER); // ocupa el mismo lugar que los otros

        //--------------------------------
        // PANEL SUPERIOR - BARRA DE BUSQUEDA
        //--------------------------------
        JPanel barraSuperior = new JPanel(new BorderLayout(10, 10));
        barraSuperior.setBackground(fondoPanel);
        barraSuperior.setBorder(new RoundedBorder(20, bordeDorado, 3));
        barraSuperior.setPreferredSize(new Dimension(300, 45));

        // Campo de texto (aún sin funcionalidad)
        JTextField txtBuscarModelo = new JTextField("Buscar modelo...");
        txtBuscarModelo.setBackground(Color.decode("#060521"));
        txtBuscarModelo.setForeground(Color.LIGHT_GRAY);
        txtBuscarModelo.setCaretColor(Color.WHITE);
        txtBuscarModelo.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        barraSuperior.add(txtBuscarModelo, BorderLayout.CENTER);

        panelModelos.add(barraSuperior, BorderLayout.NORTH);

        //--------------------------------
        // PANEL CENTRAL CON MODELOS INSTALADOS
        //--------------------------------
        JPanel contenidoModelos = new JPanel();
        contenidoModelos.setLayout(new BoxLayout(contenidoModelos, BoxLayout.Y_AXIS));
        contenidoModelos.setBackground(fondo);
        contenidoModelos.setBorder(new EmptyBorder(10, 10, 10, 10));

        // TÍTULO “MODELOS INSTALADOS”
        JLabel lblTituloModelos = new JLabel("<html><span style='color:" + letraStr +";'>Modelos</span> <span style='color:" + bordedoradoStr + ";'>instalados</span></html>");
        lblTituloModelos.setFont(Fuentes.getBlack(24f));
        lblTituloModelos.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTituloModelos.setBorder(new EmptyBorder(0, 5, 15, 0));
        contenidoModelos.add(lblTituloModelos);

        // PANEL INDIVIDUAL - MODELO KO
        JPanel panelKO = new JPanel();
        panelKO.setBackground(fondoPanel);
        panelKO.setLayout(new BorderLayout(10, 10));
        panelKO.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, bordeDorado, 3),
                new EmptyBorder(15, 15, 15, 15)
        ));
        panelKO.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120)); // un poco más alto
        panelKO.setAlignmentX(Component.LEFT_ALIGNMENT);

        // PANEL SUPERIOR DEL MODELO KO
        JPanel panelSuperiorKO = new JPanel(new BorderLayout(10, 0));
        panelSuperiorKO.setOpaque(false);

        // Sub-panel para nombre y último entrenamiento
        JPanel panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        panelInfo.setOpaque(false);

        JLabel lblNombreModelo = new JLabel("KO");
        lblNombreModelo.setFont(Fuentes.getBlack(22f));
        lblNombreModelo.setForeground(Color.WHITE);
        panelInfo.add(lblNombreModelo);

        JLabel lblUltimoEntrenamiento = new JLabel("Último entrenamiento: Hoy");
        lblUltimoEntrenamiento.setFont(Fuentes.getRegular(14f));
        lblUltimoEntrenamiento.setForeground(Color.LIGHT_GRAY);
        panelInfo.add(lblUltimoEntrenamiento);

        panelSuperiorKO.add(panelInfo, BorderLayout.WEST);

// Botón "Seleccionado"
        JButton btnSeleccionado = new JButton("        Seleccionado        ");
        btnSeleccionado.setBackground(Color.decode("#1E2A5A"));
        btnSeleccionado.setForeground(letra);
        btnSeleccionado.setFont(Fuentes.getBold(14f));
        btnSeleccionado.setBorder(new RoundedBorder(15, letra, 1));
        btnSeleccionado.setFocusPainted(false);
        panelSuperiorKO.add(btnSeleccionado, BorderLayout.EAST);

        panelKO.add(panelSuperiorKO, BorderLayout.NORTH);

// PANEL INFERIOR DEL MODELO KO (Precisión + Descripción)
        JPanel panelInferiorKO = new JPanel();
        panelInferiorKO.setLayout(new BoxLayout(panelInferiorKO, BoxLayout.Y_AXIS));
        panelInferiorKO.setOpaque(false);
        panelInferiorKO.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel lblPrecision = new JLabel("<html><span style='color:" + letra + ";'>Precision: </span> <span style='color:rgb(0,255,0);'>92.26%</span></html>");
        lblPrecision.setFont(Fuentes.getBold(16f));
        panelInferiorKO.add(lblPrecision);

        panelKO.add(panelInferiorKO, BorderLayout.CENTER);

// Agregar el panel KO al contenido
        contenidoModelos.add(panelKO);

// Agregar el contenido al panel principal
        panelModelos.add(contenidoModelos, BorderLayout.CENTER);
        
// ------------------------------
// Funcionalidades de Botones
// ------------------------------        

        btnInicio.addActionListener(e -> {
            centro.setVisible(true);
            derecha.setVisible(true);
            panelModelos.setVisible(false);
        });

        btnModelos.addActionListener(e -> {
            sidebar.setVisible(true);
            centro.setVisible(false);
            derecha.setVisible(false);
            panelModelos.setVisible(true);
        });
        
        btnHistorial.addActionListener(e -> {
            centro.setVisible(false);
            derecha.setVisible(false);
            panelModelos.setVisible(false);
        });

        btnJuego.addActionListener(e -> {
            centro.setVisible(false);
            derecha.setVisible(false);
            panelModelos.setVisible(false);
        });

        btnAjustes.addActionListener(e -> {
            centro.setVisible(false);
            derecha.setVisible(false);
            panelModelos.setVisible(false);
        });
    }

    // Método para crear botones uniformes de 200 x 82
    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setPreferredSize(new Dimension(180, 45)); // Tamaño fijo
        boton.setMaximumSize(new Dimension(180, 45));   // Evita que se estire
        boton.setMinimumSize(new Dimension(180, 45));   // Evita que se encoja
        boton.setBackground(new Color(20, 20, 20));
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setFont(new Font("Arial", Font.PLAIN, 16));
        return boton;
    }

    /**
     * Crea un gráfico donde cada segmento de la línea
     * cambia de color según si sube o baja.
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
                "",
                "Tiempo",
                "Precio",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(fondo);
        plot.setDomainGridlinePaint(new Color(60, 60, 60));
        plot.setRangeGridlinePaint(new Color(60, 60, 60));

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setBaseShapesVisible(false);

        // Color dinámico por segmento
        for (int i = 0; i < precios.length - 1; i++) {
            if (precios[i + 1] > precios[i]) {
                renderer.setSeriesPaint(i, new Color(0, 255, 0)); // verde claro
            } else {
                renderer.setSeriesPaint(i, new Color(255, 0, 0)); // rojo
            }
        }

        plot.setRenderer(renderer);

        // Estilo de ejes
        plot.getDomainAxis().setTickLabelPaint(letra);
        plot.getRangeAxis().setTickLabelPaint(letra);
        plot.getDomainAxis().setLabelPaint(letra);
        plot.getRangeAxis().setLabelPaint(letra);

        chart.setBackgroundPaint(Color.decode("#030614"));

        ChartPanel chartPanel = new ChartPanel(chart);

        // Fondo opaco y del mismo color que el resto de la UI
        chartPanel.setBackground(fondo);
        chartPanel.setOpaque(true);

        // Eliminar bordes internos que puedan interferir
        chartPanel.setBorder(BorderFactory.createEmptyBorder());

        return chartPanel;

    }

    private Font cargarFuente(String ruta, float tamaño) {
        try {
            // Cargar la fuente desde el archivo .ttf dentro de src/fonts
            Font font = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream(ruta));
            return font.deriveFont(tamaño); // Ajusta el tamaño
        } catch (Exception e) {
            e.printStackTrace();
            // Si falla, usa una fuente por defecto
            return new Font("SansSerif", Font.PLAIN, (int) tamaño);
        }
    }


    /**
     * Lee los datos del archivo de texto y los almacena en un mapa.
     * @return Un mapa de pares clave-valor (String, String) con los datos del archivo.
     */
    private Map<String, String> leerDatosDesdeArchivo() {
        Map<String, String> datos = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_DATOS))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                // Se divide la línea en la primera ocurrencia de '='
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
     * Crea y devuelve el panel con los datos de precios, leyéndolos desde un archivo.
     * @return JPanel con la información formateada.
     */
    private JPanel crearPanelDatos() {
        JPanel datosPanel = new JPanel();
        datosPanel.setBorder(new RoundedBorder(20, bordeDorado, 3));
        datosPanel.setBackground(fondoPanel);
        datosPanel.setPreferredSize(new Dimension(600, 180));
        datosPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        datosPanel.setLayout(new GridLayout(8, 2, 15, 5)); // 2 filas, 4 columnas

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
            panelDato.add(Box.createRigidArea(new Dimension(5, 0))); // Espacio entre clave y valor
            panelDato.add(labelValor);

            datosPanel.add(panelDato);
        }

        return datosPanel;
    }
    
    // --- FIN DE LOS NUEVOS MÉTODOS ---

    /**
     * Clase para crear bordes redondeados personalizados.
     */
    private static class RoundedBorder implements Border {
        private int radius;
        private Color color;
        private int thickness;

        public RoundedBorder(int radius, Color color, int thickness) {
            this.radius = radius;
            this.color = color;
            this.thickness = thickness;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x + thickness / 2, y + thickness / 2,
                    width - thickness, height - thickness,
                    radius, radius);
        }
    }

    private ImageIcon escalarImagen(ImageIcon iconoOriginal, int ancho, int alto) {
        Image imagenOriginal = iconoOriginal.getImage();
        Image imagenEscalada = imagenOriginal.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
    return new ImageIcon(imagenEscalada);
}

    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Tamaño base
                float tamañoBase = 13f;
                
                // Aplicar Inter Regular como tipografía general
                UIManager.put("Label.font", Fuentes.getRegular(tamañoBase));
                UIManager.put("Button.font", Fuentes.getBold(tamañoBase));
                UIManager.put("TextField.font", Fuentes.getRegular(tamañoBase));
                UIManager.put("TextArea.font", Fuentes.getRegular(tamañoBase));
                UIManager.put("Table.font", Fuentes.getRegular(tamañoBase));
                UIManager.put("TableHeader.font", Fuentes.getBold(tamañoBase));
                UIManager.put("Menu.font", Fuentes.getRegular(tamañoBase));
                UIManager.put("MenuItem.font", Fuentes.getRegular(tamañoBase));
                UIManager.put("TabbedPane.font", Fuentes.getRegular(tamañoBase));
                UIManager.put("TitledBorder.font", Fuentes.getBold(16f));

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Iniciar la aplicación
            new Prototipo();
        });
    }
}