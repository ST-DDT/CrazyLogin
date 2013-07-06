package de.st_ddt.crazylogin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandExecutorException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandUsageException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.modules.permissions.PermissionModule;
import de.st_ddt.crazyutil.source.Permission;

public class CommandLoginWithAutoLogout extends CommandExecutor
{

	public CommandLoginWithAutoLogout(final CrazyLogin plugin)
	{
		super(plugin);
	}

	@Override
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		if (!(sender instanceof Player))
			throw new CrazyCommandExecutorException(false);
		final Player player = (Player) sender;
		if (args.length == 0)
			throw new CrazyCommandUsageException("<Passwort...>");
		final String password = ChatHelper.listingString(" ", args);
		plugin.playerLogin(player, password);
		plugin.getPlayerAutoLogouts().add(player);
	}

	@Override
	@Permission("crazylogin.login.command")
	public boolean hasAccessPermission(final CommandSender sender)
	{
		return PermissionModule.hasPermission(sender, "crazylogin.login.command");
	}
}
