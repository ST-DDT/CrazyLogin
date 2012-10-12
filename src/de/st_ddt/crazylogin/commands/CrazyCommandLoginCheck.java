package de.st_ddt.crazylogin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazyplugin.CrazyPluginInterface;
import de.st_ddt.crazyplugin.commands.CrazyCommandExecutor;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandPermissionException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;

public class CrazyCommandLoginCheck extends CrazyLoginCommandExecutor
{

	private final CrazyCommandExecutor<? extends CrazyPluginInterface> command;

	public CrazyCommandLoginCheck(final CrazyLogin plugin, final CrazyCommandExecutor<? extends CrazyPluginInterface> command)
	{
		super(plugin);
		this.command = command;
	}

	@Override
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		if (sender instanceof Player)
			if (!plugin.isLoggedIn((Player) sender))
				throw new CrazyCommandPermissionException();
		command.command(sender, args);
	}
}
