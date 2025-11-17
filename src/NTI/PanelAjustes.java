package NTI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.Border; // (Importar Border)
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * (CLASE CORREGIDA)
 * Muestra solo las 3 opciones de configuración: Moneda, SFX y RDR.
 * NO carga la configuración en el constructor.
 * Tiene un método público cargarConfiguracionActual() para ser llamado.
 */
public class PanelAjustes extends JPanel {

    // --- Referencia a NTI ---
    private NTI nti;

    // --- Componentes de la UI ---
    private JComboBox<String> cbMoneda;
    private JToggleButton toggleSonido;
    private JToggleButton toggleRed;

    /**
     * Constructor de PanelAjustes.
     */
    public PanelAjustes(NTI nti) {
        // 1. Guardar Referencia
        this.nti = nti;

        // 2. Configurar este Panel (Tu layout original)
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBackground(nti.fondo);
        this.setBorder(new EmptyBorder(30, 20, 30, 20));
        
        // 3. Construir la UI (PERO SIN CARGAR DATOS)
        initUI();
    }

    /**
     * Construye la UI del panel de ajustes con la nueva estética.
     * (YA NO CARGA DATOS AQUÍ)
     */
    private void initUI() {
        
        JLabel lblAjustes = new JLabel("Ajustes");
        lblAjustes.setForeground(nti.bordeDorado);
        lblAjustes.setFont(Fuentes.getBlack(26f));
        lblAjustes.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblAjustes.setBorder(new EmptyBorder(0, 0, 20, 0));
        this.add(lblAjustes);

        // Helper para crear los paneles
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
            panel.add(componente, gbc);
            return panel;
        };

        // --- Panel Moneda ---
        String[] textosMoneda = {"Moneda", "Seleccione la divisa con la que desea que se muestre el sistema."};
        cbMoneda = new JComboBox<>(new String[]{"US Dolar (USD)", "Euro (EUR)", "Peso Argentino (ARS)"});
        personalizarComboBox(cbMoneda); 
        cbMoneda.addActionListener(e -> guardarConfiguracion()); 
        
        JPanel panelMoneda = crearPanelAjuste.apply(textosMoneda, cbMoneda);
        this.add(panelMoneda);
        this.add(Box.createVerticalStrut(15));

        // --- Panel Sonido ---
        String[] textosSonido = {"Efectos de sonido", "Desactive esta opción para deshabilitar los efectos de sonido."};
        toggleSonido = new SliderToggleButton(); 
        toggleSonido.addActionListener(e -> guardarConfiguracion()); 
        
        JPanel panelSonido = crearPanelAjuste.apply(textosSonido, toggleSonido);
        this.add(panelSonido);
        this.add(Box.createVerticalStrut(15));

        // --- Panel Red ---
        String[] textosRed = {"Red de refinamiento", "Desactive esta opción para deshabilitar la red neuronal que refina el resultado."};
        toggleRed = new SliderToggleButton(); 
        toggleRed.addActionListener(e -> guardarConfiguracion()); 
        
        JPanel panelRed = crearPanelAjuste.apply(textosRed, toggleRed);
        this.add(panelRed);
        
        this.add(Box.createVerticalGlue());
        
