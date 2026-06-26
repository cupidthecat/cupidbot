package net.runelite.client.plugins.cupidbot.api.actor;

import org.junit.Test;

import static org.junit.Assert.assertNull;

public class Rs2ActorModelTest
{
	@Test
	public void getWorldLocationReturnsNullWhenActorMissing()
	{
		Rs2ActorModel model = new Rs2ActorModel(null);

		assertNull(model.getWorldLocation());
	}

	@Test
	public void getWorldViewReturnsNullWhenActorMissing()
	{
		Rs2ActorModel model = new Rs2ActorModel(null);

		assertNull(model.getWorldView());
	}
}
