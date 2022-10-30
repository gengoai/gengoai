package com.gengoai;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gengoai.collection.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * The type NamesEnum.
 */
@JsonDeserialize(as = NamesEnum.class)
public final class NamesEnum extends EnumValue<NamesEnum> {
   private static final long serialVersionUID = 1L;
   private static final Set<NamesEnum> values = Sets.newConcurrentHashSet();
   private static final Registry<NamesEnum> registry = new Registry<>(NamesEnum::new, NamesEnum.class);

   /**
    * <p>Creates a new or retrieves an existing instance of NamesEnum with the given name.</p>
    *
    * @return The instance of NamesEnum corresponding th the give name.
    */
   @JsonCreator
   public static NamesEnum make(@JsonProperty String name) {
      NamesEnum toReturn = registry.make(name);
      values.add(toReturn);
      return toReturn;
   }

   /**
    * <p>Retrieves all currently known values of NamesEnum.</p>
    *
    * @return An unmodifiable collection of currently known values for NamesEnum.
    */
   public static Collection<NamesEnum> values() {
      return Collections.unmodifiableSet(values);
   }

   private NamesEnum(String name) {
      super(name);
   }

   @Override
   protected Registry<NamesEnum> registry() {
      return registry;
   }

}//END OF NamesEnum
