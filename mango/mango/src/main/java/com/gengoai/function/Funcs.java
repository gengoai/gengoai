package com.gengoai.function;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.gengoai.Validation.notNull;

/**
 * The type Funcs.
 *
 * @author David B. Bracewell
 */
public final class Funcs {
   private Funcs() {
      throw new IllegalAccessError();
   }


   public static <T, R> Function<T, R> literal(R returnValue) {
      return t -> returnValue;
   }

   public static <T> Predicate<T> instanceOf(Class<?> clazz) {
      notNull(clazz);
      return clazz::isInstance;
   }

   /**
    * As consumer consumer.
    *
    * @param <T>      the type parameter
    * @param function the function
    * @return the consumer
    */
   public static <T> Consumer<T> asConsumer(Function<? super T, ?> function) {
      notNull(function);
      return function::apply;
   }

   /**
    * As function function.
    *
    * @param <T>         the type parameter
    * @param <R>         the type parameter
    * @param consumer    the consumer
    * @param returnValue the return value
    * @return the function
    */
   public static <T, R> Function<T, R> asFunction(Consumer<? super T> consumer, R returnValue) {
      notNull(consumer);
      return t -> {
         consumer.accept(t);
         return returnValue;
      };
   }

   /**
    * As function function.
    *
    * @param <T>      the type parameter
    * @param <R>      the type parameter
    * @param consumer the consumer
    * @return the function
    */
   public static <T, R> Function<T, R> asFunction(Consumer<? super T> consumer) {
      return asFunction(consumer, null);
   }

   /**
    * When when.
    *
    * @param <I>        the type parameter
    * @param <O>        the type parameter
    * @param predicate  the predicate
    * @param trueAction the true action
    * @return the when
    */
   public static <I, O> When<I, O> when(SerializablePredicate<? super I> predicate, SerializableFunction<? super I, ? extends O> trueAction) {
      return new When<>(notNull(predicate), notNull(trueAction));
   }

   /**
    * When when.
    *
    * @param <I>       the type parameter
    * @param <O>       the type parameter
    * @param predicate the predicate
    * @param trueValue the true value
    * @return the when
    */
   public static <I, O> When<I, O> when(SerializablePredicate<? super I> predicate, O trueValue) {
      return new When<>(notNull(predicate), i -> trueValue);
   }


   /**
    * The type When.
    *
    * @param <I> the type parameter
    * @param <O> the type parameter
    */
   public static class When<I, O> implements SerializableFunction<I, O> {
      private final SerializablePredicate<? super I> predicate;
      private final SerializableFunction<? super I, ? extends O> trueAction;
      private SerializableFunction<? super I, ? extends O> falseAction = i -> null;


      private When(SerializablePredicate<? super I> predicate, SerializableFunction<? super I, ? extends O> trueAction) {
         this.predicate = predicate;
         this.trueAction = trueAction;
      }

      @Override
      public O apply(I i) {
         return predicate.test(i) ? trueAction.apply(i) : falseAction.apply(i);
      }

      /**
       * Otherwise function.
       *
       * @param falseAction the false action
       * @return the function
       */
      public SerializableFunction<I, O> otherwise(SerializableFunction<? super I, ? extends O> falseAction) {
         this.falseAction = notNull(falseAction);
         return this;
      }

      /**
       * Otherwise function.
       *
       * @param falseValue the false value
       * @return the function
       */
      public SerializableFunction<I, O> otherwise(O falseValue) {
         this.falseAction = i -> falseValue;
         return this;
      }
   }

}//END OF Funcs
