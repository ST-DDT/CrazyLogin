package de.st_ddt.crazylogin.crypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bukkit.configuration.ConfigurationSection;

import de.st_ddt.crazylogin.LoginPlugin;
import de.st_ddt.crazylogin.data.LoginData;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandErrorException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;

public abstract class SeededDefaultCrypt extends AbstractEncryptor
{

	private final MessageDigest md;

	public SeededDefaultCrypt(final LoginPlugin<? extends LoginData> plugin, final ConfigurationSection config) throws NoSuchAlgorithmException
	{
		super(plugin, config);
		md = MessageDigest.getInstance(getBasisAlgorithm());
	}

	public SeededDefaultCrypt(final LoginPlugin<? extends LoginData> plugin, final String[] args) throws CrazyException
	{
		super(plugin, args);
		try
		{
			md = MessageDigest.getInstance(getBasisAlgorithm());
		}
		catch (final NoSuchAlgorithmException e)
		{
			throw new CrazyCommandErrorException(e);
		}
	}

	@Override
	public String encrypt(final String name, final String salt, final String password)
	{
		final String text = salt + password;
		md.update(text.getBytes(charset), 0, text.length());
		final byte[] hash = md.digest();
		md.reset();
		return salt + EncryptHelper.byteArrayToHexString(hash);
	}

	@Override
	public final String getAlgorithm()
	{
		return "Seeded" + getBasisAlgorithm();
	}

	public abstract String getBasisAlgorithm();

	@Override
	public boolean match(final String name, final String password, final String encrypted)
	{
		final String salt = encrypted.substring(0, 9);
		try
		{
			return encrypted.equals(encrypt(name, salt, password));
		}
		catch (final Exception e)
		{
			return false;
		}
	}
}
