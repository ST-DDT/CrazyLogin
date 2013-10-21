package de.st_ddt.crazyutil.conditions.player;

import org.bukkit.configuration.ConfigurationSection;

import de.st_ddt.crazylogin.CrazyLogin;
import de.st_ddt.crazyutil.conditions.checker.PlayerConditionChecker;

public class ConditionPlayerLoginHasAccount extends BasicPlayerCondition
{

	public ConditionPlayerLoginHasAccount()
	{
		super();
	}

	public ConditionPlayerLoginHasAccount(final ConfigurationSection config)
	{
		super(config);
	}

	@Override
	public String getType()
	{
		return "PLAYER_LOGIN_HASACCOUNT";
	}

	@Override
	public boolean check(final PlayerConditionChecker checker)
	{
		return CrazyLogin.getPlugin().hasPlayerData(checker.getPlayer());
	}
}
