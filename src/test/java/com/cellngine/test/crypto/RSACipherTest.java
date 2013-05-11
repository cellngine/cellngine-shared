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

import java.nio.charset.Charset;

import junit.framework.TestCase;

import com.cellngine.crypto.RSACipher;

public class RSACipherTest extends TestCase
{
	final RSACipher	r	= new RSACipher();
	
	@Override
	protected void setUp() throws Exception
	{
		this.r.generateKeypair(512);
		
		super.setUp();
	}
	
	public void test1() throws Exception
	{
		this.r.encrypt("Test".getBytes("UTF-8"));
	}
	
	public void test2() throws Exception
	{
		this.r.encrypt("tseT".getBytes("UTF-8"));
	}
	
	public void test3() throws Exception
	{
		checkEncryptDecrypt(r, r);
	}
	
	public void test4() throws Exception
	{
		final RSACipher clone = r.clone();
		checkEncryptDecrypt(r, clone);
		checkEncryptDecrypt(clone, r);
		
		final RSACipher encrypt = new RSACipher();
		encrypt.loadPublicKey(r.getPublicKey());
		
		final RSACipher decrypt = new RSACipher();
		decrypt.loadPrivateKey(r.getPrivateKey());
		
		checkEncryptDecrypt(encrypt, decrypt);
	}
	
	private void checkEncryptDecrypt(final RSACipher encypt, final RSACipher decrypt) throws Exception
	{
		final Charset testCharset = Charset.forName("UTF-8");
		final String testString = "Test";
		final byte[] testBytes = testString.getBytes(testCharset);
		
		assertTrue(new String(decrypt.decrypt(encypt.encrypt(testBytes)), testCharset).equals(testString));
		assertFalse(new String(decrypt.decrypt(encypt.encrypt("test".getBytes(testCharset))), testCharset)
				.equals(testString));
	}
}