package net.runelite.client.plugins.cupidbot.util.antiban;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.cupidbot.CupidBot;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.ActivityIntensity;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseEngineMode;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSmoothness;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSpeed;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.PlayStyle;
import net.runelite.client.plugins.cupidbot.util.mouse.engine.MouseMovementTuning;

/**
 * Provides configuration settings for the anti-ban system used by various plugins within the bot framework.
 *
 * <p>
 * The <code>Rs2AntibanSettings</code> class contains a collection of static fields that define behaviors
 * and settings related to anti-ban mechanisms. These settings control how the bot simulates human-like
 * behavior to avoid detection during automated tasks. Each setting adjusts a specific aspect of the
 * anti-ban system, including break patterns, mouse movements, play style variability, and other behaviors
 * designed to mimic natural human interaction with the game.
 * </p>
 *
 * <h3>Main Features:</h3>
 * <ul>
 *   <li><strong>Action Cooldowns:</strong> Controls the cooldown behavior of actions, including random intervals
 *   and non-linear patterns.</li>
 *   <li><strong>Micro Breaks:</strong> Defines settings for taking small breaks at random intervals to simulate human pauses.</li>
 *   <li><strong>Play Style Simulation:</strong> Includes variables to simulate different play styles, attention span,
 *   and behavioral variability to create a more realistic user profile.</li>
 *   <li><strong>Mouse Movements:</strong> Settings to control mouse behavior, such as moving off-screen or randomly,
 *   mimicking natural user actions.</li>
 *   <li><strong>Dynamic Behaviors:</strong> Provides options to dynamically adjust activity intensity and behavior
 *   based on context and time of day.</li>
 * </ul>
 *
 * <h3>Fields:</h3>
 * <ul>
 *   <li><code>actionCooldownActive</code>: Tracks whether action cooldowns are currently active.</li>
 *   <li><code>microBreakActive</code>: Indicates if a micro break is currently active.</li>
 *   <li><code>antibanEnabled</code>: Globally enables or disables the anti-ban system.</li>
 *   <li><code>usePlayStyle</code>: Determines whether play style simulation is active.</li>
 *   <li><code>randomIntervals</code>: Enables random intervals between actions to avoid detection.</li>
 *   <li><code>simulateFatigue</code>: Simulates user fatigue by introducing delays or slower actions.</li>
 *   <li><code>simulateAttentionSpan</code>: Simulates varying levels of user attention over time.</li>
 *   <li><code>behavioralVariability</code>: Adds variability to actions to simulate a human's inconsistency.</li>
 *   <li><code>nonLinearIntervals</code>: Activates non-linear time intervals between actions.</li>
 *   <li><code>profileSwitching</code>: Simulates user behavior switching profiles at intervals.</li>
 *   <li><code>timeOfDayAdjust</code>: (TODO) Adjusts behaviors based on the time of day.</li>
 *   <li><code>simulateMistakes</code>: Simulates user mistakes, often controlled by natural mouse movements.</li>
 *   <li><code>naturalMouse</code>: Enables natural-looking mouse movements.</li>
 *   <li><code>moveMouseOffScreen</code>: Moves the mouse off-screen during breaks to simulate user behavior.</li>
 *   <li><code>moveMouseRandomly</code>: Moves the mouse randomly to simulate human inconsistency.</li>
 *   <li><code>contextualVariability</code>: Adjusts behaviors based on the context of the user's actions.</li>
 *   <li><code>dynamicIntensity</code>: Dynamically adjusts the intensity of user actions based on context.</li>
 *   <li><code>dynamicActivity</code>: Adjusts activities dynamically based on the user's behavior profile.</li>
 *   <li><code>devDebug</code>: Enables debug mode for developers to inspect the anti-ban system's state.</li>
 *   <li><code>takeMicroBreaks</code>: Controls whether the bot takes micro breaks at random intervals.</li>
 *   <li><code>playSchedule</code>: (TODO) Allows scheduling of playtime based on specific conditions.</li>
 *   <li><code>universalAntiban</code>: Applies the same anti-ban settings across all plugins.</li>
 *   <li><code>microBreakDurationLow</code>: Minimum duration for micro breaks, in minutes.</li>
 *   <li><code>microBreakDurationHigh</code>: Maximum duration for micro breaks, in minutes.</li>
 *   <li><code>actionCooldownChance</code>: Probability of triggering an action cooldown.</li>
 *   <li><code>microBreakChance</code>: Probability of taking a micro break.</li>
 *   <li><code>moveMouseRandomlyChance</code>: Probability of moving the mouse randomly.</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * <p>
 * These settings are typically used by anti-ban mechanisms within various plugins to adjust their behavior
 * dynamically based on the user's preferences or to simulate human-like play styles. Developers can adjust
 * these fields based on the needs of their specific automation scripts.
 * </p>
 *
 * <h3>Example:</h3>
 * <pre>
 * // Enable fatigue simulation and random intervals
 * Rs2AntibanSettings.simulateFatigue = true;
 * Rs2AntibanSettings.randomIntervals = true;
 *
 * // Set the micro break chance to 20%
 * Rs2AntibanSettings.microBreakChance = 0.2;
 * </pre>
 */

