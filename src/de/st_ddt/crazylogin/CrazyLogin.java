package de.st_ddt.crazylogin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.messaging.Messenger;

import de.st_ddt.crazylogin.commands.CrazyCommandLoginCheck;
import de.st_ddt.crazylogin.commands.CrazyLoginCommandAdminLogin;
import de.st_ddt.crazylogin.commands.CrazyLoginCommandExecutor;
import de.st_ddt.crazylogin.commands.CrazyLoginCommandLogin;
import de.st_ddt.crazylogin.commands.CrazyLoginCommandLogout;
import de.st_ddt.crazylogin.commands.CrazyLoginCommandMainCommands;
import de.st_ddt.crazylogin.commands.CrazyLoginCommandMainDropOldData;
import de.st_ddt.crazylogin.commands.CrazyLoginCommandPassword;
import de.st_ddt.crazylogin.commands.CrazyLoginCommandPlayerCreate;
import de.st_ddt.crazylogin.commands.CrazyLoginCommandPlayerDetachIP;
import de.st_ddt.crazylogin.commands.CrazyLoginCommandPlayerPassword;
import de.st_ddt.crazylogin.crypt.ChangedAlgorithmEncryptor;
import de.st_ddt.crazylogin.crypt.CrazyCrypt1;
import de.st_ddt.crazylogin.crypt.CrazyCrypt2;
import de.st_ddt.crazylogin.crypt.EncryptHelper;
import de.st_ddt.crazylogin.crypt.Encryptor;
import de.st_ddt.crazylogin.crypt.MD2Crypt;
import de.st_ddt.crazylogin.crypt.MD5Crypt;
import de.st_ddt.crazylogin.crypt.PlainCrypt;
import de.st_ddt.crazylogin.crypt.SHA_1Crypt;
import de.st_ddt.crazylogin.crypt.SHA_256Crypt;
import de.st_ddt.crazylogin.crypt.SHA_512Crypt;
import de.st_ddt.crazylogin.crypt.SeededMD2Crypt;
import de.st_ddt.crazylogin.crypt.SeededMD5Crypt;
import de.st_ddt.crazylogin.crypt.SeededSHA_1Crypt;
import de.st_ddt.crazylogin.crypt.SeededSHA_256Crypt;
import de.st_ddt.crazylogin.crypt.SeededSHA_512Crypt;
import de.st_ddt.crazylogin.crypt.UpdatingEncryptor;
import de.st_ddt.crazylogin.crypt.WebCrypt;
import de.st_ddt.crazylogin.crypt.WhirlPoolCrypt;
import de.st_ddt.crazylogin.data.LoginData;
import de.st_ddt.crazylogin.data.LoginPlayerData;
import de.st_ddt.crazylogin.data.LoginUnregisteredPlayerData;
import de.st_ddt.crazylogin.data.comparator.LoginDataComparator;
import de.st_ddt.crazylogin.data.comparator.LoginDataIPComparator;
import de.st_ddt.crazylogin.data.comparator.LoginDataLastActionComparator;
import de.st_ddt.crazylogin.databases.CrazyLoginConfigurationDatabase;
import de.st_ddt.crazylogin.databases.CrazyLoginDataDatabase;
import de.st_ddt.crazylogin.databases.CrazyLoginFlatDatabase;
import de.st_ddt.crazylogin.databases.CrazyLoginMySQLDatabase;
import de.st_ddt.crazylogin.databases.CrazyLoginSQLiteDatabase;
import de.st_ddt.crazylogin.events.CrazyLoginLoginEvent;
import de.st_ddt.crazylogin.events.CrazyLoginLoginFailEvent;
import de.st_ddt.crazylogin.events.CrazyLoginPasswordEvent;
import de.st_ddt.crazylogin.events.CrazyLoginPreLoginEvent;
import de.st_ddt.crazylogin.events.CrazyLoginPreRegisterEvent;
import de.st_ddt.crazylogin.events.LoginFailReason;
import de.st_ddt.crazylogin.exceptions.CrazyLoginExceedingMaxRegistrationsPerIPException;
import de.st_ddt.crazylogin.exceptions.CrazyLoginException;
import de.st_ddt.crazylogin.exceptions.CrazyLoginRegistrationsDisabled;
import de.st_ddt.crazylogin.listener.CrazyLoginCrazyListener;
import de.st_ddt.crazylogin.listener.CrazyLoginDynamicPlayerListener;
import de.st_ddt.crazylogin.listener.CrazyLoginDynamicPlayerListener_125;
import de.st_ddt.crazylogin.listener.CrazyLoginDynamicPlayerListener_132;
import de.st_ddt.crazylogin.listener.CrazyLoginDynamicPlayerListener_142;
import de.st_ddt.crazylogin.listener.CrazyLoginDynamicVehicleListener;
import de.st_ddt.crazylogin.listener.CrazyLoginMessageListener;
import de.st_ddt.crazylogin.listener.CrazyLoginPlayerListener;
import de.st_ddt.crazylogin.listener.CrazyLoginWorldListener;
import de.st_ddt.crazylogin.metadata.Authenticated;
import de.st_ddt.crazylogin.tasks.DropInactiveAccountsTask;
import de.st_ddt.crazylogin.tasks.ScheduledCheckTask;
import de.st_ddt.crazyplugin.CrazyPlayerDataPlugin;
import de.st_ddt.crazyplugin.commands.CrazyPluginCommandMainMode;
import de.st_ddt.crazyplugin.data.PlayerDataFilter;
import de.st_ddt.crazyplugin.data.PlayerDataNameFilter;
import de.st_ddt.crazyplugin.events.CrazyPlayerRemoveEvent;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandCircumstanceException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandNoSuchException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandParameterException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandPermissionException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandUsageException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.ChatHelper;
import de.st_ddt.crazyutil.ChatHelperExtended;
import de.st_ddt.crazyutil.ListOptionsModder;
import de.st_ddt.crazyutil.ObjectSaveLoadHelper;
import de.st_ddt.crazyutil.VersionComparator;
import de.st_ddt.crazyutil.databases.DatabaseType;
import de.st_ddt.crazyutil.databases.PlayerDataDatabase;
import de.st_ddt.crazyutil.locales.CrazyLocale;
import de.st_ddt.crazyutil.locales.Localized;
import de.st_ddt.crazyutil.metrics.Metrics;
import de.st_ddt.crazyutil.metrics.Metrics.Graph;
import de.st_ddt.crazyutil.metrics.Metrics.Plotter;
import de.st_ddt.crazyutil.modules.login.CrazyLoginSystem;
import de.st_ddt.crazyutil.modules.login.LoginModule;
import de.st_ddt.crazyutil.modules.permissions.PermissionModule;
import de.st_ddt.crazyutil.paramitrisable.BooleanParamitrisable;
import de.st_ddt.crazyutil.paramitrisable.Paramitrisable;

public final class CrazyLogin extends CrazyPlayerDataPlugin<LoginData, LoginPlayerData> implements LoginPlugin<LoginPlayerData>
{

