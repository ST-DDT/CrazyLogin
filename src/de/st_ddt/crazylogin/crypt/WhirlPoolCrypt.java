package de.st_ddt.crazylogin.crypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bukkit.configuration.ConfigurationSection;

import de.st_ddt.crazylogin.LoginPlugin;
import de.st_ddt.crazylogin.data.LoginData;

public class WhirlPoolCrypt extends AbstractEncryptor
{

	public WhirlPoolCrypt(final LoginPlugin<? extends LoginData> plugin, final ConfigurationSection config)
	{
		super(plugin, config);
	}

	public WhirlPoolCrypt(final LoginPlugin<? extends LoginData> plugin, final String[] args)
	{
		super(plugin, args);
	}

	@Override
	public String encrypt(final String name, final String salt, final String password)
	{
		final String text = salt + password;
		MessageDigest md;
		try
		{
			md = MessageDigest.getInstance("SHA-512");
		}
		catch (final NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			return null;
		}
		byte[] hash;
		md.update(text.getBytes(charset), 0, text.length());
		hash = md.digest();
		String encrypted = EncryptHelper.byteArrayToHexString(hash);
		encrypted = encrypted.substring(0, password.length() - 1) + salt + encrypted.substring(password.length());
		return encrypted;
	}

	@Override
	public boolean match(final String name, final String password, final String encrypted)
	{
		final String salt = encrypted.substring(password.length() - 1, password.length() + 8);
		try
		{
			return encrypted.equals(encrypt(name, salt, password));
		}
		catch (final Exception e)
		{
			return false;
		}
	}

	@Override
	public String getAlgorithm()
	{
		return "Whirlpool";
	}
}
