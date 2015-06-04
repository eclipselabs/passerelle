package com.isencia.passerelle.process.model.impl.activator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import com.isencia.passerelle.process.model.ResultItemFromRawBuilder;
import com.isencia.passerelle.process.model.ResultItemFromRawBuilderRegistry;
import com.isencia.passerelle.process.model.impl.ResultItemFromRawBuilderRegistryImpl;

public class ActivatorTest {
  
  private Activator activator;
  private BundleContext bundleContext;

  @Before
  public void setup() {
    activator = new Activator();
    bundleContext = mock(BundleContext.class);
  }
  
  @Test
  public void testRegisterRegistryObject() throws Exception {
    activator.start(bundleContext);
    
    verify(bundleContext).registerService(ResultItemFromRawBuilderRegistry.class, ResultItemFromRawBuilderRegistryImpl.getInstance(), null);
  }
  
  @Test
  public void testRegisterRawResultNOPImpl() throws Exception {
    activator.start(bundleContext);
    
    assertNotNull(ResultItemFromRawBuilderRegistryImpl.getInstance().getBuilderByName(ResultItemFromRawBuilder.NOP_BUILDER_NAME));
  }

}
