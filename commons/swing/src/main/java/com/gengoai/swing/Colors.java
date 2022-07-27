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

package com.gengoai.swing;

import com.gengoai.Validation;
import com.gengoai.reflection.Reflect;
import com.gengoai.string.Strings;
import lombok.NonNull;

import java.awt.*;
import java.util.Random;

/**
 * Convenience method for working with colors
 */
public final class Colors {

   private Colors() {
      throw new IllegalAccessError();
   }


   /**
    * Adjusts the given color by multiplying the given factor on the red, green, and blue values.
    *
    * @param c      the color
    * @param factor the factor ( less than 1 for darker and greater than 1 for brighter )
    * @return the adjusted color
    */
   public static Color adjust(@NonNull Color c, double factor) {
      Validation.checkArgument(factor >= 0);
      return new Color(
            (int) (c.getRed() * factor),
            (int) (c.getGreen() * factor),
            (int) (c.getBlue() * factor)
      );
   }

   /**
    * Determines if the best font color for the given background should be BLACK or WHITE.
    *
    * @param background the background
    * @return the font color
    */
   public static Color calculateBestFontColor(@NonNull Color background) {
      if((background.getRed() * 0.299 + background.getGreen() * 0.587 + background.getBlue() * 0.114) > 128) {
         return Color.BLACK;
      }
      return Color.WHITE;
   }

   public static boolean isDark(@NonNull Color background) {
      return (background.getRed() * 0.299 + background.getGreen() * 0.587 + background.getBlue() * 0.114) < 100;
   }

   /**
    * Converts the given String into a Color value either parsing the value or Checking if it is a static field on the
    * Color class
    *
    * @param value the value
    * @return the color
    */
   public static Color parseColor(final String value) {
      if(Strings.isNullOrBlank(value)) {
         return Color.GRAY;
      }
      try {
         return Color.decode(value);
      } catch(NumberFormatException nfe) {
         try {
            return Reflect.onClass(Color.class)
                          .getField(value)
                          .get();
         } catch(Exception ce) {
            throw new RuntimeException("Invalid Color: " + value);
         }
      }
   }

   /**
    * Creates a random RGB color
    *
    * @return the color
    */
   public static Color randomColor() {
      Random rnd = new Random();
      return new Color(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
   }

   /**
    * Generates a random color mixing it with the given base color
    *
    * @param baseColor the base color
    * @return the color
    */
   public static Color randomColor(@NonNull Color baseColor) {
      Random rnd = new Random();
      int red = (rnd.nextInt(256) + baseColor.getRed()) / 2;
      int green = (rnd.nextInt(256) + baseColor.getGreen()) / 2;
      int blue = (rnd.nextInt(256) + baseColor.getBlue()) / 2;
      return new Color(red, green, blue);
   }

   /**
    * Generates an HTML valid Hex representation of the given color.
    *
    * @param color the color
    * @return the hex representation of the color
    */
   public static String toHexString(Color color) {
      return "#" +
            Strings.padStart(Integer.toHexString(color.getRed()), 2, '0') +
            Strings.padStart(Integer.toHexString(color.getGreen()), 2, '0') +
            Strings.padStart(Integer.toHexString(color.getBlue()), 2, '0');
   }

}//END OF Colors
