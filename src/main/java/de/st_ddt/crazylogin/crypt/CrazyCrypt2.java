package de.st_ddt.crazylogin.crypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bukkit.configuration.ConfigurationSection;

import de.st_ddt.crazylogin.LoginPlugin;
import de.st_ddt.crazylogin.data.LoginData;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandErrorException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;

public final class CrazyCrypt2 extends AbstractEncryptor
{

	private final MessageDigest md;

	public CrazyCrypt2(final LoginPlugin<? extends LoginData> plugin, final ConfigurationSection config) throws NoSuchAlgorithmException
	{
		super(plugin, config);
		md = MessageDigest.getInstance("SHA-512");
	}

	public CrazyCrypt2(final LoginPlugin<? extends LoginData> plugin, final String[] args) throws CrazyException
	{
		super(plugin, args);
		try
		{
			md = MessageDigest.getInstance("SHA-512");
		}
		catch (final NoSuchAlgorithmException e)
		{
			throw new CrazyCommandErrorException(e);
		}
	}

	@Override
	public String encrypt(final String name, final String salt, final String password)
	{
		final String[] split = (name + "Glyphensuppe").split(name.substring(0, 1));
		final int length = split.length;
		for (int i = 0; i < length; i += 2)
			split[i] = split[i].toLowerCase();
		for (int i = 0; i < length; i += 3)
			split[i] = split[i].toUpperCase();
		final String name2 = ChatHelper.listingString(name.substring(0, 1), split);
		final int start = Math.max(Math.min(length, name.length() - 5), 0);
		final String salt2 = (name2 + salt).substring(start, 7);
		final String text = "/" + name2 + "\"+\\" + password.toLowerCase() + "78.12§)%&" + password + salt + "[HaveATry]" + name + salt2 + "Ü+'äÖ_:;68(";
		md.update(text.getBytes(charset), 0, text.length());
		final byte[] hash = md.digest();
		md.reset();
		final String hashedPassword = EncryptHelper.byteArrayToHexString(hash);
		final int pwLength = password.length() % 50;
		return hashedPassword.substring(pwLength, 128 - length) + salt.substring(0, Math.min(length, 5)) + hashedPassword.substring(0, pwLength) + salt.substring(Math.min(length, 5)) + hashedPassword.substring(128 - length);
	}

	@Override
	public String getAlgorithm()
	{
		return "CrazyCrypt2";
	}

	@Override
	public boolean match(final String name, final String password, final String encrypted)
	{
		final String[] split = (name + "Glyphensuppe").split(name.substring(0, 1));
		final int length = split.length;
		final int pwLength = password.length() % 50;
		final int a = 128 - length - pwLength;
		final int b = 128 - length + Math.min(length, 5);
		final String salt = encrypted.substring(a, a + Math.min(length, 5)) + encrypted.substring(b, b + 9 - Math.min(length, 5));
		try
		{
			final String enc = encrypt(name, salt, password);
			return encrypted.equals(enc);
		}
		catch (final Exception e)
		{
			return false;
		}
	}
}
