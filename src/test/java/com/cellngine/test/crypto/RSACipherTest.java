package com.cellngine.test.crypto;

import junit.framework.TestCase;

import com.cellngine.crypto.RSACipher;

public class RSACipherTest extends TestCase
{
	final RSACipher r = new RSACipher();

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
}