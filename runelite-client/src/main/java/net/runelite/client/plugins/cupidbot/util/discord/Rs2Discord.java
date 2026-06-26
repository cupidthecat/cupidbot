package net.runelite.client.plugins.cupidbot.util.discord;

import com.google.gson.Gson;
import net.runelite.client.config.ConfigProfile;
import net.runelite.client.plugins.cupidbot.CupidBot;
import net.runelite.client.plugins.cupidbot.util.discord.models.DiscordEmbed;
import net.runelite.client.plugins.cupidbot.util.discord.models.DiscordPayload;
import net.runelite.client.plugins.cupidbot.util.security.LoginManager;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Rs2Discord
{
	private static final OkHttpClient httpClient = new OkHttpClient();
	private static final Gson GSON = new Gson();

	public static boolean sendWebhookMessage(String bodyMessage, List<DiscordEmbed> embeds, List<String> files)
	{
		String webHookUrl = Optional.ofNullable(getDiscordWebhookUrl())
			.map(String::trim)
			.filter(url -> !url.isEmpty())
			.map(url -> {
				if (!url.startsWith("http://") && !url.startsWith("https://"))
				{
					return "https://" + url;
				}
				return url;
			})
			.orElseGet(() -> {
				CupidBot.log("The webhook URL is not configured in the RuneLite profile. Please check the configuration.");
				return null;
			});

		if (webHookUrl == null)
		{
			return false;
		}

		DiscordPayload payload = new DiscordPayload(bodyMessage, embeds);
		String jsonPayload = GSON.toJson(payload);

		MultipartBody.Builder builder = new MultipartBody.Builder()
			.setType(MultipartBody.FORM)
			.addFormDataPart("payload_json", jsonPayload);

		files.stream()
			.map(File::new)
			.filter(File::exists)
			.forEach(file -> {
				try
				{
					String mimeType = Optional.ofNullable(Files.probeContentType(file.toPath()))
						.orElse("application/octet-stream");
					builder.addFormDataPart(
						"file",
						file.getName(),
						RequestBody.create(MediaType.parse(mimeType), file)
					);
				}
				catch (IOException e)
				{
					CupidBot.log("Failed to determine MIME type for file: " + file.getPath() + " - " + e.getMessage());
				}
			});

		RequestBody requestBody = builder.build();

		try
		{
			Request request = new Request.Builder()
				.url(webHookUrl)
				.post(requestBody)
				.build();

			try (Response response = httpClient.newCall(request).execute())
			{
				if (!response.isSuccessful())
				{
					CupidBot.log("Failed to send Discord notification. Error Code: " + response.code()
						+ " - URL marked as invalid: " + webHookUrl);
				}
				return response.isSuccessful();
			}
		}
		catch (IllegalArgumentException e)
		{
			CupidBot.log("Invalid Discord webhook URL format - URL marked as invalid: " + webHookUrl
				+ " - Error: " + e.getMessage());
			return false;
		}
		catch (IOException e)
		{
			CupidBot.log("Error while sending Discord notification to URL: " + webHookUrl
				+ " - Error: " + e.getMessage());
			return false;
		}
	}

	public static boolean sendWebhookMessage(String bodyMessage, List<DiscordEmbed> embeds)
	{
		return sendWebhookMessage(bodyMessage, embeds, Collections.emptyList());
	}

	public static boolean sendWebhookMessage(String bodyMessage)
	{
		return sendWebhookMessage(bodyMessage, Collections.emptyList(), Collections.emptyList());
	}

	private static String getDiscordWebhookUrl()
	{
		return Optional.ofNullable(LoginManager.getActiveProfile())
			.map(ConfigProfile::getDiscordWebhookUrl)
			.orElse(null);
	}

	public static int convertHexToInt(String hexCode)
	{
		if (hexCode.startsWith("#"))
		{
			hexCode = hexCode.substring(1);
		}

		return Integer.parseInt(hexCode, 16);
	}

	public static int convertColorToInt(Color color)
	{
		return (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
	}

	public static boolean sendCustomNotification(String title, String description, int color, String playerName, String source)
	{
		String timestamp = java.time.LocalDateTime.now()
			.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

		DiscordEmbed embed = new DiscordEmbed();
		embed.setTitle(title);
		embed.setDescription(description);
		embed.setColor(color);
		embed.addField(new DiscordEmbed.Field("Player",
			playerName.isEmpty() ? "Unknown" : playerName, true));
		embed.addField(new DiscordEmbed.Field("Time", timestamp, true));
		embed.addField(new DiscordEmbed.Field("Source", source, true));
		embed.setFooter(new DiscordEmbed.Footer("CupidBot", null));

		return sendWebhookMessage("", Collections.singletonList(embed));
	}

	public static boolean sendNotificationWithFields(String title, String description, int color,
		List<DiscordEmbed.Field> fields, String footerText)
	{
		DiscordEmbed embed = new DiscordEmbed();
		embed.setTitle(title);
		embed.setDescription(description);
		embed.setColor(color);

		if (fields != null)
		{
			for (DiscordEmbed.Field field : fields)
			{
				embed.addField(field);
			}
		}

		embed.setFooter(new DiscordEmbed.Footer(footerText != null ? footerText : "CupidBot", null));

		return sendWebhookMessage("", Collections.singletonList(embed));
	}

	public static boolean sendSimpleNotification(String message, String playerName, String source)
	{
		return sendCustomNotification("Notification", message, 0x3498DB, playerName, source);
	}

	public static boolean sendAlert(String alertType, String message, int color, String playerName, String source)
	{
		String title = alertType.toUpperCase();
		return sendCustomNotification(title, message, color, playerName, source);
	}

	public static DiscordEmbed.Field createField(String name, String value, boolean inline)
	{
		return new DiscordEmbed.Field(name, value, inline);
	}

	public static DiscordEmbed.Field createTimestampField()
	{
		String timestamp = java.time.LocalDateTime.now()
			.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		return createField("Time", timestamp, true);
	}

	public static DiscordEmbed.Field createPlayerField(String playerName)
	{
		return createField("Player", playerName.isEmpty() ? "Unknown" : playerName, true);
	}
}
