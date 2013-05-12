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
package com.cellngine.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Provides methods that check FXML files for security and validity.
 * 
 * @author qwer <hellraz0r.386@googlemail.com>
 */
public class FXMLValidator
{
	private static Log							LOG					= LogFactory.getLog(FXMLValidator.class);
	
	private static final Charset				FXML_CHARSET		= Charset.forName("UTF-8");
	private static final AllowedElementsParser	ALLOWED_ELEMENTS	= getAllowedElements();
	private static final List<String>			ALLOWED_IMPORTS		= Arrays.asList("java.lang.*", "java.util.*",
																			"javafx.geometry.*",
																			"javafx.collections.*",
																			"javafx.scene.control.*",
																			"javafx.scene.effect.*",
																			"javafx.scene.image.*",
																			"javafx.scene.input.*",
																			"javafx.scene.layout.*",
																			"javafx.scene.paint.*",
																			"javafx.scene.shape.*",
																			"javafx.scene.paint.*",
																			"javafx.scene.text.*", "javafx.scene.web.*");
	
	private static AllowedElementsParser getAllowedElements()
	{
		try
		{
			return new AllowedElementsParser(AllowedElementsParser.class.getResourceAsStream("AllowedElements.txt"));
		}
		catch (IOException | ParseException e)
		{
			LOG.error("Could not load FXML allowed elements", e);
		}
		return null;
	}
	
	/**
	 * Provides the same functionality as {@link #validate(InputStream)}, but accepts a string that
	 * will be wrapped in a {@link ByteBuffer} for convenience. Since no {@link IOException} should
	 * be thrown, all IOExceptions are caught and re-thrown as {@link RuntimeException}s.
	 * 
	 * @param fxmlAsString
	 *            The entire contents of the FXML file.
	 * @throws InvalidFXMLException
	 *             If the FXML document is invalid or insecure.
	 */
	public void validate(final String fxmlAsString) throws InvalidFXMLException
	{
		if (fxmlAsString == null) { throw new NullPointerException(); }
		
		final ByteArrayInputStream stream = new ByteArrayInputStream(fxmlAsString.getBytes(FXML_CHARSET));
		try
		{
			validate(stream);
		}
		catch (final IOException e)
		{
			LOG.error("Validating an XML string threw an IOException", e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Reads an FXML document from an {@link InputStream} and parses it. Checks are then performed
	 * to ensure that the FXML does not contain anything that might compromise a client trying to
	 * display it. This includes unknown components or JavaScript action handlers.
	 * 
	 * @param in
	 *            The {@link InputStream} to read the FXML from.
	 * @throws IOException
	 *             If an error occurred while retrieving the FXML from the stream.
	 * @throws InvalidFXMLException
	 *             If the FXML document is invalid or insecure.
	 */
	public void validate(final InputStream in) throws IOException, InvalidFXMLException
	{
		if (in == null) { throw new NullPointerException(); }
		
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try
		{
			builder = factory.newDocumentBuilder();
		}
		catch (final ParserConfigurationException e)
		{
			LOG.error("Could not initialize XML parser", e);
			return;
		}
		
		Document document = null;
		try
		{
			document = builder.parse(in);
		}
		catch (final SAXException e)
		{
			throw new InvalidFXMLException("Could not parse XML document", e);
		}
		
		checkNode(document);
	}
	
	private void checkNode(final Node node) throws InvalidFXMLException
	{
		final String nodeName = node.getNodeName();
		final short nodeType = node.getNodeType();
		
		if (nodeType == Node.ELEMENT_NODE)
		{
			if (!ALLOWED_ELEMENTS.isElementAllowed(nodeName)) { throw new InvalidFXMLException("Element type \""
					+ nodeName + "\" not allowed"); }
			
			final NamedNodeMap nodeAttributes = node.getAttributes();
			for (int i = 0; i < nodeAttributes.getLength(); i++)
			{
				checkAttributeNode(nodeAttributes.item(i), nodeName);
			}
		}
		else if (nodeType == Node.TEXT_NODE || nodeType == Node.DOCUMENT_NODE)
		{
		}
		else if (nodeType == Node.PROCESSING_INSTRUCTION_NODE && node.getNodeName().equals("import"))
		{
			if (!ALLOWED_IMPORTS.contains(node.getNodeValue())) { throw new InvalidFXMLException("Import \""
					+ node.getNodeValue() + "\" not allowed."); }
		}
		else if (nodeType != Node.COMMENT_NODE) { throw new InvalidFXMLException("Unrecognized node: type: \""
				+ nodeType + "\", name: \"" + node.getNodeName() + "\", value: \"" + node.getNodeValue() + "\""); }
		
		final NodeList nodeChildren = node.getChildNodes();
		for (int i = 0; i < nodeChildren.getLength(); i++)
		{
			checkNode(nodeChildren.item(i));
		}
	}
	
	private void checkAttributeNode(final Node node, final String parentName) throws InvalidFXMLException
	{
		final String nodeName = node.getNodeName();
		final String nodeValue = node.getNodeValue();
		
		if (node.getNodeType() == Node.ATTRIBUTE_NODE)
		{
			if (nodeName.equals("fx:factory"))
			{
				if (!nodeValue.equals("observableArrayList")) { throw new InvalidFXMLException("fx:factory \""
						+ nodeValue + "\" not allowed"); }
			}
			else if (!ALLOWED_ELEMENTS.isAttributeAllowed(parentName, node.getNodeName())) { throw new InvalidFXMLException(
					"Attribute \"" + node.getNodeName() + "\" on element type \"" + parentName + "\" not allowed"); }
		}
		else
		{
			throw new InvalidFXMLException("Node " + node.getNodeName() + " is not an attribute.");
		}
	}
	
	/**
	 * Thrown when an invalid or insecure XML document is encountered.
	 * 
	 * @author qwer <hellraz0r.386@googlemail.com>
	 */
	public static class InvalidFXMLException extends Exception
	{
		public InvalidFXMLException(final String message)
		{
			super(message);
		}
		
		public InvalidFXMLException(final String message, final Throwable t)
		{
			super(message, t);
		}
		
		private static final long	serialVersionUID	= 722605387466263303L;
	}
}
