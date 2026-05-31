package NTI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import juego.PanelJuego;
import NTI.PanelModelos;

public class NTI extends JFrame {

    Color bordeDorado = Color.decode("#D4AF37");
    Color fondo = Color.decode("#030614");
    Color fondoPanel = Color.decode("#060521");
    Color letra = Color.white;
    String bordedoradoStr = "rgb(" + bordeDorado.getRed() + "," + bordeDorado.getGreen() + "," + bordeDorado.getBlue() + ")";
    String letraStr = "rgb(" + letra.getRed() + "," + letra.getGreen() + "," + letra.getBlue() + ")";

    Empresa emp;
    Noticia not;
    public Entorno ent = new Entorno();
    Registro registro;
    Accion accion;
    Lectura lectura;
    Modelo modelo;
    
    public String estiloGraficaActual;

    public PanelInicio panelInicio;
    public PanelJuego panelJuego;
    public PanelModelos panelModelos;
    public PanelAjustes panelAjustes;
    public PanelHistorial panelHistorial; 

    public CardLayout cardLayout;
    public JPanel contentPanel;

    // Campos para botones del Sidebar
    private JButton btnInicio, btnModelos, btnHistorial, btnJuego, btnAjustes;
    
    // Campos para los iconos (10 en total)
    private ImageIcon iconoInicio, iconoInicioSel;
    private ImageIcon iconoModelos, iconoModelosSel;
    private ImageIcon iconoHistorial, iconoHistorialSel;
    private ImageIcon iconoJuego, iconoJuegoSel;
    private ImageIcon iconoAjustes, iconoAjustesSel;

    final DecimalFormat percentFormat = new DecimalFormat("0.00'%'");
    final DecimalFormat maeFormat = new DecimalFormat("'$'0.00");
    final DecimalFormat dfPercent = new DecimalFormat("0.00'%'");
    final DecimalFormat df8 = new DecimalFormat("0.00000000");
    final DecimalFormat df4 = new DecimalFormat("0.0000");