	private static CrazyLogin plugin;
	private final Map<String, Date> antiRequestSpamTable = new HashMap<String, Date>();
	private final Map<String, Integer> loginFailures = new HashMap<String, Integer>();
	private final Map<String, Date> tempBans = new HashMap<String, Date>();
	private final Map<String, Location> saveLoginLocations = new HashMap<String, Location>();
	private final CrazyPluginCommandMainMode modeCommand = new CrazyPluginCommandMainMode(this);
	private CrazyLoginPlayerListener playerListener;
	private CrazyLoginDynamicPlayerListener dynamicPlayerListener;
	private CrazyLoginDynamicVehicleListener dynamicVehicleListener;
	private boolean dynamicHooksRegistered;
	// plugin config
	private boolean alwaysNeedPassword;
	private boolean confirmPassword;
	private boolean dynamicProtection;
	private int autoLogout;
	private int autoKick;
	private long autoTempBan;
	private int autoKickUnregistered;
	private int autoKickLoginFailer;
	private long autoTempBanLoginFailer;
	private boolean autoKickCommandUsers;
	private boolean blockGuestCommands;
	private boolean blockGuestChat;
	private boolean blockGuestJoin;
	private boolean removeGuestData;
	private List<String> commandWhiteList;
	private String uniqueIDKey;
	private boolean disableRegistrations;
	private boolean disableAdminLogin;
	private boolean doNotSpamRequests;
	private boolean doNotSpamRegisterRequests;
	private boolean forceSingleSession;
	private boolean forceSingleSessionSameIPBypass;
	private boolean forceSaveLogin;
	private boolean hideInventory;
	private boolean hidePlayer;
	private boolean delayJoinQuitMessages;
	private boolean useCustomJoinQuitMessages;
	private boolean hidePasswordsFromConsole;
	private Encryptor encryptor;
	private int autoDelete;
	private int maxStoredIPs;
	private int maxOnlinesPerIP;
	private int maxRegistrationsPerIP;
	private boolean pluginCommunicationEnabled;
	private double moveRange;
	private String filterNames;
	private boolean blockDifferentNameCases;
	private int minNameLength;
	private int maxNameLength;
	static
	{
		EncryptHelper.registerAlgorithm("Plaintext", PlainCrypt.class);
		EncryptHelper.registerAlgorithm("MD2", MD2Crypt.class);
		EncryptHelper.registerAlgorithm("MD5", MD5Crypt.class);
		EncryptHelper.registerAlgorithm("SHA-1", SHA_1Crypt.class);
		EncryptHelper.registerAlgorithm("SHA-256", SHA_256Crypt.class);
		EncryptHelper.registerAlgorithm("SHA-512", SHA_512Crypt.class);
		EncryptHelper.registerAlgorithm("SeededMD2", SeededMD2Crypt.class);
		EncryptHelper.registerAlgorithm("SeededMD5", SeededMD5Crypt.class);
		EncryptHelper.registerAlgorithm("SeededSHA-1", SeededSHA_1Crypt.class);
		EncryptHelper.registerAlgorithm("SeededSHA-256", SeededSHA_256Crypt.class);
		EncryptHelper.registerAlgorithm("SeededSHA-512", SeededSHA_512Crypt.class);
		EncryptHelper.registerAlgorithm("CrazyCrypt1", CrazyCrypt1.class);
		EncryptHelper.registerAlgorithm("CrazyCrypt2", CrazyCrypt2.class);
		EncryptHelper.registerAlgorithm("WebCrypt", WebCrypt.class);
		EncryptHelper.registerAlgorithm("Whirlpool", WhirlPoolCrypt.class);
		LoginModule.LOGINSYSTEMS.add(0, CrazyLoginSystem.class);
	}

	public static CrazyLogin getPlugin()
	{
		return plugin;
	}

	public CrazyLogin()
	{
		super();
		registerModes();
		registerFilter();
		registerSorters();
	}

