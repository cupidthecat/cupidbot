package net.runelite.client.plugins.cupidbot;

import javax.swing.SwingUtilities;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * Local splash tip provider. CupidBot does not fetch splash text from a service.
 */
public class RandomFactClient
{
	private static final List<String> LOCAL_TIPS = List.of(
		"CupidBot loads local plugins from ~/.runelite/cupidbot-plugins.",
		"CupidBot stores launcher client jars in ~/.cupidbot.",
		"The CupidBot launcher can manage multiple Jagex accounts locally.",
		"Script settings are stored per profile, so each profile can have its own configuration.",
		"CupidBot supports local plugin jars, so startup only loads the plugins you install.",
		"CupidBot can use RuneLite profile data to fill credentials when auto-login is configured.",
		"CupidBot's web walker handles pathfinding and common obstacle traversal.",
		"CupidBot uses menu swapping to prioritize specific in-game actions.",
		"The QoL plugin includes features such as auto eating, prayer potion handling, and cannon reload helpers.",
		"Inventory setups can help equip and withdraw items based on saved profiles.",
		"Use --developer-mode for plugin debugging or local development.",
		"Run with --clean-randomdat to delete random.dat before recreating it as read-only.",
		"HTTP proxies do not handle in-game network traffic. Use SOCKS5 proxies instead.",
		"A full Graceful set reduces run energy drain by 30%.",
		"Minnows at 82 Fishing are profitable and can reach 40k+ XP per hour.",
		"Herb runs can take about 90 seconds and can be profitable every growth cycle.",
		"Mahogany Homes is a cheaper way to train Construction while still giving decent XP rates.",
		"Construction is expensive, but POH portals and restoration pools can save supplies over time.",
		"Bone Voyage unlocks Fossil Island, including birdhouses, herbiboar, and Volcanic Mine.",
		"Karambwans can be combo-eaten with other food to restore a large amount of HP in one tick.",
		"Looting bags can double inventory storage while you are in the wilderness.",
		"Protect Item lets you keep four items on death instead of three.",
		"Using Preserve extends many boosted stat timers by 50%.",
		"Piety gives a major melee boost and is often worth the prayer drain at bosses.",
		"Stamina potions make long movement-heavy trips smoother.",
		"Barrows is efficient when you reach about 88% reward potential by killing a few tunnel monsters.",
		"Morytania Hard Diary makes Barrows more profitable by improving rune rewards.",
		"Black d'hide has strong magic defence for its cost.",
		"Dragon hunter lance is better than a rapier against dragons and drakes.",
		"Ancestral robe pieces each add magic damage, making them valuable for high-end magic setups.",
		"Avernic defender is a small strength upgrade over dragon defender, but the bonus can affect max hits.",
		"Ring of suffering recoil charges can deal thousands of passive damage on long boss trips.",
		"At Zulrah, preset gear swaps can improve kills per hour compared with manual switching.",
		"Tombs of Amascut scales by invocation level; 150 invocation is a common profit target.",
		"Chambers of Xeric scales with team size; solos can give better personal loot but take longer.",
		"Deaths at Tombs of Amascut cost time and supplies, not invocation score.",
		"Gauntlet requires no supplies and becomes strong solo GP per hour once mastered.",
		"Nex rewards coordinated teams and benefits from freezes or reliable minion damage.",
		"Revenants can be profitable, but risk and world hopping are part of the activity.",
		"Using an alt account to scout worlds can save time at crowded bosses.",
		"Using a weapon special attack before switching gear can avoid losing attack speed.",
		"You can animation stall some food eats to avoid losing a tick during PvM.",
		"Tempoross is a low-requirement money maker once you have 35 Fishing.",
		"The Lunar spellbook's NPC Contact can work in instanced areas.",
		"The Legends' Guild shop sells dragon battleaxes, which can be useful for stat-boosting setups."
	);

	public static String getRandomFact()
	{
		return LOCAL_TIPS.get(ThreadLocalRandom.current().nextInt(LOCAL_TIPS.size()));
	}

	static List<String> getLocalTips()
	{
		return Collections.unmodifiableList(LOCAL_TIPS);
	}

	public static void getRandomFactAsync(Consumer<String> callback)
	{
		CompletableFuture
			.supplyAsync(RandomFactClient::getRandomFact)
			.thenAccept(fact -> SwingUtilities.invokeLater(() -> callback.accept(fact)));
	}
}
