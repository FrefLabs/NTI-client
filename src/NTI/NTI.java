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
import java.text.DecimalFormat;
import java.util.Map;

import juego.PanelJuego; 
import NTI.Registro;     
import NTI.Accion;
import NTI.Lectura;

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
    Entorno ent = new Entorno();
    
    private PanelJuego panelJuego;
    private Registro registro;
    private Accion accion;
    private Lectura lectura;
    private Modelo modelo;
    
    // --- Componentes UI como campos de clase ---
    private JPanel listaValores; // (MODIFICADO) Hecho variable de clase
    private JLabel lblFallbackModelos; // (NUEVO) Para "No hay modelos"
    
    // --- Variables de clase para PanelModelos ---
    private JPanel panelListaModelos; 
    private JPanel panelDetalleModelo; 
    private CardLayout cardLayoutModelos;
    private JPanel panelModelosContenido;
    private JLabel lblFallbackRecomendados;

    // --- Componentes del panel (PARA ACTUALIZAR) ---
    private JLabel lblDetalle_Titulo;
    private JLabel lblDetalle_Descripcion;
    private JButton btnDetalle_Seleccionado;
    private JLabel lblDetalle_MSE, lblDetalle_RMSE, lblDetalle_MAE, lblDetalle_R2, lblDetalle_MaxError, lblDetalle_MinError, lblDetalle_Percentil90, lblDetalle_Precision;
    private JLabel lblDetalle_FechaIni, lblDetalle_FechaFin, lblDetalle_Arquitectura, lblDetalle_Funciones, lblDetalle_Tasa, lblDetalle_NMaxError, lblDetalle_Epocas;

    // Formateadores para la vista detallada
    private final DecimalFormat df8 = new DecimalFormat("0.00000000");
    private final DecimalFormat df4 = new DecimalFormat("0.0000");
    private final DecimalFormat dfPercent = new DecimalFormat("0.00'%'");

    // --- Formateadores para los datos decimales (BDD) ---
    private final DecimalFormat percentFormat = new DecimalFormat("0.00'%'");
    private final DecimalFormat maeFormat = new DecimalFormat("'$'0.00");

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
        ImageIcon logoIconOriginal = new ImageIcon(getClass().getResource("/img/Logo.png"));

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

        // --- Lista de Modelos mas precisos ---
        listaValores = new JPanel();
        listaValores.setLayout(new BoxLayout(listaValores, BoxLayout.Y_AXIS));
        listaValores.setBackground(fondoPanel);
        listaValores.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, bordeDorado, 3),
                new EmptyBorder(10, 10, 10, 10)
        ));

        // Crear el título con BorderLayout

        JPanel panelTitulo = new JPanel(new BorderLayout());
        panelTitulo.setOpaque(false);
        panelTitulo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel lblTituloModelos = new JLabel("<html><span style='color:" + letra + ";'>Modelos más </span> <span style='color:" + bordeDorado + ";'>precisos</span></html>");
        lblTituloModelos.setFont(Fuentes.getBlack(14f));
        lblTituloModelos.setBorder(new EmptyBorder(0, 5, 10, 5));

        panelTitulo.add(lblTituloModelos, BorderLayout.WEST); // WEST fuerza izquierda
        listaValores.add(panelTitulo); // Agregar el panel con el título

        // Crear la etiqueta de fallback y ocultarla (en caso de que no hayan modelos)
        lblFallbackModelos = new JLabel("Aún no hay modelos disponibles", SwingConstants.CENTER);
        lblFallbackModelos.setForeground(letra.darker());
        lblFallbackModelos.setFont(Fuentes.getRegular(16f));
        lblFallbackModelos.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblFallbackModelos.setVisible(false);
        
        // Contenedor para mantener el borde y el tamaño
        JPanel contenedorLista = new JPanel(new BorderLayout());
        contenedorLista.setBackground(fondoPanel);
        contenedorLista.setPreferredSize(new Dimension(450, 160));
        contenedorLista.setMaximumSize(new Dimension(Integer.MAX_VALUE, 245));
        contenedorLista.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenedorLista.add(listaValores, BorderLayout.CENTER);
        
        derecha.add(contenedorLista); // Añadir el contenedor
        derecha.add(Box.createVerticalStrut(15));

        // --- Noticias
        JPanel noticias = new JPanel();
        noticias.setLayout(new BoxLayout(noticias, BoxLayout.Y_AXIS));
        noticias.setBackground(fondoPanel);
        noticias.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, bordeDorado, 3),
                new EmptyBorder(20, 0, 10, 15) // margen interior del contenedor
        ));
        noticias.setAlignmentX(Component.LEFT_ALIGNMENT);
        derecha.add(noticias);

        // Obtener noticias desde la base de datos
        Vector<Tupla> datnot = not.getDatos();
        
        for (Tupla noticia : datnot) {
            // Panel individual de noticia
            JPanel panelNoticia = new JPanel(new BorderLayout());
            panelNoticia.setBackground(fondoPanel);
            panelNoticia.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

            // Panel de texto con BoxLayout vertical
            JPanel textoPanel = new JPanel();
            textoPanel.setLayout(new BoxLayout(textoPanel, BoxLayout.Y_AXIS));
            textoPanel.setBackground(fondoPanel);

            // Título usando JTextArea para ajuste automático de ancho
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

            // Panel para la fuente a la derecha
            JPanel fuentePanel = new JPanel(new BorderLayout());
            fuentePanel.setBackground(fondoPanel);
            JLabel fuenteLabel = new JLabel(noticia.getFuente());
            fuenteLabel.setForeground(bordeDorado);
            fuenteLabel.setFont(Fuentes.getBlack(12f));
            fuenteLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            fuentePanel.add(fuenteLabel, BorderLayout.EAST);

            // Agregar título y fuente al panel de texto
            textoPanel.add(title);
            textoPanel.add(Box.createVerticalStrut(3));
            textoPanel.add(fuentePanel);

            // Agregar textoPanel al panelNoticia
            panelNoticia.add(textoPanel, BorderLayout.CENTER);

            // Añadir al panel general
            noticias.add(panelNoticia);
            noticias.add(Box.createVerticalStrut(10));
        }

        // Se agrega el panel derecho al panel principal
        panelInicio.add(derecha, BorderLayout.EAST);
        
        setVisible(true);
        
