package net.runelite.client.plugins.cupidbot;

import net.runelite.api.Client;
import net.runelite.api.ScriptEvent;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CupidBotPluginBankPinTest
{
	@Mock
	private Client client;

	@Mock
	private Widget button;

	@Mock
	private Widget buttonRect;

	@Mock
	private ScriptEvent keyEvent;

	@Test
	public void bankPinKeyListenerRunsButtonOpForMatchingDigit()
	{
		Object[] onOpListener = new Object[] { 1, "pin" };
		when(client.getWidget(123)).thenReturn(button);
		when(button.getChild(0)).thenReturn(buttonRect);
		when(buttonRect.getOnOpListener()).thenReturn(onOpListener);
		when(keyEvent.getTypedKeyChar()).thenReturn((int) '3');
		when(client.getGameCycle()).thenReturn(50);

		assertTrue(CupidBotPlugin.installBankPinKeyListener(client, 123, 3));

		JavaScriptCallback callback = captureKeyListener();
		callback.run(keyEvent);

		verify(client).runScript(onOpListener);
		verify(client).setVarcIntValue(VarClientID.KEYBOARD_TIMEOUT, 51);
	}

	@Test
	public void bankPinKeyListenerIgnoresOtherDigits()
	{
		Object[] onOpListener = new Object[] { 1, "pin" };
		when(client.getWidget(123)).thenReturn(button);
		when(button.getChild(0)).thenReturn(buttonRect);
		when(buttonRect.getOnOpListener()).thenReturn(onOpListener);
		when(keyEvent.getTypedKeyChar()).thenReturn((int) '4');

		assertTrue(CupidBotPlugin.installBankPinKeyListener(client, 123, 3));

		JavaScriptCallback callback = captureKeyListener();
		callback.run(keyEvent);

		verify(client, never()).runScript(onOpListener);
		verify(client, never()).setVarcIntValue(VarClientID.KEYBOARD_TIMEOUT, 1);
	}

	private JavaScriptCallback captureKeyListener()
	{
		ArgumentCaptor<Object> listenerCaptor = ArgumentCaptor.forClass(Object.class);
		verify(buttonRect).setOnKeyListener(listenerCaptor.capture());
		return (JavaScriptCallback) listenerCaptor.getValue();
	}
}
