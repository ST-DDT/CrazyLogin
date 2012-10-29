package de.st_ddt.crazylogin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazylogin.listener.CrazyLoginPlayerListener;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandCircumstanceException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandExecutorException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandPermissionException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandUsageException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.ChatHelperExtended;
import de.st_ddt.crazyutil.locales.Localized;
import de.st_ddt.crazyutil.modules.permissions.PermissionModule;

public class CrazyLoginCommandAdminLogin extends CrazyLoginCommandExecutor
{

	private final CrazyLoginPlayerListener playerListener;

	public CrazyLoginCommandAdminLogin(final CrazyLogin plugin, final CrazyLoginPlayerListener playerListener)
	{
		super(plugin);
		this.playerListener = playerListener;
	}

	@Override
	@Localized({ "CRAZYLOGIN.ADMINLOGIN.FAILEDWARN $Player$ $IP$", "CRAZYLOGIN.LOGIN.FAILED", "CRAZYLOGIN.LOGIN.SUCCESS" })
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		if (sender instanceof ConsoleCommandSender)
			throw new CrazyCommandExecutorException(false);
		final Player player = (Player) sender;
		final LoginPlayerData playerData = plugin.getPlayerData(player);
		if (playerData == null)
			throw new CrazyCommandCircumstanceException("when this player is protected by a password!");
		if (args.length < 2)
			throw new CrazyCommandUsageException("<Admin> <AdminPassword...>");
		final LoginPlayerData adminData = plugin.getPlayerData(args[0]);
		if (adminData == null)
			throw new CrazyCommandCircumstanceException("when " + args[0] + " is online and logged in!");
		final Player admin = adminData.getPlayer();
		if (admin == null)
			throw new CrazyCommandCircumstanceException("when " + args[0] + " is online and logged in!");
		if (!admin.isOnline() || !adminData.isLoggedIn())
			throw new CrazyCommandCircumstanceException("when " + adminData.getName() + " is online and logged in!");
		if (!admin.hasPermission("crazylogin.adminlogin"))
			throw new CrazyCommandPermissionException();
		final String password = ChatHelper.listingString(" ", ChatHelperExtended.shiftArray(args, 1));
		if (!adminData.isPassword(password))
		{
			plugin.sendLocaleMessage("ADMINLOGIN.FAILEDWARN", admin, player.getName(), player.getAddress().getAddress().getHostAddress());
			plugin.sendLocaleMessage("LOGIN.FAILED", player);
			return;
		}
		playerData.setOnline(true);
		plugin.sendLocaleMessage("LOGIN.SUCCESS", player);
		plugin.getCrazyLogger().log("Login", player.getName() + "(via " + admin.getName() + ") logged in successfully.");
		playerListener.removeFromMovementBlocker(player);
		playerListener.disableSaveLogin(player);
		playerListener.disableHidenInventory(player);
	}

	@Override
	public boolean hasAccessPermission(final CommandSender sender)
	{
		return PermissionModule.hasPermission(sender, "crazylogin.blockadminlogin");
	}
}
