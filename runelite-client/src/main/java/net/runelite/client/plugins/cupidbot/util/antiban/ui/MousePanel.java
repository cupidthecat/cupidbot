package net.runelite.client.plugins.cupidbot.util.antiban.ui;

import net.runelite.client.plugins.cupidbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.cupidbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseEngineMode;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSmoothness;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSpeed;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import static net.runelite.client.plugins.cupidbot.util.antiban.ui.UiHelper.setupSlider;

public class MousePanel extends JPanel
{
    private static final Insets ROW_INSETS = new Insets(3, 5, 3, 5);
    private static final Insets SECTION_INSETS = new Insets(8, 5, 3, 5);
    private static final int ROW_CONTROL_WIDTH = 200;
    private static final int ROW_CONTROL_MIN_WIDTH = 80;
    private static final int ENGINE_MODE_WIDTH = 92;
    private static final int ADVANCED_POPUP_WIDTH = 226;
    private static final int ADVANCED_POPUP_HEIGHT = 420;
    private static final int CARD_WIDTH = PluginPanel.PANEL_WIDTH - (PluginPanel.BORDER_OFFSET * 2);

    private boolean updatingValues;

    private final JCheckBox useNaturalMouse = new JCheckBox("Natural Mouse");
    private final JComboBox<MouseEngineMode> mouseEngineModeComboBox = new JComboBox<>(MouseEngineMode.values());
    private final JCheckBox simulateMistakes = new JCheckBox("Simulate Mistakes");
    private final JCheckBox moveMouseOffScreen = new JCheckBox("Move Mouse Off Screen");
    private final JSlider moveMouseOffScreenChance = new JSlider(0, 100, (int) (Rs2AntibanSettings.moveMouseOffScreenChance * 100));
    private final JLabel moveMouseOffScreenChanceLabel = new JLabel("Move Mouse Off Screen (%): " + (int) (Rs2AntibanSettings.moveMouseOffScreenChance * 100));
    private final JCheckBox moveMouseRandomly = new JCheckBox("Move Mouse Randomly");
    private final JSlider moveMouseRandomlyChance = new JSlider(0, 100, (int) (Rs2AntibanSettings.moveMouseRandomlyChance * 100));
    private final JLabel moveMouseRandomlyChanceLabel = new JLabel("Random Mouse Movement (%): " + (int) (Rs2AntibanSettings.moveMouseRandomlyChance * 100));

    private final JLabel mouseSpeedLabel = new JLabel();
    private final JSlider mouseSpeedSlider = new JSlider(
            0,
            MouseSpeed.values().length - 1,
            Rs2AntibanSettings.mouseSpeed.getSliderIndex());
    private final JLabel mouseSmoothnessLabel = new JLabel();
    private final JSlider mouseSmoothnessSlider = new JSlider(
            0,
            MouseSmoothness.values().length - 1,
            Rs2AntibanSettings.mouseSmoothness.getSliderIndex());
    private final JCheckBox advancedMouseControls = new JCheckBox("Advanced");
    private final JPopupMenu advancedMousePopup = new JPopupMenu();
    private final JPanel advancedMousePopupPanel = new JPanel(new GridBagLayout());
    private final JScrollPane advancedMousePopupScrollPane = new JScrollPane(advancedMousePopupPanel);
    private final JLabel mouseReactionDelayLabel = new JLabel();
    private final JSlider mouseReactionDelaySlider = new JSlider(0, 500, Rs2AntibanSettings.mouseReactionDelayMs);
    private final JCheckBox mouseReactionDelayRandom = new JCheckBox("Random");
    private final JLabel mouseReactionDelayMinLabel = new JLabel();
    private final JSlider mouseReactionDelayMinSlider = new JSlider(0, 500, Rs2AntibanSettings.mouseReactionDelayMinMs);
    private final JLabel mouseReactionDelayMaxLabel = new JLabel();
    private final JSlider mouseReactionDelayMaxSlider = new JSlider(0, 500, Rs2AntibanSettings.mouseReactionDelayMaxMs);
    private final JLabel mouseSettleDelayLabel = new JLabel();
    private final JSlider mouseSettleDelaySlider = new JSlider(0, 500, Rs2AntibanSettings.mouseSettleDelayMs);
    private final JCheckBox mouseSettleDelayRandom = new JCheckBox("Random");
    private final JLabel mouseSettleDelayMinLabel = new JLabel();
    private final JSlider mouseSettleDelayMinSlider = new JSlider(0, 500, Rs2AntibanSettings.mouseSettleDelayMinMs);
    private final JLabel mouseSettleDelayMaxLabel = new JLabel();
    private final JSlider mouseSettleDelayMaxSlider = new JSlider(0, 500, Rs2AntibanSettings.mouseSettleDelayMaxMs);
    private final JLabel mouseButtonHoldLabel = new JLabel();
    private final JSlider mouseButtonHoldSlider = new JSlider(0, 500, Rs2AntibanSettings.mouseButtonHoldMs);
    private final JCheckBox mouseButtonHoldRandom = new JCheckBox("Random");
    private final JLabel mouseButtonHoldMinLabel = new JLabel();
    private final JSlider mouseButtonHoldMinSlider = new JSlider(0, 500, Rs2AntibanSettings.mouseButtonHoldMinMs);
    private final JLabel mouseButtonHoldMaxLabel = new JLabel();
    private final JSlider mouseButtonHoldMaxSlider = new JSlider(0, 500, Rs2AntibanSettings.mouseButtonHoldMaxMs);
    private final JLabel mouseCurveScaleLabel = new JLabel();
    private final JSlider mouseCurveScaleSlider = new JSlider(0, 200, Rs2AntibanSettings.mouseCurveScale);
    private final JLabel mousePathNoiseScaleLabel = new JLabel();
    private final JSlider mousePathNoiseScaleSlider = new JSlider(0, 200, Rs2AntibanSettings.mousePathNoiseScale);
    private final JLabel mouseMicroJitterScaleLabel = new JLabel();
    private final JSlider mouseMicroJitterScaleSlider = new JSlider(0, 200, Rs2AntibanSettings.mouseMicroJitterScale);
    private final JLabel mouseOvershootScaleLabel = new JLabel();
    private final JSlider mouseOvershootScaleSlider = new JSlider(0, 200, Rs2AntibanSettings.mouseOvershootScale);
    private final JLabel mouseCorrectionScaleLabel = new JLabel();
    private final JSlider mouseCorrectionScaleSlider = new JSlider(0, 200, Rs2AntibanSettings.mouseCorrectionScale);
    private final JLabel mouseEndpointErrorScaleLabel = new JLabel();
    private final JSlider mouseEndpointErrorScaleSlider = new JSlider(0, 200, Rs2AntibanSettings.mouseEndpointErrorScale);
    private final JLabel mouseDragStabilityScaleLabel = new JLabel();
    private final JSlider mouseDragStabilityScaleSlider = new JSlider(0, 200, Rs2AntibanSettings.mouseDragStabilityScale);
    private final JLabel mouseScrollBurstScaleLabel = new JLabel();
    private final JSlider mouseScrollBurstScaleSlider = new JSlider(0, 200, Rs2AntibanSettings.mouseScrollBurstScale);

