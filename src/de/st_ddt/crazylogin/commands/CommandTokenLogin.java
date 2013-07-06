package de.st_ddt.crazylogin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazylogin.data.Token;
import de.st_ddt.crazylogin.listener.PlayerListener;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandCircumstanceException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandExecutorException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandUsageException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.modules.permissions.PermissionModule;
import de.st_ddt.crazyutil.source.Localized;
import de.st_ddt.crazyutil.source.Permission;

public class CommandTokenLogin extends CommandExecutor
{

	private final PlayerListener playerListener;

	public CommandTokenLogin(final CrazyLogin plugin, final PlayerListener playerListener)
	{
		super(plugin);
		this.playerListener = playerListener;
	}

	@Override
	@Localized({ "CRAZYLOGIN.TOKENLOGIN.FAILEDWARN $Player$ $IP$ $TokenCreator$", "CRAZYLOGIN.LOGIN.FAILED", "CRAZYLOGIN.LOGIN.SUCCESS" })
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		if (!(sender instanceof Player))
			throw new CrazyCommandExecutorException(false);
		final Player player = (Player) sender;
		final LoginPlayerData playerData = plugin.getPlayerData(player);
		if (playerData == null)
			throw new CrazyCommandCircumstanceException("when this player is protected by a password!");
		if (args.length != 1)
			throw new CrazyCommandUsageException("<Token>");
		final Token token = plugin.getLoginTokens().remove(player.getName().toLowerCase());
		if (token == null || !token.isValid())
		{
			plugin.getCrazyLogger().log("LoginFail", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " tried to use a token, although there isn't an active one.");
			throw new CrazyCommandCircumstanceException("if token is available!");
		}
		if (!token.checkToken(args[0]))
		{
			final Player creator = Bukkit.getPlayerExact(token.getCreator());
			if (creator != null)
				plugin.sendLocaleMessage("TOKENLOGIN.FAILEDWARN", creator, player.getName(), player.getAddress().getAddress().getHostAddress(), token.getCreator());
			plugin.sendLocaleMessage("TOKENLOGIN.FAILEDWARN", Bukkit.getConsoleSender(), player.getName(), player.getAddress().getAddress().getHostAddress(), token.getCreator());
			plugin.sendLocaleMessage("LOGIN.FAILED", player);
			plugin.getCrazyLogger().log("LoginFail", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " entered a wrong token. (Created by " + token.getCreator() + ")");
			return;
		}
		playerData.setLoggedIn(true);
		plugin.sendLocaleMessage("LOGIN.SUCCESS", player);
		plugin.getCrazyLogger().log("Login", player.getName() + " (via Token " + token.getCreator() + ") logged in successfully.");
		playerListener.removeMovementBlocker(player);
		playerListener.disableSaveLogin(player);
		playerListener.disableHidenInventory(player);
		plugin.getPlayerAutoLogouts().add(player);
	}

	@Override
	@Permission("crazylogin.blocktokenlogin")
	public boolean hasAccessPermission(final CommandSender sender)
	{
		return !PermissionModule.hasPermission(sender, "crazylogin.blocktokenlogin") && !plugin.isTokenLoginDisabled();
	}
}
