package de.st_ddt.crazylogin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandExecutorException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandPermissionException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandUsageException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.ChatHelperExtended;
import de.st_ddt.crazyutil.modules.permissions.PermissionModule;
import de.st_ddt.crazyutil.source.Localized;

public class CommandPassword extends CommandExecutor
{

	public CommandPassword(final CrazyLogin plugin)
	{
		super(plugin);
	}

	@Override
	@Localized("CRAZYLOGIN.COMMAND.REGISTER.WARNCONFIRMPASSWORDDISABLED")
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		if (!(sender instanceof Player))
			throw new CrazyCommandExecutorException(false);
		final Player player = (Player) sender;
		if (!plugin.isLoggedIn(player) && plugin.hasPlayerData(player))
			throw new CrazyCommandPermissionException();
		if (!plugin.hasPlayerData(player))
			if (!PermissionModule.hasPermission(player, "crazylogin.register.command"))
				throw new CrazyCommandPermissionException();
		String password = null;
		if (plugin.isConfirmPasswordEnabled())
		{
			if (args.length % 2 == 1)
				throw new CrazyCommandUsageException("<Password> <Password>");
			password = ChatHelper.listingString(" ", ChatHelperExtended.cutArray(args, args.length / 2));
			if (!password.equals(ChatHelper.listingString(" ", ChatHelperExtended.shiftArray(args, args.length / 2))))
				throw new CrazyCommandUsageException("<Password> <Password>");
		}
		else
			password = ChatHelper.listingString(" ", args);
		plugin.playerPassword(player, password);
		if (!plugin.isConfirmPasswordEnabled())
			if (args.length % 2 == 0)
				if (ChatHelper.listingString(" ", ChatHelperExtended.cutArray(args, args.length / 2)).equals(ChatHelper.listingString(" ", ChatHelperExtended.shiftArray(args, args.length / 2))))
					plugin.sendLocaleMessage("COMMAND.REGISTER.WARNCONFIRMPASSWORDDISABLED", player);
	}
}
