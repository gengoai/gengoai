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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A <code>ThreadPoolExecutor</code> which blocks when the queue of items to be executed becomes full.
 *
 * @author David B. Bracewell
 */
public class BlockingThreadPoolExecutor extends ThreadPoolExecutor {

   private final AtomicInteger tasksInProgress = new AtomicInteger();
   private final Semaphore semaphore;
   private final Lock lock = new ReentrantLock();
   private final Condition done = lock.newCondition();
   private boolean isDone = false;

   /**
    * Creates an executor with a given core and max pool size and maximum number of items to have waiting in the queue.
    * All threads will execute until completion or failure.
    *
    * @param corePoolSize    core pool size
    * @param maximumPoolSize max pools size
    * @param maxQueueSize    max number of items to have waiting (good for memory)
    */
   public BlockingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, int maxQueueSize) {
      this(corePoolSize, maximumPoolSize, maxQueueSize, 0, TimeUnit.DAYS);
   }

   /**
    * Creates an executor with a given core and max pool size and maximum number of items to have waiting in the queue.
    * All threads will execute until completion or the keep alive time runs out.
    *
    * @param corePoolSize    core pool size
    * @param maximumPoolSize max pools size
    * @param maxQueueSize    max number of items to have waiting (good for memory)
    * @param keepAliveTime   the amount of time to let the thread run
    * @param unit            the time unit associated with keepAliveTime
    */
   public BlockingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, int maxQueueSize, long keepAliveTime, TimeUnit unit) {
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new ArrayBlockingQueue<>(maxQueueSize));
      this.semaphore = new Semaphore(maxQueueSize);
   }

   /**
    * @return The number of tasks in progress
    */
   public int tasksInProgress() {
      return tasksInProgress.get();
   }

   @Override
   public void execute(Runnable command) {
      tasksInProgress.incrementAndGet();
      try {
         semaphore.acquireUninterruptibly();
         super.execute(command);
      } catch (Error e) {
         tasksInProgress.decrementAndGet();
      }
   }


   @Override
   protected void afterExecute(Runnable r, Throwable t) {
      semaphore.release();
      super.afterExecute(r, t);

      synchronized (this) {
         tasksInProgress.decrementAndGet();
         if (tasksInProgress.intValue() == 0) {
            lock.lock();
            try {
               isDone = true;
               done.signalAll();
            } finally {
               lock.unlock();
            }
         }
      }

   }

   /**
    * Awaits termination of the threads
    *
    * @throws InterruptedException The executor was interrupted
    */
   public void awaitTermination() throws InterruptedException {
      lock.lock();
      try {
         while (!isDone) {
            done.await();
         }
      } finally {
         isDone = false;
         lock.unlock();
      }
   }

}//END OF BlockingThreadPoolExecutor
