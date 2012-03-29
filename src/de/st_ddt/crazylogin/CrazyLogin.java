package de.st_ddt.crazylogin;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import de.st_ddt.crazyplugin.CrazyPlugin;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandExecutorException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandNoSuchException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandPermissionException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandUsageException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.Pair;
import de.st_ddt.crazyutil.PairList;

public class CrazyLogin extends CrazyPlugin
{

	protected final PairList<String, PlayerData> datas = new PairList<String, PlayerData>();
	private CrazyLoginPlayerListener playerListener;
	private CrazyLoginVehicleListener vehicleListener;
	protected boolean alwaysNeedPassword;
	protected boolean autoLogout;

	@Override
	public void onEnable()
	{
		registerHooks();
		super.onEnable();
	}

	public void registerHooks()
	{
		this.playerListener = new CrazyLoginPlayerListener(this);
		this.vehicleListener = new CrazyLoginVehicleListener(this);
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(playerListener, this);
		pm.registerEvents(vehicleListener, this);
	}

	@Override
	public boolean Command(CommandSender sender, String commandLabel, String[] args) throws CrazyException
	{
		if (commandLabel.equalsIgnoreCase("login"))
		{
			CommandLogin(sender, args);
			return true;
		}
		if (commandLabel.equalsIgnoreCase("logout"))
		{
			CommandLogout(sender, args);
			return true;
		}
		return false;
	}

	private void CommandLogin(CommandSender sender, String[] args) throws CrazyCommandException
	{
		if (sender instanceof ConsoleCommandSender)
			throw new CrazyCommandExecutorException(false);
		if (args.length == 0)
			throw new CrazyCommandUsageException("/login <Passwort...>");
		Player player = (Player) sender;
		String password = "";
		for (String arg : args)
			password = password + " " + arg;
		PlayerData data = datas.findDataVia1(player.getName().toLowerCase());
		if (data == null)
		{
			sendLocaleMessage("REGISTER.HEADER", player);
			sendLocaleMessage("REGISTER.MESSAGE", player);
			return;
		}
		if (!data.login(password))
		{
			sendLocaleMessage("LOGIN.FAILED", player);
			sendLocaleMessage("LOGIN.FAILEDLOG", getServer().getConsoleSender(), player.getName(), player.getAddress().getAddress().getHostAddress());
			return;
		}
		sendLocaleMessage("LOGIN.SUCCESS", player);
		data.addIP(player.getAddress().getAddress().getHostAddress());
		save();
	}

	private void CommandLogout(CommandSender sender, String[] args) throws CrazyCommandException
	{
		if (sender instanceof ConsoleCommandSender)
			throw new CrazyCommandExecutorException(false);
		Player player = (Player) sender;
		if (!isLoggedIn(player))
			throw new CrazyCommandPermissionException();
		PlayerData data = datas.findDataVia1(player.getName().toLowerCase());
		if (data != null)
			data.logout();
		player.kickPlayer(locale.getLanguageEntry("LOGOUT.SUCCESS").getLanguageText(player));
		save();
	}

	@Override
	public boolean CommandMain(CommandSender sender, String commandLabel, String[] args) throws CrazyException
	{
		if (commandLabel.equalsIgnoreCase("password"))
		{
			CommandMainPassword(sender, args);
			return true;
		}
		if (commandLabel.equalsIgnoreCase("admin"))
		{
			CommandMainAdmin(sender, args);
			return true;
		}
		if (commandLabel.equalsIgnoreCase("mode"))
		{
			CommandMainMode(sender, args);
			return true;
		}
		return false;
	}

	private void CommandMainPassword(CommandSender sender, String[] args) throws CrazyCommandException
	{
		if (sender instanceof ConsoleCommandSender)
			throw new CrazyCommandExecutorException(false);
		if (args.length == 0)
			throw new CrazyCommandUsageException("/crazylogin password <Passwort...>");
		Player player = (Player) sender;
		PlayerData data = datas.findDataVia1(player.getName().toLowerCase());
		if (data == null)
		{
			data = new PlayerData(player);
			datas.setDataVia1(player.getName().toLowerCase(), data);
		}
		else if (!isLoggedIn(player))
			throw new CrazyCommandPermissionException();
		String password = "";
		for (String arg : args)
			password = password + " " + arg;
		data.setPassword(password);
		data.login(password);
		sendLocaleMessage("PASSWORDCHANGE.SUCCESS", player);
		save();
	}

