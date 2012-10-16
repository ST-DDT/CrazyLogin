package de.st_ddt.crazylogin.crypt;

import java.security.NoSuchAlgorithmException;

import org.bukkit.configuration.ConfigurationSection;

import de.st_ddt.crazylogin.LoginPlugin;
import de.st_ddt.crazylogin.data.LoginData;
import de.st_ddt.crazyplugin.exceptions.CrazyException;

public class SeededSHA_512Crypt extends SeededDefaultCrypt
{

	public SeededSHA_512Crypt(LoginPlugin<? extends LoginData> plugin, ConfigurationSection config) throws NoSuchAlgorithmException
	{
		super(plugin, config);
	}

	public SeededSHA_512Crypt(LoginPlugin<? extends LoginData> plugin, String[] args) throws CrazyException
	{
		super(plugin, args);
	}

	@Override
	public String getBasisAlgorithm()
	{
		return "SHA-512";
	}
}
