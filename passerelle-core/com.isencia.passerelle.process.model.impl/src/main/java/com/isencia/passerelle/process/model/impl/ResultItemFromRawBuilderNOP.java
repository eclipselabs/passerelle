package com.isencia.passerelle.process.model.impl;

import java.util.Collections;
import java.util.Map;

import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.ResultItemFromRawBuilder;

/**
 * Result builder that does not produce extra result items.
 * 
 * @author verjer
 * 
 */
public class ResultItemFromRawBuilderNOP implements ResultItemFromRawBuilder {

  @Override
  public String getName() {
    return NOP_BUILDER_NAME;
  }

  @Override
  public Map<String, ResultItem<?>> transformRawToResultItems(ResultItem<?> rawResultitem) {
    return Collections.emptyMap();
  }

}
