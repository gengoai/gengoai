package com.gengoai.concurrent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>Thread safe double container</p>
 *
 * @author David B. Bracewell
 */
public class AtomicDouble extends Number {
   private static final long serialVersionUID = 1L;
   private final AtomicLong backing;

   /**
    * Instantiates a new Atomic double.
    */
   public AtomicDouble() {
      this(0d);
   }

   /**
    * Instantiates a new Atomic double.
    *
    * @param value the value
    */
   public AtomicDouble(double value) {
      this.backing = new AtomicLong(toLong(value));
   }

   /**
    * Atomically adds the given value to the current value.
    *
    * @param value the value
    * @return the updated value
    */
   public final double addAndGet(double value) {
      while (true) {
         long current = backing.get();
         double currentVal = toDouble(current);
         double nextVal = currentVal + value;
         long next = toLong(nextVal);
         if (backing.compareAndSet(current, next)) {
            return nextVal;
         }
      }
   }

   /**
    * Atomically sets the value to the given updated value if the current value {@code ==} the expected value.
    *
    * @param expect the expected value
    * @param update the new value
    * @return {@code true} if successful. False return indicates that the actual value was not equal to the expected
    * value.
    */
   public final boolean compareAndSet(double expect, double update) {
      return backing.compareAndSet(toLong(expect), toLong(update));
   }

   @Override
   public double doubleValue() {
      return get();
   }

   @Override
   public float floatValue() {
      return (float) get();
   }

   /**
    * Gets the double value
    *
    * @return the double
    */
   public double get() {
      return toDouble(backing.get());
   }

   /**
    * Atomically adds the given value to the current value.
    *
    * @param value the value
    * @return the previous value
    */
   public final double getAndAdd(double value) {
      while (true) {
         long current = backing.get();
         double currentVal = toDouble(current);
         double nextVal = currentVal + value;
         long next = toLong(nextVal);
         if (backing.compareAndSet(current, next)) {
            return currentVal;
         }
      }
   }

   /**
    * Atomically sets to the given value and returns the old value.
    *
    * @param value the value to set
    * @return the value before the update
    */
   public double getAndSet(double value) {
      return toDouble(backing.getAndSet(toLong(value)));
   }

   @Override
   public int intValue() {
      return (int) get();
   }

   @Override
   public long longValue() {
      return (long) get();
   }

   /**
    * Sets to the given value.
    *
    * @param value the value
    */
   public void set(double value) {
      backing.set(toLong(value));
   }

   private double toDouble(long l) {
      return Double.longBitsToDouble(l);
   }

   private long toLong(double d) {
      return Double.doubleToRawLongBits(d);
   }

   @Override
   public String toString() {
      return Double.toString(get());
   }

   /**
    * Atomically sets the value to the given updated value if the current value == the expected value.
    *
    * @param expect the expected value
    * @param update the updated value
    * @return True if updated, False if not
    */
   public final boolean weakCompareAndSet(double expect, double update) {
      return backing.weakCompareAndSetPlain(toLong(expect), toLong(update));
   }

}//END OF AtomicDouble