    public NTI() {
        setTitle("NeuroFref Trading Intelligence - 1.0");
        
        try {
            // Carga la imagen 'logo.png' que está en el MISMO paquete NTI
            java.net.URL imgUrl = getClass().getResource("/img/IconoAplicativo.png");
            
            if (imgUrl != null) {
                ImageIcon icon = new ImageIcon(imgUrl);
                
                // Establece la imagen como el ícono de la ventana
                this.setIconImage(icon.getImage()); 
                
            } else {
                // (Mensaje de error si no encuentra la imagen)
                System.err.println("No se pudo encontrar el logo.png. Asegúrate de que esté en el paquete 'NTI'.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1298, 763);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(fondo);

        ent = new Entorno();
        registro = new Registro();
        lectura = new Lectura();
        
        String[] config = ent.conseguirConfig();
        
        int idModeloActual = 29;
        try {
            idModeloActual = Integer.parseInt(config[3]);
        } catch (NumberFormatException e) {
            System.err.println("Error al leer IDModelo del config, usando default 29.");
        }
        
        this.estiloGraficaActual = config[4];
        
        System.out.println("Iniciando con Modelo ID: " + idModeloActual);

        accion = new Accion();
        // accion.simbolo will be properly set later by PanelInicio.cargarDatosIniciales()
        
        modelo = new Modelo(lectura);
        modelo.setIDModeloSeleccionado(idModeloActual); 

        emp = new Empresa();
        not = new Noticia();
        
        cargarIconosSidebar();
        
        JPanel sidebar = crearPanelSidebar();
        add(sidebar, BorderLayout.WEST);
        
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        add(contentPanel, BorderLayout.CENTER);

        panelInicio = new PanelInicio(this);
        panelModelos = new PanelModelos(this);
        panelAjustes = new PanelAjustes(this);
        panelHistorial = new PanelHistorial(this);
        panelJuego = new PanelJuego(this, registro, accion, lectura);

        contentPanel.add(panelInicio, "inicio");
        contentPanel.add(panelModelos, "modelos");
        contentPanel.add(panelAjustes, "ajustes");
        contentPanel.add(panelJuego, "juego");
        contentPanel.add(panelHistorial, "historial");

        panelInicio.cargarDatosIniciales(); 
        panelModelos.cargarDatosIniciales();
        
        actualizarBotonesSidebar("inicio");
        
        setVisible(true);
    }
    /**
     * Carga los 10 iconos del sidebar desde la carpeta /img/
     */
    private void cargarIconosSidebar() {
        iconoInicio = cargarIconoSidebarHelper("boton_inicio.png");
        iconoInicioSel = cargarIconoSidebarHelper("boton_inicio_sel.png");
        
        iconoModelos = cargarIconoSidebarHelper("boton_modelos.png");
        iconoModelosSel = cargarIconoSidebarHelper("boton_modelos_sel.png");
        
        iconoHistorial = cargarIconoSidebarHelper("boton_historial.png");
        iconoHistorialSel = cargarIconoSidebarHelper("boton_historial_sel.png");
        
        iconoJuego = cargarIconoSidebarHelper("boton_juego.png");
        iconoJuegoSel = cargarIconoSidebarHelper("boton_juego_sel.png");
        
        iconoAjustes = cargarIconoSidebarHelper("boton_ajustes.png");
        iconoAjustesSel = cargarIconoSidebarHelper("boton_ajustes_sel.png");
    }

    /**
     * Helper para cargar una imagen individual desde /img/
     */
    private ImageIcon cargarIconoSidebarHelper(String nombre) {
        try {
            java.net.URL imgUrl = getClass().getResource("/img/" + nombre);
            if (imgUrl != null) {
                return new ImageIcon(imgUrl);
            } else {
                System.err.println("No se pudo encontrar el ícono: /img/" + nombre);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error al cargar ícono /img/" + nombre + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Método público que actualiza todos los iconos del sidebar.
     */
    public void actualizarBotonesSidebar(String panelActivo) {
        btnInicio.setIcon(panelActivo.equals("inicio") ? iconoInicioSel : iconoInicio);
        btnModelos.setIcon(panelActivo.equals("modelos") ? iconoModelosSel : iconoModelos);
        btnHistorial.setIcon(panelActivo.equals("historial") ? iconoHistorialSel : iconoHistorial);
        btnJuego.setIcon(panelActivo.equals("juego") ? iconoJuegoSel : iconoJuego);
        btnAjustes.setIcon(panelActivo.equals("ajustes") ? iconoAjustesSel : iconoAjustes);
    }

    public void cambiarPanel(String nombrePanel) {
        cardLayout.show(contentPanel, nombrePanel);
        actualizarBotonesSidebar(nombrePanel);

        // Mover la lógica de onPanelOcultado/Mostrado aquí
        if (nombrePanel.equals("juego")) {
            panelJuego.onPanelMostrado();
        } else {
            panelJuego.onPanelOcultado();
        }
    }

    private JPanel crearPanelSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(200, getHeight()));
        sidebar.setLayout(new BorderLayout());
        sidebar.setBackground(new Color(10, 10, 10));

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

        JPanel panelCentral = new JPanel();
        panelCentral.setBackground(new Color(10, 10, 10));
        panelCentral.setLayout(new BoxLayout(panelCentral, BoxLayout.Y_AXIS));
        panelCentral.setBorder(new EmptyBorder(5, 20, 20, 20));

        // Instanciar los campos de clase (sin texto)
        btnInicio = new JButton();
        estilizarBotonSidebar(btnInicio); // Aplicar estilo de icono
        btnModelos = new JButton();
        estilizarBotonSidebar(btnModelos);
        btnHistorial = new JButton();
        estilizarBotonSidebar(btnHistorial);
        btnJuego = new JButton();
        estilizarBotonSidebar(btnJuego);

        panelCentral.add(btnInicio);
        panelCentral.add(Box.createRigidArea(new Dimension(0, 20)));
        panelCentral.add(btnModelos);
        panelCentral.add(Box.createRigidArea(new Dimension(0, 20)));
        panelCentral.add(btnHistorial);
        panelCentral.add(Box.createRigidArea(new Dimension(0, 20)));
        panelCentral.add(btnJuego);

        JPanel panelInferior = new JPanel();
        panelInferior.setBackground(new Color(10, 10, 10));
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));
        panelInferior.setBorder(new EmptyBorder(10, 20, 20, 20));

        btnAjustes = new JButton();
        estilizarBotonSidebar(btnAjustes);
        
        panelInferior.add(btnAjustes);

        sidebar.add(panelSuperior, BorderLayout.NORTH);
        sidebar.add(panelCentral, BorderLayout.CENTER);
        sidebar.add(panelInferior, BorderLayout.SOUTH);

        btnInicio.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                AudioManager.getInstance().playBotonSound();
            }
        });
        btnInicio.addActionListener(e -> {
            cambiarPanel("inicio");
        });

        btnModelos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                AudioManager.getInstance().playBotonSound();
            }
        });
        btnModelos.addActionListener(e -> {
            cambiarPanel("modelos");

            if (panelModelos.estaEnLista()) { // Revisa si está en la vista de lista
                panelModelos.cargarDatosIniciales();
            } else if (!panelModelos.estaEnDetalle()) { // Revisa si está en la vista de detalle
                panelModelos.mostrarListaYRecargar();
            }
        });

        btnHistorial.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                AudioManager.getInstance().playBotonSound();
            }
        });
        btnHistorial.addActionListener(e -> {
            cambiarPanel("historial");
            panelHistorial.cargarDatos(); 
        });

        btnJuego.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                AudioManager.getInstance().playBotonSound();
            }
        });
        btnJuego.addActionListener(e -> {
            cambiarPanel("juego");
        });

        btnAjustes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                AudioManager.getInstance().playBotonSound();
            }
        });
        btnAjustes.addActionListener(e -> {
            cambiarPanel("ajustes");
            panelAjustes.cargarConfiguracionActual(); 
        });

        return sidebar;
    }
    
    /**
     * Método para botones de ICONO
     */
    private void estilizarBotonSidebar(JButton boton) {
        // Quitar todo el estilo visual
        boton.setFocusPainted(false);
        boton.setBorder(null);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Mantener el tamaño fijo para que el layout no se rompa
        boton.setPreferredSize(new Dimension(174, 46));
        boton.setMaximumSize(new Dimension(174, 46));
        boton.setMinimumSize(new Dimension(174, 46));
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