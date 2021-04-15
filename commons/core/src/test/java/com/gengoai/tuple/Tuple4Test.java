package com.gengoai.tuple;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class Tuple4Test {

  Tuple4<String,String, String, String> tuple;

  @Before
  public void setUp() throws Exception {
    tuple = Tuples.$("A", "A", "A", "A");
  }

  @Test
  public void copy() throws Exception {
    assertEquals(tuple, tuple.copy());
  }

  @Test
  public void degree() throws Exception {
    assertEquals(4, tuple.degree());
  }

  @Test
  public void array() throws Exception {
    assertArrayEquals(new Object[]{"A", "A", "A", "A"}, tuple.array());
  }

  @Test
  public void mapValues() throws Exception {
    assertEquals(Tuples.$("a", "a", "a", "a"), tuple.mapValues(o -> o.toString().toLowerCase()));
  }

  @Test
  public void shiftLeft() throws Exception {
    assertEquals(Tuples.$("A", "A", "A"), tuple.shiftLeft());
  }

  @Test
  public void shiftRight() throws Exception {
    assertEquals(Tuples.$("A", "A", "A"), tuple.shiftRight());
  }

  @Test
  public void appendRight() throws Exception {
    assertEquals(Tuples.$("A", "A", "A", "A", "A"), tuple.appendRight("A"));
  }

  @Test
  public void appendLeft() throws Exception {
    assertEquals(Tuples.$("A", "A", "A", "A", "A"), tuple.appendLeft("A"));
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
    assertEquals("A", tuple.get(2));
    assertEquals("A", tuple.get(3));
  }

  @Test
  public void values() throws Exception {
    assertEquals("A", tuple.getV1());
    assertEquals("A", tuple.getV2());
    assertEquals("A", tuple.getV3());
    assertEquals("A", tuple.getV4());
  }

  @Test
  public void list() throws Exception {
    assertTrue(Tuples.$(Arrays.asList("A", "A", "A", "A")) instanceof Tuple4);
  }


}