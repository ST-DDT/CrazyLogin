package de.st_ddt.crazylogin.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandNoSuchException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandUsageException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelperExtended;
import de.st_ddt.crazyutil.modules.permissions.PermissionModule;
import de.st_ddt.crazyutil.paramitrisable.PlayerDataParamitrisable;
import de.st_ddt.crazyutil.source.Localized;
import de.st_ddt.crazyutil.source.Permission;

public class CommandPlayerDetachIP extends CommandExecutor
{

	public CommandPlayerDetachIP(final CrazyLogin plugin)
	{
		super(plugin);
	}

	@Override
	@Localized({ "CRAZYLOGIN.COMMAND.DETACHIP.SUCCESS $Name$ $IP$", "CRAZYLOGIN.COMMAND.DETACHIP.FAIL $Name$ $IP$" })
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		if (args.length < 2)
			throw new CrazyCommandUsageException("<Player> <IP> [IP] ...");
		final LoginPlayerData data = plugin.getPlayerData(args[0]);
		if (data == null)
			throw new CrazyCommandNoSuchException("Player (with Account)", args[0]);
		for (final String ip : ChatHelperExtended.shiftArray(args, 1))
			if (data.getIPs().remove(ip))
				plugin.sendLocaleMessage("COMMAND.PLAYER.DETACHIP.SUCCESS", sender, data.getName(), ip);
			else
				plugin.sendLocaleMessage("COMMAND.PLAYER.DETACHIP.FAIL", sender, data.getName(), ip);
		plugin.getCrazyDatabase().saveWithoutPassword(data);
	}

	@Override
	public List<String> tab(final CommandSender sender, final String[] args)
	{
		if (args.length == 0)
			return null;
		else if (args.length == 1)
			return PlayerDataParamitrisable.tabHelp(plugin, args[0]);
		else
		{
			final LoginPlayerData data = plugin.getPlayerData(args[0]);
			if (data == null)
				return null;
			final List<String> res = new ArrayList<String>();
			final Pattern pattern = Pattern.compile(args[args.length - 1], Pattern.CASE_INSENSITIVE);
			for (final String ip : data.getIPs())
				if (pattern.matcher(ip).find())
					res.add(ip);
			return res;
		}
	}

	@Override
	@Permission("crazylogin.player.detachip")
	public boolean hasAccessPermission(final CommandSender sender)
	{
		return PermissionModule.hasPermission(sender, "crazylogin.player.detachip");
	}
}