	@Localized("CRAZYLOGIN.MODE.CHANGE $Name$ $Value$")
	private void registerModes()
	{
		modeCommand.addMode(modeCommand.new BooleanFalseMode("alwaysNeedPassword")
		{

			@Override
			public Boolean getValue()
			{
				return alwaysNeedPassword;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				alwaysNeedPassword = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new BooleanFalseMode("confirmPassword")
		{

			@Override
			public Boolean getValue()
			{
				return confirmPassword;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				confirmPassword = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new BooleanFalseMode("dynamicProtection")
		{

			@Override
			public Boolean getValue()
			{
				return dynamicProtection;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				dynamicProtection = newValue;
				if (dynamicProtection)
					unregisterDynamicHooks();
				else
					registerDynamicHooks();
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new BooleanFalseMode("forceSaveLogin")
		{

			@Override
			public Boolean getValue()
			{
				return forceSaveLogin;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				forceSaveLogin = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new BooleanFalseMode("hideInventory")
		{

			@Override
			public Boolean getValue()
			{
				return hideInventory;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				hideInventory = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new BooleanFalseMode("hidePlayer")
		{

			@Override
			public Boolean getValue()
			{
				return hidePlayer;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				hidePlayer = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new BooleanFalseMode("delayJoinQuitMessages")
		{

			@Override
			public Boolean getValue()
			{
				return delayJoinQuitMessages;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				delayJoinQuitMessages = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new BooleanFalseMode("useCustomJoinQuitMessages")
		{

			@Override
			public Boolean getValue()
			{
				return useCustomJoinQuitMessages;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				useCustomJoinQuitMessages = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new BooleanFalseMode("hidePasswordsFromConsole")
		{

			@Override
			public Boolean getValue()
			{
				return hidePasswordsFromConsole;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				hidePasswordsFromConsole = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new BooleanFalseMode("disableRegistrations")
		{

			@Override
			public Boolean getValue()
			{
				return disableRegistrations;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				disableRegistrations = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new BooleanTrueMode("disableAdminLogin")
		{

			@Override
			public Boolean getValue()
			{
				return disableAdminLogin;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				disableAdminLogin = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new BooleanFalseMode("doNotSpamRequests")
		{

			@Override
			public Boolean getValue()
			{
				return doNotSpamRequests;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				doNotSpamRequests = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new BooleanFalseMode("doNotSpamRegisterRequests")
		{

			@Override
			public Boolean getValue()
			{
				return doNotSpamRegisterRequests;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				doNotSpamRegisterRequests = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new BooleanFalseMode("forceSingleSession")
		{

			@Override
			public Boolean getValue()
			{
				return forceSingleSession;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				forceSingleSession = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new BooleanFalseMode("forceSingleSessionSameIPBypass")
		{

			@Override
			public Boolean getValue()
			{
				return forceSingleSessionSameIPBypass;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				forceSingleSessionSameIPBypass = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new IntegerMode("autoLogout")
		{

			@Override
			public void showValue(final CommandSender sender)
			{
				sendLocaleMessage("MODE.CHANGE", sender, name, getValue() == -1 ? "disabled" : getValue() + " seconds");
			}

			@Override
			public Integer getValue()
			{
				return autoLogout;
			}

			@Override
			public void setValue(final Integer newValue) throws CrazyException
			{
				autoLogout = Math.max(newValue, -1);
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new IntegerMode("autoKick")
		{

			@Override
			public void showValue(final CommandSender sender)
			{
				sendLocaleMessage("MODE.CHANGE", sender, name, getValue() == -1 ? "disabled" : getValue() + " seconds");
			}

			@Override
			public Integer getValue()
			{
				return autoKick;
			}

			@Override
			public void setValue(final Integer newValue) throws CrazyException
			{
				autoKick = Math.max(newValue, -1);
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new LongMode("autoTempBan")
		{

			@Override
			public void showValue(final CommandSender sender)
			{
				sendLocaleMessage("MODE.CHANGE", sender, name, getValue() == -1 ? "disabled" : getValue() + " seconds");
			}

			@Override
			public Long getValue()
			{
				return autoTempBan;
			}

			@Override
			public void setValue(final Long newValue) throws CrazyException
			{
				autoTempBan = Math.max(newValue, -1);
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new IntegerMode("autoKickUnregistered")
		{

			@Override
			public void showValue(final CommandSender sender)
			{
				sendLocaleMessage("MODE.CHANGE", sender, name, getValue() == -1 ? "disabled" : getValue() + " seconds");
			}

			@Override
			public Integer getValue()
			{
				return autoKickUnregistered;
			}

			@Override
			public void setValue(final Integer newValue) throws CrazyException
			{
				autoKickUnregistered = Math.max(newValue, -1);
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new IntegerMode("autoKickLoginFailer")
		{

			@Override
			public void showValue(final CommandSender sender)
			{
				sendLocaleMessage("MODE.CHANGE", sender, name, getValue() == -1 ? "disabled" : getValue() + " failed attempts");
			}

			@Override
			public Integer getValue()
			{
				return autoKickLoginFailer;
			}

			@Override
			public void setValue(final Integer newValue) throws CrazyException
			{
				autoKickLoginFailer = Math.max(newValue, -1);
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new LongMode("autoTempBanLoginFailer")
		{

			@Override
			public void showValue(final CommandSender sender)
			{
				sendLocaleMessage("MODE.CHANGE", sender, name, getValue() == -1 ? "disabled" : getValue() + " seconds");
			}

			@Override
			public Long getValue()
			{
				return autoTempBanLoginFailer;
			}

			@Override
			public void setValue(final Long newValue) throws CrazyException
			{
				autoTempBanLoginFailer = Math.max(newValue, -1);
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new BooleanFalseMode("autoKickCommandUsers")
		{

			@Override
			public Boolean getValue()
			{
				return autoKickCommandUsers;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				autoKickCommandUsers = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new BooleanFalseMode("blockGuestCommands")
		{

			@Override
			public Boolean getValue()
			{
				return blockGuestCommands;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				blockGuestCommands = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new BooleanFalseMode("blockGuestChat")
		{

			@Override
			public Boolean getValue()
			{
				return blockGuestChat;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				blockGuestChat = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new BooleanFalseMode("blockGuestJoin")
		{

			@Override
			public Boolean getValue()
			{
				return blockGuestJoin;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				blockGuestJoin = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new BooleanFalseMode("removeGuestData")
		{

			@Override
			public Boolean getValue()
			{
				return removeGuestData;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				removeGuestData = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new IntegerMode("maxStoredIPs")
		{

			@Override
			public Integer getValue()
			{
				return maxStoredIPs;
			}

			@Override
			public void setValue(final Integer newValue) throws CrazyException
			{
				maxStoredIPs = Math.max(newValue, 1);
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new IntegerMode("maxRegistrationsPerIP")
		{

			@Override
			public void showValue(final CommandSender sender)
			{
				sendLocaleMessage("MODE.CHANGE", sender, name, getValue() == -1 ? "disabled" : getValue());
			}

			@Override
			public Integer getValue()
			{
				return maxRegistrationsPerIP;
			}

			@Override
			public void setValue(final Integer newValue) throws CrazyException
			{
				maxRegistrationsPerIP = Math.max(newValue, -1);
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new IntegerMode("maxOnlinesPerIP")
		{

			@Override
			public void showValue(final CommandSender sender)
			{
				sendLocaleMessage("MODE.CHANGE", sender, name, getValue() == -1 ? "disabled" : getValue());
			}

			@Override
			public Integer getValue()
			{
				return maxOnlinesPerIP;
			}

			@Override
			public void setValue(final Integer newValue) throws CrazyException
			{
				maxOnlinesPerIP = Math.max(newValue, -1);
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new Mode<DatabaseType>("saveType", DatabaseType.class)
		{

			@Override
			public DatabaseType getValue()
			{
				return database.getType();
			}

			@Override
			public void setValue(final CommandSender sender, final String... args) throws CrazyException
			{
				if (args.length > 1)
					throw new CrazyCommandUsageException("[SaveType (MYSQL/FLAT/CONFIG)]");
				final String saveType = args[0];
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
					throw new CrazyCommandNoSuchException("SaveType", saveType, "MYSQL", "FLAT", "CONFIG");
				setValue(type);
				showValue(sender);
			}

			@Override
			public void setValue(final DatabaseType newValue) throws CrazyException
			{
				if (database != null)
					if (newValue == database.getType())
						return;
				final PlayerDataDatabase<LoginPlayerData> oldDatabase = database;
				getConfig().set("database.saveType", newValue.toString());
				loadDatabase();
				if (database == null)
					database = oldDatabase;
				else if (oldDatabase != null)
					synchronized (oldDatabase.getDatabaseLock())
					{
						database.saveAll(oldDatabase.getAllEntries());
					}
				save();
			}

			@Override
			public List<String> tab(final String... args)
			{
				final List<String> res = new ArrayList<String>();
				res.add("CONFIG");
				res.add("FLAT");
				res.add("MYSQL");
				return res;
			}
		});
		modeCommand.addMode(modeCommand.new IntegerMode("autoDelete")
		{

			@Override
			public void showValue(final CommandSender sender)
			{
				sendLocaleMessage("MODE.CHANGE", sender, name, getValue() == -1 ? "disabled" : getValue() + " days");
			}

			@Override
			public Integer getValue()
			{
				return autoDelete;
			}

			@SuppressWarnings("deprecation")
			@Override
			public void setValue(final Integer newValue) throws CrazyException
			{
				autoDelete = Math.max(newValue, -1);
				saveConfiguration();
				if (autoDelete != -1)
					getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new DropInactiveAccountsTask(plugin), 20 * 60 * 60, 20 * 60 * 60 * 6);
			}
		});
		modeCommand.addMode(modeCommand.new DoubleMode("moveRange")
		{

			@Override
			public void showValue(final CommandSender sender)
			{
				sendLocaleMessage("MODE.CHANGE", sender, name, getValue() == -1 ? "disabled" : getValue() + " blocks");
			}

			@Override
			public Double getValue()
			{
				return moveRange;
			}

			@Override
			public void setValue(final Double newValue) throws CrazyException
			{
				moveRange = Math.max(newValue, -1);
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new Mode<String>("filterNames", String.class)
		{

			@Override
			public void showValue(final CommandSender sender)
			{
				sendLocaleMessage("MODE.CHANGE", sender, "filterNames", getValue().equals(".") ? "disabled" : getValue());
			}

			@Override
			public String getValue()
			{
				return filterNames;
			}

			@Override
			public void setValue(final CommandSender sender, final String... args) throws CrazyException
			{
				String newFilter = ChatHelper.listingString(" ", args);
				if (newFilter.equalsIgnoreCase("false") || newFilter.equalsIgnoreCase("0") || newFilter.equalsIgnoreCase("off"))
					newFilter = ".";
				else if (newFilter.equalsIgnoreCase("true") || newFilter.equalsIgnoreCase("1") || newFilter.equalsIgnoreCase("on"))
					newFilter = "[a-zA-Z0-9_]";
				setValue(newFilter);
				showValue(sender);
			}

			@Override
			public void setValue(final String newValue) throws CrazyException
			{
				filterNames = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new BooleanFalseMode("blockDifferentNameCases")
		{

			@Override
			public Boolean getValue()
			{
				return blockDifferentNameCases;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				blockDifferentNameCases = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new IntegerMode("minNameLength")
		{

			@Override
			public void showValue(final CommandSender sender)
			{
				sendLocaleMessage("MODE.CHANGE", sender, name, getValue() + " characters");
			}

			@Override
			public Integer getValue()
			{
				return minNameLength;
			}

			@Override
			public void setValue(final Integer newValue) throws CrazyException
			{
				minNameLength = Math.min(Math.max(newValue, 1), 16);
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new IntegerMode("maxNameLength")
		{

			@Override
			public void showValue(final CommandSender sender)
			{
				sendLocaleMessage("MODE.CHANGE", sender, name, getValue() + " characters");
			}

			@Override
			public Integer getValue()
			{
				return maxNameLength;
			}

			@Override
			public void setValue(final Integer newValue) throws CrazyException
			{
				maxNameLength = Math.min(Math.max(newValue, 1), 255);
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new BooleanFalseMode("saveDatabaseOnShutdown")
		{

			@Override
			public Boolean getValue()
			{
				return saveDatabaseOnShutdown;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				saveDatabaseOnShutdown = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(modeCommand.new Mode<Encryptor>("algorithm", Encryptor.class)
		{

			@Override
			public Encryptor getValue()
			{
				return encryptor;
			}

			@Override
			public void setValue(final CommandSender sender, final String... args) throws CrazyException
			{
				final Encryptor encryptor = EncryptHelper.getEncryptor(plugin, args[0], ChatHelperExtended.shiftArray(args, 1));
				if (encryptor == null)
					throw new CrazyCommandNoSuchException("Encryptor", args[0], EncryptHelper.getAlgorithms());
				setValue(encryptor);
				showValue(sender);
			}

			@Override
			public void setValue(final Encryptor newValue) throws CrazyException
			{
				if (encryptor.equals(newValue))
					encryptor = newValue;
				else
					encryptor = new ChangedAlgorithmEncryptor(plugin, newValue, encryptor);
				saveConfiguration();
			}

			@Override
			public List<String> tab(final String... args)
			{
				if (args.length != 1)
					return null;
				final List<String> res = new LinkedList<String>();
				final String arg = args[0].toLowerCase();
				for (final String algo : EncryptHelper.getAlgorithms())
					if (algo.toLowerCase().startsWith(arg))
						res.add(algo);
				return res;
			}
		});
	}

	private void registerFilter()
	{
		playerDataFilters.add(new PlayerDataNameFilter<LoginData>());
		playerDataFilters.add(new PlayerDataFilter<LoginData>("ip", new String[] { "ip" })
		{

			@Override
			public FilterInstance getInstance()
			{
				return new FilterInstance()
				{

					private String ip = null;

					@Override
					public void setParameter(final String parameter) throws CrazyException
					{
						ip = parameter;
					}

					@Override
					public void filter(final Collection<? extends LoginData> datas)
					{
						if (ip != null)
							super.filter(datas);
					}

					@Override
					public boolean filter(final LoginData data)
					{
						return data.hasIP(ip);
					}
				};
			}
		});
		playerDataFilters.add(new PlayerDataFilter<LoginData>("online", new String[] { "on", "online" })
		{

			@Override
			public FilterInstance getInstance()
			{
				return new FilterInstance()
				{

					private Boolean online = null;

					@Override
					public void setParameter(String parameter) throws CrazyException
					{
						parameter = parameter.toLowerCase();
						if (parameter.equals("true"))
							online = true;
						else if (parameter.equals("1"))
							online = true;
						else if (parameter.equals("y"))
							online = true;
						else if (parameter.equals("yes"))
							online = true;
						else if (parameter.equals("false"))
							online = false;
						else if (parameter.equals("0"))
							online = false;
						else if (parameter.equals("n"))
							online = false;
						else if (parameter.equals("no"))
							online = false;
						else if (parameter.equals("*"))
							online = null;
						else
							throw new CrazyCommandParameterException(0, "Boolean (false/true/*)");
					}

					@Override
					public void filter(final Collection<? extends LoginData> datas)
					{
						if (online != null)
							super.filter(datas);
					}

					@Override
					public boolean filter(final LoginData data)
					{
						return online.equals(data.isOnline());
					}
				};
			}
		});
	}

	private void registerSorters()
	{
		playerDataSorters.put("ip", new LoginDataIPComparator());
		final LoginDataComparator lastAction = new LoginDataLastActionComparator();
		playerDataSorters.put("last", lastAction);
		playerDataSorters.put("action", lastAction);
		playerDataSorters.put("lastaction", lastAction);
		playerDataSorters.put("online", lastAction);
		playerDataSorters.put("time", lastAction);
	}

	private void registerCommands()
	{
		final CrazyLoginCommandExecutor passwordCommand = new CrazyLoginCommandPassword(this);
		getCommand("login").setExecutor(new CrazyLoginCommandLogin(this));
		getCommand("adminlogin").setExecutor(new CrazyLoginCommandAdminLogin(this, playerListener));
		getCommand("logout").setExecutor(new CrazyLoginCommandLogout(this));
		getCommand("register").setExecutor(passwordCommand);
		mainCommand.addSubCommand(new CrazyCommandLoginCheck(this, playerCommand), "p", "plr", "player", "players", "account", "accounts");
		mainCommand.addSubCommand(passwordCommand, "pw", "password");
		mainCommand.addSubCommand(new CrazyCommandLoginCheck(this, modeCommand), "mode");
		mainCommand.addSubCommand(new CrazyLoginCommandMainCommands(this), "commands");
		mainCommand.addSubCommand(new CrazyLoginCommandMainDropOldData(this), "dropolddata");
		final CrazyLoginCommandExecutor create = new CrazyLoginCommandPlayerCreate(this);
		final CrazyLoginCommandExecutor changePassword = new CrazyLoginCommandPlayerPassword(this);
		final CrazyLoginCommandExecutor delete = new CrazyLoginCommandPlayerDetachIP(this);
		mainCommand.addSubCommand(new CrazyCommandLoginCheck(this, create), "create");
		mainCommand.addSubCommand(new CrazyCommandLoginCheck(this, changePassword), "chgpw", "changepw", "changepassword");
		mainCommand.addSubCommand(new CrazyCommandLoginCheck(this, delete), "detachip");
		playerCommand.addSubCommand(create, "create");
		playerCommand.addSubCommand(changePassword, "chgpw", "changepw", "changepassword");
		playerCommand.addSubCommand(delete, "detachip");
	}

	private void registerHooks()
	{
		this.playerListener = new CrazyLoginPlayerListener(this);
		final String mcVersion = ChatHelper.getMinecraftVersion();
		if (VersionComparator.compareVersions(mcVersion, "1.3.2") == 1)
			this.dynamicPlayerListener = new CrazyLoginDynamicPlayerListener_142(this, playerListener);
		else if (VersionComparator.compareVersions(mcVersion, "1.2.5") == 1)
			this.dynamicPlayerListener = new CrazyLoginDynamicPlayerListener_132(this, playerListener);
		else
			this.dynamicPlayerListener = new CrazyLoginDynamicPlayerListener_125(this, playerListener);
		this.dynamicVehicleListener = new CrazyLoginDynamicVehicleListener(this);
		final CrazyLoginCrazyListener crazylistener = new CrazyLoginCrazyListener(this, playerListener);
		final PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(playerListener, this);
		pm.registerEvents(crazylistener, this);
		pm.registerEvents(new CrazyLoginWorldListener(this), this);
		registerDynamicHooks();
		final CrazyLoginMessageListener messageListener = new CrazyLoginMessageListener(this);
		final Messenger ms = getServer().getMessenger();
		ms.registerIncomingPluginChannel(this, "CrazyLogin", messageListener);
		ms.registerOutgoingPluginChannel(this, "CrazyLogin");
	}

	public synchronized void registerDynamicHooks()
	{
		if (dynamicHooksRegistered)
			return;
		dynamicHooksRegistered = true;
		final PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(dynamicPlayerListener, this);
		pm.registerEvents(dynamicVehicleListener, this);
		HandlerList.bakeAll();
	}

	public synchronized void unregisterDynamicHooks()
	{
		if (!dynamicProtection)
			return;
		if (!dynamicHooksRegistered)
			return;
		if (!everyoneLoggedIn())
			return;
		dynamicHooksRegistered = false;
		HandlerList.unregisterAll(dynamicPlayerListener);
		HandlerList.unregisterAll(dynamicVehicleListener);
		HandlerList.bakeAll();
	}

	private void registerMetrics()
	{
		final boolean metricsEnabled = getConfig().getBoolean("metrics.enabled", true);
		getConfig().set("metrics.enabled", metricsEnabled);
		if (!metricsEnabled)
			return;
		try
		{
			final Metrics metrics = new Metrics(this);
			final Graph playerstats = metrics.createGraph("Player Stats");
			playerstats.addPlotter(new Plotter("players total")
			{

				@Override
				public int getValue()
				{
					return Bukkit.getOfflinePlayers().length;
				}
			});
			playerstats.addPlotter(new Plotter("accounts total")
			{

				@Override
				public int getValue()
				{
					return getPlayerData().size();
				}
			});
			final Graph passwordMode = metrics.createGraph("Password Mode");
			passwordMode.addPlotter(new Plotter("alwaysNeedPassword")
			{

				@Override
				public int getValue()
				{
					return alwaysNeedPassword ? 1 : 0;
				}
			});
			passwordMode.addPlotter(new Plotter("maybePassword")
			{

				@Override
				public int getValue()
				{
					return alwaysNeedPassword ? 0 : 1;
				}
			});
			final Graph databaseType = metrics.createGraph("Database Type");
			for (final DatabaseType type : DatabaseType.values())
				databaseType.addPlotter(new Plotter(type.toString())
				{

					@Override
					public int getValue()
					{
						return (type == database.getType()) ? 1 : 0;
					}
				});
			metrics.start();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public CrazyLoginDataDatabase getCrazyDatabase()
	{
		return (CrazyLoginDataDatabase) database;
	}

	@Override
	public void onLoad()
	{
		LoginPlugin.LOGINPLUGINPROVIDER.setPlugin(this);
		// org.bukkit.common.login.LoginPlugin.LOGINPROVIDER.setLoginPlugin(new CommonLoginAPIBridge(this));
		plugin = this;
		super.onLoad();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onEnable()
	{
		registerHooks();
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new ScheduledCheckTask(this), 30 * 60 * 20, 15 * 60 * 20);
		super.onEnable();
		registerCommands();
		registerMetrics();
		// OnlinePlayer
		for (final Player player : Bukkit.getOnlinePlayers())
			playerListener.PlayerJoin(player);
	}

	@Override
	public void onDisable()
	{
		// OnlinePlayer
		for (final Player player : Bukkit.getOnlinePlayers())
			playerListener.PlayerQuit2(player);
		super.onDisable();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void loadConfiguration()
	{
		super.loadConfiguration();
		final ConfigurationSection config = getConfig();
		autoLogout = config.getInt("autoLogout", 60 * 60);
		alwaysNeedPassword = config.getBoolean("alwaysNeedPassword", true);
		confirmPassword = config.getBoolean("confirmPassword", false);
		dynamicProtection = config.getBoolean("dynamicProtection", false);
		autoKick = Math.max(config.getInt("autoKick", -1), -1);
		autoTempBan = Math.max(config.getInt("autoTempBan", -1), -1);
		tempBans.clear();
		autoKickUnregistered = Math.max(config.getInt("autoKickUnregistered", config.getInt("kickUnregistered", -1)), -1);
		autoKickLoginFailer = Math.max(config.getInt("autoKickLoginFailer", 3), -1);
		autoTempBanLoginFailer = Math.max(config.getInt("autoTempBanLoginFailer", -1), -1);
		loginFailures.clear();
		autoKickCommandUsers = config.getBoolean("autoKickCommandUsers", false);
		blockGuestCommands = config.getBoolean("blockGuestCommands", true);
		blockGuestChat = config.getBoolean("blockGuestChat", false);
		blockGuestJoin = config.getBoolean("blockGuestJoin", false);
		removeGuestData = config.getBoolean("removeGuestData", false);
		disableRegistrations = config.getBoolean("disableRegistrations", false);
		disableAdminLogin = config.getBoolean("disableAdminLogin", false);
		doNotSpamRequests = config.getBoolean("doNotSpamRequests", false);
		doNotSpamRegisterRequests = config.getBoolean("doNotSpamRegisterRequests", false);
		antiRequestSpamTable.clear();
		commandWhiteList = config.getStringList("commandWhitelist");
		if (isUpdated && !isInstalled)
			if (VersionComparator.compareVersions(previousVersion, "7") == -1)
			{
				final List<String> temp = new ArrayList<String>(commandWhiteList);
				commandWhiteList.clear();
				for (final String entry : temp)
					commandWhiteList.add(entry + ".*");
			}
		forceSingleSession = config.getBoolean("forceSingleSession", true);
		forceSingleSessionSameIPBypass = config.getBoolean("forceSingleSessionSameIPBypass", true);
		forceSaveLogin = config.getBoolean("forceSaveLogin", false);
		for (final World world : Bukkit.getWorlds())
			loadConfigurationForWorld(world);
		hideInventory = config.getBoolean("hideInventory", false);
		hidePlayer = config.getBoolean("hidePlayer", false);
		delayJoinQuitMessages = config.getBoolean("delayJoinQuitMessages", delayJoinQuitMessages);
		useCustomJoinQuitMessages = config.getBoolean("useCustomJoinQuitMessages", true);
		hidePasswordsFromConsole = config.getBoolean("hidePasswordsFromConsole", false);
		maxStoredIPs = config.getInt("maxStoredIPs", 5);
		maxOnlinesPerIP = config.getInt("maxOnlinesPerIP", 3);
		maxRegistrationsPerIP = config.getInt("maxRegistrationsPerIP", 3);
		autoDelete = Math.max(config.getInt("autoDelete", -1), -1);
		if (autoDelete != -1)
			getServer().getScheduler().scheduleAsyncRepeatingTask(this, new DropInactiveAccountsTask(this), 20 * 60 * 60, 20 * 60 * 60 * 6);
		moveRange = config.getDouble("moveRange", 5);
		playerListener.clearMovementBlocker(false);
		filterNames = config.getString("filterNames", "true");
		if (filterNames.equals("false"))
			filterNames = ".";
		else if (filterNames.equals("true"))
			filterNames = "[a-zA-Z0-9_]";
		blockDifferentNameCases = config.getBoolean("blockDifferentNameCases", false);
		minNameLength = Math.min(Math.max(config.getInt("minNameLength", 3), 1), 16);
		maxNameLength = Math.min(Math.max(config.getInt("maxNameLength", 16), minNameLength), 255);
		uniqueIDKey = config.getString("uniqueIDKey");
		pluginCommunicationEnabled = config.getBoolean("pluginCommunicationEnabled", false);
		// Encryptor
		if (isUpdated && !isInstalled && VersionComparator.compareVersions(previousVersion, "7") == -1)
		{
			String algorithm = config.getString("algorithm");
			if (algorithm.startsWith("MD") || algorithm.startsWith("SHA"))
				algorithm = "Seeded" + algorithm;
			encryptor = EncryptHelper.getEncryptor(this, algorithm, config.getConfigurationSection("customEncryptor"));
			config.set("algorithm", null);
			config.set("customEncryptor", null);
		}
		else
			encryptor = EncryptHelper.getEncryptor(this, config.getConfigurationSection("encryptor"));
		if (encryptor == null)
			encryptor = new CrazyCrypt1(this, config);
		// Logger
		logger.createLogChannels(config.getConfigurationSection("logs"), "Join", "Quit", "Register", "Login", "Logout", "LoginFail", "ChatBlocked", "CommandBlocked", "AccessDenied");
	}

	public void loadConfigurationForWorld(final World world)
	{
		final ConfigurationSection saveLoginLocationsConfig = getConfig().getConfigurationSection("saveLoginLocations");
		final Location location = ObjectSaveLoadHelper.loadLocation(saveLoginLocationsConfig.getConfigurationSection(world.getName()), null);
		if (location == null)
			saveLoginLocations.put(world.getName(), world.getSpawnLocation());
		else
		{
			saveLoginLocations.put(world.getName(), location);
			if (location.getWorld() == null)
				location.setWorld(world);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	@Localized({ "CRAZYLOGIN.DATABASE.ACCESSWARN $SaveType$", "CRAZYLOGIN.DATABASE.LOADED $EntryCount$" })
	public void loadDatabase()
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
			consoleLog(ChatColor.RED + "NO SUCH SAVETYPE " + saveType);
		}
		if (type == DatabaseType.CONFIG)
			database = new CrazyLoginConfigurationDatabase(this, config.getConfigurationSection("database.CONFIG"));
		else if (type == DatabaseType.FLAT)
			database = new CrazyLoginFlatDatabase(this, config.getConfigurationSection("database.FLAT"));
		else if (type == DatabaseType.MYSQL)
			database = new CrazyLoginMySQLDatabase(config.getConfigurationSection("database.MYSQL"));
		else if (type == DatabaseType.SQLITE)
			database = new CrazyLoginSQLiteDatabase(config.getConfigurationSection("database.SQLITE"));
		if (database != null)
			try
			{
				database.save(config, "database.");
				database.initialize();
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				database = null;
			}
		if (database == null)
		{
			broadcastLocaleMessage(true, "crazylogin.warndatabase", "DATABASE.ACCESSWARN", saveType);
			Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable()
			{

				@Override
				public void run()
				{
					if (database == null)
						broadcastLocaleMessage(true, "crazylogin.warndatabase", "DATABASE.ACCESSWARN", saveType);
				}
			}, 600, 600);
		}
		else
		{
			dropInactiveAccounts();
			sendLocaleMessage("DATABASE.LOADED", Bukkit.getConsoleSender(), database.getAllEntries().size());
		}
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
		if (database != null)
			database.save(config, "database.");
		config.set("encryptor", null);
		encryptor.save(config, "encryptor.");
		config.set("alwaysNeedPassword", alwaysNeedPassword);
		config.set("confirmPassword", confirmPassword);
		config.set("dynamicProtection", dynamicProtection);
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
		config.set("removeGuestData", removeGuestData);
		config.set("disableRegistrations", disableRegistrations);
		config.set("disableAdminLogin", disableAdminLogin);
		config.set("doNotSpamRequests", doNotSpamRequests);
		config.set("doNotSpamRegisterRequests", doNotSpamRegisterRequests);
		config.set("commandWhitelist", commandWhiteList);
		config.set("uniqueIDKey", uniqueIDKey);
		config.set("forceSingleSession", forceSingleSession);
		config.set("forceSingleSessionSameIPBypass", forceSingleSessionSameIPBypass);
		config.set("forceSaveLogin", forceSaveLogin);
		for (final Entry<String, Location> entry : saveLoginLocations.entrySet())
			ObjectSaveLoadHelper.saveLocation(config, "saveLoginLocations." + entry.getKey() + ".", entry.getValue(), true, true);
		config.set("hideInventory", hideInventory);
		config.set("hidePlayer", hidePlayer);
		config.set("delayJoinQuitMessages", delayJoinQuitMessages);
		config.set("useCustomJoinQuitMessages", useCustomJoinQuitMessages);
		config.set("hidePasswordsFromConsole", hidePasswordsFromConsole);
		config.set("autoDelete", autoDelete);
		config.set("maxStoredIPs", maxStoredIPs);
		config.set("maxOnlinesPerIP", maxOnlinesPerIP);
		config.set("maxRegistrationsPerIP", maxRegistrationsPerIP);
		config.set("pluginCommunicationEnabled", pluginCommunicationEnabled);
		config.set("moveRange", moveRange);
		if (filterNames.equals("."))
			config.set("filterNames", false);
		else
			config.set("filterNames", filterNames);
		config.set("blockDifferentNameCases", blockDifferentNameCases);
		config.set("minNameLength", minNameLength);
		config.set("maxNameLength", maxNameLength);
		super.saveConfiguration();
	}

	public int dropInactiveAccounts()
	{
		if (autoDelete != -1)
			return dropInactiveAccounts(autoDelete);
		return -1;
	}

	public int dropInactiveAccounts(final long age)
	{
		final Date compare = new Date();
		compare.setTime(compare.getTime() - age * 1000 * 60 * 60 * 24);
		return dropInactiveAccounts(compare);
	}

	protected int dropInactiveAccounts(final Date limit)
	{
		if (database == null)
			return 0;
		final LinkedList<String> deletions = new LinkedList<String>();
		synchronized (database.getDatabaseLock())
		{
			for (final LoginPlayerData data : database.getAllEntries())
				if (data.getLastActionTime().before(limit))
					if (!data.isOnline())
						deletions.add(data.getName());
		}
		for (final String name : deletions)
			new CrazyPlayerRemoveEvent(this, name).checkAndCallEvent();
		return deletions.size();
	}

	@Override
	@Localized({ "CRAZYLOGIN.LOGIN.FAILED", "CRAZYLOGIN.KICKED.LOGINFAIL", "CRAZYLOGIN.REGISTER.HEADER", "CRAZYLOGIN.LOGIN.FAILEDWARN $Name$ $IP$", "CRAZYLOGIN.LOGIN.SUCCESS", "CRAZYLOGIN.BROADCAST.JOIN $Name$" })
	public void playerLogin(final Player player, final String password) throws CrazyCommandException
	{
		if (database == null)
			throw new CrazyCommandCircumstanceException("when database is accessible");
		final LoginPlayerData data = database.getEntry(player);
		final CrazyLoginPreLoginEvent<LoginPlayerData> event = new CrazyLoginPreLoginEvent<LoginPlayerData>(this, player, data);
		event.callEvent();
		if (event.isCancelled())
		{
			new CrazyLoginLoginFailEvent<LoginPlayerData>(this, player, data, LoginFailReason.CANCELLED).callAsyncEvent();
			sendLocaleMessage("LOGIN.FAILED", player);
			return;
		}
		if (data == null)
		{
			new CrazyLoginLoginFailEvent<LoginPlayerData>(this, player, data, LoginFailReason.NO_ACCOUNT).callAsyncEvent();
			sendLocaleMessage("REGISTER.HEADER", player);
			return;
		}
		final boolean wasOnline = data.isLoggedIn();
		if (!data.login(password))
		{
			new CrazyLoginLoginFailEvent<LoginPlayerData>(this, player, data, LoginFailReason.WRONG_PASSWORD).callAsyncEvent();
			broadcastLocaleMessage(true, "crazylogin.warnloginfailure", true, "LOGIN.FAILEDWARN", player.getName(), player.getAddress().getAddress().getHostAddress());
			Integer fails = loginFailures.get(player.getName().toLowerCase());
			if (fails == null)
				fails = 0;
			fails++;
			if (fails >= autoKickLoginFailer)
			{
				player.kickPlayer(locale.getLocaleMessage(player, "KICKED.LOGINFAIL"));
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
		new CrazyLoginLoginEvent<LoginPlayerData>(this, player, data).callAsyncEvent();
		sendLocaleMessage("LOGIN.SUCCESS", player);
		logger.log("Login", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " logged in successfully.");
		if (!wasOnline)
		{
			player.setFireTicks(0);
			playerListener.sendPlayerJoinMessage(player);
		}
		playerListener.removeFromMovementBlocker(player);
		playerListener.disableSaveLogin(player);
		playerListener.disableHidenInventory(player);
		playerListener.unhidePlayer(player);
		loginFailures.remove(player.getName().toLowerCase());
		tempBans.remove(player.getAddress().getAddress().getHostAddress());
		player.setMetadata("Authenticated", new Authenticated(this, player));
		if (encryptor instanceof UpdatingEncryptor)
			data.setPassword(password);
		data.addIP(player.getAddress().getAddress().getHostAddress());
		data.notifyAction();
		database.save(data);
		unregisterDynamicHooks();
	}

	@Override
	@Localized({ "CRAZYLOGIN.LOGOUT.SUCCESS", "CRAZYLOGIN.BROADCAST.QUIT $Name$" })
	public void playerLogout(final Player player) throws CrazyCommandException
	{
		if (database == null)
			throw new CrazyCommandCircumstanceException("when database is accessible");
		if (!isLoggedIn(player))
			throw new CrazyCommandPermissionException();
		final LoginPlayerData data = getPlayerData(player);
		if (data != null)
		{
			data.notifyAction();
			data.logout();
			database.save(data);
		}
		player.removeMetadata("Authenticated", this);
		player.kickPlayer(locale.getLanguageEntry("LOGOUT.SUCCESS").getLanguageText(player));
		logger.log("Logout", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " logged out.");
	}

	@Override
	@Localized({ "CRAZYLOGIN.PASSWORDDELETE.SUCCESS", "CRAZYLOGIN.PASSWORDCHANGE.SUCCESS $Password$", "CRAZYLOGIN.BROADCAST.JOIN $Name$" })
	public void playerPassword(final Player player, final String password) throws CrazyCommandException, CrazyLoginException
	{
		if (disableRegistrations)
			throw new CrazyLoginRegistrationsDisabled();
		if (database == null)
			throw new CrazyCommandCircumstanceException("when database is accessible");
		if (password.length() == 0)
		{
			if (alwaysNeedPassword || PermissionModule.hasPermission(player, "crazylogin.requirepassword"))
				throw new CrazyCommandUsageException("<Passwort>" + (confirmPassword ? " <Passwort>" : ""));
			playerListener.removeFromMovementBlocker(player);
			sendLocaleMessage("PASSWORDDELETE.SUCCESS", player);
			deletePlayerData(player);
			return;
		}
		LoginPlayerData data = getPlayerData(player);
		final boolean wasGuest = (data == null);
		if (wasGuest)
		{
			final String ip = player.getAddress().getAddress().getHostAddress();
			final HashSet<LoginPlayerData> associates = getPlayerDatasPerIP(ip);
			if (!PermissionModule.hasPermission(player, "crazylogin.ensureregistration"))
				if (maxRegistrationsPerIP != -1)
					if (associates.size() >= maxRegistrationsPerIP)
						throw new CrazyLoginExceedingMaxRegistrationsPerIPException(maxRegistrationsPerIP, associates);
			final CrazyLoginPreRegisterEvent<LoginPlayerData> event = new CrazyLoginPreRegisterEvent<LoginPlayerData>(this, player, data);
			event.callEvent();
			if (event.isCancelled())
				throw new CrazyCommandPermissionException();
			data = new LoginPlayerData(player);
			tempBans.remove(player.getAddress().getAddress().getHostAddress());
			logger.log("Register", player.getName() + "@" + player.getAddress().getAddress().getHostAddress() + " registered successfully.");
		}
		if (pluginCommunicationEnabled)
			new CrazyLoginPasswordEvent<LoginPlayerData>(this, player, password).callAsyncEvent();
		data.setPassword(password);
		data.login(password);
		sendLocaleMessage("PASSWORDCHANGE.SUCCESS", player, password);
		if (wasGuest)
			if (alwaysNeedPassword)
			{
				player.setFireTicks(0);
				playerListener.sendPlayerJoinMessage(player);
			}
		playerListener.removeFromMovementBlocker(player);
		playerListener.disableSaveLogin(player);
		playerListener.disableHidenInventory(player);
		playerListener.unhidePlayer(player);
		getCrazyDatabase().saveWithPassword(data);
		player.setMetadata("Authenticated", new Authenticated(this, player));
	}

	@Override
	public boolean isLoggedIn(final Player player)
	{
		if (player.hasMetadata("NPC"))
			return true;
		final LoginPlayerData data = getPlayerData(player);
		if (data == null)
			return !alwaysNeedPassword && !PermissionModule.hasPermission(player, "crazylogin.requirepassword");
		return data.isLoggedIn() && player.isOnline();
	}

	@Override
	@Localized({ "CRAZYLOGIN.LOGIN.REQUEST", "CRAZYLOGIN.REGISTER.REQUEST" })
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
		if (hasPlayerData(player))
			sendLocaleMessage("LOGIN.REQUEST", player);
		else
			sendLocaleMessage("REGISTER.REQUEST", player);
	}

	@Override
	public boolean isAlwaysNeedPassword()
	{
		return alwaysNeedPassword;
	}

	public boolean isConfirmPasswordEnabled()
	{
		return confirmPassword;
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
		if (database != null)
			if (autoLogout > 0)
			{
				final Date timeOut = new Date();
				timeOut.setTime(timeOut.getTime() - autoLogout * 1000);
				if (database.isCachedDatabase())
					synchronized (database.getDatabaseLock())
					{
						for (final LoginPlayerData data : database.getAllEntries())
							if (!data.isOnline())
								data.checkTimeOut(timeOut);
					}
				else
				{
					final HashSet<LoginPlayerData> dropping = new HashSet<LoginPlayerData>();
					synchronized (database.getDatabaseLock())
					{
						for (final LoginPlayerData data : database.getAllEntries())
							if (!data.isOnline())
								if (!data.checkTimeOut(timeOut) || !data.isLoggedIn())
									dropping.add(data);
					}
					for (final LoginPlayerData data : dropping)
						database.unloadEntry(data.getName());
					dropping.clear();
				}
			}
	}

	@Override
	public int getAutoKick()
	{
		return autoKick;
	}

	public long getAutoTempBan()
	{
		return autoTempBan;
	}

	@Override
	public int getAutoKickUnregistered()
	{
		return autoKickUnregistered;
	}

	public int getAutoKickLoginFailer()
	{
		return autoKickLoginFailer;
	}

	public long getAutoTempBanLoginFailer()
	{
		return autoTempBanLoginFailer;
	}

	public boolean isAutoKickCommandUsers()
	{
		return autoKickCommandUsers;
	}

	public boolean isBlockingGuestCommandsEnabled()
	{
		return blockGuestCommands;
	}

	@Override
	public boolean isBlockingGuestChatEnabled()
	{
		return blockGuestChat;
	}

	@Override
	public boolean isBlockingGuestJoinEnabled()
	{
		return blockGuestJoin;
	}

	@Override
	public boolean isRemovingGuestDataEnabled()
	{
		return removeGuestData;
	}

	public boolean isTempBanned(final String IP)
	{
		final Date date = tempBans.get(IP);
		if (date == null)
			return false;
		return new Date().before(date);
	}

	public Date getTempBanned(final String IP)
	{
		return tempBans.get(IP);
	}

	public String getTempBannedString(final String IP)
	{
		final Date date = getTempBanned(IP);
		if (date == null)
			return DATETIMEFORMAT.format(new Date(0));
		return DATETIMEFORMAT.format(date);
	}

	public void setTempBanned(final Player player, final long duration)
	{
		setTempBanned(player.getAddress().getAddress().getHostAddress(), duration);
	}

	public void setTempBanned(final String IP, final long duration)
	{
		final Date until = new Date();
		until.setTime(until.getTime() + duration * 1000);
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

	public boolean isAvoidingSpammedRegisterRequestsEnabled()
	{
		return doNotSpamRegisterRequests;
	}

	public boolean isForceSingleSessionEnabled()
	{
		return forceSingleSession;
	}

	public boolean isForceSingleSessionSameIPBypassEnabled()
	{
		return forceSingleSessionSameIPBypass;
	}

	@Override
	public boolean isForceSaveLoginEnabled()
	{
		return forceSaveLogin;
	}

	public Map<String, Location> getSaveLoginLocations()
	{
		return saveLoginLocations;
	}

	public Location getSaveLoginLocations(final World world)
	{
		if (saveLoginLocations.containsKey(world.getName()))
			return saveLoginLocations.get(world.getName());
		else
			return world.getSpawnLocation();
	}

	public Location getSaveLoginLocations(final Player player)
	{
		return getSaveLoginLocations(player.getWorld());
	}

	@Override
	public boolean isHidingInventoryEnabled()
	{
		return hideInventory;
	}

	@Override
	public boolean isHidingPlayerEnabled()
	{
		return hidePlayer;
	}

	public boolean isDelayingJoinQuitMessagesEnabled()
	{
		return delayJoinQuitMessages;
	}

	public boolean isUsingCustomJoinQuitMessagesEnabled()
	{
		return useCustomJoinQuitMessages;
	}

	public boolean isHidingPasswordsFromConsoleEnabled()
	{
		return hidePasswordsFromConsole;
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

	public int getMaxStoredIPs()
	{
		return maxStoredIPs;
	}

	public int getMaxOnlinesPerIP()
	{
		return maxOnlinesPerIP;
	}

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

	public String getNameFilter()
	{
		return filterNames;
	}

	public boolean checkNameChars(final String name)
	{
		return name.matches(filterNames + "+");
	}

	public boolean isBlockingDifferentNameCasesEnabled()
	{
		return blockDifferentNameCases;
	}

	public boolean checkNameCase(final String name)
	{
		if (blockDifferentNameCases)
		{
			final LoginPlayerData data = getPlayerData(name);
			if (data == null)
				return true;
			else
				return data.getName().equals(name);
		}
		else
			return true;
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

	@Override
	public String getUniqueIDKey()
	{
		if (uniqueIDKey == null)
			uniqueIDKey = new CrazyCrypt1(this, (String[]) null).encrypt(getServer().getName(), null, "randomKeyGen" + (Math.random() * Double.MAX_VALUE) + "V:" + getServer().getBukkitVersion() + "'_+'#");
		return uniqueIDKey;
	}

	@Override
	public HashSet<LoginPlayerData> getPlayerDatasPerIP(final String IP)
	{
		final HashSet<LoginPlayerData> res = new HashSet<LoginPlayerData>();
		if (database == null)
			return res;
		synchronized (database.getDatabaseLock())
		{
			for (final LoginPlayerData data : database.getAllEntries())
				if (data.hasIP(IP))
					res.add(data);
		}
		return res;
	}

	@Override
	public final void broadcastLocaleMessage(final boolean console, final String permission, final boolean loggedInOnly, final String localepath, final Object... args)
	{
		broadcastLocaleMessage(console, permission, loggedInOnly, getLocale().getLanguageEntry(localepath), args);
	}

	@Override
	public final void broadcastLocaleRootMessage(final boolean console, final String permission, final boolean loggedInOnly, final String localepath, final Object... args)
	{
		broadcastLocaleMessage(console, permission, loggedInOnly, CrazyLocale.getLocaleHead().getLanguageEntry(localepath), args);
	}

	@Override
	public final void broadcastLocaleMessage(final boolean console, final String permission, final boolean loggedInOnly, final CrazyLocale locale, final Object... args)
	{
		if (permission == null)
			broadcastLocaleMessage(console, new String[] {}, loggedInOnly, locale, args);
		else
			broadcastLocaleMessage(console, new String[] { permission }, loggedInOnly, locale, args);
	}

	@Override
	public final void broadcastLocaleMessage(final boolean console, final String[] permissions, final boolean loggedInOnly, final String localepath, final Object... args)
	{
		broadcastLocaleMessage(console, permissions, loggedInOnly, getLocale().getLanguageEntry(localepath), args);
	}

	@Override
	public final void broadcastLocaleRootMessage(final boolean console, final String[] permissions, final boolean loggedInOnly, final String localepath, final Object... args)
	{
		broadcastLocaleMessage(console, permissions, loggedInOnly, CrazyLocale.getLocaleHead().getLanguageEntry(localepath), args);
	}

	@Override
	public final void broadcastLocaleMessage(final boolean console, final String[] permissions, final boolean loggedInOnly, final CrazyLocale locale, final Object... args)
	{
		if (console)
			sendLocaleMessage(locale, Bukkit.getConsoleSender(), args);
		Player: for (final Player player : Bukkit.getOnlinePlayers())
		{
			for (final String permission : permissions)
				if (!PermissionModule.hasPermission(player, permission))
					continue Player;
			if (loggedInOnly)
				if (!isLoggedIn(player))
					continue;
			sendLocaleMessage(locale, player, args);
		}
	}

	public boolean isDynamicProtectionEnabled()
	{
		return dynamicProtection;
	}

	public boolean isAdminLoginDisabled()
	{
		return disableAdminLogin;
	}

	public boolean everyoneLoggedIn()
	{
		for (final Player player : Bukkit.getOnlinePlayers())
			if (!hasPlayerData(player) || !isLoggedIn(player))
				return false;
		return true;
	}

	@Override
	public ListOptionsModder<LoginData> getPlayerDataListModder()
	{
		return new ListOptionsModder<LoginData>()
		{

			private final BooleanParamitrisable registered = new BooleanParamitrisable(true)
			{

				@Override
				public void setParameter(final String parameter) throws CrazyException
				{
					if (parameter.equals("*"))
						value = null;
					else
						super.setParameter(parameter);
				}
			};

			@Override
			public void modListPreOptions(final Map<String, Paramitrisable> params, final List<LoginData> datas)
			{
				params.put("reg", registered);
				params.put("register", registered);
				params.put("registered", registered);
			}

			@Override
			public String[] modListPostOptions(final List<LoginData> datas, final String[] pipeArgs)
			{
				if (Boolean.FALSE.equals(registered.getValue()))
					datas.clear();
				if (!Boolean.TRUE.equals(registered.getValue()))
					for (final OfflinePlayer offline : getServer().getOfflinePlayers())
						if (!hasPlayerData(offline))
							datas.add(new LoginUnregisteredPlayerData(offline));
				return pipeArgs;
			}
		};
	}
}
