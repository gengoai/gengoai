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

package com.gengoai.apollo.math.linalg;

import com.gengoai.apollo.math.linalg.dense.DenseStringNDArray;
import com.gengoai.json.Json;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public abstract class BaseObjectNDArrayTest {

   final ObjectNDArray<String> matrix34;
   final ObjectNDArray<String> matrix43;
   final ObjectNDArray<String> tensor2234;
   final ObjectNDArray<String> tensor234;
   final ObjectNDArray<String> rowVector;
   final ObjectNDArray<String> colVector;
   final ObjectNDArray<String> scalar;
   final ObjectNDArrayFactory<String> factory;

   public BaseObjectNDArrayTest(ObjectNDArrayFactory<String> factory) {
      this.factory = factory;
      this.matrix34 = factory.array(new String[][]{
            {"A", "B", "C", "D"},
            {"E", "F", "G", "H"},
            {"I", "J", "K", "L"}
      });
      this.matrix43 = factory.array(new String[][]{
            {"A", "B", "C"},
            {"D", "E", "F"},
            {"G", "H", "I"},
            {"J", "K", "L"}
      });
      this.rowVector = factory.array(new String[]{"1", "2", "3", "4"});
      this.colVector = factory.array(new String[][]{
            {"1.0"},
            {"2.0"},
            {"3.0"},
            {"4.0"},
      });
      this.tensor234 = factory.create(Shape.shape(2, 3, 4), supplier());
      this.tensor2234 = factory.create(Shape.shape(2, 2, 3, 4), supplier());
      this.scalar = factory.scalar("TEST");
   }

   public static Supplier<String> supplier() {
      return new Supplier<String>() {
         char c = 'A';

         @Override
         public String get() {
            String out = Character.toString(c);
            c++;
            return out;
         }
      };
   }

   @Test(expected = IllegalArgumentException.class)
   public void badMapAxisPosition() {
      tensor234.map(Shape.KERNEL, 2, "A", String::concat);
   }

   @Test
   public void compact() {
      tensor234.compact();
   }

   @Test
   public void fillIf() {
      assertEquals(factory.scalar("TEST"), factory.zeros(1).fillIf(Objects::isNull, "TEST"));
   }

   @Test
   public void forEachSparse() {
      ObjectNDArray<String> s = factory.array(new String[]{null, null, "A"});
      List<String> out = new ArrayList<>();
      s.forEachSparse((i, v) -> out.add(v));
      assertArrayEquals(new String[]{"A"},
                        out.toArray(new String[0]));
   }

   @Test
   public void json() throws Exception {
      String json = Json.dumps(tensor2234);
      assertEquals(tensor2234, Json.parse(json, NDArray.class));
   }

   @Test
   public void map() {
      assertEquals(factory.empty(), factory.empty().map(String::toLowerCase));
      assertEquals(factory.empty(), factory.empty().map(factory.empty(), String::concat));
      assertEquals(factory.empty(), factory.empty().map(Shape.COLUMN, factory.empty(), String::concat));
      assertEquals(factory.empty(), factory.empty().map(Shape.ROW, 0, "A", String::concat));
      assertEquals(factory.empty(), factory.empty().map(Shape.ROW, 0, factory.empty(), String::concat));

      assertEquals(factory.array(new String[][]{
            {"AA", "BA"},
            {"CA", "DA"}
      }), factory.array(new String[][]{
            {"A", "B"},
            {"C", "D"}
      }).map(Shape.ROW, factory.scalar("A"), String::concat));

      assertEquals(factory.array(new String[]{
            "CA", "DA"
      }), factory.array(new String[]{
            "C", "D"
      }).map(Shape.COLUMN, factory.array(new String[]{"A", "A"}), String::concat));

      assertEquals(factory.array(new String[]{"aa", "ab"}), factory.array(new String[]{"a", "b"})
                                                                   .map(factory.scalar("a"), (a, b) -> b + a));

      assertEquals(factory.array(new String[][]{
                         {"A1", "B2", "C3", "D4"},
                         {"E", "F", "G", "H"},
                         {"I", "J", "K", "L"}
                   }),
                   matrix34.map(Shape.ROW, 0, rowVector, String::concat));


      assertEquals(factory.array(new String[]{"11", "22", "33", "44"}),
                   rowVector.map(Shape.ROW, 0, factory.array(new String[]{"1", "2", "3", "4"}), String::concat));

      assertEquals(factory.array(new String[][][][]{
                         {

                               {
                                     {"A1", "B2", "C3", "D4"},
                                     {"E1", "F2", "G3", "H4"},
                                     {"I1", "J2", "K3", "L4"}
                               },
                               {
                                     {"M1", "N2", "O3", "P4"},
                                     {"Q1", "R2", "S3", "T4"},
                                     {"U1", "V2", "W3", "X4"}
                               }
                         },
                         {
                               {
                                     {"Y", "Z", "[", "\\"},
                                     {"]", "^", "_", "`"},
                                     {"a", "b", "c", "d"}
                               },
                               {
                                     {"e", "f", "g", "h"},
                                     {"i", "j", "k", "l"},
                                     {"m", "n", "o", "p"}
                               }
                         }
                   }),
                   tensor2234.map(Shape.KERNEL, 0, rowVector, String::concat));


      assertEquals(factory.array(new String[][][][]{
                         {

                               {
                                     {"A1", "B2", "C3", "D4"},
                                     {"E1", "F2", "G3", "H4"},
                                     {"I1", "J2", "K3", "L4"}
                               },
                               {
                                     {"M1", "N2", "O3", "P4"},
                                     {"Q1", "R2", "S3", "T4"},
                                     {"U1", "V2", "W3", "X4"}
                               }
                         },
                         {
                               {
                                     {"Y1", "Z2", "[3", "\\4"},
                                     {"]1", "^2", "_3", "`4"},
                                     {"a1", "b2", "c3", "d4"}
                               },
                               {
                                     {"e1", "f2", "g3", "h4"},
                                     {"i1", "j2", "k3", "l4"},
                                     {"m1", "n2", "o3", "p4"}
                               }
                         }
                   }),
                   tensor2234.map(Shape.ROW,  rowVector, String::concat));

      assertEquals(factory.array(new String[][][][]{
                         {

                               {
                                     {"A1", "B2", "C3", "D4"},
                                     {"E", "F", "G", "H"},
                                     {"I", "J", "K", "L"}
                               },
                               {
                                     {"M1", "N2", "O3", "P4"},
                                     {"Q", "R", "S", "T"},
                                     {"U", "V", "W", "X"}
                               }
                         },
                         {
                               {
                                     {"Y1", "Z2", "[3", "\\4"},
                                     {"]", "^", "_", "`"},
                                     {"a", "b", "c", "d"}
                               },
                               {
                                     {"e1", "f2", "g3", "h4"},
                                     {"i", "j", "k", "l"},
                                     {"m", "n", "o", "p"}
                               }
                         }
                   }),
                   tensor2234.map(Shape.ROW, 0, rowVector, String::concat));

      assertEquals(factory.array(new String[][]{
                         {"Aa", "Ba", "Ca", "Da"},
                         {"E", "F", "G", "H"},
                         {"I", "J", "K", "L"}
                   }),
                   matrix34.map(Shape.ROW, 0, factory.scalar("a"), String::concat));

      assertEquals(factory.array(new String[][]{
                         {"A1", "B1", "C1", "D1"},
                         {"E2", "F2", "G2", "H2"},
                         {"I3", "J3", "K3", "L3"}
                   }),
                   matrix34.map(factory.array(Shape.shape(3, 1), new String[]{"1", "2", "3"}), String::concat));

      assertEquals(factory.array(new String[]{"aa", "ab"}), factory.array(new String[]{"a", "b"})
                                                                   .map(factory.scalar("a"), (a, b) -> b + a));
      assertEquals(factory.array(new String[][][]{
            {
                  {"a", "b", "c", "d"},
                  {"e", "f", "g", "h"},
                  {"i", "j", "k", "l"}
            },
            {
                  {"m", "n", "o", "p"},
                  {"q", "r", "s", "t"},
                  {"u", "v", "w", "x"}
            }
      }), tensor234.map(String::toLowerCase));

      assertEquals(factory.array(new String[]{"1a", "2a", "3a", "4a"}),
                   rowVector.map("a", (a, b) -> a + b));

      assertEquals(factory.array(new String[][][]{
            {
                  {"Aa", "B", "C", "D"},
                  {"Ea", "F", "G", "H"},
                  {"Ia", "J", "K", "L"}
            },
            {
                  {"Ma", "N", "O", "P"},
                  {"Qa", "R", "S", "T"},
                  {"Ua", "V", "W", "X"}
            }
      }), tensor234.map(Shape.COLUMN, 0, "a", (a, b) -> a + b));

      assertEquals(factory.array(new String[][][]{
            {
                  {"aa", "bb", "cc", "dd"},
                  {"ee", "ff", "gg", "hh"},
                  {"ii", "jj", "kk", "ll"}
            },
            {
                  {"mm", "nn", "oo", "pp"},
                  {"qq", "rr", "ss", "tt"},
                  {"uu", "vv", "ww", "xx"}
            }
      }), tensor234.map(tensor234, (a, b) -> (a + b).toLowerCase()));
      assertEquals(factory.array(new String[][][]{
            {
                  {"aa", "bb", "cc", "dd"},
                  {"ee", "ff", "gg", "hh"},
                  {"ii", "jj", "kk", "ll"}
            },
            {
                  {"ma", "nb", "oc", "pd"},
                  {"qe", "rf", "sg", "th"},
                  {"ui", "vj", "wk", "xl"}
            }
      }), tensor234.map(Shape.ROW, factory.create(Shape.shape(3, 4), supplier()), (a, b) -> (a + b).toLowerCase()));
      assertEquals(factory.array(new String[][][]{
            {
                  {"aa", "bb", "cc", "dd"},
                  {"E", "F", "G", "H"},
                  {"I", "J", "K", "L"}
            },
            {
                  {"ma", "nb", "oc", "pd"},
                  {"Q", "R", "S", "T"},
                  {"U", "V", "W", "X"}
            }
      }), tensor234.map(Shape.ROW, 0, factory.array(new String[]{"a", "b", "c", "d"}), (a, b) -> (a + b)
            .toLowerCase()));


      assertEquals(factory.array(new String[]{"1a", "2a", "3a", "4a"}),
                   rowVector.copy().mapi("a", (a, b) -> a + b));
      assertEquals(factory.array(new String[][][]{
            {
                  {"Aa", "B", "C", "D"},
                  {"Ea", "F", "G", "H"},
                  {"Ia", "J", "K", "L"}
            },
            {
                  {"Ma", "N", "O", "P"},
                  {"Qa", "R", "S", "T"},
                  {"Ua", "V", "W", "X"}
            }
      }), tensor234.copy().mapi(Shape.COLUMN, 0, "a", (a, b) -> a + b));
      assertEquals(factory.array(new String[][][]{
            {
                  {"a", "b", "c", "d"},
                  {"e", "f", "g", "h"},
                  {"i", "j", "k", "l"}
            },
            {
                  {"m", "n", "o", "p"},
                  {"q", "r", "s", "t"},
                  {"u", "v", "w", "x"}
            }
      }), tensor234.copy().mapi(String::toLowerCase));
      assertEquals(factory.array(new String[][][]{
            {
                  {"aa", "bb", "cc", "dd"},
                  {"ee", "ff", "gg", "hh"},
                  {"ii", "jj", "kk", "ll"}
            },
            {
                  {"mm", "nn", "oo", "pp"},
                  {"qq", "rr", "ss", "tt"},
                  {"uu", "vv", "ww", "xx"}
            }
      }), tensor234.copy().mapi(tensor234, (a, b) -> (a + b).toLowerCase()));
      assertEquals(factory.array(new String[][][]{
            {
                  {"aa", "bb", "cc", "dd"},
                  {"ee", "ff", "gg", "hh"},
                  {"ii", "jj", "kk", "ll"}
            },
            {
                  {"ma", "nb", "oc", "pd"},
                  {"qe", "rf", "sg", "th"},
                  {"ui", "vj", "wk", "xl"}
            }
      }), tensor234.copy()
                   .mapi(Shape.ROW, factory.create(Shape.shape(3, 4), supplier()), (a, b) -> (a + b).toLowerCase()));
      assertEquals(factory.array(new String[][][]{
            {
                  {"aa", "bb", "cc", "dd"},
                  {"E", "F", "G", "H"},
                  {"I", "J", "K", "L"}
            },
            {
                  {"ma", "nb", "oc", "pd"},
                  {"Q", "R", "S", "T"},
                  {"U", "V", "W", "X"}
            }
      }), tensor234.copy().mapi(Shape.ROW, 0, factory.array(new String[]{"a", "b", "c", "d"}), (a, b) -> (a + b)
            .toLowerCase()));

   }

   @Test
   public void optimium() {
      assertEquals("4", rowVector.max());
      assertEquals(3, rowVector.argMaxOffset());
      assertEquals("1", rowVector.min());
      assertEquals(0, rowVector.argMinOffset());

      assertEquals(factory.array(new String[]{"A", "B", "C", "D"}), matrix34.min(Shape.ROW));
      assertEquals(nd.DINT32.array(new int[]{0, 0, 0, 0}), matrix34.argMin(Shape.ROW));
      assertEquals(factory.array(new String[]{"I", "J", "K", "L"}), matrix34.max(Shape.ROW));
      assertEquals(nd.DINT32.array(new int[]{2, 2, 2, 2}), matrix34.argMax(Shape.ROW));
   }

   @Test
   public void padding() {
      assertEquals(factory.array(new String[][]{
            {"1", "2", "3", "4", null},
            {null, null, null, null, null}
      }).toString(), rowVector.padPost(Shape.ROW, 2, Shape.COLUMN, 5).toString());
      assertEquals(factory.array(new String[][]{
            {"1", "2", "3", "4", null},
            {null, null, null, null, null}
      }).toString(), rowVector.padPost(Shape.shape(2, 5)).toString());
      assertEquals(factory.array(new String[][]{
            {"1", "2", "3", "4", "-PAD-"},
            {"-PAD-", "-PAD-", "-PAD-", "-PAD-", "-PAD-"}
      }).toString(), rowVector.padPostWith("-PAD-", Shape.ROW, 2, Shape.COLUMN, 5).toString());
      assertEquals(factory.array(new String[][]{
            {"1", "2", "3", "4", "-PAD-"},
            {"-PAD-", "-PAD-", "-PAD-", "-PAD-", "-PAD-"}
      }).toString(), rowVector.padPostWith("-PAD-", Shape.shape(2, 5)).toString());
   }

   @Test
   public void reshape() {
      ObjectNDArray<String> s = factory.array(Shape.shape(3, 2), new String[]{"A", "B", "C", "D", "E", "F"});
      s.reshape(2, 3);
      assertEquals(Shape.shape(2, 3), s.shape());
      assertArrayEquals(new String[]{"A", "B", "C", "D", "E", "F"}, s.toArray());
   }

   @Test
   public void set() {
      assertEquals(factory.array(new String[]{"1.0", "2.0", "3.0", "10.0"}),
                   factory.array(new String[]{"1.0", "2.0", "3.0", "10.0"}).set(3, "10.0"));
      assertEquals(factory.array(new String[]{"1.0", "2.0", "3.0", "10.0"}),
                   factory.array(new String[]{"1.0", "2.0", "3.0", "10.0"}).set(0, 3, "10.0"));
      assertEquals(factory.array(new String[]{"1.0", "2.0", "3.0", "10.0"}),
                   factory.array(new String[]{"1.0", "2.0", "3.0", "10.0"}).set(0, 0, 3, "10.0"));
      assertEquals(factory.array(new String[]{"1.0", "2.0", "3.0", "10.0"}),
                   factory.array(new String[]{"1.0", "2.0", "3.0", "10.0"}).set(0, 0, 0, 3, "10.0"));
      assertEquals(factory.array(new String[][]{
            {"A", "Z", "C", "D"},
            {"E", "Z", "G", "H"},
            {"I", "Z", "K", "L"}
      }), matrix34.copy().setAxis(Shape.COLUMN, 1, "Z"));
      assertEquals(factory.array(new String[][]{
            {"A", "Z", "C", "D"},
            {"E", "Z", "G", "H"},
            {"I", "Z", "K", "L"}
      }), matrix34.copy().setAxis(Shape.COLUMN, 1, factory.array(new String[]{"Z", "Z", "Z"})));

      assertEquals(factory.array(new String[][]{
                         {"A", "Z", "C", "D"},
                         {"E", "Z", "G", "H"},
                         {"I", "Z", "K", "L"}
                   }), matrix34.copy()
                               .set(0, 1, "Z")
                               .set(1, 1, "Z")
                               .set(2, 1, "Z")
      );

      assertEquals(factory.zeros(3, 4).fill("Z"), matrix34.copy().setRange(matrix34.shape().range(), "Z"));
      assertEquals(factory.zeros(3, 4).fill("Z"), matrix34.copy().setRange(matrix34.shape().range(), factory.zeros(3, 4)
                                                                                                            .fill("Z")));
      assertEquals(factory.zeros(3, 4).fill("Z"), matrix34.copy().setRange(matrix34.shape().range(), factory.zeros(4)
                                                                                                            .fill("Z")));

      assertEquals(factory.zeros(3, 4).fill("Z"), matrix34.copy().setSlice(0, factory.zeros(3, 4).fill("Z")));

   }

   @Test
   public void slice() {
      assertEquals(tensor234, tensor2234.slice(0, 0, 1, 2));
   }

   @Test
   public void testCons() {
      ObjectNDArray<String> s = factory.array(Shape.shape(3, 2), new String[]{"A", "B", "C", "D", "E", "F"});
      assertArrayEquals(new String[]{"A", "B", "C", "D", "E", "F"}, s.toArray());


      assertEquals(tensor234, factory.array(new String[][][]{
            {
                  {"A", "B", "C", "D"},
                  {"E", "F", "G", "H"},
                  {"I", "J", "K", "L"}
            },
            {
                  {"M", "N", "O", "P"},
                  {"Q", "R", "S", "T"},
                  {"U", "V", "W", "X"}
            }
      }));
      assertEquals(tensor2234, factory.array(new String[][][][]{
            {

                  {
                        {"A", "B", "C", "D"},
                        {"E", "F", "G", "H"},
                        {"I", "J", "K", "L"}
                  },
                  {
                        {"M", "N", "O", "P"},
                        {"Q", "R", "S", "T"},
                        {"U", "V", "W", "X"}
                  }
            },
            {
                  {
                        {"Y", "Z", "[", "\\"},
                        {"]", "^", "_", "`"},
                        {"a", "b", "c", "d"}
                  },
                  {
                        {"e", "f", "g", "h"},
                        {"i", "j", "k", "l"},
                        {"m", "n", "o", "p"}
                  }
            }
      }));
   }

   @Test
   public void toTensor() {
//      if( tensor234 instanceof DenseStringNDArray) {
//         assertEquals(tensor234, nd.convertTensor(tensor234.toTensor()));
//         assertEquals(rowVector, nd.convertTensor(rowVector.toTensor()));
//         assertEquals(matrix34, nd.convertTensor(matrix34.toTensor()));
//         assertEquals(tensor2234, nd.convertTensor(tensor2234.toTensor()));
//      }
   }

   @Test
   public void transpose() {
      assertEquals(factory.array(new String[][]{{"1.0", "2.0", "3.0", "4.0"}}), colVector.T());
      assertEquals(factory.array(new String[][]{
            {"A", "E", "I"},
            {"B", "F", "J"},
            {"C", "G", "K"},
            {"D", "H", "L"}
      }), matrix34.transpose(Shape.COLUMN, Shape.ROW));
   }


}
