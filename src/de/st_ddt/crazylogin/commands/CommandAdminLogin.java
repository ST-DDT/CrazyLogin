package de.st_ddt.crazylogin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazylogin.listener.PlayerListener;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandCircumstanceException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandExecutorException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandPermissionException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandUsageException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.ChatHelperExtended;
import de.st_ddt.crazyutil.modules.permissions.PermissionModule;
import de.st_ddt.crazyutil.source.Localized;
import de.st_ddt.crazyutil.source.Permission;

public class CommandAdminLogin extends CommandExecutor
{

	private final PlayerListener playerListener;

	public CommandAdminLogin(final CrazyLogin plugin, final PlayerListener playerListener)
	{
		super(plugin);
		this.playerListener = playerListener;
	}

	@Override
	@Permission("crazylogin.adminlogin")
	@Localized({ "CRAZYLOGIN.ADMINLOGIN.FAILEDWARN $Player$ $IP$", "CRAZYLOGIN.LOGIN.FAILED", "CRAZYLOGIN.LOGIN.SUCCESS" })
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		if (!(sender instanceof Player))
			throw new CrazyCommandExecutorException(false);
		final Player player = (Player) sender;
		final LoginPlayerData playerData = plugin.getPlayerData(player);
		if (playerData == null)
			throw new CrazyCommandCircumstanceException("when this player is protected by a password!");
		if (args.length < 2)
			throw new CrazyCommandUsageException("<Admin> <AdminPassword...>");
		final LoginPlayerData adminData = plugin.getPlayerData(args[0]);
		if (adminData == null)
		{
			plugin.getCrazyLogger().log("LoginFail", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " tried to use the adminlogin, but defined an invalid admin. (No account)");
			throw new CrazyCommandCircumstanceException("while " + args[0] + " is online and logged in!");
		}
		final Player admin = adminData.getPlayer();
		if (admin == null || !admin.isOnline() || !adminData.isLoggedIn())
		{
			plugin.getCrazyLogger().log("LoginFail", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " tried to use the adminlogin, but defined an invalid admin. (Not online)");
			throw new CrazyCommandCircumstanceException("while " + adminData.getName() + " is online and logged in!");
		}
		if (!admin.hasPermission("crazylogin.adminlogin"))
		{
			plugin.getCrazyLogger().log("LoginFail", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " tried to use the adminlogin, but defined an invalid admin. (Permission)");
			throw new CrazyCommandPermissionException();
		}
		final String password = ChatHelper.listingString(" ", ChatHelperExtended.shiftArray(args, 1));
		if (!adminData.isPassword(password))
		{
			plugin.sendLocaleMessage("ADMINLOGIN.FAILEDWARN", admin, player.getName(), player.getAddress().getAddress().getHostAddress());
			plugin.sendLocaleMessage("ADMINLOGIN.FAILEDWARN", Bukkit.getConsoleSender(), player.getName(), player.getAddress().getAddress().getHostAddress());
			plugin.sendLocaleMessage("LOGIN.FAILED", player);
			plugin.getCrazyLogger().log("LoginFail", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " entered a wrong adminpassword.");
			return;
		}
		playerData.setLoggedIn(true);
		plugin.sendLocaleMessage("LOGIN.SUCCESS", player);
		plugin.getCrazyLogger().log("Login", player.getName() + " (via Admin " + admin.getName() + ") logged in successfully.");
		playerListener.removeMovementBlocker(player);
		playerListener.disableSaveLogin(player);
		playerListener.disableHidenInventory(player);
		plugin.getPlayerAutoLogouts().add(player);
	}

	@Override
	@Permission("crazylogin.blockadminlogin")
	public boolean hasAccessPermission(final CommandSender sender)
	{
		return !PermissionModule.hasPermission(sender, "crazylogin.blockadminlogin") && !plugin.isAdminLoginDisabled();
	}
}
