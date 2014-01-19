package de.st_ddt.crazylogin.data;

import java.util.Random;

public class Token
{

	private static final char[] CHARS = "0123456789abcdef".toCharArray();
	private static final Random RANDOM = new Random();
	private final String creator;
	private final String token;
	private final long timeout;

	public Token(final String creator)
	{
		super();
		this.creator = creator;
		this.token = genToken();
		this.timeout = System.currentTimeMillis() + 600000;
	}

	/**
	 * @return A hexadecimal token for Login. (xxxx-xxxx-xxxx-xxxx-xxxx)
	 */
	private String genToken()
	{
		final StringBuilder builder = new StringBuilder(24);
		builder.append(randomChar());
		builder.append(randomChar());
		builder.append(randomChar());
		builder.append(randomChar());
		builder.append("-");
		builder.append(randomChar());
		builder.append(randomChar());
		builder.append(randomChar());
		builder.append(randomChar());
		builder.append("-");
		builder.append(randomChar());
		builder.append(randomChar());
		builder.append(randomChar());
		builder.append(randomChar());
		builder.append("-");
		builder.append(randomChar());
		builder.append(randomChar());
		builder.append(randomChar());
		builder.append(randomChar());
		builder.append("-");
		builder.append(randomChar());
		builder.append(randomChar());
		builder.append(randomChar());
		builder.append(randomChar());
		return builder.toString();
	}

	private char randomChar()
	{
		return CHARS[RANDOM.nextInt(CHARS.length)];
	}

	public String getCreator()
	{
		return creator;
	}

	public String getToken()
	{
		return token;
	}

	public boolean checkToken(final String checked)
	{
		return token.equals(checked);
	}

	public boolean isValid()
	{
		return timeout > System.currentTimeMillis();
	}
}