        // (LA LÓGICA DE CARGA FUE REMOVIDA DE AQUÍ)
    }

    /**
     * (MODIFICADO)
     * Lee el config.json y actualiza la UI.
     * Carga desde los índices 1 (SFX) y 2 (RDR).
     * Actualiza la UI visualmente.
     */
    public void cargarConfiguracionActual() {
        // [0]moneda, [1]sfx, [2]rdr
        String[] config = nti.ent.conseguirConfig();
        
        if (config != null) {
            // 1. Moneda (Índice 0)
            cbMoneda.setSelectedItem(config[0]);
            
            // 2. SFX (Índice 1)
            toggleSonido.setSelected(Boolean.parseBoolean(config[1]));
            
            // 3. RDR (Índice 2)
            toggleRed.setSelected(Boolean.parseBoolean(config[2]));

        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "No se encontró el archivo de configuración. Se usarán valores por defecto.",
                    "Archivo no encontrado",
                    JOptionPane.WARNING_MESSAGE
            );
        }
        
        // (CRÍTICO) Repinta los toggles para que muestren el estado cargado
        toggleSonido.repaint();
        toggleRed.repaint();
    }

    /**
     * Guarda la configuración actual en el config.json.
     * Se llama automáticamente cada vez que un control cambia.
     */
    private void guardarConfiguracion() {
        // 1. Leer valores de esta UI
        String monedaS = (String) cbMoneda.getSelectedItem();
        boolean sfxR = toggleSonido.isSelected();
        boolean rdrR = toggleRed.isSelected();

        // 2. Obtener los valores globales que NO están en esta pantalla
        int idModeloActual = nti.modelo.getIDModeloSeleccionado();
        String estiloGraficaActual = nti.estiloGraficaActual;

        // 3. Enviar los 5 valores para guardar el JSON completo
        boolean resultado = nti.ent.enviarNConfig(monedaS, sfxR, rdrR, idModeloActual, estiloGraficaActual);
        
        if (resultado) {
            System.out.println("Configuración de ajustes guardada.");
        } else {
            System.err.println("Error al guardar la configuración.");
        }
    }
    
    // =================================================================
    //  CLASES INTERNAS PARA ESTÉTICA PERSONALIZADA
    // =================================================================

    /**
     * Un JToggleButton personalizado que se ve como un slider.
     */
    private class SliderToggleButton extends JToggleButton {
        private Color colorOn = new Color(170, 120, 40); // Oro oscuro
        private Color colorOff = new Color(80, 80, 80);  // Gris oscuro
        private Color colorThumb = nti.bordeDorado;      // Oro brillante
        private int arc = 30;
        private int thumbSize = 24;
        private int margin = 3;

        public SliderToggleButton() {
            super();
            setPreferredSize(new Dimension(70, 30));
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            
            if (isSelected()) {
                g2.setColor(colorOn);
            } else {
                g2.setColor(colorOff);
            }
            g2.fillRoundRect(0, 0, width, height, arc, arc);

            g2.setColor(colorThumb);
            int thumbX;
            if (isSelected()) {
                thumbX = width - thumbSize - margin;
            } else {
                thumbX = margin;
            }
            int thumbY = (height - thumbSize) / 2;
            g2.fillOval(thumbX, thumbY, thumbSize, thumbSize);

            g2.dispose();
        }
    }

    /**
     * Aplica la estética personalizada a una JComboBox.
     * Añade un borde dorado redondeado.
     */
    private void personalizarComboBox(JComboBox<String> cb) {
        cb.setBackground(nti.fondoPanel);
        cb.setForeground(nti.letra);
        cb.setFont(Fuentes.getBold(12f));
        cb.setPreferredSize(new Dimension(180, 30)); 
        
        Border roundedBorder = new RoundedBorder(10, nti.bordeDorado, 2); 
        Border padding = new EmptyBorder(5, 10, 5, 5);
        cb.setBorder(BorderFactory.createCompoundBorder(roundedBorder, padding));
        
        cb.setUI(new CustomComboBoxUI());
        cb.setRenderer(new CustomListRenderer());
    }

    /**
     * Clase interna para la UI de la ComboBox.
     */
    private class CustomComboBoxUI extends BasicComboBoxUI {
        
        @Override
        protected JButton createArrowButton() {
            JButton button = new JButton("\u25BC"); 
            button.setBackground(nti.fondoPanel);
            button.setForeground(nti.letra);
            button.setBorder(new EmptyBorder(0, 5, 0, 10));
            button.setFocusPainted(false);
            button.setContentAreaFilled(false);
            return button;
        }
        
        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            g.setColor(nti.fondoPanel);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }

        @Override
        protected Insets getInsets() {
            return new Insets(0, 0, 0, 0); 
        }
    }

    /**
     * Clase interna para renderizar los items de la lista desplegable.
     */
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
}