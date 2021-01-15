package com.gengoai;

import com.gengoai.collection.Sets;
import com.gengoai.json.Json;
import com.gengoai.json.JsonEntry;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * @author David B. Bracewell
 */
public class ParamMapTest {

   static final ParameterDef<String> stringParam = ParameterDef.strParam("str");
   static final ParameterDef<Integer> intParam = ParameterDef.intParam("int");
   static final ParameterDef<Float> floatParam = ParameterDef.floatParam("float");
   static final ParameterDef<Double> doubleParam = ParameterDef.doubleParam("double");
   static final ParameterDef<Boolean> booleanParam = ParameterDef.boolParam("bool");

   public static class TestParameters extends ParamMap<TestParameters> {
      private static final long serialVersionUID = 1L;
      public final Parameter<String> strP = parameter(stringParam, "");
      public final Parameter<Integer> intP = parameter(intParam, -1);
      public final Parameter<Float> floatP = parameter(floatParam, -1f);
      public final Parameter<Double> doubleP = parameter(doubleParam, -1d);
      public final Parameter<Boolean> boolP = parameter(booleanParam, true);

      @Override
      public boolean equals(Object o) {
         return toString().equals(o.toString());
      }
   }

   @Test
   public void testEquality() {
      assertEquals(stringParam, ParameterDef.strParam("str"));
      assertEquals(new TestParameters().boolP, new TestParameters().boolP);
   }

   @Test
   public void json() throws Exception {
      TestParameters parameters = new TestParameters();
      parameters.set(stringParam, "notEmpty");
      assertEquals(parameters, Json.parse(Json.dumps(parameters), TestParameters.class));
   }

   @Test
   public void testConsumerUpdate() {
      TestParameters parameters = new TestParameters();
      parameters.update(p -> {
         p.set(stringParam, "testSet");
         p.set(intParam.name, 100);
      });
      assertEquals("testSet", parameters.strP.value());
      assertEquals(100, parameters.intP.value(), 0);
      assertEquals(-1d, parameters.doubleP.value(), 0d);
      assertEquals(-1f, parameters.floatP.value(), 0f);
      assertTrue(parameters.boolP.value());

      assertEquals("testSet", parameters.get("str"));
      assertEquals("testSet", parameters.get(stringParam));
      assertEquals(100, (int) parameters.<Integer>get("int"));
      assertEquals(100, parameters.get(intParam), 0);
      assertEquals(-1d, parameters.get("double"), 0d);
      assertEquals(-1d, parameters.get(doubleParam), 0d);
      assertEquals(-1f, parameters.get("float"), 0f);
      assertEquals(-1f, parameters.get(floatParam), 0f);
      assertEquals(-1f, parameters.getOrDefault(floatParam, 100f), 0f);
      assertEquals(-1f, parameters.getOrDefault(floatParam.name, 100f), 0f);
      assertEquals(42f, parameters.getOrDefault("NotThere", 42f), 0f);
      assertEquals(42L, parameters.getOrDefault(ParameterDef.longParam("LongParam"), 42L), 0f);
      assertTrue(parameters.get(booleanParam.name));
      assertTrue(parameters.get(booleanParam));
   }


   @Test(expected = IllegalArgumentException.class)
   public void testSetBadType() {
      TestParameters parameters = new TestParameters();
      parameters.set("str", "123");
      ParameterDef<JsonEntry> ii = ParameterDef.param("str", JsonEntry.class);
      parameters.get(ii);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testGetOrDefaultBadType() {
      TestParameters parameters = new TestParameters();
      parameters.getOrDefault(stringParam.name, 100L);
   }

   @Test
   public void parameterNames() {
      assertEquals(Sets.hashSetOf(stringParam.name,
                                  floatParam.name,
                                  doubleParam.name,
                                  intParam.name,
                                  booleanParam.name),
                   new TestParameters().parameterNames());
   }

   @Test(expected = IllegalArgumentException.class)
   public void testUnknownParameterGetStr() {
      TestParameters parameters = new TestParameters();
      parameters.get("Not there");
   }

   @Test(expected = IllegalArgumentException.class)
   public void testUnknownParameterGet() {
      TestParameters parameters = new TestParameters();
      parameters.get(ParameterDef.boolParam("NotThere"));
   }


   @Test(expected = IllegalArgumentException.class)
   public void testUnknownParameterSet() {
      TestParameters parameters = new TestParameters();
      parameters.set("Not there", true);
   }
}
