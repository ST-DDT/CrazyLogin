package de.st_ddt.crazylogin;

public class LoginDataIPComparator implements LoginDataComparator
{

	@Override
	public int compare(final LoginData o1, final LoginData o2)
	{
		return o1.getLatestIP().compareTo(o2.getLatestIP());
	}
}
