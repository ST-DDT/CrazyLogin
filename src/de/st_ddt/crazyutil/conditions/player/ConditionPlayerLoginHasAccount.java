package de.st_ddt.crazyutil.conditions.player;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import de.st_ddt.crazylogin.CrazyLogin;

public class ConditionPlayerLoginHasAccount extends ConditionPlayer
{

	public ConditionPlayerLoginHasAccount()
	{
		super();
	}

	public ConditionPlayerLoginHasAccount(ConfigurationSection config)
	{
		super(config);
	}

	@Override
	public String getTypeIdentifier()
	{
		return "PlayerLoginHasAccount";
	}

	@Override
	public void save(ConfigurationSection config, String path)
	{
		super.save(config, path);
	}

	@Override
	public boolean match(Player tester)
	{
		return CrazyLogin.getPlugin().hasAccount(tester);
	}
}
