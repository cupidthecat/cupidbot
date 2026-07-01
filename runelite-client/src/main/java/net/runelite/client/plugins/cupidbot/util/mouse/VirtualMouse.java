package net.runelite.client.plugins.cupidbot.util.mouse;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.plugins.cupidbot.CupidBot;
import net.runelite.client.plugins.cupidbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.cupidbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseEngineMode;
import net.runelite.client.plugins.cupidbot.util.math.Rs2Random;
import net.runelite.client.plugins.cupidbot.util.menu.NewMenuEntry;
import net.runelite.client.plugins.cupidbot.util.misc.Rs2UiHelper;
import net.runelite.client.plugins.cupidbot.util.mouse.engine.MouseActionContext;
import net.runelite.client.plugins.cupidbot.util.mouse.engine.MouseMovementPlan;
import net.runelite.client.plugins.cupidbot.util.mouse.engine.MouseMovementPlanner;
import net.runelite.client.plugins.cupidbot.util.mouse.engine.MouseMovementReport;
import net.runelite.client.plugins.cupidbot.util.mouse.engine.MouseTarget;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.cupidbot.util.Global.sleep;

@Slf4j
public class VirtualMouse extends Mouse {

    private final ScheduledExecutorService scheduledExecutorService;
    private final MouseMovementPlanner movementPlanner = new MouseMovementPlanner();

