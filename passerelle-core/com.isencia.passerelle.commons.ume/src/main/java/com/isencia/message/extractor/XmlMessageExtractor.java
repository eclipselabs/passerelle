/* Copyright 2011 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.message.extractor;

import java.io.IOException;
import java.io.Reader;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XmlMessageExtractor TODO: class comment
 * 
 * @author erwin
 */
public class XmlMessageExtractor extends DefaultHandler implements IMessageExtractor, ContentHandler {

  private final static Logger logger = LoggerFactory.getLogger(XmlMessageExtractor.class);

  private XMLReader xmlReader = null;
  private StringBuffer buffer = null;
  private InputSource input = null;
  private SAXParser saxParser = null;
  private int elementCount = 0;
  private Reader reader = null;
  private boolean characterFound = false;

  /** Creates a new instance */
  public XmlMessageExtractor() {
    // this property should be set externally, e.g. in jre/lib/jaxp.properties
    // or using some other means
    // in here, it can cause clashes with other env settings
    // System.getProperties().put("javax.xml.parsers.SAXParserFactory","org.apache.xerces.jaxp.SAXParserFactoryImpl");
    SAXParserFactory saxFactory = SAXParserFactory.newInstance();

    try {
      saxParser = saxFactory.newSAXParser();
    } catch (ParserConfigurationException e) {
    } catch (SAXException e) {
    }
  }

  /**
   * @return
   */
  public boolean initialize() {
    if (logger.isTraceEnabled()) {
      logger.trace("initialize() - entry");
    }
    characterFound = false;
    elementCount = 0;
    buffer = new StringBuffer();

    try {
      xmlReader = saxParser.getXMLReader();
      xmlReader.setContentHandler(this);
      xmlReader.setErrorHandler(this);
      xmlReader.setFeature("http://xml.org/sax/features/validation", false);
      input = new InputSource(new ReaderWrapper(this.reader));
    } catch (SAXNotRecognizedException e) {
      return false;
    } catch (SAXNotSupportedException e) {
      return false;
    } catch (SAXException e) {
      return false;
    }
    if (logger.isTraceEnabled()) {
      logger.trace("initialize() - exit");
    }
    return true;
  }

  public Object getMessage() {
    if (logger.isTraceEnabled()) {
      logger.trace("getMessage() - entry");
    }

    if (!initialize()) {
      if (logger.isTraceEnabled()) {
        logger.trace("getMessage() - exit - not yet initialized! Returning null.");
      }
      return null;
    }
    String message = null;
    try {
      xmlReader.parse(input);
    } catch (IOException e) {
      logger.error("getMessage()", e);
    } catch (SAXEndMessageException e) {
      // received an EndMessage, Return the data
      message = buffer.toString().trim();
    } catch (SAXParseException e) {
      // Trick to prevent error messages if nothing more on stream
      if (buffer.length() > 0) logger.error("getMessage()", e);
    } catch (SAXException e) {
      logger.error("getMessage()", e);
    }
    if (logger.isTraceEnabled()) {
      logger.trace("getMessage() - exit - result :" + message);
    }
    return message;
  }

  public void close() {
    if (logger.isTraceEnabled()) {
      logger.trace("close() - entry");
    }
    if (reader != null) {
      try {
        reader.close();
      } catch (IOException e) {
        logger.error("close() - Error closing reader", e);
      }
    }
    if (logger.isTraceEnabled()) {
      logger.trace("close() - exit");
    }
  }

  public boolean isOpen() {
    return true;
  }

  public void open(Reader reader) {
    if (logger.isTraceEnabled()) {
      logger.trace("open() - entry - reader :" + reader);
    }
    this.reader = reader;
    if (logger.isTraceEnabled()) {
      logger.trace("open() - exit");
    }
  }

  public void startElement(String arg0, String arg1, String arg2, Attributes arg3) throws SAXException {
    super.startElement(arg0, arg1, arg2, arg3);

    elementCount++;
  }

  public void endElement(String arg0, String arg1, String arg2) throws SAXException {
    super.endElement(arg0, arg1, arg2);

    elementCount--;

    if (elementCount == 0) {
      // End of a parse session reached
      if (logger.isDebugEnabled()) {
        logger.debug("End of parse");
      }
      throw new SAXEndMessageException();
    }
  }

  /**
   * SAXEndMessageException
   * 
   * @author erwin
   */
  private class SAXEndMessageException extends SAXException {
    private static final long serialVersionUID = 1L;

    SAXEndMessageException() {
      super("EndMessage");
    }

  }

  /**
   * ReaderWrapper
   * 
   * @author erwin
   */
  private class ReaderWrapper extends Reader {
    Reader reader = null;

    ReaderWrapper(Reader reader) {
      this.reader = reader;
    }

    /**
     * @see java.io.Reader#close()
     */
    public void close() throws IOException {
    }

    /**
     * @see java.io.Reader#read(char[], int, int)
     */
    public int read(char[] array, int offset, int length) throws IOException {
      if (logger.isTraceEnabled()) {
        logger.trace("read() - entry - offset :" + offset + " - length :" + length);
      }

      if (array == null) throw new IOException("Null array");

      // Do actual read
      char[] c = new char[1];

      int count = 0;

      if (!characterFound) {
        // Skip rubish until valid character found
        do {
          count = reader.read(c, 0, 1);
        } while (count > 0 && c[0] != '<' && c[0] != -1);

        if (count == 0)
          return 0;
        else
          characterFound = true;
      } else
        count = reader.read(c, 0, 1);

      if (count > 0 && c[0] != -1) {
        buffer.append(array[offset] = c[0]);
      } else if (logger.isDebugEnabled()) {
        logger.debug("read() - End of file found");
      }

      if (logger.isTraceEnabled()) {
        logger.trace("read() - exit - count :" + count);
      }
      return count;
    }
  }

  public IMessageExtractor cloneExtractor() {
    IMessageExtractor result = new XmlMessageExtractor();
    return result;
  }

}