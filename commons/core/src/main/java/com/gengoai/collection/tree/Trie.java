package com.gengoai.collection.tree;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.gengoai.collection.Iterators;
import com.gengoai.collection.Maps;
import com.gengoai.conversion.Cast;
import com.gengoai.string.CharMatcher;
import com.gengoai.string.Strings;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static com.gengoai.tuple.Tuples.$;

/**
 * <p>A basic <a href="https://en.wikipedia.org/wiki/Trie">Trie</a> implementation that uses hashmaps to store its
 * child nodes. The {@link #find(String, CharMatcher)} method provides functionality to find all elements of the trie in
 * the specified string in longest-first style using the specified CharPredicate to accept or reject matches based on
 * the character after the match, e.g. only match if the next character is whitespace.</p>
 *
 * <p>Note that views of the trie, i.e. keySet(), values(), entrySet(), and the resulting map from prefix(), are
 * unmodifiable.</p>
 *
 * @param <V> the value type of the trie.
 * @author David B. Bracewell
 */
public class Trie<V> implements Serializable, Map<String, V> {
   private static final long serialVersionUID = 1L;
   private final TrieNode<V> root;

   /**
    * Instantiates a new Trie.
    */
   public Trie() {
      this.root = new TrieNode<>(null, null);
   }

   /**
    * Instantiates a new Trie initializing the values to those in the given map.
    *
    * @param map A map of string to value used to populate the trie.
    */
   public Trie(Map<String, V> map) {
      this();
      putAll(map);
   }

   @JsonCreator
   private Trie(Set<Map.Entry<String, V>> entrySet) {
      this();
      entrySet.forEach(e -> put(e.getKey(), e.getValue()));
   }

   @Override
   public void clear() {
      root.children.clear();
      root.size = 0;
      root.matches = null;
      root.value = null;
      root.prune();
   }

   @Override
   public boolean containsKey(Object key) {
      if(key == null) {
         return false;
      }
      TrieNode<V> match = root.find(key.toString());
      return match != null && Strings.safeEquals(match.matches, Cast.as(key), true);
   }

   @Override
   public boolean containsValue(Object value) {
      return values().contains(value);
   }

   @Override
   @JsonValue
   public Set<Entry<String, V>> entrySet() {
      return new AbstractSet<Entry<String, V>>() {

         @Override
         public boolean contains(Object o) {
            if(o instanceof Entry) {
               Entry entry = Cast.as(o);
               return Trie.this.containsKey(entry.getKey()) && Trie.this.get(entry.getKey()).equals(entry.getValue());
            }
            return false;
         }

         @Override
         public Iterator<Entry<String, V>> iterator() {
            return root.subTreeIterator();
         }

         @Override
         public int size() {
            return root.size;
         }
      };
   }

   /**
    * Matches the strings in the trie against a specified text. Matching is doing using a greedy longest match wins way.
    * The give CharPredicate is used to determine if matches are accepted, e.g. only accept a match followed by a
    * whitespace character.
    *
    * @param text      the text to find the trie elements in
    * @param delimiter the predicate that specifies acceptable delimiters
    * @return the list of matched elements
    */
   public List<TrieMatch<V>> find(String text, CharMatcher delimiter) {
      if(Strings.isNullOrBlank(text)) {
         return Collections.emptyList();
      }
      if(delimiter == null) {
         delimiter = CharMatcher.Any;
      }

      int len = text.length();
      StringBuilder key = new StringBuilder();
      int start = 0;
      int lastMatch = -1;
      List<TrieMatch<V>> results = new ArrayList<>();

      for(int i = 0; i < len; i++) {

         key.append(text.charAt(i));

         //We have a key match
         if(containsKey(key.toString())) {
            int nextI = lastMatch = i + 1;

            //There is something longer!
            if(nextI < len && prefix(key.toString() + text.charAt(i + 1)).size() > 0) {
               continue;
            }

            lastMatch = -1;
            //check if we accept
            if(nextI >= len || delimiter.test(text.charAt(nextI))) {
               V value = get(key.toString());
               results.add(new TrieMatch<>(start, nextI, value));
               start = nextI;
            }

         } else if(prefix(key.toString()).isEmpty()) {

            //We cannot possibly match anything
            if(lastMatch != -1) {
               //We have a good match, so lets use it
               int nextI = lastMatch;
               if(nextI >= 1 && delimiter.test(text.charAt(nextI))) {
                  key = new StringBuilder(text.substring(start, nextI));
                  V value = get(key.toString());
                  results.add(new TrieMatch<>(start, nextI, value));
                  i = nextI;
                  lastMatch = -1;
                  start = nextI;
               } else {
                  start = i;
               }
            } else {
               start = i;
            }

            if(start < len) {
               key.setLength(1);
               key.setCharAt(0, text.charAt(start));
            } else {
               key.setLength(0);
            }
         }

      }

      return results;

   }

