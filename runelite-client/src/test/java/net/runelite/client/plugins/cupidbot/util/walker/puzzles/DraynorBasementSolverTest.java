package net.runelite.client.plugins.cupidbot.util.walker.puzzles;

import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DraynorBasementSolverTest {
    @Test
    public void detectsOnlyDraynorBasementTargets() {
        assertTrue(DraynorBasementSolver.isBasementTarget(new WorldPoint(3108, 9759, 0)));
        assertFalse(DraynorBasementSolver.isBasementTarget(new WorldPoint(3108, 9759, 1)));
        assertFalse(DraynorBasementSolver.isBasementTarget(new WorldPoint(3200, 3200, 0)));
    }
}
