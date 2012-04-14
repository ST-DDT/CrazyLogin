package de.st_ddt.crazylogin;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import de.st_ddt.crazylogin.crypt.AuthMeCrypt;
import de.st_ddt.crazylogin.crypt.CrazyCrypt1;
import de.st_ddt.crazylogin.crypt.CustomEncryptor;
import de.st_ddt.crazylogin.crypt.DefaultCrypt;
import de.st_ddt.crazylogin.crypt.Encryptor;
import de.st_ddt.crazylogin.crypt.PlainCrypt;
import de.st_ddt.crazylogin.crypt.WhirlPoolCrypt;
import de.st_ddt.crazylogin.databases.CrazyLoginConfigurationDatabase;
import de.st_ddt.crazylogin.databases.CrazyLoginMySQLDatabase;
import de.st_ddt.crazyplugin.CrazyPlugin;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandExecutorException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandNoSuchException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandParameterException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandPermissionException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandUsageException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.ObjectSaveLoadHelper;
import de.st_ddt.crazyutil.PairList;
import de.st_ddt.crazyutil.databases.Database;
import de.st_ddt.crazyutil.databases.MySQLConnection;

public class CrazyLogin extends CrazyPlugin
{

	private static CrazyLogin plugin;
	protected final PairList<String, LoginPlayerData> datas = new PairList<String, LoginPlayerData>();
	private CrazyLoginPlayerListener playerListener;
	private CrazyLoginVehicleListener vehicleListener;
	protected boolean alwaysNeedPassword;
	protected boolean autoLogout;
	protected int autoKick;
	protected List<String> commandWhiteList;
	protected boolean autoKickCommandUsers;
	protected String uniqueIDKey;
	protected Encryptor encryptor;
	// Database
	protected String saveType;
	protected String tableName;
	protected Database<LoginPlayerData> database;
	private String colName;
	private String colPassword;
	private String colIPs;
	private boolean doNotSpamRequests;

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
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(playerListener, this);
		pm.registerEvents(vehicleListener, this);
	}

	@Override
	public void load()
	{
		super.load();
		FileConfiguration config = getConfig();
		autoLogout = config.getBoolean("autoLogout", false);
		alwaysNeedPassword = config.getBoolean("alwaysNeedPassword", true);
		autoKick = Math.max(config.getInt("autoKick", -1), -1);
		doNotSpamRequests = config.getBoolean("doNotSpamRequests", false);
		commandWhiteList = config.getStringList("commandWhitelist");
		autoKickCommandUsers = config.getBoolean("autoKickCommandUsers", false);
		if (commandWhiteList.size() == 0)
		{
			commandWhiteList.add("/login");
			commandWhiteList.add("/register");
			commandWhiteList.add("/crazylogin password");
		}
		uniqueIDKey = config.getString("uniqueIDKey");
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
				sendLocaleMessage("ALGORITHM.MISSING", Bukkit.getConsoleSender(), algorithm);
				encryptor = new CrazyCrypt1();
			}
		}
		setupDatabase();
		if (database != null)
			for (LoginPlayerData data : database.getAllEntries())
				datas.setDataVia1(data.getName().toLowerCase(), data);
	}

	public void setupDatabase()
	{
		FileConfiguration config = getConfig();
		saveType = config.getString("database.saveType", "flat").toLowerCase();
		tableName = config.getString("database.tableName", "players");
		if (saveType.equals("flat"))
		{
			database = new CrazyLoginConfigurationDatabase(config, tableName);
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
			colName = config.getString("database.columns.name", "Name");
			config.set("database.columns.name", colName);
			colPassword = config.getString("database.columns.name", "Password");
			config.set("database.columns.name", colPassword);
			config.set("database.columns.name", colIPs);
			database = new CrazyLoginMySQLDatabase(connection, tableName, colName, colPassword, colIPs);
		}
	}

	@Override
	public void save()
	{
		FileConfiguration config = getConfig();
		config.set("database.saveType", saveType);
		config.set("database.tableName", tableName);
		if (database != null)
			database.saveAll(datas.getData2List());
		config.set("alwaysNeedPassword", alwaysNeedPassword);
		config.set("autoLogout", autoLogout);
		config.set("autoKick", autoKick);
		config.set("doNotSpamRequests", doNotSpamRequests);
		config.set("commandWhitelist", commandWhiteList);
		config.set("autoKickCommandUsers", autoKickCommandUsers);
		config.set("uniqueIDKey", uniqueIDKey);
		config.set("algorithm", encryptor.getAlgorithm());
		super.save();
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
		if (data == null)
		{
			sendLocaleMessage("REGISTER.HEADER", player);
			sendLocaleMessage("REGISTER.MESSAGE", player);
			return;
		}
		if (!data.login(password))
		{
			sendLocaleMessage("LOGIN.FAILED", player);
			broadcastLocaleMessage(true, true, "crazylogin.warnloginfailure", "LOGIN.FAILEDWARN", player.getName(), player.getAddress().getAddress().getHostAddress());
			return;
		}
		sendLocaleMessage("LOGIN.SUCCESS", player);
		data.addIP(player.getAddress().getAddress().getHostAddress());
		save();
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
			data.logout();
		player.kickPlayer(locale.getLanguageEntry("LOGOUT.SUCCESS").getLanguageText(player));
		save();
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
		return false;
	}

	private void commandMainPassword(final CommandSender sender, final String[] args) throws CrazyCommandException
	{
		if (sender instanceof ConsoleCommandSender)
			throw new CrazyCommandExecutorException(false);
		Player player = (Player) sender;
		if (args.length == 0)
		{
			if (alwaysNeedPassword)
				throw new CrazyCommandUsageException("/crazylogin password <Passwort...>");
			datas.removeDataVia1(player.getName().toLowerCase());
			sendLocaleMessage("PASSWORDCHANGE.SUCCESS", sender);
			save();
			return;
		}
		LoginPlayerData data = datas.findDataVia1(player.getName().toLowerCase());
		if (data == null)
		{
			data = new LoginPlayerData(player);
			datas.setDataVia1(player.getName().toLowerCase(), data);
		}
		else if (!isLoggedIn(player))
			throw new CrazyCommandPermissionException();
		String password = ChatHelper.listToString(args);
		data.setPassword(password);
		data.login(password);
		sendLocaleMessage("PASSWORDCHANGE.SUCCESS", player);
		save();
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
				if (alwaysNeedPassword)
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
				datas.removeDataVia1(target.getName().toLowerCase());
				sendLocaleMessage("PASSWORDCHANGE.SUCCESS", sender);
				save();
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
		save();
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
					save();
					return;
				}
				else if (args[0].equalsIgnoreCase("autoLogout"))
				{
					boolean newValue = false;
					if (args[1].equalsIgnoreCase("1") || args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("yes"))
						newValue = true;
					autoLogout = newValue;
					sendLocaleMessage("MODE.CHANGE", sender, "autoLogout", autoLogout ? "True" : "False");
					save();
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
					save();
					return;
				}
				else if (args[0].equalsIgnoreCase("saveType"))
				{
					String newValue = args[1];
					boolean changed = saveType.equals(newValue);
					if (newValue.equalsIgnoreCase("flat"))
						saveType = "flat";
					else if (newValue.equalsIgnoreCase("mysql"))
						saveType = "mysql";
					else
						throw new CrazyCommandNoSuchException("SaveType", newValue);
					sendLocaleMessage("MODE.CHANGE", sender, "saveType", saveType);
					if (changed)
						return;
					setupDatabase();
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
				else if (args[0].equalsIgnoreCase("autoLogout"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "autoLogout", autoLogout ? "True" : "False");
					return;
				}
				else if (args[0].equalsIgnoreCase("autoKick"))
				{
					sendLocaleMessage("MODE.CHANGE", sender, "autoKick", autoKick == -1 ? "disabled" : autoKick + " seconds");
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

	public boolean isAlwaysNeedPassword()
	{
		return alwaysNeedPassword;
	}

	public boolean isAutoLogoutEnabled()
	{
		return autoLogout;
	}

	public int getAutoKick()
	{
		return autoKick;
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

	public PairList<String, LoginPlayerData> getPlayerData()
	{
		return datas;
	}

	public void requestLogin(Player player)
	{
		if (doNotSpamRequests)
			return;
		if (datas.findDataVia1(player.getName().toLowerCase()) == null)
			sendLocaleMessage("REGISTER.REQUEST", player);
		else
			sendLocaleMessage("LOGIN.REQUEST", player);
	}

	// Database stuff
	public String getColName()
	{
		return colName;
	}

	public String getColPassword()
	{
		return colPassword;
	}

	public String getColIPs()
	{
		return colIPs;
	}
}
