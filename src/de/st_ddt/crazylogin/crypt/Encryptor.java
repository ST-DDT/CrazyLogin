package de.st_ddt.crazylogin.crypt;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public interface Encryptor
{

	public String encrypt(final String name, final String salt, final String password) throws UnsupportedEncodingException, NoSuchAlgorithmException;

	public boolean match(final String name, final String password, final String encrypted);

	public String getAlgorithm();
}
