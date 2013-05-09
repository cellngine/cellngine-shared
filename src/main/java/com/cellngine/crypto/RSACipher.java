package com.cellngine.crypto;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author qwer <hellraz0r.386@googlemail.com>
 */
public class RSACipher extends AsymmetricCipher
{
	private static Log LOG													= LogFactory.getLog(RSACipher.class);

	public static final String	ALGORITHM									= "RSA";
	public static final String	BLOCK_CIPHER_MODE							= "ECB";
	public static final String	PADDING										= "PKCS1Padding";
	public static final String	TRANSFORMATION								= ALGORITHM + "/" + BLOCK_CIPHER_MODE + "/" + PADDING;

	private Cipher				cipher;
	private final SecureRandom	random										= new SecureRandom();

	private PublicKey			publicKey									= null;
	private PrivateKey			privateKey									= null;

	public RSACipher()
	{
		try																	{ this.cipher = Cipher.getInstance(TRANSFORMATION); }
		catch (final NoSuchAlgorithmException | NoSuchPaddingException e)	{ LOG.error("Unable to get cipher instance (" + TRANSFORMATION + ")", e); }
	}

	@Override
	public void generateKeypair(final int keyLength)
	{
		if (keyLength <= 0)													{ throw new IllegalArgumentException("Key length must be positive and nonzero"); }

		final KeyPairGenerator generator;
		try																	{ generator = KeyPairGenerator.getInstance(ALGORITHM); }
		catch (final NoSuchAlgorithmException e)							{ LOG.error("Unable to get key generator instance (" + ALGORITHM + ")", e); return; }

		try																	{ generator.initialize(keyLength, this.random); }
		catch (final InvalidParameterException e)							{ throw new IllegalArgumentException("Unsupported key length"); }

		final KeyPair pair													= generator.generateKeyPair();
		this.publicKey														= pair.getPublic();
		this.privateKey														= pair.getPrivate();

		/*
		cipher.init(Cipher.ENCRYPT_MODE, pubKey, random);
		final byte[] cipherText = cipher.doFinal(null);
		System.out.println("cipher: " + new String(cipherText));

		cipher.init(Cipher.DECRYPT_MODE, privKey);
		final byte[] plainText = cipher.doFinal(cipherText);
		System.out.println("plain : " + new String(plainText));
		*/
	}

	private KeyFactory getKeyFactory()
	{
		try																	{ return KeyFactory.getInstance(ALGORITHM); }
		catch (final NoSuchAlgorithmException e)							{ LOG.error("Unable to get key factory instance (" + ALGORITHM + ")", e); return null; }
	}

	private <T extends java.security.spec.KeySpec> T getKeySpec(final Key key, final Class<T> keyClass)
	{
		final KeyFactory factory											= this.getKeyFactory();

		try																	{ return factory.getKeySpec(key, keyClass); }
		catch (final InvalidKeySpecException e)								{ LOG.error("Unable to get key spec from factory", e); return null; }
	}

	private PublicKey getPublicKey(final RSAPublicKeySpec keySpec)
	{
		final KeyFactory factory											= this.getKeyFactory();

		try																	{ return factory.generatePublic(keySpec); }
		catch (final InvalidKeySpecException e)								{ LOG.error("Unable to get key spec from factory", e); return null; }
	}

	private PrivateKey getPrivateKey(final RSAPrivateKeySpec keySpec)
	{
		final KeyFactory factory											= this.getKeyFactory();

		try																	{ return factory.generatePrivate(keySpec); }
		catch (final InvalidKeySpecException e)								{ LOG.error("Unable to get key spec from factory", e); return null; }
	}

	private byte[] encode(final BigInteger modulus, final BigInteger exponent)
	{
		final byte[] modulusEnc												= modulus.toByteArray();
		final byte[] exponentEnc											= exponent.toByteArray();
		final ByteBuffer buffer												= ByteBuffer.allocate(2 * 4 + modulusEnc.length + exponentEnc.length);
		buffer.putInt(modulusEnc.length);
		buffer.put(modulusEnc);
		buffer.putInt(exponentEnc.length);
		buffer.put(exponentEnc);
		return buffer.array();
	}

	@Override
	public byte[] getPublicKey()
	{
		if (this.publicKey == null)											{ return null; }

		final RSAPublicKeySpec spec											= this.getKeySpec(this.publicKey, RSAPublicKeySpec.class);

		return this.encode(spec.getModulus(), spec.getPublicExponent());
	}

	private BigInteger[] getKeyData(final byte[] fromBytes)
	{
		final ByteBuffer buffer												= ByteBuffer.wrap(fromBytes);

		final int modulusLength												= buffer.getInt();
		final byte[] modulusBuffer											= new byte[modulusLength];
		buffer.get(modulusBuffer);
		final BigInteger modulus											= new BigInteger(modulusBuffer);

		final int exponentLength											= buffer.getInt();
		final byte[] exponentBuffer											= new byte[exponentLength];
		buffer.get(exponentBuffer);
		final BigInteger exponent											= new BigInteger(exponentBuffer);

		return new BigInteger[]{ modulus, exponent };
	}

	@Override
	public void loadPublicKey(final byte[] fromBytes)
	{
		final BigInteger[] keyData											= this.getKeyData(fromBytes);
		final RSAPublicKeySpec spec											= new RSAPublicKeySpec(keyData[0], keyData[1]);
		this.publicKey														= this.getPublicKey(spec);
	}

	@Override
	public byte[] getPrivateKey()
	{
		if (this.privateKey == null)										{ return null; }

		final RSAPrivateKeySpec spec										= this.getKeySpec(this.privateKey, RSAPrivateKeySpec.class);

		return this.encode(spec.getModulus(), spec.getPrivateExponent());
	}

	@Override
	public void loadPrivateKey(final byte[] fromBytes)
	{
		final BigInteger[] keyData											= this.getKeyData(fromBytes);
		final RSAPrivateKeySpec spec										= new RSAPrivateKeySpec(keyData[0], keyData[1]);
		this.privateKey														= this.getPrivateKey(spec);
	}

	private void initCipher(final int mode)
	{
		Key key																= null;

		switch (mode)
		{
			case Cipher.ENCRYPT_MODE:										{ key = this.publicKey; break; }
			case Cipher.DECRYPT_MODE:										{ key = this.privateKey; break; }
			default:														{ throw new IllegalArgumentException(); }
		}

		try																	{ this.cipher.init(mode, key, this.random); }
		catch (final InvalidKeyException e)									{ LOG.error("Failed to initialize cipher", e); }
	}

	@Override
	public byte[] encrypt(final byte[] bytes)
	{
		if (bytes == null)													{ throw new NullPointerException(); }

		this.initCipher(Cipher.ENCRYPT_MODE);

		try																	{ return this.cipher.doFinal(bytes); }
		catch (final Exception e)											{ throw new CryptoException(e); }
	}

	@Override
	public byte[] decrypt(final byte[] bytes)
	{
		if (bytes == null)													{ throw new NullPointerException(); }

		this.initCipher(Cipher.DECRYPT_MODE);

		try																	{ return this.cipher.doFinal(bytes); }
		catch (final Exception e)											{ throw new CryptoException(e); }
	}

	@Override
	public RSACipher clone()
	{
		final RSACipher cipher												= new RSACipher();

		cipher.loadPrivateKey(this.getPrivateKey());
		cipher.loadPublicKey(this.getPublicKey());

		return cipher;
	}
}