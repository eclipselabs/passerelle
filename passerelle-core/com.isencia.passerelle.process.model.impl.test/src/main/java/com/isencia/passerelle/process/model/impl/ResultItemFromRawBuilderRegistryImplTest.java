package com.isencia.passerelle.process.model.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import com.isencia.passerelle.process.model.ResultItemFromRawBuilder;

public class ResultItemFromRawBuilderRegistryImplTest {
  
  @Test
  public void testNormalRegisterAndUnregister() throws Exception {
    ResultItemFromRawBuilderRegistryImpl resultItemFromRawBuilderRegistryImpl = new ResultItemFromRawBuilderRegistryImpl();
    ResultItemFromRawBuilder builder = mock(ResultItemFromRawBuilder.class);
    when(builder.getName()).thenReturn("testBuilder1");
    resultItemFromRawBuilderRegistryImpl.registerBuilder(builder);
    //should not fail
    resultItemFromRawBuilderRegistryImpl.getBuilderByName("testBuilder1");
    resultItemFromRawBuilderRegistryImpl.unregisterBuilder(builder);
  }

  @Test (expected = IllegalStateException.class)
  public void testThrowsIfBuilderNotExist() {
    new ResultItemFromRawBuilderRegistryImpl().getBuilderByName("testBuilder2");
  }

  @Test (expected = IllegalStateException.class)
  public void testNoDuplicatesAllowed() throws Exception {
    ResultItemFromRawBuilderRegistryImpl resultItemFromRawBuilderRegistryImpl = new ResultItemFromRawBuilderRegistryImpl();
    ResultItemFromRawBuilder builder = mock(ResultItemFromRawBuilder.class);
    when(builder.getName()).thenReturn("testBuilder3");
    resultItemFromRawBuilderRegistryImpl.registerBuilder(builder);
    resultItemFromRawBuilderRegistryImpl.registerBuilder(builder);
  }
}
