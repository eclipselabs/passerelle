/* Copyright 2012 - iSencia Belgium NV

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

package com.isencia.passerelle.process.model;

import java.io.Serializable;
import java.util.Date;

/**
 * A <code>ResultItem</code> represents a significant data item that is worth
 * maintaining/storing/analysing/showing, e.g. an individual measurement result
 * or data element obtained from a user-filled form etc.
 * <p>
 * It can be enriched in later processing with <code>Attribute</code>s, can be
 * <code>Coloured</code> etc.
 * </p>
 * 
 * @author erwin
 * 
 */
public interface ResultItem<V extends Serializable> extends NamedValue<V>, Identifiable, AttributeHolder, Coloured {
	  /**
	  * @return a deep clone of the instance
	  */	
	  ResultItem<V> clone() throws CloneNotSupportedException;

	/**
	 * This can indicate the timestamp when the result item was created inside a
	 * Passerelle process, but can also indicate a historical timestamp, e.g.
	 * when the result item represents a measurement result obtained from an
	 * external system, containing its own timestamp.
	 * 
	 * @return the creation timestamp of the item
	 */
	Date getCreationTS();

	/**
	 * 
	 * @return an identifier of the physical unit (if any) for the contained
	 *         value. By preference, this should correspond to some standard
	 *         units system like SI etc.
	 */
	String getUnit();

	/**
	 * 
	 * @return a textual name of the contained data type, that should be
	 *         consistent with the actual type of the specified generic V.
	 */
	String getDataType();

	/**
	 * 
	 * @return the parent result block to which this item belongs.
	 */
	ResultBlock getResultBlock();

	/**
	 * 
	 * @return the type of the parent result block to which this item
	 *         belongs.When not result block found nul will be returned.
	 */
	String getType();

	Integer getLevel();

}
