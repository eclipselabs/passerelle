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
package com.isencia.message.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.message.ChannelException;
import com.isencia.message.ReceiverChannel;

/**
 * DatabaseReceiverChannel
 * 
 * @author wim geeraerts
 */
public class DatabaseReceiverChannel extends ReceiverChannel {
  private final static Logger logger = LoggerFactory.getLogger(DatabaseReceiverChannel.class);

  private Connection connection;
  private String query;
  private ResultSet resultSet;
  private ResultSetMetaData metaData;
  private String[] columnNames;

  /**
   * @param server
   */
  public DatabaseReceiverChannel(Connection connection) {
    this.connection = connection;
  }

  public void open() throws ChannelException {
    if (connection == null) {
      throw new ChannelException("No connection specified");
    } else if (query == null) {
      throw new ChannelException("No query specified");
    }

    try {
      resultSet = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY).executeQuery(getQuery());
      metaData = resultSet.getMetaData();
      columnNames = new String[metaData.getColumnCount()];
      for (int column = 0; column < metaData.getColumnCount(); column++) {
        columnNames[column] = metaData.getColumnLabel(column + 1);
      }
    } catch (Exception e) {
      throw new ChannelException(e.toString());
    }

    super.open();
  }

  public void close() throws ChannelException {
    super.close();
    try {
      resultSet.close();
      connection.close();
      logger.debug("Channel Closed");
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }

  protected Object doGetMessage() throws ChannelException {
    HashMap<String, Object> msg = null;
    try {
      if (resultSet.next()) {
        msg = new HashMap<String, Object>();
        for (int i = 0; i < columnNames.length; i++) {
          String columnName = columnNames[i];
          msg.put(columnName, resultSet.getObject(columnName));
        }
      }
      return msg;
    } catch (Exception e) {
      throw new ChannelException(e.getMessage());
    }
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }
}