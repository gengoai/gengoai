package com.gengoai.tuple;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class Tuple2Test {

  Tuple2<String, String> tuple;

  @Before
  public void setUp() throws Exception {
    tuple = Tuples.$("A", "A");
  }

  @Test
  public void copy() throws Exception {
    assertEquals(tuple, tuple.copy());
  }

  @Test
  public void degree() throws Exception {
    assertEquals(2, tuple.degree());
  }

  @Test
  public void array() throws Exception {
    assertArrayEquals(new Object[]{"A", "A"}, tuple.array());
  }

  @Test
  public void mapValues() throws Exception {
    assertEquals(Tuples.$("a", "a"), tuple.mapValues(o -> o.toString().toLowerCase()));
  }

  @Test
  public void shiftLeft() throws Exception {
    assertEquals(Tuples.$("A"), tuple.shiftLeft());
  }

  @Test
  public void shiftRight() throws Exception {
    assertEquals(Tuples.$("A"), tuple.shiftRight());
  }

  @Test
  public void appendRight() throws Exception {
    assertEquals(Tuples.$("A", "A", "A"), tuple.appendRight("A"));
  }

  @Test
  public void appendLeft() throws Exception {
    assertEquals(Tuples.$("A", "A", "A"), tuple.appendLeft("A"));
  }

  @Test
  public void slice() throws Exception {
    assertEquals(tuple, tuple.slice(0, 100));
  }

  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void getErr() throws Exception {
    tuple.get(100);
  }

  @Test
  public void get() throws Exception {
    assertEquals("A", tuple.get(0));
    assertEquals("A", tuple.get(1));
  }

  @Test
  public void values() throws Exception {
    assertEquals("A", tuple.getV1());
    assertEquals("A", tuple.getV2());
  }

  @Test
  public void list() throws Exception {
    assertTrue(Tuples.$(Arrays.asList("A", "A")) instanceof Tuple2);
  }

}