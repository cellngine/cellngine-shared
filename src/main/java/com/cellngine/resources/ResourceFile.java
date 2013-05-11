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
package com.cellngine.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import com.cellngine.ByteOperations;
import com.cellngine.CO;
/**
 * A subtype of <code>java.io.File</code> that handles cellngine resource files (.crf)
 *
 * @author A.J.A. Boer <jboer@jboer.nl>
 */
public class ResourceFile extends File
{
	/*
	 * .crf file structure:
	 *
	 * - Header
	 *     - 0x00
	 *     - 0x04
	 *     - "CRF" (3 byte String)
	 *     - 0x27
	 *     - 0x44
	 *     - 0x02
	 *     - Version (4 byte integer)
	 *  - Encryption
	 *     0x00 = No, the file is not encrypted
	 *     0x01 = Yes, the following bytes are encrypted
	 *  - Number of file entries (4 byte integer)
	 *  - Per file entry:
	 *     - Resource ID (Length of String + String, SHA-256 hash of the bytes)
	 *     - Per file name:
	 *         - File name (Length of String + String)
	 *     - Compression algorithm
	 *        0x00 = No compression
	 *        0x01 = GZip compression
	 *     - File contents (Length in bytes + Bytes)
	 *  - Hash (String, SHA-256 hash of the bytes of all entries)
	 *
	 */

	private static final long			serialVersionUID	= -514000992190910068L;
	private final List<ResourceEntry>	entries				= new Vector<ResourceEntry>();

	public ResourceFile(final URI uri) throws FileNotFoundException, IOException, NoSuchAlgorithmException
	{
		super(uri);
		this.init();
	}

	public ResourceFile(final String parent, final String child) throws FileNotFoundException, IOException,
			NoSuchAlgorithmException
	{
		super(parent, child);
		this.init();
	}

	public ResourceFile(final String pathname) throws FileNotFoundException, IOException, NoSuchAlgorithmException
	{
		super(pathname);
		this.init();
	}

	public ResourceFile(final File parent, final String child) throws FileNotFoundException, IOException,
			NoSuchAlgorithmException
	{
		super(parent, child);
		this.init();
	}

	public ResourceFile(final File file) throws IOException, NoSuchAlgorithmException
	{
		this(file.getAbsolutePath());
	}

	private void init() throws FileNotFoundException, IOException, NoSuchAlgorithmException
	{
		if (this.exists())
		{
			FileInputStream fin = null;

			try
			{
				//The specifications of the file structure is at the top of this class file.

				fin = new FileInputStream(this);
				byte[] buffer;

				buffer = new byte[8];
				fin.read(buffer);

				if (buffer[0] != 0x00 || buffer[1] != 0x04 || buffer[2] != 67 || buffer[3] != 82 || buffer[4] != 70
						|| buffer[5] != 0x27 || buffer[6] != 0x44 || buffer[7] != 0x02)
				{
					throw new RuntimeException("Resource file is not valid.");
				}

				final int version = ByteOperations.getInt(fin);

				if (version == 1)
				{
					final boolean encryption = fin.read() == 0x01;

					if (encryption)
					{
						//TODO: Implement encryption/decryption.
						throw new RuntimeException("Encrypted resource files are not yet supported.");
					}
					else
					{
						final int entries = ByteOperations.getInt(fin);

						final StringBuilder sb = new StringBuilder();

						String resourceID;
						int fileNamesLength;
						Set<String> fileNames;
						int bytesLength;
						ResourceEntry entry;
						int compression;
						int fileOffset;

						for (int i = 0; i < entries; i++)
						{
							resourceID = ByteOperations.getString(fin);

							sb.append(resourceID);

							fileNamesLength = ByteOperations.getInt(fin);
							fileNames = new TreeSet<String>();

							for (int j = 0; j < fileNamesLength; j++)
							{
								fileNames.add(ByteOperations.getString(fin));
							}

							compression = fin.read();

							bytesLength = ByteOperations.getInt(fin);

							fileOffset = (int) fin.getChannel().position();
							fin.skip(bytesLength);

							entry = new ResourceEntry(this, resourceID, fileOffset, bytesLength, compression);
							entry.addFileNames(fileNames);

							this.entries.add(entry);
						}

						buffer = new byte[64];

						fin.read(buffer);

						if (!CO.toString(buffer).equals(CO.toString(CO.makeHash(sb.toString(), "SHA-512"))))
						{
							throw new RuntimeException("Validation failed for resource file \""
									+ this.getAbsolutePath() + "\"");
						}
					}
				}
				else
				{
					throw new RuntimeException("This resource file has a higher version (" + version
							+ ") than is supported.");
				}
			}
			finally
			{
				CO.closeInputStream(fin);
			}
		}
	}

	private void addEntry(final ResourceEntry entryToAdd)
	{
		synchronized (this.entries)
		{
			boolean found = false;
			for (final ResourceEntry entry : this.entries)
			{
				if (entry.getResourceID().equals(entryToAdd.getResourceID()))
				{
					found = true;
					entry.addFileNames(entryToAdd.getFileNames());
				}
			}

			if (!found)
			{
				this.entries.add(entryToAdd);
			}
		}
	}

	/**
	 * Adds a new <code>com.cellngine.components.resources.ResourceEntry</code> or updates one
	 * if it already exists.
	 *
	 * @param bytes
	 *            The data of the file represented as a byte array.
	 * @param fileName
	 *            The filename that represents this file.
	 */
	public void addEntry(final byte[] bytes, final String fileName)
	{
		final ResourceEntry entry = new ResourceEntry(bytes, fileName);

		this.addEntry(entry);
	}

