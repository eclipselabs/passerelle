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

import junit.framework.TestCase;

/**
 * ReverseStringTokenizerTest
 * 
 * @author erwin
 */
public class ReverseStringTokenizerTest extends TestCase {

  ReverseStringTokenizer rstDontReturnDelim = null;
  ReverseStringTokenizer rstReturnDelim = null;
  private final static String TEST_STRING = "   This is a test sentence for    the Reverse String; Tokenizer.   ";
  private final static String[] TEST_TOKENS_DONT_RETURN_DELIM = { "Tokenizer.", "String;", "Reverse", "the", "for", "sentence", "test", "a", "is", "This" };
  private final static String[] TEST_TOKENS_RETURN_DELIM = { " ", " ", " ", "Tokenizer.", " ", "String;", " ", "Reverse", " ", "the", " ", " ", " ", " ",
      "for", " ", "sentence", " ", "test", " ", "a", " ", "is", " ", "This", " ", " ", " " };
  private final static String TEST_TOKEN_DOT_DELIM_DONT_RETURN_DELIM = "Tokenizer";
  private final static String TEST_TOKEN_SEMICOLON_DELIM_DONT_RETURN_DELIM = "String";
  private final static String TEST_TOKEN_DOT_DELIM_RETURN_DELIM = " ";
  private final static String TEST_TOKEN_SEMICOLON_DELIM_RETURN_DELIM = " ";

  /*
   * @see TestCase#setUp()
   */
  protected void setUp() throws Exception {
    rstDontReturnDelim = new ReverseStringTokenizer(TEST_STRING, " ");
    rstReturnDelim = new ReverseStringTokenizer(TEST_STRING, " ", true);
  }

  /*
   * @see TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
    rstDontReturnDelim = null;
    rstReturnDelim = null;
  }

  public void testCountTokensDontReturnDelim() {
    assertEquals(TEST_TOKENS_DONT_RETURN_DELIM.length, rstDontReturnDelim.countTokens());
  }

  public void testHasMoreElementsDontReturnDelim() {
    assertTrue("Should have more elements at the start", rstDontReturnDelim.hasMoreElements());
    for (int i = 0; i < TEST_TOKENS_DONT_RETURN_DELIM.length; ++i) {
      rstDontReturnDelim.nextToken();
    }
    assertFalse("Should NOT have more elements at the end", rstDontReturnDelim.hasMoreElements());
  }

  public void testHasMoreTokensDontReturnDelim() {
    assertTrue("Should have more tokens at the start", rstDontReturnDelim.hasMoreTokens());
    for (int i = 0; i < TEST_TOKENS_DONT_RETURN_DELIM.length; ++i) {
      rstDontReturnDelim.nextToken();
    }
    assertFalse("Should NOT have more tokens at the end", rstDontReturnDelim.hasMoreTokens());
  }

  public void testNextElementDontReturnDelim() {
    int i = 0;
    while (rstDontReturnDelim.hasMoreElements()) {
      assertEquals("Element[" + i + "] not correct", TEST_TOKENS_DONT_RETURN_DELIM[i++], rstDontReturnDelim.nextElement());
    }
  }

  public void testNextTokenDontReturnDelim() {
    int i = 0;
    while (rstDontReturnDelim.hasMoreTokens()) {
      assertEquals("Token[" + i + "] not correct", TEST_TOKENS_DONT_RETURN_DELIM[i++], rstDontReturnDelim.nextToken());
    }
  }

  public void testNextTokenStringDontReturnDelim() {
    assertEquals(TEST_TOKEN_DOT_DELIM_DONT_RETURN_DELIM, rstDontReturnDelim.nextToken(". "));
    assertEquals(TEST_TOKEN_SEMICOLON_DELIM_DONT_RETURN_DELIM, rstDontReturnDelim.nextToken("; "));
  }

  public void testCountTokensReturnDelim() {
    assertEquals(TEST_TOKENS_RETURN_DELIM.length, rstReturnDelim.countTokens());
  }

  public void testHasMoreElementsReturnDelim() {
    assertTrue("Should have more elements at the start", rstReturnDelim.hasMoreElements());
    for (int i = 0; i < TEST_TOKENS_RETURN_DELIM.length; ++i) {
      rstReturnDelim.nextToken();
    }
    assertFalse("Should NOT have more elements at the end", rstReturnDelim.hasMoreElements());
  }

  public void testHasMoreTokensReturnDelim() {
    assertTrue("Should have more tokens at the start", rstReturnDelim.hasMoreTokens());
    for (int i = 0; i < TEST_TOKENS_RETURN_DELIM.length; ++i) {
      rstReturnDelim.nextToken();
    }
    assertFalse("Should NOT have more tokens at the end", rstReturnDelim.hasMoreTokens());
  }

  public void testNextElementReturnDelim() {
    int i = 0;
    while (rstReturnDelim.hasMoreElements()) {
      assertEquals("Element[" + i + "] not correct", TEST_TOKENS_RETURN_DELIM[i++], rstReturnDelim.nextElement());
    }
  }

  public void testNextTokenReturnDelim() {
    int i = 0;
    while (rstReturnDelim.hasMoreTokens()) {
      assertEquals("Token[" + i + "] not correct", TEST_TOKENS_RETURN_DELIM[i++], rstReturnDelim.nextToken());
    }
  }

  public void testNextTokenStringReturnDelim() {
    assertEquals(TEST_TOKEN_DOT_DELIM_RETURN_DELIM, rstReturnDelim.nextToken(". "));
    assertEquals(TEST_TOKEN_SEMICOLON_DELIM_RETURN_DELIM, rstReturnDelim.nextToken("; "));
  }

}
