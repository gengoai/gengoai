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

package com.gengoai.concurrent;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * @author David B. Bracewell
 */
public class BlockingThreadPoolExecutorTest {

  private static class Sleeper implements Runnable {
    public static final AtomicInteger ai = new AtomicInteger();

    @Override
    public void run() {
      Threads.sleep(100);
      ai.incrementAndGet();
    }
  }

  @Test
  public void testCreateBlockingThreadPool() throws Exception {
    BlockingThreadPoolExecutor executor = new BlockingThreadPoolExecutor(1, 1, 3);
    for (int i = 0; i < 10; i++) {
      executor.submit(new Sleeper());
    }
    executor.awaitTermination();
    executor.shutdown();
    assertEquals(10, Sleeper.ai.intValue());
  }
}
