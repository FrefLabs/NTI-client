package NTI;

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
import java.util.function.BiFunction;
import java.util.function.Consumer;
import javafx.beans.value.ChangeListener;
import javax.swing.event.ChangeEvent;

public class NTI extends JFrame {
    
    // Contenedor principal para las páginas
    

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

    public NTI() {
        setTitle("NeuroFref Trading Intelligence - 1.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1298, 763);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(fondo); // fondo general
        
        JPanel contentPanel = new JPanel(new CardLayout());
        add(contentPanel, BorderLayout.CENTER);

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

        JButton btnInicio = crearBoton("/img/boton_inicio.png");
        JButton btnModelos = crearBoton("/img/boton_modelos.png");
        JButton btnHistorial = crearBoton("/img/boton_historial.png");
        JButton btnJuego = crearBoton("/img/boton_juego.png");

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

        JButton btnAjustes = crearBoton("/img/boton_ajustes.png");
        panelInferior.add(btnAjustes);

        // Añadir secciones al sidebar
        sidebar.add(panelSuperior, BorderLayout.NORTH);
        sidebar.add(panelCentral, BorderLayout.CENTER);
        sidebar.add(panelInferior, BorderLayout.SOUTH);

        add(sidebar, BorderLayout.WEST);

//--------------------------------
//  PANEL INICIO COMPLETO
//--------------------------------
        // Panel principal que contendrá todo
        JPanel panelInicio = new JPanel(new BorderLayout());
        panelInicio.setBackground(fondo);

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

        // Gráfico dinámico de valores de acción
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

        // Se agrega el panel central al panel principal
        panelInicio.add(centro, BorderLayout.CENTER);

        // ------------------------- PANEL DERECHO --------------------------------------
        JPanel derecha = new JPanel();
        derecha.setLayout(new BoxLayout(derecha, BoxLayout.Y_AXIS));
        derecha.setPreferredSize(new Dimension(450, getHeight()));
        derecha.setBackground(fondo);
        derecha.setBorder(new EmptyBorder(30, 20, 30, 20));

        // --- Recomendación - Panel de Valores
        JPanel recomendacion = new JPanel();
        recomendacion.setBackground(fondoPanel);
        recomendacion.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, bordeDorado, 3),
                new EmptyBorder(10, 10, 10, 10)
        ));
        recomendacion.setLayout(new BoxLayout(recomendacion, BoxLayout.Y_AXIS));
        recomendacion.setPreferredSize(new Dimension(450, 180));
        recomendacion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        recomendacion.setAlignmentX(Component.LEFT_ALIGNMENT);
        derecha.add(recomendacion);
        derecha.add(Box.createVerticalStrut(20));

        // --- Descripción de empresas
        JPanel descripcion = new JPanel();
        descripcion.setBackground(fondoPanel);
        descripcion.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, bordeDorado, 3),
                new EmptyBorder(10, 10, 10, 10)
        ));
        descripcion.setLayout(new BoxLayout(descripcion, BoxLayout.Y_AXIS));
        descripcion.setPreferredSize(new Dimension(450, 130));
        descripcion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        descripcion.setAlignmentX(Component.LEFT_ALIGNMENT);

        Vector<Empresa> empresas = emp.obtenerEmpresasDesdeBD();
        for (Empresa e : empresas) {
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

        // --- Lista de Valores
        JPanel listaValores = new JPanel();
        listaValores.setBackground(fondoPanel);
        listaValores.setBorder(new RoundedBorder(20, bordeDorado, 3));
        listaValores.setPreferredSize(new Dimension(450, 260));
        listaValores.setMaximumSize(new Dimension(Integer.MAX_VALUE, 345));
        listaValores.setAlignmentX(Component.LEFT_ALIGNMENT);
        derecha.add(listaValores);
        derecha.add(Box.createVerticalStrut(15));

        // --- Noticias
        JPanel noticias = new JPanel();
        noticias.setLayout(new BoxLayout(noticias, BoxLayout.Y_AXIS));
        noticias.setBackground(fondoPanel);
        noticias.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, bordeDorado, 3),
                new EmptyBorder(10, 10, 10, 10)
        ));
        noticias.setPreferredSize(new Dimension(450, 170));
        noticias.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        noticias.setAlignmentX(Component.LEFT_ALIGNMENT);
        derecha.add(noticias);

        // Obtener noticias desde el archivo
        Vector<String[]> datnot = not.obtenerDatosNoticias();
        for (int i = 0; i < datnot.size(); i++) {
            String[] noticia = datnot.get(i);
            if (noticia.length < 3) {
                continue;
            }

            JPanel panelNoticia = new JPanel();
            panelNoticia.setLayout(new BoxLayout(panelNoticia, BoxLayout.X_AXIS));
            panelNoticia.setBackground(fondoPanel);
            panelNoticia.setBorder(new EmptyBorder(5, 10, 5, 10));

            String rutaImagen = (i == 0) ? "/Images/Coca.png" : "/Images/CostaCofee.png";
            ImageIcon icono = new ImageIcon(getClass().getResource(rutaImagen));
            JLabel imagenLabel = new JLabel(icono);
            imagenLabel.setBorder(new EmptyBorder(0, 0, 0, 10));

            JPanel textoPanel = new JPanel();
            textoPanel.setLayout(new BoxLayout(textoPanel, BoxLayout.Y_AXIS));
            textoPanel.setBackground(fondoPanel);

            JLabel title = new JLabel(noticia[1]);
            title.setForeground(Color.WHITE);
            title.setFont(Fuentes.getBlack(14f));

            JLabel fuente = new JLabel(noticia[0]);
            fuente.setForeground(bordeDorado);

            JLabel link = new JLabel(noticia[2]);
            link.setForeground(Color.CYAN);

            textoPanel.add(title);
            textoPanel.add(Box.createVerticalStrut(5));
            textoPanel.add(fuente);
            // textoPanel.add(link); // opcional

            panelNoticia.add(imagenLabel);
            panelNoticia.add(textoPanel);

            noticias.add(panelNoticia);
            noticias.add(Box.createVerticalStrut(10));
        }

        // Se agrega el panel derecho al panel principal
        panelInicio.add(derecha, BorderLayout.EAST);
        
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

        //--------------------------------
        // PANEL SUPERIOR - BARRA DE BÚSQUEDA
        //--------------------------------
        JPanel barraSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barraSuperior.setBackground(fondoPanel);
        barraSuperior.setBorder(new RoundedBorder(20, bordeDorado, 3));
        barraSuperior.setPreferredSize(new Dimension(600, 40));

        // Campo de texto
        JTextField txtBuscarModelo = new JTextField("Buscar modelo...");
        txtBuscarModelo.setBackground(fondoPanel);
        txtBuscarModelo.setForeground(Color.LIGHT_GRAY);
        txtBuscarModelo.setCaretColor(Color.WHITE);
        txtBuscarModelo.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(15, bordeDorado, 2), // Borde redondeado con grosor 2
            BorderFactory.createEmptyBorder(5, 10, 5, 10) // Margen interno
        ));

        // Añadir el campo de texto a la barra superior
        barraSuperior.add(txtBuscarModelo, BorderLayout.CENTER);

        // Añadir la barra superior al panel de modelos
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

        JLabel lblPrecision = new JLabel("<html><span style='color:" + letraStr + ";'>Precision: </span> <span style='color:rgb(0,255,0);'>92.26%</span></html>");
        lblPrecision.setFont(Fuentes.getBold(16f));
        panelInferiorKO.add(lblPrecision);

        panelKO.add(panelInferiorKO, BorderLayout.CENTER);

        // Agregar el panel KO al contenido
        contenidoModelos.add(panelKO);

        // Agregar el contenido al panel principal
        panelModelos.add(contenidoModelos, BorderLayout.CENTER);
        