   @Override
   public V get(Object key) {
      if(key == null) {
         return null;
      }
      TrieNode<V> match = root.find(key.toString());
      if(match != null) {
         return match.value;
      }
      return null;
   }

   @Override
   public boolean isEmpty() {
      return root.size == 0;
   }

   @Override
   public Set<String> keySet() {
      return new AbstractSet<String>() {

         @Override
         public boolean contains(Object o) {
            return Trie.this.containsKey(o);
         }

         @Override
         public Iterator<String> iterator() {
            return new KeyIterator<>(root);
         }

         @Override
         public int size() {
            return root.size;
         }

      };
   }

   /**
    * <p>Returns an unmodifiable map view of this Trie containing only those elements with the given prefix.</p>
    *
    * @param prefix the prefix to match
    * @return A unmodifiable map view of the trie whose elements have the given prefix
    */
   public Map<String, V> prefix(String prefix) {
      final TrieNode<V> match = root.find(prefix);
      if(match == null) {
         return Collections.emptyMap();
      }
      return new AbstractMap<String, V>() {
         @Override
         public Set<Entry<String, V>> entrySet() {
            return new AbstractSet<Entry<String, V>>() {
               @Override
               public Iterator<Entry<String, V>> iterator() {
                  return Iterators.unmodifiableIterator(new EntryIterator<>(match));
               }

               @Override
               public int size() {
                  return match.size;
               }
            };
         }
      };
   }

   public Iterator<String> prefixKeyIterator(String prefix) {
      final TrieNode<V> match = root.find(prefix);
      if(match == null) {
         return Collections.emptyIterator();
      }
      return Iterators.transform(Iterators.unmodifiableIterator(new EntryIterator<>(match)), Entry::getKey);
   }

   @Override
   public V put(String key, V value) {
      return root.extend(key.toCharArray(), 0, value);
   }

   @Override
   public void putAll(Map<? extends String, ? extends V> m) {
      m.forEach(this::put);
   }

   @Override
   public V remove(Object key) {
      if(key == null) {
         return null;
      }
      TrieNode<V> node = root.find(key.toString());
      V value = null;
      if(node != null) {
         node.matches = null;
         value = node.value;
         node.value = null;
         if(value != null) {
            node.size--;
         }
         node.prune();
      }
      return value;
   }

   private void search(TrieNode<V> node,
                       char letter,
                       String word,
                       int[] previous,
                       Map<String, Integer> results,
                       int maxCost,
                       int substitutionCost) {
      int columns = word.length() + 1;
      int[] current = new int[columns];
      current[0] = previous[0] + 1;
      int rowMin = current[0];

      for(int i = 1; i < columns; i++) {
         int insertCost = current[i - 1] + 1;
         int deleteCost = previous[i] + 1;
         int replaceCost = previous[i - 1];
         if(word.charAt(i - 1) != letter) {
            replaceCost += substitutionCost;
         }
         current[i] = Math.min(insertCost, Math.min(deleteCost, replaceCost));
         rowMin = Math.min(rowMin, current[i]);
      }

      if(current[columns - 1] <= maxCost && node.matches != null) {
         results.put(node.matches, current[columns - 1]);
      }

      if(rowMin <= maxCost) {
         for(TrieNode<V> child : node.children) {
            if(child != null) {
               search(child, child.nodeChar, word, current, results, maxCost, substitutionCost);
            }
         }
      }

   }

