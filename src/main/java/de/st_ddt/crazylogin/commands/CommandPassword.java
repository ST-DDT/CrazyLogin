package de.st_ddt.crazylogin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandExecutorException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandPermissionException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandUsageException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.ChatHelperExtended;
import de.st_ddt.crazyutil.source.Localized;
import de.st_ddt.crazyutil.source.Permission;

public class CommandPassword extends CommandExecutor
{

	public CommandPassword(final CrazyLogin plugin)
	{
		super(plugin);
	}

	@Override
	@Permission("crazylogin.register.command")
	@Localized("CRAZYLOGIN.COMMAND.REGISTER.WARNCONFIRMPASSWORDDISABLED")
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		if (!(sender instanceof Player))
			throw new CrazyCommandExecutorException(false);
		final Player player = (Player) sender;
		final LoginPlayerData data = owner.getPlayerData(player);
		final boolean confirmNewPassword = owner.isConfirmNewPasswordEnabled();
		final boolean confirmWithOldPassword = owner.isConfirmWithOldPasswordEnabled() && data != null;
		if (args.length < 1 + (confirmNewPassword ? 1 : 0) + (confirmWithOldPassword ? 1 : 0))
			throw new CrazyCommandUsageException((confirmWithOldPassword ? "<OldPassword> " : "") + "<NewPassword>" + (confirmNewPassword ? " <NewPassword>" : ""));
		if (data == null)
		{
			if (!player.hasPermission("crazylogin.register.command"))
				throw new CrazyCommandPermissionException();
		}
		else if (!data.isLoggedIn())
		{
			owner.sendAuthReminderMessage(player);
			throw new CrazyCommandPermissionException();
		}
		final String[] pwArgs;
		if (confirmWithOldPassword)
		{
			int i = 1;
			while (!data.isPassword(ChatHelper.listingString(" ", ChatHelperExtended.cutArray(args, i))) && i < args.length)
				i++;
			if (i == args.length)
				throw new CrazyCommandUsageException("<OldPassword> <NewPassword>" + (confirmNewPassword ? " <NewPassword>" : ""));
			pwArgs = ChatHelperExtended.shiftArray(args, i);
		}
		else
			pwArgs = args;
		final String password;
		if (confirmNewPassword)
		{
			if (pwArgs.length % 2 == 1)
				throw new CrazyCommandUsageException((confirmWithOldPassword ? "<OldPassword> " : "") + "<NewPassword> <NewPassword>");
			password = ChatHelper.listingString(" ", ChatHelperExtended.cutArray(pwArgs, pwArgs.length / 2));
			if (!password.equals(ChatHelper.listingString(" ", ChatHelperExtended.shiftArray(pwArgs, pwArgs.length / 2))))
				throw new CrazyCommandUsageException((confirmWithOldPassword ? "<OldPassword> " : "") + "<NewPassword> <NewPassword>");
		}
		else
			password = ChatHelper.listingString(" ", pwArgs);
		owner.playerPassword(player, password);
		if (!confirmNewPassword)
			if (pwArgs.length % 2 == 0)
				if (ChatHelper.listingString(" ", ChatHelperExtended.cutArray(pwArgs, pwArgs.length / 2)).equals(ChatHelper.listingString(" ", ChatHelperExtended.shiftArray(pwArgs, pwArgs.length / 2))))
					owner.sendLocaleMessage("COMMAND.REGISTER.WARNCONFIRMPASSWORDDISABLED", player);
	}
}
