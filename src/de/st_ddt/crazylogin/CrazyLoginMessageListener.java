package de.st_ddt.crazylogin;

import java.nio.charset.Charset;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import de.st_ddt.crazylogin.crypt.CrazyCrypt1;
import de.st_ddt.crazyplugin.exceptions.CrazyException;

public class CrazyLoginMessageListener implements PluginMessageListener
{

	protected final CrazyLogin plugin;
	protected final Charset charset = Charset.forName("UTF-8");
	protected final CrazyCrypt1 encryptor = new CrazyCrypt1();

	public CrazyLoginMessageListener(final CrazyLogin plugin)
	{
		super();
		this.plugin = plugin;
	}

	@Override
	public void onPluginMessageReceived(final String channel, final Player player, final byte[] bytes)
	{
		if (!channel.equals("CrazyLogin"))
			return;
		String message = new String(bytes, charset);
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
			catch (CrazyException e)
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
			catch (CrazyException e)
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
			catch (CrazyException e)
			{
				e.print(player, plugin.getChatHeader());
			}
			return;
		}
	}

	protected void sendPluginMessage(final Player player, final String message)
	{
		player.sendPluginMessage(plugin, "CrazyLogin", message.getBytes(charset));
	}

	protected String encryptID(final Player player)
	{
		try
		{
			return encryptor.encrypt(player.getName() + " " + player.getAddress().getAddress().getHostAddress(), null, plugin.getUniqueIDKey());
		}
		catch (Exception e)
		{
			return "";
		}
	}
}
