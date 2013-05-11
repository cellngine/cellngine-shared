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
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A collection of common operations.
 * 
 * @author A.J.A. Boer <jboer@jboer.nl>
 * @author qwertty <hellraz0r.386@googlemail.com>
 */
public class CO
{
	/**
	 * Converts a byte array to a hexadecimal String, courtesy of <a
	 * href="http://stackoverflow.com/a/9855338">Stack Overflow</a>.
	 * 
	 * @param bytes
	 *            The <code>byte</code> array to convert.
	 * @return A string containing the hexadecimal representation of the given byte array.
	 */
	public static String bytesToHex(final byte[] bytes)
	{
		final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		final char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++)
		{
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
	
	/**
	 * Repeatedly reads data from an <code>InputStream</code> until either <code>length</code> bytes
	 * have been read or
	 * the end of the file is reached.
	 * 
	 * @param source
	 *            The <code>InputStream</code> to read data from.
	 * @param length
	 *            The amount of bytes to read from the specified <code>InputStream</code>.
	 * @return The bytes read from the specified <code>InputStream</code>.
	 * @throws IOException
	 *             If any <code>IOException</code>s are throwing while reading data.
	 * @throws EOFException
	 *             If the end of the <code>InputStream</code> is reached before <code>length</code>
	 *             bytes could be read.
	 * @throws IllegalArgumentException
	 *             If <code>length</code> is smaller than zero.
	 * @throws NullPointerException
	 *             If <code>source</code> is null.
	 */
	public static byte[] readFully(final InputStream source, final int length) throws IOException
	{
		int bytesRead = 0;
		final byte[] contents = new byte[length];
		while (bytesRead < length)
		{
			final int read = source.read(contents, bytesRead, length - bytesRead);
			if (read == -1) { throw new EOFException(); }
			bytesRead += read;
		}
		return contents;
	}
	
	/**
	 * Attempts to invoke the <code>flush()</code>and <code>close()</code> methods on any given
	 * objects. <code>null</code> parameters will be ignored.
	 * 
	 * @param objects
	 *            One or more <code>Object</code>s on which the methods shall be invoked.
	 */
	public static void close(final Object ... objects)
	{
		for (final Object object : objects)
		{
			if (object != null)
			{
				try
				{
					final Method method = object.getClass().getMethod("flush");
					method.invoke(object);
				}
				catch (final Exception e)
				{
				}
				try
				{
					final Method method = object.getClass().getMethod("close");
					method.invoke(object);
				}
				catch (final Exception e)
				{
				}
			}
		}
	}
	
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