package com.gengoai;

import org.junit.Test;

import java.util.Objects;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class CopyableTest {

   @Test
   public void testCopy() {
      CopyClass cc = new CopyClass("Voltaire");
      assertThat(cc, equalTo(cc.copy()));
      assertThat(cc.getName(), equalTo(cc.copy().getName()));
      assertFalse(cc == cc.copy());
   }

   @Test
   public void testDeepCopy() {
      assertEquals("This is a test", Copyable.deepCopy("This is a test"));
   }

   public static class CopyClass implements Copyable<CopyClass> {
      public String name;

      @java.beans.ConstructorProperties({"name"})
      public CopyClass(String name) {
         this.name = name;
      }

      @Override
      public CopyClass copy() {
         return new CopyClass(name);
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {return true;}
         if (obj == null || getClass() != obj.getClass()) {return false;}
         final CopyClass other = (CopyClass) obj;
         return Objects.equals(this.name, other.name);
      }

      public String getName() {
         return this.name;
      }

      @Override
      public int hashCode() {
         return Objects.hash(name);
      }

      public String toString() {
         return "CopyableTest.CopyClass(name=" + this.getName() + ")";
      }
   }

}