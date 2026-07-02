package net.runelite.client.plugins.cupidbot.shortestpath.pathfinder;

import net.runelite.client.plugins.cupidbot.shortestpath.WorldPointUtil;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PathfinderConfigDangerousTilesTest {
    @Test
    public void dangerousNpcTilesIncludeHazardAndMeleeRing() {
        assertTrue(PathfinderConfig.isDangerousAdjacentTilePacked(
                WorldPointUtil.packWorldPoint(3103, 3347, 0)));
        assertTrue(PathfinderConfig.isDangerousAdjacentTilePacked(
                WorldPointUtil.packWorldPoint(3104, 3348, 0)));
        assertFalse(PathfinderConfig.isDangerousAdjacentTilePacked(
                WorldPointUtil.packWorldPoint(3105, 3349, 0)));
    }
}
