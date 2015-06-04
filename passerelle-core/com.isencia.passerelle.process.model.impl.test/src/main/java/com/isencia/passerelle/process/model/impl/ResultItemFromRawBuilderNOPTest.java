package com.isencia.passerelle.process.model.impl;

import static org.junit.Assert.*;

import org.junit.Test;

import com.isencia.passerelle.process.model.ResultItemFromRawBuilder;

public class ResultItemFromRawBuilderNOPTest {

  @Test
  public void testNoOperation() {
    ResultItemFromRawBuilderNOP builder = new ResultItemFromRawBuilderNOP();
    assertEquals(ResultItemFromRawBuilder.NOP_BUILDER_NAME, builder.getName());
    assertTrue(builder.transformRawToResultItems(null).isEmpty());
  }

}
