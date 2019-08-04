package cn.com.pan.live.util.algorithm;

import java.security.MessageDigest;

public class Md5Encoder {

	public static String encode(String content) {
		return md5(md5(content) + "ecloud");
	}

	protected static String md5(String content) {
		if (content == null) {
			return null;
		}
		StringBuffer sbReturn = new StringBuffer();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update((content).getBytes("utf-8"));

			for (byte b : md.digest()) {
				sbReturn.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
			}
			return sbReturn.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
