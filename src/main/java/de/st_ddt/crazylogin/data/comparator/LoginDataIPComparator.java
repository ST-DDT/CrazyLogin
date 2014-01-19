package de.st_ddt.crazylogin.data.comparator;

import de.st_ddt.crazylogin.data.LoginData;

public class LoginDataIPComparator implements LoginDataComparator
{

	@Override
	public int compare(final LoginData o1, final LoginData o2)
	{
		return o1.getLatestIP().compareTo(o2.getLatestIP());
	}
}
