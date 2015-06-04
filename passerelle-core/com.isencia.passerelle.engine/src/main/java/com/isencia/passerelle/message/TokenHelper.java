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
package com.isencia.passerelle.message;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.complex.ComplexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.util.StringConvertor;
import ptolemy.data.BooleanToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A utility class for working with Ptolemy tokens and their contents.
 * 
 * @author erwin
 */
public class TokenHelper {
  private static Logger logger = LoggerFactory.getLogger(TokenHelper.class);

  final private static ComplexFormat iFormat = new ComplexFormat(NumberFormat.getInstance(Locale.ENGLISH));
  final private static ComplexFormat jFormat = new ComplexFormat("j", NumberFormat.getInstance(Locale.ENGLISH));

  /**
   * Tries to get a String from the token, by:
   * <ul>
   * <li>checking if the token is not an ObjectToken, containing a String
   * <li>checking if the token is not an ObjectToken, and getting the object.toString()
   * <li>checking if the token is not a StringToken, and getting its value
   * <li>getting Token.toString() as value
   * </ul>
   * 
   * @param token
   * @return
   */
  public static String getStringFromToken(Token token) throws PasserelleException {
    if (logger.isTraceEnabled()) {
      logger.trace(token.toString()); // TODO Check if correct converted
    }
    String res = null;

    if (token != null) {
      try {
        if (token instanceof ObjectToken) {
          Object obj = ((ObjectToken) token).getValue();
          if (obj instanceof String) {
            res = (String) obj;
          } else if (obj != null) {
            res = obj.toString();
          }
        } else {
          if (token instanceof StringToken) {
            res = ((StringToken) token).stringValue();
          } else {
            res = token.toString();
          }
        }
      } catch (Exception e) {
        throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Error building String from token " + token, e);
      }
    }

    if (logger.isTraceEnabled()) {
      logger.trace("exit :" + res);
    }
    return res;
  }

  /**
   * Tries to get a Integer from the token, by:
   * <ul>
   * <li>checking if the token is not an ObjectToken, containing a Integer
   * <li>checking if the token is not an ObjectToken, and converting the object.toString() into a Integer
   * <li>checking if the token is not a StringToken, and converting the string into a Integer
   * <li>checking if the token is not a ScalarToken, and reading its integer value
   * <li>checking if the token is not a ScalarToken, and reading its double value and rounding to an integer value
   * </ul>
   * 
   * @param token
   * @return
   */
  public static Integer getIntegerFromToken(Token token) throws PasserelleException {
    if (logger.isTraceEnabled()) {
      logger.trace(token.toString()); // TODO Check if correct converted
    }
    Integer res = null;

    if (token != null) {
      try {
        if (token instanceof ObjectToken) {
          Object obj = ((ObjectToken) token).getValue();
          if (obj instanceof Integer) {
            res = (Integer) obj;
          } else if (obj != null) {
            try {
              res = Integer.valueOf(obj.toString());
            } catch (NumberFormatException e) {
              throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid Integer format in " + token, e);
            }
          }
        } else {
          if (token instanceof StringToken) {
            String tokenMessage = ((StringToken) token).stringValue();
            if (tokenMessage != null) {
              try {
                res = Integer.valueOf(tokenMessage);
              } catch (NumberFormatException e) {
                throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid Integer format in " + token, e);
              }
            }
          } else if (token instanceof ScalarToken) {
            try {
              res = new Integer(((ScalarToken) token).intValue());
            } catch (IllegalActionException e) {
              // try rounding it from a double
              try {
                res = new Integer((int) Math.round(((ScalarToken) token).doubleValue()));
              } catch (IllegalActionException e1) {
                // not even doubleValue is supported...
                throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid token for obtaining Integer value in " + token, e1);
              }
            }
          } else {
            throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid token for obtaining Integer value in " + token, null);
          }
        }
      } catch (Exception e) {
        throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Error building Integer from token in " + token, e);
      }
    }

    if (logger.isTraceEnabled()) {
      logger.trace("exit :" + res);
    }
    return res;
  }

