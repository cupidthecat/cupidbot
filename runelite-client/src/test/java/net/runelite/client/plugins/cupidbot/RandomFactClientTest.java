package net.runelite.client.plugins.cupidbot;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RandomFactClientTest
{
	@Test
	public void getRandomFactReturnsTipsFromARotatingPool()
	{
		Set<String> seenTips = new HashSet<>();

		for (int i = 0; i < 100; i++)
		{
			seenTips.add(RandomFactClient.getRandomFact());
		}

		assertTrue("Splash facts should rotate through more than one local tip", seenTips.size() > 1);
	}

	@Test
	public void localTipsDoNotExposeMicrobotOnlyClaims()
	{
		assertTrue("Splash facts should have a useful local pool", RandomFactClient.getLocalTips().size() >= 25);

		for (String tip : RandomFactClient.getLocalTips())
		{
			String normalized = tip.toLowerCase();

			assertFalse("Tip must not mention Microbot: " + tip, normalized.contains("microbot"));
			assertFalse("Tip must not mention Microbot cloud: " + tip, normalized.contains("microbot.cloud"));
			assertFalse("Tip must not mention themicrobot: " + tip, normalized.contains("themicrobot"));
			assertFalse("Tip must not mention YouTube-only claims: " + tip, normalized.contains("youtube"));
			assertFalse("Tip must not mention Discord-only claims: " + tip, normalized.contains("discord"));
			assertFalse("Tip must not keep Microbot's 2023 history claim: " + tip, normalized.contains("started in 2023"));
			assertFalse("Tip must not keep Microbot's 2023 history claim: " + tip, normalized.contains("first launched in 2023"));
		}
	}
}
