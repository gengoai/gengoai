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
 *
 */

package com.gengoai.io;

import com.gengoai.conversion.Cast;
import com.gengoai.stream.MStream;
import lombok.NonNull;

import java.io.*;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * <p>
 * A common pitfall in Java is not properly closing resources. This can become especially tricky when dealing with
 * concurrency and the new Java stream framework. Mango provides a `ResourceMonitor` which tracks `MonitoredObjects` and
 * automatically closes (frees) them when they are no longer referenced. The `ResourceMonitor` is basically a garbage
 * collector for resources!
 * </p>
 *
 * @author David B. Bracewell
 */
public class ResourceMonitor extends Thread {
   protected static final ResourceMonitor MONITOR = new ResourceMonitor();

   static {
      MONITOR.start();
   }

   private final ConcurrentHashMap<Object, KeyedWeakReference> map = new ConcurrentHashMap<>();
   private final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();

   private ResourceMonitor() {
      setPriority(Thread.MAX_PRIORITY);
      setName("GarbageCollectingConcurrentMap-cleanupthread");
      setDaemon(true);
   }

   /**
    * Monitors the given SQL Connection closing it when it is no longer referenced. Note that connection is only closed
    * and not committed.
    *
    * @param connection the connection
    * @return the connection
    */
   public static Connection monitor(@NonNull Connection connection) {
      return Cast.as(Proxy.newProxyInstance(connection.getClass().getClassLoader(),
            new Class[]{Connection.class},
            new ConnectionInvocationHandler(connection)));
   }

   /**
    * Monitors the given InputStream.
    *
    * @param stream the stream
    * @return the input stream
    */
   public static InputStream monitor(@NonNull InputStream stream) {
      return new MonitoredInputStream(stream);
   }

   /**
    * Monitors the given OutputStream.
    *
    * @param stream the stream
    * @return the output stream
    */
   public static OutputStream monitor(@NonNull OutputStream stream) {
      return new MonitoredOutputStream(stream);
   }

   /**
    * Monitors the given Reader.
    *
    * @param reader the reader
    * @return the reader
    */
   public static Reader monitor(@NonNull Reader reader) {
      return new MonitoredReader(reader);
   }

   /**
    * Monitors the given Writer.
    *
    * @param writer the writer
    * @return the writer
    */
   public static Writer monitor(@NonNull Writer writer) {
      return new MonitoredWriter(writer);
   }

   /**
    * Monitors the given Stream.
    *
    * @param <T>    the type parameter
    * @param stream the stream
    * @return the stream
    */
   public static <T> Stream<T> monitor(@NonNull Stream<T> stream) {
      return Cast.as(Proxy.newProxyInstance(Stream.class.getClassLoader(),
            new Class[]{Stream.class},
            new StreamInvocationHandler<>(stream)));
   }

   /**
    * Monitors the given MStream
    *
    * @param <T>    the type parameter
    * @param stream the stream
    * @return the m stream
    */
   public static <T> MStream<T> monitor(@NonNull MStream<T> stream) {
      return Cast.as(Proxy.newProxyInstance(MStream.class.getClassLoader(),
            new Class[]{MStream.class},
            new MStreamInvocationHandler<>(stream)));
   }

   /**
    * Monitors the given DoubleStream
    *
    * @param stream the stream
    * @return the double stream
    */
   public static DoubleStream monitor(@NonNull DoubleStream stream) {
      return Cast.as(Proxy.newProxyInstance(DoubleStream.class.getClassLoader(),
            new Class[]{DoubleStream.class},
            new DoubleStreamInvocationHandler(stream)));
   }

   /**
    * Monitors the given IntStream
    *
    * @param stream the stream
    * @return the int stream
    */
   public static IntStream monitor(@NonNull IntStream stream) {
      return Cast.as(Proxy.newProxyInstance(IntStream.class.getClassLoader(),
            new Class[]{IntStream.class},
            new IntStreamInvocationHandler(stream)));
   }

   /**
    * Monitors the given LongStream
    *
    * @param stream the stream
    * @return the long stream
    */
   public static LongStream monitor(@NonNull LongStream stream) {
      return Cast.as(Proxy.newProxyInstance(DoubleStream.class.getClassLoader(),
            new Class[]{LongStream.class},
            new LongStreamInvocationHandler(stream)));
   }

   /**
    * Monitors the given Object
    *
    * @param <T>    the type parameter
    * @param object the object
    * @return the monitored object
    */
   public static <T> MonitoredObject<T> monitor(@NonNull T object) {
      return new MonitoredObject<>(object);
   }

   /**
    * Monitors the given Object using the given onClose command
    *
    * @param <T>     the type parameter
    * @param object  the object
    * @param onClose the on close
    * @return the monitored object
    */
   public static <T> MonitoredObject<T> monitor(@NonNull T object, @NonNull Consumer<T> onClose) {
      return new MonitoredObject<>(object, onClose);
   }

   protected  <T> T addResource(@NonNull final Object referent, @NonNull T resource) {
      KeyedObject<T> monitoredResource = KeyedObject.create(resource);
      map.put(monitoredResource.key, new KeyedWeakReference(referent, monitoredResource));
      return resource;
   }

