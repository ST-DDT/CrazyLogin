package de.st_ddt.crazylogin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandParameterException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandPermissionException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandUsageException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.ChatHelperExtended;
import de.st_ddt.crazyutil.locales.Localized;

public class CrazyLoginCommandMainDropOldData extends CrazyLoginCommandExecutor
{

	public CrazyLoginCommandMainDropOldData(final CrazyLogin plugin)
	{
		super(plugin);
	}

	@Override
	@Localized({ "CRAZYLOGIN.COMMAND.DROPOLDDATA.DELETEWARN $Name$ $KeptDays$", "CRAZYLOGIN.COMMAND.DROPOLDDATA.DELETED $DropCauser$ $KeptDays$ $DroppedAmount$" })
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		if (sender instanceof Player)
			if (!plugin.isLoggedIn((Player) sender))
				throw new CrazyCommandPermissionException();
		if (!sender.hasPermission("crazylogin.dropolddata"))
		{
			String days = "(-)";
			if (args.length != 0)
				days = args[0];
			plugin.broadcastLocaleMessage(true, "crazylogin.warndelete", "COMMANDS.DROPOLDDATA.DELETEWARN", sender.getName(), days);
			throw new CrazyCommandPermissionException();
		}
		if (args.length < 2)
			if (sender instanceof ConsoleCommandSender)
				throw new CrazyCommandUsageException("<DaysToKeep> CONSOLE_CONFIRM");
			else
				throw new CrazyCommandUsageException("<DaysToKeep> <Password>");
		int days = 0;
		try
		{
			days = Integer.parseInt(args[0]);
		}
		catch (final NumberFormatException e)
		{
			throw new CrazyCommandParameterException(0, "Number (Integer)");
		}
		if (days < 0)
			return;
		final String password = ChatHelper.listingString(" ", ChatHelperExtended.shiftArray(args, 1));
		if (sender instanceof ConsoleCommandSender)
		{
			if (!password.equals("CONSOLE_CONFIRM"))
				throw new CrazyCommandUsageException("<DaysToKeep> CONSOLE_CONFIRM");
		}
		else
		{
			if (!plugin.getPlayerData((Player) sender).isPassword(password))
				throw new CrazyCommandUsageException("<DaysToKeep> <Password>");
		}
		final int amount = plugin.dropInactiveAccounts(days);
		plugin.broadcastLocaleMessage(true, "crazylogin.warndelete", true, "COMMAND.DROPOLDDATA.DELETED", sender.getName(), days, amount);
		return;
	}
}
