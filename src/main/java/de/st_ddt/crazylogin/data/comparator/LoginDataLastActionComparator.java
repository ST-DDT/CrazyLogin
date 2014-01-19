package de.st_ddt.crazylogin.data.comparator;

import de.st_ddt.crazylogin.data.LoginData;

public class LoginDataLastActionComparator implements LoginDataComparator
{

	@Override
	public int compare(final LoginData o1, final LoginData o2)
	{
		return o1.getLastActionTime().compareTo(o2.getLastActionTime());
	}
}
