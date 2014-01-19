package de.st_ddt.crazyutil.conditions.player;

import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;

public class Condition_Player_LoginHasAccount extends SimplePlayerCondition
{

	public Condition_Player_LoginHasAccount(final int index)
	{
		super(index);
	}

	public Condition_Player_LoginHasAccount(final ConfigurationSection config, final Map<String, Integer> parameterIndexes)
	{
		super(config, parameterIndexes);
	}

	@Override
	protected boolean check(final Player parameter)
	{
		return CrazyLogin.getPlugin().hasPlayerData(parameter);
	}
}
