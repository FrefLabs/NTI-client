package juego;

import NTI.Modelo; // Importa Modelo del paquete principal

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * (CORREGIDO) Esta clase es un JPanel que contiene
 * el juego (izquierda) y las reglas/ranking (derecha).
 * Está diseñada para ser una "tarjeta" en un CardLayout.
 */
public class PanelJuego extends JPanel {

    // --- Clases de Lógica ---
    private Juego juego;
    private Modelo modelo; 

    // --- Componentes de Navegación ---
    private CardLayout layoutInterno;
    private JPanel panelInterno;
    private JButton btnAccionIzquierdo; 
    private JButton btnAccionDerecho;   
    private JPanel panelBotonInferior; 
    
    // --- Componentes de Pantallas ---
    private JTextField txtNombre; 
    private JLabel lblError; 

    // --- Componentes Endless ---
    private JPanel pantallaJuegoEndless; 
    private JLabel lblPuntosJugadorEndless, lblRondaEndless, lblTiempoEndless;
    private JLabel lblErrorIAEndless; 
    private JLabel lblFechaEndless, lblOpenHoyEndless, lblCierreAyerEndless, lblAltoAyerEndless, lblBajoAyerEndless, lblVolumenAyerEndless, lblCierreMananaEndless;
    private JTextField txtPrediccionEndless;
    private JButton btnConfirmarPrediccionEndless;
    
    // --- Componentes Tira y Afloje ---
    private JPanel pantallaJuegoNormal;
    private JLabel lblRonda, lblTiempo, lblPuntosJugador, lblPuntosIA;
    private JLabel lblFecha, lblOpenHoy, lblCierreAyer, lblAltoAyer, lblBajoAyer, lblVolumenAyer;
    private JLabel lblCierreManana; 
    private JTextField txtPrediccion;
    private JButton btnConfirmarPrediccion;
    
    // --- Timers (importante que sean variables de clase) ---
    private Timer timerEndless; 
    private Timer timer; 
    private Timer rankingTimer; // Timer del panel de Ranking

    // --- Estado de Timers ---
    private int tiempoRestanteEndless;
    private int tiempoRestante;

    // --- Colores y Formato ---
    private final Color bordeDorado = Color.decode("#D4AF37");
    private final Color fondo = Color.decode("#030614");
    private final Color fondoPanel = Color.decode("#060521");
    private final Color letra = Color.white;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    private final Dimension minBotonSize = new Dimension(140, 40);

    /**
     * Constructor del Panel de Juego.
     */
    public PanelJuego() {
        juego = new Juego();
        modelo = new Modelo(); // Esto usa NTI.Modelo

        // 1. Configura este propio JPanel (que es el contenedor principal)
        this.setLayout(new GridLayout(1, 2, 20, 0)); 
        this.setBackground(fondo);
        this.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // 2. Crea y añade los componentes directamente
        JPanel panelJuegoIzquierda = crearPanelIzquierdaConFlujoInterno();
        JPanel panelDerecho = crearPanelDerechoDividido();
        
        this.add(panelJuegoIzquierda);
        this.add(panelDerecho);
    }

    // -------------------------------------------------------------------
    // MÉTODOS DE CICLO DE VIDA (para el sistema grande)
    // -------------------------------------------------------------------

    public void onPanelMostrado() {
        if (rankingTimer != null && !rankingTimer.isRunning()) {
            rankingTimer.start();
        }
    }

    public void onPanelOcultado() {
        if (timer != null) timer.stop();
        if (timerEndless != null) timerEndless.stop();
        if (rankingTimer != null) rankingTimer.stop();

        layoutInterno.show(panelInterno, "inicio");
        btnAccionIzquierdo.setText("JUGAR");
        btnAccionDerecho.setVisible(false);
        panelBotonInferior.setVisible(true); 
    }

    // -------------------------------
    // (ELIMINADO) Ya no hay crearSidebar()
    // -------------------------------

    // -------------------------------
    // (ELIMINADO) Ya no hay crearPanelJuegoYReglas()
    // -------------------------------

