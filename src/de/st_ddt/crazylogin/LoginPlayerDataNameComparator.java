package de.st_ddt.crazylogin;

public class LoginPlayerDataNameComparator implements LoginPlayerDataComparator
{

	@Override
	public int compare(final LoginPlayerData o1, final LoginPlayerData o2)
	{
		return o1.getName().compareTo(o2.getName());
	}
}
