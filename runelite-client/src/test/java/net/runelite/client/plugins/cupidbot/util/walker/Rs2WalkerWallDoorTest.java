package net.runelite.client.plugins.cupidbot.util.walker;

import net.runelite.api.WallObject;
import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Rs2WalkerWallDoorTest {
    @Test
    public void wallDoorTouchesSegmentChecksOrientationB() {
        WallObject wall = mock(WallObject.class);
        WorldPoint doorTile = new WorldPoint(3100, 3100, 0);
        when(wall.getWorldLocation()).thenReturn(doorTile);
        when(wall.getOrientationA()).thenReturn(0);
        when(wall.getOrientationB()).thenReturn(4);

        assertTrue(Rs2Walker.wallDoorTouchesSegment(
                wall,
                doorTile,
                doorTile.dx(1)));
    }
}
