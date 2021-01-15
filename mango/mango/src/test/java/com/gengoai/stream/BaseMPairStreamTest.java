package com.gengoai.stream;

import com.gengoai.config.Config;
import com.gengoai.stream.local.LocalStreamingContext;
import com.gengoai.tuple.Tuple2;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.gengoai.collection.Maps.hashMapOf;
import static com.gengoai.collection.Maps.sortedMapOf;
import static com.gengoai.tuple.Tuples.$;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public abstract class BaseMPairStreamTest {
   StreamingContext sc;

   @Test
   public void filter() throws Exception {
      Map.Entry<String, String> g = sc.pairStream(Arrays.asList($("C", "D"),
                                                                $("A", "B")))
                                      .filter((k, v) -> k.equals("A"))
                                      .collectAsList().get(0);
      assertEquals("A", g.getKey());
      assertEquals("B", g.getValue());

      g = sc.pairStream(hashMapOf($("C", "D"), $("A", null)))
            .filter((k, v) -> k != null && v != null)
            .collectAsList().get(0);
      assertEquals("C", g.getKey());
      assertEquals("D", g.getValue());
   }

   @Test
   public void filterByKey() throws Exception {
      Map.Entry<String, String> g = sc.pairStream(hashMapOf($("C", "D"),
                                                            $("A", "B")))
                                      .filterByKey(s -> s.equals("A"))
                                      .collectAsList().get(0);
      assertEquals("A", g.getKey());
      assertEquals("B", g.getValue());
   }

   @Test
   public void filterByValue() throws Exception {
      Map.Entry<String, String> g = sc.pairStream(hashMapOf($("C", "D"),
                                                            $("A", "B")))
                                      .filterByValue(s -> s.equals("B"))
                                      .collectAsList().get(0);
      assertEquals("A", g.getKey());
      assertEquals("B", g.getValue());
   }


   @Test
   public void join() throws Exception {
      MPairStream<String, Integer> s1 = sc.pairStream(hashMapOf($("A", 1), $("B", 2), $("C", 3)));
      MPairStream<String, Integer> s2 = sc.pairStream(hashMapOf($("A", 4), $("B", 1)));
      Map<String, Map.Entry<Integer, Integer>> joined = s1.join(s2).collectAsMap();
      assertEquals(1, joined.get("A").getKey().intValue());
      assertEquals(4, joined.get("A").getValue().intValue());
      assertEquals(2, joined.get("B").getKey().intValue());
      assertEquals(1, joined.get("B").getValue().intValue());
      assertFalse(joined.containsKey("C"));
   }

   @Test
   public void leftOuterJoin() throws Exception {
      MPairStream<String, Integer> s1 = sc.pairStream(hashMapOf($("A", 1), $("B", 2), $("C", 3)));
      MPairStream<String, Integer> s2 = sc.pairStream(hashMapOf($("A", 4), $("B", 1)));
      Map<String, Map.Entry<Integer, Integer>> joined = s1.leftOuterJoin(s2).collectAsMap();

      assertEquals(1, joined.get("A").getKey().intValue());
      assertEquals(4, joined.get("A").getValue().intValue());
      assertEquals(2, joined.get("B").getKey().intValue());
      assertEquals(1, joined.get("B").getValue().intValue());
      assertEquals(3, joined.get("C").getKey().intValue());
      assertNull(joined.get("C").getValue());
   }

   @Test
   public void rightOuterJoin() throws Exception {
      MPairStream<String, Integer> s1 = sc.pairStream(hashMapOf($("A", 1),
                                                                $("B", 2),
                                                                $("C", 3)));
      MPairStream<String, Integer> s2 = sc.pairStream(hashMapOf($("A", 4),
                                                                $("B", 1),
                                                                $("D", 4)));
      Map<String, Map.Entry<Integer, Integer>> joined = s1.rightOuterJoin(s2).collectAsMap();

      assertEquals(1, joined.get("A").getKey().intValue());
      assertEquals(4, joined.get("A").getValue().intValue());

      assertEquals(2, joined.get("B").getKey().intValue());
      assertEquals(1, joined.get("B").getValue().intValue());

      assertNull(joined.get("D").getKey());
      assertEquals(4, joined.get("D").getValue().intValue());

      assertFalse(joined.containsKey("C"));
   }

   @Test
   public void reduceByKey() throws Exception {
      Map<String, Integer> r = sc.stream(
         Arrays.asList(
            Tuple2.of("A", 1),
            Tuple2.of("B", 2),
            Tuple2.of("A", 4),
            Tuple2.of("C", 1),
            Tuple2.of("B", 10)
                      )
                                        )
                                 .mapToPair(t -> t)
                                 .reduceByKey((v1, v2) -> v1 + v2)
                                 .collectAsMap();
      assertEquals(5, r.get("A").intValue());
      assertEquals(12, r.get("B").intValue());
      assertEquals(1, r.get("C").intValue());
   }


   @Test
   public void streamOps() throws Exception {
      MPairStream<String, Integer> stream = sc.pairStream(hashMapOf($("A", 1),
                                                                    $("B", 2),
                                                                    $("C", 3)));

      assertEquals(sc, stream.getContext());

      AtomicBoolean closed = new AtomicBoolean(false);
      stream.cache();
      stream.repartition(10);
      stream = stream.onClose(() -> {
         closed.set(true);
      });
      stream.close();

      assertTrue(closed.get());
   }

   @Test
   public void shuffle() throws Exception {
      Map<String, Integer> orig = sortedMapOf($("A", 1),
                                              $("B", 2),
                                              $("C", 3));
      List<Map.Entry<String, Integer>> origS = sc.pairStream(orig).collectAsList();
      boolean diff = false;
      for (int i = 0; !diff && i < 10; i++) {
         if (!origS.equals(new ArrayList<>(sc.pairStream(orig).shuffle().collectAsList()))) {
            diff = true;
         }
      }
      assertTrue(diff);
   }


   @Test
   public void count() throws Exception {
      assertEquals(3, sc.pairStream(hashMapOf($("A", 1),
                                              $("B", 2),
                                              $("C", 3))).count());
      assertEquals(0, sc.emptyPair().count());
   }

   @Test
   public void keyTests() throws Exception {
      MPairStream<String, Integer> s = sc.pairStream(hashMapOf($("A", 1),
                                                               $("B", 2),
                                                               $("C", 3)));
      assertEquals(
         Arrays.asList("A", "B", "C"),
         s.keys().sorted(true).collect()
                  );

      List<Map.Entry<String, Integer>> l = sc.pairStream(hashMapOf($("A", 1),
                                                                   $("B", 2),
                                                                   $("C", 3)))
                                             .sortByKey(true)
                                             .collectAsList();
      assertEquals("A", l.get(0).getKey());
      assertEquals("B", l.get(1).getKey());
      assertEquals("C", l.get(2).getKey());


      l = sc.pairStream(hashMapOf($("A", 1),
                                  $("B", 2),
                                  $("C", 3)))
            .sortByKey((s1, s2) -> -s1.compareTo(s2)).collectAsList();
      assertEquals("A", l.get(2).getKey());
      assertEquals("B", l.get(1).getKey());
      assertEquals("C", l.get(0).getKey());
   }


   @Test
   public void union() throws Exception {
      MPairStream<String, Integer> s1 = sc.pairStream(hashMapOf($("A", 1),
                                                                $("B", 2),
                                                                $("C", 3)));
      MPairStream<String, Integer> s2 = sc.pairStream(hashMapOf($("A", 3),
                                                                $("B", 1),
                                                                $("D", 3)));

      Map<String, Integer> r = s1.union(s2).reduceByKey((v1, v2) -> v1 + v2).collectAsMap();
      assertEquals(4, r.get("A").intValue());
      assertEquals(3, r.get("B").intValue());
      assertEquals(3, r.get("C").intValue());
      assertEquals(3, r.get("D").intValue());

      s1 = sc.pairStream(hashMapOf($("A", 1),
                                   $("B", 2),
                                   $("C", 3)));
      StreamingContext other;
      if (sc instanceof LocalStreamingContext) {
         Config.setProperty("spark.master", "local[*]");
         other = StreamingContext.distributed();
      } else {
         other = StreamingContext.local();
      }
      s2 = other.pairStream(hashMapOf($("A", 3),
                                      $("B", 1),
                                      $("D", 3)));
      r = s1.union(s2).reduceByKey((v1, v2) -> v1 + v2).collectAsMap();
      assertEquals(4, r.get("A").intValue());
      assertEquals(3, r.get("B").intValue());
      assertEquals(3, r.get("C").intValue());
      assertEquals(3, r.get("D").intValue());
   }


   @Test
   public void forEach() throws Exception {
//    final MAccumulator<MultiCounter<String, String>> acc = sc.multiCounterAccumulator();
//
//    sc.pairStream(
//      $("A", "A"),
//      $("A", "B"),
//      $("A", "C")
//    )
//      .forEach((k, v) -> acc.add(new HashMapMultiCounter<>(Tuple3.of(k, v, 1.0))));
//
//    assertEquals(1, acc.value().get("A", "A"), 0.0);
//    assertEquals(1, acc.value().get("A", "B"), 0.0);
//    assertEquals(1, acc.value().get("A", "C"), 0.0);
//
//
//    acc.setValue(new HashMapMultiCounter<>());
//    sc.pairStream(
//      $("A", "A"),
//      $("A", "B"),
//      $("A", "C")
//    )
//      .forEachLocal((k, v) -> acc.add(new HashMapMultiCounter<>(Tuple3.of(k, v, 1.0))));
//
//    assertEquals(1, acc.value().get("A", "A"), 0.0);
//    assertEquals(1, acc.value().get("A", "B"), 0.0);
//    assertEquals(1, acc.value().get("A", "C"), 0.0);
   }

   @Test
   public void map() throws Exception {
      List<String> r = sc.pairStream(
         $("A", "A"),
         $("A", "B"),
         $("A", "C")
                                    )
                         .map((k, v) -> k + "," + v)
                         .collect();

      assertEquals("A,A", r.get(0));
      assertEquals("A,B", r.get(1));
      assertEquals("A,C", r.get(2));
   }
}// END OF BaseMPairStreamTest

