package de.st_ddt.crazylogin;

public class LoginPlayerDataIPComparator implements LoginPlayerDataComparator
{

	@Override
	public int compare(final LoginPlayerData o1, final LoginPlayerData o2)
	{
		return o1.getLatestIP().compareTo(o2.getLatestIP());
	}
}
