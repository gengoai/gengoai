package com.gengoai.tuple;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class Tuple0Test {

  @Test
  public void copy() throws Exception {
    assertEquals(Tuple0.INSTANCE, Tuples.$().copy());
  }

  @Test
  public void degree() throws Exception {
    assertEquals(0, Tuple0.INSTANCE.degree());
  }

  @Test
  public void array() throws Exception {
    assertArrayEquals(new Object[0], Tuple0.INSTANCE.array());
  }

  @Test
  public void mapValues() throws Exception {
    assertEquals(0, Tuple0.INSTANCE.mapValues(o -> o).degree());
  }

  @Test
  public void shiftLeft() throws Exception {
    assertEquals(Tuple0.INSTANCE, Tuples.$().shiftLeft());
  }

  @Test
  public void shiftRight() throws Exception {
    assertEquals(Tuple0.INSTANCE, Tuples.$().shiftRight());
  }

  @Test
  public void appendRight() throws Exception {
    assertEquals(Tuples.$("A"), Tuples.$().appendRight("A"));
  }

  @Test
  public void appendLeft() throws Exception {
    assertEquals(Tuples.$("A"), Tuples.$().appendLeft("A"));
  }

  @Test
  public void slice() throws Exception {
    assertEquals(Tuple0.INSTANCE, Tuples.$().slice(0, 10));
  }

  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void get() throws Exception {
    Tuple0.INSTANCE.get(1);
  }

  @Test
  public void list() throws Exception {
    assertTrue(Tuples.$(Collections.emptyList()) instanceof Tuple0);
  }

}