package net.runelite.client.plugins.cupidbot.util.player;

import net.runelite.api.MenuAction;
import net.runelite.api.gameval.InterfaceID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Rs2PlayerRunToggleTest
{
	@Test
	public void skipsClickWhenRunAlreadyMatchesTarget()
	{
		assertFalse(Rs2Player.shouldClickRunToggle(true, 1, null, 0, 1000));
		assertFalse(Rs2Player.shouldClickRunToggle(false, 0, null, 0, 1000));
	}

	@Test
	public void skipsDuplicatePendingRunToggleClick()
	{
		assertFalse(Rs2Player.shouldClickRunToggle(true, 0, true, 1000, 1500));
	}

	@Test
	public void retriesPendingRunToggleAfterDebounceWindow()
	{
		assertTrue(Rs2Player.shouldClickRunToggle(true, 0, true, 1000, 2600));
	}

	@Test
	public void allowsFirstNeededRunToggleClick()
	{
		assertTrue(Rs2Player.shouldClickRunToggle(true, 0, null, 0, 1000));
	}

	@Test
	public void buildsDirectRunToggleMenuAction()
	{
		Rs2Player.RunToggleMenuAction action = Rs2Player.runToggleMenuAction();

		assertEquals(-1, action.param0);
		assertEquals(InterfaceID.Orbs.RUNBUTTON, action.param1);
		assertEquals(MenuAction.CC_OP.getId(), action.opcode);
		assertEquals(1, action.identifier);
		assertEquals(-1, action.itemId);
		assertEquals("Toggle Run", action.option);
		assertEquals("", action.target);
		assertEquals(-1, action.canvasX);
		assertEquals(-1, action.canvasY);
	}
}