    // -------------------------------
    // PANEL DERECHO CON REGLAS Y RANKING
    // -------------------------------
    private JPanel crearPanelDerechoDividido() {
        JPanel panelDerecho = new JPanel(new GridBagLayout());
        panelDerecho.setOpaque(false); 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        
        JPanel panelReglas = crearPanelReglas();
        JPanel panelRanking = crearPanelRanking();
        panelReglas.setPreferredSize(new Dimension(0, 0));
        panelRanking.setPreferredSize(new Dimension(0, 0));

        gbc.gridy = 0;
        gbc.weighty = 0.66; 
        gbc.insets = new Insets(0, 0, 20, 0); 
        panelDerecho.add(panelReglas, gbc);

        gbc.gridy = 1;
        gbc.weighty = 0.33; 
        gbc.insets = new Insets(0, 0, 0, 0); 
        panelDerecho.add(panelRanking, gbc);

        return panelDerecho;
    }

    
    // -------------------------------
    // PANEL IZQUIERDO CON FLUJO INTERNO
    // -------------------------------
    private JPanel crearPanelIzquierdaConFlujoInterno() {
        JPanel panelContenedor = new JPanel(new BorderLayout()); 
        panelContenedor.setBackground(fondoPanel);
        
        // (IMPORTANTE) Esta línea asume que tu clase NTI.RoundedBorder es 'public static'
        // Si no lo es, cámbiala a 'public static' en NTI.java
        // o descomenta la línea de fallback.
        Border bordeRedondeado = new NTI.NTI.RoundedBorder(25, bordeDorado, 2);
        // Border bordeRedondeado = BorderFactory.createLineBorder(bordeDorado, 2); // Fallback
        
        Border padding = new EmptyBorder(10, 10, 10, 10); 
        panelContenedor.setBorder(new CompoundBorder(bordeRedondeado, padding));

        // --- 1. Panel de Título (Fijo) ---
        JPanel panelTitulo = new JPanel();
        panelTitulo.setBackground(fondoPanel);
        JLabel lblTitulo = new JLabel("N.T.I - No Tengo Idea", SwingConstants.CENTER);
        lblTitulo.setForeground(bordeDorado);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20)); 
        lblTitulo.setBorder(new EmptyBorder(10, 0, 10, 0)); 
        panelTitulo.add(lblTitulo);


        // --- 2. Panel Central (CardLayout) ---
        this.layoutInterno = new CardLayout();
        this.panelInterno = new JPanel(layoutInterno);
        panelInterno.setBackground(fondoPanel);

        // 1️⃣ Pantalla inicial (VACÍA)
        JPanel pantallaInicio = new JPanel(new BorderLayout());
        pantallaInicio.setBackground(fondoPanel);

        // 2️⃣ Pantalla para ingresar nombre
        JPanel pantallaNombre = new JPanel(new GridBagLayout());
        pantallaNombre.setBackground(fondoPanel);
        JLabel lblPideNombre = new JLabel("Ingrese su nombre:");
        lblPideNombre.setForeground(letra);
        lblPideNombre.setFont(new Font("Arial", Font.BOLD, 16));
        this.txtNombre = new JTextField(12); 
        txtNombre.setBackground(fondo);
        txtNombre.setForeground(letra);
        txtNombre.setCaretColor(letra);
        txtNombre.setBorder(BorderFactory.createLineBorder(bordeDorado));
        JButton btnContinuar = new JButton("Continuar");
        btnContinuar.setBackground(bordeDorado);
        btnContinuar.setForeground(fondo);
        this.lblError = new JLabel(""); 
        lblError.setForeground(Color.RED);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 0; pantallaNombre.add(lblPideNombre, gbc);
        gbc.gridy = 1; pantallaNombre.add(txtNombre, gbc);
        gbc.gridy = 2; pantallaNombre.add(btnContinuar, gbc);
        gbc.gridy = 3; pantallaNombre.add(lblError, gbc);

        // 3️⃣ Pantalla de selección de modo
        JPanel pantallaModo = new JPanel(new GridBagLayout());
        pantallaModo.setBackground(fondoPanel);
        JLabel lblModo = new JLabel("Seleccione el modo de juego:");
        lblModo.setForeground(letra);
        lblModo.setFont(new Font("Arial", Font.BOLD, 16));
        JButton btnNormal = new JButton("Tira y Afloje"); 
        JButton btnEndless = new JButton("Endless");
        for (JButton b : new JButton[]{btnNormal, btnEndless}) {
            b.setBackground(bordeDorado);
            b.setForeground(fondo);
            b.setFocusPainted(false);
            b.setFont(new Font("Arial", Font.BOLD, 14));
        }
        Dimension sizeNormal = btnNormal.getPreferredSize();
        Dimension sizeEndless = btnEndless.getPreferredSize();
        int maxWidth = Math.max(sizeNormal.width, sizeEndless.width);
        int maxHeight = Math.max(sizeNormal.height, sizeEndless.height);
        Dimension uniformSize = new Dimension(maxWidth, maxHeight);
        btnNormal.setPreferredSize(uniformSize);
        btnEndless.setPreferredSize(uniformSize);
        
        gbc.gridy = 0; gbc.gridx = 0; gbc.insets = new Insets(10, 10, 10, 10); // Reset insets
        pantallaModo.add(lblModo, gbc);
        gbc.gridy = 1; 
        pantallaModo.add(btnNormal, gbc);
        gbc.gridy = 2; 
        pantallaModo.add(btnEndless, gbc);

        // 4️⃣ Pantalla de Juego Normal
        this.pantallaJuegoNormal = crearPantallaJuegoNormal();
        
        // 5️⃣ Pantalla de Juego Endless
        this.pantallaJuegoEndless = crearPantallaJuegoEndless();


        // --- 3. Panel de Botón Inferior ---
        this.panelBotonInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5)); 
        panelBotonInferior.setBackground(fondoPanel);
        
        this.btnAccionIzquierdo = new JButton("JUGAR"); 
        btnAccionIzquierdo.setBackground(bordeDorado);
        btnAccionIzquierdo.setForeground(fondo);
        btnAccionIzquierdo.setFont(new Font("Arial", Font.BOLD, 16));
        btnAccionIzquierdo.setPreferredSize(minBotonSize); 
        panelBotonInferior.add(btnAccionIzquierdo);
        
        this.btnAccionDerecho = new JButton("Salir"); 
        btnAccionDerecho.setBackground(bordeDorado); 
        btnAccionDerecho.setForeground(fondo);       
        btnAccionDerecho.setFont(new Font("Arial", Font.BOLD, 16));
        btnAccionDerecho.setPreferredSize(minBotonSize); 
        panelBotonInferior.add(btnAccionDerecho);
        
        btnAccionDerecho.setVisible(false);
        panelBotonInferior.setVisible(true);


        // --- Acciones de Botones ---
        
        btnContinuar.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            if (NTI.Formato.validarNombre(nombre)) {
                juego.ingresarNombre(nombre); 
                juego.setPuntos(); 
                layoutInterno.show(panelInterno, "modo");
                lblError.setText("");
                btnAccionIzquierdo.setText("Volver Atras"); 
                btnAccionDerecho.setVisible(false);
            } else {
                lblError.setText("Nombre inválido (solo letras, 3-10 caracteres)");
            }
        });

        btnNormal.addActionListener(e -> {
            juego.ingresarModo("Normal");
            juego.setPuntos(); 
            iniciarNuevaRonda(); 
            layoutInterno.show(panelInterno, "juegoNormal"); 
            btnAccionIzquierdo.setText("Reiniciar"); 
            btnAccionDerecho.setText("Salir");
            btnAccionDerecho.setVisible(true); 
            igualarTamañoBotonesMin(btnAccionIzquierdo, btnAccionDerecho);
        });

        btnEndless.addActionListener(e -> {
            juego.ingresarModo("Endless");
            juego.setPuntos(); 
            iniciarNuevaRondaEndless(); 
            layoutInterno.show(panelInterno, "juegoEndless");
            btnAccionIzquierdo.setText("Reiniciar"); 
            btnAccionDerecho.setText("Salir");
            btnAccionDerecho.setVisible(true); 
            igualarTamañoBotonesMin(btnAccionIzquierdo, btnAccionDerecho);
        });
        
        btnAccionIzquierdo.addActionListener(e -> {
            String textoBoton = btnAccionIzquierdo.getText();
            
            switch (textoBoton) {
                case "JUGAR": 
                    layoutInterno.show(panelInterno, "nombre");
                    btnAccionIzquierdo.setText("Volver Atras");
                    btnAccionDerecho.setVisible(false);
                    break;
                    
                case "Volver Atras":
                    Component visibleComponent = null;
                    for (Component comp : panelInterno.getComponents()) {
                        if (comp.isVisible()) {
                            visibleComponent = comp;
                            break;
                        }
                    }

                    if (visibleComponent == pantallaModo) {
                        layoutInterno.show(panelInterno, "nombre");
                    
                    } else if (visibleComponent == pantallaNombre) {
                        layoutInterno.show(panelInterno, "inicio");
                        btnAccionIzquierdo.setText("JUGAR"); 
                        btnAccionDerecho.setVisible(false); 
                    }
                    break;
                    
                case "Reiniciar": 
                    if (juego.getModoJuego().equals("Normal")) {
                        if (timer != null) timer.stop();
                        juego.setPuntos();
                        iniciarNuevaRonda();
                    } else {
                        if (timerEndless != null) timerEndless.stop();
                        juego.setPuntos();
                        iniciarNuevaRondaEndless();
                        btnAccionIzquierdo.setText("Reiniciar"); 
                    }
                    break;
                    
                case "Retirarse": 
                    if (timerEndless != null) timerEndless.stop();
                    
                    JOptionPane.showMessageDialog(this, 
                        "¡Felicidades! Te retiras con " + juego.getPuntosJugador() + " puntos.",
                        "Partida Finalizada", JOptionPane.INFORMATION_MESSAGE);
                    
                    layoutInterno.show(panelInterno, "inicio");
                    txtNombre.setText(""); 
                    lblError.setText("");  
                    btnAccionIzquierdo.setText("JUGAR");
                    btnAccionDerecho.setVisible(false); 
                    break;
            }
            igualarTamañoBotonesMin(btnAccionIzquierdo, btnAccionDerecho);
        });

        btnAccionDerecho.addActionListener(e -> {
            // "Salir" siempre te lleva a la pantalla de selección de MODO
            
            if (juego.getModoJuego().equals("Normal")) {
                if (timer != null) timer.stop();
            } else {
                if (timerEndless != null) timerEndless.stop();
            }
            
            layoutInterno.show(panelInterno, "modo");
            
            btnAccionIzquierdo.setText("Volver Atras");
            btnAccionDerecho.setVisible(false);
            igualarTamañoBotonesMin(btnAccionIzquierdo, btnAccionDerecho);
        });


        // Añadir pantallas al layout interno
        panelInterno.add(pantallaInicio, "inicio");
        panelInterno.add(pantallaNombre, "nombre");
        panelInterno.add(pantallaModo, "modo");
        panelInterno.add(pantallaJuegoNormal, "juegoNormal"); 
        panelInterno.add(pantallaJuegoEndless, "juegoEndless"); 

        layoutInterno.show(panelInterno, "inicio"); 

        panelContenedor.add(panelTitulo, BorderLayout.NORTH);
        panelContenedor.add(panelInterno, BorderLayout.CENTER);
        panelContenedor.add(panelBotonInferior, BorderLayout.SOUTH); 

        return panelContenedor;
    }

    // -------------------------------
    // PANEL DERECHO: REGLAS
    // -------------------------------
    private JPanel crearPanelReglas() {
        JPanel panelReglas = new JPanel(new BorderLayout());
        panelReglas.setBackground(fondoPanel);
        Border bordeRedondeado = new NTI.NTI.RoundedBorder(25, bordeDorado, 2);
        panelReglas.setBorder(bordeRedondeado);

        JLabel lblTituloReglas = new JLabel("¿CÓMO SE JUEGA?", SwingConstants.CENTER); 
        lblTituloReglas.setFont(new Font("Arial", Font.BOLD, 18));
        lblTituloReglas.setForeground(letra);
        lblTituloReglas.setBorder(new EmptyBorder(20, 20, 10, 20)); 

        JTextArea txtReglas = new JTextArea();
        txtReglas.setEditable(false);
        txtReglas.setWrapStyleWord(true);
        txtReglas.setLineWrap(true);
        txtReglas.setForeground(letra);
        txtReglas.setBackground(fondoPanel);
        txtReglas.setFont(new Font("Arial", Font.PLAIN, 14));
        txtReglas.setText(
                "El puntaje se mide en centavos ($0.01 = 1 punto).\n\n"
                + "Tu objetivo es predecir el CIERRE de HOY.\n\n"
                + "PISTAS:\n"
                + "• Datos de AYER (Cierre, Alto, Bajo, Volumen)\n"
                + "• Datos de HOY (Open)\n"
                + "• Datos de MAÑANA (Cierre)\n\n"
                + "MODO TIRA Y AFLOJE (vs IA):\n"
                + "Ambos empiezan con 1000 puntos. Ganas el error de la IA, o pierdes tu propio error.\n"
                + "Pierdes si llegas a 0 puntos.\n"
                + "Bonus por dirección: +10 puntos.\n\n"
                + "MODO ENDLESS (Arcade):\n" 
                + "Empiezas con 1000 puntos. La IA juega como referencia.\n"
                + "Ganas o pierdes puntos según la lógica de Tira y Afloje.\n"
                + "¡Pierdes automáticamente si tu puntaje llega a 0!\n" 
                + "Bonus por dirección: +10 puntos.\n"
                + "Si superas los 1000 puntos puedes 'Retirarte'. Si no, puedes 'Reiniciar'."
        );
        txtReglas.setCaretPosition(0); 

        JScrollPane scroll = new JScrollPane(txtReglas);
        scroll.setBorder(new EmptyBorder(0, 15, 15, 15)); 
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panelReglas.add(lblTituloReglas, BorderLayout.NORTH);
        panelReglas.add(scroll, BorderLayout.CENTER);

        return panelReglas;
    }

    // -------------------------------
    // PANEL DERECHO: RANKING
    // -------------------------------
    private JPanel crearPanelRanking() {
        JPanel panelRanking = new JPanel(new BorderLayout());
        panelRanking.setBackground(fondoPanel);
        
        Border bordeRedondeado = new NTI.NTI.RoundedBorder(25, bordeDorado, 2);
        Border padding = new EmptyBorder(10, 15, 10, 15);
        panelRanking.setBorder(new CompoundBorder(bordeRedondeado, padding));

        JLabel lblTituloRanking = new JLabel("RANKING", SwingConstants.CENTER);
        lblTituloRanking.setFont(new Font("Arial", Font.BOLD, 18));
        lblTituloRanking.setForeground(letra);
        lblTituloRanking.setBorder(new EmptyBorder(10, 0, 10, 0));
        panelRanking.add(lblTituloRanking, BorderLayout.NORTH);

        CardLayout rankingLayout = new CardLayout();
        JPanel panelTarjetas = new JPanel(rankingLayout);
        panelTarjetas.setOpaque(false); 

        String textoTyA = " -----------------------------------\n"
                + " 1. NICO     (AAPL)    15 Rondas\n"
                + " 2. GASTY    (MSFT)    18 Rondas\n"
                + " 3. LAUTI    (GOOG)    22 Rondas\n"
                + " 4. ...\n"
                + " 5. ...";
        JPanel tarjetaTyA = crearTarjetaRanking("MODO TIRA Y AFLOJE (Menos Rondas)", textoTyA);

        String textoEndless = " -----------------------------------\n"
                + " 1. GASTY    (TSLA)    1580 Pts\n" 
                + " 2. NICO     (AAPL)    1230 Pts\n"
                + " 3. ...\n"
                + " 4. ...\n"
                + " 5. ...";
        JPanel tarjetaEndless = crearTarjetaRanking("MODO ENDLESS (Más Puntaje)", textoEndless); 

        panelTarjetas.add(tarjetaTyA, "TyA");
        panelTarjetas.add(tarjetaEndless, "Endless");
        panelRanking.add(panelTarjetas, BorderLayout.CENTER);

        this.rankingTimer = new Timer(15000, e -> {
            rankingLayout.next(panelTarjetas); 
        });
        this.rankingTimer.setInitialDelay(15000); 

        return panelRanking;
    }

    // -------------------------------
    // MÉTODO HELPER PARA CREAR TARJETA RANKING
    // -------------------------------
    private JPanel crearTarjetaRanking(String titulo, String textoRanking) {
        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setOpaque(false); 
        JLabel lblSubtitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Arial", Font.BOLD, 14));
        lblSubtitulo.setForeground(bordeDorado); 
        lblSubtitulo.setBorder(new EmptyBorder(5, 10, 5, 10));
        tarjeta.add(lblSubtitulo, BorderLayout.NORTH);
        JTextArea txtRanking = new JTextArea();
        txtRanking.setEditable(false);
        txtRanking.setForeground(letra);
        txtRanking.setOpaque(false); 
        txtRanking.setFont(new Font("Monospaced", Font.PLAIN, 14));
        txtRanking.setText(textoRanking);
        txtRanking.setBorder(new EmptyBorder(0, 15, 15, 15)); 
        tarjeta.add(txtRanking, BorderLayout.CENTER);
        return tarjeta;
    }


    // -------------------------------
    // CREA EL PANEL DE JUEGO "TIRA Y AFLOJE"
    // -------------------------------
    private JPanel crearPantallaJuegoNormal() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(fondoPanel);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10); 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Fila 0 a 6: Labels de datos
        lblRonda = new JLabel("Ronda: 1");
        lblRonda.setForeground(letra);
        lblRonda.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0; panel.add(lblRonda, gbc);
        lblTiempo = new JLabel("Tiempo: 30");
        lblTiempo.setForeground(bordeDorado);
        lblTiempo.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 1; gbc.gridy = 0; panel.add(lblTiempo, gbc);
        lblPuntosJugador = new JLabel("Tus Puntos: 1000");
        lblPuntosJugador.setForeground(Color.CYAN);
        lblPuntosJugador.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 1; panel.add(lblPuntosJugador, gbc);
        lblPuntosIA = new JLabel("Puntos IA: 1000");
        lblPuntosIA.setForeground(Color.PINK);
        lblPuntosIA.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 1; gbc.gridy = 1; panel.add(lblPuntosIA, gbc);
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2; gbc.insets = new Insets(10, 0, 10, 0);
        panel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1; gbc.insets = new Insets(8, 10, 8, 10); // Reset
        lblFecha = new JLabel("Prediciendo Cierre para: 2025-10-30"); 
        lblFecha.setForeground(letra);
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(lblFecha, gbc);
        gbc.gridwidth = 1;
        lblCierreAyer = new JLabel("Cierre Ayer: $149.80");
        lblCierreAyer.setForeground(letra);
        gbc.gridy = 4; gbc.gridx = 0; panel.add(lblCierreAyer, gbc);
        lblCierreManana = new JLabel("Cierre Mañana: $151.00"); 
        lblCierreManana.setForeground(letra);
        gbc.gridx = 1; panel.add(lblCierreManana, gbc);
        lblOpenHoy = new JLabel("Open Hoy: $150.20");
        lblOpenHoy.setForeground(Color.WHITE); 
        gbc.gridy = 5; gbc.gridx = 0; panel.add(lblOpenHoy, gbc);
        lblVolumenAyer = new JLabel("Volumen Ayer: 5,123,456");
        lblVolumenAyer.setForeground(letra);
        gbc.gridx = 1; panel.add(lblVolumenAyer, gbc);
        lblAltoAyer = new JLabel("Alto Ayer: $152.10");
        lblAltoAyer.setForeground(letra);
        gbc.gridy = 6; gbc.gridx = 0; panel.add(lblAltoAyer, gbc);
        lblBajoAyer = new JLabel("Bajo Ayer: $148.50");
        lblBajoAyer.setForeground(letra);
        gbc.gridx = 1; panel.add(lblBajoAyer, gbc);
        
        // Fila 7: Input de predicción
        JLabel lblTuPrediccion = new JLabel("Tu Predicción (Cierre Hoy):"); 
        lblTuPrediccion.setForeground(bordeDorado);
        gbc.gridy = 7; gbc.gridx = 0; gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(20, 10, 20, 5);
        panel.add(lblTuPrediccion, gbc);
        
        txtPrediccion = new JTextField(10);
        txtPrediccion.setBackground(fondo);
        txtPrediccion.setForeground(letra);
        txtPrediccion.setCaretColor(letra);
        txtPrediccion.setBorder(BorderFactory.createLineBorder(bordeDorado));
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(20, 5, 20, 10);
        panel.add(txtPrediccion, gbc);
        gbc.insets = new Insets(8, 10, 8, 10); // Reset
        
        // Fila 8: Botón Confirmar
        btnConfirmarPrediccion = new JButton("Confirmar");
        btnConfirmarPrediccion.setBackground(bordeDorado);
        btnConfirmarPrediccion.setForeground(fondo);
        gbc.gridy = 8; gbc.gridx = 0; gbc.gridwidth = 2; 
        gbc.fill = GridBagConstraints.NONE; 
        gbc.anchor = GridBagConstraints.CENTER; 
        panel.add(btnConfirmarPrediccion, gbc);
        
        // --- Acciones de botones internos ---
        
        btnConfirmarPrediccion.addActionListener(e -> {
            if (timer.isRunning()) {
                procesarRonda();
            }
        });
        
        this.timer = new Timer(1000, e -> {
            tiempoRestante--;
            lblTiempo.setText("Tiempo: " + tiempoRestante);
            if (tiempoRestante <= 0) {
                procesarRonda(); 
            }
        });
        return panel;
    }

    
    // -------------------------------
    // CREA EL PANEL DE JUEGO "ENDLESS"
    // -------------------------------
    private JPanel crearPantallaJuegoEndless() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(fondoPanel);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10); 
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0: Ronda y Tiempo
        lblRondaEndless = new JLabel("Ronda: 1");
        lblRondaEndless.setForeground(letra);
        lblRondaEndless.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0; panel.add(lblRondaEndless, gbc);

        lblTiempoEndless = new JLabel("Tiempo: 30");
        lblTiempoEndless.setForeground(bordeDorado);
        lblTiempoEndless.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 1; gbc.gridy = 0; panel.add(lblTiempoEndless, gbc);

        // Fila 1: Puntos Jugador y Error IA (Referencia)
        lblPuntosJugadorEndless = new JLabel("Tus Puntos: 1000");
        lblPuntosJugadorEndless.setForeground(Color.CYAN);
        lblPuntosJugadorEndless.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 1; panel.add(lblPuntosJugadorEndless, gbc);

        lblErrorIAEndless = new JLabel("Error IA: ---"); 
        lblErrorIAEndless.setForeground(Color.PINK);
        lblErrorIAEndless.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 1; gbc.gridy = 1; panel.add(lblErrorIAEndless, gbc);
        
        // Fila 2: Separador
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2; gbc.insets = new Insets(10, 0, 10, 0);
        panel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1; gbc.insets = new Insets(8, 10, 8, 10); // Reset

        // Fila 3 a 6: Labels de datos
        lblFechaEndless = new JLabel("Prediciendo Cierre para: 2025-10-30"); 
        lblFechaEndless.setForeground(letra);
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(lblFechaEndless, gbc);
        gbc.gridwidth = 1;
        lblCierreAyerEndless = new JLabel("Cierre Ayer: $149.80");
        lblCierreAyerEndless.setForeground(letra);
        gbc.gridy = 4; gbc.gridx = 0; panel.add(lblCierreAyerEndless, gbc);
        lblCierreMananaEndless = new JLabel("Cierre Mañana: $151.00"); 
        lblCierreMananaEndless.setForeground(letra);
        gbc.gridx = 1; panel.add(lblCierreMananaEndless, gbc);
        lblOpenHoyEndless = new JLabel("Open Hoy: $150.20");
        lblOpenHoyEndless.setForeground(Color.WHITE); 
        gbc.gridy = 5; gbc.gridx = 0; panel.add(lblOpenHoyEndless, gbc);
        lblVolumenAyerEndless = new JLabel("Volumen Ayer: 5,123,456");
        lblVolumenAyerEndless.setForeground(letra);
        gbc.gridx = 1; panel.add(lblVolumenAyerEndless, gbc);
        lblAltoAyerEndless = new JLabel("Alto Ayer: $152.10");
        lblAltoAyerEndless.setForeground(letra);
        gbc.gridy = 6; gbc.gridx = 0; panel.add(lblAltoAyerEndless, gbc);
        lblBajoAyerEndless = new JLabel("Bajo Ayer: $148.50");
        lblBajoAyerEndless.setForeground(letra);
        gbc.gridx = 1; panel.add(lblBajoAyerEndless, gbc);
        
        // Fila 7: Input de predicción
        JLabel lblTuPrediccion = new JLabel("Tu Predicción (Cierre Hoy):"); 
        lblTuPrediccion.setForeground(bordeDorado);
        gbc.gridy = 7; gbc.gridx = 0; gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(20, 10, 20, 5);
        panel.add(lblTuPrediccion, gbc);
        
        txtPrediccionEndless = new JTextField(10);
        txtPrediccionEndless.setBackground(fondo);
        txtPrediccionEndless.setForeground(letra);
        txtPrediccionEndless.setCaretColor(letra);
        txtPrediccionEndless.setBorder(BorderFactory.createLineBorder(bordeDorado));
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(20, 5, 20, 10);
        panel.add(txtPrediccionEndless, gbc);
        gbc.insets = new Insets(8, 10, 8, 10); // Reset
        
        // Fila 8: Botón Confirmar
        btnConfirmarPrediccionEndless = new JButton("Confirmar");
        btnConfirmarPrediccionEndless.setBackground(bordeDorado);
        btnConfirmarPrediccionEndless.setForeground(fondo);
        gbc.gridy = 8; gbc.gridx = 0; gbc.gridwidth = 2; 
        gbc.fill = GridBagConstraints.NONE; 
        gbc.anchor = GridBagConstraints.CENTER; 
        panel.add(btnConfirmarPrediccionEndless, gbc);
        
        // --- Acciones de botones internos ---
        
        btnConfirmarPrediccionEndless.addActionListener(e -> {
            if (timerEndless.isRunning()) {
                procesarRondaEndless();
            }
        });
        
        this.timerEndless = new Timer(1000, e -> {
            tiempoRestanteEndless--;
            lblTiempoEndless.setText("Tiempo: " + tiempoRestanteEndless);
            if (tiempoRestanteEndless <= 0) {
                procesarRondaEndless(); 
            }
        });

        return panel;
    }


    // -------------------------------
    // LÓGICA DE JUEGO (TIRA Y AFLOJE)
    // -------------------------------
    private void iniciarNuevaRonda() {
        juego.generarNuevaRonda(); 
        actualizarLabelsDatos();   
        
        int ronda = juego.getRondaActual();
        
        if (ronda >= 15) {
            this.tiempoRestante = 10;
        } else if (ronda >= 10) {
            this.tiempoRestante = 15;
        } else if (ronda >= 5) {
            this.tiempoRestante = 20;
        } else {
            this.tiempoRestante = 30; 
        }

        lblTiempo.setText("Tiempo: " + tiempoRestante);
        txtPrediccion.setText(""); 
        btnConfirmarPrediccion.setEnabled(true); 
        txtPrediccion.setEnabled(true); 
        timer.start(); 
    }

    private void procesarRonda() {
        if (!timer.isRunning()) return; 
        
        timer.stop(); 
        btnConfirmarPrediccion.setEnabled(false); 
        txtPrediccion.setEnabled(false); 

        double prediccionJugador;
        try {
            prediccionJugador = Double.parseDouble(txtPrediccion.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            prediccionJugador = 0.0; 
        }

        double prediccionIA = modelo.hacerPrediccion(juego.getCierreAyer(), juego.getCierreManana());
        double cierreHoy = juego.getCierreHoy(); 

        int errorJugador = (int) (Math.abs(prediccionJugador - cierreHoy) * 100);
        int errorIA = (int) (Math.abs(prediccionIA - cierreHoy) * 100);

        String resumenRonda = juego.calcularResultadoRonda(errorJugador, errorIA, prediccionJugador);
        
        lblPuntosJugador.setText("Tus Puntos: " + juego.getPuntosJugador());
        lblPuntosIA.setText("Puntos IA: " + juego.getPuntosIA());
        
        String mensaje = String.format(
            "--- RONDA %d FINALIZADA ---\n\n" +
            "El Precio de Cierre de HOY fue: %s\n\n" + 
            "Tu Predicción: %s (Error: %d puntos)\n" +
            "Predicción IA: %s (Error: %d puntos)\n\n" +
            "%s", 
            juego.getRondaActual(), 
            currencyFormat.format(cierreHoy), 
            currencyFormat.format(prediccionJugador), errorJugador,
            currencyFormat.format(prediccionIA), errorIA,
            resumenRonda 
        );
        
        JOptionPane.showMessageDialog(this, mensaje, "Fin de Ronda", JOptionPane.INFORMATION_MESSAGE);

        if (juego.isJuegoTerminado()) {
            
            String ganadorFinal = juego.getGanadorFinal();
            String mensajeFinal = (ganadorFinal.equals(juego.getNombreJugador())) ?
                "¡FELICIDADES, GANASTE!\nHas dejado a la IA en 0 puntos." :
                "¡OH NO, PERDISTE!\nLa IA te ha quitado todos tus puntos.";
            
            JOptionPane.showMessageDialog(this, mensajeFinal, "Fin del Juego", JOptionPane.INFORMATION_MESSAGE);
            
            layoutInterno.show(panelInterno, "inicio");
            txtNombre.setText(""); 
            lblError.setText("");  
            btnAccionIzquierdo.setText("JUGAR");
            btnAccionDerecho.setVisible(false);

        } else {
            iniciarNuevaRonda();
        }
    }
    
    private void actualizarLabelsDatos() {
        NumberFormat volumeFormat = NumberFormat.getIntegerInstance(Locale.US);
        lblRonda.setText("Ronda: " + juego.getRondaActual()); 
        lblPuntosJugador.setText("Tus Puntos: " + juego.getPuntosJugador());
        lblPuntosIA.setText("Puntos IA: " + juego.getPuntosIA());
        lblFecha.setText("Prediciendo Cierre para: " + juego.getFecha());
        lblCierreAyer.setText("Cierre Ayer: " + currencyFormat.format(juego.getCierreAyer()));
        lblCierreManana.setText("Cierre Mañana: " + currencyFormat.format(juego.getCierreManana()));
        lblOpenHoy.setText("Open Hoy: " + currencyFormat.format(juego.getOpenHoy()));
        lblVolumenAyer.setText("Volumen Ayer: " + volumeFormat.format(juego.getVolumenAyer()));
        lblAltoAyer.setText("Alto Ayer: " + currencyFormat.format(juego.getAltoAyer()));
        lblBajoAyer.setText("Bajo Ayer: " + currencyFormat.format(juego.getBajoAyer()));
    }


    // -------------------------------
    // LÓGICA DE JUEGO (ENDLESS)
    // -------------------------------

    /** Inicia una nueva ronda en modo Endless */
    private void iniciarNuevaRondaEndless() {
        juego.generarNuevaRonda(); 
        actualizarLabelsDatosEndless();   
        
        int ronda = juego.getRondaActual();
        
        if (ronda >= 15) {
            this.tiempoRestanteEndless = 10;
        } else if (ronda >= 10) {
            this.tiempoRestanteEndless = 15;
        } else if (ronda >= 5) {
            this.tiempoRestanteEndless = 20;
        } else {
            this.tiempoRestanteEndless = 30; 
        }

        lblTiempoEndless.setText("Tiempo: " + tiempoRestanteEndless);
        txtPrediccionEndless.setText(""); 
        btnConfirmarPrediccionEndless.setEnabled(true); 
        txtPrediccionEndless.setEnabled(true); 
        timerEndless.start(); 
    }

    /** Procesa la ronda actual en modo Endless */
    private void procesarRondaEndless() {
        if (!timerEndless.isRunning()) return; 
        
        timerEndless.stop(); 
        btnConfirmarPrediccionEndless.setEnabled(false); 
        txtPrediccionEndless.setEnabled(false); 

        double prediccionJugador;
        try {
            prediccionJugador = Double.parseDouble(txtPrediccionEndless.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            prediccionJugador = 0.0; 
        }

        // La IA sigue jugando como referencia
        double prediccionIA = modelo.hacerPrediccion(juego.getCierreAyer(), juego.getCierreManana());
        double cierreHoy = juego.getCierreHoy(); 

        // Calcula errores
        int errorJugador = (int) (Math.abs(prediccionJugador - cierreHoy) * 100);
        int errorIA = (int) (Math.abs(prediccionIA - cierreHoy) * 100);

        // Llama al método de cálculo de Juego (que solo afecta al jugador)
        String resumenRonda = juego.calcularResultadoEndless(errorJugador, errorIA, prediccionJugador);
        
        // Actualiza puntaje del jugador y el error de referencia de la IA
        lblPuntosJugadorEndless.setText("Tus Puntos: " + juego.getPuntosJugador());
        lblErrorIAEndless.setText("Error IA: " + errorIA); 
        
        // --- (NUEVA LÓGICA DE FIN DE JUEGO) ---
        if (juego.getPuntosJugador() <= 0) {
            // El jugador perdió
            JOptionPane.showMessageDialog(this, 
                "¡GAME OVER!\nTe has quedado sin puntos.",
                "Fin del Juego", JOptionPane.INFORMATION_MESSAGE);
            
            // Resetear al menú principal
            layoutInterno.show(panelInterno, "inicio");
            txtNombre.setText(""); 
            lblError.setText("");  
            btnAccionIzquierdo.setText("JUGAR");
            btnAccionDerecho.setVisible(false);
            
        } else {
            // El juego continúa, mostrar resumen de ronda
            
            String mensaje = String.format(
                "--- RONDA %d FINALIZADA ---\n\n" +
                "El Precio de Cierre de HOY fue: %s\n\n" + 
                "Tu Predicción: %s (Error: %d puntos)\n" +
                "Predicción IA: %s (Error: %d puntos)\n\n" + 
                "%s", 
                juego.getRondaActual(), 
                currencyFormat.format(cierreHoy), 
                currencyFormat.format(prediccionJugador), errorJugador,
                currencyFormat.format(prediccionIA), errorIA, 
                resumenRonda 
            );
            
            JOptionPane.showMessageDialog(this, mensaje, "Fin de Ronda", JOptionPane.INFORMATION_MESSAGE);

            // Actualiza el texto del botón inferior
            if (juego.getPuntosJugador() > 1000) {
                btnAccionIzquierdo.setText("Retirarse");
            } else {
                btnAccionIzquierdo.setText("Reiniciar");
            }
            
            // Reajusta los tamaños
            igualarTamañoBotonesMin(btnAccionIzquierdo, btnAccionDerecho);

            // Inicia la siguiente ronda automáticamente
            iniciarNuevaRondaEndless();
        }
    }
    
    /** Actualiza todos los JLabels del panel de juego ENDLESS */
    private void actualizarLabelsDatosEndless() {
        NumberFormat volumeFormat = NumberFormat.getIntegerInstance(Locale.US);

        lblRondaEndless.setText("Ronda: " + juego.getRondaActual()); 
        lblPuntosJugadorEndless.setText("Tus Puntos: " + juego.getPuntosJugador());
        
        // Resetea el error de la IA al empezar la ronda
        lblErrorIAEndless.setText("Error IA: ---");
        
        lblFechaEndless.setText("Prediciendo Cierre para: " + juego.getFecha());
        lblCierreAyerEndless.setText("Cierre Ayer: " + currencyFormat.format(juego.getCierreAyer()));
        lblCierreMananaEndless.setText("Cierre Mañana: " + currencyFormat.format(juego.getCierreManana()));
        lblOpenHoyEndless.setText("Open Hoy: " + currencyFormat.format(juego.getOpenHoy()));
        lblVolumenAyerEndless.setText("Volumen Ayer: " + volumeFormat.format(juego.getVolumenAyer()));
        lblAltoAyerEndless.setText("Alto Ayer: " + currencyFormat.format(juego.getAltoAyer()));
        lblBajoAyerEndless.setText("Bajo Ayer: " + currencyFormat.format(juego.getBajoAyer()));
    }
    
    // -------------------------------
    // HELPER PARA IGUALAR TAMAÑO DE BOTONES
    // -------------------------------
    /**
     * Compara el tamaño preferido de dos botones con el tamaño mínimo
     * y aplica el tamaño más grande de los tres a ambos.
     */
    private void igualarTamañoBotonesMin(JButton b1, JButton b2) {
        Dimension size1 = b1.getPreferredSize();
        Dimension size2 = b2.getPreferredSize();
        
        // Compara el ancho preferido con el mínimo
        int maxWidth = Math.max(minBotonSize.width, Math.max(size1.width, size2.width));
        int maxHeight = Math.max(minBotonSize.height, Math.max(size1.height, size2.height));
        
        Dimension uniformSize = new Dimension(maxWidth, maxHeight);
        
        b1.setPreferredSize(uniformSize);
        b2.setPreferredSize(uniformSize);
    }
    
    // (La clase interna RoundedBorder fue eliminada de aquí)
}