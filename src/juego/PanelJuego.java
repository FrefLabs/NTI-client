package juego;

import NTI.AudioManager;
import NTI.Modelo;
import NTI.Registro;
import NTI.Accion;
import NTI.Lectura;
import NTI.RoundedBorder;
import NTI.Formato;
import NTI.NTI;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.sound.sampled.*;
import java.net.URL;

public class PanelJuego extends JPanel {

    // --- Clases de Lógica ---
    private NTI nti; 
    private Juego juego;
    private Modelo modelo;
    private Registro registro;
    private Accion accion;
    private Lectura lectura;

    // --- Imagen de Fondo ---
    private BufferedImage fondoIzquierdo;
    private BufferedImage fondoDerecho;

    // --- Clips de Audio ---
    private Clip clipMenu;
    private Clip clipJuego;
    private Clip clipGanar;
    private Clip clipPerder;

    // --- Componentes ---
    private CardLayout layoutInterno;
    private JPanel panelInterno;
    private JButton btnAccionIzquierdo;
    private JButton btnAccionDerecho;
    private JPanel panelBotonInferior;
    private JTextField txtNombre;
    private JLabel lblError;
    private JPanel pantallaJuegoEndless;
    private JLabel lblPuntosJugadorEndless, lblRondaEndless, lblTiempoEndless;
    private JLabel lblErrorIAEndless;
    private JLabel lblFechaEndless, lblOpenHoyEndless, lblCierreAyerEndless, lblAltoAyerEndless, lblBajoAyerEndless, lblVolumenAyerEndless, lblCierreMananaEndless;
    private JTextField txtPrediccionEndless;
    private JButton btnConfirmarPrediccionEndless;
    private JPanel pantallaJuegoNormal;
    private JLabel lblRonda, lblTiempo, lblPuntosJugador, lblPuntosIA;
    private JLabel lblFecha, lblOpenHoy, lblCierreAyer, lblAltoAyer, lblBajoAyer, lblVolumenAyer;
    private JLabel lblCierreManana;
    private JTextField txtPrediccion;
    private JButton btnConfirmarPrediccion;
    private Timer timerEndless;
    private Timer timer;
    private Timer rankingTimer;
    private JTextArea txtRankingTyA;
    private JTextArea txtRankingEndless;
    private int tiempoRestanteEndless;
    private int tiempoRestante;
    
    // --- Colores y Formato ---
    private final Color bordeDorado = Color.decode("#D4AF37");
    private final Color fondo = Color.decode("#030614");
    private final Color fondoPanel = Color.decode("#060521");
    private final Color letra = Color.white;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    private final Dimension minBotonSize = new Dimension(140, 40);

    // --- Nuevas Constantes de Juego ---
    private final double INSTANT_LOSE_THRESHOLD = 0.25; // 25%
    private final int BASE_ERROR_MULTIPLIER = 5000;

    public PanelJuego(NTI nti, Registro registro, Accion accion, Lectura lectura) { 
        this.nti = nti; 
        this.registro = registro;
        this.accion = accion;
        this.lectura = lectura;
        
        juego = new Juego(lectura, registro);
        modelo = new Modelo(lectura);

        // --- Cargar y dividir la imagen de fondo ---
        try {
            BufferedImage originalImage = ImageIO.read(getClass().getResource("/img/fondo_juego.png"));
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            int midPoint = width / 2;
            
            this.fondoIzquierdo = originalImage.getSubimage(0, 0, midPoint, height);
            this.fondoDerecho = originalImage.getSubimage(midPoint, 0, width - midPoint, height);

        } catch (Exception e) {
            System.err.println("Error al cargar la imagen de fondo: /img/fondo_juego.png");
            e.printStackTrace();
            this.fondoIzquierdo = null;
            this.fondoDerecho = null;
        }

        // --- Cargar Audios ---
        clipMenu = cargarSonido("menu.wav");
        clipJuego = cargarSonido("juego.wav");
        clipGanar = cargarSonido("ganar.wav");
        clipPerder = cargarSonido("perder.wav");
        
        actualizarVolumenMusica();
        
        this.setLayout(new GridLayout(1, 2, 20, 0)); 
        this.setBackground(fondo);
        this.setBorder(new EmptyBorder(20, 10, 20, 10));
        
        JPanel panelJuegoIzquierda = new PanelJuegoIzquierdo();
        JPanel panelDerecho = new PanelJuegoDerecho();

        panelJuegoIzquierda.setLayout(new BorderLayout());
        panelDerecho.setLayout(new BorderLayout());

        panelJuegoIzquierda.setOpaque(false);
        panelDerecho.setOpaque(false);

        panelJuegoIzquierda.add(crearPanelIzquierdaContenido(), BorderLayout.CENTER);
        panelDerecho.add(crearPanelDerechoContenido(), BorderLayout.CENTER);

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
        actualizarRankings();
        reproducirSonido(clipMenu, true);
    }

