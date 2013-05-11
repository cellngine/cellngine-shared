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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * A collection of handy operations to perform on byte arrays and {@link java.io.InputStream
 * InputStream} objects.
 *
 * @author A.J.A. Boer <jboer@jboer.nl>
 */
public class ByteOperations
{
	/**
	 * Converts the provided {@code input} to a byte array representation.
	 *
	 * @param input
	 *            An {@link java.lang.Integer Integer} object.
	 * @return A byte array representation of the {@code input}.
	 */
	public static byte[] toBytes(final int input)
	{
		final ByteBuffer bb = ByteBuffer.allocate(4);

		bb.putInt(input);

		return bb.array();
	}

	/**
	 * Converts the provided {@code input} to a byte array representation.
	 *
	 * @param input
	 *            A {@link java.lang.Long Long} object.
	 * @return A byte array representation of the {@code input}.
	 */
	public static byte[] toBytes(final long input)
	{
		final ByteBuffer bb = ByteBuffer.allocate(8);

		bb.putLong(input);

		return bb.array();
	}

	/**
	 * Converts the provided {@code input} to a byte array representation.<br />
	 * <br />
	 *
	 * Since {@link java.lang.String String} objects have a variable length the length
	 * of the {@link java.lang.String String} object is added in front of the byte array.
	 *
	 * @param input
	 *            A {@link java.lang.String String} object.
	 * @return A byte array representation of the {@code input}.
	 */
	public static byte[] toBytes(final String input)
	{
		try
		{
			return toBytes(input.getBytes("UTF-8"));
		}
		catch (final UnsupportedEncodingException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Converts the provided {@code input} to a byte array representation.<br />
	 * <br />
	 *
	 * Since a {@link java.lang.Byte Byte} array has a variable length the length
	 * of the {@link java.lang.Byte Byte} array is added in front of the returned byte array.
	 *
	 * @param input
	 *            A {@link java.lang.Byte Byte} array.
	 * @return A byte array representation of the {@code input}.
	 */
	public static byte[] toBytes(final byte[] input)
	{
		try
		{
			final ByteBuffer bb = ByteBuffer.allocate(4 + input.length);
			bb.putInt(input.length);
			bb.put(input);

			return bb.array();
		}
		catch (final Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Converts the provided {@code input} to a byte array representation.<br />
	 * <br />
	 *
	 * Since {@link java.math.BigDecimal BigDecimal} objects have a variable length the length
	 * of the {@link java.math.BigDecimal BigDecimal} object is added in front of the byte array.
	 *
	 * @param input
	 *            A {@link java.math.BigDecimal BigDecimal} object.
	 * @return A byte array representation of the {@code input}.
	 */
	public static byte[] toBytes(final BigDecimal input)
	{
		return toBytes(input.toString());
	}

	/**
	 * Converts the provided {@code bytes} to the expected value.
	 *
	 * @param bytes
	 *            A byte array to take the value from. The array may be larger than the amount
	 *            of bytes needed to retrieve the value.
	 * @return An {@link java.lang.Integer Integer} object.
	 * @throws BufferUnderflowException
	 *             If the byte array does not contain the necessary amount
	 *             of bytes to return a value.
	 */
	public static int getInt(final byte[] bytes) throws BufferUnderflowException
	{
		final ByteBuffer bb = ByteBuffer.wrap(bytes);
		return bb.getInt();
	}

	/**
	 * Converts the provided {@code bytes} to the expected value.
	 *
	 * @param bytes
	 *            A byte array to take the value from. The array may be larger than the amount
	 *            of bytes needed to retrieve the value.
	 * @return A {@link java.lang.Long Long} object.
	 * @throws BufferUnderflowException
	 *             If the byte array does not contain the necessary amount
	 *             of bytes to return a value.
	 */
	public static long getLong(final byte[] bytes) throws BufferUnderflowException
	{
		final ByteBuffer bb = ByteBuffer.wrap(bytes);
		return bb.getLong();
	}

	/**
	 * Converts the provided {@code bytes} to the expected value. This function assumes that the
	 * length of the {@link java.lang.String String} object has been prefixed to the byte array.
	 *
	 * @param bytes
	 *            A byte array to take the value from. The array may be larger than the amount
	 *            of bytes needed to retrieve the value.
	 * @return A {@link java.lang.String String} object.
	 * @throws BufferUnderflowException
	 *             If the byte array does not contain the necessary amount
	 *             of bytes to return a value.
	 */
	public static String getString(final byte[] bytes) throws BufferUnderflowException
	{
		final ByteBuffer bb = ByteBuffer.wrap(bytes);
		final int length = bb.getInt();
		final byte[] buffer = new byte[length];

		bb.get(buffer);
		return CO.toString(buffer);
	}

	/**
	 * Converts the provided {@code bytes} to the expected value. This function assumes that the
	 * length of the {@link java.lang.BigDecimal BigDecimal} object has been prefixed to the byte
	 * array.
	 *
	 * @param bytes
	 *            A byte array to take the value from. The array may be larger than the amount
	 *            of bytes needed to retrieve the value.
	 * @return A {@link java.lang.BigDecimal BigDecimal} object.
	 * @throws BufferUnderflowException
	 *             If the byte array does not contain the necessary amount
	 *             of bytes to return a value.
	 */
	public static BigDecimal getBigDecimal(final byte[] bytes) throws BufferUnderflowException
	{
		return CO.toBigDecimal(getString(bytes));
	}

	/**
	 * Reads the {@link java.io.InputStream InputStream} and returns the expected value.
	 * This function will read 4 bytes from the {@link java.io.InputStream InputStream}.
	 *
	 * @param in
	 *            The {@link java.io.InputStream InputStream} to read the bytes from. The
	 *            {@link java.io.InputStream InputStream} may still have data left after this
	 *            function
	 *            has been called.
	 * @return An {@link java.lang.Integer Integer} object.
	 * @throws IOException
	 *             If the {@link java.io.InputStream InputStream} does not contain the
	 *             necessary amount of bytes to return a value.
	 */
	public static int getInt(final InputStream in) throws IOException
	{
		final byte[] buffer = new byte[4];
		in.read(buffer);
		return getInt(buffer);
	}

	/**
	 * Reads the {@link java.io.InputStream InputStream} and returns the expected value.
	 * This function will read 8 bytes from the {@link java.io.InputStream InputStream}.
	 *
	 * @param in
	 *            The {@link java.io.InputStream InputStream} to read the bytes from. The
	 *            {@link java.io.InputStream InputStream} may still have data left after this
	 *            function
	 *            has been called.
	 * @return A {@link java.lang.Long Long} object.
	 * @throws IOException
	 *             If the {@link java.io.InputStream InputStream} does not contain the
	 *             necessary amount of bytes to return a value.
	 */
	public static long getLong(final InputStream in) throws IOException
	{
		final byte[] buffer = new byte[8];
		in.read(buffer);
		return getLong(buffer);
	}

	/**
	 * Reads the {@link java.io.InputStream InputStream} and returns the expected value.
	 * This function will call {@link com.cellngine.ByteOperations.getInt getInt}
	 * and read further based on the amount returned by the
	 * {@link com.cellngine.ByteOperations.getInt getInt} function.
	 *
	 * @param in
	 *            The {@link java.io.InputStream InputStream} to read the bytes from. The
	 *            {@link java.io.InputStream InputStream} may still have data left after this
	 *            function
	 *            has been called.
	 * @return A {@link java.lang.String String} object.
	 * @throws IOException
	 *             If the {@link java.io.InputStream InputStream} does not contain the
	 *             necessary amount of bytes to return a value.
	 */
	public static String getString(final InputStream in) throws IOException
	{
		byte[] buffer = new byte[4];
		in.read(buffer);

		final int length = getInt(buffer);

		buffer = new byte[length];
		in.read(buffer);

		return CO.toString(buffer);
	}

	/**
	 * Reads the {@link java.io.InputStream InputStream} and returns the expected value.
	 * This function will call {@link com.cellngine.ByteOperations.getInt getInt}
	 * and read further based on the amount returned by the
	 * {@link com.cellngine.ByteOperations.getInt getInt} function.
	 *
	 * @param in
	 *            The {@link java.io.InputStream InputStream} to read the bytes from. The
	 *            {@link java.io.InputStream InputStream} may still have data left after this
	 *            function
	 *            has been called.
	 * @return A {@link java.lang.BigDecimal BigDecimal} object.
	 * @throws IOException
	 *             If the {@link java.io.InputStream InputStream} does not contain the
	 *             necessary amount of bytes to return a value.
	 */
	public static BigDecimal getBigDecimal(final InputStream in) throws IOException
	{
		return CO.toBigDecimal(getString(in));
	}
}