// ---------------------------------------------------
// PANELES PARA LA PAGINA MODULO (MODIFICADO)
// ---------------------------------------------------
        // Panel principal que ocupa el espacio del centro y derecha
        JPanel panelModelos = new JPanel();
        panelModelos.setLayout(new BorderLayout(20, 20));
        panelModelos.setBackground(fondo);
        panelModelos.setBorder(new EmptyBorder(30, 50, 30, 50));
        panelModelos.setVisible(false); // oculto por defecto

        //--------------------------------
        // PANEL SUPERIOR (BARRA DE BUSQUEDA Y BOTON DE CREAR MODELO)
        //--------------------------------
        JPanel barraSuperior = new JPanel(new GridBagLayout());
        barraSuperior.setOpaque(false); // Transparente
        GridBagConstraints gbcBarra = new GridBagConstraints();
        gbcBarra.fill = GridBagConstraints.BOTH;
        gbcBarra.ipady = 10; // Añade un poco de altura interna (COMO EL PADDING)

        // --- Panel de Búsqueda (80%) ---
        JPanel panelBusqueda = new JPanel(new BorderLayout(10, 0));
        panelBusqueda.setBackground(fondoPanel); // Fondo oscuro
        panelBusqueda.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, bordeDorado, 3),
                new EmptyBorder(0, 0, 0, 5)
        ));
        // Icono 'Q' (aun no tengo una imagen de la lupa ;( )
        JLabel lblIconLupa = new JLabel("Q");
        lblIconLupa.setFont(Fuentes.getBold(18f));
        lblIconLupa.setForeground(letra);
        // Padding: Arriba 5, Izq 15, Abajo 5, Der 0  (PROXIMAMENTE CAMBIOS)
        lblIconLupa.setBorder(new EmptyBorder(5, 15, 5, 0));
        panelBusqueda.add(lblIconLupa, BorderLayout.WEST);

        // Campo de texto (Modificado)
        JTextField txtBuscarModelo = new JTextField(); // El texto lo pone el listener
        txtBuscarModelo.setBackground(fondoPanel); // Mismo fondo que el panel
        txtBuscarModelo.setCaretColor(Color.WHITE);
        // Padding: Arriba 5, Izq 0, Abajo 5, Der 5
        txtBuscarModelo.setBorder(new EmptyBorder(5, 0, 5, 5));

        addPlaceholderFocusListener(txtBuscarModelo, "Buscar modelos");

        panelBusqueda.add(txtBuscarModelo, BorderLayout.CENTER);

        gbcBarra.gridx = 0;
        gbcBarra.weightx = 0.8; // 80% de ancho
        gbcBarra.insets = new Insets(0, 0, 0, 20); // 20px de espacio a la derecha
        barraSuperior.add(panelBusqueda, gbcBarra);

        // --- Botón + Crear Modelo (20%) ---
        JButton btnCrearModelo = new JButton("+ Crear Modelo");
        btnCrearModelo.setBackground(fondoPanel); // Fondo de panel
        btnCrearModelo.setForeground(bordeDorado); // Letras doradas
        btnCrearModelo.setFont(Fuentes.getBold(14f));
        btnCrearModelo.setBorder(new RoundedBorder(15, bordeDorado, 3)); // Borde dorado
        btnCrearModelo.setFocusPainted(false);

        gbcBarra.gridx = 1;
        gbcBarra.weightx = 0.2; // 20% de ancho
        gbcBarra.insets = new Insets(0, 0, 0, 0); // Sin espacio
        barraSuperior.add(btnCrearModelo, gbcBarra);

        // Añadir la barra superior al panel de modelos
        panelModelos.add(barraSuperior, BorderLayout.NORTH);

        //--------------------------------
        // PANEL CENTRAL CON CARDLAYOUT
        //--------------------------------
        cardLayoutModelos = new CardLayout();
        panelModelosContenido = new JPanel(cardLayoutModelos);
        panelModelosContenido.setOpaque(false);
        // --- Tarjeta 1: Lista de Modelos (MODIFICADO con JScrollPane) ---
        panelListaModelos = new JPanel(); // Asignar a variable de clase
        panelListaModelos.setLayout(new BoxLayout(panelListaModelos, BoxLayout.Y_AXIS));
        panelListaModelos.setBackground(fondo);
        panelListaModelos.setBorder(new EmptyBorder(10, 0, 10, 0));

        // (El código de lblFallbackRecomendados sigue igual)
        lblFallbackRecomendados = new JLabel("No hay modelos recomendados disponibles.");
        lblFallbackRecomendados.setFont(Fuentes.getRegular(16f));
        lblFallbackRecomendados.setForeground(letra.darker());
        lblFallbackRecomendados.setVisible(false);

        // Crear el JScrollPane
        JScrollPane scrollListaModelos = new JScrollPane(panelListaModelos);
        // Estilizar el scroll para que se haga "Invisible"
        scrollListaModelos.setBorder(null); // Quitar el borde
        scrollListaModelos.getViewport().setBackground(fondo); // Fondo del área visible
        scrollListaModelos.setBackground(fondo); // Fondo general

        // Ocultar barra
        scrollListaModelos.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        // Acelerar la velocidad del scroll (para cuando sean demasiados)
        scrollListaModelos.getVerticalScrollBar().setUnitIncrement(16);
        // --- Tarjeta 2: Crear Nuevo Modelo ---
        JPanel panelCrearModelo = crearPanelCrearModelo(cardLayoutModelos, panelModelosContenido);
        // --- Tarjeta 3: Detalle del Modelo ---
        panelDetalleModelo = crearPanelDetalleModelo();
        // --- Añadir tarjetas al CardLayout ---
        // Añadimos el JScrollPane en lugar del panel directo
        
        panelModelosContenido.add(scrollListaModelos, "LISTA");
        panelModelosContenido.add(panelCrearModelo, "CREAR");
        panelModelosContenido.add(panelDetalleModelo, "DETALLE");
        panelModelos.add(panelModelosContenido, BorderLayout.CENTER);
        // Acción del botón para cambiar de tarjeta
        btnCrearModelo.addActionListener(e -> cardLayoutModelos.show(panelModelosContenido, "CREAR"));
        
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
        
        String[] config = ent.conseguirConfig();
        if (config != null) {
            // COMBOBOX MONEDA
            for (int i = 0; i < cbMoneda.getItemCount(); i++) {
                if (cbMoneda.getItemAt(i).equals(config[0])) {
                    cbMoneda.setSelectedIndex(i);
                    break;
                }
            }

            // COMBOBOX IDIOMA
            for (int i = 0; i < cbIdioma.getItemCount(); i++) {
                if (cbIdioma.getItemAt(i).equals(config[1])) {
                    cbIdioma.setSelectedIndex(i);
                    break;
                }
            }

            // TOGGLES
            toggleSonido.setSelected(Boolean.parseBoolean(config[2]));
            toggleOscuro.setSelected(Boolean.parseBoolean(config[3]));
            toggleRed.setSelected(Boolean.parseBoolean(config[4]));
            
            //esto es para el cambio de color, nota: despues si se cambia el toogle por ua version mas linda cambiar esto tambien.
            for (javax.swing.event.ChangeListener cl : toggleSonido.getChangeListeners()) {
                cl.stateChanged(new ChangeEvent(toggleSonido));
            }
            for (javax.swing.event.ChangeListener cl : toggleOscuro.getChangeListeners()) {
                cl.stateChanged(new ChangeEvent(toggleOscuro));
            }
            for (javax.swing.event.ChangeListener cl : toggleRed.getChangeListeners()) {
                cl.stateChanged(new ChangeEvent(toggleRed));
            }
        } else {
            // Si no se encontró el archivo, mostrar un mensaje
            JOptionPane.showMessageDialog(
                    null,
                    "No se encontró el archivo de configuración. Se usarán valores por defecto.",
                    "Archivo no encontrado",
                    JOptionPane.WARNING_MESSAGE
            );
        }

        // Panel contenedor para Aplicar Cambios
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelBoton.setOpaque(false);
        panelBoton.setBorder(new EmptyBorder(15, 0, 0, 0));
        panelBoton.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnAplicar = new JButton("Aplicar cambios");
        btnAplicar.setBackground(fondoPanel);
        btnAplicar.setForeground(letra);
        btnAplicar.setFocusPainted(false);
        btnAplicar.setPreferredSize(new Dimension(160, 35));
        btnAplicar.setBorder(new RoundedBorder(20, bordeDorado, 2));
        btnAplicar.setFont(Fuentes.getBlack(14f));

        panelBoton.add(btnAplicar);
        panelAjustes.add(panelBoton);
        
        registro = new Registro();
        accion = new Accion();
        lectura = new Lectura();
        modelo = new Modelo(lectura);
        // (NUEVO) 3. Inicializar el panel del juego
        panelJuego = new PanelJuego(registro, accion, lectura);
        
        //agregamos los paneles al card
        contentPanel.add(panelInicio, "inicio");
        contentPanel.add(panelModelos, "modelos");
        contentPanel.add(panelAjustes, "ajustes");
        contentPanel.add(panelJuego, "juego"); // (NUEVO) 3. Añadir el panel del juego al CardLayout
        
        cargarTopModelos();
        cargarPanelModelos();

