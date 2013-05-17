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
import java.io.OutputStream;
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
import com.cellngine.crypto.RC4;
import com.cellngine.io.CommonInputStream;
import com.cellngine.io.DelayedStreamCipherInputStream;
import com.cellngine.io.StreamCipherInputStream;
import com.cellngine.io.StreamCipherOutputStream;

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
	 *     - Resource ID (Length of String + String, SHA-512 hash of the bytes)
	 *     - Per file name:
	 *         - File name (Length of String + String)
	 *     - Compression algorithm
	 *        0x00 = No compression
	 *        0x01 = GZip compression
	 *     - File contents (Length in bytes + Bytes)
	 *  - Hash (64-byte SHA-512 hash of the resource id's of all entries)
	 *
	 */

	private static final long			serialVersionUID	= -514000992190910068L;
	private final List<ResourceEntry>	entries				= new Vector<ResourceEntry>();
	private byte[]						encryptionSeed		= null;

	public ResourceFile(final URI uri, final byte[] encryptionSeed) throws FileNotFoundException, IOException,
			NoSuchAlgorithmException
	{
		super(uri);
		this.init(encryptionSeed);
	}

	public ResourceFile(final URI uri) throws FileNotFoundException, IOException, NoSuchAlgorithmException
	{
		this(uri, null);
	}

	public ResourceFile(final String parent, final String child, final byte[] encryptionSeed)
			throws FileNotFoundException, IOException, NoSuchAlgorithmException
	{
		super(parent, child);
		this.init(encryptionSeed);
	}

	public ResourceFile(final String parent, final String child) throws FileNotFoundException, IOException,
			NoSuchAlgorithmException
	{
		this(parent, child, null);
	}

	public ResourceFile(final String pathname, final byte[] encryptionSeed) throws FileNotFoundException, IOException,
			NoSuchAlgorithmException
	{
		super(pathname);
		this.init(encryptionSeed);
	}

	public ResourceFile(final String pathname) throws FileNotFoundException, IOException, NoSuchAlgorithmException
	{
		this(pathname, (byte[]) null);
	}

	public ResourceFile(final File parent, final String child, final byte[] encryptionSeed)
			throws FileNotFoundException, IOException, NoSuchAlgorithmException
	{
		super(parent, child);
		this.init(encryptionSeed);
	}

	public ResourceFile(final File parent, final String child) throws FileNotFoundException, IOException,
			NoSuchAlgorithmException
	{
		this(parent, child, null);
	}

	public ResourceFile(final File file, final byte[] encryptionSeed) throws IOException, NoSuchAlgorithmException
	{
		this(file.getAbsolutePath(), encryptionSeed);
	}

	public ResourceFile(final File file) throws IOException, NoSuchAlgorithmException
	{
		this(file.getAbsolutePath(), (byte[]) null);
	}

	private void init(final byte[] encryptionSeed) throws FileNotFoundException, IOException, NoSuchAlgorithmException
	{
		this.encryptionSeed = encryptionSeed;

		if (this.exists())
		{
			FileInputStream fin = null;
			CommonInputStream in = null;

			try
			{
				//The specifications of the file structure is at the top of this class file.

				fin = new FileInputStream(this);
				byte[] buffer;

				buffer = new byte[8];
				fin.read(buffer);

				if (buffer[0] != 0x00 || buffer[1] != 0x04 || buffer[2] != 67 || buffer[3] != 82 || buffer[4] != 70
						|| buffer[5] != 0x27 || buffer[6] != 0x44 || buffer[7] != 0x02) { throw new RuntimeException(
						"Resource file is not valid."); }

				final int version = ByteOperations.getInt(fin);

				if (version == 1)
				{
					final boolean encryption = fin.read() == 0x01;
					final int pos = (int) fin.getChannel().position();

					if (encryption)
					{
						//If no hash was provided we'll just provide our own simple one,
						//this way the regular validation will fail down the line.
						in = new CommonInputStream(new StreamCipherInputStream(fin, new RC4(
								encryptionSeed == null ? new byte[] { 0 } : encryptionSeed)));
					}
					else
					{
						in = new CommonInputStream(fin);
					}

					fin = null;

					final int entries = ByteOperations.getInt(in);

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
						resourceID = ByteOperations.getString(in);

						sb.append(resourceID);

						fileNamesLength = ByteOperations.getInt(in);
						fileNames = new TreeSet<String>();

						for (int j = 0; j < fileNamesLength; j++)
						{
							fileNames.add(ByteOperations.getString(in));
						}

						compression = in.read();

						bytesLength = ByteOperations.getInt(in);

						fileOffset = pos + (int) in.getPosition();

						in.forceSkip(bytesLength);

						entry = new ResourceEntry(this, resourceID, fileOffset, bytesLength, compression);
						entry.addFileNames(fileNames);

						this.entries.add(entry);
					}

					final byte[] hash = new byte[64];
					int i;
					int bytesRead = 0;

					do
					{
						buffer = new byte[64];

						i = in.read(buffer);

						System.arraycopy(buffer, 0, hash, bytesRead, i);
						bytesRead = bytesRead + i;
					} while (i > -1 && bytesRead < 64);

					if (!CO.toString(hash).equals(CO.toString(CO.makeHash(sb.toString(), "SHA-512")))) { throw new RuntimeException(
							"Validation failed for resource file \"" + this.getAbsolutePath() + "\""); }
				}
				else
				{
					throw new RuntimeException("This resource file has a higher version (" + version
							+ ") than is supported.");
				}
			}
			finally
			{
				CO.closeInputStream(in);
				CO.closeInputStream(fin);
			}
		}
	}

	protected InputStream getInputStream() throws FileNotFoundException, IOException
	{
		final FileInputStream fin = new FileInputStream(this);
		InputStream in = fin;

		if (this.encryptionSeed != null)
		{
			in = new DelayedStreamCipherInputStream(fin, new RC4(this.encryptionSeed), 13);
		}

		return in;
	}

	/**
	 * @return The seed on which the encryption is based. This may be {@code null} if no encryption
	 *         is used for this file.
	 */
	public byte[] getEncryptionSeed()
	{
		return this.encryptionSeed;
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
	 * @return A list of {@link com.cellngine.resources.ResourceEntry ResourceEntry} objects
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
		this.write(this.encryptionSeed);
	}

	/**
	 * Writes the resource file to disk, overwriting an existing file if present.
	 *
	 * @param encryptionSeed
	 *            The seed to use for encryption. Provide {@code null} to disable encryption.
	 * @throws FileNotFoundException
	 *             If the file could not be created.
	 * @throws IOException
	 *             If there was an error during the creation of the file.
	 * @throws NoSuchAlgorithmException
	 *             If the SHA-512 algorithm is not available to the Java virtual machine.
	 */
	public void write(final byte[] encryptionSeed) throws FileNotFoundException, IOException, NoSuchAlgorithmException
	{
		synchronized (this.entries)
		{
			this.encryptionSeed = encryptionSeed;

			FileOutputStream fout = null;
			InputStream in = null;
			FileInputStream fin = null;
			OutputStream out = null;
			OutputStream dataout = null;

			try
			{
				//The specifications of the file structure is at the top of this class file.

				/*
				 * Temporary file #1: Stores the contents of the resource file. Will be copied to the
				 * permanent file once finished. This is required because the ResourceEntry objects
				 * that belong to this ResourceFile object may require the original file.
				 */
				final File file = File.createTempFile("crf", CO.toString(this.hashCode()));

				/*
				 * Temporary file #2: Stores the data fork of the individual ResourceEntry objects.
				 * This is needed so we can determine the length of the file prior to writing it.
				 */
				final File tempfile = File.createTempFile("crft", CO.toString(this.hashCode()));

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

				if (encryptionSeed == null)
				{
					fout.write(0x00);
					fout.flush();

					out = fout;
				}
				else
				{
					fout.write(0x01);
					fout.flush();

					out = new StreamCipherOutputStream(fout, new RC4(encryptionSeed));
				}

				fout = null;

				//Amount of entries (stored as a 4-byte integer)
				out.write(ByteOperations.toBytes(this.entries.size()));

				final StringBuilder sb = new StringBuilder();
				int length;
				boolean gzip;

				for (final ResourceEntry entry : this.entries)
				{
					sb.append(entry.getResourceID());

					//Resource ID (stored as a 4-byte integer)
					out.write(ByteOperations.toBytes(entry.getResourceID()));

					//Amount of file names (stored as a 4-byte integer)
					out.write(ByteOperations.toBytes(entry.getFileNames().size()));

					for (final String fileName : entry.getFileNames())
					{
						out.write(ByteOperations.toBytes(fileName));
					}

					gzip = entry.isGzip();

					//Compression algorithm: 0x00 for none, 0x01 for GZip
					if (gzip)
					{
						out.write(0x01);
					}
					else
					{
						out.write(0x00);
					}

					//Calculate the length of the file and add it before the data fork.
					//Due to compression the actual length may be different from the one
					//advertised by the getLength function.
					length = 0;
					in = entry.getInputStream();
					fout = new FileOutputStream(tempfile, false);

					if (gzip)
					{
						dataout = new GZIPOutputStream(fout);
					}
					else
					{
						dataout = fout;
					}

					CO.writeInputStreamToOutputStream(in, dataout);

					dataout.close();
					fout.close();
					in.close();

					in = new FileInputStream(tempfile);
					length = CO.getLengthOfInputStream(in);
					in.close();

					//Store the current position, because we'll need to get
					//back to it later.

					//Write a 4-byte integer to the stream. We'll re-use it later.
					out.write(ByteOperations.toBytes(length));

					//Write the data fork (file contents) to the stream.
					in = new FileInputStream(tempfile);

					CO.writeInputStreamToOutputStream(in, out);

					in.close();
				}

				//Write a hash of the resource ID's to the file for validation.
				out.write(CO.makeHash(sb.toString(), "SHA-512")); //assumption: this is always 64 bytes

				out.close();

				fin = new FileInputStream(file);
				fout = new FileOutputStream(this, false);

				CO.writeInputStreamToOutputStream(fin, fout);

				fout.close();
				fin.close();

				file.delete();
				tempfile.delete();
			}
			finally
			{
				CO.closeOutputStream(dataout);
				CO.closeOutputStream(out);
				CO.closeInputStream(fin);
				CO.closeInputStream(in);
				CO.closeOutputStream(fout);
			}
		}
	}
}