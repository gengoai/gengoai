package com.gengoai.tuple;

import com.gengoai.json.Json;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class NTupleTest {

   NTuple tuple;

   @Test
   public void appendLeft() throws Exception {
      assertEquals(Tuples.$("A", "A", "A", "A", "A"), tuple.appendLeft("A"));
   }

   @Test
   public void appendRight() throws Exception {
      assertEquals(Tuples.$("A", "A", "A", "A", "A"), tuple.appendRight("A"));
   }

   @Test
   public void array() throws Exception {
      assertArrayEquals(new Object[]{"A", "A", "A", "A"}, tuple.array());
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
   public void get() throws Exception {
      assertEquals("A", tuple.get(0));
   }

   @Test(expected = ArrayIndexOutOfBoundsException.class)
   public void getErr() throws Exception {
      tuple.get(100);
   }

   @Test
   public void json() throws Exception {
      String json = Json.dumps(tuple);
      assertEquals(tuple.stream().map(a -> "\"" + a + "\"").collect(Collectors.joining(",", "[", "]")), json);
      Tuple t = Json.parse(json, Tuple.class);
      assertEquals(tuple, t);
   }

   @Test
   public void list() throws Exception {
      assertTrue(Tuples.$(Arrays.asList("A", "A", "A", "A", "A", "A")) instanceof NTuple);
   }

   @Test
   public void mapValues() throws Exception {
      assertEquals(Tuples.$("a", "a", "a", "a"), tuple.mapValues(o -> o.toString().toLowerCase()));
   }

   @Before
   public void setUp() throws Exception {
      tuple = NTuple.of("A", "A", "A", "A");
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
   public void slice() throws Exception {
      assertEquals(tuple, tuple.slice(0, 100));
   }

}