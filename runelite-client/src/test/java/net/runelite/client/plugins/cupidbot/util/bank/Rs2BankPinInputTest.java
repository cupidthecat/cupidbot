package net.runelite.client.plugins.cupidbot.util.bank;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Rs2BankPinInputTest
{
	@Test
	public void bankPinPromptDetectionUsesInstructionWidget()
		throws Exception
	{
		String source = Files.readString(Path.of(
			"src",
			"main",
			"java",
			"net",
			"runelite",
			"client",
			"plugins",
			"cupidbot",
			"util",
			"bank",
			"Rs2Bank.java"
		), StandardCharsets.UTF_8);

		assertTrue(source.contains("InterfaceID.BankpinKeypad.INSTRUCTION"));
		assertFalse(source.contains("hasWidgetText(expectedInstruction, 213, 10"));
	}

	@Test
	public void bankPinInputDoesNotRequireRuneLiteBankPlugin()
		throws Exception
	{
		String source = readRs2BankSource();

		assertTrue(source.contains("Rs2Keyboard.keyPress(c)"));
		assertFalse(source.contains("isBankPluginEnabled() && hasKeyboardBankPinEnabled()"));
	}

	@Test
	public void bankPinAutomationDoesNotRespectRuneLiteKeyboardToggle()
		throws Exception
	{
		String source = readRs2BankSource();

		assertFalse(source.contains("hasKeyboardBankPinEnabled()"));
		assertFalse(source.contains("isKeyboardBankPinConfigEnabled"));
		assertFalse(source.contains("Rs2Widget.clickWidget(String.valueOf(c)"));
	}

	@Test
	public void bankPinAutomationDoesNotLogWidgetClickFailures()
		throws Exception
	{
		String source = readRs2BankSource();

		assertFalse(source.contains("Failed to click bank PIN digit"));
	}

	private static String readRs2BankSource()
		throws Exception
	{
		return Files.readString(Path.of(
			"src",
			"main",
			"java",
			"net",
			"runelite",
			"client",
			"plugins",
			"cupidbot",
			"util",
			"bank",
			"Rs2Bank.java"
		), StandardCharsets.UTF_8);
	}
}
