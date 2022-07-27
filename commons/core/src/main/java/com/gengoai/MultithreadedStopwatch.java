package com.gengoai;

import com.gengoai.config.Config;
import com.gengoai.string.Strings;
import com.gengoai.tuple.Tuple2;
import lombok.NonNull;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.gengoai.tuple.Tuples.$;

/**
 * <p>A specialized version of a Stopwatch that is thread safe. Tracks start and ending times per thread to determine
 * total time taken across threads.</p>
 * <p>Normal usage of a Stopwatch is a follows:
 * <pre>
 * {@code
 *    var sw = new MultithreadedStopwatch("MyStopwatch");
 *    //Perform some activity
 *    new Thread(() -> {
 *       sw.start();
 *       // do something....
 *       sw.stop();
 *    }).start();
 *    new Thread(() -> {
 *       sw.start();
 *       // do something....
 *       sw.stop();
 *    }).start();
 *    new Thread(() -> {
 *       sw.start();
 *       // do something....
 *       sw.stop();
 *    }).start();
 *    //Wait until all threads are complete
 *    System.out.println(sw); // Output the sum of times spent in the three threads and the average time spent per start/stop.
 * }
 * </pre>
 *
 * @author David B. Bracewell
 */
public class MultithreadedStopwatch implements Serializable {
   private static final long serialVersionUID = 1L;
   private final String name;
   private Map<Long, Instant> runningStarts = new ConcurrentHashMap<>();
   private List<Tuple2<Instant, Instant>> durations = new CopyOnWriteArrayList<>();
   private Duration elapsedTime = Duration.ofNanos(0);
   private AtomicLong calls = new AtomicLong();

   /**
    * Instantiates a new Multithreaded stopwatch with the given name.
    *
    * @param name the name of the stopwatch
    */
   public MultithreadedStopwatch(String name) {
      this(name, determineLevel(name));
   }

   /**
    * Instantiates a new Multithreaded stopwatch with given name and logging at given level. The stopwatch will log the
    * current stopwatch status at the given log level on a fixed interval of every 30 seconds.
    *
    * @param name  the name of the stopwatch
    * @param level the log level
    */
   public MultithreadedStopwatch(String name, @NonNull Level level) {
      this.name = name;
      final Logger logger = Strings.isNotNullOrBlank(name)
            ? Logger.getLogger(name)
            : Logger.getGlobal();
      if (level != Level.OFF) {
         final Timer timer = new Timer("StopwatchTimer", true);
         timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
               if (calls.get() > 0 || runningStarts.size() > 0) {
                  logger.log(level, MultithreadedStopwatch.this.toString());
               }
            }
         }, 30000, 30000);
      }
   }

   private static Level determineLevel(String name) {
      String[] parts = name.split("\\.");
      for (int length = parts.length; length > 0; length--) {
         String key = "Stopwatch." + String.join(".", Arrays.copyOfRange(parts, 0, length)) + ".level";
         if (Config.hasProperty(key)) {
            return Config.get(key).as(Level.class);
         }
      }
      return Config.get("Stopwatch.level").as(Level.class, Level.OFF);
   }

   /**
    * Gets the elapsed time in given time units
    *
    * @param timeUnit the time unit
    * @return the elapsed time in the given time unit
    */
   public long elapsed(TemporalUnit timeUnit) {
      return getElapsedTime().get(timeUnit);
   }

   /**
    * Gets elapsed time in nano seconds
    *
    * @return the elapsed time in nano seconds
    */
   public synchronized Duration getElapsedTime() {
      final List<Tuple2<Instant, Instant>> all = new ArrayList<>(durations);
      final long numFinished = durations.size();
      if (runningStarts.size() > 0) {
         Instant currentTime = Instant.now();
         for (Instant value : runningStarts.values()) {
            all.add($(value, currentTime));
         }
      }
      all.sort(Map.Entry.comparingByKey());
      Duration duration = Duration.ofNanos(0);
      final List<Integer> toRemove = new ArrayList<>();
      for (int i = 0; i < all.size(); i++) {
         int startI = i;
         Tuple2<Instant, Instant> ii = all.get(i);
         Instant maxEnd = ii.v2;
         Instant lastEnd = ii.v2;
         while (i + 1 < all.size() &&
               (all.get(i + 1).v1.isBefore(lastEnd) || all.get(i + 1).v2.isBefore(maxEnd))
         ) {
            i++;
            Tuple2<Instant, Instant> ij = all.get(i);
            maxEnd = ij.v2.isAfter(maxEnd)
                  ? ij.v2
                  : maxEnd;
            lastEnd = ij.v2;
         }
         if (i < numFinished) {
            for (int k = startI; k <= i; k++) {
               toRemove.add(k);
            }
            elapsedTime = elapsedTime.plus(duration.plus(Duration.between(ii.v1, lastEnd)));
         } else {
            duration = duration.plus(Duration.between(ii.v1, lastEnd));
         }
      }
      for (int i = toRemove.size() - 1; i >= 0; i--) {
         durations.remove(toRemove.get(i).intValue());
      }
      return elapsedTime.plus(duration);
   }

   /**
    * Gets elapsed time as string.
    *
    * @return the elapsed time as string
    */
   public String getElapsedTimeAsString() {
      return getElapsedTime()
            .toString()
            .substring(2)
            .replaceAll("(\\d[HMS])(?!$)", "$1 ")
            .toLowerCase();
   }

   /**
    * Reset the stopwatch.
    */
   public void reset() {
      this.calls.set(0);
      this.runningStarts.clear();
      this.elapsedTime = Duration.ofNanos(0);
   }

   /**
    * Start the stopwatch.
    */
   public void start() {
      long threadId = Thread.currentThread().getId();
      if (runningStarts.containsKey(threadId)) {
         return;
      }
      calls.incrementAndGet();
      runningStarts.put(threadId, Instant.now());
   }

   /**
    * Stop the stopwatch.
    */
   public void stop() {
      long threadId = Thread.currentThread().getId();
      if (runningStarts.containsKey(threadId)) {
         durations.add($(runningStarts.remove(threadId), Instant.now()));
      }
   }

   @Override
   public String toString() {
      StringBuilder stringBuilder = new StringBuilder(name).append(": ").append(getElapsedTimeAsString());
      double seconds = getElapsedTime().get(ChronoUnit.SECONDS);
      double numCalls = calls.get();
      stringBuilder.append(" (")
                   .append(String.format("%.1f", numCalls / seconds))
                   .append(" ops/second)");
      return stringBuilder.toString();
   }

}//END OF Stopwatch
