package de.st_ddt.crazylogin;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.bukkit.Bukkit;
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
import de.st_ddt.crazylogin.data.LoginData;
import de.st_ddt.crazylogin.data.LoginDataComparator;
import de.st_ddt.crazylogin.data.LoginDataIPComparator;
import de.st_ddt.crazylogin.data.LoginDataLastActionComparator;
import de.st_ddt.crazylogin.data.LoginDataNameComparator;
import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazylogin.data.LoginUnregisteredPlayerData;
import de.st_ddt.crazylogin.databases.CrazyLoginConfigurationDatabase;
import de.st_ddt.crazylogin.databases.CrazyLoginFlatDatabase;
import de.st_ddt.crazylogin.databases.CrazyLoginMySQLDatabase;
import de.st_ddt.crazylogin.events.CrazyLoginLoginEvent;
import de.st_ddt.crazylogin.events.CrazyLoginLoginFailEvent;
import de.st_ddt.crazylogin.events.CrazyLoginPasswordEvent;
import de.st_ddt.crazylogin.events.CrazyLoginPreLoginEvent;
import de.st_ddt.crazylogin.events.CrazyLoginPreRegisterEvent;
import de.st_ddt.crazylogin.events.LoginFailReason;
import de.st_ddt.crazylogin.listener.CrazyLoginCrazyListener;
import de.st_ddt.crazylogin.listener.CrazyLoginMessageListener;
import de.st_ddt.crazylogin.listener.CrazyLoginPlayerListener;
import de.st_ddt.crazylogin.listener.CrazyLoginVehicleListener;
import de.st_ddt.crazylogin.tasks.DropInactiveAccountsTask;
import de.st_ddt.crazylogin.tasks.ScheduledCheckTask;
import de.st_ddt.crazyplugin.CrazyPlayerDataPlugin;
import de.st_ddt.crazyplugin.data.ParameterData;
import de.st_ddt.crazyplugin.events.CrazyPlayerRemoveEvent;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandAlreadyExistsException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandErrorException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandExceedingLimitsException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandExecutorException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandNoSuchException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandParameterException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandPermissionException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandUsageException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.CrazyPipe;
import de.st_ddt.crazyutil.ObjectSaveLoadHelper;
import de.st_ddt.crazyutil.ToStringDataGetter;
import de.st_ddt.crazyutil.databases.DatabaseType;

public class CrazyLogin extends CrazyPlayerDataPlugin<LoginData, LoginPlayerData> implements LoginPlugin<LoginPlayerData>
{

	private static CrazyLogin plugin;
	private final HashMap<String, Date> antiRequestSpamTable = new HashMap<String, Date>();
	private final HashMap<String, Integer> loginFailures = new HashMap<String, Integer>();
	private final HashMap<String, Date> tempBans = new HashMap<String, Date>();
	protected CrazyLoginPlayerListener playerListener;
	protected CrazyLoginVehicleListener vehicleListener;
	protected CrazyLoginCrazyListener crazylistener;
	protected CrazyLoginMessageListener messageListener;
	protected boolean alwaysNeedPassword;
	protected int autoLogout;
	protected int autoKick;
	protected long autoTempBan;
	protected int autoKickUnregistered;
	protected int autoKickLoginFailer;
	protected long autoTempBanLoginFailer;
	protected boolean autoKickCommandUsers;
	protected boolean blockGuestCommands;
	protected boolean blockGuestChat;
	protected boolean blockGuestJoin;
	protected boolean resetGuestLocations;
	protected List<String> commandWhiteList;
	protected String uniqueIDKey;
	protected boolean doNotSpamRequests;
	protected boolean forceSingleSession;
	protected boolean forceSingleSessionSameIPBypass;
	protected boolean forceSaveLogin;
	protected boolean hideInventory;
	protected Encryptor encryptor;
	protected int autoDelete;
	protected int maxOnlinesPerIP;
	protected int maxRegistrationsPerIP;
	protected boolean pluginCommunicationEnabled;
	protected double moveRange;
	protected String filterNames;
	protected int minNameLength;
	protected int maxNameLength;

	public static CrazyLogin getPlugin()
	{
		return plugin;
	}

	@Override
	protected String getShortPluginName()
	{
		return "cl";
	}

