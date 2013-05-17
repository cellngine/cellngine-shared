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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class extends the default implementation of an {@link java.io.InputStream InputStream}
 * object and provides two functions:<br />
 * <ul>
 * <li>The {@link #getPosition getPosition} function may be used to get the current position within
 * the InputStream, relative to the position the {@link java.io.InputStream InputStream} object was
 * in when this class was first wrapped around it.</li>
 * <li>The {@link #forceSkip forceSkip} method may be used to forcibly skip a number of bytes.
 * </ul>
 *
 * @author A.J.A. Boer <jboer@jboer.nl>
 *
 */
public class CommonInputStream extends FilterInputStream
{
	protected long	position	= 0;

	public CommonInputStream(final InputStream inputStream)
	{
		super(inputStream);
	}

	@Override
	public int read() throws IOException
	{
		final int i = this.in.read();

		if (i > -1)
		{
			this.position = this.position + 1;
		}

		return i;
	}

	@Override
	public int read(final byte[] b) throws IOException
	{
		return this.read(b, 0, b.length);
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException
	{
		final int i = this.in.read(b, off, len);

		if (i > -1)
		{
			this.position = this.position + i;
		}

		return i;
	}

	@Override
	public long skip(final long n) throws IOException
	{
		final long i = this.in.skip(n);

		if (i > -1)
		{
			this.position = this.position + i;
		}

		return i;
	}

	/**
	 * This is a modified version of the {@link #skip skip} method that forces the
	 * {@link java.io.InputStream InputStream} to skip by reading out (and subsequently discarding)
	 * the amount of bytes to skip from the {@link java.io.InputStream InputStream} object.<br />
	 * <br />
	 *
	 * The {@link #skip skip} method has the disadvantage that it may decide not to skip
	 * certain bytes (e.x., if the bytes have not yet been prepared), which can be undesirable.
	 *
	 * @param n
	 *            The amount of bytes to skip.
	 * @throws IOException
	 *             If an error occurred while reading the {@link java.io.InputStream InputStream}
	 *             object.
	 * @throws RuntimeException
	 *             If the {@link java.io.InputStream InputStream} object does not have enough bytes
	 *             left for this method to succeed.
	 */
	public void forceSkip(final long n) throws IOException
	{
		int i = (int) n;
		int j;
		byte[] buffer;

		while (i > 1024)
		{
			buffer = new byte[1024];
			j = this.read(buffer);

			if (j == -1) { throw new RuntimeException("Unable to skip " + n + " bytes; end of file reached after "
					+ (n - i) + " bytes."); }

			i = i - j;
		}

		while (i > 0)
		{
			buffer = new byte[i];
			j = this.read(buffer);

			if (j == -1) { throw new RuntimeException("Unable to skip " + n + " bytes; end of file reached after "
					+ (n - i) + " bytes."); }

			i = i - j;
		}
	}

	@Override
	public boolean markSupported()
	{
		return false;
	}

	/**
	 * @return The current position within the {@link java.io.InputStream InputStream} object.<br />
	 * <br />
	 *
	 *         This value is relative to the position that the {@link java.io.InputStream
	 *         InputStream} object was in when this object was first created.
	 */
	public long getPosition()
	{
		return this.position;
	}
}