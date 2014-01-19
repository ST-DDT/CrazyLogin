package de.st_ddt.crazylogin.crypt;

import java.util.regex.Pattern;

import org.bukkit.configuration.ConfigurationSection;

import de.st_ddt.crazylogin.LoginPlugin;
import de.st_ddt.crazylogin.data.LoginData;
import de.st_ddt.crazylogin.exceptions.PasswordRejectedException;
import de.st_ddt.crazyutil.ChatHelper;

public final class PlainCrypt extends AbstractEncryptor
{

	private final static String DEFAULTFILTER = "[^|]";
	private final String filter;
	private final Pattern filterpattern;

	public PlainCrypt(final LoginPlugin<? extends LoginData> plugin, final ConfigurationSection config)
	{
		super(plugin, config);
		filter = config.getString("filterChars", DEFAULTFILTER);
		filterpattern = Pattern.compile(filter + "*");
	}

	public PlainCrypt(final LoginPlugin<? extends LoginData> plugin, final String[] args)
	{
		super(plugin, args);
		if (args == null || args.length == 0)
			filter = DEFAULTFILTER;
		else
			filter = ChatHelper.listingString(" ", args);
		filterpattern = Pattern.compile(filter + "*");
	}

	@Override
	public String encrypt(final String name, final String salt, final String password) throws PasswordRejectedException
	{
		if (!filterpattern.matcher(password).matches())
			throw new PasswordRejectedException("The pasword " + password + " is not allowed!");
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
