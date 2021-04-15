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

package com.gengoai;

import com.gengoai.config.Config;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class HierarchicalEnumValueTest {

   public static final RanksEnum PRESIDENT = RanksEnum.create("PRESIDENT");
   public static final RanksEnum GENERAL = RanksEnum.create(PRESIDENT, "GENERAL");
   public static final RanksEnum COLONEL = RanksEnum.create(GENERAL, "COLONEL");
   public static final RanksEnum MAJOR = RanksEnum.create(COLONEL, "MAJOR");
   public static final RanksEnum CAPTAIN = RanksEnum.create("CAPTAIN");

   @Before
   public void setUp() throws Exception {
      Config.initializeTest();
   }

   @Test
   public void testGetLabel(){
      assertEquals(GENERAL, RanksEnum.create("GENERAL"));
   }

   @Test(expected = IllegalArgumentException.class)
   public void nonUniqueLabel() {
      RanksEnum.create(COLONEL, "CAPTAIN");
   }

   @Test
   public void isRoot() throws Exception {
      assertFalse(PRESIDENT.isRoot());
      assertFalse(MAJOR.isRoot());
      assertTrue(RanksEnum.ROOT.isRoot());
   }

   @Test
   public void getChildren() throws Exception {
      assertTrue(PRESIDENT.children().contains(GENERAL)); //ADMIRAL isn't defined yet.
      assertTrue(COLONEL.children().contains(MAJOR));
      assertTrue(MAJOR.children().isEmpty());
   }

   @Test
   public void isLeaf() throws Exception {
      assertFalse(PRESIDENT.isLeaf());
      assertTrue(MAJOR.isLeaf());
      assertFalse(RanksEnum.ROOT.isLeaf());
   }

   @Test
   public void getParent() throws Exception {
      //Dynamically create ADMIRAL from Config and set parent of Captain
      assertEquals(RanksEnum.ROOT.name(), CAPTAIN.parent().name());
      assertEquals(GENERAL, COLONEL.parent());
   }

   @Test
   public void values() throws Exception {
      assertTrue(RanksEnum.values().contains(CAPTAIN));
   }

   @Test
   public void valueOf() throws Exception {
      assertEquals(CAPTAIN, RanksEnum.create("captain"));
   }

   @Test
   public void isInstance() throws Exception {
      assertTrue(MAJOR.isInstance(GENERAL));
      assertTrue(MAJOR.isInstance(GENERAL, COLONEL, CAPTAIN));
      assertFalse(MAJOR.isInstance(CAPTAIN));
      assertFalse(COLONEL.isInstance(CAPTAIN, MAJOR));
   }
//
//   @Test
//   public void getAncestors() throws Exception {
//      assertEquals(Lists.arrayListOf(GENERAL, PRESIDENT), COLONEL.getAncestors());
//   }

}