@Slf4j
public class Rs2AntibanSettings {
    private static final String CONFIG_GROUP = "CupidBotAntiban";
    private static final String CONFIG_KEY = "settings";
    private static final Gson GSON = new Gson();
    private static volatile ConfigManager profileConfigManager;

    private Rs2AntibanSettings() {
        throw new IllegalStateException("Utility class");
    }

    private static class PersistentSettings {
        private Boolean antibanEnabled;
        private Boolean usePlayStyle;
        private Boolean randomIntervals;
        private Boolean simulateFatigue;
        private Boolean simulateAttentionSpan;
        private Boolean behavioralVariability;
        private Boolean nonLinearIntervals;
        private Boolean profileSwitching;
        private Boolean timeOfDayAdjust;
        private Boolean simulateMistakes;
        private Boolean naturalMouse;
        private Boolean moveMouseOffScreen;
        private Boolean moveMouseRandomly;
        private Boolean contextualVariability;
        private Boolean dynamicIntensity;
        private Boolean dynamicActivity;
        private Boolean devDebug;
        private Boolean overwriteScriptSettings;
        private Boolean takeMicroBreaks;
        private Boolean playSchedule;
        private Boolean universalAntiban;
        private Integer microBreakDurationLow;
        private Integer microBreakDurationHigh;
        private Double actionCooldownChance;
        private Double microBreakChance;
        private Double moveMouseRandomlyChance;
        private Double moveMouseOffScreenChance;
        private String mouseSpeed;
        private String mouseSmoothness;
        private String mouseEngineMode;
        private Integer mouseReactionDelayMs;
        private Boolean mouseReactionDelayRandom;
        private Integer mouseReactionDelayMinMs;
        private Integer mouseReactionDelayMaxMs;
        private Integer mouseSettleDelayMs;
        private Boolean mouseSettleDelayRandom;
        private Integer mouseSettleDelayMinMs;
        private Integer mouseSettleDelayMaxMs;
        private Integer mouseButtonHoldMs;
        private Boolean mouseButtonHoldRandom;
        private Integer mouseButtonHoldMinMs;
        private Integer mouseButtonHoldMaxMs;
        private Integer mouseCurveScale;
        private Integer mousePathNoiseScale;
        private Integer mouseMicroJitterScale;
        private Integer mouseOvershootScale;
        private Integer mouseCorrectionScale;
    }

    public static final class SettingsSnapshot {
        private final PersistentSettings settings;

        private SettingsSnapshot(PersistentSettings settings) {
            this.settings = settings;
        }
    }

    static void setProfileConfigManager(ConfigManager configManager) {
        profileConfigManager = configManager;
    }

    private static ConfigManager getProfileConfigManager() {
        ConfigManager configManager = profileConfigManager;
        return configManager != null ? configManager : CupidBot.getConfigManager();
    }

    public static void saveToProfile() {
        saveToProfile(getProfileConfigManager());
    }

