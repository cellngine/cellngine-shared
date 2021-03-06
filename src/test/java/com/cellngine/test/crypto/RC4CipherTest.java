/*
	This file is part of cellngine.

	cellngine is free software: you can redistribute it and/or modify
	it under the terms of the GNU Affero General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	cellngine is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU Affero General Public License for more details.

	You should have received a copy of the GNU Affero General Public License
	along with cellngine.  If not, see <http://www.gnu.org/licenses/>.
*/
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
