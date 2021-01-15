package com.gengoai.function;

import com.gengoai.conversion.Cast;
import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * The type Switch.
 *
 * @param <T> the type parameter
 * @author David B. Bracewell
 */
public final class Switch<T> implements SerializableFunction<Object, T> {
   private static final long serialVersionUID = 1L;
   private final PredFunc[] functions;
   private final SerializableFunction<?, T> defaultFunction;

   private Switch(PredFunc[] functions, SerializableFunction<?, T> defaultFunction) {
      this.functions = functions;
      this.defaultFunction = defaultFunction;
   }

   @SuppressWarnings("unchecked")
   @Override
   public T apply(Object o) {
      for (PredFunc function : functions) {
         if (function.predicate.test(Cast.as(o))) {
            return Cast.as(function.function.apply(Cast.as(o)));
         }
      }
      if (defaultFunction != null) {
         return Cast.as(defaultFunction.apply(Cast.as(o)));
      }
      throw new IllegalArgumentException("Unmatched Element: " + o);
   }

   @Value
   private static class PredFunc<T, V> implements Serializable {
      private static final long serialVersionUID = 1L;
      private final SerializablePredicate<? super T> predicate;
      private final SerializableFunction<? super T, V> function;

      /**
       * Instantiates a new Pred func.
       *
       * @param predicate the predicate
       * @param function  the function
       */
      public PredFunc(SerializablePredicate<? super T> predicate, SerializableFunction<? super T, V> function) {
         this.predicate = predicate;
         this.function = function;
      }
   }

   /**
    * Switch switch.
    *
    * @param <V>      the type parameter
    * @param consumer the consumer
    * @return the switch
    */
   public static <V> Switch<V> $switch(@NonNull Consumer<SwitchBuilder<V>> consumer) {
      SwitchBuilder<V> switchBuilder = new SwitchBuilder<>();
      consumer.accept(switchBuilder);
      return switchBuilder.build();
   }

   /**
    * The type Switch builder.
    *
    * @param <V> the type parameter
    */
   public static class SwitchBuilder<V> {
      private final ArrayList<PredFunc> cases = new ArrayList<>();
      private SerializableFunction<?, V> defaultFunction;

      /**
       * Instance of.
       *
       * @param <T>   the type parameter
       * @param clazz the clazz
       * @param value the value
       */
      public <T> void instanceOf(@NonNull Class<? super T> clazz, V value) {
         instanceOf(clazz, c -> value);
      }

      /**
       * Instance of.
       *
       * @param <T>      the type parameter
       * @param clazz    the clazz
       * @param function the function
       */
      public <T> void instanceOf(@NonNull Class<? super T> clazz, @NonNull SerializableFunction<T, V> function) {
         cases.add(new PredFunc<>(o -> {
            if (o == null) {
               return false;
            } else if (o instanceof Class) {
               return clazz.isAssignableFrom(Cast.as(o));
            }
            return clazz.isInstance(o);
         }, function));
      }

      /**
       * Equals.
       *
       * @param <T>         the type parameter
       * @param targetValue the target value
       * @param returnValue the return value
       */
      public <T> void equals(@NonNull T targetValue, @NonNull V returnValue) {
         cases.add(new PredFunc<>(o -> Objects.equals(o, targetValue), c -> returnValue));
      }

      /**
       * Equals.
       *
       * @param <T>      the type parameter
       * @param value    the value
       * @param function the function
       */
      public <T> void equals(T value, @NonNull SerializableFunction<T, V> function) {
         cases.add(new PredFunc<>(o -> Objects.equals(o, value), function));
      }

      /**
       * Is null.
       *
       * @param <T>      the type parameter
       * @param function the function
       */
      public <T> void isNull(@NonNull SerializableFunction<T, V> function) {
         cases.add(new PredFunc<>(Objects::isNull, function));
      }

      /**
       * When.
       *
       * @param <T>       the type parameter
       * @param predicate the predicate
       * @param function  the function
       */
      public <T> void when(SerializablePredicate<? super T> predicate,
                           @NonNull SerializableFunction<T, V> function) {
         cases.add(new PredFunc<>(predicate, function));
      }

      /**
       * When.
       *
       * @param <T>       the type parameter
       * @param <R>       the type parameter
       * @param predicate the predicate
       * @param mapper    the mapper
       * @param function  the function
       */
      public <T, R> void when(SerializablePredicate<? super T> predicate,
                              @NonNull SerializableFunction<T, R> mapper,
                              @NonNull SerializableFunction<R, V> function) {
         cases.add(new PredFunc<>(predicate, mapper.andThen(function)));
      }

      /**
       * Default action.
       *
       * @param function the function
       */
      public void defaultAction(@NonNull SerializableFunction<?, V> function) {
         this.defaultFunction = function;
      }

      /**
       * Default value.
       *
       * @param value the value
       */
      public void defaultValue(V value) {
         this.defaultFunction = o -> value;
      }

      /**
       * Default null.
       */
      public void defaultNull() {
         this.defaultFunction = o -> null;
      }

      private Switch<V> build() {
         return new Switch<>(cases.toArray(new PredFunc[0]), defaultFunction);
      }

   }

}//END OF Switch