// ------------------------------
// Funcionalidades de Botones
// ------------------------------        

        CardLayout cardLayout = (CardLayout) contentPanel.getLayout();
        
        btnInicio.addActionListener(e -> {
            cardLayout.show(contentPanel, "inicio");
            panelJuego.onPanelOcultado(); // Detener timers del juego
        });

        btnModelos.addActionListener(e -> {
            cardLayout.show(contentPanel, "modelos");
            cardLayoutModelos.show(panelModelosContenido, "LISTA");
            cargarPanelModelos(); // Recargar datos cada vez
            panelJuego.onPanelOcultado();
        });
        
        //btnHistorial.addActionListener(e -> cardLayout.show(contentPanel, "historial"));

        btnJuego.addActionListener(e -> { // Botón de la sidebar
            cardLayout.show(contentPanel, "juego");
            panelJuego.onPanelMostrado(); // Iniciar timers del juego
        }); 

        btnAjustes.addActionListener(e -> {
            cardLayout.show(contentPanel, "ajustes");
            panelJuego.onPanelOcultado(); // Detener timers del juego
        });
        
        btnAplicar.addActionListener(e -> {
            String monedaS = (String) cbMoneda.getSelectedItem();
            String idiomaS = (String) cbIdioma.getSelectedItem();
            boolean sfxR = toggleSonido.isSelected();
            boolean modoR = toggleOscuro.isSelected();
            boolean rdrR = toggleRed.isSelected();
            System.out.println("Moneda: " + monedaS);
            System.out.println("Idioma: " + idiomaS);
            System.out.println("Efectos de sonido: " + sfxR);
            System.out.println("Modo oscuro: " + modoR);
            System.out.println("Red de refinamiento: " + rdrR);
            boolean resultado = ent.enviarNConfig(monedaS, idiomaS, sfxR, modoR, rdrR);
            if (resultado) {
                JOptionPane.showMessageDialog(null, "Cambios guardados con éxito", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Ocurrió un problema, inténtelo de nuevo", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
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

    private JPanel crearPanelCrearModelo(CardLayout cardLayout, JPanel cardPanel) {
        JPanel panel = new JPanel(new BorderLayout(0, 30)); // Panel principal con 30px de gap vertical
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 10, 0));

        // --- TÍTULO ---
        JLabel lblTituloCrear = new JLabel("<html>Crear nuevo <span style='color:" + bordedoradoStr + ";'>modelo</span></html>");
        lblTituloCrear.setFont(Fuentes.getBlack(24f));
        lblTituloCrear.setForeground(letra);
        lblTituloCrear.setBorder(new EmptyBorder(0, 5, 15, 0));
        panel.add(lblTituloCrear, BorderLayout.NORTH);

        // --- PANEL DE FORMULARIO (Dos columnas) ---
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 15, 10, 15); // Espaciado
        gbc.anchor = GridBagConstraints.WEST;

        String[] labels = {"Acción", "Fecha inicio", "Fecha fin", "Arquitectura", "Funciones", "Learning Rate", "Max Error", "Max Iter"};
        String[] placeholders = {"Símbolo de la acción", "Fecha de inicio del dataset", "Fecha de finalización del dataset",
            "Capas y neuronas de la red neuronal", "Funciones de las distintas capas",
            "Tasa de aprendizaje de la red neuronal", "Máximo número de error permitido",
            "Máximo número de épocas permitidas"};

        for (int i = 0; i < labels.length; i++) {
            // Label de la Izquierda
            JLabel label = new JLabel(labels[i]);
            label.setFont(Fuentes.getBold(18f));
            label.setForeground(letra);

            // TextField de la Derecha (estilizado)
            JTextField textField = new JTextField(); // Texto se setea por listener
            textField.setBackground(fondoPanel); // Fondo oscuro
            textField.setCaretColor(letra);
            textField.setFont(Fuentes.getRegular(14f));
            textField.setPreferredSize(new Dimension(300, 40)); // Tamaño fijo
            textField.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(10, bordeDorado, 1),
                    new EmptyBorder(5, 10, 5, 10) // Padding interno
            ));

            // Añadir listener de placeholder
            addPlaceholderFocusListener(textField, placeholders[i]);

            // Añadir al layout
            // Label
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0.3; // 30%
            panelForm.add(label, gbc);

            // TextField
            gbc.gridx = 1;
            gbc.gridy = i;
            gbc.weightx = 0.7; // 70%
            panelForm.add(textField, gbc);
        }

        panel.add(panelForm, BorderLayout.CENTER);

        // --- BOTONES INFERIORES ---
        JPanel panelBotonesInferiores = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0)); // 30px de espacio entre botones
        panelBotonesInferiores.setOpaque(false);

        // --- Botón Volver Atras ---
        JButton btnVolverAtras = new JButton("Volver Atras");
        btnVolverAtras.setBackground(fondoPanel); // Fondo oscuro
        btnVolverAtras.setForeground(bordeDorado); // Letra dorada
        btnVolverAtras.setFont(Fuentes.getBold(16f));
        btnVolverAtras.setFocusPainted(false);
        btnVolverAtras.setPreferredSize(new Dimension(200, 45));
        btnVolverAtras.setBorder(new RoundedBorder(15, bordeDorado, 2));

        // --- Botón Solicitar ---
        JButton btnSolicitar = new JButton("Solicitar");
        btnSolicitar.setBackground(bordeDorado);
        btnSolicitar.setForeground(fondo); // Letra oscura
        btnSolicitar.setFont(Fuentes.getBold(16f));
        btnSolicitar.setFocusPainted(false);
        btnSolicitar.setPreferredSize(new Dimension(200, 45));
        btnSolicitar.setBorder(new RoundedBorder(15, bordeDorado, 2));

        // Acción para volver a la lista de modelos
        btnVolverAtras.addActionListener(e -> {
            cardLayout.show(cardPanel, "LISTA");
        });

        // Acción para el botón Solicitar (por ahora vacía hasta conectar el servidor)
        btnSolicitar.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Funcionalidad 'Solicitar' no implementada.");
        });

        panelBotonesInferiores.add(btnVolverAtras);
        panelBotonesInferiores.add(btnSolicitar);
        panel.add(panelBotonesInferiores, BorderLayout.SOUTH);

        return panel;
    }
    
    private void cargarPanelModelos() {
        // Limpiar el panel de la lista
        panelListaModelos.removeAll();
        lblFallbackRecomendados.setVisible(false);

        new Thread(() -> {
            // Obtener datos del modelo seleccionado
            Map<String, Object> sel = modelo.obtenerInfoSeleccionado();
            
            // Obtener datos de modelos recomendados
            Vector<Map<String, Object>> rec = modelo.obtenerRecomendados();

            // Volver al hilo de la GUI para construir los paneles
            SwingUtilities.invokeLater(() -> {
                // TÍTULO “MODELO SELECCIONADO”
                JLabel lblTituloSel = new JLabel("<html><span style='color:" + letraStr + ";'>Modelo</span> <span style='color:" + bordedoradoStr + ";'>seleccionado</span></html>");
                lblTituloSel.setFont(Fuentes.getBlack(24f));
                lblTituloSel.setAlignmentX(Component.LEFT_ALIGNMENT);
                lblTituloSel.setBorder(new EmptyBorder(0, 5, 15, 0));
                panelListaModelos.add(lblTituloSel);

                // Construir panel "Seleccionado"
                if (sel != null) {
                    JPanel panelSel = crearPanelModeloResumido(sel, true);
                    panelListaModelos.add(panelSel);
                } else {
                    // (Manejo de error si el ID seleccionado no existe)
                    JLabel lblErrorSel = new JLabel("No se pudo cargar el modelo seleccionado (ID: " + modelo.getIDModeloSeleccionado() + ")");
                    lblErrorSel.setForeground(Color.RED);
                    panelListaModelos.add(lblErrorSel);
                }

                // Espacio
                panelListaModelos.add(Box.createVerticalStrut(25));

                // TÍTULO “MODELOS RECOMENDADOS”
                JLabel lblTituloRec = new JLabel("<html><span style='color:" + letraStr + ";'>Modelos</span> <span style='color:" + bordedoradoStr + ";'>recomendados</span></html>");
                lblTituloRec.setFont(Fuentes.getBlack(24f));
                lblTituloRec.setAlignmentX(Component.LEFT_ALIGNMENT);
                lblTituloRec.setBorder(new EmptyBorder(0, 5, 15, 0));
                panelListaModelos.add(lblTituloRec);

                // Construir paneles "Recomendados"
                if (rec == null || rec.isEmpty()) {
                    lblFallbackRecomendados.setVisible(true);
                    panelListaModelos.add(lblFallbackRecomendados);
                } else {
                    for (Map<String, Object> m : rec) {
                        JPanel panelRec = crearPanelModeloResumido(m, false);
                        panelListaModelos.add(panelRec);
                        panelListaModelos.add(Box.createVerticalStrut(15));
                    }
                }
                
                panelListaModelos.add(Box.createVerticalGlue()); // Empujar todo hacia arriba
                panelListaModelos.revalidate();
                panelListaModelos.repaint();
            });
        }).start();
    }

    private JPanel crearPanelModeloResumido(Map<String, Object> datos, boolean isSeleccionado) {
        
        // ---  Asignación segura a prueba ---
        int idModelo = (datos.get("IDModelo") != null) ? (int) datos.get("IDModelo") : 0;
        String simbolo = (datos.get("Simbolo") != null) ? (String) datos.get("Simbolo") : "N/A";
        double precision = (datos.get("Precision") != null) ? (double) datos.get("Precision") : 0.0;
        String fechaIni = (datos.get("FechaIni") != null) ? datos.get("FechaIni").toString() : "N/A";
        String fechaFin = (datos.get("FechaFin") != null) ? datos.get("FechaFin").toString() : "N/A";

        // Lógica para manejar ambas claves ("PromedioError" o "MAE")
        double promError = 0.0;
        if (datos.get("PromedioError") != null) {
            promError = (double) datos.get("PromedioError");
        } else if (datos.get("MAE") != null) {
            promError = (double) datos.get("MAE");
        }
        // --- Fin de la asignación segura ---


        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(fondoPanel);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, bordeDorado, 3),
                new EmptyBorder(15, 15, 15, 15)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- Panel Superior (Info Izquierda) ---
        JPanel panelSuperior = new JPanel(new BorderLayout(10, 0));
        panelSuperior.setOpaque(false);

        JPanel panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        panelInfo.setOpaque(false);

        JLabel lblNombreModelo = new JLabel(simbolo); // Usa la variable segura
        lblNombreModelo.setFont(Fuentes.getBlack(22f));
        lblNombreModelo.setForeground(Color.WHITE);
        panelInfo.add(lblNombreModelo);

        JLabel lblIdModelo = new JLabel("Modelo #" + idModelo); // Usa la variable segura
        lblIdModelo.setFont(Fuentes.getRegular(14f));
        lblIdModelo.setForeground(Color.LIGHT_GRAY);
        panelInfo.add(lblIdModelo);
        
        panelSuperior.add(panelInfo, BorderLayout.WEST);

        // --- Panel Superior (Botón/Info Derecha) ---
        if (isSeleccionado) {
            JButton btnSeleccionado = new JButton("     Seleccionado     ");
            btnSeleccionado.setBackground(Color.decode("#1E2A5A"));
            btnSeleccionado.setForeground(letra);
            btnSeleccionado.setFont(Fuentes.getBold(14f));
            btnSeleccionado.setBorder(new RoundedBorder(15, letra, 1));
            btnSeleccionado.setFocusPainted(false);
            panelSuperior.add(btnSeleccionado, BorderLayout.EAST);
        } else {
            JLabel lblPrecision = new JLabel(dfPercent.format(precision)); // Usa la variable segura
            lblPrecision.setFont(Fuentes.getBold(18f));
            lblPrecision.setForeground(bordeDorado);
            panelSuperior.add(lblPrecision, BorderLayout.EAST);
        }
        
        panel.add(panelSuperior, BorderLayout.NORTH);

        // --- Panel Inferior (Stats) ---
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setOpaque(false);
        panelInferior.setBorder(new EmptyBorder(10, 0, 0, 0));

        if (isSeleccionado) {
            JLabel lblPrecision = new JLabel("Precisión: " + dfPercent.format(precision)); // Usa la variable segura
            lblPrecision.setFont(Fuentes.getBold(16f));
            lblPrecision.setForeground(Color.GREEN); // Verde para el seleccionado
            panelInferior.add(lblPrecision, BorderLayout.WEST);
        } else {
            JLabel lblPromError = new JLabel("Promedio de error: " + maeFormat.format(promError)); // Usa la variable segura
            lblPromError.setFont(Fuentes.getRegular(14f));
            lblPromError.setForeground(letra);
            panelInferior.add(lblPromError, BorderLayout.WEST);
        }

        JLabel lblRango = new JLabel("Rango de entrenamiento: " + fechaIni + " - " + fechaFin); // Usa variables seguras
        lblRango.setFont(Fuentes.getRegular(14f));
        lblRango.setForeground(letra.darker());
        lblRango.setHorizontalAlignment(SwingConstants.RIGHT);
        panelInferior.add(lblRango, BorderLayout.EAST);
        
        panel.add(panelInferior, BorderLayout.CENTER);

        // --- Acción de Clic ---
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mostrarDetalleModelo(idModelo); // Usa la variable segura
            }
        });

        return panel;
    }
    
    
    //Carga los datos del modelo en el panel de detalle y lo muestra.
     
    private void mostrarDetalleModelo(int idModelo) {
        // Mostrar un "Cargando..."
        cardLayoutModelos.show(panelModelosContenido, "DETALLE");
        lblDetalle_Titulo.setText("Cargando Modelo #" + idModelo + "...");
        lblDetalle_Descripcion.setText("Obteniendo datos de la base de datos...");
        
        new Thread(() -> {
            Map<String, Object> d = modelo.obtenerDetalles(idModelo);

            // Volver al hilo de la GUI para actualizar las etiquetas
            SwingUtilities.invokeLater(() -> {
                if (d == null) {
                    lblDetalle_Titulo.setText("Error al cargar Modelo #" + idModelo);
                    lblDetalle_Descripcion.setText("El modelo no se encontró en la base de datos.");
                    return;
                }
                
                // Actualizar todos los JLabels
                lblDetalle_Titulo.setText("Modelo #" + d.get("IDModelo") + " - " + d.get("Simbolo"));
                lblDetalle_Descripcion.setText("<html>" + d.get("E_Descripcion") + "</html>");
                
                // Botón Seleccionado
                if ((int)d.get("IDModelo") == modelo.getIDModeloSeleccionado()) {
                    btnDetalle_Seleccionado.setText("Seleccionado");
                    btnDetalle_Seleccionado.setEnabled(false);
                } else {
                    btnDetalle_Seleccionado.setText("Seleccionar");
                    btnDetalle_Seleccionado.setEnabled(true);
                }
                // (Guardar el ID para el ActionListener del botón)
                btnDetalle_Seleccionado.putClientProperty("IDModelo", d.get("IDModelo"));
                
                // Resultados
                lblDetalle_MSE.setText("MSE: " + df8.format(d.get("MSE")));
                lblDetalle_RMSE.setText("RMSE: " + df8.format(d.get("RMSE")));
                lblDetalle_MAE.setText("MAE: " + df8.format(d.get("MAE")));
                lblDetalle_R2.setText("R2: " + df8.format(d.get("R2")));
                lblDetalle_MaxError.setText("MaxError: " + df8.format(d.get("MaxError")));
                lblDetalle_MinError.setText("MinError: " + df8.format(d.get("MinError")));
                lblDetalle_Percentil90.setText("Percentil 90: " + df8.format(d.get("Percentil90")));
                lblDetalle_Precision.setText("Precisión: " + dfPercent.format(d.get("Precision")));
                
                // Parámetros
                lblDetalle_FechaIni.setText("Fecha inicio: " + d.get("FechaIni").toString());
                lblDetalle_FechaFin.setText("Fecha fin: " + d.get("FechaFin").toString());
                lblDetalle_Arquitectura.setText("Arquitectura: " + d.get("Arquitectura").toString());
                lblDetalle_Funciones.setText("Funciones: " + d.get("Funciones").toString());
                lblDetalle_Tasa.setText("Tasa de aprendizaje: " + d.get("TasaAprendizaje").toString());
                lblDetalle_NMaxError.setText("N° max. de error: " + d.get("NMaxError").toString());
                lblDetalle_Epocas.setText("N° max. de épocas: " + d.get("Epocas").toString());
            });
        }).start();
    }
    
    
     //Crea el panel de la tarjeta "DETALLE" (image_b0e63f.png).
     //Solo crea la estructura, los datos se llenan con 'mostrarDetalleModelo'.
     
    private JPanel crearPanelDetalleModelo() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 10, 0));

        // --- Panel Superior (Título, Descripción, Botón) ---
        JPanel panelSuperior = new JPanel(new BorderLayout(15, 10));
        panelSuperior.setBackground(fondoPanel);
        panelSuperior.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, bordeDorado, 3),
                new EmptyBorder(15, 15, 15, 15)
        ));
        
        JPanel panelTituloDesc = new JPanel();
        panelTituloDesc.setLayout(new BoxLayout(panelTituloDesc, BoxLayout.Y_AXIS));
        panelTituloDesc.setOpaque(false);
        
        lblDetalle_Titulo = new JLabel("Cargando...");
        lblDetalle_Titulo.setFont(Fuentes.getBlack(22f));
        lblDetalle_Titulo.setForeground(Color.WHITE);
        panelTituloDesc.add(lblDetalle_Titulo);
        
        lblDetalle_Descripcion = new JLabel("Cargando descripción...");
        lblDetalle_Descripcion.setFont(Fuentes.getRegular(14f));
        lblDetalle_Descripcion.setForeground(Color.LIGHT_GRAY);
        panelTituloDesc.add(lblDetalle_Descripcion);
        
        panelSuperior.add(panelTituloDesc, BorderLayout.CENTER);
        
        btnDetalle_Seleccionado = new JButton("Seleccionar");
        btnDetalle_Seleccionado.setBackground(fondoPanel);
        btnDetalle_Seleccionado.setForeground(bordeDorado);
        btnDetalle_Seleccionado.setFont(Fuentes.getBold(14f));
        btnDetalle_Seleccionado.setBorder(new RoundedBorder(15, bordeDorado, 2));
        btnDetalle_Seleccionado.setFocusPainted(false);
        
        btnDetalle_Seleccionado.addActionListener(e -> {
            int id = (int) btnDetalle_Seleccionado.getClientProperty("IDModelo");
            modelo.setIDModeloSeleccionado(id); // Actualizar el modelo
            // (Podrías guardar esto en un archivo de config si quieres)
            
            // Volver a la lista
            cardLayoutModelos.show(panelModelosContenido, "LISTA");
            cargarPanelModelos(); // Recargar la lista para que muestre el nuevo
        });
        
        panelSuperior.add(btnDetalle_Seleccionado, BorderLayout.EAST);
        panel.add(panelSuperior, BorderLayout.NORTH);

        // --- Panel Inferior (Resultados y Parámetros) ---
        JPanel panelInferior = new JPanel(new GridLayout(1, 2, 20, 0));
        panelInferior.setBackground(fondoPanel);
        panelInferior.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, bordeDorado, 3),
                new EmptyBorder(20, 25, 20, 25)
        ));
        
        // --- Columna Resultados ---
        JPanel colResultados = new JPanel();
        colResultados.setLayout(new BoxLayout(colResultados, BoxLayout.Y_AXIS));
        colResultados.setOpaque(false);
        
        JLabel lblTituloRes = new JLabel("Resultados del Modelo");
        lblTituloRes.setFont(Fuentes.getBlack(22f));
        lblTituloRes.setForeground(bordeDorado);
        colResultados.add(lblTituloRes);
        colResultados.add(Box.createVerticalStrut(20));
        
        // Crear JLabels para resultados
        lblDetalle_MSE = new JLabel("MSE: ...");
        lblDetalle_RMSE = new JLabel("RMSE: ...");
        lblDetalle_MAE = new JLabel("MAE: ...");
        lblDetalle_R2 = new JLabel("R2: ...");
        lblDetalle_MaxError = new JLabel("MaxError: ...");
        lblDetalle_MinError = new JLabel("MinError: ...");
        lblDetalle_Percentil90 = new JLabel("Percentil 90: ...");
        lblDetalle_Precision = new JLabel("Precisión: ...");
        lblDetalle_Precision.setFont(Fuentes.getBold(18f)); // Destacar
        
        // Estilizar y añadir
        for (JLabel l : new JLabel[]{lblDetalle_MSE, lblDetalle_RMSE, lblDetalle_MAE, lblDetalle_R2, lblDetalle_MaxError, lblDetalle_MinError, lblDetalle_Percentil90, lblDetalle_Precision}) {
            l.setFont(l.getFont().deriveFont(16f));
            l.setForeground(letra);
            l.setBorder(new EmptyBorder(0, 0, 10, 0));
            colResultados.add(l);
        }
        colResultados.add(Box.createVerticalGlue());
        panelInferior.add(colResultados);

        // --- Columna Parámetros ---
        JPanel colParams = new JPanel();
        colParams.setLayout(new BoxLayout(colParams, BoxLayout.Y_AXIS));
        colParams.setOpaque(false);
        
        JLabel lblTituloParams = new JLabel("Parámetros del Modelo");
        lblTituloParams.setFont(Fuentes.getBlack(22f));
        lblTituloParams.setForeground(bordeDorado);
        colParams.add(lblTituloParams);
        colParams.add(Box.createVerticalStrut(20));
        
        // Crear JLabels para parámetros
        lblDetalle_FechaIni = new JLabel("Fecha inicio: ...");
        lblDetalle_FechaFin = new JLabel("Fecha fin: ...");
        lblDetalle_Arquitectura = new JLabel("Arquitectura: ...");
        lblDetalle_Funciones = new JLabel("Funciones: ...");
        lblDetalle_Tasa = new JLabel("Tasa de aprendizaje: ...");
        lblDetalle_NMaxError = new JLabel("N° max. de error: ...");
        lblDetalle_Epocas = new JLabel("N° max. de épocas: ...");
        
        // Estilizar y añadir
        for (JLabel l : new JLabel[]{lblDetalle_FechaIni, lblDetalle_FechaFin, lblDetalle_Arquitectura, lblDetalle_Funciones, lblDetalle_Tasa, lblDetalle_NMaxError, lblDetalle_Epocas}) {
            l.setFont(l.getFont().deriveFont(16f));
            l.setForeground(letra);
            l.setBorder(new EmptyBorder(0, 0, 10, 0));
            colParams.add(l);
        }
        colParams.add(Box.createVerticalGlue());
        panelInferior.add(colParams);
        
        panel.add(panelInferior, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void cargarTopModelos() {
        // Llamar a la BD (en un hilo separado para no congelar la GUI)
        new Thread(() -> {
            Vector<Map<String, Object>> modelos = modelo.obtenerTop3ParaVista();
            // Actualizar la GUI en el hilo principal
            SwingUtilities.invokeLater(() -> {
                // Limpiar SOLO los elementos después del título
                Component titulo = listaValores.getComponent(0);
                listaValores.removeAll();
                listaValores.add(titulo);

                if (modelos == null || modelos.isEmpty()) {
                    lblFallbackModelos.setVisible(true);
                    lblFallbackModelos.setBorder(new EmptyBorder(0, 5, 0, 5));
                    listaValores.add(lblFallbackModelos);
                    listaValores.add(Box.createVerticalGlue());
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

    
     //Helper para crear el panel 
    
    private JPanel crearPanelUnModelo(int rank, String simbolo, String nombreEmpresa, double mae, double precision) {
        
        // Panel principal con GridLayout 2x2
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 0)); // 2 filas, 2 cols, hgap=10, vgap=0
        panel.setOpaque(false);
        
        // --- Padding pequeño ---
        panel.setBorder(new EmptyBorder(0, 5, 0, 5)); 
        
        // --- Altura máxima ---
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); 

        JLabel lblRank = new JLabel(rank + ". " + simbolo);
        lblRank.setForeground(letra);
        
        // --- Ticker ---
        lblRank.setFont(Fuentes.getBlack(14f)); 
        
        lblRank.setVerticalAlignment(SwingConstants.BOTTOM); 
        panel.add(lblRank);

        // (0,1) Arriba-Derecha: Vacío
        panel.add(new JLabel("")); 

        // (1,0) Abajo-Izquierda: Nombre Empresa
        JLabel lblNombre = new JLabel(nombreEmpresa);
        lblNombre.setForeground(letra.darker());
        
        // --- Nombre de empresa---
        lblNombre.setFont(Fuentes.getRegular(12f)); 
        
        lblNombre.setVerticalAlignment(SwingConstants.TOP); 
        panel.add(lblNombre);

        // (1,1) Abajo-Derecha: Stats
        String maeTexto = (mae > 0) ? "± " + maeFormat.format(mae) : maeFormat.format(mae);
        String precisionTexto = percentFormat.format(precision); 
        
        JLabel lblStats = new JLabel(maeTexto + " - " + precisionTexto);
        lblStats.setForeground(bordeDorado); 
        
        // --- Stats ---
        lblStats.setFont(Fuentes.getBold(12f)); 
        
        lblStats.setHorizontalAlignment(SwingConstants.RIGHT); 
        lblStats.setVerticalAlignment(SwingConstants.TOP); 
        
        panel.add(lblStats);

        return panel;
    }
    
    private void addPlaceholderFocusListener(JTextField textField, String placeholder) {
        // Color de placeholder (gris claro)
        Color placeholderColor = Color.LIGHT_GRAY;
        // Color de texto real (blanco)
        Color textColor = letra; // 'letra' es la variable de clase

        // Estado inicial
        textField.setText(placeholder);
        textField.setForeground(placeholderColor);

        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                // Si el texto es el placeholder, se borra y cambia a color de texto real
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(textColor);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                // Si el campo está vacío, restaurar el placeholder
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholder);
                    textField.setForeground(placeholderColor);
                }
            }
        });
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
    public static class RoundedBorder implements Border {
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