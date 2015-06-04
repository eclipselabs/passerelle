package com.isencia.passerelle.process.model.impl;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringResultItemImplTest {

  @Test
  public void testCompare() {
    // impl comparable to allow sorting of result items based on string value
    // used to get natural ordering of list of result items
    StringResultItemImpl smaller = new StringResultItemImpl();
    smaller.setValue("AAAAAAAAAAAAAAA");
    StringResultItemImpl bigger = new StringResultItemImpl();
    bigger.setValue("BBBBBBBBBBBBBBBB");
    StringResultItemImpl same = new StringResultItemImpl();
    same.setValue("AAAAAAAAAAAAAAA");
    assertEquals(1, bigger.compareTo(smaller));
    assertEquals(0, same.compareTo(smaller));
    assertEquals(-1, smaller.compareTo(bigger));
  }

}
