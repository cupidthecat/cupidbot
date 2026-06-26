package net.runelite.client.plugins.cupidbot.util.bank;

import net.runelite.client.config.ConfigProfile;
import net.runelite.client.plugins.cupidbot.util.security.Encryption;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BankPinProfileResolverTest
{
	@Test
	public void decryptsEncryptedFourDigitPin() throws Exception
	{
		Optional<String> pin = BankPinProfileResolver.decryptStoredBankPin(Encryption.encrypt("1234"));

		assertEquals(Optional.of("1234"), pin);
	}

	@Test
	public void acceptsPlainFourDigitPinForImportedProfiles()
	{
		Optional<String> pin = BankPinProfileResolver.decryptStoredBankPin("1234");

		assertEquals(Optional.of("1234"), pin);
	}

	@Test
	public void ignoresPlaceholderPins()
	{
		assertEquals(Optional.empty(), BankPinProfileResolver.decryptStoredBankPin("**bankpin**"));
		assertEquals(Optional.empty(), BankPinProfileResolver.decryptStoredBankPin("**bank pin**"));
	}

	@Test
	public void ignoresMalformedPins()
	{
		assertEquals(Optional.empty(), BankPinProfileResolver.decryptStoredBankPin("abcd"));
		assertEquals(Optional.empty(), BankPinProfileResolver.decryptStoredBankPin("12345"));
	}

	@Test
	public void resolvesDefaultProfilePinWhenActiveProfileHasNoPin() throws Exception
	{
		ConfigProfile active = profile(1, "active", "");
		ConfigProfile defaultForRsProfile = profile(2, "wintertodt", Encryption.encrypt("9876"));
		defaultForRsProfile.setDefaultForRsProfiles(new ArrayList<>(List.of("rsprofile.test")));

		Optional<String> pin = BankPinProfileResolver.resolveBankPin(
			active,
			List.of(active, defaultForRsProfile),
			"rsprofile.test");

		assertEquals(Optional.of("9876"), pin);
	}

	@Test
	public void activeProfilePinWinsOverDefaultProfilePin() throws Exception
	{
		ConfigProfile active = profile(1, "active", Encryption.encrypt("1111"));
		ConfigProfile defaultForRsProfile = profile(2, "wintertodt", Encryption.encrypt("2222"));
		defaultForRsProfile.setDefaultForRsProfiles(new ArrayList<>(List.of("rsprofile.test")));

		Optional<String> pin = BankPinProfileResolver.resolveBankPin(
			active,
			List.of(active, defaultForRsProfile),
			"rsprofile.test");

		assertEquals(Optional.of("1111"), pin);
	}

	@Test
	public void returnsEmptyWhenNoProfileHasUsablePin()
	{
		ConfigProfile active = profile(1, "active", "");
		ConfigProfile defaultForRsProfile = profile(2, "wintertodt", "**bank pin**");
		defaultForRsProfile.setDefaultForRsProfiles(new ArrayList<>(List.of("rsprofile.test")));

		Optional<String> pin = BankPinProfileResolver.resolveBankPin(
			active,
			List.of(active, defaultForRsProfile),
			"rsprofile.test");

		assertTrue(pin.isEmpty());
	}

	private static ConfigProfile profile(long id, String name, String bankPin)
	{
		try
		{
			Constructor<ConfigProfile> constructor = ConfigProfile.class.getDeclaredConstructor(long.class);
			constructor.setAccessible(true);
			ConfigProfile profile = constructor.newInstance(id);
			profile.setName(name);
			profile.setBankPin(bankPin);
			return profile;
		}
		catch (ReflectiveOperationException ex)
		{
			throw new AssertionError("Unable to create test profile", ex);
		}
	}
}
