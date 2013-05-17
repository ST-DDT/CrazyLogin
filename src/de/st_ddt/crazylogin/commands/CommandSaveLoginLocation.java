package de.st_ddt.crazylogin.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandNoSuchException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandUsageException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatConverter;
import de.st_ddt.crazyutil.ChatHelperExtended;
import de.st_ddt.crazyutil.modules.permissions.PermissionModule;
import de.st_ddt.crazyutil.paramitrisable.WorldParamitrisable;
import de.st_ddt.crazyutil.source.Localized;

public class CommandSaveLoginLocation extends CommandExecutor
{

	public CommandSaveLoginLocation(final CrazyLogin plugin)
	{
		super(plugin);
	}

	@Override
	@Localized("CRAZYLOGIN.COMMAND.SAVELOGINLOCATION $World$ $TargetWorld$ $TargetX$ $TargetY$ $TargetZ$ $TargetYaw$ $TargetPitch$")
	public void command(final CommandSender sender, final String[] args) throws CrazyException
	{
		if (args.length == 0)
			throw new CrazyCommandUsageException("<World/*> [Location]");
		final World world;
		if (args[0].equals("*"))
			world = null;
		else
		{
			world = Bukkit.getWorld(args[0]);
			if (world == null)
				throw new CrazyCommandNoSuchException("World", args[0]);
		}
		if (args.length == 1)
		{
			if (world == null)
				for (final World w : Bukkit.getWorlds())
				{
					final Location location = plugin.getSaveLoginLocation(w);
					plugin.sendLocaleMessage("COMMAND.SAVELOGINLOCATION", sender, w.getName(), location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
				}
			else
			{
				final Location location = plugin.getSaveLoginLocation(world);
				plugin.sendLocaleMessage("COMMAND.SAVELOGINLOCATION", sender, world.getName(), location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
			}
		}
		else
		{
			final Location location;
			if (args.length == 2)
			{
				final World w = Bukkit.getWorld(args[1]);
				if (w == null)
					throw new CrazyCommandNoSuchException("World", args[1], WorldParamitrisable.tabHelp(args[1]));
				location = plugin.getSaveLoginLocation(w);
			}
			else
				try
				{
					location = ChatConverter.stringToLocation(null, world, ChatHelperExtended.shiftArray(args, 1));
				}
				catch (final CrazyCommandException e)
				{
					e.shiftCommandIndex();
					throw e;
				}
			if (location.getWorld() == null)
				throw new CrazyCommandUsageException("<World/*> <World> [<X> <Y> <Z>]");
			if (world == null)
			{
				for (final World w : Bukkit.getWorlds())
					plugin.getSaveLoginLocations().put(w.getName(), location);
				plugin.sendLocaleMessage("COMMAND.SAVELOGINLOCATION", sender, "*ALL*", location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
			}
			else
			{
				plugin.getSaveLoginLocations().put(world.getName(), location);
				plugin.sendLocaleMessage("COMMAND.SAVELOGINLOCATION", sender, world.getName(), location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
			}
		}
	}

	@Override
	public List<String> tab(final CommandSender sender, final String[] args)
	{
		if (args.length == 1)
			return WorldParamitrisable.tabHelp(args[0]);
		else if (args.length == 2)
			return WorldParamitrisable.tabHelp(args[0]);
		else
			return new ArrayList<String>(0);
	}

	@Override
	public boolean hasAccessPermission(final CommandSender sender)
	{
		return PermissionModule.hasPermission(sender, "crazylogin.saveloginlocation");
	}
}
