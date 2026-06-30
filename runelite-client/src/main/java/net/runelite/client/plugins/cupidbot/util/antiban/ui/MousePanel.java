package net.runelite.client.plugins.cupidbot.util.antiban.ui;

import net.runelite.client.plugins.cupidbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.cupidbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSmoothness;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSpeed;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;

import static net.runelite.client.plugins.cupidbot.util.antiban.ui.UiHelper.setupSlider;

public class MousePanel extends JPanel
{
    private static final Insets ROW_INSETS = new Insets(3, 5, 3, 5);
    private static final Insets SECTION_INSETS = new Insets(8, 5, 3, 5);

    private boolean updatingValues;

    private final JCheckBox useNaturalMouse = new JCheckBox("Use Natural Mouse");
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

    public MousePanel()
    {
        useNaturalMouse.setToolTipText("Simulate human-like mouse movements");
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
        mouseSmoothnessSlider.setToolTipText("Controls natural mouse movement step density and trajectory texture");
        mouseSmoothnessSlider.setSnapToTicks(true);
        mouseSmoothnessSlider.setPaintTicks(true);
        mouseSmoothnessSlider.setPaintLabels(true);
        mouseSmoothnessSlider.setLabelTable(createMouseSmoothnessLabels());
        setupSlider(mouseSmoothnessSlider, 1, MouseSmoothness.values().length - 1, 1);

        setLayout(new GridBagLayout());
        setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
        setupSlider(moveMouseRandomlyChance, 20, 100, 10);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = ROW_INSETS;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;

        add(useNaturalMouse, gbc);

        gbc.insets = SECTION_INSETS;
        add(simulateMistakes, gbc);

        gbc.insets = ROW_INSETS;
        add(moveMouseOffScreen, gbc);
        add(moveMouseOffScreenChanceLabel, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(moveMouseOffScreenChance, gbc);

        gbc.fill = GridBagConstraints.NONE;
        add(moveMouseRandomly, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(moveMouseRandomlyChanceLabel, gbc);
        add(moveMouseRandomlyChance, gbc);

        gbc.fill = GridBagConstraints.NONE;
        add(mouseSpeedLabel, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(mouseSpeedSlider, gbc);

        gbc.fill = GridBagConstraints.NONE;
        add(mouseSmoothnessLabel, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(mouseSmoothnessSlider, gbc);

        setupActionListeners();

        // Make sure the default values on the UI match the current settings
        updateValues();
    }

    private void setupActionListeners()
    {
        useNaturalMouse.addActionListener(e -> {
            Rs2AntibanSettings.naturalMouse = useNaturalMouse.isSelected();
            Rs2AntibanSettings.saveToProfile();
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
    }

    public void updateValues()
    {
        updatingValues = true;
        try {
            useNaturalMouse.setSelected(Rs2AntibanSettings.naturalMouse);
            simulateMistakes.setSelected(Rs2AntibanSettings.simulateMistakes);
            moveMouseOffScreen.setSelected(Rs2AntibanSettings.moveMouseOffScreen);
            moveMouseOffScreenChance.setValue((int) (Rs2AntibanSettings.moveMouseOffScreenChance * 100));
            moveMouseRandomly.setSelected(Rs2AntibanSettings.moveMouseRandomly);
            moveMouseRandomlyChance.setValue((int) (Rs2AntibanSettings.moveMouseRandomlyChance * 100));

            MouseSpeed mouseSpeed = Rs2AntibanSettings.getEffectiveMouseSpeed(Rs2Antiban.getActivityIntensity());
            mouseSpeedSlider.setValue(mouseSpeed.getSliderIndex());
            mouseSpeedLabel.setText(Rs2AntibanSettings.dynamicIntensity
                    ? "Mouse Speed: Dynamic (" + mouseSpeed.getName() + ")"
                    : "Mouse Speed: " + mouseSpeed.getName());
            MouseSmoothness mouseSmoothness = Rs2AntibanSettings.getConfiguredMouseSmoothness();
            mouseSmoothnessSlider.setValue(mouseSmoothness.getSliderIndex());
            mouseSmoothnessLabel.setText("Mouse Smoothness: " + mouseSmoothness.getName());
        } finally {
            updatingValues = false;
        }
        moveMouseOffScreenChanceLabel.setText("Move Mouse Off Screen (%): " + moveMouseOffScreenChance.getValue());
        moveMouseRandomlyChanceLabel.setText("Random Mouse Movement (%): " + moveMouseRandomlyChance.getValue());
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
}
