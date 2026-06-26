package net.runelite.client.plugins.cupidbot;

import ch.qos.logback.classic.LoggerContext;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLiteProperties;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ClientShutdown;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.OverlayMenuClicked;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.cupidbot.pouch.PouchOverlay;
import net.runelite.client.plugins.cupidbot.ui.CupidBotPluginConfigurationDescriptor;
import net.runelite.client.plugins.cupidbot.ui.CupidBotPluginListPanel;
import net.runelite.client.plugins.cupidbot.ui.CupidBotTopLevelConfigPanel;
import net.runelite.client.plugins.cupidbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.cupidbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.cupidbot.util.huntkit.Rs2HuntKit;
import net.runelite.client.plugins.cupidbot.util.inventory.Rs2Gembag;
import net.runelite.client.plugins.cupidbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.cupidbot.util.inventory.Rs2RunePouch;
import net.runelite.client.plugins.cupidbot.util.overlay.GembagOverlay;
import net.runelite.client.plugins.cupidbot.util.player.Rs2Player;
import net.runelite.client.plugins.cupidbot.util.reflection.Rs2Reflection;
import net.runelite.client.plugins.cupidbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.cupidbot.util.leaguetransport.Rs2LeaguesTransport;
import net.runelite.client.plugins.cupidbot.util.leaguetransport.SeasonalTransportHandlers;
import net.runelite.client.plugins.cupidbot.api.boat.Rs2BoatCache;
import net.runelite.client.plugins.cupidbot.util.shop.Rs2Shop;
import net.runelite.client.plugins.cupidbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.cupidbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.cupidbot.util.security.LoginManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.util.ImageUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
@PluginDescriptor(
	name = PluginDescriptor.Default + "CupidBot",
	description = "CupidBot",
	tags = {"main", "cupidbot", "parent"},
	alwaysOn = true,
	hidden = true,
	priority = true
)
@Slf4j
public class CupidBotPlugin extends Plugin
{
	/**
	 * Max age of {@code lastTransportAttempt} for attributing locked-region chat to a click.
	 * Canonical value is {@link Rs2LeaguesTransport#LEAGUES_LOCK_CHAT_MAX_ATTEMPT_AGE_MS}; kept here for script compatibility.
	 *
	 * @apiNote Treat as stable external API: renames or semantic changes break scripts — note in changelog when modifying.
	 */
	public static final long LEAGUES_LOCK_CHAT_MAX_ATTEMPT_AGE_MS = Rs2LeaguesTransport.LEAGUES_LOCK_CHAT_MAX_ATTEMPT_AGE_MS;
	private EnumSet<WorldType> lastWorldTypeProfile = null;

	@Inject
	private Provider<CupidBotPluginListPanel> pluginListPanelProvider;

	@Inject
	private Provider<CupidBotTopLevelConfigPanel> topLevelConfigPanelProvider;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ConfigManager configManager;

	@Inject
	private CupidBotConfig cupidbotConfig;

	private CupidBotTopLevelConfigPanel topLevelConfigPanel;

	private NavigationButton navButton;

