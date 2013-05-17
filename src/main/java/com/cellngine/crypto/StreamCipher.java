package com.cellngine.crypto;

/**
 * An interface all symmetric, byte-oriented stream ciphers should implement. The key used for en-
 * and decryption should be passed to the constructor of the specific implementation.
 *
 * Implementations of this interface are not required to be thread-safe.
 *
 * @author qwertty <hellraz0r.386@googlemail.com>
 */
public interface StreamCipher
{
	/**
	 * Encrypts a given input.<br /><br />
	 *
	 * The input byte array is not modified by calls to this function.
	 *
	 * @param input
	 *            The plain- or ciphertext that shall be encrypted.
	 * @return The result of the operation.
	 * @throws <code>NullPointerException</code> if <code>input</code> is <code>null</code>.
	 */
	public byte[] encrypt(final byte[] input);

	/**
	 * Decrypts a given input.<br /><br />
	 *
	 * The input byte array is not modified by calls to this function.
	 *
	 * @param input
	 *            The ciphertext that shall be decrypted.
	 * @return The result of the operation.
	 * @throws <code>NullPointerException</code> if <code>input</code> is <code>null</code>.
	 */
	public byte[] decrypt(final byte[] input);
}
