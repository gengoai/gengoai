/*
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

package com.gengoai.apollo.math;

/**
 * @author David B. Bracewell
 */
public final class Operator {

   private Operator() {
      throw new IllegalAccessError();
   }

   /**
    * Adds two float (useful as a method reference)
    *
    * @param v1 value 1
    * @param v2 value 2
    * @return the sum of value 1 and value 2
    */
   public static float add(float v1, float v2) {
      return v1 + v2;
   }

   /**
    * Adds two doubles (useful as a method reference)
    *
    * @param v1 value 1
    * @param v2 value 2
    * @return the sum of value 1 and value 2
    */
   public static double add(double v1, double v2) {
      return v1 + v2;
   }

   /**
    * Adds two int (useful as a method reference)
    *
    * @param v1 value 1
    * @param v2 value 2
    * @return the sum of value 1 and value 2
    */
   public static int add(int v1, int v2) {
      return v1 + v2;
   }

   /**
    * Adds two int (useful as a method reference)
    *
    * @param v1 value 1
    * @param v2 value 2
    * @return the sum of value 1 and value 2
    */
   public static long add(long v1, long v2) {
      return v1 + v2;
   }


   /**
    * Divides two float (useful as a method reference)
    *
    * @param v1 value 1
    * @param v2 value 2
    * @return the result of value 1 divided by value 2
    */
   public static float divide(float v1, float v2) {
      return v1 / v2;
   }

   /**
    * Divides two doubles (useful as a method reference)
    *
    * @param v1 value 1
    * @param v2 value 2
    * @return the result of value 1 divided by value 2
    */
   public static double divide(double v1, double v2) {
      return v1 / v2;
   }

   /**
    * Divides two int (useful as a method reference)
    *
    * @param v1 value 1
    * @param v2 value 2
    * @return the result of value 1 divided by value 2
    */
   public static int divide(int v1, int v2) {
      return v1 / v2;
   }

   /**
    * Multiplies two float (useful as a method reference)
    *
    * @param v1 value 1
    * @param v2 value 2
    * @return the result of value 1 * value 2
    */
   public static float multiply(float v1, float v2) {
      return v1 * v2;
   }

   /**
    * Multiplies two doubles (useful as a method reference)
    *
    * @param v1 value 1
    * @param v2 value 2
    * @return the result of value 1 * value 2
    */
   public static double multiply(double v1, double v2) {
      return v1 * v2;
   }

   /**
    * Multiplies two int (useful as a method reference)
    *
    * @param v1 value 1
    * @param v2 value 2
    * @return the result of value 1 * value 2
    */
   public static int multiply(int v1, int v2) {
      return v1 * v2;
   }

   /**
    * Subtracts two float (useful as a method reference)
    *
    * @param v1 value 1
    * @param v2 value 2
    * @return the result of value 1 minus value 2
    */
   public static float subtract(float v1, float v2) {
      return v1 - v2;
   }

   /**
    * Subtracts two doubles (useful as a method reference)
    *
    * @param v1 value 1
    * @param v2 value 2
    * @return the result of value 1 minus value 2
    */
   public static double subtract(double v1, double v2) {
      return v1 - v2;
   }

   /**
    * Subtracts two int (useful as a method reference)
    *
    * @param v1 value 1
    * @param v2 value 2
    * @return the result of value 1 minus value 2
    */
   public static int subtract(int v1, int v2) {
      return v1 - v2;
   }
}//END OF Operator
