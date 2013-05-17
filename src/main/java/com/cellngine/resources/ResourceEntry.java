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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import com.cellngine.CO;
import com.cellngine.io.BoundedInputStream;

/**
 * An entry within a {@link com.cellngine.resources.ResourceFile ResourceFile}.
 *
 * @author A.J.A. Boer <jboer@jboer.nl>
 */
public class ResourceEntry
{
	private final Set<String>	fileNames		= new TreeSet<String>();
	private String				resourceID		= "";
	private Integer				offset			= null;
	private byte[]				bytes			= null;
	private File				file			= null;
	private Integer				length			= null;
	private Integer				realLength		= null;
	private ResourceFile		resourceFile	= null;
	private Integer				compression		= null;

	protected ResourceEntry(final byte[] bytes, final String fileName)
	{
		this.bytes = bytes;
		this.fileNames.add(fileName);
	}

	protected ResourceEntry(final File file, final String fileName)
	{
		this.file = file;
		this.fileNames.add(fileName);
	}

	protected ResourceEntry(final ResourceFile resourceFile, final String resourceID, final int offset,
			final int length, final int compression)
	{
		this.resourceFile = resourceFile;
		this.resourceID = resourceID;
		this.offset = offset;
		this.realLength = length;
		this.compression = compression;
	}

	protected void addFileNames(final Set<String> fileNames)
	{
		this.fileNames.addAll(fileNames);
	}

	/**
	 * @return A set of filenames that correspond to this entry.
	 */
	public Set<String> getFileNames()
	{
		return Collections.unmodifiableSet(this.fileNames);
	}

	/**
	 * Returns an {@link java.io.InputStream InputStream} containing
	 * the contents of the file.<br />
	 * <br />
	 *
	 * Multiple calls to this function will result in newly created {@link java.io.InputStream
	 * InputStream} objects.
	 *
	 * @return An InputStream pointing to the file data.
	 */
	public InputStream getInputStream()
	{
		if (this.bytes != null)
		{
			return new ByteArrayInputStream(this.bytes);
		}
		else if (this.file != null)
		{
			try
			{
				return new FileInputStream(this.file);
			}
			catch (final FileNotFoundException e)
			{
				throw new RuntimeException(e);
			}
		}
		else if (this.resourceFile != null)
		{
			try
			{
				//Encryption is handled by the this.resourceFile.getInputStream() function
				InputStream in = this.resourceFile.getInputStream();

				in.skip(this.offset);

				in = new BoundedInputStream(in, this.realLength);

				if (this.compression == 0x01)
				{
					in = new GZIPInputStream(in);
				}

				return in;
			}
			catch (final IOException e)
			{
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		else
		{
			throw new RuntimeException("Could not determine a way to provide an InputStream.");
		}
	}

	/**
	 * @return The length of the file data in bytes.
	 * @throws IOException
	 *             If the length could not be determined.
	 */
	public int getLength() throws IOException
	{
		if (this.length == null)
		{
			int length = 0;

			InputStream in = null;

			try
			{
				in = this.getInputStream();

				byte[] buffer;
				int i;

				do
				{
					buffer = new byte[1024];

					i = in.read(buffer);

					if (i > -1)
					{
						length = length + i;
					}
				} while (i > -1);

				this.length = length;

			}
			finally
			{
				CO.closeInputStream(in);
			}
		}

		return this.length;
	}

	/**
	 * @return A hash of the file contents, used as a unique id within the resource file.
	 */
	public String getResourceID()
	{
		if (this.resourceID.isEmpty())
		{
			try
			{
				this.resourceID = CO.toString(CO.makeHash(this.getInputStream(), "SHA-512"));
			}
			catch (final Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		return this.resourceID;
	}

	/**
	 * @return {@code true} if this file has been gzip compressed (or in case of a new entry is
	 *         eligible for compression), {@code false} if no compression will be used.
	 * @throws IOException
	 *             If there was an error while reading the {@link java.io.InputStream InputStream}.
	 */
	public boolean isGzip() throws IOException
	{
		//If the length of the uncompressed file is larger than 32 bytes
		//then it may be feasible to apply gzip compression.
		return (this.compression != null && this.compression == 0x01)
				|| (this.compression == null && this.getLength() > 32);
	}
}