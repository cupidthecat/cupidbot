package net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.util;


import net.runelite.client.plugins.cupidbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.cupidbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.cupidbot.util.antiban.SessionFatigue;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSmoothness;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSpeed;
import net.runelite.client.plugins.cupidbot.util.mouse.engine.MouseMovementTuning;
import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.api.MouseMotionFactory;
import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.api.SpeedManager;
import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.support.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class FactoryTemplates {
    /**
     * <h1>Stereotypical granny using a computer with non-optical mouse from the 90s.</h1>
     * Low speed, variating flow, lots of noise in movement.
     *
     * @return the factory
     */
    public static MouseMotionFactory createGrannyMotionFactory() {
        return createGrannyMotionFactory(new DefaultMouseMotionNature());
    }

    /**
     * <h1>Stereotypical granny using a computer with non-optical mouse from the 90s.</h1>
     * Low speed, variating flow, lots of noise in movement.
     *
     * @param nature the nature for the template to be configured on
     * @return the factory
     */
    public static MouseMotionFactory createGrannyMotionFactory(MouseMotionNature nature) {
        MouseMotionFactory factory = new MouseMotionFactory(nature);
        List<Flow> flows = new ArrayList<>(Arrays.asList(
                new Flow(FlowTemplates.jaggedFlow()),
                new Flow(FlowTemplates.random()),
                new Flow(FlowTemplates.interruptedFlow()),
                new Flow(FlowTemplates.interruptedFlow2()),
                new Flow(FlowTemplates.adjustingFlow()),
                new Flow(FlowTemplates.stoppingFlow())
        ));
        DefaultSpeedManager manager = new DefaultSpeedManager(flows);
        factory.setDeviationProvider(new SinusoidalDeviationProvider(9));
        factory.setNoiseProvider(new DefaultNoiseProvider(1.6));
        factory.getNature().setReactionTimeBaseMs(100);

        DefaultOvershootManager overshootManager = (DefaultOvershootManager) factory.getOvershootManager();
        if (Rs2AntibanSettings.simulateMistakes)
            overshootManager.setOvershoots(3);
        else
            overshootManager.setOvershoots(0);
        overshootManager.setMinDistanceForOvershoots(3);
        overshootManager.setMinOvershootMovementMs(400);
        overshootManager.setOvershootRandomModifierDivider(DefaultOvershootManager.OVERSHOOT_RANDOM_MODIFIER_DIVIDER / 2);
        overshootManager.setOvershootSpeedupDivider(DefaultOvershootManager.OVERSHOOT_SPEEDUP_DIVIDER * 2);

        factory.getNature().setTimeToStepsDivider(DefaultMouseMotionNature.TIME_TO_STEPS_DIVIDER - 2);
        manager.setMouseMovementBaseTimeMs(1000);
        factory.setSpeedManager(manager);
        return factory;
    }

    /**
     * <h1>Robotic fluent movement.</h1>
     * Custom speed, constant movement, no mistakes, no overshoots.
     *
     * @param motionTimeMsPer100Pixels approximate time a movement takes per 100 pixels of travelling
     * @return the factory
     */
    public static MouseMotionFactory createDemoRobotMotionFactory(long motionTimeMsPer100Pixels) {
        return createDemoRobotMotionFactory(new DefaultMouseMotionNature(), motionTimeMsPer100Pixels);
    }

    /**
     * <h1>Robotic fluent movement.</h1>
     * Custom speed, constant movement, no mistakes, no overshoots.
     *
     * @param nature                   the nature for the template to be configured on
     * @param motionTimeMsPer100Pixels approximate time a movement takes per 100 pixels of travelling
     * @return the factory
     */
    public static MouseMotionFactory createDemoRobotMotionFactory(
            MouseMotionNature nature, long motionTimeMsPer100Pixels
    ) {
        MouseMotionFactory factory = new MouseMotionFactory(nature);
        final Flow flow = new Flow(FlowTemplates.constantSpeed());
        double timePerPixel = motionTimeMsPer100Pixels / 100d;
        SpeedManager manager = distance -> new Pair<>(flow, (long) (timePerPixel * distance));
        factory.setDeviationProvider((totalDistanceInPixels, completionFraction) -> DoublePoint.ZERO);
        factory.setNoiseProvider(((random, xStepSize, yStepSize) -> DoublePoint.ZERO));

        DefaultOvershootManager overshootManager = (DefaultOvershootManager) factory.getOvershootManager();
        overshootManager.setOvershoots(0);

        factory.setSpeedManager(manager);
        return factory;
    }

    public static MouseMotionFactory createMouseSpeedMotionFactory(MouseMotionNature nature, MouseSpeed mouseSpeed) {
        MouseSpeed speed = mouseSpeed != null ? mouseSpeed : MouseSpeed.DEFAULT;
        int currentBaseTime = Rs2AntibanSettings.simulateFatigue
                ? effectiveMouseBaseTimeMs(speed)
                : speed.getBaseTimeMs();

        return createMouseSpeedMotionFactory(nature, speed, currentBaseTime);
    }

    public static MouseMotionFactory createMouseSpeedMotionFactory(MouseMotionNature nature, MouseSpeed mouseSpeed, int currentBaseTime) {
        return createMouseSpeedMotionFactory(
                nature,
                mouseSpeed,
                currentBaseTime,
                Rs2AntibanSettings.getConfiguredMouseSmoothness());
    }

    public static MouseMotionFactory createMouseSpeedMotionFactory(
            MouseMotionNature nature,
            MouseSpeed mouseSpeed,
            int currentBaseTime,
            MouseSmoothness mouseSmoothness) {
        return createMouseSpeedMotionFactory(nature, mouseSpeed, currentBaseTime, mouseSmoothness, null, null);
    }

    public static MouseMotionFactory createMouseSpeedMotionFactory(
            MouseMotionNature nature,
            MouseSpeed mouseSpeed,
            int currentBaseTime,
            MouseSmoothness mouseSmoothness,
            Random random,
            Integer overshootsOverride) {
        return createMouseSpeedMotionFactory(
                nature,
                mouseSpeed,
                currentBaseTime,
                mouseSmoothness,
                random,
                overshootsOverride,
                MouseMovementTuning.defaults());
    }

    public static MouseMotionFactory createMouseSpeedMotionFactory(
            MouseMotionNature nature,
            MouseSpeed mouseSpeed,
            int currentBaseTime,
            MouseSmoothness mouseSmoothness,
            Random random,
            Integer overshootsOverride,
            MouseMovementTuning tuning) {
        MouseSpeed speed = mouseSpeed != null ? mouseSpeed : MouseSpeed.DEFAULT;
        MouseSmoothness smoothness = mouseSmoothness != null ? mouseSmoothness : MouseSmoothness.DEFAULT;
        MouseMovementTuning safeTuning = tuning == null ? MouseMovementTuning.defaults() : tuning;
        Random rng = random == null ? new Random() : random;

        MouseMotionFactory factory = new MouseMotionFactory(nature);
        factory.setRandom(rng);
        DefaultSpeedManager manager = new DefaultSpeedManager(createHumanMouseFlows(speed), rng);
        factory.setDeviationProvider(new SinusoidalDeviationProvider(
                scaledDivider(smoothness.getDeviationSlopeDivider(), safeTuning.getCurveMultiplier())));
        factory.setNoiseProvider(new DefaultNoiseProvider(
                smoothness.getNoiseDivider(),
                safeTuning.getPathNoiseMultiplier(),
                safeTuning.getMicroJitterMultiplier()));
        factory.getNature().setReactionTimeVariationMs(speed.getReactionTimeVariationMs());
        factory.getNature().setTimeToStepsDivider(smoothness.getTimeToStepsDivider());
        factory.getNature().setMinSteps(smoothness.getMinSteps());
        factory.getNature().setEffectFadeSteps(smoothness.getEffectFadeSteps());
        manager.setMouseMovementBaseTimeMs(currentBaseTime);

        DefaultOvershootManager overshootManager = new DefaultOvershootManager(rng);
        factory.setOvershootManager(overshootManager);
        int overshoots = overshootsOverride == null ? speed.getOvershoots() : overshootsOverride;
        overshootManager.setOvershoots(Rs2AntibanSettings.simulateMistakes ? overshoots : 0);
        overshootManager.setMinDistanceForOvershoots(3);
        overshootManager.setMinOvershootMovementMs(speed.getMinOvershootMovementMs());
        overshootManager.setOvershootRandomModifierDivider(scaledDivider(
                DefaultOvershootManager.OVERSHOOT_RANDOM_MODIFIER_DIVIDER,
                safeTuning.getOvershootMultiplier()));

        factory.setSpeedManager(manager);
        return factory;
    }

    private static double scaledDivider(double baseDivider, double multiplier) {
        if (multiplier <= 0.0) {
            return Double.MAX_VALUE;
        }
        return baseDivider / multiplier;
    }

    public static int effectiveMouseBaseTimeMs(MouseSpeed mouseSpeed) {
        return effectiveMouseBaseTimeMs(mouseSpeed, SessionFatigue.multiplier());
    }

    static int effectiveMouseBaseTimeMs(MouseSpeed mouseSpeed, double fatigueMultiplier) {
        MouseSpeed speed = mouseSpeed != null ? mouseSpeed : MouseSpeed.DEFAULT;
        if (Double.isNaN(fatigueMultiplier) || fatigueMultiplier <= 0.0) {
            fatigueMultiplier = 1.0;
        }
        if (fatigueMultiplier == Double.POSITIVE_INFINITY) {
            return speed.getFatigueMaxBaseTimeMs();
        }
        int fatiguedBaseTime = (int) Math.round(speed.getBaseTimeMs() * fatigueMultiplier);
        return Math.min(fatiguedBaseTime, speed.getFatigueMaxBaseTimeMs());
    }

    private static List<Flow> createHumanMouseFlows(MouseSpeed mouseSpeed) {
        List<Flow> flows = new ArrayList<>(Arrays.asList(
                new Flow(FlowTemplates.variatingFlow()),
                new Flow(FlowTemplates.slowStartupFlow()),
                new Flow(FlowTemplates.slowStartup2Flow()),
                new Flow(FlowTemplates.adjustingFlow()),
                new Flow(FlowTemplates.jaggedFlow())
        ));
        if (mouseSpeed.getSliderIndex() <= MouseSpeed.RELAXED.getSliderIndex()) {
            flows.add(new Flow(FlowTemplates.interruptedFlow()));
            flows.add(new Flow(FlowTemplates.interruptedFlow2()));
            flows.add(new Flow(FlowTemplates.stoppingFlow()));
        }
        return flows;
    }

    /**
     * <h1>Gamer with fast reflexes and quick mouse movements.</h1>
     * Quick movement, low noise, some deviation, lots of overshoots.
     *
     * @return the factory
     */
    public static MouseMotionFactory createNormalGamerMotionFactory() {
        return createNormalGamerMotionFactory(new DefaultMouseMotionNature());
    }

    /**
     * <h1>Gamer with fast reflexes and quick mouse movements.</h1>
     * Quick movement, low noise, some deviation, lots of overshoots.
     *
     * @param nature the nature for the template to be configured on
     * @return the factory
     */
    public static MouseMotionFactory createNormalGamerMotionFactory(MouseMotionNature nature) {
        int initialBaseTime = 150;
        int maxBaseTime = 200;
        int currentBaseTime = initialBaseTime;
        if (Rs2AntibanSettings.simulateFatigue)
            currentBaseTime = (Rs2Antiban.mouseFatigue.calculateBaseTimeWithNoise(currentBaseTime, maxBaseTime));
        MouseMotionFactory factory = new MouseMotionFactory(nature);
        List<Flow> flows = new ArrayList<>(Arrays.asList(
                new Flow(FlowTemplates.variatingFlow()),
                new Flow(FlowTemplates.slowStartupFlow()),
                new Flow(FlowTemplates.slowStartup2Flow()),
                new Flow(FlowTemplates.adjustingFlow()),
                new Flow(FlowTemplates.jaggedFlow())
        ));
        DefaultSpeedManager manager = new DefaultSpeedManager(flows);
        factory.setDeviationProvider(new SinusoidalDeviationProvider(SinusoidalDeviationProvider.DEFAULT_SLOPE_DIVIDER));
        factory.setNoiseProvider(new DefaultNoiseProvider(DefaultNoiseProvider.DEFAULT_NOISINESS_DIVIDER));
        factory.getNature().setReactionTimeVariationMs(100);
        manager.setMouseMovementBaseTimeMs(currentBaseTime);

        DefaultOvershootManager overshootManager = (DefaultOvershootManager) factory.getOvershootManager();
        if (Rs2AntibanSettings.simulateMistakes)
            overshootManager.setOvershoots(4);
        else
            overshootManager.setOvershoots(0);
        overshootManager.setMinDistanceForOvershoots(3);
        overshootManager.setMinOvershootMovementMs(250);

        factory.setSpeedManager(manager);
        return factory;
    }

    /**
     * <h1>Gamer with fast reflexes and quick mouse movements.</h1>
     * Quick movement, low noise, some deviation, lots of overshoots.
     *
     * @return the factory
     */
    public static MouseMotionFactory createFastGamerMotionFactory() {
        return createFastGamerMotionFactory(new DefaultMouseMotionNature());
    }

    /**
     * <h1>Gamer with fast reflexes and quick mouse movements.</h1>
     * Quick movement, low noise, some deviation, lots of overshoots.
     *
     * @param nature the nature for the template to be configured on
     * @return the factory
     */
    public static MouseMotionFactory createFastGamerMotionFactory(MouseMotionNature nature) {
        int initialBaseTime = 120;
        int maxBaseTime = 170;
        int currentBaseTime = initialBaseTime;
        if (Rs2AntibanSettings.simulateFatigue)
            currentBaseTime = (Rs2Antiban.mouseFatigue.calculateBaseTimeWithNoise(currentBaseTime, maxBaseTime));
        MouseMotionFactory factory = new MouseMotionFactory(nature);
        List<Flow> flows = new ArrayList<>(Arrays.asList(
                new Flow(FlowTemplates.variatingFlow()),
                new Flow(FlowTemplates.slowStartupFlow()),
                new Flow(FlowTemplates.slowStartup2Flow()),
                new Flow(FlowTemplates.adjustingFlow()),
                new Flow(FlowTemplates.jaggedFlow())
        ));
        DefaultSpeedManager manager = new DefaultSpeedManager(flows);
        factory.setDeviationProvider(new SinusoidalDeviationProvider(SinusoidalDeviationProvider.DEFAULT_SLOPE_DIVIDER));
        factory.setNoiseProvider(new DefaultNoiseProvider(DefaultNoiseProvider.DEFAULT_NOISINESS_DIVIDER));
        factory.getNature().setReactionTimeVariationMs(100);
        manager.setMouseMovementBaseTimeMs(currentBaseTime);

        DefaultOvershootManager overshootManager = (DefaultOvershootManager) factory.getOvershootManager();
        if (Rs2AntibanSettings.simulateMistakes)
            overshootManager.setOvershoots(3);
        else
            overshootManager.setOvershoots(0);
        overshootManager.setMinDistanceForOvershoots(3);
        overshootManager.setMinOvershootMovementMs(130);

        factory.setSpeedManager(manager);
        return factory;
    }

    // Super fast gamer
    public static MouseMotionFactory createSuperFastGamerMotionFactory() {
        return createSuperFastGamerMotionFactory(new DefaultMouseMotionNature());
    }

    public static MouseMotionFactory createSuperFastGamerMotionFactory(MouseMotionNature nature) {
        int initialBaseTime = 90;
        int maxBaseTime = 120;
        int currentBaseTime = initialBaseTime;
        if (Rs2AntibanSettings.simulateFatigue)
            currentBaseTime = (Rs2Antiban.mouseFatigue.calculateBaseTimeWithNoise(currentBaseTime, maxBaseTime));

        MouseMotionFactory factory = new MouseMotionFactory(nature);
        List<Flow> flows = new ArrayList<>(Arrays.asList(
                new Flow(FlowTemplates.variatingFlow()),
                new Flow(FlowTemplates.slowStartupFlow()),
                new Flow(FlowTemplates.slowStartup2Flow()),
                new Flow(FlowTemplates.adjustingFlow()),
                new Flow(FlowTemplates.jaggedFlow())
        ));
        DefaultSpeedManager manager = new DefaultSpeedManager(flows);
        factory.setDeviationProvider(new SinusoidalDeviationProvider(SinusoidalDeviationProvider.DEFAULT_SLOPE_DIVIDER));
        factory.setNoiseProvider(new DefaultNoiseProvider(DefaultNoiseProvider.DEFAULT_NOISINESS_DIVIDER));
        factory.getNature().setReactionTimeVariationMs(90);
        manager.setMouseMovementBaseTimeMs(currentBaseTime);

        DefaultOvershootManager overshootManager = (DefaultOvershootManager) factory.getOvershootManager();
        if (Rs2AntibanSettings.simulateMistakes)
            overshootManager.setOvershoots(2);
        else
            overshootManager.setOvershoots(0);
        overshootManager.setMinDistanceForOvershoots(3);
        overshootManager.setMinOvershootMovementMs(100);

        factory.setSpeedManager(manager);
        return factory;
    }

    /**
     * <h1>Standard computer user with average speed and movement mistakes</h1>
     * medium noise, medium speed, medium noise and deviation.
     *
     * @return the factory
     */
    public static MouseMotionFactory createAverageComputerUserMotionFactory() {
        return createAverageComputerUserMotionFactory(new DefaultMouseMotionNature());
    }

    /**
     * <h1>Standard computer user with average speed and movement mistakes</h1>
     * medium noise, medium speed, medium noise and deviation.
     *
     * @param nature the nature for the template to be configured on
     * @return the factory
     */
    public static MouseMotionFactory createAverageComputerUserMotionFactory(MouseMotionNature nature) {
        MouseMotionFactory factory = new MouseMotionFactory(nature);
        List<Flow> flows = new ArrayList<>(Arrays.asList(
                new Flow(FlowTemplates.variatingFlow()),
                new Flow(FlowTemplates.interruptedFlow()),
                new Flow(FlowTemplates.interruptedFlow2()),
                new Flow(FlowTemplates.slowStartupFlow()),
                new Flow(FlowTemplates.slowStartup2Flow()),
                new Flow(FlowTemplates.adjustingFlow()),
                new Flow(FlowTemplates.jaggedFlow()),
                new Flow(FlowTemplates.stoppingFlow())
        ));
        DefaultSpeedManager manager = new DefaultSpeedManager(flows);
        factory.setDeviationProvider(new SinusoidalDeviationProvider(SinusoidalDeviationProvider.DEFAULT_SLOPE_DIVIDER));
        factory.setNoiseProvider(new DefaultNoiseProvider(DefaultNoiseProvider.DEFAULT_NOISINESS_DIVIDER));
        factory.getNature().setReactionTimeVariationMs(110);
        manager.setMouseMovementBaseTimeMs(400);

        DefaultOvershootManager overshootManager = (DefaultOvershootManager) factory.getOvershootManager();
        if (Rs2AntibanSettings.simulateMistakes)
            overshootManager.setOvershoots(4);
        else
            overshootManager.setOvershoots(0);

        factory.setSpeedManager(manager);
        return factory;
    }
}
