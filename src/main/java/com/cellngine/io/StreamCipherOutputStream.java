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
package com.cellngine.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.cellngine.crypto.StreamCipher;

/**
 * This class wraps around an {@link java.io.OutputStream OutputStream} object and encrypts
 * the bytes as they are written.
 *
 * @author A.J.A. Boer <jboer@jboer.nl>
 *
 */
public class StreamCipherOutputStream extends FilterOutputStream
{
	private final StreamCipher	streamCipher;

	/**
	 * @param outputStream
	 *            The {@link java.io.OutputStream OutputStream} object to wrap around.
	 * @param streamCipher
	 *            The {@link com.cellngine.crypto.StreamCipher StreamCipher} object to use for
	 *            encryption.
	 */
	public StreamCipherOutputStream(final OutputStream outputStream, final StreamCipher streamCipher)
	{
		super(outputStream);
		this.streamCipher = streamCipher;
	}

	@Override
	public void write(final int b) throws IOException
	{
		this.out.write(this.streamCipher.encrypt(new byte[] { (byte) b })[0]);
	}

	@Override
	public void write(final byte[] b) throws IOException
	{
		this.write(b, 0, b.length);
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException
	{
		byte[] buffer = new byte[len];

		System.arraycopy(b, off, buffer, 0, len);

		buffer = this.streamCipher.encrypt(buffer);

		System.arraycopy(buffer, off, b, 0, len);

		this.out.write(b, off, len);
	}
}