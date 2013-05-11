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
package com.cellngine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A collection of common operations.
 *
 * @author A.J.A. Boer <jboer@jboer.nl>
 */
public class CO
{
	/**
	 * Safely close an {@link java.io.InputStream} object.
	 *
	 * @param in
	 *            The {@link java.io.InputStream InputStream} to close.
	 */
	public static void closeInputStream(final InputStream in)
	{
		try
		{
			in.close();
		}
		catch (final Exception e) /* no problem */
		{
		}
	}

	/**
	 * Safely close an {@link java.io.OutputStream} object.
	 *
	 * @param in
	 *            The {@link java.io.OutputStream OutputStream} to close.
	 */
	public static void closeOutputStream(final OutputStream out)
	{
		try
		{
			out.close();
		}
		catch (final Exception e) /* no problem */
		{
		}
	}

	/**
	 * @param o
	 *            An object to convert to a {@link java.lang.String String}.
	 */
	public static String toString(final Object o)
	{
		try
		{
			return o.toString();
		}
		catch (final Exception e)
		{
			return "";
		}
	}

	/**
	 * @param bytes
	 *            A byte array to convert to a {@link java.lang.String String}.
	 */
	public static String toString(final byte[] bytes)
	{
		try
		{
			return new String(bytes, "UTF-8");
		}
		catch (final Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param o
	 *            An object to convert to an {@link java.lang.Integer Integer}.
	 */
	public static int toInt(final Object o)
	{
		try
		{
			return Integer.parseInt(toString(o));
		}
		catch (final Exception e)
		{
			return 0;
		}
	}

	/**
	 * @param o
	 *            An object to convert to an {@link java.lang.Long Long}.
	 */
	public static long toLong(final Object o)
	{
		try
		{
			return Long.parseLong(toString(o));
		}
		catch (final Exception e)
		{
			return 0l;
		}
	}

	/**
	 * @param o
	 *            An object to convert to an {@link java.math.BigDecimal BigDecimal}.
	 */
	public static BigDecimal toBigDecimal(final Object o)
	{
		try
		{
			return new BigDecimal(toString(o));
		}
		catch (final Exception e)
		{
			return new BigDecimal("0.00");
		}
	}

	/**
	 * Reads a file and returns its contents as a byte array.
	 *
	 * @param file
	 *            The file to read.
	 * @return A byte array representing the contents of the file.
	 * @throws FileNotFoundException
	 *             If the file could not be found.
	 * @throws IOException
	 *             If there was an error while reading the file.
	 */
	public static byte[] getBytesFromFile(final File file) throws FileNotFoundException, IOException
	{
		FileInputStream fis = null;

		try
		{
			fis = new FileInputStream(file);

			return getBytesFromInputStream(fis, false);
		}
		finally
		{
			closeInputStream(fis);
		}
	}

	/**
	 * Reads an {@link java.io.InputStream InputStream} and returns its contents as a byte array.
	 *
	 * @param in
	 *            The {@link java.io.InputStream InputStream} to read.
	 * @param close
	 *            {@code true} to close the {@link java.io.InputStream InputStream} after reading,
	 *            {@code false} to keep it open.
	 * @return A byte array representing the contents of the {@link java.io.InputStream InputStream}
	 *         .
	 * @throws IOException
	 *             If there was an error while reading the {@link java.io.InputStream InputStream}.
	 */
	public static byte[] getBytesFromInputStream(final InputStream in, final boolean close) throws IOException
	{
		ByteArrayOutputStream baos = null;

		try
		{
			baos = new ByteArrayOutputStream();

			byte[] buffer;
			int i;

			do
			{
				buffer = new byte[1024];

				i = in.read(buffer);

				if (i > -1)
				{
					baos.write(buffer, 0, i);
				}
			} while (i > -1);

			return baos.toByteArray();
		}
		finally
		{
			if (close)
			{
				closeInputStream(in);
			}

			closeOutputStream(baos);
		}
	}

	/**
	 * @param input
	 *            The byte array to perform the hash operation on.
	 * @param algorithm
	 *            The algorithm to use. (e.x., MD5, SHA1, SHA-256, SHA-256)
	 * @return A new byte array representing the hashed version of {@code input}.
	 * @throws NoSuchAlgorithmException
	 *             If the provided algorithm is not available to this Java virtual machine.
	 */
	public static byte[] makeHash(final byte[] input, final String algorithm) throws NoSuchAlgorithmException
	{
		final MessageDigest md = MessageDigest.getInstance(algorithm);

		return md.digest(input);
	}

	/**
	 * @param input
	 *            An {@link java.io.InputStream InputStream} to perform the hash operation on.
	 * @param algorithm
	 *            The algorithm to use. (e.x., MD5, SHA1, SHA-256, SHA-256)
	 * @return A new byte array representing the hashed version of {@code input}.
	 * @throws NoSuchAlgorithmException
	 *             If the provided algorithm is not available to this Java virtual machine.
	 * @throws IOException
	 *             If there was an error while reading the {@link java.io.InputStream InputStream}.
	 */
	public static byte[] makeHash(final InputStream input, final String algorithm) throws NoSuchAlgorithmException,
			IOException
	{
		final MessageDigest md = MessageDigest.getInstance(algorithm);

		byte[] buffer;
		int i;

		do
		{
			buffer = new byte[1024];

			i = input.read(buffer);

			if (i > -1)
			{
				md.update(buffer, 0, i);
			}
		} while (i > -1);

		return md.digest();
	}

	/**
	 * @param input
	 *            An {@link java.lang.String String} to perform the hash operation on.
	 * @param algorithm
	 *            The algorithm to use. (e.x., MD5, SHA1, SHA-256, SHA-256)
	 * @return A new byte array representing the hashed version of {@code input}.
	 * @throws NoSuchAlgorithmException
	 *             If the provided algorithm is not available to this Java virtual machine.
	 */
	public static byte[] makeHash(final String input, final String algorithm) throws NoSuchAlgorithmException
	{
		try
		{
			return makeHash(input.getBytes("UTF-8"), algorithm);
		}
		catch (final UnsupportedEncodingException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Writes the contents of the provided {@link java.io.InputStream InputStream} to the provided
	 * {@link java.io.OutputStream OutputStream}.
	 *
	 * @param in
	 *            The {@link java.io.InputStream InputStream} to read from.
	 * @param out
	 *            The {@link java.io.OutputStream OutputStream} to write to.
	 * @throws IOException
	 *             If an error occurred during either the reading or writing process.
	 */
	public static void writeInputStreamToOutputStream(final InputStream in, final OutputStream out) throws IOException
	{
		byte[] buffer;
		int i;

		do
		{
			buffer = new byte[1024];

			i = in.read(buffer);

			if (i > -1)
			{
				out.write(buffer, 0, i);
				out.flush();
			}
		} while (i > -1);
	}
}