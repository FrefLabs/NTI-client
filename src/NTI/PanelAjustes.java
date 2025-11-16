package NTI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * (NUEVA CLASE - BASADA EN TU CÓDIGO)
 * Encapsula todo el contenido de la pantalla "Ajustes".
 */
public class PanelAjustes extends JPanel {

    // --- Referencia a NTI y sus componentes ---
    private NTI nti;

    // --- Componentes de la UI (Tus variables) ---
    private JComboBox<String> cbMoneda;
    private JComboBox<String> cbIdioma;
    private JToggleButton toggleSonido;
    private JToggleButton toggleOscuro;
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

        // 3. Construir la UI
        initUI();
    }

    /**
     * (TU CÓDIGO ORIGINAL) Construye la UI del panel de ajustes.
     */
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
                    new EmptyBorder(10, 10, 10, 10)
            ));
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 0, 20);
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            JLabel lbl = new JLabel("<html><span style='font-size:16px; font-weight:bold;'>" + textos[0] + "</span><br><span style='font-size:10px;'>" + textos[1] + "</span></html>");
            lbl.setForeground(nti.letra);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.9;
            panel.add(lbl, gbc);
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.weightx = 0.1;
            panel.add(componente, gbc);
            return panel;
        };

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

        String[] textosMoneda = {"Moneda", "Seleccione la divisa con la que desea que se muestre el sistema."};
        cbMoneda = new JComboBox<>(new String[]{"US Dolar (USD)", "Euro (EUR)", "Peso Argentino (ARS)"});
        cbMoneda.setBackground(Color.BLACK);
        cbMoneda.setForeground(Color.WHITE);
        cbMoneda.setPreferredSize(new Dimension(180, 30));
        JPanel panelMoneda = crearPanelAjuste.apply(textosMoneda, cbMoneda);
        this.add(panelMoneda);
        this.add(Box.createVerticalStrut(15));

        String[] textosIdioma = {"Idioma", "Seleccione el idioma con el que desea que se muestre el sistema."};
        cbIdioma = new JComboBox<>(new String[]{"Español (ES)", "English (EN)", "Italiano"});
        cbIdioma.setBackground(Color.BLACK);
        cbIdioma.setForeground(Color.WHITE);
        cbIdioma.setPreferredSize(new Dimension(180, 30));
        JPanel panelIdioma = crearPanelAjuste.apply(textosIdioma, cbIdioma);
        this.add(panelIdioma);
        this.add(Box.createVerticalStrut(15));

        String[] textosSonido = {"Efectos de sonido", "Desactive esta opción para deshabilitar los efectos de sonido."};
        toggleSonido = new JToggleButton();
        toggleSonido.setSelected(true);
        toggleSonido.setPreferredSize(new Dimension(60, 25));
        estilizarToggle.accept(toggleSonido);
        JPanel panelSonido = crearPanelAjuste.apply(textosSonido, toggleSonido);
        this.add(panelSonido);
        this.add(Box.createVerticalStrut(15));

        String[] textosOscuro = {"Modo oscuro", "Desactive esta opción para deshabilitar el modo claro."};
        toggleOscuro = new JToggleButton();
        toggleOscuro.setSelected(true);
        toggleOscuro.setPreferredSize(new Dimension(60, 25));
        estilizarToggle.accept(toggleOscuro);
        JPanel panelOscuro = crearPanelAjuste.apply(textosOscuro, toggleOscuro);
        this.add(panelOscuro);
        this.add(Box.createVerticalStrut(15));

        String[] textosRed = {"Red de refinamiento", "Desactive esta opción para deshabilitar la red neuronal que refina el resultado."};
        toggleRed = new JToggleButton();
        toggleRed.setSelected(true);
        toggleRed.setPreferredSize(new Dimension(60, 25));
        estilizarToggle.accept(toggleRed);
        JPanel panelRed = crearPanelAjuste.apply(textosRed, toggleRed);
        this.add(panelRed);
        
        this.add(Box.createVerticalGlue());
        
        String[] config = nti.ent.conseguirConfig();
        if (config != null) {
            for (int i = 0; i < cbMoneda.getItemCount(); i++) {
                if (cbMoneda.getItemAt(i).equals(config[0])) {
                    cbMoneda.setSelectedIndex(i);
                    break;
                }
            }
            for (int i = 0; i < cbIdioma.getItemCount(); i++) {
                if (cbIdioma.getItemAt(i).equals(config[1])) {
                    cbIdioma.setSelectedIndex(i);
                    break;
                }
            }
            toggleSonido.setSelected(Boolean.parseBoolean(config[2]));
            toggleOscuro.setSelected(Boolean.parseBoolean(config[3]));
            toggleRed.setSelected(Boolean.parseBoolean(config[4]));
            
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
            JOptionPane.showMessageDialog(
                    null,
                    "No se encontró el archivo de configuración. Se usarán valores por defecto.",
                    "Archivo no encontrado",
                    JOptionPane.WARNING_MESSAGE
            );
        }

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelBoton.setOpaque(false);
        panelBoton.setBorder(new EmptyBorder(15, 0, 0, 0));
        panelBoton.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnAplicar = new JButton("Aplicar cambios");
        btnAplicar.setBackground(nti.fondoPanel);
        btnAplicar.setForeground(nti.letra);
        btnAplicar.setFocusPainted(false);
        btnAplicar.setPreferredSize(new Dimension(160, 35));
        btnAplicar.setBorder(new RoundedBorder(20, nti.bordeDorado, 2));
        btnAplicar.setFont(Fuentes.getBlack(14f));

        panelBoton.add(btnAplicar);
        this.add(panelBoton);
        
        // --- Listener del Botón ---
        btnAplicar.addActionListener(e -> {
            String monedaS = (String) cbMoneda.getSelectedItem();
            String idiomaS = (String) cbIdioma.getSelectedItem();
            boolean sfxR = toggleSonido.isSelected();
            boolean modoR = toggleOscuro.isSelected();
            boolean rdrR = toggleRed.isSelected();
            
            boolean resultado = nti.ent.enviarNConfig(monedaS, idiomaS, sfxR, modoR, rdrR);
            if (resultado) {
                JOptionPane.showMessageDialog(null, "Cambios guardados con éxito", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Ocurrió un problema, inténtelo de nuevo", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}