	private void CommandMainAdmin(CommandSender sender, String[] args) throws CrazyCommandException
	{
		if (sender instanceof Player)
		{
			Player player = (Player) sender;
			if (!isLoggedIn(player))
				throw new CrazyCommandPermissionException();
		}
		if (!sender.hasPermission("crazylogin.admin"))
			throw new CrazyCommandPermissionException();
		if (args.length < 2)
			throw new CrazyCommandUsageException("/crazylogin admin <Player> <Passwort...>");
		OfflinePlayer target = getServer().getPlayerExact(args[0]);
		if (target == null)
		{
			target = getServer().getPlayer(args[0]);
			if (target == null)
				target = getServer().getOfflinePlayer(args[0]);
		}
		if (target == null)
			throw new CrazyCommandNoSuchException("Player", args[0]);
		PlayerData data = datas.findDataVia1(target.getName().toLowerCase());
		if (data == null)
			throw new CrazyCommandNoSuchException("Player", args[0]);
		String password = "";
		for (String arg : ChatHelper.shiftArray(args, 1))
			password = password + " " + arg;
		data.setPassword(password);
		sendLocaleMessage("PASSWORDCHANGE.SUCCESS", sender);
		save();
	}

	private void CommandMainMode(CommandSender sender, String[] args) throws CrazyCommandException
	{
		if (sender instanceof Player)
		{
			Player player = (Player) sender;
			if (!isLoggedIn(player))
				throw new CrazyCommandPermissionException();
		}
		if (!sender.hasPermission("crazylogin.mode"))
			throw new CrazyCommandPermissionException();
		switch (args.length)
		{
			case 2:
				if (args[0].equalsIgnoreCase("alwaysNeedPassword"))
				{
					boolean newValue = false;
					if (args[1].equalsIgnoreCase("1") || args[1].equalsIgnoreCase("true"))
						newValue = true;
					alwaysNeedPassword = newValue;
					sendLocaleMessage("MODE.CHANGE", sender, "alwaysNeedPassword", alwaysNeedPassword ? "True" : "False");
					save();
					return;
				}
				throw new CrazyCommandNoSuchException("Mode", args[0]);
			case 1:
				if (args[0].equalsIgnoreCase("alwaysNeedPassword"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "alwaysNeedPassword", alwaysNeedPassword ? "True" : "False");
					return;
				}
				throw new CrazyCommandNoSuchException("Mode", args[0]);
			default:
				throw new CrazyCommandUsageException("/crazylogin mode <Mode> <Value>");
		}
	}

	@Override
	public void load()
	{
		super.load();
		FileConfiguration config = getConfig();
		autoLogout = config.getBoolean("autoLogout", false);
		if (config.getConfigurationSection("players") != null)
			for (String name : config.getConfigurationSection("players").getKeys(false))
			{
				OfflinePlayer player = getServer().getOfflinePlayer(name);
				PlayerData data = new PlayerData(config, "players." + name + ".");
				datas.add(player.getName().toLowerCase(), data);
			}
		alwaysNeedPassword = config.getBoolean("alwaysNeedPassword", true);
	}

	@Override
	public void save()
	{
		FileConfiguration config = getConfig();
		for (Pair<String, PlayerData> pair : datas)
			pair.getData2().save(config, "players." + pair.getData1() + ".");
		config.set("alwaysNeedPassword", alwaysNeedPassword);
		config.set("autoLogout", autoLogout);
		super.save();
	}

	public boolean isLoggedIn(Player player)
	{
		PlayerData data = datas.findDataVia1(player.getName().toLowerCase());
		if (data == null)
			return !alwaysNeedPassword;
		return data.isOnline() && player.isOnline();
	}
	
	

	
	public boolean isAlwaysNeedPassword()
	{
		return alwaysNeedPassword;
	}

	
	public boolean isAutoLogoutEnabled()
	{
		return autoLogout;
	}

	public PairList<String, PlayerData> getPlayerData()
	{
		return datas;
	}
}
