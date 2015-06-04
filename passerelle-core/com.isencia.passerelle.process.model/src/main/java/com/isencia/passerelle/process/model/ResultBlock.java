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
import java.util.Collection;
import java.util.Date;

/**
 * A simple container for <code>ResultItem</code>s, that can be given a "type" identifier.
 * 
 * @author erwin
 * 
 */
public interface ResultBlock extends Serializable, Cloneable, Identifiable, AttributeHolder, Coloured {

  /**
  * @return a deep clone of the instance
  */	
  ResultBlock clone() throws CloneNotSupportedException;

  /**
   * @return the creation timestamp of the resultblock
   */
  Date getCreationTS();

  /**
   * 
   * @return The resultblock type
   */
  String getType();

  /**
   * Add a result item
   * @param item
   * @return
   */
  ResultItem<?> putItem(ResultItem<?> item);

  /**
   * @return all the ResultItems
   */
  Collection<ResultItem<?>> getAllItems();

  /**
   * 
   * @param matcher
   * @return all items that match the matcher
   */
  Collection<ResultItem<?>> getMatchingItems(Matcher<ResultItem<?>> matcher);
  
  /**
   * @param name
   * @return the ResultItem with the given name
   */
  ResultItem<?> getItemForName(String name);
  
  
  /**
   * @return parent Task
   */
  Task getTask();

}