  /**
   * Tries to get a Integer Short the token, by:
   * <ul>
   * <li>checking if the token is not an ObjectToken, containing a Short
   * <li>checking if the token is not an ObjectToken, and converting the object.toString() into a Short
   * <li>checking if the token is not a StringToken, and converting the string into a Short
   * <li>checking if the token is not a ScalarToken, and reading its integer value and converting it to a short value
   * <li>checking if the token is not a ScalarToken, and reading its double value and rounding to an short value
   * </ul>
   * Remark that converting a integer to a short may lead to loss of precision and/or of most-significant bytes. The
   * result can be that a big integer value gets converted into some negative short value.
   * 
   * @param token
   * @return
   */
  public static Short getShortFromToken(Token token) throws PasserelleException {
    if (logger.isTraceEnabled()) {
      logger.trace(token.toString()); // TODO Check if correct converted
    }
    Short res = null;

    if (token != null) {
      try {
        if (token instanceof ObjectToken) {
          Object obj = ((ObjectToken) token).getValue();
          if (obj instanceof Short) {
            res = (Short) obj;
          } else if (obj != null) {
            try {
              res = Short.valueOf(obj.toString());
            } catch (NumberFormatException e) {
              throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid Short format in " + token, e);
            }
          }
        } else {
          if (token instanceof StringToken) {
            String tokenMessage = ((StringToken) token).stringValue();
            if (tokenMessage != null) {
              try {
                res = Short.valueOf(tokenMessage);
              } catch (NumberFormatException e) {
                throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid Short format in " + token, e);
              }
            }
          } else if (token instanceof ScalarToken) {
            try {
              res = new Short((short) ((ScalarToken) token).intValue());
            } catch (IllegalActionException e) {
              // try rounding it from a double
              try {
                res = new Short((short) Math.round(((ScalarToken) token).doubleValue()));
              } catch (IllegalActionException e1) {
                // not even doubleValue is supported...
                throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid token for obtaining Short value in " + token, e1);
              }
            }
          } else {
            throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid token for obtaining Short value in " + token, null);
          }
        }
      } catch (Exception e) {
        throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Error building Short from token in " + token, e);
      }
    }

    if (logger.isTraceEnabled()) {
      logger.trace("exit :" + res);
    }
    return res;
  }

  /**
   * Tries to get a Complex from the token, by:
   * <ul>
   * <li>checking if the token is not an ObjectToken, containing a Double
   * <li>checking if the token is not an ObjectToken, and converting the object.toString() into a Double
   * <li>checking if the token is not a StringToken, and converting the string into a Double
   * <li>checking if the token is not a ScalarToken, and reading its double value
   * </ul>
   * 
   * @param token
   * @return
   */
  public static Complex getComplexFromToken(Token token) throws PasserelleException {
    if (logger.isTraceEnabled()) {
      logger.trace(token.toString()); // TODO Check if correct converted
    }
    Complex res = null;

    if (token != null) {
      try {
        if (token instanceof ObjectToken) {
          Object obj = ((ObjectToken) token).getValue();
          if (obj instanceof Complex) {
            res = (Complex) obj;
          }
          if (obj instanceof ptolemy.math.Complex) {
            ptolemy.math.Complex tmp = (ptolemy.math.Complex) obj;
            res = new Complex(tmp.real, tmp.imag);
          } else if (obj != null) {
            String content = obj.toString();
            try {
              res = iFormat.parse(content);
            } catch (ParseException e) {
              // Try the j-format
              try {
                res = jFormat.parse(content);
              } catch (ParseException e1) {
                throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid Complex format in " + token, e1);
              }
            }
          }
        } else {
          if (token instanceof StringToken) {
            String content = ((StringToken) token).stringValue();
            if (content != null) {
              try {
                res = iFormat.parse(content);
              } catch (ParseException e) {
                // Try the j-format
                res = jFormat.parse(content);
              }
            }
          } else if (token instanceof ComplexToken) {
            ptolemy.math.Complex tmp = ((ComplexToken) token).complexValue();
            res = new Complex(tmp.real, tmp.imag);
          } else if (token instanceof ScalarToken) {
            try {
              res = new Complex(((ScalarToken) token).doubleValue(), 0);
            } catch (IllegalActionException e) {
              throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid token for obtaining Complex value in " + token, e);
            }
          } else {
            throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid token for obtaining Complex value in " + token, null);
          }
        }
      } catch (Exception e) {
        throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Error building Complex from token in " + token, e);
      }
    }

    if (logger.isTraceEnabled()) {
      logger.trace("exit :" + res);
    }
    return res;
  }

