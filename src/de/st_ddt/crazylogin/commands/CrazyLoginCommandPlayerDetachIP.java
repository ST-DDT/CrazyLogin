package de.st_ddt.crazylogin.commands;

import org.bukkit.command.CommandSender;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandNoSuchException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandPermissionException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandUsageException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.locales.Localized;

public class CrazyLoginCommandPlayerDetachIP extends CrazyLoginCommandExecutor
{

	public CrazyLoginCommandPlayerDetachIP(final CrazyLogin plugin)
	{
		super(plugin);
	}

	@Override
	@Localized({ "CRAZYLOGIN.COMMAND.DETACHIP.SUCCESS $Name$ $IP$", "CRAZYLOGIN.COMMAND.DETACHIP.FAIL $Name$ $IP$" })
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		if (!sender.hasPermission("crazylogin.player.detachip"))
			throw new CrazyCommandPermissionException();
		if (args.length < 2)
			throw new CrazyCommandUsageException("<Player> <IP> [IP] ...");
		final LoginPlayerData data = plugin.getPlayerData(args[0]);
		if (data == null)
			throw new CrazyCommandNoSuchException("Player (with Account)", args[0]);
		for (final String ip : args)
			if (data.getIPs().remove(ip))
				plugin.sendLocaleMessage("COMMAND.PLAYER.DETACHIP.SUCCESS", sender, data.getName(), ip);
			else
				plugin.sendLocaleMessage("COMMAND.PLAYER.DETACHIP.FAIL", sender, data.getName(), ip);
		plugin.getCrazyDatabase().save(data);
	}
}
