package de.st_ddt.crazylogin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandExecutorException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.locales.Localized;

public class CommandAutoLogout extends CommandExecutor
{

	public CommandAutoLogout(final CrazyLogin plugin)
	{
		super(plugin);
	}

	@Override
	@Localized("CRAZYLOGIN.COMMAND.AUTOLOGOUT")
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		if (!(sender instanceof Player))
			throw new CrazyCommandExecutorException(false);
		final Player player = (Player) sender;
		plugin.getPlayerAutoLogouts().add(player);
		plugin.sendLocaleMessage("COMMAND.AUTOLOGOUT", player);
	}
}
