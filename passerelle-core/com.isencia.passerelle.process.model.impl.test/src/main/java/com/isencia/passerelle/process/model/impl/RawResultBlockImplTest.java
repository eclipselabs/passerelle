package com.isencia.passerelle.process.model.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.RawResultBlock;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.ResultItemFromRawBuilder;

public class RawResultBlockImplTest {

  private static ResultItemFromRawBuilderRegistryImpl resultItemFromRawBuilderRegistryImpl;

  @BeforeClass
  public static void setupBuilderRegistry() {
    resultItemFromRawBuilderRegistryImpl = new ResultItemFromRawBuilderRegistryImpl();
  }

  @Test
  public void testEmtpyConstructor() {
    // should not fail
    new RawResultBlockImpl();
  }

  @Test(expected = IllegalStateException.class)
  public void testPutNewItemNoBuilder() throws Exception {
    ResultItem<?> rawResultitem = mock(ResultItem.class);
    ResultItemFromRawBuilder builder = mock(ResultItemFromRawBuilder.class);
    when(builder.getName()).thenReturn("testBuilder");
    new RawResultBlockImpl().putRawResultItem(rawResultitem, builder);
  }

  @Test
  public void testPutRawAddsBuilderNameAtt() throws Exception {
    ResultItem<?> rawResultitem = mock(ResultItem.class);
    ResultItemFromRawBuilder builder = mock(ResultItemFromRawBuilder.class);
    when(builder.getName()).thenReturn("builder1");
    resultItemFromRawBuilderRegistryImpl.registerBuilder(builder);

    // verify attribute name and value
    when(rawResultitem.putAttribute(any(Attribute.class))).then(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        Attribute att = (Attribute) invocation.getArguments()[0];
        assertEquals(RawResultBlockImpl.RESULT_ITEM_FROM_RAW_BUILDER_ATT_NAME, att.getName());
        assertEquals("builder1", att.getValue());
        return null;
      }
    });
    new RawResultBlockImpl().putRawResultItem(rawResultitem, builder);
    verify(rawResultitem).putAttribute(any(Attribute.class));
  }
  
  @Test
  public void testItemsGetMerged() throws Exception {
    //mock objects
    ResultItem<?> rawResultitem = new StringResultItemImpl();
    ResultItemFromRawBuilder builder = mock(ResultItemFromRawBuilder.class);
    ResultItem<?> generatedItem1 = mock(ResultItem.class);
    ResultItem<?> generatedItem2 = mock(ResultItem.class);
    Map<String, ResultItem<?>> generatedItems = new HashMap<String, ResultItem<?>>();
    generatedItems.put("key1", generatedItem1);
    generatedItems.put("key2", generatedItem2);

    //builder behaviour
    when(builder.getName()).thenReturn("builder2");
    when(builder.transformRawToResultItems(any(ResultItem.class))).thenReturn(generatedItems);
    
    //register builder and use
    resultItemFromRawBuilderRegistryImpl.registerBuilder(builder);
    RawResultBlock rawResultBlock = new RawResultBlockImpl();
    rawResultBlock.putRawResultItem(rawResultitem, builder);
    Collection<ResultItem<?>> mergedItems = rawResultBlock.getAllItems();
    
    //verify
    assertTrue(mergedItems.containsAll(generatedItems.values()));
    assertTrue(mergedItems.contains(rawResultitem));
    assertEquals(3, mergedItems.size());
  }
}
