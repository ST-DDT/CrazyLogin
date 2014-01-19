package de.st_ddt.crazylogin.commands;

import org.bukkit.command.CommandSender;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazylogin.data.Token;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandCircumstanceException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.source.Localized;
import de.st_ddt.crazyutil.source.Permission;

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
		final LoginPlayerData data = owner.getPlayerData(name);
		if (data == null)
			throw new CrazyCommandCircumstanceException("when this player is protected by a password!");
		if (data.isLoggedIn())
			throw new CrazyCommandCircumstanceException("when players isn't logged in!");
		final Token token = new Token(sender.getName());
		owner.getLoginTokens().put(name.toLowerCase(), token);
		owner.sendLocaleMessage("COMMAND.GENERATEDTOKEN", sender, name, token.getToken());
	}

	@Override
	@Permission("crazylogin.generatetoken")
	public boolean hasAccessPermission(final CommandSender sender)
	{
		return sender.hasPermission("crazylogin.generatetoken") && !owner.isTokenLoginDisabled();
	}
}
