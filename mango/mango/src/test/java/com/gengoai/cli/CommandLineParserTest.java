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

package com.gengoai.cli;

import com.gengoai.application.CommandLineParser;
import com.gengoai.application.NamedOption;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommandLineParserTest {

   CommandLineParser cmd;

   @Before
   public void setUp() throws Exception {
      cmd = new CommandLineParser();
      cmd.addOption(NamedOption.builder()
                               .name("arg1")
                               .description("dummy")
                               .required(true)
                               .type(String.class)
                               .build()
                   );
      cmd.addOption(NamedOption.builder()
                               .name("arg2")
                               .description("dummy")
                               .type(Boolean.class)
                               .build()
                   );
      cmd.addOption(NamedOption.builder()
                               .name("arg3")
                               .description("dummy")
                               .type(String.class)
                               .build()
                   );
      cmd.addOption(NamedOption.builder()
                               .name("a")
                               .description("dummy")
                               .type(String.class)
                               .build()
                   );
      cmd.addOption(NamedOption.builder()
                               .name("b")
                               .type(String.class)
                               .description("dummy")
                               .alias("blong")
                               .build()
                   );
      cmd.addOption(NamedOption.builder()
                               .name("z")
                               .description("dummy")
                               .type(Boolean.class)
                               .build()
                   );

   }

   @Test
   public void cleanParse() throws Exception {
      cmd.parse(new String[]{"--arg1=ALPHA", "--arg2", "-a", "BETA", "--blong", "=", "GAMMA", "-z"});
      assertEquals("ALPHA", cmd.get("arg1"));
      assertEquals(true, cmd.get("arg2"));
      assertEquals("BETA", cmd.get("a"));
      assertEquals("GAMMA", cmd.get("b"));
      assertEquals(true, cmd.get("z"));
   }

   @Test
   public void notSet() throws Exception {
      cmd.parse(new String[]{"--arg1=ALPHA", "-a", "BETA", "--blong", "=", "GAMMA"});
      assertEquals("ALPHA", cmd.get("arg1"));
      assertEquals(false, cmd.get("arg2"));
      assertEquals("BETA", cmd.get("a"));
      assertEquals("GAMMA", cmd.get("b"));
      assertEquals(false, cmd.get("z"));
   }

   @Test
   public void explicitFalse() throws Exception {
      cmd.parse(new String[]{"--arg1=ALPHA", "-a", "BETA", "--blong", "=", "GAMMA", "-z=FALSE", "--arg2", "false"});
      assertEquals("ALPHA", cmd.get("arg1"));
      assertEquals(false, cmd.get("arg2"));
      assertEquals("BETA", cmd.get("a"));
      assertEquals("GAMMA", cmd.get("b"));
      assertEquals(false, cmd.get("z"));
   }

   @Test
   public void notSpecified() throws Exception {
      cmd.parse(new String[]{"--arg1=ALPHA", "--unknown", "SIGMA"});
      assertEquals("ALPHA", cmd.get("arg1"));
      assertEquals("SIGMA", cmd.get("unknown"));
   }
}
