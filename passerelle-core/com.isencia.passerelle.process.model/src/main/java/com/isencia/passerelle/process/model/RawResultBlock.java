package com.isencia.passerelle.process.model;

/**
 * A result block which keeps raw data in storage. This can be XML, JSON, binary. The result items are generated when
 * the result block is created. This allows to minimize the number of result items to store.
 * 
 * @author verjer
 * 
 */
public interface RawResultBlock extends ResultBlock {

  /**
   * Add a raw result item. This can be a request or response in a service world or some binary data or something else.
   * 
   * @param rawResultitem
   * @return
   */
  ResultItem<?> putRawResultItem(ResultItem<?> rawResultitem, ResultItemFromRawBuilder res);
}