  /**
   * Tries to get a Double from the token, by:
   * <ul>
   * <li>checking if the token is not an ObjectToken, containing a Double
   * <li>checking if the token is not an ObjectToken, and converting the object.toString() into a Double
   * <li>checking if the token is not a StringToken, and converting the string into a Double
   * <li>checking if the token is not a ScalarToken, and reading its double value
   * </ul>
   * 
   * @param token
   * @return
   */
  public static Double getDoubleFromToken(Token token) throws PasserelleException {
    if (logger.isTraceEnabled()) {
      logger.trace(token.toString()); // TODO Check if correct converted
    }
    Double res = null;

    if (token != null) {
      try {
        if (token instanceof ObjectToken) {
          Object obj = ((ObjectToken) token).getValue();
          if (obj instanceof Double) {
            res = (Double) obj;
          } else if (obj != null) {
            try {
              res = Double.valueOf(obj.toString());
            } catch (NumberFormatException e) {
              throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid Double format in " + token, e);
            }
          }
        } else {
          if (token instanceof StringToken) {
            String tokenMessage = ((StringToken) token).stringValue();
            if (tokenMessage != null) {
              try {
                res = Double.valueOf(tokenMessage);
              } catch (NumberFormatException e) {
                throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid Double format in " + token, e);
              }
            }
          } else if (token instanceof ScalarToken) {
            try {
              res = new Double(((ScalarToken) token).doubleValue());
            } catch (IllegalActionException e) {
              throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid token for obtaining Double value in " + token, e);
            }
          } else {
            throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid token for obtaining Double value in " + token, null);
          }
        }
      } catch (Exception e) {
        throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Error building Double from token in " + token, e);
      }
    }

    if (logger.isTraceEnabled()) {
      logger.trace("exit :" + res);
    }
    return res;
  }

  /**
   * Tries to get a Float from the token, by:
   * <ul>
   * <li>checking if the token is not an ObjectToken, containing a Float
   * <li>checking if the token is not an ObjectToken, and converting the object.toString() into a Float
   * <li>checking if the token is not a StringToken, and converting the string into a Float
   * <li>checking if the token is not a ScalarToken, and reading its double value and converting it to a float
   * </ul>
   * Remark that converting a double to a float may lead to loss of precision. Furthermore, if the double value in the
   * token is bigger than Float.MAX_VALUE, the converted float will just be "infinity".
   * 
   * @param token
   * @return
   */
  public static Float getFloatFromToken(Token token) throws PasserelleException {
    if (logger.isTraceEnabled()) {
      logger.trace(token.toString()); // TODO Check if correct converted
    }
    Float res = null;

    if (token != null) {
      try {
        if (token instanceof ObjectToken) {
          Object obj = ((ObjectToken) token).getValue();
          if (obj instanceof Float) {
            res = (Float) obj;
          } else if (obj != null) {
            try {
              res = Float.valueOf(obj.toString());
            } catch (NumberFormatException e) {
              throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid Float format in " + token, e);
            }
          }
        } else {
          if (token instanceof StringToken) {
            String tokenMessage = ((StringToken) token).stringValue();
            if (tokenMessage != null) {
              try {
                res = Float.valueOf(tokenMessage);
              } catch (NumberFormatException e) {
                throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid Float format in " + token, e);
              }
            }
          } else if (token instanceof ScalarToken) {
            try {
              res = new Float(((ScalarToken) token).doubleValue());
            } catch (IllegalActionException e) {
              throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid token for obtaining Float value in " + token, e);
            }
          } else {
            throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid token for obtaining Float value in " + token, null);
          }
        }
      } catch (Exception e) {
        throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Error building Float from token in " + token, e);
      }
    }

    if (logger.isTraceEnabled()) {
      logger.trace("exit :" + res);
    }
    return res;
  }