    @Inject
    public VirtualMouse() {
        super();
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void setLastClick(Point point) {
        lastClick2 = lastClick;
        lastClick = point;
    }

	public void setLastMove(Point point) {
		lastMove = point;
		points.add(point);
		if (points.size() > MAX_POINTS) {
			points.pollFirst();
		}
	}

    private int[] scaleForDispatch(int x, int y) {
        Client c;
        try {
            c = CupidBot.getClient();
        } catch (Exception ex) {
            return new int[]{x, y};
        }
        if (c == null || !c.isStretchedEnabled()) {
            return new int[]{x, y};
        }
        Dimension stretched = c.getStretchedDimensions();
        Dimension real = c.getRealDimensions();
        if (stretched == null || real == null || real.width == 0 || real.height == 0) {
            return new int[]{x, y};
        }
        return new int[]{
                (int) ((long) x * stretched.width / real.width),
                (int) ((long) y * stretched.height / real.height)
        };
    }

    private void dispatchMouse(int id, Point point, int button, int clickCount) {
        int[] s = scaleForDispatch(point.getX(), point.getY());
        Canvas canvas = getCanvas();
        MouseEvent event = new MouseEvent(canvas, id, System.currentTimeMillis(), 0,
                s[0], s[1], clickCount, false, button);
        dispatchWithoutFocusGrab(canvas, event);
    }

    private void dispatchMouseMove(int id, Point point) {
        int[] s = scaleForDispatch(point.getX(), point.getY());
        Canvas canvas = getCanvas();
        MouseEvent event = new MouseEvent(canvas, id, System.currentTimeMillis(), 0,
                s[0], s[1], 0, false);
        dispatchWithoutFocusGrab(canvas, event);
    }

    private void dispatchWheel(Point point, int wheelRotation, int unitsToScroll) {
        int[] s = scaleForDispatch(point.getX(), point.getY());
        Canvas canvas = getCanvas();
        MouseWheelEvent event = new MouseWheelEvent(canvas, MouseEvent.MOUSE_WHEEL,
                System.currentTimeMillis(), 0, s[0], s[1], 0, false, 0, unitsToScroll, wheelRotation);
        dispatchWithoutFocusGrab(canvas, event);
    }

    // Jagex's MOUSE_PRESSED listener calls canvas.requestFocus() when the event source is the
    // Canvas, which yanks OS keyboard focus away from whatever app the user is typing in. Flip
    // focusable off for the duration of the synthetic dispatch so requestFocus is a no-op; mouse
    // delivery itself is unaffected by focusable state.
    //
    // IMPORTANT: only do this when the canvas is NOT currently the focus owner. If the user is
    // actively typing in the in-game chat (which lives inside the canvas), the canvas IS the focus
    // owner, and setFocusable(false) immediately yanks focus away to the parent container — exactly
    // the opposite of what this method is trying to prevent. Detect that case and skip the toggle.
    private void dispatchWithoutFocusGrab(Canvas canvas, AWTEvent event) {
        boolean canvasIsFocused = canvas.isFocusOwner();
        boolean wasFocusable = canvas.isFocusable();
        boolean shouldGuard = wasFocusable && !canvasIsFocused;
        if (shouldGuard) canvas.setFocusable(false);
        BotEventGuard.begin();
        try {
            canvas.dispatchEvent(event);
        } finally {
            BotEventGuard.end();
            if (shouldGuard) canvas.setFocusable(true);
        }
    }

    private void handleClick(Point point, boolean rightClick, MouseMovementPlan plan) {
        entered(point);
        exited(point);
        moved(point);
        sleep(nextClickSettleDelayMs(plan));
        pressed(point, rightClick ? MouseEvent.BUTTON3 : MouseEvent.BUTTON1);
        sleep(nextClickHoldDelayMs(plan));
        released(point, rightClick ? MouseEvent.BUTTON3 : MouseEvent.BUTTON1);
        sleep(nextClickReleaseDelayMs(plan));
        clicked(point, rightClick ? MouseEvent.BUTTON3 : MouseEvent.BUTTON1);
        setLastClick(point);
    }

    static int nextClickStageDelayMs() {
        return Rs2Random.logNormalBounded(25, 90);
    }

    static int nextClickSettleDelayMs(MouseMovementPlan plan) {
        return plan == null ? nextClickStageDelayMs() : plan.getSettleDelayMs();
    }

    static int nextClickHoldDelayMs(MouseMovementPlan plan) {
        return plan == null ? nextClickStageDelayMs() : plan.getButtonDownTimeMs();
    }

    static int nextClickReleaseDelayMs(MouseMovementPlan plan) {
        return plan == null ? nextClickStageDelayMs() : 0;
    }

    static int nextDragPressDelayMs(MouseMovementPlan plan) {
        return plan == null ? Rs2Random.logNormalBounded(35, 140) : plan.getButtonDownTimeMs();
    }

    static int nextDragReleaseDelayMs(MouseMovementPlan plan) {
        return plan == null ? Rs2Random.logNormalBounded(40, 140) : plan.getSettleDelayMs();
    }

    static int scrollBurstTicks(int wheelRotation, MouseMovementPlan plan) {
        if (wheelRotation == 0) {
            return 1;
        }
        int baseTicks = Math.max(1, Math.abs(wheelRotation));
        double multiplier = plan == null ? 1.0 : plan.getTuning().getScrollBurstMultiplier();
        return clamp((int) Math.ceil(baseTicks * multiplier), 1, 6);
    }

    private static int nextScrollSettleDelayMs(MouseMovementPlan plan) {
        return plan == null ? Rs2Random.logNormalBounded(40, 100) : plan.getSettleDelayMs();
    }

    private static int nextScrollBurstDelayMs() {
        return Rs2Random.logNormalBounded(18, 55);
    }

    private boolean shouldMoveNaturally(Point point) {
        return point.getX() > 1
                && point.getY() > 1
                && CupidBot.naturalMouse != null;
    }

    private MouseMovementPlan planMovement(MouseTarget target, MouseActionContext context) {
        java.awt.Point mousePosition = getMousePosition();
        MouseEngineMode mode = Rs2AntibanSettings.getConfiguredMouseEngineMode();
        return movementPlanner.plan(
                new Point(mousePosition.x, mousePosition.y),
                target,
                context,
                mode,
                Rs2AntibanSettings.getEffectiveMouseSpeed(
                        Rs2Antiban.getActivityIntensity(), Rs2Antiban.getPlayStyle()),
                Rs2AntibanSettings.getConfiguredMouseSmoothness(),
                seedForMode(mode, target, context));
    }

    private Long seedForMode(MouseEngineMode mode, MouseTarget target, MouseActionContext context) {
        if (mode != MouseEngineMode.QA_REPLAY) {
            return null;
        }

        Rectangle bounds = target.getBounds();
        long seed = 17L;
        seed = seed * 31L + bounds.x;
        seed = seed * 31L + bounds.y;
        seed = seed * 31L + bounds.width;
        seed = seed * 31L + bounds.height;
        seed = seed * 31L + context.ordinal();
        return seed;
    }

    private MouseMovementReport moveNaturallyOrInstant(MouseMovementPlan plan) {
        Point targetPoint = plan.getTargetPoint();
        MouseMovementReport report;
        if (shouldMoveNaturally(targetPoint)) {
            report = CupidBot.naturalMouse.moveTo(plan);
        } else {
            moveInstant(targetPoint);
            report = MouseMovementReport.fromPath(plan, List.of(plan.getStartPoint(), targetPoint));
        }
        setLastMovementReport(report);
        return report;
    }

    private MouseMovementReport moveNaturallyOrInstant(Point point) {
        return moveNaturallyOrInstant(planMovement(MouseTarget.point(point), MouseActionContext.GENERAL));
    }

    private MouseTarget targetForEntry(Point fallback, NewMenuEntry entry) {
        if (entry != null && Rs2UiHelper.hasActor(entry)) {
            return MouseTarget.rectangle(Rs2UiHelper.getActorClickbox(entry.getActor()));
        }
        if (entry != null && Rs2UiHelper.isGameObject(entry)) {
            return MouseTarget.rectangle(Rs2UiHelper.getObjectClickbox(entry.getGameObject()));
        }
        return MouseTarget.point(fallback);
    }

    private MouseActionContext contextForEntry(NewMenuEntry entry, MouseActionContext fallback) {
        if (entry != null && Rs2UiHelper.hasActor(entry)) {
            return MouseActionContext.ACTOR;
        }
        if (entry != null && Rs2UiHelper.isGameObject(entry)) {
            return MouseActionContext.WORLD_OBJECT;
        }
        return fallback;
    }

    public Mouse click(Point point, boolean rightClick) {
        if (point == null) return this;

        Runnable clickAction = () -> clickTarget(MouseTarget.point(point), MouseActionContext.GENERAL, rightClick, null);

        if (CupidBot.getClient().isClientThread()) {
            scheduledExecutorService.schedule(clickAction, 0, TimeUnit.MILLISECONDS);
        } else {
            clickAction.run();
        }

        return this;
    }


    public Mouse click(Point point, boolean rightClick, NewMenuEntry entry) {
        if (point == null) return this;

        Runnable clickAction = () -> clickTarget(
                targetForEntry(point, entry),
                contextForEntry(entry, MouseActionContext.MENU),
                rightClick,
                entry);

        if (CupidBot.getClient().isClientThread()) {
            scheduledExecutorService.schedule(clickAction, 0, TimeUnit.MILLISECONDS);
        } else {
            clickAction.run();
        }

        return this;
    }


    public Mouse click(int x, int y) {
        return click(new Point(x, y), false);
    }

    public Mouse click(double x, double y) {
        return click(new Point((int) x, (int) y), false);
    }

    public Mouse click(Rectangle rectangle) {
        return click(MouseTarget.rectangle(rectangle), MouseActionContext.MENU);
    }

    @Override
    public Mouse click(int x, int y, boolean rightClick) {
        return click(new Point(x, y), rightClick);
    }

    @Override
    public Mouse click(Point point) {
        return click(point, false);
    }

    @Override
    public Mouse click(Point point, NewMenuEntry entry) {
        return click(point, false, entry);
    }

    @Override
    public Mouse click() {
        return click(CupidBot.getClient().getMouseCanvasPosition());
    }

    @Override
    public Mouse click(MouseTarget target, MouseActionContext context) {
        if (target == null) return this;
        Runnable clickAction = () -> clickTarget(
                target,
                context == null ? MouseActionContext.GENERAL : context,
                false,
                null);
        return scheduleClick(clickAction);
    }

    @Override
    public Mouse click(MouseTarget target, MouseActionContext context, NewMenuEntry entry) {
        if (target == null) return this;
        Runnable clickAction = () -> clickTarget(
                target,
                contextForEntry(entry, context == null ? MouseActionContext.MENU : context),
                false,
                entry);
        return scheduleClick(clickAction);
    }

    private Mouse scheduleClick(Runnable clickAction) {
        if (CupidBot.getClient().isClientThread()) {
            scheduledExecutorService.schedule(clickAction, 0, TimeUnit.MILLISECONDS);
        } else {
            clickAction.run();
        }
        return this;
    }

    private Mouse clickTarget(MouseTarget target, MouseActionContext context, boolean rightClick, NewMenuEntry entry) {
        MouseMovementPlan plan = planMovement(target, context);
        moveNaturallyOrInstant(plan);
        if (entry != null) {
            CupidBot.targetMenu = entry;
        }
        handleClick(plan.getTargetPoint(), rightClick, plan);
        return this;
    }

    public Mouse move(Point point) {
        if (point == null) return this;
        moveNaturallyOrInstant(planMovement(MouseTarget.point(point), MouseActionContext.GENERAL));
        return this;
    }

    public Mouse moveInstant(Point point) {
        if (point == null) return this;
        setLastMove(point);
        dispatchMouseMove(MouseEvent.MOUSE_MOVED, point);
        return this;
    }

    public Mouse moveInstant(int x, int y) {
        return moveInstant(new Point(x, y));
    }

    public Mouse move(Rectangle rect) {
        if (rect == null) return this;
        return move(MouseTarget.rectangle(rect), MouseActionContext.GENERAL);
    }

    public Mouse move(Polygon polygon) {
        if (polygon == null) return this;
        return move(MouseTarget.polygon(polygon), MouseActionContext.GENERAL);
    }

    @Override
    public Mouse move(MouseTarget target, MouseActionContext context) {
        if (target == null) return this;
        moveNaturallyOrInstant(planMovement(target, context == null ? MouseActionContext.GENERAL : context));
        return this;
    }

    private Mouse scroll(Point point, int wheelRotation, int unitsToScroll) {
        if (point == null) return this;

        Runnable scrollAction = () -> {
            MouseMovementPlan plan = planMovement(MouseTarget.point(point), MouseActionContext.SCROLL);
            moveNaturallyOrInstant(plan);
            sleep(nextScrollSettleDelayMs(plan));
            int direction = wheelRotation < 0 ? -1 : 1;
            int ticks = scrollBurstTicks(wheelRotation, plan);
            int unitsPerTick = Math.max(1, Math.abs(unitsToScroll) / ticks) * direction;
            for (int i = 0; i < ticks; i++) {
                dispatchWheel(point, direction, unitsPerTick);
                if (i + 1 < ticks) {
                    sleep(nextScrollBurstDelayMs());
                }
            }
        };

        if (CupidBot.getClient().isClientThread()) {
            scheduledExecutorService.schedule(scrollAction, 0, TimeUnit.MILLISECONDS);
        } else {
            scrollAction.run();
        }

        return this;
    }

    public Mouse scrollDown(Point point) {
        return scroll(point, 2, 10);
    }

    public Mouse scrollUp(Point point) {
        return scroll(point, -2, -10);
    }

    @Override
    public java.awt.Point getMousePosition() {
        Point point = lastMove;
        return new java.awt.Point(point.getX(), point.getY());
    }

    @Override
    public Mouse move(int x, int y) {
        return move(new Point(x, y));
    }

    @Override
    public Mouse move(double x, double y) {
        return move(new Point((int) x, (int) y));
    }

    private synchronized void pressed(Point point, int button) {
        dispatchMouse(MouseEvent.MOUSE_PRESSED, point, button, 1);
    }

    private synchronized void released(Point point, int button) {
        dispatchMouse(MouseEvent.MOUSE_RELEASED, point, button, 1);
    }

    private synchronized void clicked(Point point, int button) {
        dispatchMouse(MouseEvent.MOUSE_CLICKED, point, button, 1);
    }

    private synchronized void exited(Point point) {
        dispatchMouseMove(MouseEvent.MOUSE_EXITED, point);
    }

    private synchronized void entered(Point point) {
        dispatchMouseMove(MouseEvent.MOUSE_ENTERED, point);
    }

    private synchronized void moved(Point point) {
        dispatchMouseMove(MouseEvent.MOUSE_MOVED, point);
    }

    public void shutdown() {
        scheduledExecutorService.shutdownNow();
    }

    public Mouse drag(Point startPoint, Point endPoint) {
        if (startPoint == null || endPoint == null) return this;

        MouseMovementPlan startPlan = planMovement(MouseTarget.point(startPoint), MouseActionContext.DRAG);
        MouseMovementPlan endPlan = planMovement(MouseTarget.point(endPoint), MouseActionContext.DRAG);
        return drag(startPlan, endPlan);
    }

    private Mouse drag(MouseMovementPlan startPlan, MouseMovementPlan endPlan) {
        if (startPlan == null || endPlan == null) return this;

        Point startPoint = startPlan.getTargetPoint();
        Point endPoint = endPlan.getTargetPoint();
        moveNaturallyOrInstant(startPlan);
        sleep(nextDragReleaseDelayMs(startPlan));
        pressed(startPoint, MouseEvent.BUTTON1);
        sleep(nextDragPressDelayMs(startPlan));
        moveNaturallyOrInstant(endPlan);
        sleep(nextDragReleaseDelayMs(endPlan));
        released(endPoint, MouseEvent.BUTTON1);

        return this;
    }

    @Override
    public Mouse drag(MouseTarget startTarget, MouseTarget endTarget, MouseActionContext context) {
        if (startTarget == null || endTarget == null) return this;

        MouseMovementPlan startPlan = planMovement(startTarget, MouseActionContext.DRAG);
        MouseMovementPlan endPlan = planMovement(endTarget, MouseActionContext.DRAG);
        return drag(startPlan, endPlan);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
