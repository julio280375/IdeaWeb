package com.idea;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.util.Base64;

public class AESSFC {
	private SecretKey key;
	private Cipher cipher;
	private String algoritmo = "AES";
	private int keysize = 16;
	

	public void addKey(String value) {
		byte[] valuebytes;
		if (value.length() > 1 && value.length() <= 16)
			valuebytes = value.getBytes();
		else
			valuebytes = mickeyIDEA.getBytes();
		key = new SecretKeySpec(Arrays.copyOf(valuebytes, keysize), algoritmo);
	}

	public String asHex(byte buf[]) {
		StringBuilder strbuf = new StringBuilder(buf.length * 2);
		int i;

		for (i = 0; i < buf.length; i++) {
			if (((int) buf[i] & 0xff) < 0x10)
				strbuf.append("0");
			strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
		}
		return strbuf.toString();
	}

	public String encriptar(String texto) {
		String value = "";
		try {
			cipher = Cipher.getInstance(algoritmo);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] textobytes = texto.getBytes();
			byte[] cipherbytes = cipher.doFinal(textobytes);
			value = Base64.getEncoder().encodeToString(cipherbytes);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException ex) {
			System.out.println(ex.getMessage());
		}
		return value;
	}

	public String desencriptar(String texto) {
		String str = "";
		try {
			byte[] value = Base64.getDecoder().decode(texto);
			cipher = Cipher.getInstance(algoritmo);
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] cipherbytes = cipher.doFinal(value);
			str = new String(cipherbytes);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException ex) {
			System.out.println(ex.getMessage());
		}
		return str;
	}
	
	private String mickeyIDEA = "1D3AL05C48051D34";
	
}