  /**
   * Tries to get a Boolean from the token, by:
   * <ul>
   * <li>checking if the token is not an ObjectToken, containing a Boolean
   * <li>checking if the token is not an ObjectToken, and converting the object.toString() into a Boolean
   * <li>checking if the token is not a StringToken, and converting the string into a Boolean
   * <li>checking if the token is not a ScalarToken, and reading its boolean value
   * </ul>
   * The string-to-boolean conversion will look for following (case-insensitive) patterns (based on using
   * com.isencia.util.StringConvertor.stringToBoolean()):
   * <ul>
   * <li>yes/no
   * <li>on/off
   * <li>1/0
   * <li>true/false
   * </ul>
   * 
   * @param token
   * @return
   */
  public static Boolean getBooleanFromToken(Token token) throws PasserelleException {
    if (logger.isTraceEnabled()) {
      logger.trace(token.toString()); // TODO Check if correct converted
    }
    Boolean res = null;

    if (token != null) {
      try {
        if (token instanceof ObjectToken) {
          Object obj = ((ObjectToken) token).getValue();
          if (obj instanceof Boolean) {
            res = (Boolean) obj;
          } else if (obj != null) {
            res = StringConvertor.stringToBoolean(obj.toString());
          }
        } else {
          if (token instanceof StringToken) {
            String tokenMessage = ((StringToken) token).stringValue();
            if (tokenMessage != null) {
              res = StringConvertor.stringToBoolean(tokenMessage);
            }
          } else if (token instanceof BooleanToken) {
            res = new Boolean(((BooleanToken) token).booleanValue());
          } else {
            throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid token for obtaining Boolean value in " + token, null);
          }
        }
      } catch (Exception e) {
        throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Error building Boolean from token in " + token, e);
      }
    }

    if (logger.isTraceEnabled()) {
      logger.trace("exit :" + res);
    }
    return res;
  }

  /**
   * Tries to get a Byte from the token, by:
   * <ul>
   * <li>checking if the token is not an ObjectToken, containing a Byte
   * <li>checking if the token is not an ObjectToken, and converting the object.toString() into a Byte
   * <li>checking if the token is not a StringToken, and converting the string into a Byte
   * <li>checking if the token is not a ScalarToken, and reading its byte value
   * </ul>
   * 
   * @param token
   * @return
   */
  public static Byte getByteFromToken(Token token) throws PasserelleException {
    if (logger.isTraceEnabled()) {
      logger.trace(token.toString()); // TODO Check if correct converted
    }
    Byte res = null;

    if (token != null) {
      try {
        if (token instanceof ObjectToken) {
          Object obj = ((ObjectToken) token).getValue();
          if (obj instanceof Byte) {
            res = (Byte) obj;
          } else if (obj != null) {
            try {
              res = Byte.valueOf(obj.toString());
            } catch (NumberFormatException e) {
              throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid Byte format in " + token, e);
            }
          }
        } else {
          if (token instanceof StringToken) {
            String tokenMessage = ((StringToken) token).stringValue();
            if (tokenMessage != null) {
              try {
                res = Byte.valueOf(tokenMessage);
              } catch (NumberFormatException e) {
                throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid Byte format in " + token, e);
              }
            }
          } else if (token instanceof ScalarToken) {
            res = new Byte(((ScalarToken) token).byteValue());
          } else {
            throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid token for obtaining Byte value in " + token, null);
          }
        }
      } catch (Exception e) {
        throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Error building Byte from token in " + token, e);
      }
    }

    if (logger.isTraceEnabled()) {
      logger.trace("exit :" + res);
    }
    return res;
  }

}
