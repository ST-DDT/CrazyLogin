package de.st_ddt.crazylogin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandExecutorException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandPermissionException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;

public class CrazyLoginCommandPassword extends CrazyLoginCommandExecutor
{

	public CrazyLoginCommandPassword(final CrazyLogin plugin)
	{
		super(plugin);
	}

	@Override
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		if (sender instanceof ConsoleCommandSender)
			throw new CrazyCommandExecutorException(false);
		final Player player = (Player) sender;
		if (!plugin.isLoggedIn(player) && plugin.hasPlayerData(player))
			throw new CrazyCommandPermissionException();
		if (!plugin.hasPlayerData(player))
			if (!player.hasPermission("crazylogin.register.command"))
				throw new CrazyCommandPermissionException();
		plugin.playerPassword(player, ChatHelper.listingString(" ", args));
	}
}
