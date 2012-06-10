package de.st_ddt.crazylogin;

import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.crypt.CrazyCrypt1;
import de.st_ddt.crazyplugin.CrazyPluginMessageListener;
import de.st_ddt.crazyplugin.exceptions.CrazyException;

public class CrazyLoginMessageListener extends CrazyPluginMessageListener
{

	protected final CrazyLogin plugin;
	protected final CrazyCrypt1 encryptor = new CrazyCrypt1();

	public CrazyLoginMessageListener(final CrazyLogin plugin)
	{
		super(plugin);
		this.plugin = plugin;
	}

	@Override
	public void pluginMessageRecieved(final String channel, final Player player, String message)
	{
		if (message.equals("Q_sAuth"))
		{
			sendPluginMessage(player, "A_sAuth " + encryptID(player));
			return;
		}
		if (message.equals("Q_PlrIP"))
		{
			sendPluginMessage(player, "A_PlrIP " + player.getAddress().getAddress().getHostAddress());
			return;
		}
		if (message.equals("Q_State"))
		{
			sendPluginMessage(player, "A_State " + (plugin.hasAccount(player) ? "1" : "0") + " " + (plugin.isLoggedIn(player) ? "1" : "0"));
			return;
		}
		if (message.startsWith("Q_Login "))
		{
			message = message.substring(8);
			try
			{
				plugin.command(player, "login", message.split(" "));
			}
			catch (final CrazyException e)
			{
				e.print(player, plugin.getChatHeader());
			}
			return;
		}
		if (message.startsWith("Q_ChgPW "))
		{
			message = message.substring(8);
			try
			{
				plugin.commandMain(player, "password", message.split(" "));
			}
			catch (final CrazyException e)
			{
				e.print(player, plugin.getChatHeader());
			}
			return;
		}
		if (message.equals("Q_Logout"))
		{
			try
			{
				plugin.command(player, "logout", null);
			}
			catch (final CrazyException e)
			{
				e.print(player, plugin.getChatHeader());
			}
			return;
		}
	}

	protected String encryptID(final Player player)
	{
		try
		{
			return encryptor.encrypt(player.getName() + " " + player.getAddress().getAddress().getHostAddress(), null, plugin.getUniqueIDKey());
		}
		catch (final Exception e)
		{
			return "";
		}
	}
}
