package de.st_ddt.crazylogin.commands;

import org.bukkit.command.CommandSender;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginData;
import de.st_ddt.crazyplugin.commands.CrazyPlayerDataPluginCommandPlayerDelete;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.source.Localized;

public class CommandPlayerDelete extends CrazyPlayerDataPluginCommandPlayerDelete<LoginData>
{

	public CommandPlayerDelete(final CrazyLogin plugin)
	{
		super(plugin);
	}

	@Override
	@Localized("CRAZYLOGIN.COMMAND.PLAYER.DELETE.SUCCESS $Name$")
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		super.command(sender, args);
		plugin.getCrazyLogger().log("Account", ChatHelper.listingString(" ", args) + " deleted his account successfully (via " + sender.getName() + ").");
	}
}
