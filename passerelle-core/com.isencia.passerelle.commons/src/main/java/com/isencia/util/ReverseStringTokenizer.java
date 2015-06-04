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
package com.isencia.util;

import java.util.NoSuchElementException;

/**
 * ReverseStringTokenizer Based on java.util.StringTokenizer, but traverses the
 * original string in reverse direction, thus first returning the last token
 * etc.
 * 
 * @author erwin
 */
public class ReverseStringTokenizer implements java.util.Enumeration {

  private int currentPosition;

  private int endPosition;

  private String str;

  private String delimiters;

  private boolean retTokens;

  /**
   * Constructs a string tokenizer for the specified string. The tokenizer uses
   * the default delimiter set, which is <code>"&#92;t&#92;n&#92;r&#92;f"</code>
   * : the space character, the tab character, the newline character, the
   * carriage-return character, and the form-feed character. Delimiter
   * characters themselves will not be treated as tokens.
   * 
   * @param str a string to be parsed.
   */
  public ReverseStringTokenizer(String str) {
    this(str, " \t\n\r\f", false);
  }

  /**
   * Constructs a string tokenizer for the specified string. The characters in
   * the <code>delim</code> argument are the delimiters for separating tokens.
   * Delimiter characters themselves will not be treated as tokens.
   * 
   * @param str a string to be parsed.
   * @param delim the delimiters.
   */
  public ReverseStringTokenizer(String str, String delim) {
    this(str, delim, false);
  }

  /**
   * Constructs a string tokenizer for the specified string. All characters in
   * the <code>delim</code> argument are the delimiters for separating tokens.
   * <p>
   * If the <code>returnTokens</code> flag is <code>true</code>, then the
   * delimiter characters are also returned as tokens. Each delimiter is
   * returned as a string of length one. If the flag is <code>false</code>, the
   * delimiter characters are skipped and only serve as separators between
   * tokens.
   * 
   * @param str a string to be parsed.
   * @param delim the delimiters.
   * @param returnTokens flag indicating whether to return the delimiters as
   *          tokens.
   */
  public ReverseStringTokenizer(String str, String delim, boolean returnTokens) {
    this.str = str;
    endPosition = 0;
    currentPosition = str.length() - 1;
    delimiters = delim;
    retTokens = returnTokens;
  }

  /**
   * Calculates the number of times that this tokenizer's <code>nextToken</code>
   * method can be called before it generates an exception. The current position
   * is not advanced.
   * 
   * @return the number of tokens remaining in the string using the current
   *         delimiter set.
   * @see java.util.StringTokenizer#nextToken()
   */
  public int countTokens() {
    int count = 0;
    int currpos = currentPosition;

    while (currpos >= endPosition) {
      /*
       * This is just skipDelimiters(); but it does not affect currentPosition.
       */
      while (!retTokens && (currpos > endPosition) && (delimiters.indexOf(str.charAt(currpos)) >= 0)) {
        currpos--;
      }

      if (currpos < endPosition) {
        break;
      }

      int start = currpos;
      while ((currpos > endPosition) && (delimiters.indexOf(str.charAt(currpos)) < 0)) {
        currpos--;
      }
      if (retTokens) {
        if ((start == currpos) && (delimiters.indexOf(str.charAt(currpos)) >= 0)) {
          currpos--;
        }
        count++;
      } else if (start != currpos) {
        count++;
      } else {
        // currpos was not moved, so it means there's only delimiters left
        // in the remainder of the string
        break;
      }

    }
    return count;
  }

  /**
   * Returns the same value as the <code>hasMoreTokens</code> method. It exists
   * so that this class can implement the <code>Enumeration</code> interface.
   * 
   * @return <code>true</code> if there are more tokens; <code>false</code>
   *         otherwise.
   * @see java.util.Enumeration
   * @see java.util.StringTokenizer#hasMoreTokens()
   */
  public boolean hasMoreElements() {
    return hasMoreTokens();
  }

  /**
   * Tests if there are more tokens available from this tokenizer's string. If
   * this method returns <tt>true</tt>, then a subsequent call to
   * <tt>nextToken</tt> with no argument will successfully return a token.
   * 
   * @return <code>true</code> if and only if there is at least one token in the
   *         string after the current position; <code>false</code> otherwise.
   */
  public boolean hasMoreTokens() {
    skipDelimiters();
    return (currentPosition >= endPosition);
  }

  /**
   * Returns the same value as the <code>nextToken</code> method, except that
   * its declared return value is <code>Object</code> rather than
   * <code>String</code>. It exists so that this class can implement the
   * <code>Enumeration</code> interface.
   * 
   * @return the next token in the string.
   * @exception NoSuchElementException if there are no more tokens in this
   *              tokenizer's string.
   * @see java.util.Enumeration
   * @see java.util.StringTokenizer#nextToken()
   */
  public Object nextElement() {
    return nextToken();
  }

  /**
   * Returns the next token from this string tokenizer.
   * 
   * @return the next token from this string tokenizer.
   * @exception NoSuchElementException if there are no more tokens in this
   *              tokenizer's string.
   */
  public String nextToken() {
    skipDelimiters();

    if (currentPosition < endPosition) {
      throw new java.util.NoSuchElementException();
    }

    int start = currentPosition;
    while ((currentPosition >= endPosition) && (delimiters.indexOf(str.charAt(currentPosition)) < 0)) {
      currentPosition--;
    }
    if (retTokens && (start == currentPosition) && (delimiters.indexOf(str.charAt(currentPosition)) >= 0)) {
      currentPosition--;
    }
    return str.substring(currentPosition + 1, start + 1);
  }

  /**
   * Returns the next token in this string tokenizer's string. First, the set of
   * characters considered to be delimiters by this <tt>StringTokenizer</tt>
   * object is changed to be the characters in the string <tt>delim</tt>. Then
   * the next token in the string after the current position is returned. The
   * current position is advanced beyond the recognized token. The new delimiter
   * set remains the default after this call.
   * 
   * @param delim the new delimiters.
   * @return the next token, after switching to the new delimiter set.
   * @exception NoSuchElementException if there are no more tokens in this
   *              tokenizer's string.
   */
  public String nextToken(String delim) {
    delimiters = delim;
    return nextToken();
  }

  /**
   * Skips delimiters.
   */
  private void skipDelimiters() {
    while (!retTokens && (currentPosition >= endPosition) && (delimiters.indexOf(str.charAt(currentPosition)) >= 0)) {
      currentPosition--;
    }
  }

}