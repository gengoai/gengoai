package com.gengoai.collection;

import com.gengoai.io.resource.Resource;
import com.gengoai.io.resource.StringResource;
import com.gengoai.stream.Streams;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gengoai.tuple.Tuples.$;
import static org.junit.Assert.assertEquals;

/**
 * @author David B. Bracewell
 */
public class StreamsTest {


   @Test
   public void inputStream() throws Exception {
      Resource r = new StringResource("This is a test");
      InputStream is = r.inputStream();
      try (Stream<String> stream = Streams.asStream(is)) {
         assertEquals("This is a test", stream.collect(Collectors.joining()));
      }
      try {
         is.read();
      } catch (Exception e) {
         assertEquals("Stream closed", e.getMessage());
      }
   }

   @Test
   public void reader() throws Exception {
      Resource r = new StringResource("This is a test");
      Reader reader = r.reader();
      try (Stream<String> stream = Streams.asStream(reader)) {
         assertEquals("This is a test", stream.collect(Collectors.joining()));
      }
      try {
         int i = reader.read();
      } catch (Exception e) {
         assertEquals("Stream closed", e.getMessage());
      }
   }

   @Test
   public void iterator() throws Exception {
      assertEquals("A, B, C",
                   Streams.asStream(Arrays.asList("A", "B", "C").iterator()).collect(Collectors.joining(", "))
                  );
      assertEquals("A, B, C",
                   Streams.asParallelStream(Arrays.asList("A", "B", "C").iterator()).collect(Collectors.joining(", "))
                  );

   }

   @Test
   public void iterable() throws Exception {
      assertEquals("A, B, C",
                   Streams.asStream(Arrays.asList("A", "B", "C")).collect(Collectors.joining(", "))
                  );
      assertEquals("A, B, C",
                   Streams.asParallelStream(Arrays.asList("A", "B", "C")).collect(Collectors.joining(", "))
                  );
   }


   @Test
   public void zip() throws Exception {
      Assert.assertEquals(Lists.arrayListOf($("A", 1),
                                            $("B", 2),
                                            $("C", 3)
                                           ),
                          Streams.zip(Stream.of("A", "B", "C"), Stream.of(1, 2, 3, 4)).collect(Collectors.toList())
                         );
      assertEquals(0L, Streams.zip(Stream.empty(), Stream.of(1, 2, 3, 4)).count());
   }

   @Test
   public void zipWithIndex() throws Exception {
      Assert.assertEquals(Lists.arrayListOf($("A", 0L),
                                            $("B", 1L),
                                            $("C", 2L)
                                           ),
                          Streams.zipWithIndex(Stream.of("A", "B", "C")).collect(Collectors.toList())
                         );
      assertEquals(0L, Streams.zipWithIndex(Stream.empty()).count());
   }
}