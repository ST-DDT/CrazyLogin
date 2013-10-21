package de.st_ddt.crazyutil.conditions.player;

import org.bukkit.configuration.ConfigurationSection;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazyutil.conditions.checker.PlayerConditionChecker;

//TODO Register this class
public class ConditionPlayerLoginIsLoggedIn extends BasicPlayerCondition
{

	public ConditionPlayerLoginIsLoggedIn()
	{
		super();
	}

	public ConditionPlayerLoginIsLoggedIn(final ConfigurationSection config)
	{
		super(config);
	}

	@Override
	public String getType()
	{
		return "PLAYER_LOGIN_ISLOGGEDIN";
	}

	@Override
	public boolean check(final PlayerConditionChecker checker)
	{
		return CrazyLogin.getPlugin().isLoggedIn(checker.getPlayer());
	}
}