    public MousePanel()
    {
        useNaturalMouse.setToolTipText("Simulate human-like mouse movements");
        mouseEngineModeComboBox.setToolTipText("Controls target-aware mouse planning style");
        mouseEngineModeComboBox.setFocusable(false);
        constrainPreferredWidth(mouseEngineModeComboBox, ENGINE_MODE_WIDTH);
        simulateMistakes.setToolTipText("Simulate mistakes in mouse movements");
        moveMouseOffScreen.setToolTipText("Move the mouse off screen if activity cooldown is active");
        moveMouseOffScreenChance.setToolTipText("Chance to move the mouse off screen when activity cooldown is active");
        moveMouseRandomly.setToolTipText("Move the mouse randomly when activity cooldown is active");
        moveMouseRandomlyChance.setToolTipText("Chance to move the mouse randomly when activity cooldown is active");

        mouseSpeedSlider.setToolTipText("Controls natural mouse movement speed");
        mouseSpeedSlider.setSnapToTicks(true);
        mouseSpeedSlider.setPaintTicks(true);
        mouseSpeedSlider.setPaintLabels(true);
        mouseSpeedSlider.setLabelTable(createMouseSpeedLabels());
        setupSlider(mouseSpeedSlider, 1, MouseSpeed.values().length - 1, 1);
        constrainPreferredWidth(mouseSpeedSlider, ROW_CONTROL_WIDTH);
        mouseSmoothnessSlider.setToolTipText("Controls natural mouse movement step density and trajectory texture");
        mouseSmoothnessSlider.setSnapToTicks(true);
        mouseSmoothnessSlider.setPaintTicks(true);
        mouseSmoothnessSlider.setPaintLabels(true);
        mouseSmoothnessSlider.setLabelTable(createMouseSmoothnessLabels());
        setupSlider(mouseSmoothnessSlider, 1, MouseSmoothness.values().length - 1, 1);
        constrainPreferredWidth(mouseSmoothnessSlider, ROW_CONTROL_WIDTH);
        advancedMouseControls.setToolTipText("Show detailed natural mouse timing and path controls");
        setupAdvancedTimingSlider(mouseReactionDelaySlider, "Reaction before movement starts");
        setupTimingRandomToggle(mouseReactionDelayRandom, "Randomize reaction delay within a min/max range");
        setupAdvancedTimingSlider(mouseReactionDelayMinSlider, "Minimum reaction delay before movement starts");
        setupAdvancedTimingSlider(mouseReactionDelayMaxSlider, "Maximum reaction delay before movement starts");
        setupAdvancedTimingSlider(mouseSettleDelaySlider, "Pause after movement before clicking");
        setupTimingRandomToggle(mouseSettleDelayRandom, "Randomize settle delay within a min/max range");
        setupAdvancedTimingSlider(mouseSettleDelayMinSlider, "Minimum pause after movement before clicking");
        setupAdvancedTimingSlider(mouseSettleDelayMaxSlider, "Maximum pause after movement before clicking");
        setupAdvancedTimingSlider(mouseButtonHoldSlider, "How long a click button stays down");
        setupTimingRandomToggle(mouseButtonHoldRandom, "Randomize button hold time within a min/max range");
        setupAdvancedTimingSlider(mouseButtonHoldMinSlider, "Minimum click button hold time");
        setupAdvancedTimingSlider(mouseButtonHoldMaxSlider, "Maximum click button hold time");
        setupAdvancedSlider(mouseCurveScaleSlider, "Curve strength percentage");
        setupAdvancedSlider(mousePathNoiseScaleSlider, "Low-frequency path drift percentage");
        setupAdvancedSlider(mouseMicroJitterScaleSlider, "Small per-step hand jitter percentage");
        setupAdvancedSlider(mouseOvershootScaleSlider, "Overshoot distance and likelihood percentage");
        setupAdvancedSlider(mouseCorrectionScaleSlider, "Corrective sub-movement percentage");
        setupAdvancedSlider(mouseEndpointErrorScaleSlider, "Endpoint miss radius percentage");
        setupAdvancedSlider(mouseDragStabilityScaleSlider, "Drag stabilization percentage");
        setupAdvancedSlider(mouseScrollBurstScaleSlider, "Scroll burst tick percentage");

        setLayout(new GridBagLayout());
        setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
        setupSlider(moveMouseRandomlyChance, 20, 100, 10);
        constrainPreferredWidth(moveMouseOffScreenChance, ROW_CONTROL_WIDTH);
        constrainPreferredWidth(moveMouseRandomlyChance, ROW_CONTROL_WIDTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = ROW_INSETS;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        JPanel naturalMouseRow = new JPanel(new BorderLayout(2, 0));
        naturalMouseRow.setOpaque(false);
        naturalMouseRow.add(useNaturalMouse, BorderLayout.WEST);
        naturalMouseRow.add(mouseEngineModeComboBox, BorderLayout.EAST);
        add(naturalMouseRow, gbc);

        gbc.insets = SECTION_INSETS;
        add(simulateMistakes, gbc);

        gbc.insets = ROW_INSETS;
        add(moveMouseOffScreen, gbc);
        add(moveMouseOffScreenChanceLabel, gbc);

        add(moveMouseOffScreenChance, gbc);

        add(moveMouseRandomly, gbc);

        add(moveMouseRandomlyChanceLabel, gbc);
        add(moveMouseRandomlyChance, gbc);

        add(mouseSpeedLabel, gbc);

        add(mouseSpeedSlider, gbc);

        JPanel smoothnessHeaderRow = new JPanel(new BorderLayout(2, 0));
        smoothnessHeaderRow.setOpaque(false);
        smoothnessHeaderRow.add(mouseSmoothnessLabel, BorderLayout.WEST);
        smoothnessHeaderRow.add(advancedMouseControls, BorderLayout.EAST);
        add(smoothnessHeaderRow, gbc);

        add(mouseSmoothnessSlider, gbc);

        buildAdvancedControlsPopup();

        setupActionListeners();

        // Make sure the default values on the UI match the current settings
        updateValues();
    }

    @Override
    public Dimension getPreferredSize()
    {
        Dimension preferredSize = super.getPreferredSize();
        return new Dimension(Math.min(preferredSize.width, CARD_WIDTH), preferredSize.height);
    }

    private void setupActionListeners()
    {
        useNaturalMouse.addActionListener(e -> {
            Rs2AntibanSettings.naturalMouse = useNaturalMouse.isSelected();
            Rs2AntibanSettings.saveToProfile();
        });
        mouseEngineModeComboBox.addActionListener(e -> {
            if (updatingValues) {
                return;
            }
            Object selected = mouseEngineModeComboBox.getSelectedItem();
            if (selected instanceof MouseEngineMode) {
                Rs2AntibanSettings.mouseEngineMode = (MouseEngineMode) selected;
                Rs2AntibanSettings.saveToProfile();
            }
        });
        simulateMistakes.addActionListener(e -> {
            Rs2AntibanSettings.simulateMistakes = simulateMistakes.isSelected();
            Rs2AntibanSettings.saveToProfile();
        });
        moveMouseOffScreen.addActionListener(e -> {
            Rs2AntibanSettings.moveMouseOffScreen = moveMouseOffScreen.isSelected();
            Rs2AntibanSettings.saveToProfile();
        });
        moveMouseOffScreenChance.addChangeListener(e -> {
            if (updatingValues) {
                return;
            }
            Rs2AntibanSettings.moveMouseOffScreenChance = moveMouseOffScreenChance.getValue() / 100.0;
            moveMouseOffScreenChanceLabel.setText("Move Mouse Off Screen (%): " + moveMouseOffScreenChance.getValue());
            if (!moveMouseOffScreenChance.getValueIsAdjusting()) {
                Rs2AntibanSettings.saveToProfile();
            }
        });
        moveMouseRandomly.addActionListener(e -> {
            Rs2AntibanSettings.moveMouseRandomly = moveMouseRandomly.isSelected();
            Rs2AntibanSettings.saveToProfile();
        });
        moveMouseRandomlyChance.addChangeListener(e -> {
            if (updatingValues) {
                return;
            }
            Rs2AntibanSettings.moveMouseRandomlyChance = moveMouseRandomlyChance.getValue() / 100.0;
            moveMouseRandomlyChanceLabel.setText("Random Mouse Movement (%): " + moveMouseRandomlyChance.getValue());
            if (!moveMouseRandomlyChance.getValueIsAdjusting()) {
                Rs2AntibanSettings.saveToProfile();
            }
        });

        mouseSpeedSlider.addChangeListener(e -> {
            MouseSpeed mouseSpeed = MouseSpeed.fromSliderIndex(mouseSpeedSlider.getValue());
            if (updatingValues) {
                return;
            }
            Rs2AntibanSettings.mouseSpeed = mouseSpeed;
            Rs2AntibanSettings.dynamicIntensity = false;
            Rs2Antiban.setActivityIntensity(mouseSpeed.toActivityIntensity());
            mouseSpeedLabel.setText("Mouse Speed: " + mouseSpeed.getName());
            if (!mouseSpeedSlider.getValueIsAdjusting()) {
                Rs2AntibanSettings.saveToProfile();
            }
        });
        mouseSmoothnessSlider.addChangeListener(e -> {
            MouseSmoothness mouseSmoothness = MouseSmoothness.fromSliderIndex(mouseSmoothnessSlider.getValue());
            if (updatingValues) {
                return;
            }
            Rs2AntibanSettings.mouseSmoothness = mouseSmoothness;
            mouseSmoothnessLabel.setText("Mouse Smoothness: " + mouseSmoothness.getName());
            if (!mouseSmoothnessSlider.getValueIsAdjusting()) {
                Rs2AntibanSettings.saveToProfile();
            }
        });
        advancedMouseControls.addActionListener(e -> setAdvancedControlsVisible(advancedMouseControls.isSelected()));
        bindTimingRandomToggle(mouseReactionDelayRandom,
                selected -> Rs2AntibanSettings.mouseReactionDelayRandom = selected);
        bindTimingRandomToggle(mouseSettleDelayRandom,
                selected -> Rs2AntibanSettings.mouseSettleDelayRandom = selected);
        bindTimingRandomToggle(mouseButtonHoldRandom,
                selected -> Rs2AntibanSettings.mouseButtonHoldRandom = selected);
        bindAdvancedSlider(mouseReactionDelaySlider, mouseReactionDelayLabel, "Reaction (ms)",
                value -> Rs2AntibanSettings.mouseReactionDelayMs = value);
        bindTimingRangeSliders(mouseReactionDelayMinSlider, mouseReactionDelayMinLabel, "Reaction Min (ms)",
                value -> Rs2AntibanSettings.mouseReactionDelayMinMs = value,
                mouseReactionDelayMaxSlider, mouseReactionDelayMaxLabel, "Reaction Max (ms)",
                value -> Rs2AntibanSettings.mouseReactionDelayMaxMs = value);
        bindAdvancedSlider(mouseSettleDelaySlider, mouseSettleDelayLabel, "Settle (ms)",
                value -> Rs2AntibanSettings.mouseSettleDelayMs = value);
        bindTimingRangeSliders(mouseSettleDelayMinSlider, mouseSettleDelayMinLabel, "Settle Min (ms)",
                value -> Rs2AntibanSettings.mouseSettleDelayMinMs = value,
                mouseSettleDelayMaxSlider, mouseSettleDelayMaxLabel, "Settle Max (ms)",
                value -> Rs2AntibanSettings.mouseSettleDelayMaxMs = value);
        bindAdvancedSlider(mouseButtonHoldSlider, mouseButtonHoldLabel, "Button Hold (ms)",
                value -> Rs2AntibanSettings.mouseButtonHoldMs = value);
        bindTimingRangeSliders(mouseButtonHoldMinSlider, mouseButtonHoldMinLabel, "Button Hold Min (ms)",
                value -> Rs2AntibanSettings.mouseButtonHoldMinMs = value,
                mouseButtonHoldMaxSlider, mouseButtonHoldMaxLabel, "Button Hold Max (ms)",
                value -> Rs2AntibanSettings.mouseButtonHoldMaxMs = value);
        bindAdvancedSlider(mouseCurveScaleSlider, mouseCurveScaleLabel, "Curve (%)",
                value -> Rs2AntibanSettings.mouseCurveScale = value);
        bindAdvancedSlider(mousePathNoiseScaleSlider, mousePathNoiseScaleLabel, "Path Noise (%)",
                value -> Rs2AntibanSettings.mousePathNoiseScale = value);
        bindAdvancedSlider(mouseMicroJitterScaleSlider, mouseMicroJitterScaleLabel, "Micro Jitter (%)",
                value -> Rs2AntibanSettings.mouseMicroJitterScale = value);
        bindAdvancedSlider(mouseOvershootScaleSlider, mouseOvershootScaleLabel, "Overshoot (%)",
                value -> Rs2AntibanSettings.mouseOvershootScale = value);
        bindAdvancedSlider(mouseCorrectionScaleSlider, mouseCorrectionScaleLabel, "Corrections (%)",
                value -> Rs2AntibanSettings.mouseCorrectionScale = value);
        bindAdvancedSlider(mouseEndpointErrorScaleSlider, mouseEndpointErrorScaleLabel, "Endpoint Error (%)",
                value -> Rs2AntibanSettings.mouseEndpointErrorScale = value);
        bindAdvancedSlider(mouseDragStabilityScaleSlider, mouseDragStabilityScaleLabel, "Drag Stability (%)",
                value -> Rs2AntibanSettings.mouseDragStabilityScale = value);
        bindAdvancedSlider(mouseScrollBurstScaleSlider, mouseScrollBurstScaleLabel, "Scroll Burst (%)",
                value -> Rs2AntibanSettings.mouseScrollBurstScale = value);
    }

    public void updateValues()
    {
        updatingValues = true;
        try {
            useNaturalMouse.setSelected(Rs2AntibanSettings.naturalMouse);
            mouseEngineModeComboBox.setSelectedItem(Rs2AntibanSettings.getConfiguredMouseEngineMode());
            simulateMistakes.setSelected(Rs2AntibanSettings.simulateMistakes);
            moveMouseOffScreen.setSelected(Rs2AntibanSettings.moveMouseOffScreen);
            moveMouseOffScreenChance.setValue((int) (Rs2AntibanSettings.moveMouseOffScreenChance * 100));
            moveMouseRandomly.setSelected(Rs2AntibanSettings.moveMouseRandomly);
            moveMouseRandomlyChance.setValue((int) (Rs2AntibanSettings.moveMouseRandomlyChance * 100));

            MouseSpeed mouseSpeed = Rs2AntibanSettings.getEffectiveMouseSpeed(
                    Rs2Antiban.getActivityIntensity(), Rs2Antiban.getPlayStyle());
            mouseSpeedSlider.setValue(mouseSpeed.getSliderIndex());
            mouseSpeedLabel.setText(Rs2AntibanSettings.dynamicIntensity
                    ? "Mouse Speed: Dynamic (" + mouseSpeed.getName() + ")"
                    : "Mouse Speed: " + mouseSpeed.getName());
            MouseSmoothness mouseSmoothness = Rs2AntibanSettings.getConfiguredMouseSmoothness();
            mouseSmoothnessSlider.setValue(mouseSmoothness.getSliderIndex());
            mouseSmoothnessLabel.setText("Mouse Smoothness: " + mouseSmoothness.getName());
            setSliderValueIfNotAdjusting(mouseReactionDelaySlider, Rs2AntibanSettings.mouseReactionDelayMs);
            mouseReactionDelayRandom.setSelected(Rs2AntibanSettings.mouseReactionDelayRandom);
            setTimingRangeValuesIfNotAdjusting(
                    mouseReactionDelayMinSlider, mouseReactionDelayMaxSlider,
                    Rs2AntibanSettings.mouseReactionDelayMinMs, Rs2AntibanSettings.mouseReactionDelayMaxMs);
            setSliderValueIfNotAdjusting(mouseSettleDelaySlider, Rs2AntibanSettings.mouseSettleDelayMs);
            mouseSettleDelayRandom.setSelected(Rs2AntibanSettings.mouseSettleDelayRandom);
            setTimingRangeValuesIfNotAdjusting(
                    mouseSettleDelayMinSlider, mouseSettleDelayMaxSlider,
                    Rs2AntibanSettings.mouseSettleDelayMinMs, Rs2AntibanSettings.mouseSettleDelayMaxMs);
            setSliderValueIfNotAdjusting(mouseButtonHoldSlider, Rs2AntibanSettings.mouseButtonHoldMs);
            mouseButtonHoldRandom.setSelected(Rs2AntibanSettings.mouseButtonHoldRandom);
            setTimingRangeValuesIfNotAdjusting(
                    mouseButtonHoldMinSlider, mouseButtonHoldMaxSlider,
                    Rs2AntibanSettings.mouseButtonHoldMinMs, Rs2AntibanSettings.mouseButtonHoldMaxMs);
            setSliderValueIfNotAdjusting(mouseCurveScaleSlider, Rs2AntibanSettings.mouseCurveScale);
            setSliderValueIfNotAdjusting(mousePathNoiseScaleSlider, Rs2AntibanSettings.mousePathNoiseScale);
            setSliderValueIfNotAdjusting(mouseMicroJitterScaleSlider, Rs2AntibanSettings.mouseMicroJitterScale);
            setSliderValueIfNotAdjusting(mouseOvershootScaleSlider, Rs2AntibanSettings.mouseOvershootScale);
            setSliderValueIfNotAdjusting(mouseCorrectionScaleSlider, Rs2AntibanSettings.mouseCorrectionScale);
            setSliderValueIfNotAdjusting(mouseEndpointErrorScaleSlider, Rs2AntibanSettings.mouseEndpointErrorScale);
            setSliderValueIfNotAdjusting(mouseDragStabilityScaleSlider, Rs2AntibanSettings.mouseDragStabilityScale);
            setSliderValueIfNotAdjusting(mouseScrollBurstScaleSlider, Rs2AntibanSettings.mouseScrollBurstScale);
            updateAdvancedLabels();
        } finally {
            updatingValues = false;
        }
        moveMouseOffScreenChanceLabel.setText("Move Mouse Off Screen (%): " + moveMouseOffScreenChance.getValue());
        moveMouseRandomlyChanceLabel.setText("Random Mouse Movement (%): " + moveMouseRandomlyChance.getValue());
        setAdvancedControlsVisible(advancedMouseControls.isSelected());
    }

    private Hashtable<Integer, JLabel> createMouseSpeedLabels()
    {
        Hashtable<Integer, JLabel> labels = new Hashtable<>();
        for (MouseSpeed mouseSpeed : MouseSpeed.values()) {
            labels.put(mouseSpeed.getSliderIndex(), new JLabel(mouseSpeed.getName()));
        }
        return labels;
    }

    private Hashtable<Integer, JLabel> createMouseSmoothnessLabels()
    {
        Hashtable<Integer, JLabel> labels = new Hashtable<>();
        for (MouseSmoothness mouseSmoothness : MouseSmoothness.values()) {
            labels.put(mouseSmoothness.getSliderIndex(), new JLabel(mouseSmoothness.getName()));
        }
        return labels;
    }

    private static void constrainPreferredWidth(JComponent component, int preferredWidth)
    {
        Dimension preferredSize = component.getPreferredSize();
        Dimension minimumSize = component.getMinimumSize();

        component.setPreferredSize(new Dimension(preferredWidth, preferredSize.height));
        component.setMinimumSize(new Dimension(ROW_CONTROL_MIN_WIDTH, minimumSize.height));
    }

    private void setupAdvancedSlider(JSlider slider, String tooltip)
    {
        slider.setToolTipText(tooltip);
        slider.setSnapToTicks(true);
        setupSlider(slider, 50, slider.getMaximum(), 10);
        slider.setPaintLabels(false);
        constrainPreferredWidth(slider, ROW_CONTROL_WIDTH);
    }

    private void setupAdvancedTimingSlider(JSlider slider, String tooltip)
    {
        slider.setToolTipText(tooltip);
        slider.setSnapToTicks(true);
        setupSlider(slider, 50, slider.getMaximum(), 5);
        slider.setPaintLabels(false);
        constrainPreferredWidth(slider, ROW_CONTROL_WIDTH);
    }

    private void setupTimingRandomToggle(JCheckBox checkBox, String tooltip)
    {
        checkBox.setToolTipText(tooltip);
        checkBox.setOpaque(false);
        checkBox.setFocusable(false);
    }

    private void buildAdvancedControlsPopup()
    {
        advancedMousePopupPanel.setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
        advancedMousePopupScrollPane.setBorder(null);
        advancedMousePopupScrollPane.setOpaque(false);
        advancedMousePopupScrollPane.getViewport().setOpaque(false);
        advancedMousePopupScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        advancedMousePopupScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        advancedMousePopupScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        advancedMousePopupScrollPane.setPreferredSize(new Dimension(ADVANCED_POPUP_WIDTH, ADVANCED_POPUP_HEIGHT));
        advancedMousePopup.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR.darker()),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));

        GridBagConstraints popupGbc = new GridBagConstraints();
        popupGbc.insets = ROW_INSETS;
        popupGbc.anchor = GridBagConstraints.WEST;
        popupGbc.fill = GridBagConstraints.HORIZONTAL;
        popupGbc.weightx = 1.0;
        popupGbc.gridx = 0;
        popupGbc.gridy = GridBagConstraints.RELATIVE;
        popupGbc.gridwidth = GridBagConstraints.REMAINDER;

        addTimingControl(advancedMousePopupPanel, mouseReactionDelayLabel, mouseReactionDelaySlider,
                mouseReactionDelayRandom, mouseReactionDelayMinLabel, mouseReactionDelayMinSlider,
                mouseReactionDelayMaxLabel, mouseReactionDelayMaxSlider, popupGbc);
        addTimingControl(advancedMousePopupPanel, mouseSettleDelayLabel, mouseSettleDelaySlider,
                mouseSettleDelayRandom, mouseSettleDelayMinLabel, mouseSettleDelayMinSlider,
                mouseSettleDelayMaxLabel, mouseSettleDelayMaxSlider, popupGbc);
        addTimingControl(advancedMousePopupPanel, mouseButtonHoldLabel, mouseButtonHoldSlider,
                mouseButtonHoldRandom, mouseButtonHoldMinLabel, mouseButtonHoldMinSlider,
                mouseButtonHoldMaxLabel, mouseButtonHoldMaxSlider, popupGbc);
        addAdvancedControl(advancedMousePopupPanel, mouseCurveScaleLabel, mouseCurveScaleSlider, popupGbc);
        addAdvancedControl(advancedMousePopupPanel, mousePathNoiseScaleLabel, mousePathNoiseScaleSlider, popupGbc);
        addAdvancedControl(advancedMousePopupPanel, mouseMicroJitterScaleLabel, mouseMicroJitterScaleSlider, popupGbc);
        addAdvancedControl(advancedMousePopupPanel, mouseOvershootScaleLabel, mouseOvershootScaleSlider, popupGbc);
        addAdvancedControl(advancedMousePopupPanel, mouseCorrectionScaleLabel, mouseCorrectionScaleSlider, popupGbc);
        addAdvancedControl(advancedMousePopupPanel, mouseEndpointErrorScaleLabel, mouseEndpointErrorScaleSlider, popupGbc);
        addAdvancedControl(advancedMousePopupPanel, mouseDragStabilityScaleLabel, mouseDragStabilityScaleSlider, popupGbc);
        addAdvancedControl(advancedMousePopupPanel, mouseScrollBurstScaleLabel, mouseScrollBurstScaleSlider, popupGbc);

        advancedMousePopup.add(advancedMousePopupScrollPane);
    }

    private void addAdvancedControl(JPanel panel, JLabel label, JSlider slider, GridBagConstraints gbc)
    {
        panel.add(label, gbc);
        panel.add(slider, gbc);
    }

    private void addTimingControl(
            JPanel panel,
            JLabel valueLabel,
            JSlider valueSlider,
            JCheckBox randomToggle,
            JLabel minLabel,
            JSlider minSlider,
            JLabel maxLabel,
            JSlider maxSlider,
            GridBagConstraints gbc)
    {
        JPanel headerRow = new JPanel(new BorderLayout(2, 0));
        headerRow.setOpaque(false);
        headerRow.add(valueLabel, BorderLayout.WEST);
        headerRow.add(randomToggle, BorderLayout.EAST);
        panel.add(headerRow, gbc);
        panel.add(valueSlider, gbc);
        panel.add(minLabel, gbc);
        panel.add(minSlider, gbc);
        panel.add(maxLabel, gbc);
        panel.add(maxSlider, gbc);
    }

    private void bindAdvancedSlider(JSlider slider, JLabel label, String labelPrefix, IntConsumer settingUpdater)
    {
        slider.addChangeListener(e -> {
            label.setText(labelPrefix + ": " + slider.getValue());
            if (updatingValues) {
                return;
            }
            settingUpdater.accept(slider.getValue());
            if (!slider.getValueIsAdjusting()) {
                Rs2AntibanSettings.saveToProfile();
            }
        });
    }

    private void bindTimingRandomToggle(JCheckBox checkBox, Consumer<Boolean> settingUpdater)
    {
        checkBox.addActionListener(e -> {
            boolean selected = checkBox.isSelected();
            settingUpdater.accept(selected);
            updateAdvancedLabels();
            Rs2AntibanSettings.saveToProfile();
        });
    }

    private void bindTimingRangeSliders(
            JSlider minSlider,
            JLabel minLabel,
            String minLabelPrefix,
            IntConsumer minSettingUpdater,
            JSlider maxSlider,
            JLabel maxLabel,
            String maxLabelPrefix,
            IntConsumer maxSettingUpdater)
    {
        minSlider.addChangeListener(e -> {
            if (updatingValues) {
                updateTimingRangeLabels(minSlider, minLabel, minLabelPrefix, maxSlider, maxLabel, maxLabelPrefix);
                return;
            }
            if (minSlider.getValue() > maxSlider.getValue()) {
                maxSlider.setValue(minSlider.getValue());
            }
            updateTimingRangeLabels(minSlider, minLabel, minLabelPrefix, maxSlider, maxLabel, maxLabelPrefix);
            minSettingUpdater.accept(minSlider.getValue());
            maxSettingUpdater.accept(maxSlider.getValue());
            if (!minSlider.getValueIsAdjusting()) {
                Rs2AntibanSettings.saveToProfile();
            }
        });

        maxSlider.addChangeListener(e -> {
            if (updatingValues) {
                updateTimingRangeLabels(minSlider, minLabel, minLabelPrefix, maxSlider, maxLabel, maxLabelPrefix);
                return;
            }
            if (maxSlider.getValue() < minSlider.getValue()) {
                minSlider.setValue(maxSlider.getValue());
            }
            updateTimingRangeLabels(minSlider, minLabel, minLabelPrefix, maxSlider, maxLabel, maxLabelPrefix);
            minSettingUpdater.accept(minSlider.getValue());
            maxSettingUpdater.accept(maxSlider.getValue());
            if (!maxSlider.getValueIsAdjusting()) {
                Rs2AntibanSettings.saveToProfile();
            }
        });
    }

    private void updateTimingRangeLabels(
            JSlider minSlider,
            JLabel minLabel,
            String minLabelPrefix,
            JSlider maxSlider,
            JLabel maxLabel,
            String maxLabelPrefix)
    {
        minLabel.setText(minLabelPrefix + ": " + minSlider.getValue());
        maxLabel.setText(maxLabelPrefix + ": " + maxSlider.getValue());
    }

    private static void setSliderValueIfNotAdjusting(JSlider slider, int value)
    {
        if (!slider.getValueIsAdjusting()) {
            slider.setValue(value);
        }
    }

    private static void setTimingRangeValuesIfNotAdjusting(
            JSlider minSlider,
            JSlider maxSlider,
            int minValue,
            int maxValue)
    {
        if (!minSlider.getValueIsAdjusting() && !maxSlider.getValueIsAdjusting()) {
            minSlider.setValue(minValue);
            maxSlider.setValue(maxValue);
        }
    }

    private void updateAdvancedLabels()
    {
        mouseReactionDelayLabel.setText(mouseReactionDelayRandom.isSelected()
                ? "Reaction (random)"
                : "Reaction (ms): " + mouseReactionDelaySlider.getValue());
        mouseReactionDelayMinLabel.setText("Reaction Min (ms): " + mouseReactionDelayMinSlider.getValue());
        mouseReactionDelayMaxLabel.setText("Reaction Max (ms): " + mouseReactionDelayMaxSlider.getValue());
        mouseSettleDelayLabel.setText(mouseSettleDelayRandom.isSelected()
                ? "Settle (random)"
                : "Settle (ms): " + mouseSettleDelaySlider.getValue());
        mouseSettleDelayMinLabel.setText("Settle Min (ms): " + mouseSettleDelayMinSlider.getValue());
        mouseSettleDelayMaxLabel.setText("Settle Max (ms): " + mouseSettleDelayMaxSlider.getValue());
        mouseButtonHoldLabel.setText(mouseButtonHoldRandom.isSelected()
                ? "Button Hold (random)"
                : "Button Hold (ms): " + mouseButtonHoldSlider.getValue());
        mouseButtonHoldMinLabel.setText("Button Hold Min (ms): " + mouseButtonHoldMinSlider.getValue());
        mouseButtonHoldMaxLabel.setText("Button Hold Max (ms): " + mouseButtonHoldMaxSlider.getValue());
        mouseCurveScaleLabel.setText("Curve (%): " + mouseCurveScaleSlider.getValue());
        mousePathNoiseScaleLabel.setText("Path Noise (%): " + mousePathNoiseScaleSlider.getValue());
        mouseMicroJitterScaleLabel.setText("Micro Jitter (%): " + mouseMicroJitterScaleSlider.getValue());
        mouseOvershootScaleLabel.setText("Overshoot (%): " + mouseOvershootScaleSlider.getValue());
        mouseCorrectionScaleLabel.setText("Corrections (%): " + mouseCorrectionScaleSlider.getValue());
        mouseEndpointErrorScaleLabel.setText("Endpoint Error (%): " + mouseEndpointErrorScaleSlider.getValue());
        mouseDragStabilityScaleLabel.setText("Drag Stability (%): " + mouseDragStabilityScaleSlider.getValue());
        mouseScrollBurstScaleLabel.setText("Scroll Burst (%): " + mouseScrollBurstScaleSlider.getValue());
        updateTimingControlStates();
    }

    private void updateTimingControlStates()
    {
        boolean advancedVisible = advancedMouseControls.isSelected();
        updateTimingControlState(mouseReactionDelayRandom, mouseReactionDelayLabel, mouseReactionDelaySlider,
                mouseReactionDelayMinLabel, mouseReactionDelayMinSlider,
                mouseReactionDelayMaxLabel, mouseReactionDelayMaxSlider, advancedVisible);
        updateTimingControlState(mouseSettleDelayRandom, mouseSettleDelayLabel, mouseSettleDelaySlider,
                mouseSettleDelayMinLabel, mouseSettleDelayMinSlider,
                mouseSettleDelayMaxLabel, mouseSettleDelayMaxSlider, advancedVisible);
        updateTimingControlState(mouseButtonHoldRandom, mouseButtonHoldLabel, mouseButtonHoldSlider,
                mouseButtonHoldMinLabel, mouseButtonHoldMinSlider,
                mouseButtonHoldMaxLabel, mouseButtonHoldMaxSlider, advancedVisible);
    }

    private void updateTimingControlState(
            JCheckBox randomToggle,
            JLabel valueLabel,
            JSlider valueSlider,
            JLabel minLabel,
            JSlider minSlider,
            JLabel maxLabel,
            JSlider maxSlider,
            boolean advancedVisible)
    {
        boolean random = randomToggle.isSelected();
        valueLabel.setVisible(advancedVisible);
        randomToggle.setVisible(advancedVisible);
        valueSlider.setVisible(advancedVisible && !random);
        minLabel.setVisible(advancedVisible && random);
        minSlider.setVisible(advancedVisible && random);
        maxLabel.setVisible(advancedVisible && random);
        maxSlider.setVisible(advancedVisible && random);
        valueSlider.setEnabled(!random);
        minLabel.setEnabled(random);
        minSlider.setEnabled(random);
        maxLabel.setEnabled(random);
        maxSlider.setEnabled(random);
    }

    private void setAdvancedControlsVisible(boolean visible)
    {
        for (JComponent component : advancedControlComponents()) {
            component.setVisible(visible);
        }
        updateTimingControlStates();

        advancedMousePopup.revalidate();
        advancedMousePopup.repaint();

        if (visible && advancedMouseControls.isShowing()) {
            showAdvancedControlsPopup();
        } else if (!visible) {
            advancedMousePopup.setVisible(false);
        }

        revalidate();
        repaint();
    }

    private void showAdvancedControlsPopup()
    {
        int xOffset = Math.min(0, advancedMouseControls.getWidth() - advancedMousePopup.getPreferredSize().width);
        advancedMousePopup.show(advancedMouseControls, xOffset, advancedMouseControls.getHeight());
    }

    private List<JComponent> advancedControlComponents()
    {
        return Arrays.asList(
                mouseReactionDelayLabel,
                mouseReactionDelaySlider,
                mouseReactionDelayRandom,
                mouseReactionDelayMinLabel,
                mouseReactionDelayMinSlider,
                mouseReactionDelayMaxLabel,
                mouseReactionDelayMaxSlider,
                mouseSettleDelayLabel,
                mouseSettleDelaySlider,
                mouseSettleDelayRandom,
                mouseSettleDelayMinLabel,
                mouseSettleDelayMinSlider,
                mouseSettleDelayMaxLabel,
                mouseSettleDelayMaxSlider,
                mouseButtonHoldLabel,
                mouseButtonHoldSlider,
                mouseButtonHoldRandom,
                mouseButtonHoldMinLabel,
                mouseButtonHoldMinSlider,
                mouseButtonHoldMaxLabel,
                mouseButtonHoldMaxSlider,
                mouseCurveScaleLabel,
                mouseCurveScaleSlider,
                mousePathNoiseScaleLabel,
                mousePathNoiseScaleSlider,
                mouseMicroJitterScaleLabel,
                mouseMicroJitterScaleSlider,
                mouseOvershootScaleLabel,
                mouseOvershootScaleSlider,
                mouseCorrectionScaleLabel,
                mouseCorrectionScaleSlider,
                mouseEndpointErrorScaleLabel,
                mouseEndpointErrorScaleSlider,
                mouseDragStabilityScaleLabel,
                mouseDragStabilityScaleSlider,
                mouseScrollBurstScaleLabel,
                mouseScrollBurstScaleSlider);
    }
}
