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

import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper around a regular {@link java.io.InputStream} object that
 * imposes a limit on the amount of bytes that may be read.
 */
public class BoundedInputStream extends InputStream
{
	private final InputStream	inputStream;
	private final int			maxLength;

	private long				position	= 0;
	private long				mark;

	/**
	 * Creates a new {@link com.cellngine.io.BoundedInputStream
	 * BoundedInputStream} which wraps itself around the provided {@code inputStream} object.
	 *
	 * @param inputStream
	 *            The {@link java.io.InputStream} to wrap around.
	 * @param maxLength
	 *            The maximum amount of bytes that may be read from this
	 *            {@link com.cellngine.io.BoundedInputStream BoundedInputStream}.
	 */
	public BoundedInputStream(final InputStream inputStream, final int maxLength)
	{
		this.inputStream = inputStream;
		this.maxLength = maxLength;
	}

	@Override
	public int read() throws IOException
	{
		if (this.position >= this.maxLength)
		{
			return -1;
		}

		final int i = this.inputStream.read();

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
		if (this.position >= this.maxLength)
		{
			return -1;
		}

		final int i = this.inputStream.read(b, off,
				(this.position + len > this.maxLength ? (int) (this.maxLength - this.position) : len));

		if (i > -1)
		{
			this.position = this.position + i;
		}

		return i;
	}

	@Override
	public long skip(final long n) throws IOException
	{
		if (this.position >= this.maxLength)
		{
			return -1;
		}

		final long i = this.inputStream.skip(n);

		if (i > -1)
		{
			this.position = this.position + i;
		}

		return i;
	}

	@Override
	public int available() throws IOException
	{
		return this.inputStream.available();
	}

	@Override
	public String toString()
	{
		return this.inputStream.toString();
	}

	@Override
	public void close() throws IOException
	{
		this.inputStream.close();
	}

	@Override
	public boolean markSupported()
	{
		return this.inputStream.markSupported();
	}

	@Override
	public synchronized void mark(final int readlimit)
	{
		this.inputStream.mark(readlimit);
		this.mark = this.position;
	}

	@Override
	public synchronized void reset() throws IOException
	{
		this.inputStream.reset();
		this.position = this.mark;
	}
}