   protected <T> T addResource(final Object referent, T resource, Consumer<T> onClose) {
      KeyedObject<T> monitoredResource = KeyedObject.create(resource, onClose);
      map.put(monitoredResource.key, new KeyedWeakReference(referent, monitoredResource));
      return resource;
   }

   @Override
   public void run() {
      try {
         while (true) {
            KeyedWeakReference ref = (KeyedWeakReference) referenceQueue.remove();
            try {
               map.remove(ref.monitoredResource.key);
               ref.monitoredResource.close();
            } catch (Exception e) {
               if (!e.getMessage().toLowerCase().contains("already closed")) {
                  e.printStackTrace();
               }
            }
         }
      } catch (InterruptedException e) {
         //
      }
   }

   private static class ConnectionInvocationHandler implements InvocationHandler {
      /**
       * The Backing.
       */
      final Connection backing;

      private ConnectionInvocationHandler(Connection backing) {
         this.backing = MONITOR.addResource(this, backing);
      }

      @Override
      public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
         return method.invoke(backing, objects);
      }
   }

   private static class MStreamInvocationHandler<T> implements InvocationHandler {
      /**
       * The Backing.
       */
      final MStream<T> backing;

      private MStreamInvocationHandler(MStream<T> backing) {
         this.backing = MONITOR.addResource(this, backing);
      }

      @Override
      public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
         return method.invoke(backing, objects);
      }
   }

   private static class MonitoredInputStream extends InputStream {
      /**
       * The Backing.
       */
      final InputStream backing;

      private MonitoredInputStream(InputStream backing) {
         this.backing = MONITOR.addResource(this, backing);
      }

      @Override
      public void close() throws IOException {
         backing.close();
      }

      @Override
      public int read() throws IOException {
         return backing.read();
      }
   }

   private static class MonitoredOutputStream extends OutputStream {
      /**
       * The Backing.
       */
      final OutputStream backing;

      private MonitoredOutputStream(OutputStream backing) {
         this.backing = MONITOR.addResource(this, backing);
      }

      @Override
      public void close() throws IOException {
         backing.close();
      }

      @Override
      public void write(int i) throws IOException {
         backing.write(i);
      }
   }

   private static class MonitoredReader extends Reader {
      /**
       * The Backing.
       */
      final Reader backing;

      private MonitoredReader(Reader backing) {
         this.backing = MONITOR.addResource(this, backing);
         ;
      }

      @Override
      public void close() throws IOException {
         backing.close();
      }

      @Override
      public int read(char[] chars, int i, int i1) throws IOException {
         return backing.read(chars, i, i1);
      }
   }

   private static class MonitoredWriter extends Writer {
      /**
       * The Backing.
       */
      final Writer backing;

      private MonitoredWriter(Writer backing) {
         this.backing = MONITOR.addResource(this, backing);
      }

      @Override
      public void close() throws IOException {
         backing.close();
      }

      @Override
      public void flush() throws IOException {
         backing.flush();
      }

      @Override
      public void write(char[] chars, int i, int i1) throws IOException {
         backing.write(chars, i, i1);
      }
   }

   private static class StreamInvocationHandler<T> implements InvocationHandler {
      /**
       * The Backing stream.
       */
      final Stream<T> backingStream;

      private StreamInvocationHandler(Stream<T> backingStream) {
         this.backingStream = MONITOR.addResource(this, backingStream);
      }

      @Override
      public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
         return method.invoke(backingStream, objects);
      }
   }

   private static class DoubleStreamInvocationHandler implements InvocationHandler {
      /**
       * The Backing stream.
       */
      final DoubleStream backingStream;

      private DoubleStreamInvocationHandler(DoubleStream backingStream) {
         this.backingStream = MONITOR.addResource(this, backingStream);
      }

      @Override
      public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
         return method.invoke(backingStream, objects);
      }
   }

   private static class LongStreamInvocationHandler implements InvocationHandler {
      /**
       * The Backing stream.
       */
      final LongStream backingStream;

      private LongStreamInvocationHandler(LongStream backingStream) {
         this.backingStream = MONITOR.addResource(this, backingStream);
      }

      @Override
      public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
         return method.invoke(backingStream, objects);
      }
   }

   private static class IntStreamInvocationHandler implements InvocationHandler {
      /**
       * The Backing stream.
       */
      final IntStream backingStream;

      private IntStreamInvocationHandler(IntStream backingStream) {
         this.backingStream = MONITOR.addResource(this, backingStream);
      }

      @Override
      public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
         return method.invoke(backingStream, objects);
      }
   }

   private class KeyedWeakReference extends WeakReference<Object> {
      /**
       * The Monitored resource.
       */
      public final KeyedObject<?> monitoredResource;

      /**
       * Instantiates a new Keyed weak reference.
       *
       * @param referenceObject   the reference object
       * @param monitoredResource the monitored resource
       */
      public KeyedWeakReference(Object referenceObject, KeyedObject<?> monitoredResource) {
         super(referenceObject, referenceQueue);
         this.monitoredResource = monitoredResource;
      }
   }

}//END OF ReferenceMonitor
