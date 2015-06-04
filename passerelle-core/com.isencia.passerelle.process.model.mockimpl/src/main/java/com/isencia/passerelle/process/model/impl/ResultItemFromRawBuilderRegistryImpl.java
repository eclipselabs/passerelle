package com.isencia.passerelle.process.model.impl;

import java.util.HashMap;
import java.util.Map;

import com.isencia.passerelle.process.model.ResultItemFromRawBuilder;
import com.isencia.passerelle.process.model.ResultItemFromRawBuilderRegistry;

public class ResultItemFromRawBuilderRegistryImpl implements ResultItemFromRawBuilderRegistry {

  private static ResultItemFromRawBuilderRegistry INSTANCE = null;

  private Map<String, ResultItemFromRawBuilder> builderMap = new HashMap<String, ResultItemFromRawBuilder>();

  public ResultItemFromRawBuilderRegistryImpl() {
    INSTANCE = this;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.isencia.passerelle.process.model.ResultItemFromRawBuilderRegistry#registerBuilder(com.isencia.passerelle.process
   * .model.ResultItemFromRawBuilder)
   */
  public void registerBuilder(ResultItemFromRawBuilder builder) {
    if (builderMap.containsKey(builder.getName())) {
      throw new IllegalStateException("A ResultItemFromRawBuilder is already registered with name " + builder.getName());
    }
    builderMap.put(builder.getName(), builder);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.isencia.passerelle.process.model.ResultItemFromRawBuilderRegistry#unregisterBuilder(com.isencia.passerelle.
   * process.model.ResultItemFromRawBuilder)
   */
  public void unregisterBuilder(ResultItemFromRawBuilder builder) {
    builderMap.remove(builder.getName());
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.isencia.passerelle.process.model.ResultItemFromRawBuilderRegistry#getBuilderByName(java.lang.String)
   */
  public ResultItemFromRawBuilder getBuilderByName(String builderName) {
    if (!builderMap.containsKey(builderName)) {
      throw new IllegalStateException("No ResultItemFromRawBuilder is registered with name " + builderName);
    }
    return builderMap.get(builderName);
  }

  /**
   * No synchronized access for speed. Should never be null as this object is created by the activator.
   * 
   * @return
   */
  protected static ResultItemFromRawBuilderRegistry getInstance() {
    return INSTANCE;
  }

}
