package de.st_ddt.crazylogin;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.messaging.Messenger;

import de.st_ddt.crazylogin.crypt.AuthMeCrypt;
import de.st_ddt.crazylogin.crypt.CrazyCrypt1;
import de.st_ddt.crazylogin.crypt.CustomEncryptor;
import de.st_ddt.crazylogin.crypt.DefaultCrypt;
import de.st_ddt.crazylogin.crypt.Encryptor;
import de.st_ddt.crazylogin.crypt.PlainCrypt;
import de.st_ddt.crazylogin.crypt.WhirlPoolCrypt;
import de.st_ddt.crazylogin.databases.CrazyLoginConfigurationDatabase;
import de.st_ddt.crazylogin.databases.CrazyLoginFlatDatabase;
import de.st_ddt.crazylogin.databases.CrazyLoginMySQLDatabase;
import de.st_ddt.crazylogin.events.CrazyLoginLoginEvent;
import de.st_ddt.crazylogin.events.CrazyLoginLoginFailEvent;
import de.st_ddt.crazylogin.events.CrazyLoginPasswordEvent;
import de.st_ddt.crazylogin.events.CrazyLoginPreLoginEvent;
import de.st_ddt.crazylogin.events.CrazyLoginPreRegisterEvent;
import de.st_ddt.crazylogin.events.LoginFailReason;
import de.st_ddt.crazylogin.tasks.DropInactiveAccountsTask;
import de.st_ddt.crazyplugin.CrazyPlugin;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandExceedingLimitsException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandExecutorException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandNoSuchException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandParameterException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandPermissionException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandUsageException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.ObjectSaveLoadHelper;
import de.st_ddt.crazyutil.databases.Database;
import de.st_ddt.crazyutil.databases.DatabaseType;
import de.st_ddt.crazyutil.databases.MySQLConnection;

public class CrazyLogin extends CrazyPlugin implements LoginPlugin
{

	private static CrazyLogin plugin;
	private final HashMap<String, Date> antiRequestSpamTable = new HashMap<String, Date>();
	private final HashMap<String, Integer> loginFailures = new HashMap<String, Integer>();
	protected final HashMap<String, LoginPlayerData> datas = new HashMap<String, LoginPlayerData>();
	private CrazyLoginPlayerListener playerListener;
	private CrazyLoginVehicleListener vehicleListener;
	private CrazyLoginCrazyListener crazylistener;
	private CrazyLoginMessageListener messageListener;
	protected boolean alwaysNeedPassword;
	protected int autoLogout;
	protected int autoKick;
	protected boolean autoKickCommandUsers;
	protected int autoKickUnregistered;
	protected int autoKickLoginFailer;
	protected List<String> commandWhiteList;
	protected String uniqueIDKey;
	protected boolean doNotSpamRequests;
	protected boolean forceSingleSession;
	protected Encryptor encryptor;
	protected int autoDelete;
	protected int maxRegistrationsPerIP;
	protected boolean pluginCommunicationEnabled;
	protected int moveRange;
	protected int minNameLength;
	protected int maxNameLength;
	// Database
	protected Database<LoginPlayerData> database;

	public static CrazyLogin getPlugin()
	{
		return plugin;
	}

	@Override
	public void onEnable()
	{
		plugin = this;
		registerHooks();
		super.onEnable();
	}

	public void registerHooks()
	{
		this.playerListener = new CrazyLoginPlayerListener(this);
		this.vehicleListener = new CrazyLoginVehicleListener(this);
		this.crazylistener = new CrazyLoginCrazyListener(this);
		final PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(playerListener, this);
		pm.registerEvents(vehicleListener, this);
		pm.registerEvents(crazylistener, this);
		this.messageListener = new CrazyLoginMessageListener(this);
		final Messenger ms = getServer().getMessenger();
		ms.registerIncomingPluginChannel(this, "CrazyLogin", messageListener);
		ms.registerOutgoingPluginChannel(this, "CrazyLogin");
	}

