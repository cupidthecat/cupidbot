package net.runelite.client;

import com.google.gson.Gson;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CupidBotNetworkBoundaryTest
{
	@Test
	public void telemetryIsDisabledByDefault()
	{
		assertTrue(RuneLite.isCupidBotTelemetryDisabled());
		assertTrue(RuneLiteDebug.isCupidBotTelemetryDisabled());
	}

	@Test
	public void launcherUpdatesAreNotDisabledByDefault()
	{
		assertFalse(RuneLite.isCupidBotLauncherUpdateDisabled());
		assertFalse(RuneLiteDebug.isCupidBotLauncherUpdateDisabled());
	}

	@Test
	public void telemetryClientDoesNotSendNetworkRequests() throws Exception
	{
		try (MockWebServer server = new MockWebServer())
		{
			server.start();
			TelemetryClient telemetryClient = new TelemetryClient(new OkHttpClient(), new Gson(), server.url("/"));

			telemetryClient.submitTelemetry();
			telemetryClient.submitError("test", "message", Map.of());

			assertNull(server.takeRequest(100, TimeUnit.MILLISECONDS));
		}
	}
}
