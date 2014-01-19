package de.st_ddt.crazylogin.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import de.st_ddt.crazycore.CrazyCore;
import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazylogin.exceptions.PasswordRejectedException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandErrorException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandNoSuchException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandUsageException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.ChatHelperExtended;
import de.st_ddt.crazyutil.paramitrisable.PlayerDataParamitrisable;
import de.st_ddt.crazyutil.source.Localized;
import de.st_ddt.crazyutil.source.Permission;

public class CommandPlayerPassword extends CommandExecutor
{

	public CommandPlayerPassword(final CrazyLogin plugin)
	{
		super(plugin);
	}

	@Override
	@Permission("crazylogin.player.password.protected")
	@Localized({ "CRAZYLOGIN.COMMAND.PLAYER.PASSWORD.SUCCESS $Name$", "CRAZYLOGIN.COMMAND.REGISTER.WARNCONFIRMPASSWORDDISABLED" })
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		if (args.length < (owner.isConfirmNewPasswordEnabled() ? 3 : 2))
			throw new CrazyCommandUsageException("<Player> <Password>" + (owner.isConfirmNewPasswordEnabled() ? " <Password>" : ""));
		final LoginPlayerData data = owner.getPlayerData(args[0]);
		if (data == null)
			throw new CrazyCommandNoSuchException("Player (with Account)", args[0]);
		CrazyCore.getPlugin().checkProtectedPlayer(data.getName(), sender, "crazylogin.player.password.protected", owner.getName(), "change player's password");
		final String[] passwordArgs = ChatHelperExtended.shiftArray(args, 1);
		final String password;
		if (owner.isConfirmNewPasswordEnabled())
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
			throw e;
		}
		catch (final Exception e)
		{
			throw new CrazyCommandErrorException(e);
		}
		if (data.isOnline())
			owner.getMessageListener().sendPluginMessage(data.getPlayer(), "Q_StorePW " + password);
		owner.sendLocaleMessage("COMMAND.PLAYER.PASSWORD.SUCCESS", sender, data.getName());
		owner.getCrazyDatabase().save(data);
		owner.getCrazyLogger().log("Account", data.getName() + " changed his password successfully (via " + sender.getName() + ").");
		if (!owner.isConfirmNewPasswordEnabled())
			if (passwordArgs.length % 2 == 0)
				if (ChatHelper.listingString(" ", ChatHelperExtended.cutArray(passwordArgs, passwordArgs.length / 2)).equals(ChatHelper.listingString(" ", ChatHelperExtended.shiftArray(passwordArgs, passwordArgs.length / 2))))
					owner.sendLocaleMessage("COMMAND.REGISTER.WARNCONFIRMPASSWORDDISABLED", sender);
	}

	@Override
	public List<String> tab(final CommandSender sender, final String[] args)
	{
		if (args.length != 1)
			return null;
		return PlayerDataParamitrisable.tabHelp(owner, args[0]);
	}

	@Override
	@Permission("crazylogin.player.password")
	public boolean hasAccessPermission(final CommandSender sender)
	{
		return sender.hasPermission("crazylogin.player.password");
	}
}
