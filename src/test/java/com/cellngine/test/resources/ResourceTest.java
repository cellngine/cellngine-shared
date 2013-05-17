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
package com.cellngine.test.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

import junit.framework.TestCase;

import com.cellngine.CO;
import com.cellngine.resources.ResourceEntry;
import com.cellngine.resources.ResourceFile;

/**
 * <pre>
 * Test case for the ResourceFile object.
 *
 * In order to run all the test in this test case, the following setup
 * is required:
 *
 * 1. Ensure that the folder "C:\Temp" exists and that your local user
 *    has permission to access this folder.
 *
 *    If you are running another operating system you will need to adjust
 *    the TEMP_PATH variable at the top of this class. Please do not
 *    commit any changes to this path to the repository.
 *
 * 2. Download the file "250mb.bin" at
 *    http://speedtest.tweak.nl/250mb.bin
 *
 *    Place the file in "C:\Temp\250mb.bin"
 *
 * Precautions:
 *
 * - The contents of the provided TEMP_PATH may be altered. Do not point
 *   this folder at a non-volatile directory.
 *
 * - The generated resource files will be deleted before each test, but they
 *   will stick around after the tests have finished so you can perform manual
 *   actions.
 * </pre>
 *
 * @author A.J.A. Boer <jboer@jboer.nl>
 */
public class ResourceTest extends TestCase
{
	private static String	TEMP_PATH		= "C:\\Temp\\";
	private static byte[]	ENCRYPTION_KEY	= "encryptiontest".getBytes();

	private void test1(final boolean useEncryption, final String fileName) throws Exception
	{
		new File(TEMP_PATH + fileName + ".crf").delete();

		ResourceFile rf = new ResourceFile(TEMP_PATH + fileName + ".crf", useEncryption ? ENCRYPTION_KEY : null);

		rf.addEntry("Hello, world".getBytes(), "dummy.txt");
		rf.addEntry("Hello, world".getBytes(), "test.txt");

		rf.write(useEncryption ? ENCRYPTION_KEY : null);

		rf = new ResourceFile(TEMP_PATH + fileName + ".crf", useEncryption ? ENCRYPTION_KEY : null);

		final List<ResourceEntry> entries = rf.getEntries();

		assertTrue(entries.size() == 1);

		for (final ResourceEntry entry : entries)
		{
			assertTrue(CO.toString(CO.getBytesFromInputStream(entry.getInputStream(), true)).equals("Hello, world"));
		}
	}

	private void test2(final boolean useEncryption, final String fileName) throws Exception
	{
		new File(TEMP_PATH + fileName + ".crf").delete();

		final String input = "!@$#ÖËÜµ€012345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ012345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ012345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ012345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ012345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ012345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ012345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ012345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ012345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ012345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ012345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ012345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ012345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ012345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ012345678901234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ";

		ResourceFile rf = new ResourceFile(TEMP_PATH + fileName + ".crf", useEncryption ? ENCRYPTION_KEY : null);

		rf.addEntry(input.getBytes("UTF-8"), "dummy.txt");

		rf.write(useEncryption ? ENCRYPTION_KEY : null);

		rf = new ResourceFile(TEMP_PATH + fileName + ".crf", useEncryption ? ENCRYPTION_KEY : null);

		rf.write(useEncryption ? ENCRYPTION_KEY : null);

		rf = new ResourceFile(TEMP_PATH + fileName + ".crf", useEncryption ? ENCRYPTION_KEY : null);

		final List<ResourceEntry> entries = rf.getEntries();

		assertTrue(entries.size() == 1);

		for (final ResourceEntry entry : entries)
		{
			assertTrue(entry.isGzip());
			assertTrue(CO.toString(CO.getBytesFromInputStream(entry.getInputStream(), true)).equals(input));
		}
	}

	private void test3_1(final boolean useEncryption, final String fileName) throws Exception
	{
		new File(TEMP_PATH + fileName + ".crf").delete();

		final ResourceFile rf = new ResourceFile(TEMP_PATH + fileName + ".crf", useEncryption ? ENCRYPTION_KEY : null);

		rf.addEntry(new File(TEMP_PATH + "250mb.bin"));

		rf.write(useEncryption ? ENCRYPTION_KEY : null);
	}

	private void test3_2(final boolean useEncryption, final String fileName) throws Exception
	{
		final ResourceFile rf = new ResourceFile(TEMP_PATH + fileName + ".crf", useEncryption ? ENCRYPTION_KEY : null);

		final List<ResourceEntry> entries = rf.getEntries();

		assertTrue(entries.size() == 1);

		for (final ResourceEntry entry : entries)
		{
			assertTrue(entry.getFileNames().size() == 1);
			assertTrue(entry.getFileNames().toArray(new String[] {})[0].equals("250mb.bin"));
		}
	}