	/**
	 * Adds a new <code>com.cellngine.components.resources.ResourceEntry</code> or updates one
	 * if it already exists.<br />
	 * <br />
	 *
	 * This is a convenience method that calls <code>addEntry(file, file.getName())</code>.
	 *
	 * @param file
	 *            A <code>java.io.File</code> to add as a resource.
	 * @throws IOException
	 *             If there was an error reading the file to import.
	 */
	public void addEntry(final File file) throws IOException
	{
		this.addEntry(file, file.getName());
	}

	/**
	 * Adds a new {@link com.cellngine.resources.ResourceEntry ResourceEntry} or updates
	 * one
	 * if it already exists.
	 *
	 * @param file
	 *            A {@link java.io.File} to add as a resource.
	 * @param fileName
	 *            The filename that represents this file.
	 * @throws IOException
	 *             If there was an error reading the file to import.
	 */
	public void addEntry(final File file, final String fileName) throws IOException
	{
		final ResourceEntry entry = new ResourceEntry(file, fileName);

		this.addEntry(entry);
	}

	/**
	 * @return A list of {@link com.cellngine.resources.ResourceEntry ResourceEntry}
	 *         objects
	 *         representing the files embedded in this resource file.
	 */
	public List<ResourceEntry> getEntries()
	{
		return Collections.unmodifiableList(this.entries);
	}

	/**
	 * Writes the resource file to disk, overwriting an existing file if present.
	 *
	 * @throws FileNotFoundException
	 *             If the file could not be created.
	 * @throws IOException
	 *             If there was an error during the creation of the file.
	 * @throws NoSuchAlgorithmException
	 *             If the SHA-512 algorithm is not available to the Java virtual machine.
	 */
	public void write() throws FileNotFoundException, IOException, NoSuchAlgorithmException
	{
		synchronized (this.entries)
		{
			FileOutputStream fout = null;
			InputStream in = null;
			GZIPOutputStream gos = null;
			FileInputStream fin = null;

			try
			{
				//The specifications of the file structure is at the top of this class file.

				final File file = File.createTempFile("crf", CO.toString(this.hashCode()));

				fout = new FileOutputStream(file, false);

				//Write the header.
				fout.write(0x00);
				fout.write(0x04);
				fout.write("CRF".getBytes("UTF-8"));
				fout.write(0x27);
				fout.write(0x44);
				fout.write(0x02);

				//Write the version number.
				fout.write(ByteOperations.toBytes(1));

				//Encryption: 0x00 = no, 0x01 = yes
				//TODO: Implement encryption/decryption.

				//No encryption.
				fout.write(0x00);

				//Amount of entries (stored as a 4-byte integer)
				fout.write(ByteOperations.toBytes(this.entries.size()));

				final StringBuilder sb = new StringBuilder();
				int length;
				boolean gzip;
				long pos;

				for (final ResourceEntry entry : this.entries)
				{
					sb.append(entry.getResourceID());

					//Resource ID (stored as a 4-byte integer)
					fout.write(ByteOperations.toBytes(entry.getResourceID()));

					//Amount of file names (stored as a 4-byte integer)
					fout.write(ByteOperations.toBytes(entry.getFileNames().size()));

					for (final String fileName : entry.getFileNames())
					{
						fout.write(ByteOperations.toBytes(fileName));
					}

					gzip = entry.isGzip();

					//Compression algorithm: 0x00 for none, 0x01 for GZip
					if (gzip)
					{
						fout.write(0x01);
					}
					else
					{
						fout.write(0x00);
					}

					//Store the current position, because we'll need to get
					//back to it later.
					pos = fout.getChannel().position();

					//Write a 4-byte integer to the stream. We'll re-use it later.
					fout.write(ByteOperations.toBytes(0));

					//Write the data fork (file contents) to the stream.
					in = entry.getInputStream();

					if (gzip)
					{
						gos = new GZIPOutputStream(fout);

						CO.writeInputStreamToOutputStream(entry.getInputStream(), gos);

						gos.finish();
					}
					else
					{
						CO.writeInputStreamToOutputStream(entry.getInputStream(), fout);
					}

					in.close();

					//Recalculate the length of the file and add it before the data fork.
					//Due to compression the actual length may be different from the one
					//advertised by the getLength function.
					//We reduce the length by 4 bytes because the 4 bytes are introduced
					//by the 4-byte integer placed before the data fork (contains the length).
					length = (int) (fout.getChannel().position() - pos - 4);

					//Go back to the 4-byte integer we stored before the data fork.
					fout.getChannel().position(pos);

					//Write the length to the stream.
					fout.write(ByteOperations.toBytes(length));

					//Restore the position to the end of the file.
					fout.getChannel().position(fout.getChannel().size());
				}

				//Write a hash of the resource ID's to the file for validation.
				fout.write(CO.makeHash(sb.toString(), "SHA-512")); //64 bytes

				fout.close();

				fin = new FileInputStream(file);
				fout = new FileOutputStream(this, false);
				CO.writeInputStreamToOutputStream(fin, fout);
			}
			finally
			{
				CO.closeOutputStream(fout);
				CO.closeOutputStream(gos);
				CO.closeInputStream(in);
				CO.closeInputStream(fin);
			}
		}
	}
}