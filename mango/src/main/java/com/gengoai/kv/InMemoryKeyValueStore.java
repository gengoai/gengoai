package com.gengoai.kv;

import java.util.Collections;
import java.util.Map;

/**
 * @author David B. Bracewell
 */
public class InMemoryKeyValueStore<K, V> extends AbstractKeyValueStore<K, V> {
   private static final long serialVersionUID = 1L;
   private transient volatile Map<K, V> map;

   /**
    * Instantiates a new Abstract navigable key value store.
    *
    * @param namespace the namespace
    * @param readOnly  the read only
    */
   public InMemoryKeyValueStore(String namespace, boolean readOnly) {
      super(namespace, readOnly);
      delegate();
   }

   @Override
   public void close() throws Exception {
      map = null;
   }

   @Override
   public void commit() {

   }

   @Override
   protected Map<K, V> delegate() {
      if(map == null) {
         synchronized(this) {
            if(map == null) {
               map = MapRegistry.get(getNameSpace());
               if(isReadOnly()) {
                  map = Collections.unmodifiableMap(map);
               }
            }
         }
      }
      return map;
   }

}//END OF InMemoryKeyValueStore
