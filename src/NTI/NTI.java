package NTI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import juego.PanelJuego;

/**
 * (CLASE PRINCIPAL - CORREGIDA)
 * El ActionListener de btnAjustes ahora llama a
 * panelAjustes.cargarConfiguracionActual() CADA VEZ que se presiona.
 */
public class NTI extends JFrame {

    // --- Colores y Variables (Tus originales) ---
    public static final String ARCHIVO_DATOS = "datos.txt";
    Color bordeDorado = Color.decode("#D4AF37");
    Color fondo = Color.decode("#030614");
    Color fondoPanel = Color.decode("#060521");
    Color letra = Color.white;
    String bordedoradoStr = "rgb(" + bordeDorado.getRed() + "," + bordeDorado.getGreen() + "," + bordeDorado.getBlue() + ")";
    String letraStr = "rgb(" + letra.getRed() + "," + letra.getGreen() + "," + letra.getBlue() + ")";

    // --- Lógica y Datos (Instanciados una vez) ---
    Empresa emp;
    Noticia not;
    Entorno ent;
    Registro registro;
    Accion accion;
    Lectura lectura;
    Modelo modelo;
    
    public String estiloGraficaActual;

    // --- Paneles de Contenido (Refactorizados) ---
    public PanelInicio panelInicio;
    
    private PanelJuego panelJuego;
    private PanelModelos panelModelos;
    private PanelAjustes panelAjustes;
    private PanelHistorial panelHistorial; 

    // --- Componentes del CardLayout ---
    private CardLayout cardLayout;
    private JPanel contentPanel;
    
    // --- Formateadores (usados por los paneles) ---
    final DecimalFormat percentFormat = new DecimalFormat("0.00'%'");
    final DecimalFormat maeFormat = new DecimalFormat("'$'0.00");
    final DecimalFormat dfPercent = new DecimalFormat("0.00'%'");
    final DecimalFormat df8 = new DecimalFormat("0.00000000"); // (Para PanelModelos)
    final DecimalFormat df4 = new DecimalFormat("0.0000"); // (Para PanelModelos)

    public NTI() {
        setTitle("NeuroFref Trading Intelligence - 1.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1298, 763);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(fondo);

        // --- 1. Inicializar Lógica (CON NUEVO ORDEN DE CARGA) ---
        
        ent = new Entorno();
        registro = new Registro();
        lectura = new Lectura();
        
        String[] config = ent.conseguirConfig();
        
        int idModeloActual = 29; // Default
        try {
            idModeloActual = Integer.parseInt(config[3]);
        } catch (NumberFormatException e) {
            System.err.println("Error al leer IDModelo del config, usando default 29.");
        }
        
        this.estiloGraficaActual = config[4];
        
        String simboloActual = lectura.obtenerSimboloPorIDModelo(idModeloActual);
        System.out.println("Iniciando con Modelo ID: " + idModeloActual + " (Símbolo: " + simboloActual + ")");

        accion = new Accion();
        accion.simbolo = simboloActual; 
        
        modelo = new Modelo(lectura);
        modelo.setIDModeloSeleccionado(idModeloActual); 

        emp = new Empresa();
        not = new Noticia();
        
        // --- 2. Crear Sidebar (Tu código original) ---
        JPanel sidebar = crearPanelSidebar();
        add(sidebar, BorderLayout.WEST);
        
        // --- 3. Crear Panel Contenedor (CardLayout) ---
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        add(contentPanel, BorderLayout.CENTER);

        // --- 4. Instanciar Paneles Refactorizados ---
        panelInicio = new PanelInicio(this);
        panelModelos = new PanelModelos(this);
        panelAjustes = new PanelAjustes(this); // (El constructor YA NO carga datos)
        panelHistorial = new PanelHistorial(this);
        
        panelJuego = new PanelJuego(registro, accion, lectura);

        // --- 5. Añadir Paneles al CardLayout ---
        contentPanel.add(panelInicio, "inicio");
        contentPanel.add(panelModelos, "modelos");
        contentPanel.add(panelAjustes, "ajustes");
        contentPanel.add(panelJuego, "juego");
        contentPanel.add(panelHistorial, "historial");

        // --- 6. Cargar datos iniciales y mostrar ---
        panelInicio.cargarDatosIniciales(); 
        panelModelos.cargarDatosIniciales();
        
        setVisible(true);
    }

    /**
     * (MÉTODO CORREGIDO)
     * El listener de 'btnAjustes' ahora llama a 'cargarConfiguracionActual'.
     */
    private JPanel crearPanelSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(200, getHeight()));
        sidebar.setLayout(new BorderLayout());
        sidebar.setBackground(new Color(10, 10, 10));

