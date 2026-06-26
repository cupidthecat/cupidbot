package net.runelite.client.plugins.cupidbot;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import javax.inject.Inject;
import java.io.IOException;
import java.util.UUID;

/**
 * Local CupidBot session facade.
 *
 * <p>CupidBot intentionally has no project-owned runtime service. These methods
 * keep older session call sites working without making outbound HTTP requests.
 */
@Slf4j
public class CupidBotApi {

    @Inject
    CupidBotApi(OkHttpClient ignoredClient, Gson ignoredGson) {
    }

    /**
     * Opens a local-only session identifier.
     */
    public UUID cupidbotOpen() throws IOException {
        return UUID.randomUUID();
    }

    /**
     * Plugin install telemetry is intentionally disabled in CupidBot.
     */
    public void increasePluginInstall(String internalName, String displayName, String version)
    {
        log.debug("Skipping local CupidBot plugin install telemetry for {}", internalName);
    }

    /**
     * Session pings are intentionally local no-ops.
     */
    public void cupidbotPing(UUID uuid, boolean loggedIn) throws IOException {
        log.trace("CupidBot local session ping: {}, loggedIn={}", uuid, loggedIn);
    }

    /**
     * Session deletion is a local no-op.
     */
    public void cupidbotDelete(UUID uuid) throws IOException {
        log.trace("CupidBot local session closed: {}", uuid);
    }
}
