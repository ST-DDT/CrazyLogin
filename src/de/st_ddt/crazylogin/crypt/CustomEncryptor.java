package de.st_ddt.crazylogin.crypt;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public abstract class CustomEncryptor implements Encryptor
{
	
	//You have to use the default constructor (without parameters)
	//If you access the config, please use "customEncryptor.valueName" to store data

	@Override
	public abstract String encrypt(String name, String salt, String password) throws UnsupportedEncodingException, NoSuchAlgorithmException;

	@Override
	public abstract boolean match(String name, String password, String encrypted);

	@Override
	public String getAlgorithm()
	{
		return "Custom";
	}
}
