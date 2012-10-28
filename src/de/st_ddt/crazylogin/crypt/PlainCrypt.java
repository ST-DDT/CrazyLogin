package de.st_ddt.crazylogin.crypt;

import org.bukkit.configuration.ConfigurationSection;

import de.st_ddt.crazylogin.LoginPlugin;
import de.st_ddt.crazylogin.data.LoginData;

public final class PlainCrypt extends AbstractEncryptor
{

	public PlainCrypt(final LoginPlugin<? extends LoginData> plugin, final ConfigurationSection config)
	{
		super(plugin, config);
	}

	public PlainCrypt(final LoginPlugin<? extends LoginData> plugin, final String[] args)
	{
		super(plugin, args);
	}

	@Override
	public String encrypt(final String name, final String salt, final String password)
	{
		return password;
	}

	@Override
	public boolean match(final String name, final String password, final String encrypted)
	{
		return encrypted.equals(password);
	}

	@Override
	public String getAlgorithm()
	{
		return "Plaintext";
	}
}
