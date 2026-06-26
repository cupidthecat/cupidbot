package net.runelite.client.plugins.cupidbot.accountselector;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.ProfileManager;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.cupidbot.CupidBot;
import net.runelite.client.plugins.cupidbot.util.mouse.VirtualMouse;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Mocrosoft + "AutoLogin",
        description = "CupidBot autologin plugin",
        tags = {"account", "cupidbot", "login"},
        enabledByDefault = false
)
@Slf4j
public class AutoLoginPlugin extends Plugin {
    @Inject
    AutoLoginScript accountSelectorScript;

    @Inject
    AutoLoginConfig autoLoginConfig;
    @Provides
    AutoLoginConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AutoLoginConfig.class);
    }

    @Override
    protected void startUp() throws AWTException {
		CupidBot.pauseAllScripts.compareAndSet(true, false);
        accountSelectorScript.run(autoLoginConfig);
    }

    protected void shutDown() {
        accountSelectorScript.shutdown();
    }

}
