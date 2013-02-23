package de.st_ddt.crazylogin.commands;

import org.bukkit.command.CommandSender;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazylogin.data.Token;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandCircumstanceException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.modules.permissions.PermissionModule;
import de.st_ddt.crazyutil.source.Localized;

public class CommandMainGenerateToken extends CommandExecutor
{

	public CommandMainGenerateToken(final CrazyLogin plugin)
	{
		super(plugin);
	}

	@Override
	@Localized("CRAZYLOGIN.COMMAND.GENERATEDTOKEN $Player$ $Token$")
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		final String name = ChatHelper.listingString(" ", args);
		final LoginPlayerData data = plugin.getPlayerData(name);
		if (data == null)
			throw new CrazyCommandCircumstanceException("when this player is protected by a password!");
		if (data.isLoggedIn())
			throw new CrazyCommandCircumstanceException("when players isn't logged in!");
		final Token token = new Token(sender.getName());
		plugin.getLoginTokens().put(name.toLowerCase(), token);
		plugin.sendLocaleMessage("COMMAND.GENERATEDTOKEN", sender, name, token.getToken());
	}

	@Override
	public boolean hasAccessPermission(final CommandSender sender)
	{
		return PermissionModule.hasPermission(sender, "crazylogin.generatetoken") && !plugin.isTokenLoginDisabled();
	}
}
