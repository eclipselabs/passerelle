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
package com.isencia.passerelle.core;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;

/**
 * A Passerelle-specific Ptolemy Token class
 * 
 * @author erwin
 */
public class PasserelleToken extends Token {
  private static final long serialVersionUID = -5030959777598127572L;

  /**
   * Used to let channels die between connected actors
   */
  public final static PasserelleToken POISON_PILL = new PasserelleToken();

  private ManagedMessage message;

  /**
   * @param msg
   */
  public PasserelleToken(ManagedMessage msg) {
    this.message = msg;
  }

  private PasserelleToken() {
  }

  /**
   * @return Returns the message.
   */
  public ManagedMessage getMessage() {
    return message;
  }

  public Type getType() {
    return PasserelleType.PASSERELLE_MSG_TYPE;
  }

  public Class getMessageContentType() {
    try {
      return getMessage().getBodyContent().getClass();
    } catch (MessageException e) {
      return null;
    }
  }

  public String getMessageContentMimeType() {
    try {
      return getMessage().getBodyContentType();
    } catch (MessageException e) {
      return null;
    }
  }

  /**
   * Returns <code>true</code> if this <code>PasserelleToken</code> is the same as the o argument.
   * 
   * @return <code>true</code> if this <code>PasserelleToken</code> is the same as the o argument.
   */
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (o.getClass() != getClass()) {
      return false;
    }
    PasserelleToken castedObj = (PasserelleToken) o;
    return ((this.message == null ? castedObj.message == null : this.message.equals(castedObj.message)));
  }

  /**
   * Override hashCode.
   * 
   * @return the Objects hashcode.
   */
  public int hashCode() {
    int hashCode = super.hashCode();
    hashCode = 31 * hashCode + (message == null ? 0 : message.hashCode());
    return hashCode;
  }

  /**
   * Return the value of this token as a string that can be parsed by the expression language to recover a token with the same value. The returned syntax looks
   * like a function call to a one argument method named "object". The argument is the string representation of the contained object, or the string "null" if
   * the object is null. Notice that this syntax is not currently parseable by the expression language.
   * 
   * @return A String representing the object.
   */
  public String toString() {
    if (getMessage() != null) {
      return "message(" + getMessage().toString() + ")";
    } else {
      return "message(null)";
    }
  }

  public BooleanToken isEqualTo(Token rightArgument) throws IllegalActionException {
    if (rightArgument == null) {
      return BooleanToken.getInstance(false);
    } else if (rightArgument instanceof PasserelleToken) {
      if (((PasserelleToken) rightArgument).getMessage() == null) {
        // need to check this, as ObjectToken.equals()
        // could generate NPE for null values
        return BooleanToken.getInstance(getMessage() != null);
      } else
        return BooleanToken.getInstance(this.equals(rightArgument));
    } else
      throw new IllegalActionException("Equality test not supported between " + this.getClass().getName() + " and " + rightArgument.getClass().getName() + ".");
  }

}
