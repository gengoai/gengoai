
import java.util.Collection;
import com.gengoai.HierarchicalRegistry;

/**
 * The type ${TEMPLATE}.
 */
public class ${TEMPLATE} extends HierarchicalEnumValue<${TEMPLATE}> {
   private static final long serialVersionUID = 1L;
   private static final HierarchicalRegistry<${TEMPLATE}> registry = new HierarchicalRegistry<>(
      ${TEMPLATE}::new,
      ${TEMPLATE}.class,
      "ROOT");
   public static final ${TEMPLATE} ROOT=registry.ROOT;

   /**
    * Makes a new or retrieves an existing ${TEMPLATE} with the given parent and name
    *
    * @param parent the parent ${TEMPLATE}
    * @param name   the name of the ${TEMPLATE}
    * @return the ${TEMPLATE}
    */
   public static ${TEMPLATE} make(${TEMPLATE} parent, String name) {
      return registry.make(parent, name);
   }

   /**
    * Makes a new or retrieves an existing ${TEMPLATE}.
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
    * @param name the name (or label) of the ${TEMPLATE} to retrieve
    * @return the ${TEMPLATE}
    * @throws java.lang.IllegalArgumentException if the name does not correspond to an already defined element
    */
   public static ${TEMPLATE} valueOf(String name) {
      return registry.valueOf(name);
      }

   /**
    * Returns a collection of all currently registered ${TEMPLATE}
    *
    * @return the collection of ${TEMPLATE}
    */
   public static Collection<${TEMPLATE}> values() {
      return registry.values();
   }

   private ${TEMPLATE}(String name) {
      super(name);
   }


   @Override
   protected HierarchicalRegistry<${TEMPLATE}> registry() {
      return registry;
   }

}//END OF ${TEMPLATE}
