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

import com.cellngine.crypto.StreamCipher;

/**
 * This class wraps around an {@link java.io.InputStream InputStream} object and decrypts
 * the bytes as they are read.<br />
 * <br />
 *
 * The implementation of this class is taken from the {@link javax.crypto.CipherInputStream} class
 * provided by the Java SDK.
 *
 * @author A.J.A. Boer <jboer@jboer.nl>
 *
 */
public class StreamCipherInputStream extends FilterInputStream
{
	private final StreamCipher	streamCipher;

	/* the buffer holding data that have been read in from the
	   underlying stream, but have not been processed by the cipher
	   engine. the size 512 bytes is somewhat randomly chosen */
	private final byte[]		ibuffer	= new byte[512];

	// having reached the end of the underlying input stream
	private boolean				done	= false;

	/* the buffer holding data that have been processed by the cipher
	   engine, but have not been read out */
	private byte[]				obuffer;

	// the offset pointing to the next "new" byte
	private int					ostart	= 0;

	// the offset pointing to the last "new" byte
	private int					ofinish	= 0;

	/**
	 * @param inputStream
	 *            The {@link java.io.InputStream InputStream} object to wrap around.
	 * @param streamCipher
	 *            The {@link com.cellngine.crypto.StreamCipher StreamCipher} object to use for
	 *            decryption.
	 */
	public StreamCipherInputStream(final InputStream inputStream, final StreamCipher streamCipher)
	{
		super(inputStream);
		this.streamCipher = streamCipher;
	}

	/**
	 * private convenience function.
	 *
	 * Entry condition: ostart = ofinish
	 *
	 * Exit condition: ostart <= ofinish
	 *
	 * return (ofinish-ostart) (we have this many bytes for you)
	 * return 0 (no data now, but could have more later)
	 * return -1 (absolutely no more data)
	 */
	private int getMoreData() throws IOException
	{
		if (this.done) { return -1; }

		final int readin = this.in.read(this.ibuffer);

		if (readin == -1)
		{
			this.done = true;

			return -1;
		}
		else
		{
			final byte[] buffer = new byte[readin];
			System.arraycopy(this.ibuffer, 0, buffer, 0, buffer.length);

			try
			{
				this.obuffer = this.streamCipher.decrypt(buffer);
			}
			catch (final NullPointerException e)
			{
				this.obuffer = null;
			}

			this.ostart = 0;

			if (this.obuffer == null)
			{
				this.ofinish = 0;
			}
			else
			{
				this.ofinish = this.obuffer.length;
			}

			return this.ofinish;
		}
	}

	@Override
	public int read() throws IOException
	{
		if (this.ostart >= this.ofinish)
		{
			// we loop for new data as the spec says we are blocking
			int i = 0;
			while (i == 0)
			{
				i = this.getMoreData();
			}

			if (i == -1) { return -1; }
		}

		return (this.obuffer[this.ostart++] & 0xff);
	};

	@Override
	public int read(final byte b[]) throws IOException
	{
		return this.read(b, 0, b.length);
	}

	@Override
	public int read(final byte b[], final int off, final int len) throws IOException
	{
		if (this.ostart >= this.ofinish)
		{
			// we loop for new data as the spec says we are blocking
			int i = 0;
			while (i == 0)
			{
				i = this.getMoreData();
			}

			if (i == -1) { return -1; }
		}

		if (len <= 0) { return 0; }

		int available = this.ofinish - this.ostart;

		if (len < available)
		{
			available = len;
		}

		if (b != null)
		{
			System.arraycopy(this.obuffer, this.ostart, b, off, available);
		}

		this.ostart = this.ostart + available;

		return available;
	}

	@Override
	public long skip(long n) throws IOException
	{
		final int available = this.ofinish - this.ostart;
		if (n > available)
		{
			n = available;
		}

		if (n < 0) { return 0; }

		this.ostart += n;

		return n;
	}

	@Override
	public int available() throws IOException
	{
		return (this.ofinish - this.ostart);
	}

	@Override
	public void close() throws IOException
	{
		this.in.close();

		this.ostart = 0;
		this.ofinish = 0;
	}

	@Override
	public boolean markSupported()
	{
		return false;
	}
}