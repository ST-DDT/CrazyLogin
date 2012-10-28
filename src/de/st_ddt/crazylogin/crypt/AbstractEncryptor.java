package de.st_ddt.crazylogin.crypt;

import java.nio.charset.Charset;

import org.bukkit.configuration.ConfigurationSection;

import de.st_ddt.crazylogin.LoginPlugin;
import de.st_ddt.crazylogin.data.LoginData;

public abstract class AbstractEncryptor implements Encryptor
{

	protected final Charset charset = Charset.forName("UTF-8");

	public AbstractEncryptor(final LoginPlugin<? extends LoginData> plugin, final ConfigurationSection config)
	{
		super();
	}

	public AbstractEncryptor(final LoginPlugin<? extends LoginData> plugin, final String[] args)
	{
		super();
	}

	@Override
	public final boolean equals(final Object obj)
	{
		if (obj instanceof Encryptor)
			return equals((Encryptor) obj);
		return false;
	}

	@Override
	public boolean equals(final Encryptor encryptor)
	{
		return getAlgorithm().equals(encryptor.getAlgorithm());
	}

	@Override
	public String toString()
	{
		return getAlgorithm();
	}

	@Override
	public void save(final ConfigurationSection config, final String path)
	{
		config.set(path + "name", getAlgorithm());
	}
}