	@Override
	public void load()
	{
		super.load();
		final ConfigurationSection config = getConfig();
		if (config.getBoolean("autoLogout", false))
			autoLogout = 0;
		else
			autoLogout = config.getInt("autoLogout", 60 * 60);
		alwaysNeedPassword = config.getBoolean("alwaysNeedPassword", true);
		autoKick = Math.max(config.getInt("autoKick", -1), -1);
		autoKickCommandUsers = config.getBoolean("autoKickCommandUsers", false);
		autoKickUnregistered = Math.max(config.getInt("kickUnregistered", -1), -1);
		autoKickLoginFailer = Math.max(config.getInt("autoKickLoginFailer", 3), -1);
		loginFailures.clear();
		doNotSpamRequests = config.getBoolean("doNotSpamRequests", false);
		commandWhiteList = config.getStringList("commandWhitelist");
		if (commandWhiteList.size() == 0)
		{
			commandWhiteList.add("/login");
			commandWhiteList.add("/register");
			commandWhiteList.add("/crazylogin password");
		}
		forceSingleSession = config.getBoolean("forceSingleSession", true);
		maxRegistrationsPerIP = config.getInt("maxRegistrationsPerIP", 3);
		autoDelete = Math.max(config.getInt("autoDelete", -1), -1);
		if (autoDelete != -1)
			getServer().getScheduler().scheduleAsyncRepeatingTask(this, new DropInactiveAccountsTask(this), 20 * 60 * 60, 20 * 60 * 60 * 6);
		moveRange = config.getInt("moveRange", 5);
		minNameLength = Math.min(Math.max(config.getInt("minNameLength", 3), 1), 16);
		maxNameLength = Math.min(Math.max(config.getInt("maxNameLength", 16), minNameLength), 16);
		uniqueIDKey = config.getString("uniqueIDKey");
		pluginCommunicationEnabled = config.getBoolean("pluginCommunicationEnabled", false);
		final String algorithm = config.getString("algorithm", "CrazyCrypt1");
		if (algorithm.equalsIgnoreCase("CrazyCrypt1"))
		{
			encryptor = new CrazyCrypt1();
		}
		else if (algorithm.equalsIgnoreCase("Whirlpool"))
		{
			encryptor = new WhirlPoolCrypt();
		}
		else if (algorithm.equalsIgnoreCase("Plaintext"))
		{
			encryptor = new PlainCrypt();
		}
		else if (algorithm.equalsIgnoreCase("AuthMe"))
		{
			encryptor = new AuthMeCrypt();
		}
		else if (algorithm.equalsIgnoreCase("Custom"))
		{
			final String encryption = config.getString("customEncryptor.class");
			encryptor = ObjectSaveLoadHelper.load(encryption, CustomEncryptor.class, new Class[0], new Object[0]);
		}
		else
		{
			try
			{
				encryptor = new DefaultCrypt(algorithm);
			}
			catch (final NoSuchAlgorithmException e)
			{
				broadcastLocaleMessage(true, "crazylogin.warnalgorithm", "ALGORITHM.MISSING", algorithm);
				encryptor = new CrazyCrypt1();
			}
		}
		setupDatabase();
		datas.clear();
		for (Player player : getServer().getOnlinePlayers())
			requestLogin(player);
		if (database != null)
			for (final LoginPlayerData data : database.getAllEntries())
				datas.put(data.getName().toLowerCase(), data);
		dropInactiveAccounts();
	}

