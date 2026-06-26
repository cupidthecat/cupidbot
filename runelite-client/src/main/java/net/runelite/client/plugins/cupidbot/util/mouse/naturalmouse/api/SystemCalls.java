package net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.api;

import java.awt.*;

/**
 * Abstracts ordinary static System calls away
 */
public interface SystemCalls {
    long currentTimeMillis();

    void sleep(long time);

    Dimension getScreenSize();

    void setMousePosition(int x, int y);
}
