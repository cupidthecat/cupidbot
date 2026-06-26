package net.runelite.client.plugins.cupidbot.agentserver.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.client.plugins.cupidbot.CupidBot;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class StateHandler extends AgentHandler {

	private final Client client;

	public StateHandler(Gson gson, Client client) {
		super(gson);
		this.client = client;
	}

	@Override
	public String getPath() {
		return "/state";
	}

	@Override
	protected void handleRequest(HttpExchange exchange) throws IOException {
		try {
			requireGet(exchange);
		} catch (HttpMethodException e) {
			sendJson(exchange, 405, errorResponse(e.getMessage()));
			return;
		}

		Map<String, Object> state = new LinkedHashMap<>();
		GameState gameState = client.getGameState();
		state.put("loggedIn", CupidBot.isLoggedIn());
		state.put("gameState", gameState.name());

		if (CupidBot.isLoggedIn()) {
			try {
				CupidBot.getClientThread().runOnClientThreadOptional(() -> {
					Player localPlayer = client.getLocalPlayer();
					if (localPlayer == null) return null;

					Map<String, Object> playerInfo = new LinkedHashMap<>();
					playerInfo.put("name", localPlayer.getName());
					playerInfo.put("combatLevel", localPlayer.getCombatLevel());
					playerInfo.put("healthRatio", localPlayer.getHealthRatio());
					playerInfo.put("healthScale", localPlayer.getHealthScale());
					playerInfo.put("animating", localPlayer.getAnimation() != -1);
					playerInfo.put("animationId", localPlayer.getAnimation());
					playerInfo.put("moving", localPlayer.getPoseAnimation() != localPlayer.getIdlePoseAnimation());
					playerInfo.put("interacting", localPlayer.isInteracting());

					var loc = CupidBot.getRs2PlayerStateCache().getLocalPlayerPosition();
					if (loc != null) {
						Map<String, Integer> position = new LinkedHashMap<>();
						position.put("x", loc.getX());
						position.put("y", loc.getY());
						position.put("plane", loc.getPlane());
						playerInfo.put("position", position);
					}

					state.put("player", playerInfo);
					return null;
				});
			} catch (Exception e) {
				log.debug("Failed to retrieve player state", e);
			}
		}

		state.put("scriptsPaused", CupidBot.pauseAllScripts.get());
		sendJson(exchange, 200, state);
	}
}
