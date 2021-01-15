package com.gengoai.conversion;

import com.gengoai.EnumValue;
import com.gengoai.Registry;
import org.junit.Test;

import static com.gengoai.collection.Arrays2.arrayOf;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class DynamicEnumTypeConverterTest {

   public static class TestEnum extends EnumValue<TestEnum> {
      final static Registry<TestEnum> registry = new Registry<>(TestEnum::new, TestEnum.class);

      protected TestEnum(String name) {
         super(name);
      }

      public static TestEnum make(String name) {
         return registry.make(name);
      }


      @Override
      protected Registry<TestEnum> registry() {
         return registry;
      }
   }

   static final TestEnum e1 = TestEnum.make("e1");
   static final TestEnum e2 = TestEnum.make("e2");

   @Test
   public void convert() throws TypeConversionException {
      assertEquals(e1, Converter.convert("e1", TestEnum.class));
      assertEquals(e2, Converter.convert(e2, TestEnum.class));
      assertEquals(e1, Converter.convert(TestEnum.class.getName() + ".e1", EnumValue.class));
   }

   @Test(expected = TypeConversionException.class)
   public void fail() throws TypeConversionException {
      Converter.convert(arrayOf("e1"), TestEnum.class);
   }

}