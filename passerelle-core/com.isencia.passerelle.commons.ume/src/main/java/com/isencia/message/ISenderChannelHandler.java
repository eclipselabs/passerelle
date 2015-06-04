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
package com.isencia.message;

/**
 * An ISenderChannelHandler checks on all providers of a ISenderChannel, whether
 * they have any messages available, when its messageAvailable() method is
 * called.
 * 
 * @author erwin
 */
public interface ISenderChannelHandler {

  /**
   * Indicates to the handler that a new message may be available. The handler
   * will then take over responsibility to find the message and process/send it.
   */
  public void messageAvailable();

  /**
   * Indicates that the handler should prepare itself to start accepting
   * messageAvailable() notifications.
   */
  public void open();

  /**
   * @return boolean whether the handler is active or not
   */
  public boolean isOpen();

  /**
   * Indicates that the handler can do a clean shutdown, i.e. finish processing
   * all pending messages and then stop.
   */
  public void close();
}
