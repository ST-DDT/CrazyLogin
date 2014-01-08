package de.st_ddt.crazylogin.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazyplugin.commands.CrazyCommandExecutor;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandPermissionException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHeaderProvider;

public class CrazyCommandLoginCheck extends CommandExecutor
{

	private final CrazyCommandExecutor<? extends ChatHeaderProvider> command;

	public CrazyCommandLoginCheck(final CrazyLogin plugin, final CrazyCommandExecutor<? extends ChatHeaderProvider> command)
	{
		super(plugin);
		this.command = command;
	}

	@Override
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		if (sender instanceof Player)
			if (!owner.isLoggedIn((Player) sender))
				throw new CrazyCommandPermissionException();
		command.command(sender, args);
	}

	@Override
	public List<String> tab(final CommandSender sender, final String[] args)
	{
		return command.tab(sender, args);
	}

	@Override
	public boolean hasAccessPermission(final CommandSender sender)
	{
		if (sender instanceof Player)
			if (!owner.isLoggedIn((Player) sender))
				return false;
		return command.hasAccessPermission(sender);
	}

	@Override
	public boolean isAccessible(final CommandSender sender)
	{
		if (sender instanceof Player)
			if (!owner.isLoggedIn((Player) sender))
				return false;
		return command.isAccessible(sender);
	}
}
