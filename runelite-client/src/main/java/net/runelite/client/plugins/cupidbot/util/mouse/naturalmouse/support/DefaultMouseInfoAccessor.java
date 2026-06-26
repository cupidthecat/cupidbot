package net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.support;

import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.api.MouseInfoAccessor;

import java.awt.*;

public class DefaultMouseInfoAccessor implements MouseInfoAccessor {

    @Override
    public Point getMousePosition() {
        return MouseInfo.getPointerInfo().getLocation();
    }
}
