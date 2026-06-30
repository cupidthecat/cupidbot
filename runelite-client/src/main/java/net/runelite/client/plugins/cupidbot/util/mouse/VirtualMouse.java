package net.runelite.client.plugins.cupidbot.util.mouse;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.plugins.cupidbot.CupidBot;
import net.runelite.client.plugins.cupidbot.util.math.Rs2Random;
import net.runelite.client.plugins.cupidbot.util.menu.NewMenuEntry;
import net.runelite.client.plugins.cupidbot.util.misc.Rs2UiHelper;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.cupidbot.util.Global.sleep;

@Slf4j
public class VirtualMouse extends Mouse {

    private final ScheduledExecutorService scheduledExecutorService;

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

    private void handleClick(Point point, boolean rightClick) {
        entered(point);
        exited(point);
        moved(point);
        sleep(nextClickStageDelayMs());
        pressed(point, rightClick ? MouseEvent.BUTTON3 : MouseEvent.BUTTON1);
        sleep(nextClickStageDelayMs());
        released(point, rightClick ? MouseEvent.BUTTON3 : MouseEvent.BUTTON1);
        sleep(nextClickStageDelayMs());
        clicked(point, rightClick ? MouseEvent.BUTTON3 : MouseEvent.BUTTON1);
        setLastClick(point);
    }

    static int nextClickStageDelayMs() {
        return Rs2Random.logNormalBounded(25, 90);
    }

    private boolean shouldMoveNaturally(Point point) {
        return point.getX() > 1
                && point.getY() > 1
                && CupidBot.naturalMouse != null;
    }

    private void moveNaturallyOrInstant(Point point) {
        if (shouldMoveNaturally(point)) {
            CupidBot.naturalMouse.moveTo(point.getX(), point.getY());
        } else {
            moveInstant(point);
        }
    }

    public Mouse click(Point point, boolean rightClick) {
        if (point == null) return this;

        Runnable clickAction = () -> {
            if (shouldMoveNaturally(point)) {
                CupidBot.naturalMouse.moveTo(point.getX(), point.getY());
            }
            handleClick(point, rightClick);
        };

        if (CupidBot.getClient().isClientThread()) {
            scheduledExecutorService.schedule(clickAction, 0, TimeUnit.MILLISECONDS);
        } else {
            clickAction.run();
        }

        return this;
    }


    public Mouse click(Point point, boolean rightClick, NewMenuEntry entry) {
        if (point == null) return this;

        Runnable clickAction = () -> {
            Point newPoint = point;
            if (shouldMoveNaturally(point)) {
                CupidBot.naturalMouse.moveTo(point.getX(), point.getY());

                if (Rs2UiHelper.hasActor(entry)) {
                    Rectangle rectangle = Rs2UiHelper.getActorClickbox(entry.getActor());
                    if (!Rs2UiHelper.isMouseWithinRectangle(rectangle)) {
                        newPoint = Rs2UiHelper.getClickingPoint(rectangle, true);
                        CupidBot.naturalMouse.moveTo(newPoint.getX(), newPoint.getY());
                    }
                }

                if (Rs2UiHelper.isGameObject(entry)) {
                    Rectangle rectangle = Rs2UiHelper.getObjectClickbox(entry.getGameObject());
                    if (!Rs2UiHelper.isMouseWithinRectangle(rectangle)) {
                        newPoint = Rs2UiHelper.getClickingPoint(rectangle, true);
                        CupidBot.naturalMouse.moveTo(newPoint.getX(), newPoint.getY());

                    }
                }
            }

            CupidBot.targetMenu = entry;
            handleClick(newPoint, rightClick);
        };

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
        return click(Rs2UiHelper.getClickingPoint(rectangle, true), false);
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

    public Mouse move(Point point) {
        if (point == null) return this;
        moveNaturallyOrInstant(point);
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
        Point pt = new Point((int) rect.getCenterX(), (int) rect.getCenterY());
        return move(pt);
    }

    public Mouse move(Polygon polygon) {
        if (polygon == null) return this;
        Point point = new Point((int) polygon.getBounds().getCenterX(), (int) polygon.getBounds().getCenterY());
        return move(point);
    }

    private Mouse scroll(Point point, int wheelRotation, int unitsToScroll) {
        if (point == null) return this;

        Runnable scrollAction = () -> {
            moveNaturallyOrInstant(point);
            sleep(Rs2Random.logNormalBounded(40, 100));
            dispatchWheel(point, wheelRotation, unitsToScroll);
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

        if (shouldMoveNaturally(startPoint))
            CupidBot.naturalMouse.moveTo(startPoint.getX(), startPoint.getY());
        else
            moveInstant(startPoint);
        sleep(Rs2Random.logNormalBounded(50, 80));
        pressed(startPoint, MouseEvent.BUTTON1);
        sleep(Rs2Random.logNormalBounded(80, 120));
        if (shouldMoveNaturally(endPoint))
            CupidBot.naturalMouse.moveTo(endPoint.getX(), endPoint.getY());
        else
            moveInstant(endPoint);
        sleep(Rs2Random.logNormalBounded(80, 120));
        released(endPoint, MouseEvent.BUTTON1);

        return this;
    }
}