        // Panel Título (Logo)
        JPanel panelSuperior = new JPanel();
        panelSuperior.setPreferredSize(new Dimension(200, 100));
        panelSuperior.setBackground(new Color(10, 10, 10));
        panelSuperior.setBorder(new EmptyBorder(20, 0, 0, 0));
        ImageIcon logoIconOriginal = new ImageIcon(getClass().getResource("/img/Logo.png"));
        ImageIcon logoIconEscalado = escalarImagen(logoIconOriginal, 160, 90);
        JLabel logoLabel = new JLabel(logoIconEscalado);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoLabel.setVerticalAlignment(SwingConstants.CENTER);
        panelSuperior.setLayout(new BorderLayout());
        panelSuperior.add(logoLabel, BorderLayout.CENTER);

        // Panel Central (Botones)
        JPanel panelCentral = new JPanel();
        panelCentral.setBackground(new Color(10, 10, 10));
        panelCentral.setLayout(new BoxLayout(panelCentral, BoxLayout.Y_AXIS));
        panelCentral.setBorder(new EmptyBorder(5, 20, 20, 20));

        JButton btnInicio = crearBoton("/img/boton_inicio.png");
        JButton btnModelos = crearBoton("/img/boton_modelos.png");
        JButton btnHistorial = crearBoton("/img/boton_historial.png");
        JButton btnJuego = crearBoton("/img/boton_juego.png");

        JButton[] botones = {btnInicio, btnModelos, btnHistorial, btnJuego};
        for (int i = 0; i < botones.length; i++) {
            panelCentral.add(botones[i]);
            if (i < botones.length - 1) {
                panelCentral.add(Box.createRigidArea(new Dimension(0, 20)));
            }
        }

        // Panel Inferior (Ajustes)
        JPanel panelInferior = new JPanel();
        panelInferior.setBackground(new Color(10, 10, 10));
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));
        panelInferior.setBorder(new EmptyBorder(10, 20, 20, 20));
        JButton btnAjustes = crearBoton("/img/boton_ajustes.png");
        panelInferior.add(btnAjustes);

        sidebar.add(panelSuperior, BorderLayout.NORTH);
        sidebar.add(panelCentral, BorderLayout.CENTER);
        sidebar.add(panelInferior, BorderLayout.SOUTH);

        // --- Action Listeners (Tu código original) ---
        btnInicio.addActionListener(e -> {
            cardLayout.show(contentPanel, "inicio");
            panelJuego.onPanelOcultado();
        });

        btnModelos.addActionListener(e -> {
            cardLayout.show(contentPanel, "modelos");
            panelModelos.mostrarListaYRecargar();
            panelJuego.onPanelOcultado();
        });
        
        btnHistorial.addActionListener(e -> {
            cardLayout.show(contentPanel, "historial");
            panelHistorial.cargarDatos(); 
            panelJuego.onPanelOcultado();
        });

        btnJuego.addActionListener(e -> {
            cardLayout.show(contentPanel, "juego");
            panelJuego.onPanelMostrado();
        });

        // --- (INICIO DE LA CORRECCIÓN) ---
        btnAjustes.addActionListener(e -> {
            // 1. (LÍNEA NUEVA) Le dice al panel que lea el JSON
            panelAjustes.cargarConfiguracionActual(); 
            
            // 2. Muestra el panel (ahora actualizado)
            cardLayout.show(contentPanel, "ajustes");
            
            // 3. Oculta el panel de juego
            panelJuego.onPanelOcultado();
        });
        // --- (FIN DE LA CORRECCIÓN) ---

        return sidebar;
    }

    /**
     * (TU MÉTODO ORIGINAL) Helper para crear botones de la sidebar.
     */
    private JButton crearBoton(String rutaImagen) {
        JButton boton = new JButton();
        boton.setPreferredSize(new Dimension(174, 46));
        boton.setMaximumSize(new Dimension(174, 46));
        boton.setMinimumSize(new Dimension(174, 46));
        boton.setBackground(new Color(20, 20, 20));
        boton.setFocusPainted(false);
        boton.setBorder(new TransparentRoundedBorder(15)); // (Usa la clase externa)

        try {
            ImageIcon icono = new ImageIcon(getClass().getResource(rutaImagen));
            boton.setIcon(icono);
        } catch (Exception e) {
            System.err.println("No se pudo cargar la imagen: " + rutaImagen);
            boton.setText(rutaImagen.substring(5, rutaImagen.indexOf('.'))); // Fallback
            boton.setForeground(letra);
        }
        
        boton.setText("");
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return boton;
    }

    private ImageIcon escalarImagen(ImageIcon iconoOriginal, int ancho, int alto) {
        Image imagenOriginal = iconoOriginal.getImage();
        Image imagenEscalada = imagenOriginal.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        return new ImageIcon(imagenEscalada);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                float tamañoBase = 13f;
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
            new NTI();
        });
    }
}