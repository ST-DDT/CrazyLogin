package de.st_ddt.crazylogin.crypt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.TreeMap;

import org.bukkit.configuration.ConfigurationSection;

import de.st_ddt.crazylogin.LoginPlugin;
import de.st_ddt.crazylogin.data.LoginData;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandUsageException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.ChatHelperExtended;
import de.st_ddt.crazyutil.paramitrisable.Paramitrisable;
import de.st_ddt.crazyutil.paramitrisable.StringParamitrisable;

public class WebCrypt extends AbstractEncryptor
{

	protected final String encryptURL;
	protected final String matchURL;
	protected final String idKey;

	public WebCrypt(final LoginPlugin<? extends LoginData> plugin, final ConfigurationSection config) throws NoSuchAlgorithmException
	{
		super(plugin, config);
		this.idKey = new SHA_512Crypt(plugin, config).encrypt(null, null, plugin.getUniqueIDKey());
		this.encryptURL = config.getString("encryptURL");
		this.matchURL = config.getString("matchURL");
	}

	public WebCrypt(final LoginPlugin<? extends LoginData> plugin, final String[] args) throws CrazyException
	{
		super(plugin, args);
		this.idKey = new SHA_512Crypt(plugin, args).encrypt(null, null, plugin.getUniqueIDKey());
		final TreeMap<String, Paramitrisable> params = new TreeMap<String, Paramitrisable>();
		final StringParamitrisable encrypt = new StringParamitrisable(null);
		params.put("encrypt", encrypt);
		params.put("encrypturl", encrypt);
		final StringParamitrisable match = new StringParamitrisable(null);
		params.put("match", match);
		params.put("matchurl", match);
		ChatHelperExtended.readParameters(args, params);
		this.encryptURL = encrypt.getValue();
		if (encryptURL == null)
			throw new CrazyCommandUsageException("<encrypturl:URL> [matchurl:URL]");
		this.matchURL = match.getValue();
	}

	@Override
	public String encrypt(final String name, final String salt, final String password)
	{
		try (InputStream stream = new URL(ChatHelper.putArgs(encryptURL, name, password, salt, idKey)).openStream();)
		{
			if (stream == null)
				return null;
			try (InputStreamReader inreader = new InputStreamReader(stream);
					BufferedReader reader = new BufferedReader(inreader);)
			{
				return reader.readLine();
			}
		}
		catch (final IOException e)
		{
			return null;
		}
	}

	@Override
	public boolean match(final String name, final String password, final String encrypted)
	{
		if (matchURL == null)
			return encrypted.equals(encrypt(name, null, password));
		try (InputStream stream = new URL(ChatHelper.putArgs(matchURL, name, password, encrypted, idKey)).openStream())
		{
			if (stream == null)
				return false;
			try (InputStreamReader inreader = new InputStreamReader(stream);
					BufferedReader reader = new BufferedReader(inreader);)
			{
				return reader.readLine().equalsIgnoreCase("YES");
			}
		}
		catch (final IOException e)
		{
			return false;
		}
	}

	@Override
	public String getAlgorithm()
	{
		return "WebCrypt";
	}

	@Override
	public void save(final ConfigurationSection config, final String path)
	{
		super.save(config, path);
		config.set(path + "encryptURL", encryptURL);
		config.set(path + "matchURL", matchURL);
	}
}
