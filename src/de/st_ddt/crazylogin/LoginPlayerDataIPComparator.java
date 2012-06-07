package de.st_ddt.crazylogin;

public class LoginPlayerDataIPComparator implements LoginPlayerDataComparator
{

	@Override
	public int compare(LoginPlayerData o1, LoginPlayerData o2)
	{
		return o1.getLatestIP().compareTo(o2.getLatestIP());
	}
}
