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
import org.bukkit.configuration.file.FileConfiguration;
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
import de.st_ddt.crazyutil.Pair;
import de.st_ddt.crazyutil.PairList;
import de.st_ddt.crazyutil.databases.Database;
import de.st_ddt.crazyutil.databases.MySQLConnection;

public class CrazyLogin extends CrazyPlugin implements LoginPlugin
{

	private static CrazyLogin plugin;
	private final HashMap<String, Date> antiRequestSpamTable = new HashMap<String, Date>();
	protected final PairList<String, LoginPlayerData> datas = new PairList<String, LoginPlayerData>();
	private CrazyLoginPlayerListener playerListener;
	private CrazyLoginVehicleListener vehicleListener;
	private CrazyLoginMessageListener messageListener;
	protected boolean alwaysNeedPassword;
	protected int autoLogout;
	protected int autoKick;
	protected int autoKickUnregistered;
	protected List<String> commandWhiteList;
	protected boolean autoKickCommandUsers;
	protected String uniqueIDKey;
	protected boolean doNotSpamRequests;
	protected boolean forceSingleSession;
	protected Encryptor encryptor;
	protected int autoDelete;
	protected int maxRegistrationsPerIP;
	protected boolean pluginCommunicationEnabled;
	protected int moveRange;
	// Database
	protected String saveType;
	protected String tableName;
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
		final PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(playerListener, this);
		pm.registerEvents(vehicleListener, this);
		this.messageListener = new CrazyLoginMessageListener(this);
		final Messenger ms = getServer().getMessenger();
		ms.registerIncomingPluginChannel(this, "CrazyLogin", messageListener);
		ms.registerOutgoingPluginChannel(this, "CrazyLogin");
	}

	@Override
	public void load()
	{
		super.load();
		FileConfiguration config = getConfig();
		if (config.getBoolean("autoLogout", false))
			autoLogout = 0;
		else
			autoLogout = config.getInt("autoLogout", 60 * 60);
		alwaysNeedPassword = config.getBoolean("alwaysNeedPassword", true);
		autoKick = Math.max(config.getInt("autoKick", -1), -1);
		autoKickUnregistered = Math.max(config.getInt("kickUnregistered", -1), -1);
		doNotSpamRequests = config.getBoolean("doNotSpamRequests", false);
		commandWhiteList = config.getStringList("commandWhitelist");
		autoKickCommandUsers = config.getBoolean("autoKickCommandUsers", false);
		forceSingleSession = config.getBoolean("forceSingleSession", true);
		maxRegistrationsPerIP = config.getInt("maxRegistrationsPerIP", 3);
		autoDelete = Math.max(config.getInt("autoDelete", -1), -1);
		moveRange = config.getInt("moveRange", 5);
		if (autoDelete != -1)
			getServer().getScheduler().scheduleAsyncRepeatingTask(this, new DropInactiveAccountsTask(this), 20 * 60 * 60, 20 * 60 * 60 * 6);
		if (commandWhiteList.size() == 0)
		{
			commandWhiteList.add("/login");
			commandWhiteList.add("/register");
			commandWhiteList.add("/crazylogin password");
		}
		uniqueIDKey = config.getString("uniqueIDKey");
		pluginCommunicationEnabled = config.getBoolean("pluginCommunicationEnabled", false);
		String algorithm = config.getString("algorithm", "CrazyCrypt1");
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
			String encryption = config.getString("customEncryptor.class");
			encryptor = ObjectSaveLoadHelper.load(encryption, CustomEncryptor.class, new Class[0], new Object[0]);
		}
		else
		{
			try
			{
				encryptor = new DefaultCrypt(algorithm);
			}
			catch (NoSuchAlgorithmException e)
			{
				broadcastLocaleMessage(true, "crazylogin.warnalgorithm", "ALGORITHM.MISSING", algorithm);
				encryptor = new CrazyCrypt1();
			}
		}
		setupDatabase();
		datas.clear();
		if (database != null)
			for (LoginPlayerData data : database.getAllEntries())
				datas.setDataVia1(data.getName().toLowerCase(), data);
		dropInactiveAccounts();
	}

	public void setupDatabase()
	{
		FileConfiguration config = getConfig();
		saveType = config.getString("database.saveType", "flat").toLowerCase();
		tableName = config.getString("database.tableName", "players");
		// Columns
		String colName = config.getString("database.columns.name", "name");
		config.set("database.columns.name", colName);
		String colPassword = config.getString("database.columns.password", "password");
		config.set("database.columns.password", colPassword);
		String colIPs = config.getString("database.columns.ips", "ips");
		config.set("database.columns.ips", colIPs);
		String colLastAction = config.getString("database.columns.lastAction", "lastAction");
		config.set("database.columns.lastAction", colLastAction);
		try
		{
			if (saveType.equals("config"))
			{
				database = new CrazyLoginConfigurationDatabase(config, tableName, colName, colPassword, colIPs, colLastAction);
			}
			else if (saveType.equals("mysql"))
			{
				String host = config.getString("database.host", "localhost");
				config.set("database.host", host);
				String port = config.getString("database.port", "3306");
				config.set("database.port", port);
				String databasename = config.getString("database.dbname", "Crazy");
				config.set("database.dbname", databasename);
				String user = config.getString("database.user", "root");
				config.set("database.user", user);
				String password = config.getString("database.password", "");
				config.set("database.password", password);
				MySQLConnection connection = new MySQLConnection(host, port, databasename, user, password);
				database = new CrazyLoginMySQLDatabase(connection, tableName, colName, colPassword, colIPs, colLastAction);
			}
			else if (saveType.equals("flat"))
			{
				File file = new File(getDataFolder().getPath() + "/" + tableName + ".db");
				database = new CrazyLoginFlatDatabase(file, colName, colPassword, colIPs, colLastAction);
			}
		}
		catch (Exception e)
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
		FileConfiguration config = getConfig();
		config.set("database.saveType", saveType);
		config.set("database.tableName", tableName);
		dropInactiveAccounts();
		if (database != null)
			database.saveAll(datas.getData2List());
		saveConfiguration();
		super.save();
	}

	public int dropInactiveAccounts()
	{
		if (autoDelete != -1)
			return dropInactiveAccounts(autoDelete);
		return -1;
	}

	protected int dropInactiveAccounts(int age)
	{
		Date compare = new Date();
		compare.setTime(compare.getTime() - age * 1000 * 60 * 60 * 24);
		return dropInactiveAccounts(compare);
	}

	protected int dropInactiveAccounts(Date limit)
	{
		int amount = 0;
		Iterator<Pair<String, LoginPlayerData>> it = datas.iterator();
		while (it.hasNext())
		{
			LoginPlayerData data = it.next().getData2();
			if (data.getLastActionTime().before(limit))
			{
				amount++;
				it.remove();
				if (database != null)
					database.delete(data.getName());
			}
		}
		return amount;
	}

	public void saveConfiguration()
	{
		ConfigurationSection config = getConfig();
		config.set("alwaysNeedPassword", alwaysNeedPassword);
		config.set("autoLogout", autoLogout);
		config.set("autoKick", autoKick);
		config.set("kickUnregistered", autoKickUnregistered);
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
		Player player = (Player) sender;
		String password = ChatHelper.listToString(args);
		LoginPlayerData data = datas.findDataVia1(player.getName().toLowerCase());
		CrazyLoginPreLoginEvent event = new CrazyLoginPreLoginEvent(this, player, data);
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
			sendLocaleMessage("LOGIN.FAILED", player);
			broadcastLocaleMessage(true, "crazylogin.warnloginfailure", "LOGIN.FAILEDWARN", player.getName(), player.getAddress().getAddress().getHostAddress());
			return;
		}
		getServer().getPluginManager().callEvent(new CrazyLoginLoginEvent(this, data, player));
		sendLocaleMessage("LOGIN.SUCCESS", player);
		data.addIP(player.getAddress().getAddress().getHostAddress());
		if (database != null)
			database.save(data);
	}

	private void commandLogout(final CommandSender sender, final String[] args) throws CrazyCommandException
	{
		if (sender instanceof ConsoleCommandSender)
			throw new CrazyCommandExecutorException(false);
		Player player = (Player) sender;
		if (!isLoggedIn(player))
			throw new CrazyCommandPermissionException();
		LoginPlayerData data = datas.findDataVia1(player.getName().toLowerCase());
		if (data != null)
		{
			playerListener.notifyLogout(player);
			data.logout();
		}
		player.kickPlayer(locale.getLanguageEntry("LOGOUT.SUCCESS").getLanguageText(player));
		if (database != null)
			database.save(data);
	}

	@Override
	public boolean commandMain(final CommandSender sender, final String commandLabel, final String[] args) throws CrazyException
	{
		if (commandLabel.equalsIgnoreCase("password"))
		{
			commandMainPassword(sender, args);
			return true;
		}
		if (commandLabel.equalsIgnoreCase("admin"))
		{
			commandMainAdmin(sender, args);
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
		return false;
	}

	private void commandMainPassword(final CommandSender sender, final String[] args) throws CrazyCommandException
	{
		if (sender instanceof ConsoleCommandSender)
			throw new CrazyCommandExecutorException(false);
		Player player = (Player) sender;
		if (!isLoggedIn(player) && hasAccount(player))
			throw new CrazyCommandPermissionException();
		if (args.length == 0)
		{
			if (alwaysNeedPassword)
				throw new CrazyCommandUsageException("/crazylogin password <Passwort...>");
			datas.removeDataVia1(player.getName().toLowerCase());
			sendLocaleMessage("PASSWORDDELETE.SUCCESS", sender);
			if (database != null)
				database.delete(player.getName());
			return;
		}
		LoginPlayerData data = datas.findDataVia1(player.getName().toLowerCase());
		if (data == null)
		{
			if (!sender.hasPermission("crazylogin.register"))
				throw new CrazyCommandPermissionException();
			String ip = player.getAddress().getAddress().getHostAddress();
			int registrations = getRegistrationsPerIP(ip).size();
			if (registrations >= maxRegistrationsPerIP)
				throw new CrazyCommandExceedingLimitsException("Max Registrations per IP", maxRegistrationsPerIP);
			CrazyLoginPreRegisterEvent event = new CrazyLoginPreRegisterEvent(this, player, data);
			getServer().getPluginManager().callEvent(event);
			if (event.isCancelled())
				throw new CrazyCommandPermissionException();
			data = new LoginPlayerData(player);
			datas.setDataVia1(player.getName().toLowerCase(), data);
		}
		String password = ChatHelper.listToString(args);
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
		if (sender instanceof Player)
		{
			Player player = (Player) sender;
			if (!isLoggedIn(player))
				throw new CrazyCommandPermissionException();
		}
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
				datas.removeDataVia1(target.getName().toLowerCase());
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
		LoginPlayerData data = datas.findDataVia1(target.getName().toLowerCase());
		if (data == null)
			throw new CrazyCommandNoSuchException("Player", args[0]);
		String password = ChatHelper.listToString(ChatHelper.shiftArray(args, 1));
		data.setPassword(password);
		sendLocaleMessage("PASSWORDCHANGE.SUCCESS", sender);
		if (database != null)
			database.save(data);
	}

	private void commandMainDelete(CommandSender sender, String[] args) throws CrazyCommandException
	{
		if (sender instanceof Player)
		{
			Player player = (Player) sender;
			if (!isLoggedIn(player))
				throw new CrazyCommandPermissionException();
		}
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
		catch (NumberFormatException e)
		{
			throw new CrazyCommandParameterException(0, "Integer");
		}
		if (days < 0)
			return;
		String password = ChatHelper.listToString(ChatHelper.shiftArray(args, 1), " ");
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
		int amount = dropInactiveAccounts(days);
		broadcastLocaleMessage(true, "crazylogin.warndelete", "ACCOUNTS.DELETED", sender.getName(), days, amount);
	}

	private void commandMainMode(final CommandSender sender, final String[] args) throws CrazyCommandException
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
					catch (NumberFormatException e)
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
					catch (NumberFormatException e)
					{
						throw new CrazyCommandParameterException(1, "Integer", "-1 = disabled", "Time in Seconds > 60");
					}
					autoKick = time;
					sendLocaleMessage("MODE.CHANGE", sender, "autoKick", autoKick == -1 ? "disabled" : autoKick + " seconds");
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
					catch (NumberFormatException e)
					{
						throw new CrazyCommandParameterException(1, "Integer", "-1 = disabled", "Time in Seconds");
					}
					autoKickUnregistered = Math.max(time, -1);
					sendLocaleMessage("MODE.CHANGE", sender, "autoKickUnregistered", autoKickUnregistered == -1 ? "disabled" : autoKickUnregistered + " seconds");
					saveConfiguration();
					return;
				}
				else if (args[0].equalsIgnoreCase("saveType"))
				{
					String newValue = args[1];
					boolean changed = saveType.equals(newValue);
					if (newValue.equalsIgnoreCase("config"))
						saveType = "config";
					else if (newValue.equalsIgnoreCase("mysql"))
						saveType = "mysql";
					else if (newValue.equalsIgnoreCase("flat"))
						saveType = "flat";
					else
						throw new CrazyCommandNoSuchException("SaveType", newValue);
					sendLocaleMessage("MODE.CHANGE", sender, "saveType", saveType);
					if (changed)
						return;
					getConfig().set("database.saveType", saveType);
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
					catch (NumberFormatException e)
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
					catch (NumberFormatException e)
					{
						throw new CrazyCommandParameterException(1, "Integer");
					}
					moveRange = Math.max(range, 0);
					sendLocaleMessage("MODE.CHANGE", sender, "moveRange", moveRange + " blocks");
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
					sendLocaleMessage("MODE.CHANGE", sender, "saveType", saveType);
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
				throw new CrazyCommandNoSuchException("Mode", args[0]);
			default:
				throw new CrazyCommandUsageException("/crazylogin mode <Mode> <Value>");
		}
	}

	public boolean isLoggedIn(final Player player)
	{
		LoginPlayerData data = datas.findDataVia1(player.getName().toLowerCase());
		if (data == null)
			return !alwaysNeedPassword;
		return data.isOnline() && player.isOnline();
	}

	public boolean hasAccount(final OfflinePlayer player)
	{
		return (datas.findDataVia1(player.getName().toLowerCase()) != null);
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
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
		return uniqueIDKey;
	}

	public Encryptor getEncryptor()
	{
		return encryptor;
	}

	public int getAutoDelete()
	{
		return autoDelete;
	}

	public void setAutoDelete(int autoDelete)
	{
		this.autoDelete = autoDelete;
	}

	public PairList<String, LoginPlayerData> getPlayerData()
	{
		return datas;
	}

	public LoginPlayerData getPlayerData(String name)
	{
		return datas.findDataVia1(name);
	}

	public LoginPlayerData getPlayerData(OfflinePlayer player)
	{
		return getPlayerData(player.getName().toLowerCase());
	}

	public void requestLogin(Player player)
	{
		if (doNotSpamRequests)
			return;
		Date now = new Date();
		Date date = antiRequestSpamTable.get(player.getName());
		if (date != null)
			if (date.after(now))
				return;
		now.setTime(now.getTime() + 5000L);
		antiRequestSpamTable.put(player.getName(), now);
		if (datas.findDataVia1(player.getName().toLowerCase()) == null)
			sendLocaleMessage("REGISTER.REQUEST", player);
		else
			sendLocaleMessage("LOGIN.REQUEST", player);
	}

	public boolean isForceSingleSessionEnabled()
	{
		return forceSingleSession;
	}

	public List<LoginPlayerData> getRegistrationsPerIP(String ip)
	{
		List<LoginPlayerData> list = new LinkedList<LoginPlayerData>();
		for (LoginPlayerData data : datas.getData2List())
			if (data.hasIP(ip))
				list.add(data);
		return list;
	}

	public int getMoveRange()
	{
		return moveRange;
	}
}
