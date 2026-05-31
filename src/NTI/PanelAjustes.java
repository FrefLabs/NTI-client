package NTI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.Border;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiFunction;
import juego.PanelJuego;
import java.util.Vector;

public class PanelAjustes extends JPanel {

    private NTI nti;
    private boolean inicializando = false;

    private JComboBox<String> cbMoneda;
    private JToggleButton toggleSonido;
    private JToggleButton toggleRed;
    private JSlider sliderVolumen;
    private JLabel lblValorVolumen;

    public PanelAjustes(NTI nti) {
        this.nti = nti;
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBackground(nti.fondo);
        this.setBorder(new EmptyBorder(30, 20, 30, 20));
        initUI();
    }

    private void initUI() {

        JLabel lblAjustes = new JLabel("Ajustes");
        lblAjustes.setForeground(nti.bordeDorado);
        lblAjustes.setFont(Fuentes.getBlack(26f));
        lblAjustes.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblAjustes.setBorder(new EmptyBorder(0, 0, 20, 0));
        this.add(lblAjustes);

        BiFunction<String[], JComponent, JPanel> crearPanelAjuste = (textos, componente) -> {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(nti.fondoPanel);
            panel.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(15, nti.bordeDorado, 2),
                    new EmptyBorder(30, 20, 30, 10)
            ));
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 0, 20);
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;

            String descripcion = textos[1];
            if (textos[0].equals("Red de refinamiento")) {
                descripcion = "Desactive esta opción para deshabilitar la red neuronal que refina el resultado en base a un análisis fundamental.";
            }

            JLabel lbl = new JLabel("<html><span style='font-size:16px; font-weight:bold;'>" + textos[0] + "</span><br><span style='font-size:10px; color:#AAAAAA;'>" + descripcion + "</span></html>");
            lbl.setForeground(nti.letra);
            gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 0.9;
            panel.add(lbl, gbc);

            gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST; gbc.weightx = 0.1;

            if (componente instanceof JSlider) {
                JPanel panelSlider = new JPanel(new BorderLayout(10, 0));
                panelSlider.setOpaque(false);
                lblValorVolumen = new JLabel("50%");
                lblValorVolumen.setForeground(nti.letra);
                lblValorVolumen.setFont(Fuentes.getBold(12f));
                panelSlider.add(componente, BorderLayout.CENTER);
                panelSlider.add(lblValorVolumen, BorderLayout.EAST);
                panel.add(panelSlider, gbc);
            } else {
                panel.add(componente, gbc);
            }
            return panel;
        };
        
        // Panel de Moneda: Usando el snippet del usuario
        String[] textosMoneda = {"Moneda", "Seleccione la divisa con la que desea que se muestre el sistema."};
        cbMoneda = new JComboBox<>(); // Inicializar vacío, será llenado por actualizarListaDeMonedas()
        personalizarComboBox(cbMoneda);
        cbMoneda.addActionListener(e -> {
            if (inicializando) return;
            guardarConfiguracion();
        });
        cbMoneda.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                AudioManager.getInstance().playComboInSound();
            }
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                AudioManager.getInstance().playComboOutSound();
            }
            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                AudioManager.getInstance().playComboOutSound();
            }
        });

        JPanel panelMoneda = crearPanelAjuste.apply(textosMoneda, cbMoneda);
        this.add(panelMoneda);
        this.add(Box.createVerticalStrut(15)); // Espacio entre este panel y el siguiente


        String[] textosSonido = {"Efectos de sonido (SFX)", "Desactive esta opción para deshabilitar los efectos de sonido de botones."};
        toggleSonido = new SliderToggleButton();
        toggleSonido.addActionListener(e -> {
            if (inicializando) return;
            AudioManager.getInstance().setSfxEnabled(toggleSonido.isSelected());
            guardarConfiguracion();
        });
        toggleSonido.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                AudioManager.getInstance().playBotonSound();
            }
        });

        JPanel panelSonido = crearPanelAjuste.apply(textosSonido, toggleSonido);
        this.add(panelSonido);
        this.add(Box.createVerticalStrut(15));

        String[] textosRed = {"Red de refinamiento", "Desactive esta opción para deshabilitar la red neuronal que refina el resultado."};
        toggleRed = new SliderToggleButton();
        toggleRed.addActionListener(e -> {
            if (inicializando) return;
            guardarConfiguracion();
        });
        toggleRed.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                AudioManager.getInstance().playBotonSound();
            }
        });

        JPanel panelRed = crearPanelAjuste.apply(textosRed, toggleRed);
        this.add(panelRed);
        this.add(Box.createVerticalStrut(15));

        String[] textosMusica = {"Volumen de Música", "Ajuste el volumen de la música de fondo (distinto a SFX)."};
        sliderVolumen = new JSlider(0, 100, 50);
        personalizarSlider(sliderVolumen);

        sliderVolumen.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                AudioManager.getInstance().playBotonSound();
            }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (inicializando) return;
                guardarConfiguracion();
                if (nti.panelJuego != null) {
                    nti.panelJuego.actualizarVolumenMusica();
                }
            }
        });
        sliderVolumen.addChangeListener(e -> {
            if (lblValorVolumen != null) {
                lblValorVolumen.setText(sliderVolumen.getValue() + "%");
            }
        });

        JPanel panelMusica = crearPanelAjuste.apply(textosMusica, sliderVolumen);
        this.add(panelMusica);

        this.add(Box.createVerticalGlue());
    }

    public void cargarConfiguracionActual() {
        this.inicializando = true;
        
        actualizarListaDeMonedas(); // Llama al nuevo método aquí
        
        // No seleccionar aquí, ya se hace en actualizarListaDeMonedas
        toggleSonido.setSelected(nti.ent.sfx);
        toggleRed.setSelected(nti.ent.rdr);
        sliderVolumen.setValue(nti.ent.volumenMusica); 
        // No es necesario repaint aquí si los componentes no se redibujan completamente
        this.inicializando = false;
    }
    
    private void actualizarListaDeMonedas() {
        // Ejecutar en un hilo separado para no bloquear la UI
        new Thread(() -> {
            Vector<String> monedas = nti.lectura.obtenerMonedasDisponibles();
            
            SwingUtilities.invokeLater(() -> {
                if (monedas != null && !monedas.isEmpty()) {
                    cbMoneda.setModel(new DefaultComboBoxModel<>(monedas));
                    cbMoneda.setEnabled(true);
                    
                    // Re-seleccionar el item guardado
                    if (nti.ent.moneda != null && !nti.ent.moneda.isEmpty() && monedas.contains(nti.ent.moneda)) {
                        cbMoneda.setSelectedItem(nti.ent.moneda);
                    } else {
                        // Si la moneda guardada no existe en la lista de la API, seleccionar la primera o un default.
                        cbMoneda.setSelectedIndex(0);
                        
                    }
                } else {
                    // Si falla la carga, el combobox mostrará el texto "Error de carga" y estará deshabilitado
                    cbMoneda.setModel(new DefaultComboBoxModel<>(new String[]{"Error de carga"}));
                    cbMoneda.setEnabled(false);
                }
            });
        }).start();
    }

    private void guardarConfiguracion() {
        // Evitar guardar una configuración inválida si el combobox está en estado de error
        if (!cbMoneda.isEnabled() || cbMoneda.getSelectedItem() == null || cbMoneda.getSelectedItem().toString().equals("Error de carga")) {
            System.err.println("ADVERTENCIA: No se guardó la configuración de moneda debido a un error de carga o estado inválido.");
            return; 
        }
    
        String moneda = (String) cbMoneda.getSelectedItem();
        boolean sfx = toggleSonido.isSelected();
        boolean rdr = toggleRed.isSelected();
        int volumen = sliderVolumen.getValue();

        nti.ent.cargarConfiguracionDesdeJSON(); // Carga la configuración más reciente del disco antes de guardar
        boolean resultado = nti.ent.enviarNConfig(moneda, sfx, rdr, nti.ent.idModelo, nti.ent.estiloGrafica, volumen);

        if (resultado) {
            System.out.println("Configuración de ajustes guardada.");
            // Si la configuración se guarda correctamente, actualizar nti.ent.moneda con la selección actual
            nti.ent.moneda = moneda; 
            nti.panelInicio.cargarDatosIniciales(); // Recargar el panel de inicio
        } else {
            System.err.println("Error al guardar la configuración.");
        }
    }
    
    private void personalizarSlider(JSlider slider) {
        slider.setOpaque(false);
        slider.setFocusable(false); 
        slider.setPreferredSize(new Dimension(150, 30));
        slider.setUI(new CustomSliderUI(slider, nti.bordeDorado, new Color(80, 80, 80)));
        slider.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
    private class SliderToggleButton extends JToggleButton {

        private final Color colorOn = new Color(170, 120, 40);
        private final Color colorOff = new Color(80, 80, 80);
        private final Color colorThumb = nti.bordeDorado;
        private final int arc = 40;
        private final int thumbSize = 32;
        private final int margin = 4;
        public SliderToggleButton() {

            super();
            setPreferredSize(new Dimension(95, 40));
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isSelected() ? colorOn : colorOff);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(colorThumb);
            int thumbX = isSelected() ? getWidth() - thumbSize - margin : margin;
            int thumbY = (getHeight() - thumbSize) / 2;
            g2.fillOval(thumbX, thumbY, thumbSize, thumbSize);
            g2.dispose();
        }
    }

    private void personalizarComboBox(JComboBox<String> cb) {
        cb.setBackground(nti.fondoPanel);
        cb.setForeground(nti.letra);
        cb.setFont(Fuentes.getBold(12f));
        cb.setPreferredSize(new Dimension(180, 30)); 
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cb.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(10, nti.bordeDorado, 2), new EmptyBorder(5, 10, 5, 5)));
        cb.setUI(new CustomComboBoxUI());
        cb.setRenderer(new CustomListRenderer());
    }

    private class CustomComboBoxUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton button = new JButton("\u25BC"); 
            button.setBackground(nti.fondoPanel);
            button.setForeground(nti.letra);
            button.setBorder(new EmptyBorder(0, 5, 0, 10));
            button.setFocusPainted(false);
            button.setContentAreaFilled(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return button;
        }
        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            g.setColor(nti.fondoPanel);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    private class CustomListRenderer extends DefaultListCellRenderer {
         @Override
         public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
             super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
             list.setBackground(nti.fondoPanel);
             setBackground(isSelected ? nti.bordeDorado : nti.fondoPanel);
             setForeground(isSelected ? nti.fondoPanel : nti.letra);
             setBorder(new EmptyBorder(5, 10, 5, 10));
             return this;
         }
    }
    
    private class CustomSliderUI extends BasicSliderUI {
        private Color trackColor;
        private Color thumbColor;
        private BasicStroke trackStroke;
        private final Dimension THUMB_SIZE = new Dimension(20, 20);

        public CustomSliderUI(JSlider slider, Color thumbColor, Color trackColor) {
            super(slider);
            this.thumbColor = thumbColor;
            this.trackColor = trackColor;
            this.trackStroke = new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND); 
        }
        
        @Override
        public void calculateTrackRect() {
            super.calculateTrackRect(); 
            if (trackRect != null) {
                trackRect.x += THUMB_SIZE.width / 2;
                trackRect.width -= THUMB_SIZE.width;
            }
        }
        
        @Override
        public void paintTrack(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle trackBounds = trackRect;
            int cy = trackBounds.height / 2; 
            g2d.setColor(trackColor);
            g2d.setStroke(trackStroke);
            g2d.drawLine(trackRect.x, trackRect.y + cy, trackRect.x + trackRect.width, trackRect.y + cy);
        }

        @Override
        public void paintThumb(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(thumbColor);
            g2d.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
        }

        @Override
        protected Dimension getThumbSize() {
            return THUMB_SIZE; 
        }

        @Override
        public void paintFocus(Graphics g) {}
    }
}
