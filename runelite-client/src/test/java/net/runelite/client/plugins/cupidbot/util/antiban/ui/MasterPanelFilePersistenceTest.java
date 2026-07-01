package net.runelite.client.plugins.cupidbot.util.antiban.ui;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.cupidbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.cupidbot.util.antiban.Rs2AntibanSettings;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.lang.reflect.Field;

import javax.swing.JButton;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MasterPanelFilePersistenceTest
{
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUp()
	{
		Rs2Antiban.resetAntibanSettings(true);
	}

	@After
	public void tearDown() throws Exception
	{
		setProfileConfigManager(null);
		Rs2Antiban.resetAntibanSettings(true);
	}

	@Test
	public void importAndExportButtonsAreAvailable() throws Exception
	{
		MasterPanel panel = new MasterPanel();

		assertEquals("Import", getButton(panel, "importButton").getText());
		assertEquals("Export", getButton(panel, "exportButton").getText());
		assertEquals("Reset", getButton(panel, "resetButton").getText());
	}

	@Test
	public void importSettingsFromFileAppliesFileAndSavesProfile() throws Exception
	{
		ConfigManager configManager = mock(ConfigManager.class);
		setProfileConfigManager(configManager);
		File settingsFile = tempFolder.newFile("cupidbot-antiban-settings.json");
		MasterPanel panel = new MasterPanel();
		Rs2AntibanSettings.usePlayStyle = true;
		Rs2AntibanSettings.takeMicroBreaks = true;
		Rs2AntibanSettings.microBreakDurationLow = 8;

		panel.exportSettingsToFile(settingsFile.toPath());

		Rs2AntibanSettings.reset();
		assertFalse(Rs2AntibanSettings.usePlayStyle);
		assertFalse(Rs2AntibanSettings.takeMicroBreaks);

		panel.importSettingsFromFile(settingsFile.toPath());

		assertTrue(Rs2AntibanSettings.usePlayStyle);
		assertTrue(Rs2AntibanSettings.takeMicroBreaks);
		assertEquals(8, Rs2AntibanSettings.microBreakDurationLow);
		verify(configManager, atLeastOnce()).setConfiguration(anyString(), anyString(), anyString());
	}

	private static void setProfileConfigManager(ConfigManager configManager) throws Exception
	{
		Field field = Rs2AntibanSettings.class.getDeclaredField("profileConfigManager");
		field.setAccessible(true);
		field.set(null, configManager);
	}

	private static JButton getButton(MasterPanel panel, String name) throws Exception
	{
		Field field = MasterPanel.class.getDeclaredField(name);
		field.setAccessible(true);
		return (JButton) field.get(panel);
	}
}
