/**
 * The type ${TEMPLATE}.
 */
public class ${TEMPLATE} extends EnumValue<${TEMPLATE}> {
   private static final long serialVersionUID = 1L;
   private static final Registry<${TEMPLATE}> registry = new Registry<>(${TEMPLATE}::new,${TEMPLATE}.class);


   /**
    * Returns a collection of all known ${TEMPLATE} in the enumeration.
    *
    * @return the collection of known ${TEMPLATE}
    */
   public static Collection<${TEMPLATE}> values() {
      return registry.values();
   }

   /**
    * Makes a new or retrieves an existing ${TEMPLATE} with the given name
    *
    * @param name the name of the ${TEMPLATE}
    * @return the ${TEMPLATE}
    */
   public static ${TEMPLATE} make(String name) {
      return registry.make(name);
   }

   /**
    * Retrieves an already defined element with the given
    *
    * @param name the name of the ${TEMPLATE} to retrieve
    * @return the ${TEMPLATE}
    * @throws java.lang.IllegalArgumentException if the name does not correspond to an already defined element
    */
   public static ${TEMPLATE} valueOf(String name) {
      return registry.valueOf(name);
   }

   private ${TEMPLATE}(String name) {
      super(name);
   }

   @Override
   protected Registry<${TEMPLATE}> registry() {
      return registry;
   }


}//END OF CLASS ${TEMPLATE}