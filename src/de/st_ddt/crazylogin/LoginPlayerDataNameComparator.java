package de.st_ddt.crazylogin;

public class LoginPlayerDataNameComparator implements LoginPlayerDataComparator
{

	@Override
	public int compare(LoginPlayerData o1, LoginPlayerData o2)
	{
		return o1.getName().compareTo(o2.getName());
	}
}
