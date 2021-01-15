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

package com.gengoai.math;

/**
 * Taken from <a href="https://commons.apache.org/proper/commons-codec/">commons-codec</a> MurmurHash3.java
 */
public class HashingFunctions {

   /**
    * Performs the final avalanche mix step of the 32-bit hash function {@code MurmurHash3_x86_32}.
    *
    * @param hash The current hash
    * @return The final hash
    */
   private static int fmix32(int hash) {
      hash ^= hash >>> 16;
      hash *= -2048144789;
      hash ^= hash >>> 13;
      hash *= -1028477387;
      hash ^= hash >>> 16;
      return hash;
   }

   /**
    * Gets the little-endian int from 4 bytes starting at the specified index.
    *
    * @param data The data
    * @param index The index
    * @return The little-endian int
    */
   private static int getLittleEndianInt(byte[] data, int index) {
      return data[index] & 255 | (data[index + 1] & 255) << 8 | (data[index + 2] & 255) << 16 | (data[index + 3] & 255) << 24;
   }

   /**
    * Generates 32-bit hash from the byte array with a seed of zero. This is a helper method that will produce the same
    * result as:
    *
    * <pre>
    * int offset = 0;
    * int seed = 0;
    * int hash = MurmurHash3.hash32x86(data, offset, data.length, seed);
    * </pre>
    *
    * @param data The input byte array
    * @return The 32-bit hash
    * @see #hash32x86(byte[], int, int, int)
    * @since 1.14
    */
   public static int hash32x86(final byte[] data) {
      return hash32x86(data, 0, data.length, 0);
   }

   /**
    * Generates 32-bit hash from the byte array with the given offset, length and seed.
    *
    * <p>This is an implementation of the 32-bit hash function {@code MurmurHash3_x86_32}
    * from from Austin Applyby's original MurmurHash3 {@code c++} code in SMHasher.</p>
    *
    * @param data The input byte array
    * @param offset The offset of data
    * @param length The length of array
    * @param seed The initial seed value
    * @return The 32-bit hash
    * @since 1.14
    */
   public static int hash32x86(byte[] data, int offset, int length, int seed) {
      int hash = seed;
      int nblocks = length >> 2;

      int index;
      int k1;
      for(index = 0; index < nblocks; ++index) {
         k1 = offset + (index << 2);
         int k = getLittleEndianInt(data, k1);
         hash = mix32(k, hash);
      }

      index = offset + (nblocks << 2);
      k1 = 0;
      switch(offset + length - index) {
         case 3:
            k1 ^= (data[index + 2] & 255) << 16;
         case 2:
            k1 ^= (data[index + 1] & 255) << 8;
         case 1:
            k1 ^= data[index] & 255;
            k1 *= -862048943;
            k1 = Integer.rotateLeft(k1, 15);
            k1 *= 461845907;
            hash ^= k1;
         default:
            hash ^= length;
            return fmix32(hash);
      }
   }

   /**
    * Performs the intermediate mix step of the 32-bit hash function {@code MurmurHash3_x86_32}.
    *
    * @param k The data to add to the hash
    * @param hash The current hash
    * @return The new hash
    */
   private static int mix32(int k, int hash) {
      k *= -862048943;
      k = Integer.rotateLeft(k, 15);
      k *= 461845907;
      hash ^= k;
      return Integer.rotateLeft(hash, 13) * 5 + -430675100;
   }

}//END OF HashingFunctions
