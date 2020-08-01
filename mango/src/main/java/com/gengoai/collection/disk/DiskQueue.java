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

package com.gengoai.collection.disk;

import com.gengoai.Validation;
import com.gengoai.collection.Iterators;
import com.gengoai.io.MonitoredObject;
import com.gengoai.io.ResourceMonitor;
import com.gengoai.io.resource.Resource;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

public class DiskQueue<E> implements Queue<E>, Serializable, AutoCloseable {
   private static final long serialVersionUID = 1L;
   private final MonitoredObject<MapDBHandle> handle;
   @Getter
   private final String nameSpace;
   @Getter
   private final boolean readOnly;
   private volatile transient Queue<E> queue;

   @Builder
   private DiskQueue(@NonNull Resource file, String namespace, boolean compressed, boolean readOnly) {
      this.nameSpace = Validation.notNullOrBlank(namespace);
      this.handle = ResourceMonitor.monitor(new MapDBHandle(file, compressed));
      this.readOnly = readOnly;
   }

   @Override
   public boolean add(E e) {
      if (readOnly) {
         throw new UnsupportedOperationException();
      }
      return delegate().add(e);
   }

   @Override
   public boolean addAll(Collection<? extends E> collection) {
      if (readOnly) {
         throw new UnsupportedOperationException();
      }
      return delegate().addAll(collection);
   }

   @Override
   public void clear() {
      if (readOnly) {
         throw new UnsupportedOperationException();
      }
      delegate().clear();
   }

   @Override
   public void close() throws Exception {
      handle.object.close();
   }

   public void commit() {
      handle.object.commit();
   }

   @Override
   public boolean contains(Object o) {
      return delegate().contains(o);
   }

   @Override
   public boolean containsAll(Collection<?> collection) {
      return delegate().containsAll(collection);
   }

   private Queue<E> delegate() {
      if (queue == null) {
         synchronized (this) {
            if (queue == null) {
               queue = this.handle.object.getStore().getQueue(getNameSpace());
            }
         }
      }
      return queue;
   }

   @Override
   public E element() {
      return delegate().element();
   }

   @Override
   public boolean isEmpty() {
      return delegate().isEmpty();
   }

   @Override
   public Iterator<E> iterator() {
      if (readOnly) {
         return Iterators.unmodifiableIterator(delegate().iterator());
      }
      return delegate().iterator();
   }

   @Override
   public boolean offer(E e) {
      if (readOnly) {
         throw new UnsupportedOperationException();
      }
      return delegate().offer(e);
   }

   @Override
   public E peek() {
      return delegate().peek();
   }

   @Override
   public E poll() {
      if (readOnly) {
         throw new UnsupportedOperationException();
      }
      return delegate().poll();
   }

   @Override
   public boolean remove(Object o) {
      if (readOnly) {
         throw new UnsupportedOperationException();
      }
      return delegate().remove(o);
   }

   @Override
   public E remove() {
      if (readOnly) {
         throw new UnsupportedOperationException();
      }
      return delegate().remove();
   }

   @Override
   public boolean removeAll(Collection<?> collection) {
      if (readOnly) {
         throw new UnsupportedOperationException();
      }
      return delegate().removeAll(collection);
   }

   @Override
   public boolean retainAll(Collection<?> collection) {
      if (readOnly) {
         throw new UnsupportedOperationException();
      }
      return delegate().retainAll(collection);
   }

   @Override
   public int size() {
      return delegate().size();
   }

   @Override
   public Object[] toArray() {
      return delegate().toArray();
   }

   @Override
   public <T> T[] toArray(T[] ts) {
      return delegate().toArray(ts);
   }
}//END OF DiskQueue
