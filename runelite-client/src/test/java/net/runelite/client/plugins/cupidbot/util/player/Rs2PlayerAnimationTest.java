package net.runelite.client.plugins.cupidbot.util.player;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.cupidbot.CupidBot;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.Category;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class Rs2PlayerAnimationTest
{
	private Object previousClient;
	private Object previousClientThread;

	@Before
	public void setUp() throws Exception
	{
		previousClient = getStaticField("client");
		previousClientThread = getStaticField("clientThread");
	}

	@After
	public void tearDown() throws Exception
	{
		setStaticField("client", previousClient);
		setStaticField("clientThread", previousClientThread);
	}

	@Test
	public void getPoseAnimationReturnsIdleSentinelWhenLocalPlayerIsMissing() throws Exception
	{
		installClientWithMissingLocalPlayer();

		assertEquals(-1, Rs2Player.getPoseAnimation());
	}

	@Test
	public void antibanCategoryBusyIsFalseWhenLoggedInStateHasNoLocalPlayer() throws Exception
	{
		ClientThread clientThread = installClientWithMissingLocalPlayer();

		assertFalse(Category.SKILLING_FIREMAKING.isBusy());
		verify(clientThread, never()).runOnClientThreadOptional(any());
	}

	private static ClientThread installClientWithMissingLocalPlayer() throws Exception
	{
		Client client = mock(Client.class);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		when(client.getLocalPlayer()).thenReturn(null);

		ClientThread clientThread = clientThreadThatRunsCallables();
		setStaticField("client", client);
		setStaticField("clientThread", clientThread);
		return clientThread;
	}

	private static ClientThread clientThreadThatRunsCallables()
	{
		ClientThread clientThread = mock(ClientThread.class);
		doAnswer(invocation ->
		{
			Callable<?> callable = invocation.getArgument(0);
			return Optional.ofNullable(callable.call());
		}).when(clientThread).runOnClientThreadOptional(any());
		return clientThread;
	}

	private static Object getStaticField(String name) throws Exception
	{
		Field field = CupidBot.class.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(null);
	}

	private static void setStaticField(String name, Object value) throws Exception
	{
		Field field = CupidBot.class.getDeclaredField(name);
		field.setAccessible(true);
		field.set(null, value);
	}
}