   @Override
   public int size() {
      return root.size;
   }

   /**
    * Suggest map.
    *
    * @param string the string
    * @return the map
    */
   public Map<String, Integer> suggest(String string) {
      return suggest(string, 3, 1);
   }

   /**
    * Suggest map.
    *
    * @param string  the string
    * @param maxCost the max cost
    * @return the map
    */
   public Map<String, Integer> suggest(String string, int maxCost) {
      return suggest(string, maxCost, 1);
   }

   /**
    * Suggest map.
    *
    * @param string           the string
    * @param maxCost          the max cost
    * @param substitutionCost the substitution cost
    * @return the map
    */
   public Map<String, Integer> suggest(String string, int maxCost, int substitutionCost) {
      if(Strings.isNullOrBlank(string)) {
         return Collections.emptyMap();
      } else if(containsKey(string)) {
         return Maps.hashMapOf($(string, 0));
      }
      Map<String, Integer> results = new HashMap<>();
      int[] current = new int[string.length() + 1];
      for(int i = 0; i < current.length; i++) {
         current[i] = i;
      }
      for(TrieNode<V> child : root.children) {
         if(child != null) {
            search(child, child.nodeChar, string, current, results, maxCost, substitutionCost);
         }
      }
      return results;
   }

   @Override
   public String toString() {
      return entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(", ", "{", "}"));
   }

   /**
    * Compresses the memory of the individual trie nodes.
    *
    * @return this trie
    */
   public Trie<V> trimToSize() {
      Queue<TrieNode<V>> queue = new LinkedList<>();
      queue.add(root);
      while(!queue.isEmpty()) {
         TrieNode<V> node = queue.remove();
         node.children.trimToSize();
         queue.addAll(node.children);
      }
      return this;
   }

   @Override
   public Collection<V> values() {
      return new AbstractCollection<V>() {
         @Override
         public Iterator<V> iterator() {
            return new ValueIterator<>(root);
         }

         @Override
         public int size() {
            return root.size;
         }
      };
   }

   private static class EntryIterator<V> extends TrieIterator<V, Entry<String, V>> {

      private EntryIterator(TrieNode<V> node) {
         super(node);
      }

      @Override
      Entry<String, V> convert(final TrieNode<V> old) {
         return new Entry<String, V>() {
            TrieNode<V> node = old;

            @Override
            public boolean equals(Object obj) {
               if(obj == null || !(obj instanceof Entry)) {
                  return false;
               }
               Entry e = Cast.as(obj);
               return e.getKey().equals(node.matches) && e.getValue().equals(node.value);
            }

            @Override
            public String getKey() {
               return node.matches;
            }

            @Override
            public V getValue() {
               return node.value;
            }

            @Override
            public V setValue(V value) {
               V oldValue = node.value;
               node.value = value;
               return oldValue;
            }

            @Override
            public String toString() {
               return node.matches + "=" + node.value;
            }
         };
      }

   }

   private static class KeyIterator<V> extends TrieIterator<V, String> {

      private KeyIterator(TrieNode<V> node) {
         super(node);
      }

      @Override
      String convert(TrieNode<V> node) {
         return node.matches;
      }

   }

   private static abstract class TrieIterator<V, E> implements Iterator<E> {
      private final Queue<TrieNode<V>> queue = new LinkedList<>();
      private TrieNode<V> current = null;
      private TrieNode<V> old = null;

      private TrieIterator(TrieNode<V> node) {
         if(node.matches != null) {
            queue.add(node);
         } else {
            queue.addAll(node.children);
         }
      }

      /**
       * Convert e.
       *
       * @param node the node
       * @return the e
       */
      abstract E convert(TrieNode<V> node);

      @Override
      public boolean hasNext() {
         return move() != null;
      }

      private TrieNode<V> move() {
         while(current == null || current.matches == null) {
            if(queue.isEmpty()) {
               return null;
            }
            current = queue.remove();
            queue.addAll(current.children);
         }
         return current;
      }

      @Override
      public E next() {
         old = move();
         if(old == null) {
            throw new NoSuchElementException();
         }
         current = null;
         return convert(old);
      }

   }

   private static class TrieNode<V> implements Serializable, Comparable<TrieNode<V>> {
      private static final long serialVersionUID = 1L;
      private final int depth;
      private final Character nodeChar;
      private final TrieNode<V> parent;
      private ArrayList<TrieNode<V>> children = new ArrayList<>(5);
      private String matches;
      private int size = 0;
      private V value;

      private TrieNode(Character nodeChar, TrieNode<V> parent) {
         this.nodeChar = nodeChar;
         this.parent = parent;
         if(parent == null) {
            this.depth = 0;
         } else {
            this.depth = parent.depth + 1;
         }
      }

      @Override
      public int compareTo(TrieNode<V> o) {
         return nodeChar.compareTo(o.nodeChar);
      }

      /**
       * Contains boolean.
       *
       * @param string the string
       * @return the boolean
       */
      public boolean contains(String string) {
         return find(string) != null;
      }

      @Override
      public boolean equals(Object o) {
         if(this == o) return true;
         if(!(o instanceof TrieNode)) return false;
         TrieNode<?> trieNode = (TrieNode<?>) o;
         return depth == trieNode.depth &&
               size == trieNode.size &&
               Objects.equals(nodeChar, trieNode.nodeChar) &&
               Objects.equals(parent, trieNode.parent) &&
               Objects.equals(value, trieNode.value) &&
               Objects.equals(matches, trieNode.matches) &&
               Objects.equals(children, trieNode.children);
      }

      /**
       * Extend v.
       *
       * @param word  the word
       * @param start the start
       * @param value the value
       * @return the v
       */
      V extend(char[] word, int start, V value) {
         TrieNode<V> node = this;
         if(start == word.length) {
            V old = node.value;
            node.value = value;
            node.matches = new String(word);
            if(old == null) {
               node.size++;
            }
            return old;
         }

         TrieNode<V> cNode = new TrieNode<>(word[start], this);
         int index = Collections.binarySearch(children, cNode);
         if(index < 0) {
            if(children.isEmpty()) {
               children.add(cNode);
               index = 0;
            } else {
               index = Math.abs(index) - 1;
               children.add(index, cNode);
            }
         }
         cNode = children.get(index);
         V toReturn = cNode.extend(word, start + 1, value);
         if(toReturn == null) {
            size++;
         }
         return toReturn;
      }

      /**
       * Find trie node.
       *
       * @param string the string
       * @return the trie node
       */
      TrieNode<V> find(String string) {
         if(string == null || string.length() == 0) {
            return null;
         }
         TrieNode<V> node = this;
         if(nodeChar == null) {
            node = get(string.charAt(0));
         } else if(nodeChar != string.charAt(0)) {
            return null;
         }
         for(int i = 1; node != null && i < string.length(); i++) {
            node = node.get(string.charAt(i));
            //            node = node.children.get(string.charAt(i));
         }
         return node;
      }

      private TrieNode<V> get(char c) {
         TrieNode<V> tnode = new TrieNode<>(c, parent);
         int index = Collections.binarySearch(children, tnode);
         if(index < 0) {
            return null;
         }
         return children.get(index);
      }

      @Override
      public int hashCode() {
         return Objects.hash(nodeChar, parent, depth, value, matches, size, children);
      }

      /**
       * Prune.
       */
      void prune() {
         if(parent == null) {
            return;
         }
         if(matches == null && children.isEmpty()) {
            parent.children.remove(this);
         }
         parent.size--;
         parent.prune();
      }

      /**
       * Sub tree iterator iterator.
       *
       * @return the iterator
       */
      Iterator<Entry<String, V>> subTreeIterator() {
         return new EntryIterator<>(this);
      }

      @Override
      public String toString() {
         return "(" + matches + ", " + value + ")";
      }

   }

   private static class ValueIterator<V> extends TrieIterator<V, V> {

      private ValueIterator(TrieNode<V> node) {
         super(node);
      }

      @Override
      V convert(TrieNode<V> node) {
         return node.value;
      }
   }

}// END OF Trie