	@Provides
	@Singleton
	CupidBotConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CupidBotConfig.class);
	}

	@Inject
	private OverlayManager overlayManager;
	@Inject
	private CupidBotOverlay cupidbotOverlay;
	@Inject
	private GembagOverlay gembagOverlay;
	@Inject
	private PouchOverlay pouchOverlay;
	@Inject
	private EventBus eventBus;
	private GameChatAppender gameChatAppender;

	@Inject
	private CupidBotVersionChecker cupidbotVersionChecker;
	
	// Widget change tracking for overlay cache invalidation
	private volatile boolean widgetLayoutChanged = false;
	private Rectangle lastCheckedBounds = null;
	private boolean lastOverlapResult = false;
	/**
	 * Initializes the cache system and registers all caches with the EventBus.
	 * Cache loading from configuration will happen later during game events.
	 */
	@Override
	protected void startUp() throws AWTException
	{
		log.info("CupidBot: {} - {}", RuneLiteProperties.getCupidBotVersion(), RuneLiteProperties.getCupidBotCommit());
		log.info("JVM: {} {}", System.getProperty("java.vendor"), System.getProperty("java.runtime.version"));

		cupidbotVersionChecker.checkForUpdate();

		gameChatAppender = new GameChatAppender();
		gameChatAppender.setName("GAME_CHAT");
		
		// Set pattern based on new configuration
		String pattern = cupidbotConfig.getGameChatLogPattern().getPattern();
		gameChatAppender.setPattern(pattern);

		final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		gameChatAppender.setContext(context);
		context.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(gameChatAppender);

		// Start appender if logging is enabled
		if (cupidbotConfig.enableGameChatLogging()) {
			gameChatAppender.start();
		}
		
		// Initialize the cached configuration in GameChatAppender
		GameChatAppender.updateConfiguration(
			cupidbotConfig.enableGameChatLogging(),
			cupidbotConfig.getGameChatLogLevel().getLevel(),
			cupidbotConfig.onlyCupidBotLogging()
		);

		CupidBot.pauseAllScripts.set(false);
		CupidBot.enableAutoRunOn = cupidbotConfig.enableAutoRunOn();
		CupidBot.useStaminaPotsIfNeeded = cupidbotConfig.useStaminaPotsIfNeeded();
		CupidBot.getBlockingEventManager().start();

		CupidBotPluginListPanel pluginListPanel = pluginListPanelProvider.get();
		pluginListPanel.addFakePlugin(new CupidBotPluginConfigurationDescriptor(
			"CupidBot", "CupidBot client settings",
			new String[]{"client"},
			cupidbotConfig, configManager.getConfigDescriptor(cupidbotConfig)
		));
		pluginListPanel.rebuildPluginList();

		topLevelConfigPanel = topLevelConfigPanelProvider.get();

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "cupidbot_config_icon_lg.png");

		navButton = NavigationButton.builder()
			.tooltip("Community Plugins")
			.icon(icon)
			.priority(0)
			.panel(topLevelConfigPanel)
			.build();

		clientToolbar.addNavigation(navButton);

		new InputSelector(clientToolbar);

		CupidBot.getPouchScript().startUp();

		Rs2Walker.setSeasonalTransportHandlers(SeasonalTransportHandlers.defaultHandlerList());

		if (overlayManager != null)
		{
			overlayManager.add(cupidbotOverlay);
			overlayManager.add(gembagOverlay);
			overlayManager.add(pouchOverlay);
		}

	}

	protected void shutDown()
	{
		overlayManager.remove(cupidbotOverlay);
		overlayManager.remove(gembagOverlay);
		overlayManager.remove(pouchOverlay);
		clientToolbar.removeNavigation(navButton);
		if (gameChatAppender.isStarted()) gameChatAppender.stop();
		cupidbotVersionChecker.shutdown();
	}


	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		CupidBot.setIsGainingExp(true);
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		String newProfile = event.getNewProfile();
		String oldProfile = event.getPreviousProfile();
		if ((newProfile != null && !newProfile.isEmpty()) &&
			(oldProfile == null || oldProfile.isEmpty() || !newProfile.equals(oldProfile))
		)
		{
			log.info("\nReceived RuneScape profile change event from '{}' to '{}'", oldProfile, newProfile);
		}
		
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		CupidBot.getPouchScript().onItemContainerChanged(event);
		if (event.getContainerId() == InventoryID.INV)
		{
			Rs2Inventory.storeInventoryItemsInMemory(event);
		}
		else if (event.getContainerId() == InventoryID.WORN)
		{
			Rs2Equipment.storeEquipmentItemsInMemory(event);
		}
		else if (event.getContainerId() == InventoryID.BANK)
		{
			Rs2Bank.updateLocalBank(event);
		}
		else if (event.getContainerId() == InventoryID.HUNTSMANS_KIT)
		{
			Rs2HuntKit.updateLocalKit(event);
		}
		else if (Arrays.stream(getShopContainerIds()).anyMatch(sid -> Objects.equals(event.getContainerId(), sid))) {
			Rs2Shop.storeShopItemsInMemory(event, event.getContainerId());
		}
	}

	@Subscribe
	public void onScriptCallbackEvent(ScriptCallbackEvent event)
	{
		if (!"bankpinButtonSetup".equals(event.getEventName()))
		{
			return;
		}

		Client client = CupidBot.getClient();
		if (client == null || client.getIntStackSize() < 2)
		{
			return;
		}

		int[] intStack = client.getIntStack();
		int intStackSize = client.getIntStackSize();
		installBankPinKeyListener(client, intStack[intStackSize - 2], intStack[intStackSize - 1]);
	}

	static boolean installBankPinKeyListener(Client client, int compId, int buttonId)
	{
		Widget button = client.getWidget(compId);
		if (button == null)
		{
			return false;
		}

		Widget buttonRect = button.getChild(0);
		if (buttonRect == null)
		{
			return false;
		}

		Object[] onOpListener = buttonRect.getOnOpListener();
		if (onOpListener == null)
		{
			return false;
		}

		buttonRect.setOnKeyListener((JavaScriptCallback) e -> {
			int typedChar = e.getTypedKeyChar() - '0';
			if (typedChar != buttonId)
			{
				return;
			}

			client.runScript(onOpListener);
			client.setVarcIntValue(VarClientID.KEYBOARD_TIMEOUT, client.getGameCycle() + 1);
		});
		return true;
	}

	/**
	 * Retrieves all currently open container IDs from {@link InventoryID}
	 * and excludes specific container IDs.
	 *
	 * @return an array of open container IDs excluding the specified excluded IDs
	 */
	private int[] getShopContainerIds()
	{
		Field[] fields = InventoryID.class.getFields();
		List<Integer> openContainerIds = new ArrayList<>();
		int[] excludedIds = { 90, 93, 94, 95 };

		for (Field field : fields)
		{
			if (field.getType() != int.class)
				continue;

			try
			{
				int containerId = field.getInt(null);
				ItemContainer container = CupidBot.getClient().getItemContainer(containerId);
				
				if (container != null && container.getItems() != null && container.getItems().length > 0) {
					boolean hasItems = Arrays.stream(container.getItems())
						.anyMatch(item -> item != null && item.getId() != -1);
						
					if (hasItems && Arrays.stream(excludedIds).noneMatch(excludedId -> excludedId == containerId)) {
						openContainerIds.add(containerId);
					}
				}
			}
			catch (IllegalAccessException e)
            {
                log.error("Failed to access field: {}", field.getName(), e);
            }
		}
		return openContainerIds.stream().mapToInt(Integer::intValue).toArray();
	}



	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		
	   if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
	   {
		   // Region-based login detection logic
		   final Client client = CupidBot.getClient();
		   if (client != null) {
				EnumSet<WorldType> worldTypeProfile = normalizeWorldTypesForProfileComparison(client.getWorldType());
				if (lastWorldTypeProfile != null && !lastWorldTypeProfile.equals(worldTypeProfile))
				{
					Rs2Bank.invalidateBankMirrorCache("world-type-profile-transition");
				}
				lastWorldTypeProfile = worldTypeProfile;
				int[] currentRegions = client.getTopLevelWorldView().getMapRegions();
				boolean wasLoggedIn = LoginManager.getLastKnownGameState() == GameState.LOGGED_IN;
				if (!wasLoggedIn) {
					LoginManager.markLoggedIn();
					Rs2RunePouch.fullUpdate();
				}
				if (currentRegions != null) {
					CupidBot.setLastKnownRegions(currentRegions.clone());
				}
		   }
	   }
	   if (gameStateChanged.getGameState() == GameState.HOPPING || gameStateChanged.getGameState() == GameState.LOGIN_SCREEN || gameStateChanged.getGameState() == GameState.CONNECTION_LOST)
	   {
		   // Clear all cache states when logging out through Rs2CacheManager
		   //Rs2CacheManager.emptyCacheState(); // should not be nessary here, handled in ClientShutdown event,
		   // and we also handle correct cache loading in onRuneScapeProfileChanged event
		   LoginManager.markLoggedOut();
		   CupidBot.setLastKnownRegions(null);
		   Rs2LeaguesTransport.onLogout();
	   }
	   // update last known game state to track login/logout transitions
	   LoginManager.setLastKnownGameState(gameStateChanged.getGameState());
	}

	private static EnumSet<WorldType> normalizeWorldTypesForProfileComparison(EnumSet<WorldType> rawTypes)
	{
		EnumSet<WorldType> normalized = rawTypes == null
				? EnumSet.noneOf(WorldType.class)
				: rawTypes.clone();
		// Profile compare should ignore normal-world and combat-variant flags.
		normalized.remove(WorldType.MEMBERS);
		normalized.remove(WorldType.PVP);
		normalized.remove(WorldType.BOUNTY);
		normalized.remove(WorldType.SKILL_TOTAL);
		normalized.remove(WorldType.HIGH_RISK);
		normalized.remove(WorldType.LAST_MAN_STANDING);
		return normalized;
	}

	@Subscribe
	public void onVarClientIntChanged(VarClientIntChanged event)
	{
		Rs2Tab.onVarClientIntChanged(event);
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		Rs2Player.handlePotionTimers(event);
		Rs2Player.handleTeleblockTimer(event);
		Rs2RunePouch.onVarbitChanged(event);
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		Rs2Player.handleAnimationChanged(event);
	}

	@Subscribe(priority = 999)
	private void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (CupidBot.targetMenu != null && event.getType() != CupidBot.targetMenu.getType().getId())
		{
			CupidBot.getClient().getMenu().setMenuEntries(new MenuEntry[]{});
		}

		if (CupidBot.targetMenu != null)
		{
			MenuEntry entry =
				CupidBot.getClient().getMenu().createMenuEntry(-1)
                    .setItemId(0)
					.setOption(CupidBot.targetMenu.getOption())
					.setTarget(CupidBot.targetMenu.getTarget())
					.setIdentifier(CupidBot.targetMenu.getIdentifier())
					.setType(CupidBot.targetMenu.getType())
					.setParam0(CupidBot.targetMenu.getParam0())
					.setParam1(CupidBot.targetMenu.getParam1())
                    .setWorldViewId(CupidBot.targetMenu.getWorldViewId())
					.setForceLeftClick(false);

			if (CupidBot.targetMenu.getItemId() > 0)
			{
				try
				{
					Rs2Reflection.setItemId(entry, CupidBot.targetMenu.getItemId());
				}
				catch (IllegalAccessException | InvocationTargetException e)
				{
					log.error(e.getMessage(), e);
				}
			}
			CupidBot.getClient().getMenu().setMenuEntries(new MenuEntry[]{entry});
		}
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked event)
	{
		CupidBot.getPouchScript().onMenuOptionClicked(event);
		Rs2Gembag.onMenuOptionClicked(event);
		CupidBot.targetMenu = null;
		if (cupidbotConfig.enableMenuEntryLogging()) log.info(event.getMenuEntry().toString());
	}

	@Subscribe
	private void onChatMessage(ChatMessage event)
	{
		if (event.getType() == ChatMessageType.ENGINE)
		{
			String msg = event.getMessage();
			if (msg != null && msg.equalsIgnoreCase("I can't reach that!"))
			{
				CupidBot.cantReachTarget = true;
			}
		}
		if (event.getType() == ChatMessageType.GAMEMESSAGE)
		{
			String msg = event.getMessage();
			if (msg != null && containsIgnoreCase(msg, "you can't log into a non-members"))
			{
				CupidBot.cantHopWorld = true;
			}

			// Leagues: "haven't unlocked access to X area" -> blacklist last transport dest.
			if (msg != null)
			{
				Rs2LeaguesTransport.onLockedRegionGameMessage(msg);
			}
		}
		CupidBot.getPouchScript().onChatMessage(event);
		Rs2Gembag.onChatMessage(event);
	}

	private static boolean containsIgnoreCase(String haystack, String needle)
	{
		if (haystack == null || needle == null || needle.isEmpty())
		{
			return false;
		}
		int hLen = haystack.length();
		int nLen = needle.length();
		if (nLen > hLen)
		{
			return false;
		}
		for (int i = 0; i <= hLen - nLen; i++)
		{
			if (haystack.regionMatches(true, i, needle, 0, nLen))
			{
				return true;
			}
		}
		return false;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged ev)
	{
		if (ev.getGroup().equals(CupidBotConfig.configGroup)) {
			switch (ev.getKey()) {
				case CupidBotConfig.keyEnableAutoRunOn:
					CupidBot.enableAutoRunOn = cupidbotConfig.enableAutoRunOn();
					break;
				case CupidBotConfig.keyUseStaminaPotsIfNeeded:
					CupidBot.useStaminaPotsIfNeeded = cupidbotConfig.useStaminaPotsIfNeeded();
					break;
				case CupidBotConfig.keyEnableGameChatLogging:
				case CupidBotConfig.keyGameChatLogPattern:
				case CupidBotConfig.keyGameChatLogLevel:
				case CupidBotConfig.keyOnlyCupidBotLogging:
					// Handle any logging-related configuration changes
					final boolean shouldBeStarted = cupidbotConfig.enableGameChatLogging();

					// Update the cached configuration in GameChatAppender
					GameChatAppender.updateConfiguration(
							cupidbotConfig.enableGameChatLogging(),
							cupidbotConfig.getGameChatLogLevel().getLevel(),
							cupidbotConfig.onlyCupidBotLogging()
					);

					if (shouldBeStarted) {
						// Update pattern if needed
						String pattern = cupidbotConfig.getGameChatLogPattern().getPattern();
						gameChatAppender.setPattern(pattern);

						if (!gameChatAppender.isStarted()) {
							gameChatAppender.start();
						}
					} else if (gameChatAppender.isStarted()) {
						gameChatAppender.stop();
					}
					break;
				default:
					break;
			}
		}
		if (ev.getKey().equals("displayPouchCounter"))
		{
			if (Objects.equals(ev.getNewValue(), "true"))
			{
				CupidBot.getPouchScript().startUp();
			}
			else
			{
				CupidBot.getPouchScript().shutdown();
			}
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		Rs2RunePouch.onWidgetLoaded(event);
		
		// Mark that widget layout has changed for cache invalidation
		widgetLayoutChanged = true;
		log.debug("Widget {} loaded, layout changed", event.getGroupId());
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		// Mark that widget layout has changed for cache invalidation
		widgetLayoutChanged = true;
		log.debug("Widget {} closed, layout changed", event.getGroupId());
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		// Case 1: Hitsplat applied to the local player (indicates someone or something is attacking you)
		if (event.getActor().equals(CupidBot.getClient().getLocalPlayer()))
		{
			if (!event.getHitsplat().isOthers())
			{
				Rs2Player.updateCombatTime();
			}
		}

		// Case 2: Hitsplat is applied to another player (indicates you are attacking another player)
		else if (event.getActor() instanceof Player)
		{
			if (event.getHitsplat().isMine())
			{
				Rs2Player.updateCombatTime();
			}
		}

		// Case 3: Hitsplat is applied to an NPC (indicates you are attacking an NPC)
		else if (event.getActor() instanceof NPC)
		{
			if (event.getHitsplat().isMine())
			{
				Rs2Player.updateCombatTime();
			}
		}
	}

	@Subscribe
	public void onOverlayMenuClicked(OverlayMenuClicked overlayMenuClicked)
	{
		OverlayMenuEntry overlayMenuEntry = overlayMenuClicked.getEntry();
		if (overlayMenuEntry.getMenuAction() == MenuAction.RUNELITE_OVERLAY_CONFIG)
		{
			Overlay overlay = overlayMenuClicked.getOverlay();
			Plugin plugin = overlay.getPlugin();
			if (plugin == null)
			{
				return;
			}

			// Expand config panel for plugin
			SwingUtilities.invokeLater(() ->
			{
				clientToolbar.openPanel(navButton);
				topLevelConfigPanel.openConfigurationPanel(plugin.getName());
			});
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		// Start Leagues teleport calibration ASAP after login (non-blocking; prompts for consent once).
		Rs2LeaguesTransport.tickLeaguesCalibration();
	}

	@Subscribe(priority = 100)
	private void onClientShutdown(ClientShutdown e)
	{

	}

	/**
	 * Dynamically checks if any visible widget overlaps with the specified bounds
	 * @param overlayBoundsCanvas The bounds to check for widget overlap
	 * @return true if any visible widget overlaps with the specified bounds
	 */
	public boolean hasWidgetOverlapWithBounds(Rectangle overlayBoundsCanvas) {
		if (overlayBoundsCanvas == null || CupidBot.getClient() == null) {
			return false;
		}

	   int viewportXOffset = CupidBot.getClient().getViewportXOffset();
	   int viewportYOffset = CupidBot.getClient().getViewportYOffset();

		// Use cached result if widget layout hasn't changed and bounds are the same
		if (!this.widgetLayoutChanged && overlayBoundsCanvas.equals(this.lastCheckedBounds)) {
			return this.lastOverlapResult;
		}

	   boolean result = CupidBot.getClientThread().runOnClientThreadOptional(() -> {
		   try {
			   return Rs2Widget.checkBoundsOverlapWidgetInMainModal(overlayBoundsCanvas, viewportXOffset, viewportYOffset);
		   } catch (Exception e) {
			   log.debug("Error checking widget overlap: {}", e.getMessage());
			   return false;
		   }
	   }).orElse(false);

		// Cache the result
		widgetLayoutChanged = false;
		lastCheckedBounds = new Rectangle(overlayBoundsCanvas);
		lastOverlapResult = result;

		return result;
	}

    @Subscribe
    public void onWorldViewLoaded(WorldViewLoaded event)
    {
        CupidBot.getWorldViewIds().add(event.getWorldView().getId());
    }

    @Subscribe
    public void onWorldViewUnloaded(WorldViewUnloaded event)
    {
        CupidBot.getWorldViewIds().remove(event.getWorldView().getId());
    }
}
