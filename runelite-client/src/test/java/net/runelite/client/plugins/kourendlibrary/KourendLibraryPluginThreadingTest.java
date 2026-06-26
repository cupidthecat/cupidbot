package net.runelite.client.plugins.kourendlibrary;

import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.events.ConfigChanged;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KourendLibraryPluginThreadingTest
{
	@Test
	public void showTargetHintArrowConfigChangeDefersClientReads() throws Exception
	{
		KourendLibraryPlugin plugin = new KourendLibraryPlugin();
		Client client = mock(Client.class);
		ClientThread clientThread = mock(ClientThread.class);

		when(client.getLocalPlayer()).thenThrow(new AssertionError("client state read was not deferred"));
		setField(plugin, "client", client);
		setField(plugin, "clientThread", clientThread);

		plugin.onConfigChanged(configChanged("showTargetHintArrow"));

		verify(clientThread).invokeLater(any(Runnable.class));
		verify(client, never()).getLocalPlayer();
	}

	@Test
	public void hideButtonConfigChangeDefersRegionCheck() throws Exception
	{
		KourendLibraryPlugin plugin = new KourendLibraryPlugin();
		KourendLibraryConfig config = mock(KourendLibraryConfig.class);
		ClientThread clientThread = mock(ClientThread.class);

		when(config.hideButton()).thenReturn(true);
		setField(plugin, "config", config);
		setField(plugin, "clientThread", clientThread);

		plugin.onConfigChanged(configChanged("hideButton"));

		verify(clientThread).invokeLater(any(Runnable.class));
	}

	private static ConfigChanged configChanged(String key)
	{
		ConfigChanged configChanged = new ConfigChanged();
		configChanged.setGroup(KourendLibraryConfig.GROUP_KEY);
		configChanged.setKey(key);
		return configChanged;
	}

	private static void setField(Object target, String name, Object value) throws Exception
	{
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}
}
