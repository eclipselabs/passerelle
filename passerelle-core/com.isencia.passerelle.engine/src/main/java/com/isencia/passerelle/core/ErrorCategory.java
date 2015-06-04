/**
 * 
 */
package com.isencia.passerelle.core;

import java.util.SortedSet;
import com.isencia.sherpa.commons.Enumerated;

/**
 * An extensible enumerator of hierarchical error categories.
 * <p>
 * 
 * </p>
 * 
 * @author erwin
 *
 */
public class ErrorCategory extends Enumerated<ErrorCode> {

  private static final long serialVersionUID = 1L;

  private static final char PREFIX_SEPARATOR = '_';
  
  public static final ErrorCategory PASS_ROOTCATEGORY = new ErrorCategory("PASS_ROOTCATEGORY", null, "PASS");
  public static final ErrorCategory FUNCTIONAL = new ErrorCategory("FUNCTIONAL", PASS_ROOTCATEGORY, "FUNC");
  public static final ErrorCategory TECHNICAL = new ErrorCategory("TECHNICAL", PASS_ROOTCATEGORY, "TECH");
  
  /**
   * the (optional) parent category
   */
  private ErrorCategory parent;
  /**
   * a short category identifier that will be used to generate prefixes for error codes 
   */
  private String prefix;
  /**
   * the complete parent-first chain of prefixes based on the category hierarchy
   */
  private String prefixChain;
  
  /**
   * 
   * @param name should be the same as the constant identifier
   * @param parent the (optional) parent category
   * @param prefix a short category identifier that will be used to generate prefixes for error codes
   */
  public ErrorCategory(String name, ErrorCategory parent, String prefix) {
    super(name);
    this.parent = parent;
    this.prefix = prefix;
    this.prefixChain = _getPrefixChain();
  }
  
  private String _getPrefixChain() {
    StringBuilder pB = new StringBuilder(prefix);
    ErrorCategory parent = this.parent;
    while(parent !=null) {
      pB.insert(0, parent.prefix+PREFIX_SEPARATOR);
      parent = parent.getParent();
    }
    return pB.toString();
  }
  
  /**
   * 
   * @return the (optional) parent category; null if no parent
   */
  public ErrorCategory getParent() {
    return parent;
  }
  
  /**
   * 
   * @return the short prefix of this category
   */
  public String getPrefix() {
    return prefix;
  }
  /**
   * 
   * @return the complete parent-first chain of prefixes based on the category hierarchy
   */
  public String getPrefixChain() {
    return prefixChain;
  }

  public static ErrorCategory valueOf(String name) {
    return(Enumerated.valueOf(ErrorCategory.class,name));
  }
  
  public static ErrorCategory valueOf(int ordinal) {
    return(Enumerated.valueOf(ErrorCategory.class,ordinal));
  }
  
  public static SortedSet<ErrorCategory> values() {
    return(Enumerated.values(ErrorCategory.class));
  }
}
