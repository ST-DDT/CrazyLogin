package de.st_ddt.crazylogin.exceptions;

import de.st_ddt.crazyplugin.exceptions.CrazyException;

public class CrazyLoginException extends CrazyException
{

	private static final long serialVersionUID = -4866090558668074475L;

	public CrazyLoginException()
	{
		super();
	}

	@Override
	public String getLangPath()
	{
		return "CRAZYLOGIN.EXCEPTION";
	}
}
