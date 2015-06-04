package com.isencia.passerelle.process.model;

/**
 * A registry provides access to the {@link RawResultBlock} implementation to find {@link ResultItemFromRawBuilder}
 * instances so they can create additional result items from the raw result items.
 * 
 * An instance of this class should be made available as OSGI service.
 * 
 * @author verjer
 * 
 */
public interface ResultItemFromRawBuilderRegistry {

  /**
   * Register a builder.
   * 
   * @param builder
   *          builder to register
   * @throws an
   *           {@link IllegalStateException} when a builder with the same name exists already.
   */
  void registerBuilder(ResultItemFromRawBuilder builder);

  /**
   * Unregister a builder
   * 
   * @param builder
   *          builder to unregister
   */
  void unregisterBuilder(ResultItemFromRawBuilder builder);

  /**
   * Get the builder from the registry by name.
   * 
   * @param builderName
   *          the unique name of the builder
   * @return the result builder or throws an {@link IllegalStateException} when no result builder is found.
   */
  ResultItemFromRawBuilder getBuilderByName(String builderName);
}
