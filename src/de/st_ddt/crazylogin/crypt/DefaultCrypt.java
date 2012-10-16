package de.st_ddt.crazylogin.crypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bukkit.configuration.ConfigurationSection;

import de.st_ddt.crazylogin.LoginPlugin;
import de.st_ddt.crazylogin.data.LoginData;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandErrorException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;

public abstract class DefaultCrypt extends AbstractEncryptor
{

	private final MessageDigest md;

	public DefaultCrypt(final LoginPlugin<? extends LoginData> plugin, final ConfigurationSection config) throws NoSuchAlgorithmException
	{
		super(plugin, config);
		md = MessageDigest.getInstance(getAlgorithm());
	}

	public DefaultCrypt(final LoginPlugin<? extends LoginData> plugin, final String[] args) throws CrazyException
	{
		super(plugin, args);
		try
		{
			md = MessageDigest.getInstance(getAlgorithm());
		}
		catch (final NoSuchAlgorithmException e)
		{
			throw new CrazyCommandErrorException(e);
		}
	}

	@Override
	public String encrypt(final String name, final String salt, final String password)
	{
		md.update(password.getBytes(charset));
		final byte[] hash = md.digest();
		md.reset();
		return EncryptHelper.byteArrayToHexString(hash);
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
}
