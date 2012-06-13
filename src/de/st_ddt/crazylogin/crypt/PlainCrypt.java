package de.st_ddt.crazylogin.crypt;

public class PlainCrypt implements Encryptor
{

	@Override
	public String encrypt(final String name, final String salt, final String password)
	{
		return password;
	}

	@Override
	public boolean match(final String name, final String password, final String encrypted)
	{
		return encrypted.equals(password);
	}

	@Override
	public String getAlgorithm()
	{
		return "Plaintext";
	}
}
