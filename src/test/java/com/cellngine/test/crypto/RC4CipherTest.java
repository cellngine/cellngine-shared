package com.cellngine.test.crypto;

import java.util.Arrays;

import junit.framework.TestCase;

import com.cellngine.CO;
import com.cellngine.crypto.RC4;
import com.cellngine.crypto.StreamCipher;

/**
 * 
 * @author qwer <hellraz0r.386@googlemail.com>
 */
public class RC4CipherTest extends TestCase
{
	public void test1()
	{
		final byte[] key = "Key".getBytes();
		final byte[] plaintext = "Plaintext".getBytes();
		
		final StreamCipher encCipher = new RC4(key);
		final byte[] buf = Arrays.copyOf(plaintext, plaintext.length);
		
		encCipher.encrypt(buf);
		assertEquals(CO.bytesToHex(buf), "9AE466368E7EA8F2F5");
		
		final StreamCipher decCipher = new RC4(key);
		decCipher.decrypt(buf);
		
		assertTrue(Arrays.equals(buf, plaintext));
	}
}
