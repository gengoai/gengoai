/*
 * (c) 2005 David B. Bracewell
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.gengoai.math;

import com.gengoai.stream.Streams;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static com.gengoai.Validation.checkArgument;


/**
 * <p>Commonly needed math routines and methods that work over arrays and iterable. </p>
 *
 * @author David B. Bracewell
 */
public final class Math2 {
   private static final int EXP_TABLE_SIZE = 2000;
   private static final int MAX_EXP = 12;
   private static final double[] EXP_TABLE = new double[EXP_TABLE_SIZE];

   static {
      for (int i = 0; i < EXP_TABLE_SIZE; i++) {
         EXP_TABLE[i] = Math.exp((2.0 * i / (double) EXP_TABLE_SIZE - 1) * MAX_EXP);
         EXP_TABLE[i] /= EXP_TABLE[i] + 1;
      }
   }

   /**
    * The constant LOG_2.
    */
   public static final double LOG_2 = Math.log(2);

   private Math2() {
      throw new IllegalAccessError();
   }

   public static double exp(double value) {
      if (value < -MAX_EXP) {
         return 0d;
      }
      if (value > MAX_EXP) {
         return 1.0;
      }
      int index = (int) ((value + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2.0));
      return EXP_TABLE[index];
   }

   /**
    * Calculates the AUC (Area Under the Curve)
    *
    * @param x the x
    * @param y the y
    * @return the AUC
    */
   public static double auc(double[] x, double[] y) {
      Arrays.sort(x);
      Arrays.sort(y);

      int n0 = x.length;
      int n1 = y.length;

      int i0 = 0, i1 = 0;
      int rank = 1;
      double sum = 0d;

      while (i0 < n0 && i1 < n1) {
         double v0 = x[i0];
         double v1 = y[i1];

         if (v0 < v1) {
            i0++;
            rank++;
         } else if (v1 < v0) {
            i1++;
            sum += rank;
            rank++;
         } else {
            int k0 = 0;
            while (i0 < n0 && x[i0] == v0) {
               k0++;
               i0++;
            }


            int k1 = 0;
            while (i1 < n1 && y[i1] == v0) {
               k1++;
               i1++;
            }


            sum += (rank + (k0 + k1 - 1) / 2.0) * k1;
            rank += k0 + k1;
         }
      }

      if (i1 < n1) {
         sum += (rank + (n1 - i1 - 1) / 2.0) * (n1 - i1);
      }


      return (sum / n1 - (n1 + 1.0) / 2.0) / n0;
   }

   /**
    * <p>Clips a value to ensure it falls between the lower or upper bound of range.</p>
    *
    * @param value the value to clip
    * @param min   the lower bound of the range
    * @param max   the upper bound of the range
    * @return the clipped value
    */
   public static double clip(double value, double min, double max) {
      checkArgument(max > min, "upper bound must be > lower bound");
      if (value < min) {
         return min;
      } else if (value > max) {
         return max;
      }
      return value;
   }

   /**
    * Calculates the base 2 log of a given number
    *
    * @param number the number to calculate the base 2 log of
    * @return the base 2 log of the given number
    */
   public static double log2(double number) {
      return Math.log(number) / LOG_2;
   }

   public static <E> E maxBy(E e1, E e2, Comparator<E> comparator) {
      int cmp = comparator.compare(e1, e2);
      if (cmp >= 0) {
         return e1;
      }
      return e2;
   }

   public static <E> E minBy(E e1, E e2, Comparator<E> comparator) {
      int cmp = comparator.compare(e1, e2);
      if (cmp <= 0) {
         return e1;
      }
      return e2;
   }


   /**
    * <p>Rescales a value from an old range to a new range, e.g. change the value 2 in a 1 to 5 scale to the value 3.25
    * in a 1 to 10 scale</p>
    *
    * @param value       the value to rescale
    * @param originalMin the lower bound of the original range
    * @param originalMax the upper bound of the original range
    * @param newMin      the lower bound of the new range
    * @param newMax      the upper bound of the new range
    * @return the given value rescaled to fall between newMin and new Max
    * @throws IllegalArgumentException if originalMax {@code <=} originalMin or newMax {@code <=}  newMin
    */
   public static double rescale(double value, double originalMin, double originalMax, double newMin, double newMax) {
      checkArgument(originalMax > originalMin, "original upper bound must be > original lower bound");
      checkArgument(newMax > newMin, "new upper bound must be > new lower bound");
      return ((value - originalMin) / (originalMax - originalMin)) * (newMax - newMin) + newMin;
   }

   /**
    * Safe log double.
    *
    * @param d the d
    * @return the double
    */
   public static double safeLog(double d) {
      if (Double.isFinite(d)) {
         return d <= 0d ? -10 : Math.log(d);
      }
      return 0d;
   }

