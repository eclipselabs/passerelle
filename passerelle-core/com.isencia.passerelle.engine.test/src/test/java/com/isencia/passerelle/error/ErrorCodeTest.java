package com.isencia.passerelle.error;

import com.isencia.passerelle.core.ErrorCategory;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.ErrorCode.Severity;
import junit.framework.TestCase;

public class ErrorCodeTest extends TestCase {
  
  static class TestErrorCategory extends ErrorCategory {
    public final static ErrorCategory ROOT = new TestErrorCategory("ROOT", null, "ROOT");
    public final static ErrorCategory T1 = new TestErrorCategory("T1", ROOT, "T1");
    public final static ErrorCategory T2 = new TestErrorCategory("T2", ROOT, "T2");

//    public final static ErrorCategory T3 = new ErrorCategory("T3", ErrorCategory.PASS_ROOTCATEGORY, "T3");
    
    /**
     * @param name
     * @param parent
     * @param prefix
     */
    public TestErrorCategory(String name, ErrorCategory parent, String prefix) {
      super(name, parent, prefix);
    }
    
  }

  @Override
  protected void tearDown() throws Exception {
    TestErrorCode.clear();
  }

  public void testErrorCategoryTECHNICAL() {
    assertNotNull("Should have found TECHNICAL category",ErrorCategory.valueOf("TECHNICAL"));
  }
  
  public void testErrorCategoryFUNCTIONAL() {
    assertNotNull("Should have found FUNCTIONAL category",ErrorCategory.valueOf("FUNCTIONAL"));
  }
  
  public void testErrorCategoryExtras1() {
    ErrorCategory ec = TestErrorCategory.T1;
    assertTrue("Extra error category T1 should be found",ErrorCategory.values().contains(ec));
  }
  
  public void testErrorCategoryExtras2() {
    ErrorCategory ec = TestErrorCategory.T1;
    assertEquals("Extra error category T1 should be found",ec,ErrorCategory.valueOf("T1"));
  }
  
  public void testErrorCodeConstruction() {
    TestErrorCode TESTERRCODE = new TestErrorCode("TESTERRCODE", "1234", ErrorCategory.FUNCTIONAL, Severity.ERROR, "nothing special");
    assertEquals(TESTERRCODE, ErrorCode.valueOf("TESTERRCODE"));
  }
  
  public void testErrorCodeAutoTopic() {
    TestErrorCode TESTERRCODE = new TestErrorCode("TESTERRCODE", "1234", ErrorCategory.FUNCTIONAL, Severity.ERROR, "nothing special");
    assertEquals("FUNC/ERROR/TESTERRCODE", TESTERRCODE.getTopic());
  }
  
  public void testErrorCodeSpecificTopic() {
    TestErrorCode TESTERRCODE = new TestErrorCode("TESTERRCODE", "1234", "myTopic", ErrorCategory.FUNCTIONAL, Severity.ERROR, "nothing special");
    assertEquals("myTopic", TESTERRCODE.getTopic());
  }
  
//TODO fix tests below, throw IllegalArgEx instead?
  public void ___testErrorCodeInvalidCodeTooLong() {
    try {
      new TestErrorCode("TESTERRCODE", "12345", ErrorCategory.FUNCTIONAL, Severity.ERROR, "nothing special");
      fail("Invalid error code 12345 must be refused");
    } catch (AssertionError e) {
    }
  }
  
  public void ___testErrorCodeInvalidCodeTooShort() {
    try {
      new TestErrorCode("TESTERRCODE", "123", ErrorCategory.FUNCTIONAL, Severity.ERROR, "nothing special");
      fail("Invalid error code 123 must be refused");
    } catch (AssertionError e) {
    }
  }
  
  public void ___testErrorCodeInvalidCodeAlphaNumeric() {
    try {
      new TestErrorCode("TESTERRCODE", "12C4", ErrorCategory.FUNCTIONAL, Severity.ERROR, "nothing special");
      fail("Invalid error code 12C4 must be refused");
    } catch (AssertionError e) {
    }
  }
}
