package de.st_ddt.crazylogin.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandNoSuchException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.locales.Localized;
import de.st_ddt.crazyutil.modules.permissions.PermissionModule;
import de.st_ddt.crazyutil.paramitrisable.OfflinePlayerParamitrisable;

public class CommandPlayerDelete extends CommandExecutor
{

	public CommandPlayerDelete(final CrazyLogin plugin)
	{
		super(plugin);
	}

	@Override
	@Localized("CRAZYLOGIN.COMMAND.PLAYER.DELETE.SUCCESS $Name$")
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		final String name = ChatHelper.listingString(" ", args);
		if (!plugin.deletePlayerData(name))
			throw new CrazyCommandNoSuchException("PlayerData", name);
		plugin.sendLocaleMessage("COMMAND.PLAYER.DELETE.SUCCESS", sender, name);
		plugin.getCrazyLogger().log("Account", name + " deleted his account successfully (via " + sender.getName() + ").");
	}

	@Override
	public List<String> tab(final CommandSender sender, final String[] args)
	{
		if (args.length != 1)
			return null;
		return OfflinePlayerParamitrisable.tabHelp(args[0]);
	}

	@Override
	public boolean hasAccessPermission(final CommandSender sender)
	{
		return PermissionModule.hasPermission(sender, "crazylogin.player.delete");
	}
}
