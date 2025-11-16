package NTI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import juego.PanelJuego;

/**
 * (CLASE PRINCIPAL - CORREGIDA Y REFACTORIZADA)
 * Contiene el JFrame, la Sidebar original y el CardLayout.
 * La lógica de cada panel ha sido movida a su propia clase.
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
    
    // --- Paneles de Contenido (Refactorizados) ---
    private PanelJuego panelJuego;
    private PanelInicio panelInicio;
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

        // --- 1. Inicializar Lógica (Como en tu original) ---
        emp = new Empresa();
        not = new Noticia();
        ent = new Entorno();
        registro = new Registro();
        accion = new Accion();
        lectura = new Lectura();
        modelo = new Modelo(lectura);
        
        // --- 2. Crear Sidebar (Tu código original) ---
        JPanel sidebar = crearPanelSidebar();
        add(sidebar, BorderLayout.WEST);
        
        // --- 3. Crear Panel Contenedor (CardLayout) ---
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        add(contentPanel, BorderLayout.CENTER);

        // --- 4. Instanciar Paneles Refactorizados ---
        // (Pasamos 'this' (la instancia de NTI) para que accedan a la lógica)
        panelInicio = new PanelInicio(this);
        panelModelos = new PanelModelos(this);
        panelAjustes = new PanelAjustes(this);
        panelHistorial = new PanelHistorial(this);
        
        // (PanelJuego ya estaba limpio)
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
     * (TU MÉTODO ORIGINAL) Crea la sidebar con botones de imagen.
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
            // (La recarga de datos ahora la maneja el panel si es necesario)
            // panelInicio.cargarDatosIniciales(); (Opcional, si quieres recargar cada vez)
            panelJuego.onPanelOcultado();
        });

        btnModelos.addActionListener(e -> {
            cardLayout.show(contentPanel, "modelos");
            panelModelos.mostrarListaYRecargar(); // (Llama al método de PanelModelos)
            panelJuego.onPanelOcultado();
        });
        
        btnHistorial.addActionListener(e -> {
            cardLayout.show(contentPanel, "historial");
            panelHistorial.cargarDatos(); // (Llama al método de PanelHistorial)
            panelJuego.onPanelOcultado();
        });

        btnJuego.addActionListener(e -> {
            cardLayout.show(contentPanel, "juego");
            panelJuego.onPanelMostrado();
        });

        btnAjustes.addActionListener(e -> {
            cardLayout.show(contentPanel, "ajustes");
            panelJuego.onPanelOcultado();
        });

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

    /**
     * (TU MÉTODO ORIGINAL) Helper para escalar el logo.
     */
    private ImageIcon escalarImagen(ImageIcon iconoOriginal, int ancho, int alto) {
        Image imagenOriginal = iconoOriginal.getImage();
        Image imagenEscalada = imagenOriginal.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        return new ImageIcon(imagenEscalada);
    }
    
    /**
     * (TU MÉTODO ORIGINAL) Método Main.
     */
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