    public void onPanelOcultado() {
        if (timer != null) timer.stop();
        if (timerEndless != null) timerEndless.stop();
        if (rankingTimer != null) rankingTimer.stop();
        
        pararTodosLosSonidos();

        layoutInterno.show(panelInterno, "inicio");
        btnAccionIzquierdo.setText("JUGAR");
        btnAccionDerecho.setVisible(false);
        panelBotonInferior.setVisible(true);
    }
    
    // -------------------------------------------------------------------
    // MÉTODOS DE GESTIÓN DE AUDIO
    // -------------------------------------------------------------------

    public void actualizarVolumenMusica() {
        int porcentaje = nti.ent.volumenMusica; 
        setVolumen(clipMenu, porcentaje);
        setVolumen(clipJuego, porcentaje);
        setVolumen(clipGanar, porcentaje);
        setVolumen(clipPerder, porcentaje);
    }
    
    private void setVolumen(Clip clip, int porcentaje) {
        if (clip == null) return;
        
        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();
            float range = max - min;
            
            float gain;
            if (porcentaje == 0) {
                gain = min; // Silencio total
            } else {
                // Mapeo logarítmico
                gain = (float) (min + (range * (Math.log10(1 + 9 * (porcentaje / 100.0)) / Math.log10(10.0))));
            }
            
            if (gain < min) gain = min;
            if (gain > max) gain = max;
            
            gainControl.setValue(gain);
            
        } catch (Exception e) {
            System.err.println("No se pudo ajustar el volumen: " + e.getMessage());
        }
    }
    
    private Clip cargarSonido(String nombreArchivo) {
        try {
            URL url = getClass().getResource("/audios/" + nombreArchivo);
            if (url == null) {
                System.err.println("No se pudo encontrar el audio: /audios/" + nombreArchivo);
                return null;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error al cargar el audio " + nombreArchivo + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private void pararTodosLosSonidos() {
        if (clipMenu != null && clipMenu.isRunning()) {
            clipMenu.stop();
        }
        if (clipJuego != null && clipJuego.isRunning()) {
            clipJuego.stop();
        }
    }

    private void reproducirSonido(Clip clip, boolean loop) {
        if (clip == null) return;
        
        if (loop) {
            pararTodosLosSonidos();
        }
        
        clip.setFramePosition(0); 
        
        if (loop) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            clip.start(); 
        }
    }

    // -------------------------------
    // PANEL DERECHO: REGLAS Y RANKING
    // -------------------------------
    private JPanel crearPanelDerechoContenido() {
        JPanel panelDerecho = new JPanel(new GridBagLayout());
        panelDerecho.setOpaque(false); 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        
        JPanel panelReglasPlaceholder = new JPanel();
        panelReglasPlaceholder.setOpaque(false);

        JPanel panelRanking = crearPanelRanking(); 
        panelReglasPlaceholder.setPreferredSize(new Dimension(0, 0));
        panelRanking.setPreferredSize(new Dimension(0, 0));

        gbc.gridy = 0;
        gbc.weighty = 0.70; 
        gbc.insets = new Insets(0, 0, 0, 0); 
        panelDerecho.add(panelReglasPlaceholder, gbc);

        gbc.gridy = 1;
        gbc.weighty = 0.30; 
        gbc.insets = new Insets(0, 0, 0, 0);
        panelDerecho.add(panelRanking, gbc);

        return panelDerecho;
    }
    
    // -------------------------------
    // PANEL IZQUIERDO CON FLUJO INTERNO
    // -------------------------------
    private JPanel crearPanelIzquierdaContenido() {
        JPanel panelContenedor = new JPanel(new BorderLayout());
        panelContenedor.setOpaque(false); 

        // --- Panel Central (CardLayout) ---
        this.layoutInterno = new CardLayout();
        this.panelInterno = new JPanel(layoutInterno);
        panelInterno.setOpaque(false); 
        panelInterno.setBorder(new EmptyBorder(160, 60, 100, 60));

        JPanel pantallaInicio = new JPanel(new BorderLayout());
        pantallaInicio.setOpaque(false); 

        JPanel pantallaNombre = new JPanel(new GridBagLayout());
        pantallaNombre.setBackground(fondoPanel); 
        pantallaNombre.setOpaque(true);
        pantallaNombre.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel lblPideNombre = new JLabel("Ingrese su nombre:");
        lblPideNombre.setForeground(letra);
        lblPideNombre.setFont(new Font("Arial", Font.BOLD, 16));
        this.txtNombre = new JTextField(12);
        txtNombre.setBackground(fondo);
        txtNombre.setForeground(letra);
        txtNombre.setCaretColor(letra);
        txtNombre.setBorder(BorderFactory.createLineBorder(bordeDorado));
        
        JButton btnContinuar = new JButton("Continuar");
        btnContinuar.setBackground(fondoPanel);
        btnContinuar.setForeground(letra);
        btnContinuar.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(20, bordeDorado, 2), new EmptyBorder(5, 10, 5, 10)));
        // Añadir cursor de mano
        btnContinuar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        this.lblError = new JLabel("");
        lblError.setForeground(Color.RED);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 0; pantallaNombre.add(lblPideNombre, gbc);
        gbc.gridy = 1; pantallaNombre.add(txtNombre, gbc);
        gbc.gridy = 2; pantallaNombre.add(btnContinuar, gbc);
        gbc.gridy = 3; pantallaNombre.add(lblError, gbc);

        JPanel pantallaModo = new JPanel(new GridBagLayout());
        pantallaModo.setBackground(fondoPanel); 
        pantallaModo.setOpaque(true);
        pantallaModo.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblModo = new JLabel("Seleccione el modo de juego:");
        lblModo.setForeground(letra);
        lblModo.setFont(new Font("Arial", Font.BOLD, 16));
        JButton btnNormal = new JButton("Tira Y Afloje");
        JButton btnEndless = new JButton("Endless");
        for (JButton b : new JButton[]{btnNormal, btnEndless}) {
            b.setBackground(fondoPanel);
            b.setForeground(letra);
            b.setFocusPainted(false);
            b.setFont(new Font("Arial", Font.BOLD, 14));
            b.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(20, bordeDorado, 2), new EmptyBorder(5, 10, 5, 10)));
            // Añadir cursor de mano
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        Dimension sizeNormal = btnNormal.getPreferredSize();
        Dimension sizeEndless = btnEndless.getPreferredSize();
        int maxWidth = Math.max(sizeNormal.width, sizeEndless.width);
        int maxHeight = Math.max(sizeNormal.height, sizeEndless.height);
        Dimension uniformSize = new Dimension(maxWidth, maxHeight);
        btnNormal.setPreferredSize(uniformSize);
        btnEndless.setPreferredSize(uniformSize);
        
        gbc.gridy = 0; gbc.gridx = 0; gbc.insets = new Insets(10, 10, 10, 10);
        pantallaModo.add(lblModo, gbc);
        gbc.gridy = 1;
        pantallaModo.add(btnNormal, gbc);
        gbc.gridy = 2;
        pantallaModo.add(btnEndless, gbc);

        this.pantallaJuegoNormal = crearPantallaJuegoNormal();
        this.pantallaJuegoEndless = crearPantallaJuegoEndless(); 

        // --- Panel de Botón Inferior ---
        this.panelBotonInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        panelBotonInferior.setOpaque(false); 
        panelBotonInferior.setBorder(new EmptyBorder(0, 0, 80, 0)); 
        
        this.btnAccionIzquierdo = new JButton("JUGAR");
        btnAccionIzquierdo.setBackground(fondoPanel);
        btnAccionIzquierdo.setForeground(letra);
        btnAccionIzquierdo.setFont(new Font("Arial", Font.BOLD, 16));
        btnAccionIzquierdo.setPreferredSize(minBotonSize);
        btnAccionIzquierdo.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(20, bordeDorado, 2), new EmptyBorder(5, 10, 5, 10)));
        // (NUEVO) Añadir cursor de mano
        btnAccionIzquierdo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panelBotonInferior.add(btnAccionIzquierdo);
        
        this.btnAccionDerecho = new JButton("Salir");
        btnAccionDerecho.setBackground(fondoPanel);
        btnAccionDerecho.setForeground(letra);
        btnAccionDerecho.setFont(new Font("Arial", Font.BOLD, 16));
        btnAccionDerecho.setPreferredSize(minBotonSize);
        btnAccionDerecho.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(20, bordeDorado, 2), new EmptyBorder(5, 10, 5, 10)));
        // Añadir cursor de mano
        btnAccionDerecho.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panelBotonInferior.add(btnAccionDerecho);
        
        btnAccionDerecho.setVisible(false);
        panelBotonInferior.setVisible(true);

        // --- Acciones de Botones (Lógica sin cambios) ---
        btnContinuar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                AudioManager.getInstance().playBotonSound();
            }
        });
        btnContinuar.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            if (Formato.validarNombre(nombre)) {
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
        btnNormal.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                AudioManager.getInstance().playBotonSound();
            }
        });
        btnNormal.addActionListener(e -> {
            reproducirSonido(clipJuego, true);
            juego.ingresarModo("Tira Y Afloje");
            juego.setPuntos();
            iniciarNuevaRonda(); 
            layoutInterno.show(panelInterno, "juegoNormal");
            btnAccionIzquierdo.setText("Reiniciar");
            btnAccionDerecho.setText("Salir");
            btnAccionDerecho.setVisible(true);
            
        });
        btnEndless.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                AudioManager.getInstance().playBotonSound();
            }
        });
        btnEndless.addActionListener(e -> {
            reproducirSonido(clipJuego, true);
            juego.ingresarModo("Endless");
            juego.setPuntos();
            iniciarNuevaRondaEndless(); 
            layoutInterno.show(panelInterno, "juegoEndless");
            btnAccionIzquierdo.setText("Reiniciar");
            btnAccionDerecho.setText("Salir");
            btnAccionDerecho.setVisible(true);
            
        });
        btnAccionIzquierdo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                AudioManager.getInstance().playBotonSound();
            }
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
                        reproducirSonido(clipMenu, true);
                    }
                    break;
                case "Reiniciar":
                    reproducirSonido(clipJuego, true);
                    if (juego.getModoJuego().equals("Tira Y Afloje")) { 
                        if (timer != null) timer.stop();
                        juego.setPuntos();
                        iniciarNuevaRonda();
                    } else { // "Endless"
                        if (timerEndless != null) timerEndless.stop();
                        juego.setPuntos();
                        iniciarNuevaRondaEndless();
                        btnAccionIzquierdo.setText("Reiniciar");
                    }
                    break;
                case "Retirarse":
                    pararTodosLosSonidos();
                    reproducirSonido(clipGanar, false);
                    if (timerEndless != null) timerEndless.stop();
                    juego.guardarPartida(accion.simbolo);
                    JOptionPane.showMessageDialog(this,
                        "¡Felicidades! Te retiras con " + juego.getPuntosJugador() + " puntos.",
                        "Partida Finalizada", JOptionPane.INFORMATION_MESSAGE);
                    layoutInterno.show(panelInterno, "inicio");
                    txtNombre.setText("");
                    lblError.setText("");
                    btnAccionIzquierdo.setText("JUGAR");
                    btnAccionDerecho.setVisible(false);
                    reproducirSonido(clipMenu, true); // Reinicia música de menú
                    break;
            }
            
        });
        btnAccionDerecho.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                AudioManager.getInstance().playBotonSound();
            }
        });
        btnAccionDerecho.addActionListener(e -> {
            pararTodosLosSonidos();
            reproducirSonido(clipMenu, true);
            if (juego.getModoJuego().equals("Tira Y Afloje")) {
                if (timer != null) timer.stop();
            }
            else {
                if (timerEndless != null) timerEndless.stop();
            }
            layoutInterno.show(panelInterno, "modo");
            btnAccionIzquierdo.setText("Volver Atras");
            btnAccionDerecho.setVisible(false);
            
        });

        panelInterno.add(pantallaInicio, "inicio");
        panelInterno.add(pantallaNombre, "nombre");
        panelInterno.add(pantallaModo, "modo");
        panelInterno.add(pantallaJuegoNormal, "juegoNormal");
        panelInterno.add(pantallaJuegoEndless, "juegoEndless");
        layoutInterno.show(panelInterno, "inicio");
        
        panelContenedor.add(panelInterno, BorderLayout.CENTER);
        panelContenedor.add(panelBotonInferior, BorderLayout.SOUTH);

        return panelContenedor;
    }
    
    // -------------------------------
    // PANEL DERECHO: RANKING
    // -------------------------------
    private JPanel crearPanelRanking() {
        JPanel panelRanking = new JPanel(new BorderLayout());
        panelRanking.setOpaque(false); 
        panelRanking.setBorder(new EmptyBorder(20, 30, 0, 30)); 
        CardLayout rankingLayout = new CardLayout();
        JPanel panelTarjetas = new JPanel(rankingLayout);
        panelTarjetas.setOpaque(false);
        this.txtRankingTyA = new JTextArea(" -----------------------------------\n  (Cargando Ranking...)");
        this.txtRankingEndless = new JTextArea(" -----------------------------------\n  (Cargando Ranking...)");
        JPanel tarjetaTyA = crearTarjetaRanking("MODO TIRA Y AFLOJE (Menos Rondas)", txtRankingTyA);
        JPanel tarjetaEndless = crearTarjetaRanking("MODO ENDLESS (Más Puntaje)", txtRankingEndless);
        tarjetaTyA.setOpaque(false);
        tarjetaEndless.setOpaque(false);
        panelTarjetas.add(tarjetaTyA, "TyA");
        panelTarjetas.add(tarjetaEndless, "Endless");
        panelRanking.add(panelTarjetas, BorderLayout.CENTER);
        this.rankingTimer = new Timer(15000, e -> {
            rankingLayout.next(panelTarjetas);
        });
        this.rankingTimer.setInitialDelay(15000);
        return panelRanking;
    }

    private JPanel crearTarjetaRanking(String titulo, JTextArea txtRanking) {
        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setOpaque(false); 
        JLabel lblSubtitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Arial", Font.BOLD, 14));
        lblSubtitulo.setForeground(bordeDorado);
        lblSubtitulo.setBorder(new EmptyBorder(5, 10, 5, 10));
        tarjeta.add(lblSubtitulo, BorderLayout.NORTH);
        txtRanking.setEditable(false);
        txtRanking.setForeground(letra);
        txtRanking.setOpaque(false);
        txtRanking.setFont(new Font("Monospaced", Font.PLAIN, 14));
        txtRanking.setBorder(new EmptyBorder(15, 15, 15, 15));
        tarjeta.add(txtRanking, BorderLayout.CENTER);
        return tarjeta;
    }
    
    // -------------------------------
    // MÉTODO PARA ACTUALIZAR RANKINGS
    // -------------------------------
    private void actualizarRankings() {
        txtRankingTyA.setText("Actualizando...");
        txtRankingEndless.setText("Actualizando...");
        new Thread(() -> {
            Map<String, Vector<String[]>> rankings = lectura.obtenerRankings();
            if (rankings == null) {
                String errorText = "Error al cargar rankings: Servidor falló.";
                SwingUtilities.invokeLater(() -> {
                    txtRankingEndless.setText(errorText);
                    txtRankingTyA.setText(errorText);
                });
                return;
            }
            Vector<String[]> endlessData = rankings.get("Endless");
            String endlessText;
            if (endlessData != null && !endlessData.isEmpty()) {
                StringBuilder sb = new StringBuilder("");
                int rank = 1;
                for (String[] fila : endlessData) {
                    sb.append(String.format(" %d. %-10s (%-4s)    %s Pts\n",
                        rank++, fila[0], fila[2], fila[1]));
                }
                endlessText = sb.toString();
            } else {
                endlessText = "No hay datos de ranking.\n";
            }
            Vector<String[]> tyaData = rankings.get("TyA");
            String tyaText;
            if (tyaData != null && !tyaData.isEmpty()) {
                StringBuilder sb = new StringBuilder("");
                int rank = 1;
                for (String[] fila : tyaData) {
                    sb.append(String.format(" %d. %-10s (%-4s)    %s Rondas\n",
                        rank++, fila[0], fila[2], fila[1]));
                }
                tyaText = sb.toString();
            } else {
                tyaText = "No hay datos de ranking.\n";
            }
            SwingUtilities.invokeLater(() -> {
                txtRankingEndless.setText(endlessText);
                txtRankingTyA.setText(tyaText);
            });
        }).start();
    }


    // -------------------------------
    // CREA EL PANEL DE JUEGO "TIRA Y AFLOJE"
    // -------------------------------
    private JPanel crearPantallaJuegoNormal() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(fondoPanel);
        panel.setOpaque(true); 
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
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
        gbc.gridwidth = 1; gbc.insets = new Insets(8, 10, 8, 10);
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
        gbc.insets = new Insets(0, 10, 8, 10);
        btnConfirmarPrediccion = new JButton("Confirmar");
        btnConfirmarPrediccion.setBackground(fondoPanel);
        btnConfirmarPrediccion.setForeground(letra);
        btnConfirmarPrediccion.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(20, bordeDorado, 2), new EmptyBorder(5, 10, 5, 10)));
        // Añadir cursor de mano
        btnConfirmarPrediccion.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridy = 8; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(btnConfirmarPrediccion, gbc);
        btnConfirmarPrediccion.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                AudioManager.getInstance().playBotonSound();
            }
        });
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
        panel.setOpaque(true); 
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        lblRondaEndless = new JLabel("Ronda: 1");
        lblRondaEndless.setForeground(letra);
        lblRondaEndless.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0; panel.add(lblRondaEndless, gbc);
        lblTiempoEndless = new JLabel("Tiempo: 30");
        lblTiempoEndless.setForeground(bordeDorado);
        lblTiempoEndless.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 1; gbc.gridy = 0; panel.add(lblTiempoEndless, gbc);
        lblPuntosJugadorEndless = new JLabel("Tus Puntos: 1000");
        lblPuntosJugadorEndless.setForeground(Color.CYAN);
        lblPuntosJugadorEndless.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 1; panel.add(lblPuntosJugadorEndless, gbc);
        lblErrorIAEndless = new JLabel("Error IA: ---");
        lblErrorIAEndless.setForeground(Color.PINK);
        lblErrorIAEndless.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 1; gbc.gridy = 1; panel.add(lblErrorIAEndless, gbc);
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2; gbc.insets = new Insets(10, 0, 10, 0);
        panel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1; gbc.insets = new Insets(8, 10, 8, 10);
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
        lblAltoAyerEndless = new JLabel("Altoayer: $152.10");
        lblAltoAyerEndless.setForeground(letra);
        gbc.gridy = 6; gbc.gridx = 0; panel.add(lblAltoAyerEndless, gbc);
        lblBajoAyerEndless = new JLabel("Bajo Ayer: $148.50");
        lblBajoAyerEndless.setForeground(letra);
        gbc.gridx = 1; panel.add(lblBajoAyerEndless, gbc);
        JLabel lblTuPrediccion = new JLabel("Tu Predicción (Cierre Hoy):");
        lblTuPrediccion.setForeground(bordeDorado);
        gbc.gridy = 7; gbc.gridx = 0; gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(20, 10, 15, 5);
        panel.add(lblTuPrediccion, gbc);
        txtPrediccionEndless = new JTextField(10);
        txtPrediccionEndless.setBackground(fondo);
        txtPrediccionEndless.setForeground(letra);
        txtPrediccionEndless.setCaretColor(letra);
        txtPrediccionEndless.setBorder(BorderFactory.createLineBorder(bordeDorado));
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(20, 5, 15, 10);
        panel.add(txtPrediccionEndless, gbc);
        gbc.insets = new Insets(0, 10, 5, 10);
        btnConfirmarPrediccionEndless = new JButton("Confirmar");
        btnConfirmarPrediccionEndless.setBackground(fondoPanel);
        btnConfirmarPrediccionEndless.setForeground(letra);
        btnConfirmarPrediccionEndless.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(20, bordeDorado, 2), new EmptyBorder(5, 10, 5, 10)));
        // Añadir cursor de mano
        btnConfirmarPrediccionEndless.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridy = 8; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(btnConfirmarPrediccionEndless, gbc);
        btnConfirmarPrediccionEndless.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                AudioManager.getInstance().playBotonSound();
            }
        });
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
    // LÓGICA DE JUEGO (TIRA Y AFLOJE) (sin cambios)
    // -------------------------------
    private void iniciarNuevaRonda() {
        String simboloParaJuego = (this.accion.simbolo == null) ? "KO" : this.accion.simbolo;
        boolean exito = juego.generarNuevaRonda(simboloParaJuego);
        if (!exito) {
            if (timer != null) timer.stop();
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los datos para la nueva ronda.\nInténtalo de nuevo más tarde.", "Error de Red", JOptionPane.ERROR_MESSAGE);
            layoutInterno.show(panelInterno, "modo"); // Go back to mode selection
            btnAccionIzquierdo.setText("Volver Atras");
            btnAccionDerecho.setVisible(false);
            return;
        }
        
        actualizarLabelsDatos();
        int ronda = juego.getRondaActual();
        if (ronda >= 15) { this.tiempoRestante = 10; }
        else if (ronda >= 10) { this.tiempoRestante = 15; }
        else if (ronda >= 5) { this.tiempoRestante = 20; }
        else { this.tiempoRestante = 30; }
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
        
        double cierreHoy = juego.getCierreHoy();
        
        // Comprobación de Derrota Instantánea
        double porcentajeErrorJugador = (Math.abs(prediccionJugador - cierreHoy) / cierreHoy);
        if (porcentajeErrorJugador > INSTANT_LOSE_THRESHOLD) {
            juego.forzarDerrota();
            pararTodosLosSonidos();
            reproducirSonido(clipPerder, false);
            JOptionPane.showMessageDialog(this, 
                String.format("¡DERROTA INSTANTÁNEA!\n\nTu predicción ($%.2f) se desvió más de un %.0f%% del precio real ($%.2f).\nLa precisión es clave.", 
                prediccionJugador, INSTANT_LOSE_THRESHOLD * 100, cierreHoy), 
                "Fin del Juego", 
                JOptionPane.ERROR_MESSAGE);
            
            // Volver al menú
            layoutInterno.show(panelInterno, "inicio");
            txtNombre.setText("");
            lblError.setText("");
            btnAccionIzquierdo.setText("JUGAR");
            btnAccionDerecho.setVisible(false);
            panelBotonInferior.setVisible(true); 
            reproducirSonido(clipMenu, true);
            return; // Termina el método aquí
        }

        // Cálculo de Puntos de Error Porcentual
        double prediccionIA = modelo.hacerPrediccion(juego.getCierreAyer(), juego.getCierreManana());
        double porcentajeErrorIA = (Math.abs(prediccionIA - cierreHoy) / cierreHoy);

        int errorJugador = (int) (porcentajeErrorJugador * BASE_ERROR_MULTIPLIER);
        int errorIA = (int) (porcentajeErrorIA * BASE_ERROR_MULTIPLIER);

        // Procesar resultado con la lógica de Juego
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
        
        // Comprobar fin de juego normal
        if (juego.isJuegoTerminado()) {
            pararTodosLosSonidos();
            String ganadorFinal = juego.getGanadorFinal();
            String mensajeFinal;
            if (ganadorFinal.equals(juego.getNombreJugador())) {
                mensajeFinal = "¡FELICIDADES, GANASTE!\nHas dejado a la IA en 0 puntos.";
                reproducirSonido(clipGanar, false);
                juego.guardarPartida(accion.simbolo);
            } else {
                mensajeFinal = "¡OH NO, PERDISTE!\nLa IA te ha quitado todos tus puntos.";
                reproducirSonido(clipPerder, false);
                juego.guardarPartida(accion.simbolo);
            }
            JOptionPane.showMessageDialog(this, mensajeFinal, "Fin del Juego", JOptionPane.INFORMATION_MESSAGE);
            layoutInterno.show(panelInterno, "inicio");
            txtNombre.setText("");
            lblError.setText("");
            btnAccionIzquierdo.setText("JUGAR");
            btnAccionDerecho.setVisible(false);
            panelBotonInferior.setVisible(true); 
            reproducirSonido(clipMenu, true);
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
        lblAltoAyer.setText("Altoayer: " + currencyFormat.format(juego.getAltoAyer()));
        lblBajoAyer.setText("Bajo Ayer: " + currencyFormat.format(juego.getBajoAyer()));
    }


    // -------------------------------
    // LÓGICA DE JUEGO (ENDLESS) (sin cambios)
    // -------------------------------
    private void iniciarNuevaRondaEndless() {
        String simboloParaJuego = (this.accion.simbolo == null) ? "KO" : this.accion.simbolo;
        boolean exito = juego.generarNuevaRonda(simboloParaJuego);
        if (!exito) {
            if (timerEndless != null) timerEndless.stop();
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los datos para la nueva ronda.\nInténtalo de nuevo más tarde.", "Error de Red", JOptionPane.ERROR_MESSAGE);
            layoutInterno.show(panelInterno, "modo"); // Go back to mode selection
            btnAccionIzquierdo.setText("Volver Atras");
            btnAccionDerecho.setVisible(false);
            return;
        }

        actualizarLabelsDatosEndless();
        int ronda = juego.getRondaActual();
        if (ronda >= 15) { this.tiempoRestanteEndless = 10; }
        else if (ronda >= 10) { this.tiempoRestanteEndless = 15; }
        else if (ronda >= 5) { this.tiempoRestanteEndless = 20; }
        else { this.tiempoRestanteEndless = 30; }
        lblTiempoEndless.setText("Tiempo: " + tiempoRestanteEndless);
        txtPrediccionEndless.setText("");
        btnConfirmarPrediccionEndless.setEnabled(true);
        txtPrediccionEndless.setEnabled(true);
        timerEndless.start();
    }

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

        double cierreHoy = juego.getCierreHoy();

        // Comprobación de Derrota Instantánea
        double porcentajeErrorJugador = (Math.abs(prediccionJugador - cierreHoy) / cierreHoy);
        if (porcentajeErrorJugador > INSTANT_LOSE_THRESHOLD) {
            juego.forzarDerrota();
            pararTodosLosSonidos();
            reproducirSonido(clipPerder, false);
            JOptionPane.showMessageDialog(this, 
                String.format("¡DERROTA INSTANTÁNEA!\n\nTu predicción ($%.2f) se desvió más de un %.0f%% del precio real ($%.2f).\nLa partida ha terminado.", 
                prediccionJugador, INSTANT_LOSE_THRESHOLD * 100, cierreHoy), 
                "Fin del Juego", 
                JOptionPane.ERROR_MESSAGE);
            
            // Guardar partida y volver al menú
            juego.guardarPartida(accion.simbolo);
            layoutInterno.show(panelInterno, "inicio");
            txtNombre.setText("");
            lblError.setText("");
            btnAccionIzquierdo.setText("JUGAR");
            btnAccionDerecho.setVisible(false);
            panelBotonInferior.setVisible(true); 
            reproducirSonido(clipMenu, true);
            return; // Termina el método aquí
        }

        // Cálculo de Puntos de Error Porcentual
        double prediccionIA = modelo.hacerPrediccion(juego.getCierreAyer(), juego.getCierreManana());
        double porcentajeErrorIA = (Math.abs(prediccionIA - cierreHoy) / cierreHoy);
        
        int errorJugador = (int) (porcentajeErrorJugador * BASE_ERROR_MULTIPLIER);
        int errorIA = (int) (porcentajeErrorIA * BASE_ERROR_MULTIPLIER);
        
        // Procesar resultado con la lógica de Juego
        String resumenRonda = juego.calcularResultadoEndless(errorJugador, errorIA, prediccionJugador);
        
        actualizarLabelsDatosEndless();
        lblErrorIAEndless.setText("Error IA: " + errorIA);
        
        if (juego.getPuntosJugador() <= 0) {
            // Este caso se maneja en la lógica de 'calcularResultadoEndless' que actualiza los puntos,
            pararTodosLosSonidos();
            reproducirSonido(clipPerder, false);
            JOptionPane.showMessageDialog(this, 
                "¡Has perdido todos tus puntos!\nLa partida ha terminado.", 
                "Fin del Juego", 
                JOptionPane.INFORMATION_MESSAGE);
            
            juego.guardarPartida(accion.simbolo);
            layoutInterno.show(panelInterno, "inicio");
            txtNombre.setText("");
            lblError.setText("");
            btnAccionIzquierdo.setText("JUGAR");
            btnAccionDerecho.setVisible(false);
            panelBotonInferior.setVisible(true); 
            reproducirSonido(clipMenu, true);

        } else {
             String mensaje = String.format("--- RONDA %d FINALIZADA ---\n\nEl Precio de Cierre de HOY fue: %s\n\nTu Predicción: %s (Error: %d pts)\nPredicción IA: %s (Error: %d pts)\n\n%s",
                juego.getRondaActual(), currencyFormat.format(juego.getCierreHoy()), currencyFormat.format(prediccionJugador), errorJugador, currencyFormat.format(prediccionIA), errorIA, resumenRonda);
            JOptionPane.showMessageDialog(this, mensaje, "Fin de Ronda", JOptionPane.INFORMATION_MESSAGE);
            btnAccionIzquierdo.setText(juego.getPuntosJugador() > 1000 ? "Retirarse" : "Reiniciar");
            iniciarNuevaRondaEndless();
        }
    }
    
    private void actualizarLabelsDatosEndless() {
        lblRondaEndless.setText("Ronda: " + juego.getRondaActual());
        lblPuntosJugadorEndless.setText("Tus Puntos: " + juego.getPuntosJugador());
        lblErrorIAEndless.setText("Error IA: ---");
        lblFechaEndless.setText("Prediciendo Cierre para: " + juego.getFecha());
        lblCierreAyerEndless.setText("Cierre Ayer: " + currencyFormat.format(juego.getCierreAyer()));
        lblCierreMananaEndless.setText("Cierre Mañana: " + currencyFormat.format(juego.getCierreManana()));
        lblOpenHoyEndless.setText("Open Hoy: " + currencyFormat.format(juego.getOpenHoy()));
        lblVolumenAyerEndless.setText("Volumen Ayer: " + NumberFormat.getIntegerInstance(Locale.US).format(juego.getVolumenAyer()));
        lblAltoAyerEndless.setText("Altoayer: " + currencyFormat.format(juego.getAltoAyer()));
        lblBajoAyerEndless.setText("Bajo Ayer: " + currencyFormat.format(juego.getBajoAyer()));
    }
    
    private class PanelJuegoIzquierdo extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (fondoIzquierdo != null) {
                g.drawImage(fondoIzquierdo, 0, 0, this.getWidth(), this.getHeight(), this);
            }
        }
    }
    
    private class PanelJuegoDerecho extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (fondoDerecho != null) {
                g.drawImage(fondoDerecho, 0, 0, this.getWidth(), this.getHeight(), this);
            }
        }
    }
}