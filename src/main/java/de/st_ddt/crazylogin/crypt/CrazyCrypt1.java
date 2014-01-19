package de.st_ddt.crazylogin.crypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bukkit.configuration.ConfigurationSection;

import de.st_ddt.crazylogin.LoginPlugin;
import de.st_ddt.crazylogin.data.LoginData;

public final class CrazyCrypt1 extends AbstractEncryptor
{

	public CrazyCrypt1(final LoginPlugin<? extends LoginData> plugin, final ConfigurationSection config)
	{
		super(plugin, config);
	}

	public CrazyCrypt1(final LoginPlugin<? extends LoginData> plugin, final String[] args)
	{
		super(plugin, args);
	}

	@Override
	public String encrypt(final String name, final String salt, final String password)
	{
		final String text = "ÜÄaeut//&/=I " + password + "7421€547" + name + "__+IÄIH§%NK " + password;
		try
		{
			final MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(text.getBytes(charset), 0, text.length());
			return EncryptHelper.byteArrayToHexString(md.digest());
		}
		catch (final NoSuchAlgorithmException e)
		{
			return null;
		}
	}

	@Override
	public boolean match(final String name, final String password, final String encrypted)
	{
		try
		{
			return encrypted.equals(encrypt(name, null, password));
		}
		catch (final Exception e)
		{
			return false;
		}
	}

	@Override
	public String getAlgorithm()
	{
		return "CrazyCrypt1";
	}
}
