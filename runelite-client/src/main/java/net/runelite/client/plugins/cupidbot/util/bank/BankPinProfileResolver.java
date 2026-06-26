package net.runelite.client.plugins.cupidbot.util.bank;

import net.runelite.client.config.ConfigProfile;
import net.runelite.client.plugins.cupidbot.util.security.Encryption;

import java.util.List;
import java.util.Optional;

final class BankPinProfileResolver
{
	private static final int PIN_LENGTH = 4;
	private static final int MAX_DECRYPT_ATTEMPTS = 3;

	private BankPinProfileResolver()
	{
	}

	static Optional<String> resolveBankPin(ConfigProfile activeProfile, List<ConfigProfile> profiles, String rsProfileKey)
	{
		Optional<String> activePin = bankPinFromProfile(activeProfile);
		if (activePin.isPresent())
		{
			return activePin;
		}

		if (profiles == null || rsProfileKey == null || rsProfileKey.isBlank())
		{
			return Optional.empty();
		}

		return profiles.stream()
			.filter(BankPinProfileResolver::isUserProfile)
			.filter(profile ->
			{
				List<String> defaultForRsProfiles = profile.getDefaultForRsProfiles();
				return defaultForRsProfiles != null && defaultForRsProfiles.contains(rsProfileKey);
			})
			.map(BankPinProfileResolver::bankPinFromProfile)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.findFirst();
	}

	static Optional<String> decryptStoredBankPin(String storedBankPin)
	{
		String candidate = normalize(storedBankPin);
		if (candidate == null || isPlaceholder(candidate))
		{
			return Optional.empty();
		}

		if (isUsablePin(candidate))
		{
			return Optional.of(candidate);
		}

		for (int i = 0; i < MAX_DECRYPT_ATTEMPTS; i++)
		{
			try
			{
				String decrypted = normalize(Encryption.decrypt(candidate));
				if (decrypted == null || isPlaceholder(decrypted))
				{
					return Optional.empty();
				}
				if (isUsablePin(decrypted))
				{
					return Optional.of(decrypted);
				}
				if (decrypted.equals(candidate))
				{
					return Optional.empty();
				}
				candidate = decrypted;
			}
			catch (Exception ex)
			{
				return Optional.empty();
			}
		}

		return Optional.empty();
	}

	private static Optional<String> bankPinFromProfile(ConfigProfile profile)
	{
		if (profile == null)
		{
			return Optional.empty();
		}
		return decryptStoredBankPin(profile.getBankPin());
	}

	private static boolean isUserProfile(ConfigProfile profile)
	{
		if (profile == null || profile.getName() == null)
		{
			return false;
		}
		return !profile.isInternal();
	}

	private static String normalize(String value)
	{
		if (value == null)
		{
			return null;
		}
		return value.trim();
	}

	private static boolean isUsablePin(String value)
	{
		return value != null && value.length() == PIN_LENGTH && value.matches("\\d+");
	}

	private static boolean isPlaceholder(String value)
	{
		return value == null || value.isBlank() || value.replaceAll("\\s+", "").equalsIgnoreCase("**bankpin**");
	}
}
