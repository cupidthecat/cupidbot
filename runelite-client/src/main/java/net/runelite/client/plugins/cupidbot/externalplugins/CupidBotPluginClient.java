/*
 * Copyright (c) 2023 CupidBot
 * All rights reserved.
 */
package net.runelite.client.plugins.cupidbot.externalplugins;

import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class CupidBotPluginClient
{
	private static final File PLUGIN_DIR = new File(RuneLite.RUNELITE_DIR, "cupidbot-plugins");
	private static final File PLUGINS_JSON = new File(PLUGIN_DIR, "plugins.json");

	private final Gson gson;

	@Inject
	private CupidBotPluginClient(OkHttpClient ignoredOkHttpClient, Gson gson)
	{
		this.gson = gson;
	}

	/**
	 * Reads the local CupidBot plugin manifest from ~/.runelite/cupidbot-plugins/plugins.json.
	 */
	public List<CupidBotPluginManifest> downloadManifest() throws IOException
	{
		if (!PLUGINS_JSON.isFile())
		{
			log.info("No local CupidBot plugin manifest found at {}", PLUGINS_JSON.getAbsolutePath());
			return Collections.emptyList();
		}

		try (FileReader reader = new FileReader(PLUGINS_JSON, StandardCharsets.UTF_8))
		{
			List<CupidBotPluginManifest> manifests = gson.fromJson(
				reader,
				new TypeToken<List<CupidBotPluginManifest>>() {}.getType());
			return manifests == null ? Collections.emptyList() : manifests;
		}
	}

	/**
	 * Loads plugin icons only from local paths. HTTP(S) icon URLs are ignored.
	 */
	public BufferedImage downloadIcon(String iconUrl) throws IOException
	{
		if (Strings.isNullOrEmpty(iconUrl)
			|| iconUrl.startsWith("http://")
			|| iconUrl.startsWith("https://"))
		{
			return null;
		}

		File iconFile = resolveLocalFile(iconUrl);
		return iconFile.isFile() ? ImageIO.read(iconFile) : null;
	}

	public HttpUrl getJarURL(CupidBotPluginManifest manifest, String versionOverride)
	{
		return null;
	}

	public Map<String, Integer> getPluginCounts() throws IOException
	{
		return Map.of();
	}

	public JsonArray fetchAllReleases()
	{
		return new JsonArray();
	}

	public List<String> parseVersionsFromReleases(CupidBotPluginManifest manifest, JsonArray ignoredReleases)
	{
		if (manifest == null || Strings.isNullOrEmpty(manifest.getVersion()))
		{
			return Collections.emptyList();
		}
		return List.of(manifest.getVersion());
	}

	public Optional<String> findFirstAssetCreatedAt(CupidBotPluginManifest manifest, JsonArray ignoredReleases)
	{
		return manifest == null ? Optional.empty() : manifest.getAddedInstant().map(Object::toString);
	}

	public List<String> fetchAvailableVersions(CupidBotPluginManifest manifest)
	{
		return parseVersionsFromReleases(manifest, new JsonArray());
	}

	private File resolveLocalFile(String path)
	{
		if (path.startsWith("file:"))
		{
			return new File(URI.create(path));
		}

		File file = new File(path);
		return file.isAbsolute() ? file : new File(PLUGIN_DIR, path);
	}
}