	public void setupDatabase()
	{
		final ConfigurationSection config = getConfig();
		String saveType = config.getString("database.saveType", "FLAT").toUpperCase();
		DatabaseType type = null;
		try
		{
			type = DatabaseType.valueOf(saveType);
		}
		catch (Exception e)
		{
			System.out.println("NO SUCH SAVETYPE " + saveType);
			type = null;
		}
		final String tableName = config.getString("database.tableName", "players");
		config.set("database.tableName", tableName);
		// Columns
		final String colName = config.getString("database.columns.name", "name");
		config.set("database.columns.name", colName);
		final String colPassword = config.getString("database.columns.password", "password");
		config.set("database.columns.password", colPassword);
		final String colIPs = config.getString("database.columns.ips", "ips");
		config.set("database.columns.ips", colIPs);
		final String colLastAction = config.getString("database.columns.lastAction", "lastAction");
		config.set("database.columns.lastAction", colLastAction);
		try
		{
			if (type == DatabaseType.CONFIG)
			{
				database = new CrazyLoginConfigurationDatabase(config, tableName, colName, colPassword, colIPs, colLastAction);
			}
			else if (type == DatabaseType.MYSQL)
			{
				final MySQLConnection connection = new MySQLConnection(config, "localhost", "3306", "Crazy", "root", "");
				database = new CrazyLoginMySQLDatabase(connection, tableName, colName, colPassword, colIPs, colLastAction);
			}
			else if (type == DatabaseType.FLAT)
			{
				final File file = new File(getDataFolder().getPath() + "/" + tableName + ".db");
				database = new CrazyLoginFlatDatabase(file, colName, colPassword, colIPs, colLastAction);
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			database = null;
		}
		finally
		{
			if (database == null)
				broadcastLocaleMessage(true, "crazylogin.warndatabase", "CRAZYLOGIN.DATABASE.ACCESSWARN", saveType);
		}
	}

	@Override
	public void save()
	{
		final ConfigurationSection config = getConfig();
		if (database != null)
			config.set("database.saveType", database.getType().toString());
		dropInactiveAccounts();
		if (database != null)
			database.saveAll(datas.values());
		saveConfiguration();
	}

	public int dropInactiveAccounts()
	{
		if (autoDelete != -1)
			return dropInactiveAccounts(autoDelete);
		return -1;
	}

	protected int dropInactiveAccounts(final long age)
	{
		final Date compare = new Date();
		compare.setTime(compare.getTime() - age * 1000 * 60 * 60 * 24);
		return dropInactiveAccounts(compare);
	}

	protected int dropInactiveAccounts(final Date limit)
	{
		int amount = 0;
		final Iterator<LoginPlayerData> it = datas.values().iterator();
		while (it.hasNext())
		{
			LoginPlayerData data = it.next();
			if (data.getLastActionTime().before(limit))
			{
				amount++;
				datas.remove(data.getName().toLowerCase());
				if (database != null)
					database.delete(data.getName());
			}
		}
		return amount;
	}

	public void saveConfiguration()
	{
		final ConfigurationSection config = getConfig();
		config.set("alwaysNeedPassword", alwaysNeedPassword);
		config.set("autoLogout", autoLogout);
		config.set("autoKick", autoKick);
		config.set("kickUnregistered", autoKickUnregistered);
		config.set("autoKickLoginFailer", autoKickLoginFailer);
		config.set("doNotSpamRequests", doNotSpamRequests);
		config.set("commandWhitelist", commandWhiteList);
		config.set("autoKickCommandUsers", autoKickCommandUsers);
		config.set("uniqueIDKey", uniqueIDKey);
		config.set("algorithm", encryptor.getAlgorithm());
		config.set("autoDelete", autoDelete);
		config.set("forceSingleSession", forceSingleSession);
		config.set("maxRegistrationsPerIP", maxRegistrationsPerIP);
		config.set("pluginCommunicationEnabled", pluginCommunicationEnabled);
		config.set("moveRange", moveRange);
		config.set("minNameLength", minNameLength);
		config.set("maxNameLength", maxNameLength);
		saveConfig();
	}

	@Override
	public boolean command(final CommandSender sender, final String commandLabel, final String[] args) throws CrazyException
	{
		if (commandLabel.equalsIgnoreCase("login"))
		{
			commandLogin(sender, args);
			return true;
		}
		if (commandLabel.equalsIgnoreCase("logout"))
		{
			commandLogout(sender, args);
			return true;
		}
		if (commandLabel.equalsIgnoreCase("register"))
		{
			commandMainPassword(sender, args);
			return true;
		}
		return false;
	}

	private void commandLogin(final CommandSender sender, final String[] args) throws CrazyCommandException
	{
		if (sender instanceof ConsoleCommandSender)
			throw new CrazyCommandExecutorException(false);
		if (args.length == 0)
			throw new CrazyCommandUsageException("/login <Passwort...>");
		final Player player = (Player) sender;
		final String password = ChatHelper.listingString(args);
		final LoginPlayerData data = datas.get(player.getName().toLowerCase());
		final CrazyLoginPreLoginEvent event = new CrazyLoginPreLoginEvent(this, player, data);
		getServer().getPluginManager().callEvent(event);
		if (event.isCancelled())
		{
			getServer().getPluginManager().callEvent(new CrazyLoginLoginFailEvent(this, data, player, LoginFailReason.CANCELLED));
			sendLocaleMessage("LOGIN.FAILED", player);
			return;
		}
		if (data == null)
		{
			getServer().getPluginManager().callEvent(new CrazyLoginLoginFailEvent(this, data, player, LoginFailReason.NO_ACCOUNT));
			sendLocaleMessage("REGISTER.HEADER", player);
			sendLocaleMessage("REGISTER.MESSAGE", player);
			return;
		}
		if (!data.login(password))
		{
			getServer().getPluginManager().callEvent(new CrazyLoginLoginFailEvent(this, data, player, LoginFailReason.WRONG_PASSWORD));
			broadcastLocaleMessage(true, "crazylogin.warnloginfailure", "LOGIN.FAILEDWARN", player.getName(), player.getAddress().getAddress().getHostAddress());
			Integer fails = loginFailures.get(player.getName().toLowerCase());
			if (fails == null)
				fails = 0;
			fails++;
			if (fails >= autoKickLoginFailer)
			{
				player.kickPlayer(locale.getLocaleMessage(player, "LOGIN.FAILED"));
				fails = 0;
			}
			else
				sendLocaleMessage("LOGIN.FAILED", player);
			loginFailures.put(player.getName().toLowerCase(), fails);
			return;
		}
		getServer().getPluginManager().callEvent(new CrazyLoginLoginEvent(this, data, player));
		sendLocaleMessage("LOGIN.SUCCESS", player);
		loginFailures.remove(player.getName().toLowerCase());
		data.addIP(player.getAddress().getAddress().getHostAddress());
		if (database != null)
			database.save(data);
	}

	private void commandLogout(final CommandSender sender, final String[] args) throws CrazyCommandException
	{
		if (sender instanceof ConsoleCommandSender)
			throw new CrazyCommandExecutorException(false);
		final Player player = (Player) sender;
		if (!isLoggedIn(player))
			throw new CrazyCommandPermissionException();
		final LoginPlayerData data = datas.get(player.getName().toLowerCase());
		if (data != null)
		{
			playerListener.notifyLogout(player);
			data.logout();
			if (database != null)
				database.save(data);
		}
		player.kickPlayer(locale.getLanguageEntry("LOGOUT.SUCCESS").getLanguageText(player));
	}

	@Override
	public boolean commandMain(final CommandSender sender, final String commandLabel, final String[] args) throws CrazyException
	{
		if (commandLabel.equalsIgnoreCase("password"))
		{
			commandMainPassword(sender, args);
			return true;
		}
		// Commands which requires login
		if (sender instanceof Player)
		{
			if (!isLoggedIn((Player) sender))
				throw new CrazyCommandPermissionException();
		}
		if (commandLabel.equalsIgnoreCase("admin"))
		{
			commandMainAdmin(sender, args);
			return true;
		}
		if (commandLabel.equalsIgnoreCase("player") || commandLabel.equalsIgnoreCase("playerinfo"))
		{
			commandMainPlayer(sender, args);
			return true;
		}
		if (commandLabel.equalsIgnoreCase("mode"))
		{
			commandMainMode(sender, args);
			return true;
		}
		if (commandLabel.equalsIgnoreCase("delete"))
		{
			commandMainDelete(sender, args);
			return true;
		}
		if (commandLabel.equalsIgnoreCase("commands"))
		{
			commandMainCommands(sender, args);
			return true;
		}
		return false;
	}

	private void commandMainPassword(final CommandSender sender, final String[] args) throws CrazyCommandException
	{
		if (sender instanceof ConsoleCommandSender)
			throw new CrazyCommandExecutorException(false);
		final Player player = (Player) sender;
		if (!isLoggedIn(player) && hasAccount(player))
			throw new CrazyCommandPermissionException();
		if (args.length == 0)
		{
			if (alwaysNeedPassword)
				throw new CrazyCommandUsageException("/crazylogin password <Passwort...>");
			datas.remove(player.getName().toLowerCase());
			sendLocaleMessage("PASSWORDDELETE.SUCCESS", sender);
			if (database != null)
				database.delete(player.getName());
			return;
		}
		LoginPlayerData data = datas.get(player.getName().toLowerCase());
		if (data == null)
		{
			if (!sender.hasPermission("crazylogin.register"))
				throw new CrazyCommandPermissionException();
			final String ip = player.getAddress().getAddress().getHostAddress();
			final int registrations = getRegistrationsPerIP(ip).size();
			if (registrations >= maxRegistrationsPerIP)
				throw new CrazyCommandExceedingLimitsException("Max Registrations per IP", maxRegistrationsPerIP);
			final CrazyLoginPreRegisterEvent event = new CrazyLoginPreRegisterEvent(this, player, data);
			getServer().getPluginManager().callEvent(event);
			if (event.isCancelled())
				throw new CrazyCommandPermissionException();
			data = new LoginPlayerData(player);
			datas.put(player.getName().toLowerCase(), data);
		}
		final String password = ChatHelper.listingString(args);
		if (pluginCommunicationEnabled)
			getServer().getPluginManager().callEvent(new CrazyLoginPasswordEvent(this, player, password));
		data.setPassword(password);
		data.login(password);
		sendLocaleMessage("PASSWORDCHANGE.SUCCESS", player);
		if (database != null)
			database.save(data);
	}

	private void commandMainAdmin(final CommandSender sender, final String[] args) throws CrazyCommandException
	{
		if (!sender.hasPermission("crazylogin.admin"))
			throw new CrazyCommandPermissionException();
		switch (args.length)
		{
			case 0:
				throw new CrazyCommandUsageException("/crazylogin admin <Player> <Passwort...>");
			case 1:
				OfflinePlayer target = getServer().getPlayerExact(args[0]);
				if (target == null)
				{
					target = getServer().getPlayer(args[0]);
					if (target == null)
						target = getServer().getOfflinePlayer(args[0]);
				}
				if (target == null)
					throw new CrazyCommandNoSuchException("Player", args[0]);
				datas.remove(target.getName().toLowerCase());
				sendLocaleMessage("PASSWORDDELETE.SUCCESS", sender);
				if (database != null)
					database.delete(target.getName());
				return;
		}
		OfflinePlayer target = getServer().getPlayerExact(args[0]);
		if (target == null)
		{
			target = getServer().getPlayer(args[0]);
			if (target == null)
				target = getServer().getOfflinePlayer(args[0]);
		}
		if (target == null)
			throw new CrazyCommandNoSuchException("Player", args[0]);
		final LoginPlayerData data = datas.get(target.getName().toLowerCase());
		if (data == null)
			throw new CrazyCommandNoSuchException("Player", args[0]);
		final String password = ChatHelper.listingString(ChatHelper.shiftArray(args, 1));
		data.setPassword(password);
		sendLocaleMessage("PASSWORDCHANGE.SUCCESS", sender);
		if (database != null)
			database.save(data);
	}

	private void commandMainDelete(final CommandSender sender, final String[] args) throws CrazyCommandException
	{
		if (!sender.hasPermission("crazylogin.delete"))
		{
			String days = "(-)";
			if (args.length != 0)
				days = args[0];
			broadcastLocaleMessage(true, "crazylogin.warndelete", "ACCOUNTS.DELETEWARN", sender.getName(), days);
			throw new CrazyCommandPermissionException();
		}
		if (args.length < 2)
			if (sender instanceof ConsoleCommandSender)
				throw new CrazyCommandUsageException("/crazylogin delete <DaysToKeep> CONSOLE_CONFIRM");
			else
				throw new CrazyCommandUsageException("/crazylogin delete <DaysToKeep> <Password>");
		int days = 0;
		try
		{
			days = Integer.parseInt(args[0]);
		}
		catch (final NumberFormatException e)
		{
			throw new CrazyCommandParameterException(0, "Integer");
		}
		if (days < 0)
			return;
		final String password = ChatHelper.listingString(" ", ChatHelper.shiftArray(args, 1));
		if (sender instanceof ConsoleCommandSender)
		{
			if (!password.equals("CONSOLE_CONFIRM"))
				throw new CrazyCommandUsageException("/crazylogin delete <DaysToKeep> CONSOLE_CONFIRM");
		}
		else
		{
			if (!getPlayerData(sender.getName().toLowerCase()).isPassword(password))
				throw new CrazyCommandUsageException("/crazylogin delete <DaysToKeep> <Password>");
		}
		final int amount = dropInactiveAccounts(days);
		broadcastLocaleMessage(true, "crazylogin.warndelete", "ACCOUNTS.DELETED", sender.getName(), days, amount);
	}

	private void commandMainPlayer(CommandSender sender, String[] args) throws CrazyCommandException
	{
		Player target = null;
		switch (args.length)
		{
			case 0:
				if (sender instanceof ConsoleCommandSender)
					throw new CrazyCommandUsageException("/crazylogin player <Player>");
				target = (Player) sender;
				break;
			case 1:
				target = getServer().getPlayer(args[0]);
				if (target == null)
					throw new CrazyCommandNoSuchException("Player", args[0]);
				break;
			default:
				throw new CrazyCommandUsageException("/crazylogin player [Player]");
		}
		if (sender == target)
			if (!sender.hasPermission("crazylogin.playerinfo.self"))
				throw new CrazyCommandPermissionException();
			else if (!sender.hasPermission("crazylogin.playerinfo.other"))
				throw new CrazyCommandPermissionException();
		sendLocaleMessage("PLAYERINFO.HEAD", sender, DateFormat.format(new Date()));
		sendLocaleMessage("PLAYERINFO.USERNAME", sender, target.getName());
		sendLocaleMessage("PLAYERINFO.DISPLAYNAME", sender, target.getDisplayName());
		sendLocaleMessage("PLAYERINFO.IPADDRESS", sender, target.getAddress().getAddress().getHostName());
		sendLocaleMessage("PLAYERINFO.CONNECTION", sender, target.getAddress().getHostName());
		if (sender.hasPermission("crazylogin.playerinfo.extended"))
			sendLocaleMessage("PLAYERINFO.URL", sender, target.getAddress().getAddress().getHostName());
	}

	private void commandMainMode(final CommandSender sender, final String[] args) throws CrazyCommandException
	{
		if (!sender.hasPermission("crazylogin.mode"))
			throw new CrazyCommandPermissionException();
		switch (args.length)
		{
			case 2:
				if (args[0].equalsIgnoreCase("alwaysNeedPassword"))
				{
					boolean newValue = false;
					if (args[1].equalsIgnoreCase("1") || args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("yes"))
						newValue = true;
					alwaysNeedPassword = newValue;
					sendLocaleMessage("MODE.CHANGE", sender, "alwaysNeedPassword", alwaysNeedPassword ? "True" : "False");
					saveConfiguration();
					return;
				}
				else if (args[0].equalsIgnoreCase("forceSingleSession"))
				{
					boolean newValue = false;
					if (args[1].equalsIgnoreCase("1") || args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("yes"))
						newValue = true;
					alwaysNeedPassword = newValue;
					sendLocaleMessage("MODE.CHANGE", sender, "forceSingleSession", forceSingleSession ? "True" : "False");
					saveConfiguration();
					return;
				}
				else if (args[0].equalsIgnoreCase("autoLogout"))
				{
					int newValue = autoLogout;
					try
					{
						newValue = Integer.parseInt(args[1]);
					}
					catch (final NumberFormatException e)
					{
						throw new CrazyCommandParameterException(1, "Integer", "-1 = disabled", "0 = instant", "1... time in seconds");
					}
					autoLogout = Math.max(newValue, -1);
					sendLocaleMessage("MODE.CHANGE", sender, "autoLogout", autoLogout == -1 ? "disabled" : autoLogout + " seconds");
					saveConfiguration();
					return;
				}
				else if (args[0].equalsIgnoreCase("autoKick"))
				{
					int time = autoKick;
					try
					{
						time = Integer.parseInt(args[1]);
					}
					catch (final NumberFormatException e)
					{
						throw new CrazyCommandParameterException(1, "Integer", "-1 = disabled", "Time in Seconds > 60");
					}
					autoKick = time;
					sendLocaleMessage("MODE.CHANGE", sender, "autoKick", autoKick == -1 ? "disabled" : autoKick + " seconds");
					saveConfiguration();
					return;
				}
				else if (args[0].equalsIgnoreCase("autoKickCommandUsers"))
				{
					boolean newValue = false;
					if (args[1].equalsIgnoreCase("1") || args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("yes"))
						newValue = true;
					autoKickCommandUsers = newValue;
					sendLocaleMessage("MODE.CHANGE", sender, "autoKickCommandUsers", autoKickCommandUsers ? "True" : "False");
					saveConfiguration();
					return;
				}
				else if (args[0].equalsIgnoreCase("autoKickUnregistered"))
				{
					int time = autoKickUnregistered;
					try
					{
						time = Integer.parseInt(args[1]);
					}
					catch (final NumberFormatException e)
					{
						throw new CrazyCommandParameterException(1, "Integer", "-1 = disabled", "Time in Seconds");
					}
					autoKickUnregistered = Math.max(time, -1);
					sendLocaleMessage("MODE.CHANGE", sender, "autoKickUnregistered", autoKickUnregistered == -1 ? "disabled" : autoKickUnregistered + " seconds");
					saveConfiguration();
					return;
				}
				else if (args[0].equalsIgnoreCase("autoKickLoginFailer"))
				{
					int tries = autoKickLoginFailer;
					try
					{
						tries = Integer.parseInt(args[1]);
					}
					catch (final NumberFormatException e)
					{
						throw new CrazyCommandParameterException(1, "Integer", "-1 = disabled", "tries");
					}
					autoKickLoginFailer = Math.max(tries, -1);
					sendLocaleMessage("MODE.CHANGE", sender, "autoKickLoginFailer", autoKickLoginFailer == -1 ? "disabled" : autoKickUnregistered + " tries");
					saveConfiguration();
					return;
				}
				else if (args[0].equalsIgnoreCase("saveType"))
				{
					final String saveType = args[1];
					DatabaseType type = null;
					try
					{
						type = DatabaseType.valueOf(saveType.toUpperCase());
					}
					catch (Exception e)
					{
						type = null;
					}
					if (type == null)
						throw new CrazyCommandNoSuchException("SaveType", saveType);
					sendLocaleMessage("MODE.CHANGE", sender, "saveType", saveType);
					if (type == database.getType())
						return;
					getConfig().set("database.saveType", type.toString());
					setupDatabase();
					save();
					return;
				}
				else if (args[0].equalsIgnoreCase("autoDelete"))
				{
					int time = autoDelete;
					try
					{
						time = Integer.parseInt(args[1]);
					}
					catch (final NumberFormatException e)
					{
						throw new CrazyCommandParameterException(1, "Integer", "-1 = disabled", "Time in Days");
					}
					autoDelete = Math.max(time, -1);
					sendLocaleMessage("MODE.CHANGE", sender, "autoDelete", autoDelete == -1 ? "disabled" : autoKick + " days");
					saveConfiguration();
					if (autoDelete != -1)
						getServer().getScheduler().scheduleAsyncRepeatingTask(this, new DropInactiveAccountsTask(this), 20 * 60 * 60, 20 * 60 * 60 * 6);
					return;
				}
				else if (args[0].equalsIgnoreCase("moveRange"))
				{
					int range = moveRange;
					try
					{
						range = Integer.parseInt(args[1]);
					}
					catch (final NumberFormatException e)
					{
						throw new CrazyCommandParameterException(1, "Integer");
					}
					moveRange = Math.max(range, 0);
					sendLocaleMessage("MODE.CHANGE", sender, "moveRange", moveRange + " blocks");
					saveConfiguration();
					return;
				}
				else if (args[0].equalsIgnoreCase("minNameLength"))
				{
					int length = minNameLength;
					try
					{
						length = Integer.parseInt(args[1]);
					}
					catch (final NumberFormatException e)
					{
						throw new CrazyCommandParameterException(1, "Integer");
					}
					minNameLength = Math.min(Math.max(length, 1), 16);
					sendLocaleMessage("MODE.CHANGE", sender, "minNameLength", minNameLength + " characters");
					saveConfiguration();
					return;
				}
				else if (args[0].equalsIgnoreCase("maxNameLength"))
				{
					int length = maxNameLength;
					try
					{
						length = Integer.parseInt(args[1]);
					}
					catch (final NumberFormatException e)
					{
						throw new CrazyCommandParameterException(1, "Integer");
					}
					maxNameLength = Math.min(Math.max(length, 1), 16);
					sendLocaleMessage("MODE.CHANGE", sender, "maxNameLength", maxNameLength + " characters");
					saveConfiguration();
					return;
				}
				throw new CrazyCommandNoSuchException("Mode", args[0]);
			case 1:
				if (args[0].equalsIgnoreCase("alwaysNeedPassword"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "alwaysNeedPassword", alwaysNeedPassword ? "True" : "False");
					return;
				}
				else if (args[0].equalsIgnoreCase("forceSingleSession"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "forceSingleSession", forceSingleSession ? "True" : "False");
					return;
				}
				else if (args[0].equalsIgnoreCase("autoLogout"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "autoLogout", autoLogout);
					return;
				}
				else if (args[0].equalsIgnoreCase("autoKick"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "autoKick", autoKick == -1 ? "disabled" : autoKick + " seconds");
					return;
				}
				else if (args[0].equalsIgnoreCase("autoKickUnregistered"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "autoKickUnregistered", autoKickUnregistered == -1 ? "disabled" : autoKickUnregistered + " seconds");
					return;
				}
				else if (args[0].equalsIgnoreCase("algorithm"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "algorithm", encryptor.getAlgorithm());
					return;
				}
				else if (args[0].equalsIgnoreCase("saveType"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "saveType", database.getType().toString());
					return;
				}
				else if (args[0].equalsIgnoreCase("autoDelete"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "autoDelete", autoDelete == -1 ? "disabled" : autoKick + " days");
					return;
				}
				else if (args[0].equalsIgnoreCase("moveRange"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "moveRange", moveRange + " blocks");
					return;
				}
				else if (args[0].equalsIgnoreCase("minNameLength"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "minNameLength", minNameLength + " characters");
					return;
				}
				else if (args[0].equalsIgnoreCase("maxNameLength"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "maxNameLength", maxNameLength + " characters");
					return;
				}
				throw new CrazyCommandNoSuchException("Mode", args[0]);
			default:
				throw new CrazyCommandUsageException("/crazylogin mode <Mode> [Value]");
		}
	}

	private void commandMainCommands(final CommandSender sender, final String[] args) throws CrazyCommandException
	{
		if (!sender.hasPermission("crazylogin.commands"))
			throw new CrazyCommandPermissionException();
		final int maxPage = (commandWhiteList.size() + 9) / 10;
		switch (args.length)
		{
			case 0:
				sendLocaleMessage("COMMAND.LISTHEAD", sender, 1, maxPage);
				sendLocaleMessage("LIST.SEPERATOR", sender);
				if (commandWhiteList.size() == 0)
					sendLocaleMessage("LIST.EMPTYPAGE", sender);
				else
					for (int i = 0; i < Math.min(10, commandWhiteList.size()); i++)
						sendLocaleMessage("LIST.ENTRY", sender, i + 1, commandWhiteList.get(i));
				return;
			case 1:
				int page = 1;
				try
				{
					page = Integer.parseInt(args[0]);
				}
				catch (final NumberFormatException e)
				{
					throw new CrazyCommandParameterException(1, "Integer");
				}
				sendLocaleMessage("COMMAND.LISTHEAD", sender, 1, (commandWhiteList.size() + 9) / 10);
				sendLocaleMessage("LIST.SEPERATOR", sender);
				if (commandWhiteList.size() <= (page - 1) * 10)
					sendLocaleMessage("LIST.EMPTYPAGE", sender);
				else
					for (int i = (page - 1) * 10; i < Math.min(page * 10, commandWhiteList.size()); i++)
						sendLocaleMessage("LIST.ENTRY", sender, i + 1, commandWhiteList.get(i));
				return;
			default:
				final String[] newArgs = ChatHelper.shiftArray(args, 1);
				final String command = ChatHelper.listingString(" ", newArgs);
				if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("set"))
				{
					if (!commandWhiteList.contains(command))
						commandWhiteList.add(command);
					sendLocaleMessage("COMMAND.ADDED", sender);
					saveConfiguration();
					return;
				}
				else if (args[0].equalsIgnoreCase("del") || args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("rem") || args[0].equalsIgnoreCase("remove"))
				{
					commandWhiteList.remove(command);
					sendLocaleMessage("COMMAND.REMOVED", sender);
					saveConfiguration();
					return;
				}
				throw new CrazyCommandUsageException("/crazylogin command [Page]", "/crazylogin command <add/del> command...");
		}
	}