// ---------------------------------------------------
// PANELES PARA LA PAGINA AJUSTES
// ---------------------------------------------------

        JPanel panelAjustes = new JPanel();
        panelAjustes.setLayout(new BoxLayout(panelAjustes, BoxLayout.Y_AXIS));
        panelAjustes.setBackground(fondo);
        panelAjustes.setBorder(new EmptyBorder(30, 20, 30, 20));
        panelAjustes.setVisible(false);

        // LABEL TITULO AJUSTES
        JLabel lblAjustes = new JLabel("Ajustes");
        lblAjustes.setForeground(bordeDorado);
        lblAjustes.setFont(Fuentes.getBlack(26f));
        lblAjustes.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblAjustes.setBorder(new EmptyBorder(0, 0, 20, 0));
        panelAjustes.add(lblAjustes);

        // -------------------- Método auxiliar para paneles --------------------
        BiFunction<String[], JComponent, JPanel> crearPanelAjuste = (textos, componente) -> {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(fondoPanel);
            panel.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(15, bordeDorado, 2),
                    new EmptyBorder(10, 10, 10, 10)
            ));
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 0, 20); // margen derecho 20px
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;

            // JLabel con título grande y subtítulo pequeño
            JLabel lbl = new JLabel("<html><span style='font-size:16px; font-weight:bold;'>" + textos[0] + "</span><br><span style='font-size:10px;'>" + textos[1] + "</span></html>");
            lbl.setForeground(letra);

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.9;
            panel.add(lbl, gbc);

            // Componente a la derecha
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.weightx = 0.1;
            panel.add(componente, gbc);

            return panel;
        };

        // -------------------- Método auxiliar para togglesbtn --------------------
        Consumer<JToggleButton> estilizarToggle = toggle -> {
            toggle.setFocusPainted(false);
            toggle.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            toggle.setOpaque(true);
            toggle.setContentAreaFilled(true);
            toggle.addChangeListener(e -> {
                if (toggle.isSelected()) {
                    toggle.setBackground(Color.BLACK);
                    toggle.setForeground(Color.WHITE);
                } else {
                    toggle.setBackground(Color.WHITE);
                    toggle.setForeground(Color.BLACK);
                }
            });
            // disparar el cambio inicial
            for (javax.swing.event.ChangeListener cl : toggle.getChangeListeners()) {
                cl.stateChanged(new ChangeEvent(toggle));
            }
        };

        // -------------------- MONEDA --------------------
        String[] textosMoneda = {"Moneda", "Seleccione la divisa con la que desea que se muestre el sistema."};
        JComboBox<String> cbMoneda = new JComboBox<>(new String[]{"US Dolar (USD)", "Euro (EUR)", "Peso Argentino (ARS)"});
        cbMoneda.setBackground(Color.BLACK);
        cbMoneda.setForeground(Color.WHITE);
        cbMoneda.setPreferredSize(new Dimension(180, 30));
        JPanel panelMoneda = crearPanelAjuste.apply(textosMoneda, cbMoneda);
        panelAjustes.add(panelMoneda);
        panelAjustes.add(Box.createVerticalStrut(15));

        // -------------------- IDIOMA --------------------
        String[] textosIdioma = {"Idioma", "Seleccione el idioma con el que desea que se muestre el sistema."};
        JComboBox<String> cbIdioma = new JComboBox<>(new String[]{"Español (ES)", "English (EN)", "Italiano"});
        cbIdioma.setBackground(Color.BLACK);
        cbIdioma.setForeground(Color.WHITE);
        cbIdioma.setPreferredSize(new Dimension(180, 30));
        JPanel panelIdioma = crearPanelAjuste.apply(textosIdioma, cbIdioma);
        panelAjustes.add(panelIdioma);
        panelAjustes.add(Box.createVerticalStrut(15));

        // -------------------- EFECTOS DE SONIDO --------------------
        String[] textosSonido = {"Efectos de sonido", "Desactive esta opción para deshabilitar los efectos de sonido."};
        JToggleButton toggleSonido = new JToggleButton();
        toggleSonido.setSelected(true);
        toggleSonido.setPreferredSize(new Dimension(60, 25));
        estilizarToggle.accept(toggleSonido);
        JPanel panelSonido = crearPanelAjuste.apply(textosSonido, toggleSonido);
        panelAjustes.add(panelSonido);
        panelAjustes.add(Box.createVerticalStrut(15));

        // -------------------- MODO OSCURO --------------------
        String[] textosOscuro = {"Modo oscuro", "Desactive esta opción para deshabilitar el modo claro."};
        JToggleButton toggleOscuro = new JToggleButton();
        toggleOscuro.setSelected(true);
        toggleOscuro.setPreferredSize(new Dimension(60, 25));
        estilizarToggle.accept(toggleOscuro);
        JPanel panelOscuro = crearPanelAjuste.apply(textosOscuro, toggleOscuro);
        panelAjustes.add(panelOscuro);
        panelAjustes.add(Box.createVerticalStrut(15));

        // -------------------- RED DE REFINAMIENTO --------------------
        String[] textosRed = {"Red de refinamiento", "Desactive esta opción para deshabilitar la red neuronal que refina el resultado."};
        JToggleButton toggleRed = new JToggleButton();
        toggleRed.setSelected(true);
        toggleRed.setPreferredSize(new Dimension(60, 25));
        estilizarToggle.accept(toggleRed);
        JPanel panelRed = crearPanelAjuste.apply(textosRed, toggleRed);
        panelAjustes.add(panelRed);
            
        // Espaciador flexible antes del botón para empujar hacia abajo
        panelAjustes.add(Box.createVerticalGlue());

        // Panel contenedor para el botón alineado a la derecha
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelBoton.setOpaque(false);
        panelBoton.setBorder(new EmptyBorder(15, 0, 0, 0));
        panelBoton.setAlignmentX(Component.LEFT_ALIGNMENT); // clave: evitar que se estire todo

        JButton btnAplicar = new JButton("Aplicar cambios");
        btnAplicar.setBackground(fondoPanel);
        btnAplicar.setForeground(letra);
        btnAplicar.setFocusPainted(false);
        btnAplicar.setPreferredSize(new Dimension(160, 35));
        btnAplicar.setBorder(new RoundedBorder(20, bordeDorado, 2));
        btnAplicar.setFont(Fuentes.getBlack(14f));

        panelBoton.add(btnAplicar);
        panelAjustes.add(panelBoton);
        
        contentPanel.add(panelInicio, "inicio");
        contentPanel.add(panelModelos, "modelos");
        contentPanel.add(panelAjustes, "ajustes");