	private void test3_3(final boolean useEncryption, final String fileName) throws Exception
	{
		final ResourceFile rf = new ResourceFile(TEMP_PATH + fileName + ".crf", useEncryption ? ENCRYPTION_KEY : null);

		final List<ResourceEntry> entries = rf.getEntries();

		for (final ResourceEntry entry : entries)
		{
			InputStream in = null;
			FileInputStream fin = null;

			try
			{
				in = entry.getInputStream();
				fin = new FileInputStream(new File(TEMP_PATH + "250mb.bin"));

				assertTrue(isEqual(in, fin));
			}
			finally
			{
				CO.closeInputStream(in);
				CO.closeInputStream(fin);
			}
		}
	}

	/*
	 * A simple test. A resource file is created, and resources
	 * are directly added from a byte array.
	 *
	 * The same byte array is added twice but with a different
	 * filename. This should result in only a single resource
	 * entry which contains the two filenames.
	 *
	 * After writing the resource file it is immediately opened
	 * and read. The entire resource file is validated at this point.
	 *
	 * On pass: The creation of resource files as well as the loading
	 * of resource files works.
	 *
	 * Manual control: The expected file size (last updated: 2013-05-10)
	 * is 188 bytes (without gzip compression).
	 */
	public void test1_1() throws Exception
	{
		this.test1(false, "test1");
	}

	/*
	 * A resource file is created with several "strange" characters and
	 * a sufficiently long string that it should generate a gzip-compressed
	 * data fork.
	 *
	 * The same resource file is then reopened and an attempt is made to save
	 * the same file. In order to do this the original file has to be accessed
	 * in order to retrieve the data fork of the resource entry that is being
	 * copied.
	 *
	 * After that the resource file is again reopened and validation is performed
	 * to ensure that 1) the file has been gzipped and 2) that the file contents
	 * match the input.
	 *
	 * On pass: The String in the "input" variable matches the contents of the file,
	 * gzip compression works, re-saving resource files works.
	 *
	 * Manual control: The expected file size (as of 2013-05-10) is 245 bytes.
	 */
	public void test2_1() throws Exception
	{
		this.test2(false, "test2");
	}

	/*
	 * A resource file is created with a large file (250 MB)
	 * embedded.
	 *
	 * Manual control: The expected file size (last updated: 2013-05-10)
	 * is 254.993 bytes (with gzip compression).
	 *
	 * Manual control: This test should take about 10-14 seconds on a
	 * high-end (as of 2012) system.
	 */
	public void test3_1() throws Exception
	{
		this.test3_1(false, "test3");
	}

	/*
	 * The resource file created in test 3.1 is opened for reading.
	 *
	 * On pass: The resource file from test 3.1 can be opened and contains
	 * the right entry.
	 *
	 * Manual control: This test should take less than a second on a
	 * high-end (as of 2012) system.
	 */
	public void test3_2() throws Exception
	{
		this.test3_2(false, "test3");
	}

	/*
	 * The resource file created in test 3.1 is opened for reading.
	 *
	 * The byte stream is read and compared against the original.
	 *
	 * Comparison took 5-6 seconds on a high-end (as of 2012) system.
	 *
	 * On pass: The data from the resource file entry matches the
	 * original file.
	 */
	public void test3_3() throws Exception
	{
		this.test3_3(false, "test3");
	}

	/*
	 * A copy of test 1.1 with encryption turned on.
	 */
	public void test4_1() throws Exception
	{
		this.test1(true, "test4");
	}

	/*
	 * A copy of test 2.1 with encryption turned on.
	 */
	public void test5_1() throws Exception
	{
		this.test2(true, "test5");
	}

	/*
	 * A copy of test 3.1 with encryption turned on.
	 */
	public void test6_1() throws Exception
	{
		this.test3_1(true, "test6");
	}

	/*
	 * A copy of test 3.2 with encryption turned on.
	 */
	public void test6_2() throws Exception
	{
		this.test3_2(true, "test6");
	}

	/*
	 * A copy of test 3.3 with encryption turned on.
	 */
	public void test6_3() throws Exception
	{
		this.test3_3(true, "test6");
	}

	//Source: http://stackoverflow.com/questions/4245863/fast-way-to-compare-inputstreams
	private static boolean isEqual(final InputStream i1, final InputStream i2) throws IOException
	{
		final ReadableByteChannel ch1 = Channels.newChannel(i1);
		final ReadableByteChannel ch2 = Channels.newChannel(i2);

		final ByteBuffer buf1 = ByteBuffer.allocateDirect(1024);
		final ByteBuffer buf2 = ByteBuffer.allocateDirect(1024);

		try
		{
			while (true)
			{
				final int n1 = ch1.read(buf1);
				final int n2 = ch2.read(buf2);

				if (n1 == -1 || n2 == -1) { return n1 == n2; }

				buf1.flip();
				buf2.flip();

				for (int i = 0; i < Math.min(n1, n2); i++)
				{
					if (buf1.get() != buf2.get()) { return false; }
				}

				buf1.compact();
				buf2.compact();
			}
		}
		finally
		{
			if (i1 != null)
			{
				i1.close();
			}

			if (i2 != null)
			{
				i2.close();
			}
		}
	}
}
