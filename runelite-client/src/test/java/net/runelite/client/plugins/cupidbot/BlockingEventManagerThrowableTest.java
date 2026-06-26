package net.runelite.client.plugins.cupidbot;

import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BlockingEventManagerThrowableTest
{
	private BlockingEventManager manager;
	private Thread.UncaughtExceptionHandler previousUncaughtExceptionHandler;
	private boolean uncaughtExceptionHandlerChanged;

	@After
	public void tearDown()
	{
		if (manager != null)
		{
			manager.shutdown();
		}

		if (uncaughtExceptionHandlerChanged)
		{
			Thread.setDefaultUncaughtExceptionHandler(previousUncaughtExceptionHandler);
		}
	}

	@Test
	public void validateContinuesAfterBlockingEventThrowsError() throws Exception
	{
		manager = new BlockingEventManager();
		getBlockingEvents(manager).clear();
		BlockingEvent badEvent = new TestBlockingEvent("bad", BlockingEventPriority.HIGHEST)
		{
			@Override
			public boolean validate()
			{
				throw new NoClassDefFoundError("ch/qos/logback/classic/spi/ThrowableProxy");
			}
		};
		BlockingEvent goodEvent = new TestBlockingEvent("good", BlockingEventPriority.NORMAL);

		manager.add(badEvent);
		manager.add(goodEvent);

		invokeValidateAndEnqueue(manager);

		assertTrue(getEventQueue(manager).contains(goodEvent));
	}

	@Test
	public void executeContainsBlockingEventErrors() throws Exception
	{
		manager = new BlockingEventManager();
		getBlockingEvents(manager).clear();
		CountDownLatch uncaught = new CountDownLatch(1);
		AtomicReference<Throwable> uncaughtThrowable = new AtomicReference<>();
		previousUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		uncaughtExceptionHandlerChanged = true;
		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
		{
			if ("CupidBot-BlockingEvent".equals(thread.getName()))
			{
				uncaughtThrowable.set(throwable);
				uncaught.countDown();
			}
		});

		BlockingEvent badEvent = new TestBlockingEvent("bad", BlockingEventPriority.HIGHEST)
		{
			@Override
			public boolean execute()
			{
				throw new NoClassDefFoundError("ch/qos/logback/classic/spi/ThrowableProxy");
			}
		};
		getEventQueue(manager).offer(badEvent);

		assertTrue(manager.shouldBlockAndProcess());

		assertFalse("Blocking event errors must not escape to the executor thread",
			uncaught.await(2, TimeUnit.SECONDS));
		assertNull(uncaughtThrowable.get());
	}

	private static void invokeValidateAndEnqueue(BlockingEventManager manager) throws Exception
	{
		Method method = BlockingEventManager.class.getDeclaredMethod("validateAndEnqueueWithBackoff");
		method.setAccessible(true);
		try
		{
			method.invoke(manager);
		}
		catch (InvocationTargetException ex)
		{
			Throwable cause = ex.getCause();
			if (cause instanceof Exception)
			{
				throw (Exception) cause;
			}
			fail("Blocking event validation should contain throwable: " + cause);
		}
	}

	@SuppressWarnings("unchecked")
	private static BlockingQueue<BlockingEvent> getEventQueue(BlockingEventManager manager) throws Exception
	{
		Field field = BlockingEventManager.class.getDeclaredField("eventQueue");
		field.setAccessible(true);
		return (BlockingQueue<BlockingEvent>) field.get(manager);
	}

	@SuppressWarnings("unchecked")
	private static List<BlockingEvent> getBlockingEvents(BlockingEventManager manager) throws Exception
	{
		Field field = BlockingEventManager.class.getDeclaredField("blockingEvents");
		field.setAccessible(true);
		return (List<BlockingEvent>) field.get(manager);
	}

	private static class TestBlockingEvent implements BlockingEvent
	{
		private final String name;
		private final BlockingEventPriority priority;

		private TestBlockingEvent(String name, BlockingEventPriority priority)
		{
			this.name = name;
			this.priority = priority;
		}

		@Override
		public boolean validate()
		{
			return true;
		}

		@Override
		public boolean execute()
		{
			return true;
		}

		@Override
		public BlockingEventPriority priority()
		{
			return priority;
		}

		@Override
		public String getName()
		{
			return name;
		}
	}
}