// ------------------------------
// Funcionalidades de Botones
// ------------------------------        

        CardLayout cardLayout = (CardLayout) contentPanel.getLayout();

        btnInicio.addActionListener(e -> cardLayout.show(contentPanel, "inicio"));

        btnModelos.addActionListener(e -> cardLayout.show(contentPanel, "modelos"));
        
        //btnHistorial.addActionListener(e -> cardLayout.show(contentPanel, "historial"));

        //btnJuego.addActionListener(e -> cardLayout.show(contentPanel, "juego"));

        btnAjustes.addActionListener(e -> cardLayout.show(contentPanel, "ajustes"));
    }

    // Crear botones de la sidebar
    private JButton crearBoton(String rutaImagen) {
        JButton boton = new JButton();
        boton.setPreferredSize(new Dimension(174, 46));
        boton.setMaximumSize(new Dimension(174, 46));
        boton.setMinimumSize(new Dimension(174, 46));
        boton.setBackground(new Color(20, 20, 20));
        boton.setFocusPainted(false);
        boton.setBorder(new TransparentRoundedBorder(15)); // Borde redondeado sin color

        // Cargar la imagen directamente sin escalar
        ImageIcon icono = new ImageIcon(getClass().getResource(rutaImagen));
        boton.setIcon(icono);

        // Configurar el botón para que no muestre texto
        boton.setText("");
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);

        return boton;
    }


    private static class TransparentRoundedBorder implements Border {
        private int radius;

        public TransparentRoundedBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 0, 0); // Sin márgenes internos
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Dibujar un borde redondeado sin color visible
            g2.setColor(c.getBackground()); // Usar el color de fondo del botón
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
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
            new NTI();
        });
    }
}