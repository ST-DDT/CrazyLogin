package de.st_ddt.crazylogin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.messaging.Messenger;

import de.st_ddt.crazycore.CrazyCore;
import de.st_ddt.crazylogin.commands.CommandAdminLogin;
import de.st_ddt.crazylogin.commands.CommandAutoLogout;
import de.st_ddt.crazylogin.commands.CommandExecutor;
import de.st_ddt.crazylogin.commands.CommandLogin;
import de.st_ddt.crazylogin.commands.CommandLoginWithAutoLogout;
import de.st_ddt.crazylogin.commands.CommandLogout;
import de.st_ddt.crazylogin.commands.CommandMainCommands;
import de.st_ddt.crazylogin.commands.CommandMainDropOldData;
import de.st_ddt.crazylogin.commands.CommandMainGenerateToken;
import de.st_ddt.crazylogin.commands.CommandPassword;
import de.st_ddt.crazylogin.commands.CommandPlayerCheckPassword;
import de.st_ddt.crazylogin.commands.CommandPlayerCreate;
import de.st_ddt.crazylogin.commands.CommandPlayerDetachIP;
import de.st_ddt.crazylogin.commands.CommandPlayerExpirePassword;
import de.st_ddt.crazylogin.commands.CommandPlayerPassword;
import de.st_ddt.crazylogin.commands.CommandPlayerReverify;
import de.st_ddt.crazylogin.commands.CommandSaveLoginLocation;
import de.st_ddt.crazylogin.commands.CommandTokenLogin;
import de.st_ddt.crazylogin.commands.CrazyCommandLoginCheck;
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
import de.st_ddt.crazylogin.data.Token;
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
import de.st_ddt.crazylogin.exceptions.CrazyLoginRegistrationsDisabled;
import de.st_ddt.crazylogin.exceptions.PasswordRejectedException;
import de.st_ddt.crazylogin.exceptions.PasswordRejectedLengthException;
import de.st_ddt.crazylogin.listener.CrazyListener;
import de.st_ddt.crazylogin.listener.DynamicPlayerListener;
import de.st_ddt.crazylogin.listener.DynamicPlayerListener_1_4_2;
import de.st_ddt.crazylogin.listener.DynamicPlayerListener_1_5;
import de.st_ddt.crazylogin.listener.DynamicVehicleListener;
import de.st_ddt.crazylogin.listener.MessageListener;
import de.st_ddt.crazylogin.listener.PlayerListener;
import de.st_ddt.crazylogin.listener.WorldListener;
import de.st_ddt.crazylogin.metadata.Authenticated;
import de.st_ddt.crazylogin.tasks.DropInactiveAccountsTask;
import de.st_ddt.crazylogin.tasks.ScheduledCheckTask;
import de.st_ddt.crazylogin.util.temp.VersionHelper;
import de.st_ddt.crazyplugin.CrazyPlayerDataPlugin;
import de.st_ddt.crazyplugin.data.PlayerDataFilter;
import de.st_ddt.crazyplugin.data.PlayerDataNameFilter;
import de.st_ddt.crazyplugin.events.CrazyPlayerRemoveEvent;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandCircumstanceException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandErrorException;
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
import de.st_ddt.crazyutil.PreSetList;
import de.st_ddt.crazyutil.databases.DatabaseType;
import de.st_ddt.crazyutil.databases.PlayerDataDatabase;
import de.st_ddt.crazyutil.locales.CrazyLocale;
import de.st_ddt.crazyutil.metrics.Metrics;
import de.st_ddt.crazyutil.metrics.Metrics.Graph;
import de.st_ddt.crazyutil.metrics.Metrics.Plotter;
import de.st_ddt.crazyutil.modes.BooleanFalseMode;
import de.st_ddt.crazyutil.modes.BooleanTrueMode;
import de.st_ddt.crazyutil.modes.DoubleMode;
import de.st_ddt.crazyutil.modes.DurationMode;
import de.st_ddt.crazyutil.modes.IntegerMode;
import de.st_ddt.crazyutil.modes.LongMode;
import de.st_ddt.crazyutil.modes.Mode;
import de.st_ddt.crazyutil.modules.login.CrazyLoginSystem;
import de.st_ddt.crazyutil.modules.login.LoginModule;
import de.st_ddt.crazyutil.paramitrisable.BooleanParamitrisable;
import de.st_ddt.crazyutil.paramitrisable.Paramitrisable;
import de.st_ddt.crazyutil.source.Localized;
import de.st_ddt.crazyutil.source.LocalizedVariable;
import de.st_ddt.crazyutil.source.Permission;
import de.st_ddt.crazyutil.source.PermissionVariable;

@LocalizedVariable(variables = { "CRAZYPLUGIN", "CRAZYPLAYERDATAPLUGIN" }, values = { "CRAZYLOGIN", "CRAZYLOGIN" })
@PermissionVariable(variables = { "CRAZYPLUGIN", "CRAZYPLAYERDATAPLUGIN" }, values = { "CRAZYLOGIN", "CRAZYLOGIN" })
public final class CrazyLogin extends CrazyPlayerDataPlugin<LoginData, LoginPlayerData> implements LoginPlugin<LoginPlayerData>
{