	@Override
	public boolean isLoggedIn(final Player player)
	{
		final LoginPlayerData data = datas.get(player.getName().toLowerCase());
		if (data == null)
			return !alwaysNeedPassword;
		return data.isOnline() && player.isOnline();
	}

	@Override
	public boolean hasAccount(final OfflinePlayer player)
	{
		return hasAccount(player.getName());
	}

	public boolean hasAccount(final String player)
	{
		return (datas.get(player.toLowerCase()) != null);
	}

	public boolean isAlwaysNeedPassword()
	{
		return alwaysNeedPassword;
	}

	public int getAutoLogoutTime()
	{
		return autoLogout;
	}

	public boolean isAutoLogoutEnabled()
	{
		return autoLogout >= 0;
	}

	public boolean isInstantAutoLogoutEnabled()
	{
		return autoLogout == 0;
	}

	public int getAutoKick()
	{
		return autoKick;
	}

	public int getAutoKickUnregistered()
	{
		return autoKickUnregistered;
	}

	public List<String> getCommandWhiteList()
	{
		return commandWhiteList;
	}

	public boolean isAutoKickCommandUsers()
	{
		return autoKickCommandUsers;
	}

	public String getUniqueIDKey()
	{
		if (uniqueIDKey == null)
			try
			{
				uniqueIDKey = new CrazyCrypt1().encrypt(getServer().getName(), null, "randomKeyGen" + (Math.random() * Integer.MAX_VALUE) + "V:" + getServer().getBukkitVersion() + "'_+'#");
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				return null;
			}
		return uniqueIDKey;
	}