    static void saveToProfile(ConfigManager configManager) {
        if (configManager == null) {
            log.debug("ConfigManager not available, skipping antiban settings save");
            return;
        }

        PersistentSettings settings = snapshot();
        try {
            configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY, GSON.toJson(settings));
            configManager.sendConfig();
        } catch (Exception ex) {
            log.warn("Unable to save antiban settings to profile", ex);
        }
    }

    public static boolean loadFromProfile() {
        return loadFromProfile(getProfileConfigManager());
    }

    static boolean loadFromProfile(ConfigManager configManager) {
        if (configManager == null) {
            log.debug("ConfigManager not available, skipping antiban settings load");
            return false;
        }

        String json;
        try {
            json = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY);
        } catch (Exception ex) {
            log.warn("Unable to load antiban settings from profile", ex);
            return false;
        }

        if (json == null || json.isEmpty()) {
            return false;
        }

        try {
            PersistentSettings settings = GSON.fromJson(json, PersistentSettings.class);
            if (settings == null) {
                return false;
            }
            reset();
            apply(settings);
            return true;
        } catch (JsonSyntaxException ex) {
            log.warn("Unable to parse antiban settings from profile", ex);
            return false;
        }
    }

    public static MouseSpeed getConfiguredMouseSpeed() {
        return mouseSpeed != null ? mouseSpeed : MouseSpeed.DEFAULT;
    }

    public static MouseSmoothness getConfiguredMouseSmoothness() {
        return mouseSmoothness != null ? mouseSmoothness : MouseSmoothness.DEFAULT;
    }

    public static MouseEngineMode getConfiguredMouseEngineMode() {
        return mouseEngineMode != null ? mouseEngineMode : MouseEngineMode.DEFAULT;
    }

    public static SettingsSnapshot captureSettings() {
        return new SettingsSnapshot(snapshot());
    }

    public static void restoreSettings(SettingsSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        reset();
        apply(snapshot.settings);
    }

    static void restoreMouseSettings(SettingsSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        applyMouseSettings(snapshot.settings);
    }

    public static MouseSpeed getEffectiveMouseSpeed(ActivityIntensity activityIntensity) {
        return getEffectiveMouseSpeed(activityIntensity, Rs2Antiban.getPlayStyle());
    }

    public static MouseSpeed getEffectiveMouseSpeed(ActivityIntensity activityIntensity, PlayStyle playStyle) {
        if (dynamicIntensity) {
            MouseSpeed playStyleSpeed = MouseSpeed.fromPlayStyle(playStyle);
            if (playStyleSpeed != null) {
                return playStyleSpeed;
            }
            if (playStyle == PlayStyle.RANDOM) {
                return getConfiguredMouseSpeed();
            }
            return MouseSpeed.fromActivityIntensity(activityIntensity);
        }
        return getConfiguredMouseSpeed();
    }

    private static PersistentSettings snapshot() {
        PersistentSettings settings = new PersistentSettings();
        settings.antibanEnabled = antibanEnabled;
        settings.usePlayStyle = usePlayStyle;
        settings.randomIntervals = randomIntervals;
        settings.simulateFatigue = simulateFatigue;
        settings.simulateAttentionSpan = simulateAttentionSpan;
        settings.behavioralVariability = behavioralVariability;
        settings.nonLinearIntervals = nonLinearIntervals;
        settings.profileSwitching = profileSwitching;
        settings.timeOfDayAdjust = timeOfDayAdjust;
        settings.simulateMistakes = simulateMistakes;
        settings.naturalMouse = naturalMouse;
        settings.moveMouseOffScreen = moveMouseOffScreen;
        settings.moveMouseRandomly = moveMouseRandomly;
        settings.contextualVariability = contextualVariability;
        settings.dynamicIntensity = dynamicIntensity;
        settings.dynamicActivity = dynamicActivity;
        settings.devDebug = devDebug;
        settings.overwriteScriptSettings = overwriteScriptSettings;
        settings.takeMicroBreaks = takeMicroBreaks;
        settings.playSchedule = playSchedule;
        settings.universalAntiban = universalAntiban;
        settings.microBreakDurationLow = microBreakDurationLow;
        settings.microBreakDurationHigh = microBreakDurationHigh;
        settings.actionCooldownChance = normalizeProbability(actionCooldownChance);
        settings.microBreakChance = normalizeProbability(microBreakChance);
        settings.moveMouseRandomlyChance = normalizeProbability(moveMouseRandomlyChance);
        settings.moveMouseOffScreenChance = normalizeProbability(moveMouseOffScreenChance);
        settings.mouseSpeed = mouseSpeed != null ? mouseSpeed.name() : MouseSpeed.DEFAULT.name();
        settings.mouseSmoothness = mouseSmoothness != null ? mouseSmoothness.name() : MouseSmoothness.DEFAULT.name();
        settings.mouseEngineMode = mouseEngineMode != null ? mouseEngineMode.name() : MouseEngineMode.DEFAULT.name();
        settings.mouseReactionDelayMs = MouseMovementTuning.clampTimingMs(mouseReactionDelayMs);
        settings.mouseReactionDelayRandom = mouseReactionDelayRandom;
        settings.mouseReactionDelayMinMs = MouseMovementTuning.normalizeTimingMinMs(
                mouseReactionDelayMinMs, mouseReactionDelayMaxMs);
        settings.mouseReactionDelayMaxMs = MouseMovementTuning.normalizeTimingMaxMs(
                mouseReactionDelayMinMs, mouseReactionDelayMaxMs);
        settings.mouseSettleDelayMs = MouseMovementTuning.clampTimingMs(mouseSettleDelayMs);
        settings.mouseSettleDelayRandom = mouseSettleDelayRandom;
        settings.mouseSettleDelayMinMs = MouseMovementTuning.normalizeTimingMinMs(
                mouseSettleDelayMinMs, mouseSettleDelayMaxMs);
        settings.mouseSettleDelayMaxMs = MouseMovementTuning.normalizeTimingMaxMs(
                mouseSettleDelayMinMs, mouseSettleDelayMaxMs);
        settings.mouseButtonHoldMs = MouseMovementTuning.clampTimingMs(mouseButtonHoldMs);
        settings.mouseButtonHoldRandom = mouseButtonHoldRandom;
        settings.mouseButtonHoldMinMs = MouseMovementTuning.normalizeTimingMinMs(
                mouseButtonHoldMinMs, mouseButtonHoldMaxMs);
        settings.mouseButtonHoldMaxMs = MouseMovementTuning.normalizeTimingMaxMs(
                mouseButtonHoldMinMs, mouseButtonHoldMaxMs);
        settings.mouseCurveScale = MouseMovementTuning.clampPercent(mouseCurveScale);
        settings.mousePathNoiseScale = MouseMovementTuning.clampPercent(mousePathNoiseScale);
        settings.mouseMicroJitterScale = MouseMovementTuning.clampPercent(mouseMicroJitterScale);
        settings.mouseOvershootScale = MouseMovementTuning.clampPercent(mouseOvershootScale);
        settings.mouseCorrectionScale = MouseMovementTuning.clampPercent(mouseCorrectionScale);
        return settings;
    }

    private static void apply(PersistentSettings settings) {
        if (settings.antibanEnabled != null) {
            antibanEnabled = settings.antibanEnabled;
        }
        if (settings.usePlayStyle != null) {
            usePlayStyle = settings.usePlayStyle;
        }
        if (settings.randomIntervals != null) {
            randomIntervals = settings.randomIntervals;
        }
        if (settings.simulateFatigue != null) {
            simulateFatigue = settings.simulateFatigue;
        }
        if (settings.simulateAttentionSpan != null) {
            simulateAttentionSpan = settings.simulateAttentionSpan;
        }
        if (settings.behavioralVariability != null) {
            behavioralVariability = settings.behavioralVariability;
        }
        if (settings.nonLinearIntervals != null) {
            nonLinearIntervals = settings.nonLinearIntervals;
        }
        if (settings.profileSwitching != null) {
            profileSwitching = settings.profileSwitching;
        }
        if (settings.timeOfDayAdjust != null) {
            timeOfDayAdjust = settings.timeOfDayAdjust;
        }
        if (settings.simulateMistakes != null) {
            simulateMistakes = settings.simulateMistakes;
        }
        if (settings.naturalMouse != null) {
            naturalMouse = settings.naturalMouse;
        }
        if (settings.moveMouseOffScreen != null) {
            moveMouseOffScreen = settings.moveMouseOffScreen;
        }
        if (settings.moveMouseRandomly != null) {
            moveMouseRandomly = settings.moveMouseRandomly;
        }
        if (settings.contextualVariability != null) {
            contextualVariability = settings.contextualVariability;
        }
        if (settings.dynamicIntensity != null) {
            dynamicIntensity = settings.dynamicIntensity;
        }
        if (settings.dynamicActivity != null) {
            dynamicActivity = settings.dynamicActivity;
        }
        if (settings.devDebug != null) {
            devDebug = settings.devDebug;
        }
        if (settings.overwriteScriptSettings != null) {
            overwriteScriptSettings = settings.overwriteScriptSettings;
        }
        if (settings.takeMicroBreaks != null) {
            takeMicroBreaks = settings.takeMicroBreaks;
        }
        if (settings.playSchedule != null) {
            playSchedule = settings.playSchedule;
        }
        if (settings.universalAntiban != null) {
            universalAntiban = settings.universalAntiban;
        }
        if (settings.microBreakDurationLow != null) {
            microBreakDurationLow = settings.microBreakDurationLow;
        }
        if (settings.microBreakDurationHigh != null) {
            microBreakDurationHigh = settings.microBreakDurationHigh;
        }
        if (settings.actionCooldownChance != null) {
            actionCooldownChance = normalizeProbability(settings.actionCooldownChance);
        }
        if (settings.microBreakChance != null) {
            microBreakChance = normalizeProbability(settings.microBreakChance);
        }
        if (settings.moveMouseRandomlyChance != null) {
            moveMouseRandomlyChance = normalizeProbability(settings.moveMouseRandomlyChance);
        }
        if (settings.moveMouseOffScreenChance != null) {
            moveMouseOffScreenChance = normalizeProbability(settings.moveMouseOffScreenChance);
        }
        applyMouseSettings(settings);
    }

    private static void applyMouseSettings(PersistentSettings settings) {
        if (settings.mouseSpeed != null) {
            mouseSpeed = MouseSpeed.fromConfigValue(settings.mouseSpeed);
        }
        if (settings.mouseSmoothness != null) {
            mouseSmoothness = MouseSmoothness.fromConfigValue(settings.mouseSmoothness);
        }
        if (settings.mouseEngineMode != null) {
            mouseEngineMode = MouseEngineMode.fromConfigValue(settings.mouseEngineMode);
        }
        if (settings.mouseReactionDelayMs != null) {
            mouseReactionDelayMs = MouseMovementTuning.clampTimingMs(settings.mouseReactionDelayMs);
        }
        if (settings.mouseReactionDelayRandom != null) {
            mouseReactionDelayRandom = settings.mouseReactionDelayRandom;
        }
        int reactionMin = settings.mouseReactionDelayMinMs != null
                ? settings.mouseReactionDelayMinMs
                : mouseReactionDelayMinMs;
        int reactionMax = settings.mouseReactionDelayMaxMs != null
                ? settings.mouseReactionDelayMaxMs
                : mouseReactionDelayMaxMs;
        mouseReactionDelayMinMs = MouseMovementTuning.normalizeTimingMinMs(reactionMin, reactionMax);
        mouseReactionDelayMaxMs = MouseMovementTuning.normalizeTimingMaxMs(reactionMin, reactionMax);
        if (settings.mouseSettleDelayMs != null) {
            mouseSettleDelayMs = MouseMovementTuning.clampTimingMs(settings.mouseSettleDelayMs);
        }
        if (settings.mouseSettleDelayRandom != null) {
            mouseSettleDelayRandom = settings.mouseSettleDelayRandom;
        }
        int settleMin = settings.mouseSettleDelayMinMs != null
                ? settings.mouseSettleDelayMinMs
                : mouseSettleDelayMinMs;
        int settleMax = settings.mouseSettleDelayMaxMs != null
                ? settings.mouseSettleDelayMaxMs
                : mouseSettleDelayMaxMs;
        mouseSettleDelayMinMs = MouseMovementTuning.normalizeTimingMinMs(settleMin, settleMax);
        mouseSettleDelayMaxMs = MouseMovementTuning.normalizeTimingMaxMs(settleMin, settleMax);
        if (settings.mouseButtonHoldMs != null) {
            mouseButtonHoldMs = MouseMovementTuning.clampTimingMs(settings.mouseButtonHoldMs);
        }
        if (settings.mouseButtonHoldRandom != null) {
            mouseButtonHoldRandom = settings.mouseButtonHoldRandom;
        }
        int buttonHoldMin = settings.mouseButtonHoldMinMs != null
                ? settings.mouseButtonHoldMinMs
                : mouseButtonHoldMinMs;
        int buttonHoldMax = settings.mouseButtonHoldMaxMs != null
                ? settings.mouseButtonHoldMaxMs
                : mouseButtonHoldMaxMs;
        mouseButtonHoldMinMs = MouseMovementTuning.normalizeTimingMinMs(buttonHoldMin, buttonHoldMax);
        mouseButtonHoldMaxMs = MouseMovementTuning.normalizeTimingMaxMs(buttonHoldMin, buttonHoldMax);
        if (settings.mouseCurveScale != null) {
            mouseCurveScale = MouseMovementTuning.clampPercent(settings.mouseCurveScale);
        }
        if (settings.mousePathNoiseScale != null) {
            mousePathNoiseScale = MouseMovementTuning.clampPercent(settings.mousePathNoiseScale);
        }
        if (settings.mouseMicroJitterScale != null) {
            mouseMicroJitterScale = MouseMovementTuning.clampPercent(settings.mouseMicroJitterScale);
        }
        if (settings.mouseOvershootScale != null) {
            mouseOvershootScale = MouseMovementTuning.clampPercent(settings.mouseOvershootScale);
        }
        if (settings.mouseCorrectionScale != null) {
            mouseCorrectionScale = MouseMovementTuning.clampPercent(settings.mouseCorrectionScale);
        }
    }

    public static double normalizeProbability(double value) {
        if (Double.isNaN(value)) {
            return 0.0;
        }
        if (value == Double.POSITIVE_INFINITY) {
            return 1.0;
        }
        if (value <= 0.0 || value == Double.NEGATIVE_INFINITY) {
            return 0.0;
        }
        if (value >= 1.0) {
            return 1.0;
        }
        return value;
    }

    public static boolean actionCooldownActive = false;
    public static boolean microBreakActive = false;
    public static boolean antibanEnabled = true;
    public static boolean usePlayStyle = false;
    public static boolean randomIntervals = false;
    public static boolean simulateFatigue = false;
    public static boolean simulateAttentionSpan = false;
    public static boolean behavioralVariability = false;
    public static boolean nonLinearIntervals = false;
    public static boolean profileSwitching = false;
    public static boolean timeOfDayAdjust = false; //TODO: Implement this
    public static boolean simulateMistakes = false; //Handled by the natural mouse
    public static boolean naturalMouse = true;
    public static boolean moveMouseOffScreen = false;
    public static boolean moveMouseRandomly = false;
    public static boolean contextualVariability = false;
    public static boolean dynamicIntensity = false;
    public static boolean dynamicActivity = false;
    public static boolean devDebug = false;
    public static boolean overwriteScriptSettings = false;

    public static boolean takeMicroBreaks = false; // will take micro breaks lasting 3-15 minutes at random intervals by default.
    public static boolean playSchedule = false; //TODO: Implement this
    public static boolean universalAntiban = false; // Will attempt to use the same antiban settings for all plugins that has not yet implemented their own antiban settings.
    public static int microBreakDurationLow = AntibanPlugin.MICRO_BREAK_DURATION_LOW_DEFAULT; // 3 minutes
    public static int microBreakDurationHigh = AntibanPlugin.MICRO_BREAK_DURATION_HIGH_DEFAULT; // 15 minutes
    public static double actionCooldownChance = 0.1; // 10% chance of activating the action cooldown by default
    public static double microBreakChance = 0.1; // 10% chance of taking a micro break by default
    public static double moveMouseRandomlyChance = 0.1; // 10% chance of moving the mouse randomly by default
    public static double moveMouseOffScreenChance = 0.1; // 10% chance of moving the mouse off screen by default
    public static MouseSpeed mouseSpeed = MouseSpeed.DEFAULT;
    public static MouseSmoothness mouseSmoothness = MouseSmoothness.DEFAULT;
    public static MouseEngineMode mouseEngineMode = MouseEngineMode.DEFAULT;
    public static int mouseReactionDelayMs = MouseMovementTuning.DEFAULT_REACTION_DELAY_MS;
    public static boolean mouseReactionDelayRandom = false;
    public static int mouseReactionDelayMinMs = MouseMovementTuning.DEFAULT_REACTION_DELAY_MIN_MS;
    public static int mouseReactionDelayMaxMs = MouseMovementTuning.DEFAULT_REACTION_DELAY_MAX_MS;
    public static int mouseSettleDelayMs = MouseMovementTuning.DEFAULT_SETTLE_DELAY_MS;
    public static boolean mouseSettleDelayRandom = false;
    public static int mouseSettleDelayMinMs = MouseMovementTuning.DEFAULT_SETTLE_DELAY_MIN_MS;
    public static int mouseSettleDelayMaxMs = MouseMovementTuning.DEFAULT_SETTLE_DELAY_MAX_MS;
    public static int mouseButtonHoldMs = MouseMovementTuning.DEFAULT_BUTTON_HOLD_MS;
    public static boolean mouseButtonHoldRandom = false;
    public static int mouseButtonHoldMinMs = MouseMovementTuning.DEFAULT_BUTTON_HOLD_MIN_MS;
    public static int mouseButtonHoldMaxMs = MouseMovementTuning.DEFAULT_BUTTON_HOLD_MAX_MS;
    public static int mouseCurveScale = MouseMovementTuning.DEFAULT_PERCENT;
    public static int mousePathNoiseScale = MouseMovementTuning.DEFAULT_PERCENT;
    public static int mouseMicroJitterScale = MouseMovementTuning.DEFAULT_PERCENT;
    public static int mouseOvershootScale = MouseMovementTuning.DEFAULT_PERCENT;
    public static int mouseCorrectionScale = MouseMovementTuning.DEFAULT_PERCENT;

    // reset method to reset all settings to default values
    public static void reset() {
        actionCooldownActive = false;
        microBreakActive = false;
        antibanEnabled = true;
        usePlayStyle = false;
        randomIntervals = false;
        simulateFatigue = false;
        simulateAttentionSpan = false;
        behavioralVariability = false;
        nonLinearIntervals = false;
        profileSwitching = false;
        timeOfDayAdjust = false;
        simulateMistakes = false;
        naturalMouse = true;
        moveMouseOffScreen = false;
        moveMouseRandomly = false;
        contextualVariability = false;
        dynamicIntensity = false;
        dynamicActivity = false;
        devDebug = false;
        overwriteScriptSettings = false;
        takeMicroBreaks = false;
        playSchedule = false;
        universalAntiban = false;
        microBreakDurationLow = AntibanPlugin.MICRO_BREAK_DURATION_LOW_DEFAULT;
        microBreakDurationHigh = AntibanPlugin.MICRO_BREAK_DURATION_HIGH_DEFAULT;
        actionCooldownChance = 0.1;
        microBreakChance = 0.1;
        moveMouseRandomlyChance = 0.1;
        moveMouseOffScreenChance = 0.1;
        mouseSpeed = MouseSpeed.DEFAULT;
        mouseSmoothness = MouseSmoothness.DEFAULT;
        mouseEngineMode = MouseEngineMode.DEFAULT;
        mouseReactionDelayMs = MouseMovementTuning.DEFAULT_REACTION_DELAY_MS;
        mouseReactionDelayRandom = false;
        mouseReactionDelayMinMs = MouseMovementTuning.DEFAULT_REACTION_DELAY_MIN_MS;
        mouseReactionDelayMaxMs = MouseMovementTuning.DEFAULT_REACTION_DELAY_MAX_MS;
        mouseSettleDelayMs = MouseMovementTuning.DEFAULT_SETTLE_DELAY_MS;
        mouseSettleDelayRandom = false;
        mouseSettleDelayMinMs = MouseMovementTuning.DEFAULT_SETTLE_DELAY_MIN_MS;
        mouseSettleDelayMaxMs = MouseMovementTuning.DEFAULT_SETTLE_DELAY_MAX_MS;
        mouseButtonHoldMs = MouseMovementTuning.DEFAULT_BUTTON_HOLD_MS;
        mouseButtonHoldRandom = false;
        mouseButtonHoldMinMs = MouseMovementTuning.DEFAULT_BUTTON_HOLD_MIN_MS;
        mouseButtonHoldMaxMs = MouseMovementTuning.DEFAULT_BUTTON_HOLD_MAX_MS;
        mouseCurveScale = MouseMovementTuning.DEFAULT_PERCENT;
        mousePathNoiseScale = MouseMovementTuning.DEFAULT_PERCENT;
        mouseMicroJitterScale = MouseMovementTuning.DEFAULT_PERCENT;
        mouseOvershootScale = MouseMovementTuning.DEFAULT_PERCENT;
        mouseCorrectionScale = MouseMovementTuning.DEFAULT_PERCENT;
    }
}
