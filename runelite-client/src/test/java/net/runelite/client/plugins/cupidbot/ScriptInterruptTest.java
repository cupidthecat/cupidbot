package net.runelite.client.plugins.cupidbot;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScriptInterruptTest
{
	@Test
	public void detectsClientThreadInterruptWrapper()
	{
		RuntimeException ex = new RuntimeException(
			"Interrupted waiting for client thread",
			new InterruptedException());

		assertTrue(Script.isInterruption(ex));
	}

	@Test
	public void detectsNestedInterruptedException()
	{
		RuntimeException ex = new RuntimeException(
			"outer",
			new IllegalStateException("inner", new InterruptedException()));

		assertTrue(Script.isInterruption(ex));
	}

	@Test
	public void rejectsUnrelatedRuntimeException()
	{
		assertFalse(Script.isInterruption(new RuntimeException("Timed out waiting for client thread")));
	}
}
