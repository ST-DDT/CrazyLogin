package de.st_ddt.crazylogin.crypt;

import org.bukkit.configuration.ConfigurationSection;

import de.st_ddt.crazylogin.LoginPlugin;
import de.st_ddt.crazylogin.data.LoginData;

public class ChangedAlgorithmEncryptor extends AbstractEncryptor implements UpdatingEncryptor
{

	private final Encryptor current;
	private final Encryptor old;

	public ChangedAlgorithmEncryptor(final LoginPlugin<? extends LoginData> plugin, final ConfigurationSection config)
	{
		super(plugin, config);
		current = EncryptHelper.getEncryptor(plugin, config.getConfigurationSection("current"));
		old = EncryptHelper.getEncryptor(plugin, config.getConfigurationSection("old"));
	}

	public ChangedAlgorithmEncryptor(final LoginPlugin<? extends LoginData> plugin, final Encryptor current, final Encryptor old)
	{
		super(plugin, (ConfigurationSection) null);
		this.current = current;
		this.old = old;
	}

	@Override
	public String encrypt(final String name, final String salt, final String password)
	{
		return current.encrypt(name, salt, password);
	}

	@Override
	public boolean match(final String name, final String password, final String encrypted)
	{
		if (current.match(name, password, encrypted))
			return true;
		return old.match(name, password, encrypted);
	}

	public boolean matchOld(final String name, final String password, final String encrypted)
	{
		return old.match(name, password, encrypted);
	}

	@Override
	public String getAlgorithm()
	{
		return "ChangedAlgorithm";
	}

	public Encryptor getCurrentEncryptor()
	{
		return current;
	}

	public Encryptor getOldEncryptor()
	{
		return old;
	}

	@Override
	public void save(final ConfigurationSection config, final String path)
	{
		super.save(config, path);
		current.save(config, path + "current.");
		old.save(config, path + "old.");
	}

	@Override
	public boolean equals(final Encryptor obj)
	{
		return getCurrentEncryptor().equals(obj);
	}

	@Override
	public String toString()
	{
		return "ChangedAlgorithm (" + old.toString() + " -> " + current.toString() + ")";
	}
}
