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

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 
 * @author qwer <hellraz0r.386@googlemail.com>
 */
public class AllowedElementsParser
{
	private Map<String, List<String>>	allowedElements	= new HashMap<>();
	
	public AllowedElementsParser(final InputStream in) throws IOException, ParseException
	{
		if (in == null) { throw new NullPointerException(); }
		
		final StringBuffer sb = new StringBuffer();
		
		int read;
		while ((read = in.read()) != -1)
		{
			sb.append((char) read);
		}
		
		parse(sb.toString());
	}
	
	public AllowedElementsParser(final String allowedElementsString) throws ParseException
	{
		if (allowedElementsString == null) { throw new NullPointerException(); }
		
		parse(allowedElementsString);
	}
	
	public boolean isElementAllowed(final String elementName)
	{
		return allowedElements.containsKey(elementName);
	}
	
	public boolean isAttributeAllowed(final String elementName, final String attributeName)
	{
		if (elementName == null || attributeName == null) { throw new NullPointerException(); }
		
		final List<String> allowedAttributesForAll = allowedElements.get("*");
		if (allowedAttributesForAll != null && allowedAttributesForAll.contains(attributeName)) { return true; }
		
		final List<String> allowedAttributes = allowedElements.get(elementName);
		if (allowedAttributes == null) { return false; }
		
		return allowedAttributes.contains(attributeName) || allowedAttributes.contains("*");
	}
	
	private void parse(final String allowedElementsString) throws ParseException
	{
		final int length = allowedElementsString.length();
		final StringBuilder sb = new StringBuilder(allowedElementsString);
		
		ParserMode mode = ParserMode.NORMAL;
		ParserMode preCommentsMode = null;
		
		String[] currentElements = null;
		
		StringBuilder buffer = new StringBuilder();
		while (sb.length() > 0)
		{
			final char c = removeFirstFromString(sb);
			// Ignore whitespaces
			if (c != ' ' && c != '\t' && c != '\r' && c != '\n')
			{
				if (mode != ParserMode.COMMENT && c == '/' && sb.length() > 0 && sb.charAt(0) == '*')
				{
					sb.deleteCharAt(0);
					preCommentsMode = mode;
					mode = ParserMode.COMMENT;
					continue;
				}
				else if (mode == ParserMode.COMMENT && c == '*' && sb.length() > 0 && sb.charAt(0) == '/')
				{
					sb.deleteCharAt(0);
					mode = preCommentsMode;
					continue;
				}
				else if (mode == ParserMode.NORMAL)
				{
					if (c != '(') { throw new ParseException("Expected (, got " + c, allowedElementsString.length()
							- length); }
					mode = ParserMode.IN_ELEMENT_DEF;
				}
				else if (mode == ParserMode.IN_ELEMENT_DEF)
				{
					if (c == ')')
					{
						currentElements = buffer.toString().split(Pattern.quote(","));
						buffer = new StringBuilder();
						mode = ParserMode.AFTER_ELEMENT_DEF;
					}
					else
					{
						buffer.append(c);
					}
				}
				else if (mode == ParserMode.AFTER_ELEMENT_DEF)
				{
					if (c != ':') { throw new ParseException("Expected : after elements definition, got " + c,
							allowedElementsString.length() - length); }
					mode = ParserMode.BEFORE_ATTRIBUTE_DEF;
				}
				else if (mode == ParserMode.BEFORE_ATTRIBUTE_DEF)
				{
					if (c != '(') { throw new ParseException("Expected ( after :, got " + c,
							allowedElementsString.length() - length); }
					mode = ParserMode.IN_ATTRIBUTE_DEF;
				}
				else if (mode == ParserMode.IN_ATTRIBUTE_DEF)
				{
					if (c == ')')
					{
						final String[] currentAttributes = buffer.toString().split(Pattern.quote(","));
						buffer = new StringBuilder();
						
						for (final String element : currentElements)
						{
							List<String> list = this.allowedElements.get(element);
							if (list == null)
							{
								list = new ArrayList<String>(currentAttributes.length);
							}
							for (final String attribute : currentAttributes)
							{
								if (attribute.length() > 0 && !list.contains(attribute))
								{
									list.add(attribute);
								}
							}
							this.allowedElements.put(element, list);
						}
						
						mode = ParserMode.NORMAL;
					}
					else
					{
						buffer.append(c);
					}
				}
			}
		}
		allowedElements.remove("");
	}
	
	private char removeFirstFromString(final StringBuilder sb)
	{
		final char c = sb.charAt(0);
		sb.deleteCharAt(0);
		return c;
	}
	
	private enum ParserMode
	{
		NORMAL, COMMENT, IN_ELEMENT_DEF, AFTER_ELEMENT_DEF, BEFORE_ATTRIBUTE_DEF, IN_ATTRIBUTE_DEF
	}
}
