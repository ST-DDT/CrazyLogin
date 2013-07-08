package de.st_ddt.crazylogin.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazylogin.exceptions.PasswordRejectedException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandAlreadyExistsException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandCircumstanceException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandErrorException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandUsageException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.ChatHelperExtended;
import de.st_ddt.crazyutil.modules.permissions.PermissionModule;
import de.st_ddt.crazyutil.source.Localized;
import de.st_ddt.crazyutil.source.Permission;

public class CommandPlayerCreate extends CommandExecutor
{

	public CommandPlayerCreate(final CrazyLogin plugin)
	{
		super(plugin);
	}

	@Override
	@Localized({ "CRAZYLOGIN.COMMAND.PLAYER.CREATE.SUCCESS $Name$", "CRAZYLOGIN.COMMAND.REGISTER.WARNCONFIRMPASSWORDDISABLED" })
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		if (plugin.getCrazyDatabase() == null)
			throw new CrazyCommandCircumstanceException("when database is accessible");
		if (args.length < (plugin.isConfirmNewPasswordEnabled() ? 3 : 2))
			throw new CrazyCommandUsageException("<Player> <Passwort>" + (plugin.isConfirmNewPasswordEnabled() ? " <Password>" : ""));
		final String name = args[0];
		if (plugin.hasPlayerData(name))
			throw new CrazyCommandAlreadyExistsException("Account", name);
		final LoginPlayerData data = new LoginPlayerData(name);
		final String[] passwordArgs = ChatHelperExtended.shiftArray(args, 1);
		String password = null;
		if (plugin.isConfirmNewPasswordEnabled())
		{
			if (passwordArgs.length % 2 == 1)
				throw new CrazyCommandUsageException("<Player> <Password> <Password>");
			password = ChatHelper.listingString(" ", ChatHelperExtended.cutArray(passwordArgs, passwordArgs.length / 2));
			if (passwordArgs.length > 0)
				if (!password.equals(ChatHelper.listingString(" ", ChatHelperExtended.shiftArray(passwordArgs, passwordArgs.length / 2))))
					throw new CrazyCommandUsageException("<Player> <Password> <Password>");
		}
		else
			password = ChatHelper.listingString(" ", passwordArgs);
		try
		{
			data.setPassword(password);
		}
		catch (final PasswordRejectedException e)
		{
			throw new CrazyCommandErrorException(e);
		}
		catch (final Exception e)
		{
			throw new CrazyCommandErrorException(e);
		}
		if (data.isOnline())
			plugin.getMessageListener().sendPluginMessage(data.getPlayer(), "Q_StorePW " + password);
		plugin.sendLocaleMessage("COMMAND.PLAYER.CREATE.SUCCESS", sender, name);
		plugin.getCrazyDatabase().save(data);
		plugin.getCrazyLogger().log("Account", data.getName() + " registered successfully (via " + sender.getName() + ").");
		if (!plugin.isConfirmNewPasswordEnabled())
			if (passwordArgs.length % 2 == 0)
				if (ChatHelper.listingString(" ", ChatHelperExtended.cutArray(passwordArgs, passwordArgs.length / 2)).equals(ChatHelper.listingString(" ", ChatHelperExtended.shiftArray(passwordArgs, passwordArgs.length / 2))))
					plugin.sendLocaleMessage("COMMAND.REGISTER.WARNCONFIRMPASSWORDDISABLED", sender);
	}

	@Override
	public List<String> tab(final CommandSender sender, final String[] args)
	{
		if (args.length != 1)
			return null;
		final List<String> res = new ArrayList<String>();
		final Pattern pattern = Pattern.compile(args[0], Pattern.CASE_INSENSITIVE);
		for (final OfflinePlayer player : Bukkit.getOfflinePlayers())
			if (pattern.matcher(player.getName()).find())
				if (!plugin.hasPlayerData(player))
					res.add(player.getName());
		return res;
	}

	@Override
	@Permission("crazylogin.player.create")
	public boolean hasAccessPermission(final CommandSender sender)
	{
		return PermissionModule.hasPermission(sender, "crazylogin.player.create");
	}
}
