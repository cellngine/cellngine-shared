package com.cellngine.crypto;

/**
 *
 * @author qwer <hellraz0r.386@googlemail.com>
 */
public abstract class AsymmetricCipher
{
	/**
	 * @return An instance of the recommended asymmetric block cipher algorithm.
	 */
	public static AsymmetricCipher getInstance()
	{
		return null;
	}

	// Key management

	/**
	 * Generates a new keypair containing a private and a public key.
	 *
	 * @param keyLength
	 *            The key length in bits.
	 * @throws IllegalArgumentException
	 *             If the key length is negative, zero or not supported by the algorithm.
	 */
	public abstract void generateKeypair(int keyLength);

	/**
	 * @return The public key in its encoded form, or <code>null</code> if no public key has been generated or imported
	 *         yet.
	 */
	public abstract byte[] getPublicKey();

	/**
	 * Loads an encoded public key.
	 *
	 * @param fromBytes
	 *            The encoded form of the public key as returned by {@link AsymmetricCipher#getPublicKey()}.
	 * @throws EncodingException
	 *             If the public key can not be read from the specified byte array.
	 */
	public abstract void loadPublicKey(byte[] fromBytes);

	/**
	 * @return The private key in its encoded form, or <code>null</code> if no private key has been generated or
	 *         imported yet.
	 */
	public abstract byte[] getPrivateKey();

	/**
	 * Loads an encoded private key.
	 *
	 * @param fromBytes
	 *            The encoded form of the public key as returned by {@link AsymmetricCipher#getPrivateKey()}.
	 * @throws EncodingException
	 *             If the private key can not be read from the specified byte array.
	 */
	public abstract void loadPrivateKey(byte[] fromBytes);

	// En- / decryption

	/**
	 * Encrypts the contents of a given byte array and returns the ciphertext in a new byte array.
	 *
	 * @param bytes
	 *            The bytes to encrypt.
	 * @return The encrypted bytes.
	 * @throws MissingKeyException
	 *             If no public key has been loaded or generated before.
	 * @throws RuntimeException
	 *             If something went wrong during the encryption.
	 */
	public abstract byte[] encrypt(byte[] bytes);

	/**
	 * Decrypts the contents of a given byte array and returns the plaintext in a new byte array.
	 *
	 * @param bytes
	 *            The bytes to decrypt.
	 * @return The decrypted bytes.
	 * @throws MissingKeyException
	 *             If no private key has been loaded or generated before.
	 * @throws CryptoException
	 *             If something went wrong during the encryption.
	 */
	public abstract byte[] decrypt(byte[] bytes);

	/**
	 * Thrown when a {@link AsymmetricCipher} implementation is told to use or return a key it doesn't have.
	 *
	 * @author qwer <hellraz0r.386@googlemail.com>
	 */
	public class MissingKeyException extends Error
	{
		private static final long	serialVersionUID	= -259269775385900564L;
	}

	public class CryptoException extends RuntimeException
	{
		private static final long serialVersionUID		= -7254592985328820067L;

		public CryptoException(final Exception e)		{ super(e); }
	}
}