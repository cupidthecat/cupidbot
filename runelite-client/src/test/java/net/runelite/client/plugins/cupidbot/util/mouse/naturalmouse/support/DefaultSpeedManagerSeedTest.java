package net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.support;

import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.util.FlowTemplates;
import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.util.Pair;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DefaultSpeedManagerSeedTest
{
	@Test
	public void sameSeedProducesSameFlowAndDuration()
	{
		DefaultSpeedManager first = new DefaultSpeedManager(Arrays.asList(
			new Flow(FlowTemplates.constantSpeed()),
			new Flow(FlowTemplates.adjustingFlow())), new Random(1234L));
		DefaultSpeedManager second = new DefaultSpeedManager(Arrays.asList(
			new Flow(FlowTemplates.constantSpeed()),
			new Flow(FlowTemplates.adjustingFlow())), new Random(1234L));
		first.setMouseMovementBaseTimeMs(180);
		second.setMouseMovementBaseTimeMs(180);

		Pair<Flow, Long> firstFlow = first.getFlowWithTime(240.0);
		Pair<Flow, Long> secondFlow = second.getFlowWithTime(240.0);

		assertEquals(firstFlow.y, secondFlow.y);
		assertArrayEquals(firstFlow.x.getFlowCharacteristics(), secondFlow.x.getFlowCharacteristics(), 1e-9);
	}
}
