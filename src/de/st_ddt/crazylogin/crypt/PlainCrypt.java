package de.st_ddt.crazylogin.crypt;

public class PlainCrypt implements Encryptor
{

	@Override
	public String encrypt(String name, String salt, String password)
	{
		return password;
	}

	@Override
	public boolean match(String name, String password, String encrypted)
	{
		return encrypted.equals(password);
	}

	@Override
	public String getAlgorithm()
	{
		return "Plaintext";
	}
}
