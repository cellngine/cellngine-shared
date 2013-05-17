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
