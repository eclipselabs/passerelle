package com.isencia.passerelle.process.model;

import java.util.Map;

/**
 * Build result items from a raw result.
 * 
 * @author verjer
 * 
 */
public interface ResultItemFromRawBuilder {
  String NOP_BUILDER_NAME = ResultItemFromRawBuilder.class.getName() + "NOP";

  /**
   * Make sure the name is something unique, so that they can be distinguished in the registry.
   * 
   * @return name of the builder
   * @see ResultItemFromRawBuilderRegistry
   */
  String getName();

  /**
   * Generate result items from the raw result item.
   * 
   * @param rawResultitem
   *          raw item
   * @return a map containing newly generated result items as values. The key is the name of the result item for easy
   *         access.
   */
  Map<String, ResultItem<?>> transformRawToResultItems(ResultItem<?> rawResultitem);
}
