package net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.plugins.cupidbot.CupidBot;
import net.runelite.client.plugins.cupidbot.util.Global;
import net.runelite.client.plugins.cupidbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.cupidbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.ActivityIntensity;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSmoothness;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSpeed;
import net.runelite.client.plugins.cupidbot.util.math.Rs2Random;
import net.runelite.client.plugins.cupidbot.util.mouse.engine.MouseActionContext;
import net.runelite.client.plugins.cupidbot.util.mouse.engine.MouseMovementPlan;
import net.runelite.client.plugins.cupidbot.util.mouse.engine.MouseMovementPlanner;
import net.runelite.client.plugins.cupidbot.util.mouse.engine.MouseMovementReport;
import net.runelite.client.plugins.cupidbot.util.mouse.engine.MouseTarget;
import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.api.MouseInfoAccessor;
import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.api.MouseMotionFactory;
import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.api.SystemCalls;
import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.support.DefaultMouseMotionNature;
import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.support.DefaultSpeedManager;
import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.support.Flow;
import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.support.MouseMotionNature;
import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.util.FactoryTemplates;
import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.util.FlowTemplates;
import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.util.Pair;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class NaturalMouse {
    public final MouseMotionNature nature;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MouseMovementPlanner planner = new MouseMovementPlanner();
    @Inject
    private Client client;
    @Getter
    @Setter
    private List<Flow> flows = List.of(
            new Flow(FlowTemplates.variatingFlow()),
            new Flow(FlowTemplates.slowStartupFlow()),
            new Flow(FlowTemplates.slowStartup2Flow()),
            new Flow(FlowTemplates.jaggedFlow()),
            new Flow(FlowTemplates.interruptedFlow()),
            new Flow(FlowTemplates.interruptedFlow2()),
            new Flow(FlowTemplates.stoppingFlow()),
            new Flow(FlowTemplates.adjustingFlow()),
            new Flow(FlowTemplates.random())
    );

    private volatile MouseMotionFactory cachedFactory;
    private volatile ActivityIntensity cachedIntensity;
    private volatile MouseSpeed cachedMouseSpeed;
    private volatile MouseSmoothness cachedMouseSmoothness;
    private volatile boolean cachedDynamicIntensity;
    private volatile boolean cachedSimulateFatigue;
    private volatile int cachedEffectiveBaseTimeMs;

    @Inject
    public NaturalMouse() {
        nature = new DefaultMouseMotionNature();
        nature.setSystemCalls(new SystemCallsImpl());
        nature.setMouseInfo(new MouseInfoImpl());
    }

    public synchronized void moveTo(int dx, int dy) {
//		if(Rs2UiHelper.isStretchedEnabled())
//		{
//			dx = Rs2UiHelper.stretchX(dx);
//			dy = Rs2UiHelper.stretchY(dy);
//		}
        Point mousePos = CupidBot.getMouse().getMousePosition();
        MouseMovementPlan plan = planner.plan(
                new net.runelite.api.Point(mousePos.x, mousePos.y),
                MouseTarget.point(new net.runelite.api.Point(dx, dy)),
                MouseActionContext.GENERAL,
                Rs2AntibanSettings.getConfiguredMouseEngineMode(),
                Rs2AntibanSettings.getEffectiveMouseSpeed(
                        Rs2Antiban.getActivityIntensity(), Rs2Antiban.getPlayStyle()),
                Rs2AntibanSettings.getConfiguredMouseSmoothness(),
                null);
        moveTo(plan);
    }

    public synchronized MouseMovementReport moveTo(MouseMovementPlan plan) {
        if (plan == null) {
            return MouseMovementReport.empty();
        }
        int finalDx = plan.getTargetPoint().getX();
        int finalDy = plan.getTargetPoint().getY();
        Point mousePos = CupidBot.getMouse().getMousePosition();
        // check if current mouse position is already at the destination
        if (mousePos.x == finalDx && mousePos.y == finalDy) {
            return MouseMovementReport.fromPath(plan, List.of(
                    new net.runelite.api.Point(mousePos.x, mousePos.y)));
        }

        if (!CupidBot.getClient().isClientThread()) {
            return move(plan);
        } else {

            executorService.submit(() -> move(plan));
            return MouseMovementReport.empty();
        }
    }

    private synchronized MouseMovementReport move(MouseMovementPlan plan) {
        if (plan.getReactionDelayMs() > 0) {
            Global.sleep(plan.getReactionDelayMs());
        }
        var motion = getFactory(plan).build(plan.getTargetPoint().getX(), plan.getTargetPoint().getY());
        List<net.runelite.api.Point> path = new ArrayList<>();
        path.add(plan.getStartPoint());
        try {
            motion.move((x, y) -> path.add(new net.runelite.api.Point(x, y)));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return MouseMovementReport.fromPath(plan, path);
    }

    public MouseMotionFactory getFactory() {
        ActivityIntensity intensity = Rs2Antiban.getActivityIntensity();
        boolean dynamicIntensity = Rs2AntibanSettings.dynamicIntensity;
        boolean simulateFatigue = Rs2AntibanSettings.simulateFatigue;
        MouseSpeed mouseSpeed = Rs2AntibanSettings.getEffectiveMouseSpeed(intensity, Rs2Antiban.getPlayStyle());
        MouseSmoothness mouseSmoothness = Rs2AntibanSettings.getConfiguredMouseSmoothness();
        int effectiveBaseTimeMs = simulateFatigue
                ? FactoryTemplates.effectiveMouseBaseTimeMs(mouseSpeed)
                : mouseSpeed.getBaseTimeMs();

        if (cachedFactory != null
                && intensity == cachedIntensity
                && mouseSpeed == cachedMouseSpeed
                && mouseSmoothness == cachedMouseSmoothness
                && dynamicIntensity == cachedDynamicIntensity
                && simulateFatigue == cachedSimulateFatigue
                && effectiveBaseTimeMs == cachedEffectiveBaseTimeMs) {
            return cachedFactory;
        }
        log.debug("Creating {} mouse motion factory", mouseSpeed.getName());
        MouseMotionFactory factory = FactoryTemplates.createMouseSpeedMotionFactory(
                nature,
                mouseSpeed,
                effectiveBaseTimeMs,
                mouseSmoothness);
        cachedIntensity = intensity;
        cachedMouseSpeed = mouseSpeed;
        cachedMouseSmoothness = mouseSmoothness;
        cachedDynamicIntensity = dynamicIntensity;
        cachedSimulateFatigue = simulateFatigue;
        cachedEffectiveBaseTimeMs = effectiveBaseTimeMs;
        cachedFactory = factory;
        return factory;

//		var factory = new MouseMotionFactory();
//		factory.setNature(nature);
//		factory.setRandom(random);
//
//		var manager = new SpeedManagerImpl(flows);
//		factory.setDeviationProvider(new SinusoidalDeviationProvider(15.0));
//		factory.setNoiseProvider(new DefaultNoiseProvider(2.0));
//		factory.getNature().setReactionTimeVariationMs(120);
//		manager.setMouseMovementBaseTimeMs(130);
//
//		var overshootManager = (DefaultOvershootManager) factory.getOvershootManager();
//		overshootManager.setOvershoots(2);
//		factory.setSpeedManager(manager);
//
//		return factory;
    }

    private MouseMotionFactory getFactory(MouseMovementPlan plan) {
        ActivityIntensity intensity = Rs2Antiban.getActivityIntensity();
        MouseSpeed mouseSpeed = Rs2AntibanSettings.getEffectiveMouseSpeed(intensity, Rs2Antiban.getPlayStyle());
        MouseSmoothness mouseSmoothness = Rs2AntibanSettings.getConfiguredMouseSmoothness();
        return FactoryTemplates.createMouseSpeedMotionFactory(
                nature,
                mouseSpeed,
                plan.getFactoryBaseTimeMs(),
                mouseSmoothness,
                new Random(plan.getSeed()),
                Math.max(plan.getOvershootCount(), plan.getCorrectionCount()),
                plan);
    }

    /**
     * Moves the mouse off screen with a default 100% chance.
     * This method will always move the mouse off screen when called.
     */
    public void moveOffScreen() {
        // Always move the mouse off screen (default behavior)
        moveOffScreen(100.0); // Calls the overloaded method with a 100% chance
    }

    /**
     * Moves the mouse off screen based on a given percentage chance.
     *
     * @param chancePercentage the chance (in percentage) to move the mouse off screen.
     *                         This value should be between 0.0 and 100.0 (inclusive).
     *                         Note: This parameter should not be a fractional value between
     *                         0.0 and 0.99; use values representing a whole percentage (e.g., 25.0, 50.0).
     */
    public void moveOffScreen(double chancePercentage) {
        if (chancePercentage >= 100 || Rs2Random.dicePercentage(chancePercentage)) {
            // Move off screen if the chance is met
            int horizontal = random.nextBoolean() ? -1 : client.getCanvasWidth() + 1;
            int vertical = random.nextBoolean() ? -1 : client.getCanvasHeight() + 1;

            boolean exitHorizontally = random.nextBoolean();
            if (exitHorizontally) {
                moveTo(horizontal, random.nextInt(0, client.getCanvasHeight() + 1));
            } else {
                moveTo(random.nextInt(0, client.getCanvasWidth() + 1), vertical);
            }
        }
    }

    // Move to a random point on the screen
    public void moveRandom() {
        moveTo(random.nextInt(0, client.getCanvasWidth() + 1), random.nextInt(0, client.getCanvasHeight() + 1));
    }

    private static class SpeedManagerImpl extends DefaultSpeedManager {
        private SpeedManagerImpl(Collection<Flow> flows) {
            super(flows);
        }

        @Override
        public Pair<Flow, Long> getFlowWithTime(double distance) {
            var pair = super.getFlowWithTime(distance);
            return new Pair<>(pair.x, pair.y);
        }
    }

    private static class MouseInfoImpl implements MouseInfoAccessor {
        @Override
        public Point getMousePosition() {
            return CupidBot.getMouse().getMousePosition();
        }
    }

    private class SystemCallsImpl implements SystemCalls {
        @Override
        public long currentTimeMillis() {
            return System.currentTimeMillis();
        }

        @Override
        public void sleep(long time) {
            Global.sleep((int) time);
        }

        @Override
        public Dimension getScreenSize() {
            return CupidBot.getClient().getCanvas().getSize();
        }

        @Override
        public void setMousePosition(int x, int y) {
            CupidBot.getMouse().moveInstant(x, y);

        }
    }
}
