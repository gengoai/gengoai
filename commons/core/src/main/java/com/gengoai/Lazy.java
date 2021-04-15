package com.gengoai;

import com.gengoai.function.SerializableSupplier;
import lombok.NonNull;

/**
 * <p>Lazily create a value in a thread safe manner. Common usage is as follows:</p>
 * <pre>
 * {@code
 *    //Declare a lazy initialized object.
 *    Lazy<ExpensiveObject> lazy = new Lazy(() -> createExpensiveObject());
 *
 *    //Now we will actually create the object.
 *    lazy.get().operation();
 *
 *    //Successive calls will use the already created object.
 *    lazy.get().operation_2();
 * }
 * </pre>
 *
 * @param <T> the type parameter
 * @author David B. Bracewell
 */
public final class Lazy<T> implements SerializableSupplier<T> {
   private static final long serialVersionUID = 1L;
   private final SerializableSupplier<? extends T> supplier;
   private volatile transient T object;

   /**
    * Instantiates a new Lazy created object.
    *
    * @param supplier the supplier used to create the object
    */
   public Lazy(@NonNull SerializableSupplier<? extends T> supplier) {
      this.supplier = supplier;
   }

   @Override
   public T get() {
      T value = object;
      if (value == null) {
         synchronized (this) {
            value = object;
            if (object == null) {
               object = value = supplier.get();
            }
         }
      }
      return value;
   }

}// END OF Lazy