	@Override
	public void onEnable()
	{
		plugin = this;
		registerHooks();
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new ScheduledCheckTask(this), 30 * 60 * 20, 15 * 60 * 20);
		super.onEnable();
	}

	@Override
	public void onDisable()
	{
		for (LoginPlayerData data : getOnlinePlayerDatas())
			data.notifyAction();
		playerListener.shutdown();
		super.onDisable();
	}

	public void registerHooks()
	{
		this.playerListener = new CrazyLoginPlayerListener(this);
		this.vehicleListener = new CrazyLoginVehicleListener(this);
		this.crazylistener = new CrazyLoginCrazyListener(this, playerListener);
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
		autoLogout = config.getInt("autoLogout", 60 * 60);
		alwaysNeedPassword = config.getBoolean("alwaysNeedPassword", true);
		autoKick = Math.max(config.getInt("autoKick", -1), -1);
		autoTempBan = Math.max(config.getInt("autoTempBan", -1), -1);
		tempBans.clear();
		autoKickUnregistered = Math.max(config.getInt("autoKickUnregistered", config.getInt("kickUnregistered", -1)), -1);
		autoKickLoginFailer = Math.max(config.getInt("autoKickLoginFailer", 3), -1);
		autoTempBanLoginFailer = Math.max(config.getInt("autoTempBanLoginFailer", -1), -1);
		loginFailures.clear();
		autoKickCommandUsers = config.getBoolean("autoKickCommandUsers", false);
		blockGuestCommands = config.getBoolean("blockGuestCommands", false);
		blockGuestChat = config.getBoolean("blockGuestChat", false);
		blockGuestJoin = config.getBoolean("blockGuestJoin", false);
		resetGuestLocations = config.getBoolean("resetGuestLocations", true);
		doNotSpamRequests = config.getBoolean("doNotSpamRequests", false);
		antiRequestSpamTable.clear();
		commandWhiteList = config.getStringList("commandWhitelist");
		if (commandWhiteList.size() == 0)
		{
			commandWhiteList.add("/login");
			commandWhiteList.add("/register");
			commandWhiteList.add("/crazylogin password");
			commandWhiteList.add("/worldedit cui");
		}
		forceSingleSession = config.getBoolean("forceSingleSession", true);
		forceSingleSessionSameIPBypass = config.getBoolean("forceSingleSessionSameIPBypass", true);
		forceSaveLogin = config.getBoolean("forceSaveLogin", false);
		hideInventory = config.getBoolean("hideInventory", false);
		maxOnlinesPerIP = config.getInt("maxOnlinesPerIP", 3);
		maxRegistrationsPerIP = config.getInt("maxRegistrationsPerIP", 3);
		autoDelete = Math.max(config.getInt("autoDelete", -1), -1);
		if (autoDelete != -1)
			getServer().getScheduler().scheduleAsyncRepeatingTask(this, new DropInactiveAccountsTask(this), 20 * 60 * 60, 20 * 60 * 60 * 6);
		moveRange = config.getDouble("moveRange", 5);
		playerListener.clearMovementBlocker(false);
		filterNames = config.getString("filterNames", "false");
		if (filterNames.equals("false"))
			filterNames = ".";
		else if (filterNames.equals("true"))
			filterNames = "[a-zA-Z0-9_]";
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
			if (config.getConfigurationSection("customEncryptor") == null)
				config.createSection("customEncryptor");
			encryptor = ObjectSaveLoadHelper.load(encryption, CustomEncryptor.class, new Class[] { LoginPlugin.class, ConfigurationSection.class }, new Object[] { this, config.getConfigurationSection("customEncryptor") });
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
		// Logger
		logger.createLogChannels(config.getConfigurationSection("logs"), "Join", "Quit", "Login", "Logout", "LoginFail", "ChatBlocked", "CommandBlocked", "AccessDenied");
		// Database
		setupDatabase();
		dropInactiveAccounts();
		// OnlinePlayer
		for (final Player player : getServer().getOnlinePlayers())
		{
			requestLogin(player);
			playerListener.addToMovementBlocker(player);
		}
	}

	public void setupDatabase()
	{
		final ConfigurationSection config = getConfig();
		final String saveType = config.getString("database.saveType", "FLAT").toUpperCase();
		DatabaseType type = null;
		try
		{
			type = DatabaseType.valueOf(saveType);
		}
		catch (final Exception e)
		{
			System.out.println("NO SUCH SAVETYPE " + saveType);
			type = null;
		}
		final String tableName = config.getString("database.tableName", "CrazyLogin_players");
		config.set("database.tableName", tableName);
		try
		{
			if (type == DatabaseType.CONFIG)
			{
				database = new CrazyLoginConfigurationDatabase(tableName, config, this);
			}
			else if (type == DatabaseType.MYSQL)
			{
				database = new CrazyLoginMySQLDatabase(tableName, config);
			}
			else if (type == DatabaseType.FLAT)
			{
				final File file = new File(getDataFolder().getPath() + "/" + tableName + ".db");
				database = new CrazyLoginFlatDatabase(tableName, config, file);
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
				broadcastLocaleMessage(true, "crazylogin.warndatabase", "DATABASE.ACCESSWARN", saveType);
			else
			{
				database.loadAllEntries();
				sendLocaleMessage("DATABASE.LOADED", Bukkit.getConsoleSender(), database.getAllEntries().size());
			}
		}
	}

	@Override
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

	protected synchronized int dropInactiveAccounts(final Date limit)
	{
		final LinkedList<String> deletions = new LinkedList<String>();
		final Iterator<LoginPlayerData> it = database.getAllEntries().iterator();
		while (it.hasNext())
		{
			final LoginPlayerData data = it.next();
			if (data.getLastActionTime().before(limit))
				deletions.add(data.getName());
		}
		for (final String name : deletions)
		{
			database.deleteEntry(name);
			if (pluginCommunicationEnabled)
				Bukkit.getPluginManager().callEvent(new CrazyPlayerRemoveEvent(this, name));
		}
		return deletions.size();
	}

	@Override
	public void saveDatabase()
	{
		dropInactiveAccounts();
		super.saveDatabase();
	}

	@Override
	public void saveConfiguration()
	{
		final ConfigurationSection config = getConfig();
		config.set("alwaysNeedPassword", alwaysNeedPassword);
		config.set("autoLogout", autoLogout);
		config.set("autoKick", autoKick);
		config.set("autoTempBan", autoTempBan);
		config.set("autoKickUnregistered", autoKickUnregistered);
		config.set("autoKickLoginFailer", autoKickLoginFailer);
		config.set("autoTempBanLoginFailer", autoTempBanLoginFailer);
		config.set("autoKickCommandUsers", autoKickCommandUsers);
		config.set("blockGuestCommands", blockGuestCommands);
		config.set("blockGuestChat", blockGuestChat);
		config.set("blockGuestJoin", blockGuestJoin);
		config.set("resetGuestLocations", resetGuestLocations);
		config.set("doNotSpamRequests", doNotSpamRequests);
		config.set("commandWhitelist", commandWhiteList);
		config.set("uniqueIDKey", uniqueIDKey);
		config.set("algorithm", encryptor.getAlgorithm());
		config.set("autoDelete", autoDelete);
		config.set("forceSingleSession", forceSingleSession);
		config.set("forceSingleSessionSameIPBypass", forceSingleSessionSameIPBypass);
		config.set("forceSaveLogin", forceSaveLogin);
		config.set("hideInventory", hideInventory);
		config.set("maxOnlinesPerIP", maxOnlinesPerIP);
		config.set("maxRegistrationsPerIP", maxRegistrationsPerIP);
		config.set("pluginCommunicationEnabled", pluginCommunicationEnabled);
		config.set("moveRange", moveRange);
		if (filterNames.equals("."))
			config.set("filterNames", false);
		else
			config.set("filterNames", filterNames);
		config.set("minNameLength", minNameLength);
		config.set("maxNameLength", maxNameLength);
		logger.save(config, "logs.");
		super.saveConfiguration();
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

	public void commandLogin(final CommandSender sender, final String[] args) throws CrazyCommandException
	{
		if (sender instanceof ConsoleCommandSender)
			throw new CrazyCommandExecutorException(false);
		if (args.length == 0)
			throw new CrazyCommandUsageException("/login <Passwort...>");
		final Player player = (Player) sender;
		final String password = ChatHelper.listingString(args);
		final LoginPlayerData data = database.getEntry(player);
		final CrazyLoginPreLoginEvent<LoginPlayerData> event = new CrazyLoginPreLoginEvent<LoginPlayerData>(this, player, data);
		getServer().getPluginManager().callEvent(event);
		if (event.isCancelled())
		{
			getServer().getPluginManager().callEvent(new CrazyLoginLoginFailEvent<LoginPlayerData>(this, player, data, LoginFailReason.CANCELLED));
			sendLocaleMessage("LOGIN.FAILED", player);
			return;
		}
		if (data == null)
		{
			getServer().getPluginManager().callEvent(new CrazyLoginLoginFailEvent<LoginPlayerData>(this, player, data, LoginFailReason.NO_ACCOUNT));
			sendLocaleMessage("REGISTER.HEADER", player);
			sendLocaleMessage("REGISTER.MESSAGE", player);
			return;
		}
		if (!data.login(password))
		{
			getServer().getPluginManager().callEvent(new CrazyLoginLoginFailEvent<LoginPlayerData>(this, player, data, LoginFailReason.WRONG_PASSWORD));
			broadcastLocaleMessage(true, "crazylogin.warnloginfailure", "LOGIN.FAILEDWARN", player.getName(), player.getAddress().getAddress().getHostAddress());
			Integer fails = loginFailures.get(player.getName().toLowerCase());
			if (fails == null)
				fails = 0;
			fails++;
			if (fails >= autoKickLoginFailer)
			{
				player.kickPlayer(locale.getLocaleMessage(player, "LOGIN.FAILED"));
				if (autoTempBanLoginFailer > 0)
					setTempBanned(player, autoTempBanLoginFailer);
				fails = 0;
			}
			else
				sendLocaleMessage("LOGIN.FAILED", player);
			loginFailures.put(player.getName().toLowerCase(), fails);
			logger.log("LoginFail", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " entered a wrong password");
			return;
		}
		getServer().getPluginManager().callEvent(new CrazyLoginLoginEvent<LoginPlayerData>(this, player, data));
		sendLocaleMessage("LOGIN.SUCCESS", player);
		logger.log("Login", player.getName() + " logged in successfully");
		playerListener.removeFromMovementBlocker(player);
		playerListener.disableSaveLogin(player);
		playerListener.disableHidenInventory(player);
		loginFailures.remove(player.getName().toLowerCase());
		tempBans.remove(player.getAddress().getAddress().getHostAddress());
		data.addIP(player.getAddress().getAddress().getHostAddress());
		if (database != null)
			database.save(data);
	}

	public void commandLogout(final CommandSender sender, final String[] args) throws CrazyCommandException
	{
		if (sender instanceof ConsoleCommandSender)
			throw new CrazyCommandExecutorException(false);
		final Player player = (Player) sender;
		if (!isLoggedIn(player))
			throw new CrazyCommandPermissionException();
		final LoginPlayerData data = database.getEntry(player);
		if (data != null)
		{
			data.logout();
			if (database != null)
				database.save(data);
		}
		player.kickPlayer(locale.getLanguageEntry("LOGOUT.SUCCESS").getLanguageText(player));
		logger.log("Logout", player.getName() + " logged out");
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
		if (commandLabel.equals("admin"))
		{
			commandMainAdmin(sender, args);
			return true;
		}
		if (commandLabel.equals("list") || commandLabel.equals("accounts"))
		{
			commandMainList(sender, args);
			return true;
		}
		if (commandLabel.equals("dropolddata"))
		{
			commandMainDropOldData(sender, args);
			return true;
		}
		if (commandLabel.equals("mode"))
		{
			commandMainMode(sender, args);
			return true;
		}
		if (commandLabel.equals("commands"))
		{
			commandMainCommands(sender, args);
			return true;
		}
		return super.commandMain(sender, commandLabel, args);
	}

	public void commandMainPassword(final CommandSender sender, final String[] args) throws CrazyCommandException
	{
		if (sender instanceof ConsoleCommandSender)
			throw new CrazyCommandExecutorException(false);
		final Player player = (Player) sender;
		if (!isLoggedIn(player) && hasPlayerData(player))
			throw new CrazyCommandPermissionException();
		if (args.length == 0)
		{
			if (alwaysNeedPassword)
				throw new CrazyCommandUsageException("/crazylogin password <Passwort...>");
			playerListener.removeFromMovementBlocker(player);
			sendLocaleMessage("PASSWORDDELETE.SUCCESS", sender);
			database.deleteEntry(player);
			return;
		}
		LoginPlayerData data = getPlayerData(player);
		if (data == null)
		{
			if (!sender.hasPermission("crazylogin.register"))
				throw new CrazyCommandPermissionException();
			final String ip = player.getAddress().getAddress().getHostAddress();
			final int registrations = getPlayerDatasPerIP(ip).size();
			if (!sender.hasPermission("crazylogin.ensureregistration"))
				if (maxRegistrationsPerIP != -1)
					if (registrations >= maxRegistrationsPerIP)
						throw new CrazyCommandExceedingLimitsException("Max Registrations per IP", maxRegistrationsPerIP);
			final CrazyLoginPreRegisterEvent<LoginPlayerData> event = new CrazyLoginPreRegisterEvent<LoginPlayerData>(this, player, data);
			getServer().getPluginManager().callEvent(event);
			if (event.isCancelled())
				throw new CrazyCommandPermissionException();
			data = new LoginPlayerData(player);
			tempBans.remove(player.getAddress().getAddress().getHostAddress());
		}
		final String password = ChatHelper.listingString(args);
		if (pluginCommunicationEnabled)
			getServer().getPluginManager().callEvent(new CrazyLoginPasswordEvent<LoginPlayerData>(this, player, password));
		data.setPassword(password);
		data.login(password);
		sendLocaleMessage("PASSWORDCHANGE.SUCCESS", player);
		playerListener.removeFromMovementBlocker(player);
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
				playerListener.removeFromMovementBlocker(target);
				sendLocaleMessage("PASSWORDDELETE.SUCCESS", sender);
				database.deleteEntry(target);
				return;
			default:
				target = getServer().getPlayerExact(args[0]);
				if (target == null)
				{
					target = getServer().getPlayer(args[0]);
					if (target == null)
						target = getServer().getOfflinePlayer(args[0]);
				}
				if (target == null)
					throw new CrazyCommandNoSuchException("Player", args[0]);
				final LoginPlayerData data = database.getEntry(target);
				if (data == null)
					throw new CrazyCommandNoSuchException("Player", args[0]);
				final String password = ChatHelper.listingString(ChatHelper.shiftArray(args, 1));
				data.setPassword(password);
				sendLocaleMessage("PASSWORDCHANGE.SUCCESS", sender);
				if (database != null)
					database.save(data);
				return;
		}
	}

	private void commandMainList(final CommandSender sender, final String[] args) throws CrazyCommandException
	{
		if (!sender.hasPermission("crazylogin.list"))
			throw new CrazyCommandPermissionException();
		int page = 1;
		int amount = 10;
		final int length = args.length;
		String nameFilter = null;
		String IPFilter = null;
		Boolean registeredFilter = true;
		Boolean onlineFilter = null;
		LoginDataComparator comparator = new LoginDataNameComparator();
		String[] pipe = null;
		for (int i = 0; i < length; i++)
		{
			final String arg = args[i].toLowerCase();
			if (arg.startsWith("page:"))
				try
				{
					page = Integer.parseInt(arg.substring(5));
					if (page < 0)
						throw new CrazyCommandParameterException(i, "positive Integer");
				}
				catch (final NumberFormatException e)
				{
					throw new CrazyCommandParameterException(i, "page:Integer");
				}
			else if (arg.startsWith("amount:"))
			{
				if (arg.substring(7).equals("*"))
					amount = -1;
				else
					try
					{
						amount = Integer.parseInt(arg.substring(7));
					}
					catch (final NumberFormatException e)
					{
						throw new CrazyCommandParameterException(i, "amount:Integer");
					}
			}
			else if (arg.startsWith("name:"))
			{
				if (arg.substring(5).equals("*"))
					nameFilter = null;
				else
					nameFilter = arg.substring(5).toLowerCase();
			}
			else if (arg.startsWith("ip:"))
			{
				if (arg.substring(3).equals("*"))
					IPFilter = null;
				else
					IPFilter = arg.substring(3);
			}
			else if (arg.startsWith("registered:"))
			{
				final String temp = arg.substring(11);
				if (temp.equals("*"))
					registeredFilter = null;
				else if (temp.equalsIgnoreCase("true") || temp.equals("1"))
					registeredFilter = true;
				else
					registeredFilter = false;
			}
			else if (arg.startsWith("online:"))
			{
				final String temp = arg.substring(7);
				if (temp.equals("*"))
					onlineFilter = null;
				else if (temp.equalsIgnoreCase("true") || temp.equals("1"))
					onlineFilter = true;
				else
					onlineFilter = false;
			}
			else if (arg.startsWith("sort:"))
			{
				final String temp = arg.substring(5);
				if (temp.equals("name"))
					comparator = new LoginDataNameComparator();
				else if (temp.equals("ip"))
					comparator = new LoginDataIPComparator();
				else if (temp.equals("date") || temp.equals("time"))
					comparator = new LoginDataLastActionComparator();
				else
					throw new CrazyCommandParameterException(i, "sortType", "sort:Name/IP/Date");
			}
			else if (arg.equals(">"))
			{
				pipe = ChatHelper.shiftArray(args, i + 1);
				break;
			}
			else if (arg.equals("*"))
			{
				page = Integer.MIN_VALUE;
			}
			else
				try
				{
					page = Integer.parseInt(arg);
				}
				catch (final NumberFormatException e)
				{
					throw new CrazyCommandUsageException("/crazylogin list [name:Player] [ip:IP] [registered:True/False/*] [online:True/False/*] [amount:Integer] [sort:Name/IP/Date] [[page:]Integer] [> Pipe]");
				}
		}
		final ArrayList<LoginData> dataList = new ArrayList<LoginData>();
		if (registeredFilter == null)
		{
			dataList.addAll(database.getAllEntries());
			for (final OfflinePlayer offline : getServer().getOfflinePlayers())
				if (!hasPlayerData(offline))
					dataList.add(new LoginUnregisteredPlayerData(offline));
		}
		else if (registeredFilter.equals(true))
		{
			dataList.addAll(database.getAllEntries());
		}
		else
		{
			for (final OfflinePlayer offline : getServer().getOfflinePlayers())
				if (!hasPlayerData(offline))
					dataList.add(new LoginUnregisteredPlayerData(offline));
		}
		if (IPFilter != null)
		{
			final Iterator<LoginData> it = dataList.iterator();
			while (it.hasNext())
				if (!it.next().hasIP(IPFilter))
					it.remove();
		}
		if (nameFilter != null)
		{
			Pattern pattern = null;
			try
			{
				pattern = Pattern.compile(nameFilter);
			}
			catch (final PatternSyntaxException e)
			{
				throw new CrazyCommandErrorException(e);
			}
			final Iterator<LoginData> it = dataList.iterator();
			while (it.hasNext())
				if (!pattern.matcher(it.next().getName().toLowerCase()).matches())
					it.remove();
		}
		if (onlineFilter != null)
		{
			final Iterator<LoginData> it = dataList.iterator();
			while (it.hasNext())
				if (!onlineFilter.equals(it.next().isOnline()))
					it.remove();
		}
		Collections.sort(dataList, comparator);
		if (pipe != null)
		{
			final ArrayList<ParameterData> datas = new ArrayList<ParameterData>(dataList);
			CrazyPipe.pipe(sender, datas, pipe);
			return;
		}
		sendListMessage(sender, "PLAYERDATA.LISTHEAD", amount, page, dataList, new ToStringDataGetter());
	}

	protected void commandMainDropOldData(final CommandSender sender, final String[] args) throws CrazyCommandException
	{
		if (!sender.hasPermission("crazylogin.dropOldData"))
		{
			String days = "(-)";
			if (args.length != 0)
				days = args[0];
			broadcastLocaleMessage(true, "crazylogin.warndelete", "ACCOUNTS.DELETEWARN", sender.getName(), days);
			throw new CrazyCommandPermissionException();
		}
		if (args.length < 2)
			if (sender instanceof ConsoleCommandSender)
				throw new CrazyCommandUsageException("/crazylogin dropOldData <DaysToKeep> CONSOLE_CONFIRM");
			else
				throw new CrazyCommandUsageException("/crazylogin dropOldData <DaysToKeep> <Password>");
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
		final String password = ChatHelper.listingString(" ", (Object[]) ChatHelper.shiftArray(args, 1));
		if (sender instanceof ConsoleCommandSender)
		{
			if (!password.equals("CONSOLE_CONFIRM"))
				throw new CrazyCommandUsageException("/crazylogin dropOldData <DaysToKeep> CONSOLE_CONFIRM");
		}
		else
		{
			if (!database.getEntry((Player) sender).isPassword(password))
				throw new CrazyCommandUsageException("/crazylogin dropOldData <DaysToKeep> <Password>");
		}
		final int amount = dropInactiveAccounts(days);
		broadcastLocaleMessage(true, "crazylogin.warndelete", "ACCOUNTS.DELETED", sender.getName(), days, amount);
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
				else if (args[0].equalsIgnoreCase("forceSaveLogin"))
				{
					boolean newValue = false;
					if (args[1].equalsIgnoreCase("1") || args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("yes"))
						newValue = true;
					forceSaveLogin = newValue;
					sendLocaleMessage("MODE.CHANGE", sender, "forceSaveLogin", forceSaveLogin ? "True" : "False");
					saveConfiguration();
					return;
				}
				else if (args[0].equalsIgnoreCase("hideInventory"))
				{
					boolean newValue = false;
					if (args[1].equalsIgnoreCase("1") || args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("yes"))
						newValue = true;
					hideInventory = newValue;
					sendLocaleMessage("MODE.CHANGE", sender, "hideInventory", hideInventory ? "True" : "False");
					saveConfiguration();
					return;
				}
				else if (args[0].equalsIgnoreCase("forceSingleSession"))
				{
					boolean newValue = false;
					if (args[1].equalsIgnoreCase("1") || args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("yes"))
						newValue = true;
					forceSingleSession = newValue;
					sendLocaleMessage("MODE.CHANGE", sender, "forceSingleSession", forceSingleSession ? "True" : "False");
					saveConfiguration();
					return;
				}
				else if (args[0].equalsIgnoreCase("forceSingleSessionSameIPBypass"))
				{
					boolean newValue = false;
					if (args[1].equalsIgnoreCase("1") || args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("yes"))
						newValue = true;
					forceSingleSessionSameIPBypass = newValue;
					sendLocaleMessage("MODE.CHANGE", sender, "forceSingleSessionSameIPBypass", forceSingleSessionSameIPBypass ? "True" : "False");
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
				else if (args[0].equalsIgnoreCase("autoTempBan"))
				{
					long time = autoTempBan;
					try
					{
						time = Long.parseLong(args[1]);
					}
					catch (final NumberFormatException e)
					{
						throw new CrazyCommandParameterException(1, "Integer", "-1 = disabled", "Time in Seconds > 60");
					}
					autoTempBan = time;
					sendLocaleMessage("MODE.CHANGE", sender, "autoTempBan", autoTempBan == -1 ? "disabled" : autoTempBan + " seconds");
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
				else if (args[0].equalsIgnoreCase("autoTempBanLoginFailer"))
				{
					long time = autoTempBanLoginFailer;
					try
					{
						time = Long.parseLong(args[1]);
					}
					catch (final NumberFormatException e)
					{
						throw new CrazyCommandParameterException(1, "Integer", "-1 = disabled", "Time in Seconds > 60");
					}
					autoTempBanLoginFailer = time;
					sendLocaleMessage("MODE.CHANGE", sender, "autoTempBanLoginFailer", autoTempBanLoginFailer == -1 ? "disabled" : autoTempBanLoginFailer + " seconds");
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
				else if (args[0].equalsIgnoreCase("blockGuestCommands"))
				{
					boolean newValue = false;
					if (args[1].equalsIgnoreCase("1") || args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("yes"))
						newValue = true;
					blockGuestCommands = newValue;
					sendLocaleMessage("MODE.CHANGE", sender, "blockGuestCommands", blockGuestCommands ? "True" : "False");
					saveConfiguration();
					return;
				}
				else if (args[0].equalsIgnoreCase("blockGuestChat"))
				{
					boolean newValue = false;
					if (args[1].equalsIgnoreCase("1") || args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("yes"))
						newValue = true;
					blockGuestChat = newValue;
					sendLocaleMessage("MODE.CHANGE", sender, "blockGuestChat", blockGuestChat ? "True" : "False");
					saveConfiguration();
					return;
				}
				else if (args[0].equalsIgnoreCase("blockGuestJoin"))
				{
					boolean newValue = false;
					if (args[1].equalsIgnoreCase("1") || args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("yes"))
						newValue = true;
					blockGuestJoin = newValue;
					sendLocaleMessage("MODE.CHANGE", sender, "blockGuestJoin", blockGuestJoin ? "True" : "False");
					saveConfiguration();
					return;
				}
				else if (args[0].equalsIgnoreCase("resetGuestLocations"))
				{
					boolean newValue = false;
					if (args[1].equalsIgnoreCase("1") || args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("yes"))
						newValue = true;
					resetGuestLocations = newValue;
					if (resetGuestLocations)
						playerListener.clearMovementBlocker(true);
					sendLocaleMessage("MODE.CHANGE", sender, "resetGuestLocations", resetGuestLocations ? "True" : "False");
					saveConfiguration();
					return;
				}
				else if (args[0].equalsIgnoreCase("maxRegistrationsPerIP"))
				{
					int newValue = maxRegistrationsPerIP;
					try
					{
						newValue = Integer.parseInt(args[1]);
					}
					catch (final NumberFormatException e)
					{
						throw new CrazyCommandParameterException(1, "Integer", "-1 = disabled");
					}
					maxRegistrationsPerIP = Math.max(newValue, -1);
					sendLocaleMessage("MODE.CHANGE", sender, "maxRegistrationsPerIP", maxRegistrationsPerIP);
					saveConfiguration();
					return;
				}
				else if (args[0].equalsIgnoreCase("maxOnlinesPerIP"))
				{
					int newValue = maxOnlinesPerIP;
					try
					{
						newValue = Integer.parseInt(args[1]);
					}
					catch (final NumberFormatException e)
					{
						throw new CrazyCommandParameterException(1, "Integer", "-1 = disabled");
					}
					maxOnlinesPerIP = Math.max(newValue, -1);
					sendLocaleMessage("MODE.CHANGE", sender, "maxOnlinesPerIP", maxOnlinesPerIP);
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
					catch (final Exception e)
					{
						type = null;
					}
					if (type == null)
						throw new CrazyCommandNoSuchException("SaveType", saveType);
					sendLocaleMessage("MODE.CHANGE", sender, "saveType", saveType);
					if (type == database.getType())
						return;
					final Collection<LoginPlayerData> datas = database.getAllEntries();
					getConfig().set("database.saveType", type.toString());
					setupDatabase();
					database.saveAll(datas);
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
					sendLocaleMessage("MODE.CHANGE", sender, "autoDelete", autoDelete == -1 ? "disabled" : autoDelete + " days");
					saveConfiguration();
					if (autoDelete != -1)
						getServer().getScheduler().scheduleAsyncRepeatingTask(this, new DropInactiveAccountsTask(this), 20 * 60 * 60, 20 * 60 * 60 * 6);
					return;
				}
				else if (args[0].equalsIgnoreCase("moveRange"))
				{
					double range = moveRange;
					try
					{
						range = Double.parseDouble(args[1]);
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
				else if (args[0].equalsIgnoreCase("filterNames"))
				{
					String newFilter = filterNames;
					newFilter = args[0];
					if (newFilter.equalsIgnoreCase("false") || newFilter.equalsIgnoreCase("0") || newFilter.equalsIgnoreCase("off"))
						newFilter = ".";
					else if (newFilter.equalsIgnoreCase("true") || newFilter.equalsIgnoreCase("1") || newFilter.equalsIgnoreCase("on"))
						newFilter = "[a-zA-Z0-9_]";
					filterNames = newFilter;
					sendLocaleMessage("MODE.CHANGE", sender, "filterNames", filterNames.equals(".") ? "disabled" : filterNames);
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
				else if (args[0].equalsIgnoreCase("saveDatabaseOnShutdown"))
				{
					boolean newValue = false;
					if (args[1].equalsIgnoreCase("1") || args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("yes"))
						newValue = true;
					saveDatabaseOnShutdown = newValue;
					sendLocaleMessage("MODE.CHANGE", sender, "saveDatabaseOnShutdown", saveDatabaseOnShutdown ? "True" : "False");
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
				else if (args[0].equalsIgnoreCase("forceSaveLogin"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "forceSaveLogin", forceSaveLogin ? "True" : "False");
					return;
				}
				else if (args[0].equalsIgnoreCase("hideInventory"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "hideInventory", hideInventory ? "True" : "False");
					return;
				}
				else if (args[0].equalsIgnoreCase("forceSingleSession"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "forceSingleSession", forceSingleSession ? "True" : "False");
					return;
				}
				else if (args[0].equalsIgnoreCase("forceSingleSessionSameIPBypass"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "forceSingleSessionSameIPBypass", forceSingleSessionSameIPBypass ? "True" : "False");
					return;
				}
				else if (args[0].equalsIgnoreCase("autoLogout"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "autoLogout", autoLogout == -1 ? "disabled" : autoLogout + " seconds");
					return;
				}
				else if (args[0].equalsIgnoreCase("autoKick"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "autoKick", autoKick == -1 ? "disabled" : autoKick + " seconds");
					return;
				}
				else if (args[0].equalsIgnoreCase("autoTempBan"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "autoTempBan", autoTempBan == -1 ? "disabled" : autoTempBan + " seconds");
					return;
				}
				else if (args[0].equalsIgnoreCase("autoKickUnregistered"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "autoKickUnregistered", autoKickUnregistered == -1 ? "disabled" : autoKickUnregistered + " seconds");
					return;
				}
				else if (args[0].equalsIgnoreCase("autoKickLoginFailer"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "autoKickLoginFailer", autoKickLoginFailer == -1 ? "disabled" : autoKickUnregistered + " tries");
					return;
				}
				else if (args[0].equalsIgnoreCase("autoTempBanLoginFailer"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "autoTempBanLoginFailer", autoTempBanLoginFailer == -1 ? "disabled" : autoTempBanLoginFailer + " seconds");
					return;
				}
				else if (args[0].equalsIgnoreCase("autoKickCommandUsers"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "autoKickCommandUsers", autoKickCommandUsers ? "True" : "False");
					return;
				}
				else if (args[0].equalsIgnoreCase("blockGuestCommands"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "blockGuestCommands", blockGuestCommands ? "True" : "False");
					return;
				}
				else if (args[0].equalsIgnoreCase("blockGuestChat"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "blockGuestChat", blockGuestChat ? "True" : "False");
					return;
				}
				else if (args[0].equalsIgnoreCase("blockGuestJoin"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "blockGuestJoin", blockGuestJoin ? "True" : "False");
					return;
				}
				else if (args[0].equalsIgnoreCase("resetGuestLocations"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "resetGuestLocations", resetGuestLocations ? "True" : "False");
					return;
				}
				else if (args[0].equalsIgnoreCase("maxRegistrationsPerIP"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "maxRegistrationsPerIP", maxRegistrationsPerIP);
					return;
				}
				else if (args[0].equalsIgnoreCase("maxOnlinesPerIP"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "maxOnlinesPerIP", maxOnlinesPerIP);
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
				else if (args[0].equalsIgnoreCase("filterNames"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "filterNames", filterNames.equals(".") ? "disabled" : filterNames);
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
				else if (args[0].equalsIgnoreCase("algorithm"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "algorithm", encryptor.getAlgorithm());
					return;
				}
				else if (args[0].equalsIgnoreCase("saveDatabaseOnShutdown"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "saveDatabaseOnShutdown", saveDatabaseOnShutdown ? "True" : "False");
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
		int page = 1;
		switch (args.length)
		{
			case 1:
				page = 1;
				try
				{
					page = Integer.parseInt(args[0]);
				}
				catch (final NumberFormatException e)
				{
					throw new CrazyCommandParameterException(1, "Integer");
				}
			case 0:
				sendListMessage(sender, "COMMAND.LISTHEAD", 10, page, commandWhiteList, new ToStringDataGetter());
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
	public boolean commandPlayer(final CommandSender sender, final String commandLabel, final String[] args) throws CrazyException
	{
		if (commandLabel.equals("create"))
		{
			commandPlayerCreate(sender, args);
			return true;
		}
		return super.commandPlayer(sender, commandLabel, args);
	}

	protected void commandPlayerCreate(final CommandSender sender, final String[] args) throws CrazyCommandException
	{
		if (!sender.hasPermission("crazylogin.player.create"))
			throw new CrazyCommandPermissionException();
		if (args.length < 2)
			throw new CrazyCommandUsageException("/crazylogin create <Name> <Password>");
		final String name = args[0];
		if (database.hasEntry(name))
			throw new CrazyCommandAlreadyExistsException("Account", name);
		final LoginPlayerData data = new LoginPlayerData(name);
		final String password = ChatHelper.listingString(ChatHelper.shiftArray(args, 1));
		data.setPassword(password);
		sendLocaleMessage("PASSWORDCHANGE.SUCCESS", sender);
		database.save(data);
	}

	@Override
	public boolean isLoggedIn(final Player player)
	{
		final LoginPlayerData data = database.getEntry(player);
		if (data == null)
			return !alwaysNeedPassword;
		return data.isLoggedIn() && player.isOnline();
	}

	@Override
	public void updateAccount(final String name)
	{
		if (database.isStaticDatabase())
			return;
		LoginPlayerData data = database.getEntry(name);
		boolean online = false;
		if (data != null)
			online = data.isLoggedIn();
		data = database.loadEntry(name);
		if (data == null)
			return;
		data.setOnline(online);
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
		if (database.hasEntry(player))
			sendLocaleMessage("LOGIN.REQUEST", player);
		else
			sendLocaleMessage("REGISTER.REQUEST", player);
	}

	@Override
	public boolean isAlwaysNeedPassword()
	{
		return alwaysNeedPassword;
	}

	@Override
	public boolean isAutoLogoutEnabled()
	{
		return autoLogout != -1;
	}

	@Override
	public boolean isInstantAutoLogoutEnabled()
	{
		return autoLogout == 0;
	}

	@Override
	public int getAutoLogoutTime()
	{
		return autoLogout;
	}

	public void checkTimeOuts()
	{
		if (autoLogout > 0)
		{
			final Date timeOut = new Date();
			timeOut.setTime(timeOut.getTime() - autoLogout * 1000);
			for (final LoginPlayerData data : database.getAllEntries())
				data.checkTimeOut(this, timeOut, true);
		}
	}

	@Override
	public int getAutoKick()
	{
		return autoKick;
	}

	@Override
	public long getAutoTempBan()
	{
		return autoTempBan;
	}

	@Override
	public int getAutoKickUnregistered()
	{
		return autoKickUnregistered;
	}

	@Override
	public int getAutoKickLoginFailer()
	{
		return autoKickLoginFailer;
	}

	@Override
	public long getAutoTempBanLoginFailer()
	{
		return autoTempBanLoginFailer;
	}

	@Override
	public boolean isAutoKickCommandUsers()
	{
		return autoKickCommandUsers;
	}

	@Override
	public boolean isBlockingGuestCommandsEnabled()
	{
		return blockGuestCommands;
	}

	public boolean isBlockingGuestChatEnabled()
	{
		return blockGuestChat;
	}

	public boolean isBlockingGuestJoinEnabled()
	{
		return blockGuestJoin;
	}

	@Override
	public boolean isResettingGuestLocationsEnabled()
	{
		return resetGuestLocations;
	}

	@Override
	public boolean isTempBanned(final String IP)
	{
		final Date date = tempBans.get(IP);
		if (date == null)
			return false;
		return new Date().before(date);
	}

	@Override
	public Date getTempBanned(final String IP)
	{
		return tempBans.get(IP);
	}

	@Override
	public String getTempBannedString(final String IP)
	{
		final Date date = getTempBanned(IP);
		if (date == null)
			return DateFormat.format(new Date(0));
		return DateFormat.format(date);
	}

	public void setTempBanned(final Player player, final long duration)
	{
		setTempBanned(player.getAddress().getAddress().getHostAddress(), duration * 1000);
	}

	public void setTempBanned(final String IP, final long duration)
	{
		final Date until = new Date();
		until.setTime(until.getTime() + autoTempBan * 1000);
		tempBans.put(IP, until);
	}

	@Override
	public List<String> getCommandWhiteList()
	{
		return commandWhiteList;
	}

	public boolean isAvoidingSpammedRequestsEnabled()
	{
		return doNotSpamRequests;
	}

	@Override
	public boolean isForceSingleSessionEnabled()
	{
		return forceSingleSession;
	}

	@Override
	public boolean isForceSingleSessionSameIPBypassEnabled()
	{
		return forceSingleSessionSameIPBypass;
	}

	@Override
	public boolean isForceSaveLoginEnabled()
	{
		return forceSaveLogin;
	}

	@Override
	public boolean isHidingInventoryEnabled()
	{
		return hideInventory;
	}

	@Override
	public Encryptor getEncryptor()
	{
		return encryptor;
	}

	@Override
	public int getAutoDelete()
	{
		return autoDelete;
	}

	@Override
	public int getMaxOnlinesPerIP()
	{
		return maxOnlinesPerIP;
	}

	@Override
	public int getMaxRegistrationsPerIP()
	{
		return maxRegistrationsPerIP;
	}

	public boolean isPluginCommunicationEnabled()
	{
		return pluginCommunicationEnabled;
	}

	@Override
	public double getMoveRange()
	{
		return moveRange;
	}

	@Override
	public boolean checkNameChars(final String name)
	{
		return name.matches(filterNames + "*");
	}

	@Override
	public int getMinNameLength()
	{
		return minNameLength;
	}

	@Override
	public int getMaxNameLength()
	{
		return maxNameLength;
	}

	@Override
	public boolean checkNameLength(final String name)
	{
		final int length = name.length();
		if (length < minNameLength)
			return false;
		if (length > maxNameLength)
			return false;
		return true;
	}

	@Override
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
	public HashSet<LoginPlayerData> getPlayerDatasPerIP(final String IP)
	{
		final HashSet<LoginPlayerData> res = new HashSet<LoginPlayerData>();
		for (final LoginPlayerData data : database.getAllEntries())
			if (data.hasIP(IP))
				res.add(data);
		return res;
	}
}
