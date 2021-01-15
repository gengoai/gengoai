package com.gengoai;

import com.gengoai.string.Strings;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.Serializable;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static com.gengoai.Validation.checkState;
import static com.gengoai.Validation.notNull;

/**
 * <p>Tracks start and ending times to determine total time taken.</p>
 * <p>Normal usage of a Stopwatch is a follows:
 * <pre>
 * {@code
 *    var sw = Stopwatch.createStarted() //optionally you can give your Stopwatch a name
 *    //Perform some activity
 *    sw.stop();
 *    System.out.println(sw); // output total time
 *    sw.reset();
 *    sw.start();
 *    //Perform another activity
 *    sw.stop();
 *    System.out.println(sw); // output total time
 * }
 * </pre>
 * </p>
 * <p> In cases where you do not want the stopwatch to start on creation you can use {@link #createStopped()}.
 * Additionally, you can use the Stopwatch as a resource to automatically log the timing on close as follows:</p>
 * <pre>
 * {@code
 *    //By default the log level is set to OFF and the Logger is the global logger
 *    //We change that by:
 *    try( var sw = Stopwatch.createStarted(MyLogger, Level.INFO) ){
 *       //Perform some activity
 *    }
 * }
 * </pre>
 *
 * @author David B. Bracewell
 */
public class Stopwatch implements Serializable, AutoCloseable {
   private static final long serialVersionUID = 1L;
   private final String name;
   private long start = -1L;
   private long elapsedTime = 0L;
   private boolean isRunning = false;
   @Getter
   @Setter
   @NonNull
   private Logger logger;
   @Getter
   @Setter
   @NonNull
   private Level logLevel;


   private Stopwatch(boolean started, String name) {
      this(started, name, LogUtils.getGlobalLogger(), Level.OFF);
   }

   private Stopwatch(boolean started, String name, Logger logger, Level level) {
      this.name = name;
      if (started) {
         start();
      }
      this.logger = logger;
      this.logLevel = level;
   }


   /**
    * Calculates the time to execute the given runnable
    *
    * @param runnable the runnable to time
    * @return the stopwatch in a stopped state
    */
   public static Stopwatch timeIt(Runnable runnable) {
      Stopwatch toReturn = createStarted();
      runnable.run();
      toReturn.stop();
      return toReturn;
   }

   /**
    * Calculates the time to execute the given runnable nTrial times
    *
    * @param nTrials  the nunber of times to execute the runnable
    * @param runnable the runnable to time
    * @return the stopwatch in a stopped state
    */
   public static Stopwatch timeIt(int nTrials, Runnable runnable) {
      Stopwatch toReturn = createStarted();
      IntStream.range(0, nTrials).forEach(i -> runnable.run());
      toReturn.stop();
      return toReturn.averageTime(nTrials);
   }

   /**
    * Create a named stopwatch that is started.
    *
    * @param name the name of the stopwatch for reporting purposes
    * @return the stopwatch
    */
   public static Stopwatch createStarted(String name) {
      return new Stopwatch(true, name);
   }

   /**
    * Create a stopwatch that is started.
    *
    * @param name the name of the stopwatch for reporting purposes
    * @return the stopwatch
    */
   public static Stopwatch createStarted(String name, Logger logger, Level level) {
      return new Stopwatch(true, name, logger, level);
   }

   /**
    * Create a stopwatch that is started.
    *
    * @return the stopwatch
    */
   public static Stopwatch createStarted() {
      return new Stopwatch(true, null);
   }

   /**
    * Create a stopwatch that is started.
    *
    * @return the stopwatch
    */
   public static Stopwatch createStarted(Logger logger, Level level) {
      return new Stopwatch(true, null, logger, level);
   }

   /**
    * Create a stopwatch that is stopped.
    *
    * @param name the name of the stopwatch for reporting purposes
    * @return the stopwatch
    */
   public static Stopwatch createStopped(String name) {
      return new Stopwatch(false, name);
   }

   /**
    * Create a stopwatch that is stopped.
    *
    * @return the stopwatch
    */
   public static Stopwatch createStopped() {
      return new Stopwatch(false, null);
   }


   /**
    * Sets the elapsed time of the stopwatch to <code>elapsed time / trials</code>
    *
    * @param trials the number of trials to use to average the time
    * @return this stopwatch
    */
   public Stopwatch averageTime(long trials) {
      checkState(!isRunning, "Can only average when stopped");
      elapsedTime = (long) Math.floor((double) elapsedTime / trials);
      return this;
   }

   @Override
   public void close() {
      logger.log(logLevel, this.toString());
   }

   /**
    * Gets the elapsed time in given time units
    *
    * @param timeUnit the time unit
    * @return the elapsed time in the given time unit
    */
   public long elapsed(TimeUnit timeUnit) {
      return notNull(timeUnit).convert(getElapsedTime(), TimeUnit.NANOSECONDS);
   }

   /**
    * Gets elapsed time in nano seconds
    *
    * @return the elapsed time in nano seconds
    */
   public long getElapsedTime() {
      return isRunning
            ? elapsedTime + (TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()) - this.start)
            : elapsedTime;
   }

   private long getSystemNano() {
      return TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
   }

   /**
    * Reset the stopwatch.
    */
   public void reset() {
      this.isRunning = false;
      this.start = -1;
      this.elapsedTime = 0L;
   }

   public void resetAndStart() {
      this.isRunning = false;
      reset();
      start();
   }

   /**
    * Start the stopwatch.
    */
   public void start() {
      checkState(!isRunning, "Cannot start an already started Stopwatch");
      this.isRunning = true;
      this.start = getSystemNano();
   }

   /**
    * Stop the stopwatch.
    */
   public void stop() {
      checkState(isRunning, "Cannot stop an already stopped Stopwatch");
      this.isRunning = false;
      elapsedTime += (getSystemNano() - this.start);
   }

   @Override
   public String toString() {
      StringBuilder stringBuilder = new StringBuilder();
      if (Strings.isNotNullOrBlank(name)) {
         stringBuilder.append(name).append(": ");
      }
      stringBuilder.append(Duration.ofNanos(getElapsedTime()).toString()
                                   .substring(2)
                                   .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                                   .toLowerCase());
      return stringBuilder.toString();
   }
}//END OF Stopwatch
