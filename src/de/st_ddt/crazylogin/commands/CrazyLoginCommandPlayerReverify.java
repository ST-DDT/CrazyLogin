package de.st_ddt.crazylogin.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandNoSuchException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandUsageException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.locales.Localized;
import de.st_ddt.crazyutil.modules.permissions.PermissionModule;
import de.st_ddt.crazyutil.paramitrisable.PlayerDataParamitrisable;

public class CrazyLoginCommandPlayerReverify extends CrazyLoginCommandExecutor
{

	public CrazyLoginCommandPlayerReverify(final CrazyLogin plugin)
	{
		super(plugin);
	}

	@Override
	@Localized("CRAZYLOGIN.COMMAND.REVERIFY.SUCCESS $Name$")
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		if (args.length != 1)
			throw new CrazyCommandUsageException("<Player>");
		final String arg = args[0];
		if (arg.equals("*"))
		{
			for (final LoginPlayerData data : plugin.getPlayerData())
				data.setOnline(false);
			plugin.sendLocaleMessage("COMMAND.PLAYER.REVERIFY.SUCCESS", sender, arg);
		}
		else
		{
			final LoginPlayerData data = plugin.getPlayerData(arg);
			if (data == null)
				throw new CrazyCommandNoSuchException("Player (with Account)", arg);
			data.setOnline(false);
			plugin.sendLocaleMessage("COMMAND.PLAYER.REVERIFY.SUCCESS", sender, data.getName());
		}
	}

	@Override
	public List<String> tab(final CommandSender sender, final String[] args)
	{
		if (args.length != 1)
			return null;
		else
			return PlayerDataParamitrisable.tabHelp(plugin, args[0]);
	}

	@Override
	public boolean hasAccessPermission(final CommandSender sender)
	{
		return PermissionModule.hasPermission(sender, "crazylogin.player.reverify");
	}
}
