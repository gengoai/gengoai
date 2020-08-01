package com.gengoai.math;

import com.gengoai.function.SerializableBiFunction;
import lombok.NonNull;

/**
 * Methods for comparing numeric (double) values.
 *
 * @author David B. Bracewell
 */
public enum NumericComparison implements SerializableBiFunction<Number, Number, Boolean> {
   /**
    * Is <code>beingCompared</code> greater than <code>comparedAgainst</code>
    */
   GT {
      @Override
      public boolean compare(double beingCompared, double comparedAgainst) {
         return beingCompared > comparedAgainst;
      }
   },
   /**
    * Is <code>beingCompared</code> greater than or equal to <code>comparedAgainst</code>
    */
   GTE {
      @Override
      public boolean compare(double beingCompared, double comparedAgainst) {
         return beingCompared >= comparedAgainst;
      }
   },
   /**
    * Is <code>beingCompared</code> less than <code>comparedAgainst</code>
    */
   LT {
      @Override
      public boolean compare(double beingCompared, double comparedAgainst) {
         return beingCompared < comparedAgainst;
      }
   },
   /**
    * Is <code>beingCompared</code> less than or equal to <code>comparedAgainst</code>
    */
   LTE {
      @Override
      public boolean compare(double beingCompared, double comparedAgainst) {
         return beingCompared <= comparedAgainst;
      }
   },
   /**
    * Is <code>beingCompared</code> equal to <code>comparedAgainst</code>
    */
   EQ {
      @Override
      public boolean compare(double beingCompared, double comparedAgainst) {
         return Double.compare(beingCompared, comparedAgainst) == 0;
      }
   },
   /**
    * Is <code>beingCompared</code> not equal to <code>comparedAgainst</code>
    */
   NE {
      @Override
      public boolean compare(double beingCompared, double comparedAgainst) {
         return beingCompared != comparedAgainst;
      }
   };

   public static NumericComparison fromString(@NonNull String string) {
      switch (string) {
         case "=":
            return EQ;
         case ">":
            return GT;
         case ">=":
            return GTE;
         case "<":
            return LT;
         case "<=":
            return LTE;
         case "!=":
            return NE;
      }
      throw new IllegalArgumentException();
   }

   @Override
   public Boolean apply(Number number, Number number2) {
      return compare(number.doubleValue(), number2.doubleValue());
   }

   public String asString() {
      switch (this) {
         case EQ:
            return "=";
         case GT:
            return ">";
         case GTE:
            return ">=";
         case LT:
            return "<";
         case LTE:
            return "<=";
         case NE:
            return "!=";
      }
      throw new IllegalArgumentException();
   }

   /**
    * Compares two given numeric values
    *
    * @param beingCompared   The number being compared
    * @param comparedAgainst The number being compared against
    * @return true if the inequality holds
    */
   public abstract boolean compare(double beingCompared, double comparedAgainst);

}// END OF Inequality
