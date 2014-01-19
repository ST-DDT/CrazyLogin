package de.st_ddt.crazylogin.crypt;

import org.bukkit.configuration.ConfigurationSection;

import de.st_ddt.crazylogin.LoginPlugin;
import de.st_ddt.crazylogin.data.LoginData;
import de.st_ddt.crazylogin.exceptions.PasswordRejectedException;

public class ChangedAlgorithmEncryptor extends AbstractEncryptor implements UpdatingEncryptor
{

	private final Encryptor current;
	private final Encryptor old;

	public ChangedAlgorithmEncryptor(final LoginPlugin<? extends LoginData> plugin, final ConfigurationSection config)
	{
		super(plugin, config);
		final Encryptor e1 = EncryptHelper.getEncryptor(plugin, config.getConfigurationSection("current"));
		final Encryptor e2 = EncryptHelper.getEncryptor(plugin, config.getConfigurationSection("old"));
		if (e1 == null || e2 == null)
		{
			System.err.println();
			System.err.println("  +-------------------------+");
			System.err.println("  |         WARNING         |");
			System.err.println("  +-------------------------+");
			System.err.println("Something went wrong with your ChangedAlgorithmEncryptor.");
			System.err.println("Couldn't find the requested encryptor!");
			System.err.println("I'll fix it for.");
			if (e1 == null)
			{
				System.err.println("Most passwords may be locked!");
				if (e2 == null)
				{
					System.err.println("Something really bad happened to your encryption.");
					System.err.println("Using default encryption instead.");
					current = new CrazyCrypt1(plugin, (String[]) null);
				}
				else
					current = e2;
			}
			else
			{
				System.err.println("Some passwords may be locked!");
				current = e1;
			}
			old = null;
		}
		else
		{
			current = e1;
			old = e2;
		}
	}

	public ChangedAlgorithmEncryptor(final LoginPlugin<? extends LoginData> plugin, final Encryptor current, final Encryptor old)
	{
		super(plugin, (ConfigurationSection) null);
		if (current == null)
			throw new IllegalArgumentException("Encryptor cannot be null!");
		this.current = current;
		this.old = old;
	}

	@Override
	public String encrypt(final String name, final String salt, final String password) throws PasswordRejectedException
	{
		return current.encrypt(name, salt, password);
	}

	@Override
	public boolean match(final String name, final String password, final String encrypted)
	{
		if (current.match(name, password, encrypted))
			return true;
		else
			return matchOld(name, password, encrypted);
	}

	public boolean matchOld(final String name, final String password, final String encrypted)
	{
		if (old == null)
			return false;
		else
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
		if (old == null)
			current.save(config, path);
		else
		{
			super.save(config, path);
			config.set(path + "type", getClass().getName());
			current.save(config, path + "current.");
			old.save(config, path + "old.");
		}
	}

	@Override
	public boolean equals(final Encryptor obj)
	{
		return getCurrentEncryptor().equals(obj);
	}

	@Override
	public String toString()
	{
		if (old == null)
			return current.toString();
		else
			return "ChangedAlgorithm (" + old + " -> " + current + ")";
	}
}