   /**
    * Safe log double.
    *
    * @param d the d
    * @return the double
    */
   public static double safeLog2(double d) {
      if (Double.isFinite(d)) {
         return d <= 0d ? -10 : Math.log(d) / Math.log(2);
      }
      return 0d;
   }

   /**
    * <p>Sums the numbers in a given iterable treating them as doubles.</p>
    *
    * @param values the iterable of numbers to sum
    * @return the sum of the iterable
    * @throws NullPointerException if the values are null
    */
   public static double sum(Iterable<? extends Number> values) {
      return summaryStatistics(values).getSum();
   }

   /**
    * <p>Sums the numbers in the given array.</p>
    *
    * @param values the values to sum
    * @return the sum of the values
    * @throws NullPointerException if the values are null
    */
   public static double sum(double... values) {
      return DoubleStream.of(values).sum();
   }

   /**
    * <p>Sums the numbers in the given array.</p>
    *
    * @param values the values to sum
    * @return the sum of the values
    * @throws NullPointerException if the values are null
    */
   public static int sum(int... values) {
      return IntStream.of(values).sum();
   }

   /**
    * <p>Sums the numbers in the given array.</p>
    *
    * @param values the values to sum
    * @return the sum of the values
    * @throws NullPointerException if the values are null
    */
   public static long sum(long... values) {
      return LongStream.of(values).sum();
   }

   /**
    * <p>Calculates the summary statistics for the values in the given array.</p>
    *
    * @param values the values to calculate summary statistics over
    * @return the summary statistics of the given array
    * @throws NullPointerException if the values are null
    */
   public static EnhancedDoubleStatistics summaryStatistics(double... values) {
      return DoubleStream.of(values).parallel().collect(EnhancedDoubleStatistics::new,
                                                        EnhancedDoubleStatistics::accept,
                                                        EnhancedDoubleStatistics::combine);
   }

   /**
    * <p>Calculates the summary statistics for the values in the given array.</p>
    *
    * @param values the values to calculate summary statistics over
    * @return the summary statistics of the given array
    * @throws NullPointerException if the values are null
    */
   public static EnhancedDoubleStatistics summaryStatistics(int... values) {
      return IntStream.of(values).parallel().mapToDouble(i -> i).collect(EnhancedDoubleStatistics::new,
                                                                         EnhancedDoubleStatistics::accept,
                                                                         EnhancedDoubleStatistics::combine);
   }

   /**
    * <p>Calculates the summary statistics for the values in the given array.</p>
    *
    * @param values the values to calculate summary statistics over
    * @return the summary statistics of the given array
    * @throws NullPointerException if the values are null
    */
   public static EnhancedDoubleStatistics summaryStatistics(long... values) {
      return LongStream.of(values).parallel().mapToDouble(i -> i).collect(EnhancedDoubleStatistics::new,
                                                                          EnhancedDoubleStatistics::accept,
                                                                          EnhancedDoubleStatistics::combine);
   }

   /**
    * <p>Calculates the summary statistics for the values in the given iterable.</p>
    *
    * @param values the values to calculate summary statistics over
    * @return the summary statistics of the given iterable
    * @throws NullPointerException if the iterable is null
    */
   public static EnhancedDoubleStatistics summaryStatistics(Iterable<? extends Number> values) {
      return Streams.asStream(values)
                    .parallel()
                    .mapToDouble(Number::doubleValue)
                    .collect(EnhancedDoubleStatistics::new,
                             EnhancedDoubleStatistics::accept,
                             EnhancedDoubleStatistics::combine);
   }

   /**
    * Try to parse a string into a double returning null if a parse error occurs
    *
    * @param string the string to parse
    * @return the parsed Double value or null if not parsable
    */
   public static Double tryParseDouble(String string) {
      try {
         return Double.parseDouble(string);
      } catch (NumberFormatException t) {
         return null;
      }
   }

   /**
    * Try to parse a string into a float returning null if a parse error occurs
    *
    * @param string the string to parse
    * @return the parsed Float value or null if not parsable
    */
   public static Float tryParseFloat(String string) {
      try {
         return Float.parseFloat(string);
      } catch (NumberFormatException t) {
         return null;
      }
   }

   /**
    * Try to parse a string into a integer returning null if a parse error occurs
    *
    * @param string the string to parse
    * @return the parsed Integer value or null if not parsable
    */
   public static Integer tryParseInteger(String string) {
      try {
         return Integer.parseInt(string);
      } catch (NumberFormatException t) {
         return null;
      }
   }

   /**
    * Try to parse a string into a long returning null if a parse error occurs
    *
    * @param string the string to parse
    * @return the parsed long value or null if not parsable
    */
   public static Long tryParseLong(String string) {
      try {
         return Long.parseLong(string);
      } catch (NumberFormatException t) {
         return null;
      }
   }

}//END OF Math2
