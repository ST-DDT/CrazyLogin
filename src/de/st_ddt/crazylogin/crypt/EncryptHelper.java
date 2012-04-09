package de.st_ddt.crazylogin.crypt;

public final class EncryptHelper
{

	private EncryptHelper()
	{
		super();
	}

	public static String convertToHex(byte[] data)
	{
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++)
		{
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do
			{
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			}
			while (two_halfs++ < 1);
		}
		return buf.toString();
	}
}