	private static CrazyLogin plugin;
	private final Map<String, Date> antiRequestSpamTable = new HashMap<String, Date>();
	private final Map<String, Integer> loginFailuresPerIP = new HashMap<String, Integer>();
	/**
	 * Number of illegal command executions with the given IP.
	 */
	private final Map<String, Integer> illegalCommandUsesPerIP = new HashMap<String, Integer>();
	private final Map<String, Date> tempBans = new HashMap<String, Date>();
	private final Map<String, Token> loginTokens = new HashMap<String, Token>();
	private final Set<Player> playerAutoLogouts = new HashSet<Player>();
	private final Map<String, Location> saveLoginLocations = new HashMap<String, Location>();
	private PlayerListener playerListener;
	private DynamicPlayerListener dynamicPlayerListener;
	private DynamicVehicleListener dynamicVehicleListener;
	private MessageListener messageListener;
	private boolean dynamicHooksRegistered;
	// plugin config
	private boolean alwaysNeedPassword;
	private boolean confirmNewPassword;
	private boolean confirmWithOldPassword;
	private boolean dynamicProtection;
	private boolean hideWarnings;
	private int autoLogout;
	private int autoKick;
	private long autoTempBan;
	private int autoKickUnregistered;
	private int autoKickLoginFailer;
	private long autoTempBanLoginFailer;
	private int autoKickCommandUsers;
	private long autoTempBanCommandUsers;
	private boolean blockGuestCommands;
	private boolean blockGuestChat;
	private boolean blockGuestJoin;
	private boolean removeGuestData;
	private List<String> commandWhiteList;
	private String uniqueIDKey;
	private boolean disableRegistrations;
	private boolean disableAdminLogin;
	private boolean disableTokenLogin;
	private boolean doNotSpamAuthRequests;
	private boolean doNotSpamRegisterRequests;
	private long delayAuthRequests;
	private long repeatAuthRequests;
	private boolean forceSingleSession;
	private boolean forceSingleSessionSameIPBypass;
	private long delayPreRegisterSecurity;
	private long delayPreLoginSecurity;
	private boolean saveLoginEnabled;
	private boolean forceSaveLogin;
	private boolean hideInventory;
	private boolean hidePlayer;
	private boolean hideChat;
	private boolean delayJoinQuitMessages;
	private boolean useCustomJoinQuitMessages;
	private boolean hidePasswordsFromConsole;
	private Encryptor encryptor;
	private int minPasswordLength;
	private int protectedAccountMinPasswordLength;
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
		// Encryption Algorithms
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
		// LoginSystem
		LoginModule.registerLoginSystem(CrazyLoginSystem.class);
		LoginModule.clear();
	}

	public static CrazyLogin getPlugin()
	{
		return plugin;
	}

	public CrazyLogin()
	{
		super();
		registerPreSetLists();
		registerModes();
		registerFilter();
		registerSorters();
	}

	private void registerPreSetLists()
	{
		new PreSetList("login_verified")
		{

			@Override
			public List<String> getList()
			{
				final List<String> names = new ArrayList<String>();
				for (final LoginPlayerData data : getOnlinePlayerDatas())
					if (data.isLoggedIn())
						names.add(data.getName());
				return names;
			}
		};
		new PreSetList("login_notverified")
		{

			@Override
			public List<String> getList()
			{
				final List<String> names = new ArrayList<String>();
				for (final LoginPlayerData data : getOnlinePlayerDatas())
					if (!data.isLoggedIn())
						names.add(data.getName());
				return names;
			}
		};
		new PreSetList("login_guest")
		{

			@Override
			public List<String> getList()
			{
				final List<String> names = new ArrayList<String>();
				for (final Player player : Bukkit.getOnlinePlayers())
					if (!hasPlayerData(player))
						names.add(player.getName());
				return names;
			}
		};
	}

	@Localized("CRAZYLOGIN.MODE.CHANGE $Name$ $Value$")
	private void registerModes()
	{
		modeCommand.addMode(new BooleanFalseMode(this, "alwaysNeedPassword")
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
		modeCommand.addMode(new BooleanFalseMode(this, "confirmNewPassword")
		{

			@Override
			public Boolean getValue()
			{
				return confirmNewPassword;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				confirmNewPassword = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(new BooleanFalseMode(this, "confirmWithOldPassword")
		{

			@Override
			public Boolean getValue()
			{
				return confirmWithOldPassword;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				confirmWithOldPassword = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(new BooleanFalseMode(this, "dynamicProtection")
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
		modeCommand.addMode(new BooleanFalseMode(this, "hideWarnings")
		{

			@Override
			public Boolean getValue()
			{
				return hideWarnings;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				hideWarnings = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(new DurationMode(this, "delayPreRegisterSecurity")
		{

			@Override
			public Long getValue()
			{
				return delayPreRegisterSecurity * 50;
			}

			@Override
			public void setValue(final Long newValue) throws CrazyException
			{
				delayPreRegisterSecurity = Math.max(newValue / 50, -1);
				saveConfiguration();
			}
		});
		modeCommand.addMode(new DurationMode(this, "delayPreLoginSecurity")
		{

			@Override
			public Long getValue()
			{
				return delayPreLoginSecurity * 50;
			}

			@Override
			public void setValue(final Long newValue) throws CrazyException
			{
				delayPreLoginSecurity = Math.max(newValue / 50, -1);
				saveConfiguration();
			}
		});
		modeCommand.addMode(new BooleanFalseMode(this, "saveLoginEnabled")
		{

			@Override
			public Boolean getValue()
			{
				return saveLoginEnabled;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				saveLoginEnabled = newValue;
				if (newValue)
					saveLoginEnabled = true;
				else
				{
					saveLoginEnabled = false;
					forceSaveLogin = false;
				}
				saveConfiguration();
			}
		});
		modeCommand.addMode(new BooleanFalseMode(this, "forceSaveLogin")
		{

			@Override
			public Boolean getValue()
			{
				return forceSaveLogin;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				if (newValue)
				{
					saveLoginEnabled = true;
					forceSaveLogin = true;
				}
				else
					forceSaveLogin = false;
				saveConfiguration();
			}
		});
		modeCommand.addMode(new BooleanFalseMode(this, "hideInventory")
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
		modeCommand.addMode(new BooleanFalseMode(this, "hidePlayer")
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
		modeCommand.addMode(new BooleanFalseMode(this, "hideChat")
		{

			@Override
			public Boolean getValue()
			{
				return hideChat;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				hideChat = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(new BooleanFalseMode(this, "delayJoinQuitMessages")
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
		modeCommand.addMode(new BooleanFalseMode(this, "useCustomJoinQuitMessages")
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
		modeCommand.addMode(new BooleanFalseMode(this, "hidePasswordsFromConsole")
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
		modeCommand.addMode(new BooleanFalseMode(this, "disableRegistrations")
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
		modeCommand.addMode(new BooleanTrueMode(this, "disableAdminLogin")
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
		modeCommand.addMode(new BooleanTrueMode(this, "disableTokenLogin")
		{

			@Override
			public Boolean getValue()
			{
				return disableTokenLogin;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				disableTokenLogin = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(new BooleanFalseMode(this, "doNotSpamAuthRequests")
		{

			@Override
			public Boolean getValue()
			{
				return doNotSpamAuthRequests;
			}

			@Override
			public void setValue(final Boolean newValue) throws CrazyException
			{
				doNotSpamAuthRequests = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(new BooleanFalseMode(this, "doNotSpamRegisterRequests")
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
		modeCommand.addMode(new DurationMode(this, "delayAuthRequests")
		{

			@Override
			public Long getValue()
			{
				return delayAuthRequests * 50;
			}

			@Override
			public void setValue(final Long newValue) throws CrazyException
			{
				delayAuthRequests = Math.max(newValue / 50, 0);
				saveConfiguration();
			}
		});
		modeCommand.addMode(new DurationMode(this, "repeatAuthRequests")
		{

			@Override
			public Long getValue()
			{
				return repeatAuthRequests * 50;
			}

			@Override
			public void setValue(final Long newValue) throws CrazyException
			{
				repeatAuthRequests = Math.max(newValue / 50, 0);
				saveConfiguration();
			}
		});
		modeCommand.addMode(new BooleanFalseMode(this, "forceSingleSession")
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
		modeCommand.addMode(new BooleanFalseMode(this, "forceSingleSessionSameIPBypass")
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
		modeCommand.addMode(new IntegerMode(this, "autoLogout")
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
		modeCommand.addMode(new IntegerMode(this, "autoKick")
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
		modeCommand.addMode(new LongMode(this, "autoTempBan")
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
		modeCommand.addMode(new IntegerMode(this, "autoKickUnregistered")
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
		modeCommand.addMode(new IntegerMode(this, "autoKickLoginFailer")
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
		modeCommand.addMode(new LongMode(this, "autoTempBanLoginFailer")
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
		modeCommand.addMode(new IntegerMode(this, "autoKickCommandUsers")
		{

			@Override
			public Integer getValue()
			{
				return autoKickCommandUsers;
			}

			@Override
			public void setValue(final Integer newValue) throws CrazyException
			{
				autoKickCommandUsers = newValue;
				saveConfiguration();
			}
		});
		modeCommand.addMode(new LongMode(this, "autoTempBanCommandUsers")
		{

			@Override
			public void showValue(final CommandSender sender)
			{
				sendLocaleMessage("MODE.CHANGE", sender, name, getValue() == -1 ? "disabled" : getValue() + " seconds");
			}

			@Override
			public Long getValue()
			{
				return autoTempBanCommandUsers;
			}

			@Override
			public void setValue(final Long newValue) throws CrazyException
			{
				autoTempBanCommandUsers = Math.max(newValue, -1);
				saveConfiguration();
			}
		});
		modeCommand.addMode(new BooleanFalseMode(this, "blockGuestCommands")
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
		modeCommand.addMode(new BooleanFalseMode(this, "blockGuestChat")
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
		modeCommand.addMode(new BooleanFalseMode(this, "blockGuestJoin")
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
		modeCommand.addMode(new BooleanFalseMode(this, "removeGuestData")
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
		modeCommand.addMode(new IntegerMode(this, "maxStoredIPs")
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
		modeCommand.addMode(new IntegerMode(this, "maxRegistrationsPerIP")
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
		modeCommand.addMode(new IntegerMode(this, "maxOnlinesPerIP")
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
		modeCommand.addMode(new Mode<DatabaseType>(this, "saveType", DatabaseType.class)
		{

			@Override
			@Localized("CRAZYLOGIN.PLUGININFO.DATABASEENTRIES $EntryCount$")
			public void showValue(final CommandSender sender)
			{
				super.showValue(sender);
				if (database != null)
					sendLocaleMessage("PLUGININFO.DATABASEENTRIES", sender, database.getAllEntries().size());
			}

			@Override
			public DatabaseType getValue()
			{
				return database.getType();
			}

			@Override
			public void setValue(final CommandSender sender, final String... args) throws CrazyException
			{
				if (args.length > 1)
					throw new CrazyCommandUsageException("[SaveType (CONFIG/FLAT/MYSQL/SQLITE)]");
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
					throw new CrazyCommandNoSuchException("SaveType", saveType, "CONFIG", "FLAT", "MYSQL", "SQLITE");
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
				res.add("SQLITE");
				return res;
			}
		});
		modeCommand.addMode(new IntegerMode(this, "autoDelete")
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

			@Override
			public void setValue(final Integer newValue) throws CrazyException
			{
				autoDelete = Math.max(newValue, -1);
				saveConfiguration();
				if (autoDelete != -1)
					getServer().getScheduler().runTaskTimerAsynchronously(plugin, new DropInactiveAccountsTask(CrazyLogin.this), 20 * 60 * 60, 20 * 60 * 60 * 6);
			}
		});
		modeCommand.addMode(new DoubleMode(this, "moveRange")
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
		modeCommand.addMode(new Mode<String>(this, "filterNames", String.class)
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
		modeCommand.addMode(new BooleanFalseMode(this, "blockDifferentNameCases")
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
		modeCommand.addMode(new IntegerMode(this, "minNameLength")
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
		modeCommand.addMode(new IntegerMode(this, "maxNameLength")
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
		modeCommand.addMode(new BooleanFalseMode(this, "saveDatabaseOnShutdown")
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
		modeCommand.addMode(new Mode<Encryptor>(this, "algorithm", Encryptor.class)
		{

			@Override
			public Encryptor getValue()
			{
				return encryptor;
			}

			@Override
			public void setValue(final CommandSender sender, final String... args) throws CrazyException
			{
				final Encryptor encryptor = EncryptHelper.getEncryptor(CrazyLogin.this, args[0], ChatHelperExtended.shiftArray(args, 1));
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
					encryptor = new ChangedAlgorithmEncryptor(CrazyLogin.this, newValue, encryptor);
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
		modeCommand.addMode(new IntegerMode(this, "minPasswordLength")
		{

			@Override
			public void setValue(final Integer newValue) throws CrazyException
			{
				minPasswordLength = newValue;
			}

			@Override
			public Integer getValue()
			{
				return minPasswordLength;
			}
		});
		modeCommand.addMode(new IntegerMode(this, "protectedAccountMinPasswordLength")
		{

			@Override
			public void setValue(final Integer newValue) throws CrazyException
			{
				protectedAccountMinPasswordLength = newValue;
			}

			@Override
			public Integer getValue()
			{
				return protectedAccountMinPasswordLength;
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
					public boolean isActive()
					{
						return ip != null;
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
					public boolean isActive()
					{
						return online != null;
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

	protected void registerCommands()
	{
		final CommandExecutor passwordCommand = new CommandPassword(this);
		registerCommand2("login", new CommandLogin(this));
		registerCommand2("loginonce", new CommandLoginWithAutoLogout(this));
		registerCommand2("adminlogin", new CommandAdminLogin(this, playerListener));
		registerCommand2("tokenlogin", new CommandTokenLogin(this, playerListener));
		registerCommand2("autologout", new CommandAutoLogout(this));
		registerCommand2("logout", new CommandLogout(this));
		registerCommand2("register", passwordCommand);
		mainCommand.addSubCommand(new CrazyCommandLoginCheck(this, playerCommand), "p", "plr", "player", "players", "account", "accounts");
		mainCommand.addSubCommand(passwordCommand, "pw", "password");
		mainCommand.addSubCommand(new CommandMainGenerateToken(this), "generatetoken");
		mainCommand.addSubCommand(new CrazyCommandLoginCheck(this, modeCommand), "mode");
		mainCommand.addSubCommand(new CommandMainCommands(this), "commands");
		mainCommand.addSubCommand(new CommandSaveLoginLocation(this), "sll", "saveloginlocation");
		mainCommand.addSubCommand(new CommandMainDropOldData(this), "dropolddata");
		final CommandExecutor create = new CommandPlayerCreate(this);
		final CommandExecutor changePassword = new CommandPlayerPassword(this);
		final CommandExecutor detachip = new CommandPlayerDetachIP(this);
		final CommandExecutor reverify = new CommandPlayerReverify(this);
		final CommandExecutor expire = new CommandPlayerExpirePassword(this);
		final CommandExecutor checkPassword = new CommandPlayerCheckPassword(this);
		mainCommand.addSubCommand(new CrazyCommandLoginCheck(this, create), "create");
		mainCommand.addSubCommand(new CrazyCommandLoginCheck(this, changePassword), "chgpw", "changepw", "changepassword");
		mainCommand.addSubCommand(new CrazyCommandLoginCheck(this, detachip), "detachip");
		mainCommand.addSubCommand(new CrazyCommandLoginCheck(this, reverify), "reverify");
		mainCommand.addSubCommand(new CrazyCommandLoginCheck(this, reverify), "expire");
		mainCommand.addSubCommand(new CrazyCommandLoginCheck(this, checkPassword), "chkpw", "checkpw", "checkpassword");
		playerCommand.addSubCommand(create, "create");
		playerCommand.addSubCommand(changePassword, "chgpw", "changepw", "changepassword");
		playerCommand.addSubCommand(detachip, "detachip");
		playerCommand.addSubCommand(reverify, "reverify");
		playerCommand.addSubCommand(expire, "expire");
		playerCommand.addSubCommand(checkPassword, "chkpw", "checkpw", "checkpassword");
	}
	

	public final void registerCommand2(final String commandName, final CommandExecutor commandExecutor)
	{
		final PluginCommand command = getCommand(commandName);
		if (command != null)
			command.setExecutor(commandExecutor);
	}

	protected void registerHooks()
	{
		this.playerListener = new PlayerListener(this);
		if (VersionHelper.hasRequiredVersion("1.4.7"))
			this.dynamicPlayerListener = new DynamicPlayerListener_1_5(this, playerListener);
		else
			this.dynamicPlayerListener = new DynamicPlayerListener_1_4_2(this, playerListener);
		this.dynamicVehicleListener = new DynamicVehicleListener(this);
		final CrazyListener crazylistener = new CrazyListener(this, playerListener);
		final PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(playerListener, this);
		pm.registerEvents(crazylistener, this);
		pm.registerEvents(new WorldListener(this), this);
		registerDynamicHooks();
		messageListener = new MessageListener(this);
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
			final long limit = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 14;
			int temp = 0;
			for (final OfflinePlayer data : Bukkit.getOfflinePlayers())
				if (data.getLastPlayed() > limit)
					temp++;
			final int activePlayers = temp;
			playerstats.addPlotter(new Plotter("active players")
			{

				@Override
				public int getValue()
				{
					return activePlayers;
				}
			});
			final int players = Bukkit.getOfflinePlayers().length;
			playerstats.addPlotter(new Plotter("players total")
			{

				@Override
				public int getValue()
				{
					return players;
				}
			});
			temp = 0;
			final Date limitDate = new Date(limit);
			for (final LoginPlayerData data : getPlayerData())
				if (data.getLastActionTime().after(limitDate))
					temp++;
			final int activeAccounts = temp;
			playerstats.addPlotter(new Plotter("active accounts")
			{

				@Override
				public int getValue()
				{
					return activeAccounts;
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
						if (database == null)
							return 0;
						else
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
		plugin = this;
		super.onLoad();
	}

	@Override
	public void onEnable()
	{
		registerHooks();
		super.onEnable();
		getServer().getScheduler().runTaskTimerAsynchronously(this, new ScheduledCheckTask(this), 30 * 60 * 20, 15 * 60 * 20);
		registerMetrics();
		registerCommands();
	}

	@Override
	public void onDisable()
	{
		// OnlinePlayer
		for (final Player player : Bukkit.getOnlinePlayers())
			playerListener.PlayerQuit2(player);
		super.onDisable();
	}

	@Override
	public void loadConfiguration()
	{
		super.loadConfiguration();
		ConfigurationSection config = getConfig();
		autoLogout = config.getInt("autoLogout", 60 * 60);
		alwaysNeedPassword = config.getBoolean("alwaysNeedPassword", true);
		confirmNewPassword = config.getBoolean("confirmNewPassword", config.getBoolean("confirmPassowrd", false));
		confirmWithOldPassword = config.getBoolean("confirmWithOldPassword", false);
		dynamicProtection = config.getBoolean("dynamicProtection", false);
		hideWarnings = config.getBoolean("hideWarnings", false);
		autoKick = Math.max(config.getInt("autoKick", -1), -1);
		autoTempBan = Math.max(config.getInt("autoTempBan", -1), -1);
		tempBans.clear();
		autoKickUnregistered = Math.max(config.getInt("autoKickUnregistered", config.getInt("kickUnregistered", -1)), -1);
		autoKickLoginFailer = Math.max(config.getInt("autoKickLoginFailer", 3), -1);
		autoTempBanLoginFailer = Math.max(config.getInt("autoTempBanLoginFailer", -1), -1);
		loginFailuresPerIP.clear();
		autoKickCommandUsers = Math.max(config.getInt("autoKickCommandUsers", config.getBoolean("autoKickCommandUsers", false) ? 1 : -1), -1);
		autoTempBanCommandUsers = Math.max(config.getInt("autoTempBanCommandUsers", -1), -1);
		illegalCommandUsesPerIP.clear();
		blockGuestCommands = config.getBoolean("blockGuestCommands", true);
		blockGuestChat = config.getBoolean("blockGuestChat", false);
		blockGuestJoin = config.getBoolean("blockGuestJoin", false);
		removeGuestData = config.getBoolean("removeGuestData", false);
		disableRegistrations = config.getBoolean("disableRegistrations", false);
		disableAdminLogin = config.getBoolean("disableAdminLogin", true);
		disableTokenLogin = config.getBoolean("disableTokenLogin", true);
		loginTokens.clear();
		doNotSpamAuthRequests = config.getBoolean("doNotSpamAuthRequests", false);
		doNotSpamRegisterRequests = config.getBoolean("doNotSpamRegisterRequests", false);
		delayAuthRequests = Math.max(config.getLong("delayAuthRequests", 0), 0);
		repeatAuthRequests = Math.max(config.getLong("repeatAuthRequests", 200), 0);
		antiRequestSpamTable.clear();
		commandWhiteList = config.getStringList("commandWhitelist");
		forceSingleSession = config.getBoolean("forceSingleSession", true);
		forceSingleSessionSameIPBypass = config.getBoolean("forceSingleSessionSameIPBypass", true);
		if (config.getBoolean("delayPreRegisterSecurity", true))
			delayPreRegisterSecurity = config.getInt("delayPreRegisterSecurity", 5);
		else
			delayPreRegisterSecurity = 0;
		if (config.getBoolean("delayPreLoginSecurity", true))
			delayPreLoginSecurity = config.getInt("delayPreLoginSecurity", 0);
		else
			delayPreLoginSecurity = 0;
		saveLoginEnabled = config.getBoolean("saveLoginEnabled", true);
		forceSaveLogin = saveLoginEnabled && config.getBoolean("forceSaveLogin", false);
		for (final World world : Bukkit.getWorlds())
			loadConfigurationForWorld(world);
		hideInventory = config.getBoolean("hideInventory", false);
		hidePlayer = config.getBoolean("hidePlayer", false);
		hideChat = config.getBoolean("hideChat", false);
		delayJoinQuitMessages = config.getBoolean("delayJoinQuitMessages", delayJoinQuitMessages);
		useCustomJoinQuitMessages = config.getBoolean("useCustomJoinQuitMessages", true);
		hidePasswordsFromConsole = config.getBoolean("hidePasswordsFromConsole", false);
		maxStoredIPs = config.getInt("maxStoredIPs", 5);
		maxOnlinesPerIP = config.getInt("maxOnlinesPerIP", 3);
		maxRegistrationsPerIP = config.getInt("maxRegistrationsPerIP", 3);
		autoDelete = Math.max(config.getInt("autoDelete", -1), -1);
		if (autoDelete != -1)
			getServer().getScheduler().runTaskTimerAsynchronously(this, new DropInactiveAccountsTask(this), 20 * 60 * 60, 20 * 60 * 60 * 6);
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
		encryptor = EncryptHelper.getEncryptor(this, config.getConfigurationSection("encryptor"));
		if (encryptor == null)
		{
			consoleLog("Could not find an active encryptor.");
			consoleLog("Defaulting to CrazyCrypt1");
			encryptor = new CrazyCrypt1(this, config);
		}
		minPasswordLength = config.getInt("minPasswordLength", 3);
		protectedAccountMinPasswordLength = config.getInt("protectedAccountMinPasswordLength", 7);
		 this.logger.createLogChannels(config.getConfigurationSection("logs"), getLogChannels());
	}

	protected String[] getLogChannels()
	{
		return new String[] { "Join", "Quit", "Login", "Account", "Logout", "LoginFail", "ChatBlocked", "CommandBlocked", "AccessDenied" };
	}

	public void loadConfigurationForWorld(final World world)
	{
		final Location location = ObjectSaveLoadHelper.loadLocation(getConfig().getConfigurationSection("saveLoginLocations." + world.getName()), null);
		if (location == null)
			saveLoginLocations.put(world.getName(), world.getSpawnLocation());
		else
		{
			saveLoginLocations.put(world.getName(), location);
			if (location.getWorld() == null)
				location.setWorld(world);
		}
	}

	@Override
	@Permission("crazylogin.warndatabase")
	@Localized({ "CRAZYLOGIN.DATABASE.ACCESSWARN $SaveType$", "CRAZYLOGIN.DATABASE.LOADED $EntryCount$" })
	public void loadDatabase()
	{
		ConfigurationSection config = getConfig();
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
		if (database == null) {
			broadcastLocaleMessage(true, "crazylogin.warndatabase", "DATABASE.ACCESSWARN", saveType);
			getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {

				@Override
				public void run() {
					if (database == null)
						broadcastLocaleMessage(true, "crazylogin.warndatabase", "DATABASE.ACCESSWARN", saveType);
				}
			}, 600, 600);
		}
		else {
			sendLocaleMessage("DATABASE.LOADED", Bukkit.getConsoleSender(), database.size());
		}
		// OnlinePlayer
		for (final Player player : Bukkit.getOnlinePlayers()) {
			playerListener.PlayerJoin(player);
			messageListener.sendPluginMessage(player, "A_State " + (hasPlayerData(player) ? "1" : "0") + " 0");
		}
	}

	@Override
	public void saveConfiguration()
	{
		super.saveConfiguration();
		ConfigurationSection config = getConfig();
		config.set("encryptor", null);
		encryptor.save(config, "encryptor.");
		config.set("minPasswordLength", minPasswordLength);
		config.set("protectedAccountMinPasswordLength", protectedAccountMinPasswordLength);
		config.set("alwaysNeedPassword", alwaysNeedPassword);
		config.set("confirmNewPassword", confirmNewPassword);
		config.set("confirmPassword", null);
		config.set("confirmWithOldPassword", confirmWithOldPassword);
		config.set("dynamicProtection", dynamicProtection);
		config.set("hideWarnings", hideWarnings);
		config.set("autoLogout", autoLogout);
		config.set("autoKick", autoKick);
		config.set("autoTempBan", autoTempBan);
		config.set("autoKickUnregistered", autoKickUnregistered);
		config.set("autoKickLoginFailer", autoKickLoginFailer);
		config.set("autoTempBanLoginFailer", autoTempBanLoginFailer);
		config.set("autoKickCommandUsers", autoKickCommandUsers);
		config.set("autoTempBanCommandUsers", autoTempBanCommandUsers);
		config.set("blockGuestCommands", blockGuestCommands);
		config.set("blockGuestChat", blockGuestChat);
		config.set("blockGuestJoin", blockGuestJoin);
		config.set("removeGuestData", removeGuestData);
		config.set("disableRegistrations", disableRegistrations);
		config.set("disableAdminLogin", disableAdminLogin);
		config.set("disableTokenLogin", disableTokenLogin);
		config.set("doNotSpamAuthRequests", doNotSpamAuthRequests);
		config.set("doNotSpamRegisterRequests", doNotSpamRegisterRequests);
		config.set("delayAuthRequests", delayAuthRequests);
		config.set("repeatAuthRequests", repeatAuthRequests);
		config.set("commandWhitelist", commandWhiteList);
		config.set("uniqueIDKey", uniqueIDKey);
		config.set("forceSingleSession", forceSingleSession);
		config.set("forceSingleSessionSameIPBypass", forceSingleSessionSameIPBypass);
		config.set("delayPreRegisterSecurity", delayPreRegisterSecurity <= 0 ? false : delayPreRegisterSecurity);
		config.set("delayPreLoginSecurity", delayPreLoginSecurity <= 0 ? false : delayPreLoginSecurity);
		config.set("saveLoginEnabled", saveLoginEnabled);
		config.set("forceSaveLogin", saveLoginEnabled && forceSaveLogin);
		for (final Entry<String, Location> entry : saveLoginLocations.entrySet())
			ObjectSaveLoadHelper.saveLocation(config, "saveLoginLocations." + entry.getKey() + ".", entry.getValue(), true, true);
		config.set("hideInventory", hideInventory);
		config.set("hidePlayer", hidePlayer);
		config.set("hideChat", hideChat);
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
	}

	public int dropInactiveAccounts()
	{
		if (autoDelete != -1)
			return dropInactiveAccounts(autoDelete);
		return -1;
	}

	public int dropInactiveAccounts(final long age)
	{
		return dropInactiveAccounts(new Date(System.currentTimeMillis() - age * 1000 * 60 * 60 * 24));
	}

	protected int dropInactiveAccounts(final Date limit)
	{
		if (database == null)
			return 0;
		final LinkedList<String> deletions = new LinkedList<String>();
		Set<String> onlineNames = new HashSet<String>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			onlineNames.add(p.getName());
		}
		synchronized (database.getDatabaseLock())
		{
			for (final LoginPlayerData data : database.getAllEntries())
				if (data.getLastActionTime().before(limit))
					if (!onlineNames.contains(data.getName()))
						deletions.add(data.getName());
		}
		for (final String name : deletions)
			new CrazyPlayerRemoveEvent(name).checkAndCallEvent();
		return deletions.size();
	}

	@Override
	@Permission("crazylogin.warnloginfailure")
	@Localized({ "CRAZYLOGIN.LOGIN.FAILED", "CRAZYLOGIN.KICKED.LOGINFAIL $Fails$", "CRAZYLOGIN.REGISTER.HEADER", "CRAZYLOGIN.LOGIN.FAILEDWARN $Name$ $IP$ $AttemptPerIP$ $AttemptPerAccount$", "CRAZYLOGIN.LOGIN.SUCCESS", "CRAZYLOGIN.LOGIN.FAILINFO $Fails$", "CRAZYLOGIN.LOGIN.PASSWORDREQUIRECHANGE", "CRAZYLOGIN.LOGIN.PASSWORDREQUIRECHANGE.LENGTH $CurrentLength$ $MinLength$" })
	public void playerLogin(final Player player, final String password) throws CrazyCommandException
	{
		if (database == null)
			throw new CrazyCommandCircumstanceException("when database is accessible");
		final LoginPlayerData data = database.getEntry(player);
		final CrazyLoginPreLoginEvent event = new CrazyLoginPreLoginEvent(player, data);
		event.callEvent();
		if (event.isCancelled())
		{
			new CrazyLoginLoginFailEvent(player, data, LoginFailReason.CANCELLED).callEvent();
			sendLocaleMessage("LOGIN.FAILED", player);
			return;
		}
		if (data == null)
		{
			new CrazyLoginLoginFailEvent(player, data, LoginFailReason.NO_ACCOUNT).callEvent();
			sendLocaleMessage("REGISTER.HEADER", player);
			return;
		}
		final String IP = player.getAddress().getAddress().getHostAddress();
		final boolean wasOnline = data.isLoggedIn();
		if (!data.login(password))
		{
			new CrazyLoginLoginFailEvent(player, data, LoginFailReason.WRONG_PASSWORD).callEvent();
			Integer fails = loginFailuresPerIP.get(IP);
			if (fails == null)
				fails = 1;
			else
				fails++;
			if (fails % autoKickLoginFailer == 0)
			{
				logger.log("LoginFail", player.getName() + " @ " + IP + " has been kicked for entering a wrong password (AttemptPerIP: " + fails + ", AttemptPerAccount: " + data.getLoginFails() + ")");
				player.kickPlayer(locale.getFormatedLocaleMessage(player, "KICKED.LOGINFAIL", fails));
				if (autoTempBanLoginFailer > 0)
					setTempBanned(player, autoTempBanLoginFailer);
			}
			else
			{
				logger.log("LoginFail", player.getName() + " @ " + IP + " entered a wrong password (AttemptPerIP: " + fails + ", AttemptPerAccount: " + data.getLoginFails() + ")");
				sendLocaleMessage("LOGIN.FAILED", player);
			}
			loginFailuresPerIP.put(IP, fails);
			if (!plugin.isHidingWarningsEnabled())
				broadcastLocaleMessage(true, "crazylogin.warnloginfailure", true, "LOGIN.FAILEDWARN", player.getName(), IP, fails, data.getLoginFails());
			getCrazyDatabase().saveWithoutPassword(data);
			return;
		}
		new CrazyLoginLoginEvent(player, data).callEvent();
		sendLocaleMessage("LOGIN.SUCCESS", player);
		final int fails = data.getLoginFails();
		data.resetLoginFails();
		if (fails > 0)
			sendLocaleMessage("LOGIN.FAILINFO", player, fails);
		logger.log("Login", player.getName() + " @ " + IP + " logged in successfully.");
		if (!wasOnline)
		{
			player.setFireTicks(0);
			playerListener.sendPlayerJoinMessage(player);
		}
		if (encryptor instanceof UpdatingEncryptor)
		{
			try
			{
				data.setPassword(password);
			}
			catch (final PasswordRejectedException e)
			{
				sendLocaleMessage("LOGIN.PASSWORDREQUIRECHANGE", player);
			}
			catch (final Exception e)
			{
				throw new CrazyCommandErrorException(e);
			}
			database.save(data);
		}
		final int passwordLength = password.length();
		final int minLength;
		if (CrazyCore.getPlugin().isProtectedPlayer(player))
			minLength = protectedAccountMinPasswordLength;
		else
			minLength = minPasswordLength;
		if (passwordLength < minLength)
			sendLocaleMessage("LOGIN.PASSWORDREQUIRECHANGE.LENGTH", player, passwordLength, minLength);
		if (data.isPasswordExpired())
			sendAuthReminderMessage(player);
		else
			playerListener.removeMovementBlocker(player);
		playerListener.disableSaveLogin(player);
		playerListener.disableHidenInventory(player);
		playerListener.unhidePlayer(player);
		loginFailuresPerIP.remove(IP);
		illegalCommandUsesPerIP.remove(IP);
		tempBans.remove(IP);
		data.addIP(IP);
		getCrazyDatabase().saveWithoutPassword(data);
		player.setMetadata("Authenticated", new Authenticated(this, player));
		unregisterDynamicHooks();
	}

	@Override
	@Localized({ "CRAZYLOGIN.LOGOUT.SUCCESS" })
	public void playerLogout(final Player player) throws CrazyCommandException
	{
		if (database == null)
			throw new CrazyCommandCircumstanceException("when database is accessible");
		if (!isLoggedIn(player))
			throw new CrazyCommandPermissionException();
		final LoginPlayerData data = getPlayerData(player);
		if (data != null)
		{
			data.logout();
			getCrazyDatabase().saveWithoutPassword(data);
		}
		player.removeMetadata("Authenticated", this);
		playerAutoLogouts.remove(player);
		player.kickPlayer(locale.getFormatedLocaleMessage(player, "LOGOUT.SUCCESS"));
		if (delayJoinQuitMessages)
			playerListener.sendDefaultPlayerQuitMessage(player);
		logger.log("Logout", player.getName() + " @ " + player.getAddress().getAddress().getHostAddress() + " logged out.");
	}

	@Override
	@Permission({ "crazylogin.requirepassword", "crazylogin.ensureregistration" })
	@Localized({ "CRAZYLOGIN.PASSWORDDELETE.SUCCESS", "CRAZYLOGIN.PASSWORDCHANGE.SUCCESS $Password$" })
	public void playerPassword(final Player player, final String password) throws CrazyException
	{
		if (disableRegistrations)
			throw new CrazyLoginRegistrationsDisabled();
		if (database == null)
			throw new CrazyCommandCircumstanceException("when database is accessible");
		final int passwordLength = password.length();
		if (passwordLength == 0)
		{
			if (alwaysNeedPassword || player.hasPermission("crazylogin.requirepassword"))
				throw new CrazyCommandUsageException((confirmWithOldPassword ? "<OldPassword> " : "") + "<NewPassword>" + (confirmNewPassword ? " <NewPassword>" : ""));
			playerListener.removeMovementBlocker(player);
			sendLocaleMessage("PASSWORDDELETE.SUCCESS", player);
			deletePlayerData(player);
			logger.log("Account", player.getName() + "@" + player.getAddress().getAddress().getHostAddress() + " deleted his account successfully.");
			return;
		}
		final int minLength;
		if (CrazyCore.getPlugin().isProtectedPlayer(player))
			minLength = protectedAccountMinPasswordLength;
		else
			minLength = minPasswordLength;
		if (passwordLength < minLength)
			throw new PasswordRejectedLengthException(passwordLength, minLength);
		LoginPlayerData data = getPlayerData(player);
		final boolean wasGuest = (data == null);
		if (wasGuest)
		{
			final String ip = player.getAddress().getAddress().getHostAddress();
			final HashSet<LoginPlayerData> associates = getPlayerDatasPerIP(ip);
			if (!player.hasPermission("crazylogin.ensureregistration"))
				if (maxRegistrationsPerIP != -1)
					if (associates.size() >= maxRegistrationsPerIP)
						throw new CrazyLoginExceedingMaxRegistrationsPerIPException(maxRegistrationsPerIP, associates);
			final CrazyLoginPreRegisterEvent event = new CrazyLoginPreRegisterEvent(player, associates);
			event.callEvent();
			if (event.isCancelled())
				throw new CrazyCommandPermissionException();
			data = new LoginPlayerData(player);
			tempBans.remove(player.getAddress().getAddress().getHostAddress());
			logger.log("Account", player.getName() + "@" + player.getAddress().getAddress().getHostAddress() + " registered successfully.");
		}
		else
			logger.log("Account", player.getName() + "@" + player.getAddress().getAddress().getHostAddress() + " changed his password successfully.");
		if (pluginCommunicationEnabled)
			new CrazyLoginPasswordEvent(player, data, password).callEvent();
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
		messageListener.sendPluginMessage(player, "Q_StorePW " + password);
		data.login(password);
		sendLocaleMessage("PASSWORDCHANGE.SUCCESS", player, password);
		if (wasGuest)
			if (alwaysNeedPassword)
			{
				player.setFireTicks(0);
				playerListener.sendPlayerJoinMessage(player);
			}
		playerListener.removeMovementBlocker(player);
		playerListener.disableSaveLogin(player);
		playerListener.disableHidenInventory(player);
		playerListener.unhidePlayer(player);
		getCrazyDatabase().save(data);
		player.setMetadata("Authenticated", new Authenticated(this, player));
		unregisterDynamicHooks();
	}

	/**
	 * Checks whether the player is allowed to execute the given command.
	 * 
	 * @param player
	 *            The player who should be checked.
	 * @param command
	 *            The command which should be checked.
	 * @return True, if the given player is allowed to executed the given command. False otherwise.
	 */
	@Permission("crazylogin.warncommandexploits")
	@Localized({ "CRAZYLOGIN.KICKED.COMMANDUSAGE", "CRAZYLOGIN.COMMAND.EXPLOITWARN $Name$ $IP$ $Command$ $Fails$" })
	public boolean playerCommand(final Player player, final String command)
	{
		if (hasPlayerData(player))
		{
			if (isLoggedIn(player))
				return true;
		}
		else if (!blockGuestCommands)
			return true;
		final String lowerCommand = command.toLowerCase();
		if (lowerCommand.startsWith("/"))
		{
			for (final String whiteCommand : commandWhiteList)
				if (lowerCommand.matches(whiteCommand))
					return true;
			final String IP = player.getAddress().getAddress().getHostAddress();
			Integer fails = illegalCommandUsesPerIP.get(IP);
			if (fails == null)
				fails = 1;
			else
				fails++;
			if (autoKickCommandUsers > 0 && fails % autoKickCommandUsers == 0)
			{
				logger.log("CommandBlocked", player.getName() + " @ " + IP + " has been kicked for trying to illegaly execute a command", command, "(AttemptPerIP: " + fails + ")");
				player.kickPlayer(locale.getFormatedLocaleMessage(player, "KICKED.COMMANDUSAGE"));
				if (autoTempBanCommandUsers > 0)
					setTempBanned(player, autoTempBanCommandUsers);
			}
			else
			{
				logger.log("CommandBlocked", player.getName() + " @ " + IP + " tried to illegaly execute a command", command, "(AttemptPerIP: " + fails + ")");
				sendAuthReminderMessage(player);
			}
			illegalCommandUsesPerIP.put(IP, fails);
			if (!hideWarnings)
				broadcastLocaleMessage(true, "crazylogin.warncommandexploits", true, "COMMAND.EXPLOITWARN", player.getName(), IP, command.replaceAll("\\$", "_"), fails);
			return false;
		}
		else
			return true;
	}

	@Override
	@Permission("crazylogin.requirepassword")
	public boolean isLoggedIn(final Player player)
	{
		if (player.hasMetadata("NPC"))
			return true;
		final LoginPlayerData data = getPlayerData(player);
		if (data == null)
			return !alwaysNeedPassword && !player.hasPermission("crazylogin.requirepassword");
		if (player.isOnline())
			return data.isLoggedIn();
		else
			return data.checkTimeOut();
	}

	/**
	 * Checks whether the player is logged in and the password is not expired.
	 * 
	 * @param player
	 *            The player to be checked.
	 * @return True, if the player is logged in successfully and his password is not expired. Otherwise False.
	 */
	@Permission("crazylogin.requirepassword")
	public boolean isLoggedInPlus(final Player player)
	{
		if (player.hasMetadata("NPC"))
			return true;
		final LoginPlayerData data = getPlayerData(player);
		if (data == null)
			return !alwaysNeedPassword && !player.hasPermission("crazylogin.requirepassword");
		if (player.isOnline())
			return data.isLoggedIn() && !data.isPasswordExpired();
		else
			return data.checkTimeOut();
	}

	@Override
	public void forceRelogin(final OfflinePlayer player)
	{
		forceRelogin(getPlayerData(player));
	}

	@Override
	public void forceRelogin(final String name)
	{
		forceRelogin(getPlayerData(name));
	}

	public void forceRelogin(final LoginPlayerData data)
	{
		if (data == null)
			return;
		data.setLoggedIn(false);
		final Player player = data.getPlayer();
		if (player != null)
			playerListener.PlayerJoin(data.getPlayer());
	}

	public void expirePassword(final String name)
	{
		expirePassword(getPlayerData(name));
	}

	public void expirePassword(final LoginPlayerData data)
	{
		if (data == null)
			return;
		data.expirePassword();
		((CrazyLoginDataDatabase) database).saveWithoutPassword(data);
		final Player player = data.getPlayer();
		if (player != null)
			sendAuthReminderMessage(player);
	}

	@Override
	@Localized({ "CRAZYLOGIN.REGISTER.REQUEST", "CRAZYLOGIN.LOGIN.PASSWORDEXPIRED", "CRAZYLOGIN.LOGIN.REQUEST" })
	public void sendAuthReminderMessage(final Player player)
	{
		if (doNotSpamAuthRequests)
			return;
		final Date now = new Date();
		final Date date = antiRequestSpamTable.get(player.getName());
		if (date == null)
		{
			now.setTime(now.getTime() + 5000L);
			antiRequestSpamTable.put(player.getName(), now);
		}
		else
		{
			if (date.after(now))
				return;
			date.setTime(now.getTime() + 5000L);
		}
		final LoginPlayerData data = plugin.getPlayerData(player);
		if (data == null)
			sendLocaleMessage("REGISTER.REQUEST", player);
		else if (data.isLoggedIn())
			sendLocaleMessage("LOGIN.PASSWORDEXPIRED", player);
		else
			sendLocaleMessage("LOGIN.REQUEST", player);
	}

	@Override
	public boolean isAlwaysNeedPassword()
	{
		return alwaysNeedPassword;
	}

	public boolean isConfirmNewPasswordEnabled()
	{
		return confirmNewPassword;
	}

	public boolean isConfirmWithOldPasswordEnabled()
	{
		return confirmWithOldPassword;
	}

	public boolean isHidingWarningsEnabled()
	{
		return hideWarnings;
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
				final Date timeOut = new Date(System.currentTimeMillis() - autoLogout * 1000);
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
								if (!data.checkTimeOut(timeOut))
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
		else
			return System.currentTimeMillis() < date.getTime();
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
		else
			return DATETIMEFORMAT.format(date);
	}

	public void setTempBanned(final Player player, final long duration)
	{
		setTempBanned(player.getAddress().getAddress().getHostAddress(), duration);
	}

	public void setTempBanned(final String IP, final long duration)
	{
		tempBans.put(IP, new Date(System.currentTimeMillis() + duration * 1000));
	}

	@Override
	public List<String> getCommandWhiteList()
	{
		return commandWhiteList;
	}

	public boolean isAvoidingSpammedAuthRequests()
	{
		return doNotSpamAuthRequests;
	}

	public boolean isAvoidingSpammedRegisterRequests()
	{
		return doNotSpamRegisterRequests;
	}

	public long getDelayAuthRequests()
	{
		return delayAuthRequests;
	}

	public long getRepeatAuthRequests()
	{
		return repeatAuthRequests;
	}

	public boolean isForceSingleSessionEnabled()
	{
		return forceSingleSession;
	}

	public boolean isForceSingleSessionSameIPBypassEnabled()
	{
		return forceSingleSessionSameIPBypass;
	}

	public boolean isDelayingPreRegisterSecurityEnabled()
	{
		return delayPreRegisterSecurity > 0;
	}

	public long getDelayPreRegisterSecurity()
	{
		return delayPreRegisterSecurity;
	}

	public boolean isDelayingPreLoginSecurityEnabled()
	{
		return delayPreLoginSecurity > 0;
	}

	public long getDelayPreLoginSecurity()
	{
		return delayPreLoginSecurity;
	}

	public boolean isSaveLoginEnabled()
	{
		return saveLoginEnabled;
	}

	@Override
	public boolean isForceSaveLoginEnabled()
	{
		return saveLoginEnabled && forceSaveLogin;
	}

	public Map<String, Location> getSaveLoginLocations()
	{
		return saveLoginLocations;
	}

	public Location getSaveLoginLocation(final World world)
	{
		if (saveLoginLocations.containsKey(world.getName()))
			return saveLoginLocations.get(world.getName()).clone();
		else
			return world.getSpawnLocation();
	}

	public Location getSaveLoginLocation(final Player player)
	{
		return getSaveLoginLocation(player.getWorld());
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

	@Override
	public boolean isHidingChatEnabled()
	{
		return hideChat;
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
	public HashSet<LoginPlayerData> getPlayerDatasPerPartialIP(final String partialIP)
	{
		final HashSet<LoginPlayerData> res = new HashSet<LoginPlayerData>();
		if (database == null)
			return res;
		synchronized (database.getDatabaseLock())
		{
			for (final LoginPlayerData data : database.getAllEntries())
				for (final String ip : data.getIPs())
					if (ip.startsWith(partialIP))
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
				if (!player.hasPermission(permission))
					continue Player;
			if (loggedInOnly)
				if (!isLoggedIn(player))
					continue;
			sendLocaleMessage(locale, player, args);
		}
	}

	public MessageListener getMessageListener()
	{
		return messageListener;
	}

	public boolean isDynamicProtectionEnabled()
	{
		return dynamicProtection;
	}

	public boolean isAdminLoginDisabled()
	{
		return disableAdminLogin;
	}

	public boolean isTokenLoginDisabled()
	{
		return disableTokenLogin;
	}

	public Map<String, Token> getLoginTokens()
	{
		return loginTokens;
	}

	public boolean everyoneLoggedIn()
	{
		for (final Player player : Bukkit.getOnlinePlayers())
			if (!hasPlayerData(player) || !isLoggedIn(player))
				return false;
		return true;
	}

	public Set<Player> getPlayerAutoLogouts()
	{
		return playerAutoLogouts;
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