	@Override
	public Encryptor getEncryptor()
	{
		return encryptor;
	}

	public int getAutoDelete()
	{
		return autoDelete;
	}

	public void setAutoDelete(final int autoDelete)
	{
		this.autoDelete = autoDelete;
	}

	public HashMap<String, LoginPlayerData> getPlayerData()
	{
		return datas;
	}

	@Override
	public LoginPlayerData getPlayerData(final OfflinePlayer player)
	{
		return getPlayerData(player.getName());
	}

	public LoginPlayerData getPlayerData(final String name)
	{
		return datas.get(name.toLowerCase());
	}

	public boolean deletePlayerData(final String player)
	{
		LoginPlayerData data = datas.remove(player.toLowerCase());
		if (data == null)
			return false;
		if (database != null)
			database.delete(data.getName());
		return true;
	}

	public void requestLogin(final Player player)
	{
		if (doNotSpamRequests)
			return;
		final Date now = new Date();
		final Date date = antiRequestSpamTable.get(player.getName());
		if (date != null)
			if (date.after(now))
				return;
		now.setTime(now.getTime() + 5000L);
		antiRequestSpamTable.put(player.getName(), now);
		if (datas.get(player.getName().toLowerCase()) == null)
			sendLocaleMessage("REGISTER.REQUEST", player);
		else
			sendLocaleMessage("LOGIN.REQUEST", player);
	}

	public boolean isForceSingleSessionEnabled()
	{
		return forceSingleSession;
	}

	public List<LoginPlayerData> getRegistrationsPerIP(final String ip)
	{
		final List<LoginPlayerData> list = new LinkedList<LoginPlayerData>();
		for (final LoginPlayerData data : datas.values())
			if (data.hasIP(ip))
				list.add(data);
		return list;
	}

	public int getMoveRange()
	{
		return moveRange;
	}

	public int getMinNameLength()
	{
		return minNameLength;
	}

	public int getMaxNameLength()
	{
		return maxNameLength;
	}

	public boolean checkNameLength(final String name)
	{
		final int length = name.length();
		if (length < minNameLength)
			return false;
		if (length > maxNameLength)
			return false;
		return true;
	}
}
