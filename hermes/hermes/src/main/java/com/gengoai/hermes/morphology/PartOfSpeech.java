/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.gengoai.hermes.morphology;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.gengoai.Tag;
import com.gengoai.Validation;
import com.gengoai.annotation.Preload;
import com.gengoai.conversion.Cast;
import com.gengoai.hermes.Annotation;
import com.gengoai.hermes.HString;
import com.gengoai.hermes.Types;
import com.gengoai.string.Strings;
import com.gengoai.tuple.Tuple2;
import lombok.Getter;
import lombok.NonNull;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.gengoai.hermes.morphology.PennTreeBank.*;

/**
 * <p>
 * Interface defining a part-of-speech. A part-of-speech has an associated name representing a human-readable label, a
 * tag which is used in annotated corpora, and a {@link UniversalFeatureSet} set relating to the features the tag
 * invokes.
 * </p>
 * <p>
 * Out-of-the-box Hermes provides the Universal and Penn Treebank tag sets. New tags can be created by calling {@link
 * PartOfSpeech#create(String, String, PartOfSpeech, boolean, Tuple2[])}, which will register the tag. Note: It is
 * important that name and tag are unique. In the case where a pos tag would have the same name or tag value, it should
 * reuse an existing tag when compatible or prefix the name and tag with a tag set identifier, e.g. if we want a tag
 * with the name "NN" and it does not correspond to the Penn Treebank tag, we could prefix with tag set identifier, like
 * "myNN". Note: This means your training and testing corpora will need to be modified to include the prefix.
 * </p>
 */
@Preload
@JsonAutoDetect(
      fieldVisibility = JsonAutoDetect.Visibility.NONE,
      setterVisibility = JsonAutoDetect.Visibility.NONE,
      getterVisibility = JsonAutoDetect.Visibility.NONE,
      isGetterVisibility = JsonAutoDetect.Visibility.NONE,
      creatorVisibility = JsonAutoDetect.Visibility.NONE
)
public final class PartOfSpeech implements Tag, Serializable {
   private static final Map<String, PartOfSpeech> tags = new ConcurrentHashMap<>();
   private static final long serialVersionUID = 1L;

   //--------------------------------------------------------------------------------------------------------------
   // Universal Part-of-speech Tags
   //--------------------------------------------------------------------------------------------------------------
   public static final PartOfSpeech ANY = upos("ANY", "UNKNOWN", null);
   public static final PartOfSpeech ADJECTIVE = upos("ADJECTIVE", "ADJ", ANY);
   public static final PartOfSpeech ADPOSITION = upos("ADPOSITION", "ADP", ANY);
   public static final PartOfSpeech ADVERB = upos("ADVERB", "ADV", ANY);
   public static final PartOfSpeech AUXILIARY = upos("AUXILIARY", "AUX", ANY);
   public static final PartOfSpeech CCONJ = upos("CCONJ", "CCONJ", ANY);
   public static final PartOfSpeech DETERMINER = upos("DETERMINER", "DET", ANY);
   public static final PartOfSpeech INTERJECTION = upos("INTERJECTION", "INT", ANY);
   public static final PartOfSpeech NOUN = upos("NOUN", "NOUN", ANY);
   public static final PartOfSpeech NUMERAL = upos("NUMERAL", "NUM", ANY);
   public static final PartOfSpeech OTHER = upos("OTHER", "X", ANY);
   public static final PartOfSpeech PARTICLE = upos("PARTICLE", "PART", ANY);
   public static final PartOfSpeech PRONOUN = upos("PRONOUN", "PRON", ANY);
   public static final PartOfSpeech PROPER_NOUN = upos("PROPER_NOUN", "PROPN", NOUN);
   public static final PartOfSpeech PUNCTUATION = upos("PUNCTUATION", "PUNCT", ANY);
   public static final PartOfSpeech SCONJ = upos("SCONJ", "SCONJ", ANY);
   public static final PartOfSpeech SYMBOL = upos("SYMBOL", "SYM", ANY);
   public static final PartOfSpeech VERB = upos("VERB", "VERB", ANY);
   //--------------------------------------------------------------------------------------------------------------

   @Getter
   private final UniversalFeatureSet features;
   private final PartOfSpeech parent;
   @Getter
   private final boolean isPhraseTag;
   private final boolean isUniversalTag;
   private final String name;
   private final String tag;

   private static PartOfSpeech create(String name,
                                      String tag,
                                      PartOfSpeech parent,
                                      boolean isPhraseTag,
                                      boolean isUniversalTag,
                                      UniversalFeatureSet features) {
      Validation.notNullOrBlank(name);
      Validation.notNullOrBlank(tag);
      name = name.toUpperCase();
      tag = tag.toUpperCase();
      PartOfSpeech pos = null;
      if(tags.containsKey(name)) {
         pos = tags.get(name);
      } else if(tags.containsKey(tag)) {
         pos = tags.get(tag);
      }

      if(pos == null) {
         pos = new PartOfSpeech(name, tag, parent, isPhraseTag, isUniversalTag, features);
         tags.put(name, pos);
         tags.put(tag, pos);
         return pos;
      }

      if(pos.name().equalsIgnoreCase(name) &&
            pos.tag().equalsIgnoreCase(tag) &&
            pos.isPhraseTag() == isPhraseTag &&
            pos.isUniversalTag == isUniversalTag &&
            Objects.equals(pos.parent, parent)) {
         return pos;
      }
      throw new IllegalStateException("Duplicate tag name: " + name);
   }

   @SafeVarargs
   public static PartOfSpeech create(String name,
                                     String tag,
                                     @NonNull PartOfSpeech parent,
                                     boolean isPhraseTag,
                                     Tuple2<UniversalFeature, UniversalFeatureValue>... features) {
      return create(name, tag, parent, isPhraseTag, false, new UniversalFeatureSet(features));
   }

   /**
    * Determines the best fundamental POS (NOUN, VERB, ADJECTIVE, or ADVERB) for a text.
    *
    * @param text The text
    * @return The part of speech
    */
   public static PartOfSpeech forText(@NonNull HString text) {
      if(text.hasAttribute(Types.PART_OF_SPEECH)) {
         return text.attribute(Types.PART_OF_SPEECH);
      }

      if(text.tokenLength() == 1 && text.tokenAt(0).hasAttribute(Types.PART_OF_SPEECH)) {
         return text.tokenAt(0).attribute(Types.PART_OF_SPEECH);
      }

      if(text.document() != null && text.document().isCompleted(Types.DEPENDENCY)) {
         HString head = text.head();
         if(!head.isEmpty()) {
            return forText(head);
         }
      }

      PartOfSpeech tag = ANY;
      for(Annotation token : text.tokens()) {
         Tag temp = token.attribute(Types.PART_OF_SPEECH);
         if(temp != null) {
            if(temp.isInstance(VERB)) {
               return VERB;
            } else if(temp.isInstance(NOUN)) {
               tag = NOUN;
            } else if(temp.isInstance(ADJECTIVE) && tag != NOUN) {
               tag = ADJECTIVE;
            } else if(temp.isInstance(ADVERB) && tag != NOUN) {
               tag = ADVERB;
            }
         }
      }

      return tag;
   }

   private static PartOfSpeech upos(String name, String tag, PartOfSpeech parent) {
      return create(name, tag, parent, false, true, new UniversalFeatureSet());
   }

   /**
    * Gets the PartOfSpeech from the given name or tag
    *
    * @param nameOrTag the name or tag of the PartOfSpeech we want
    * @return the PartOfSpeech
    */
   @JsonCreator
   public static PartOfSpeech valueOf(String nameOrTag) {
      if( nameOrTag == null) {
         return null;
      }
      nameOrTag = nameOrTag.toUpperCase();
      if(tags.containsKey(nameOrTag)) {
         return tags.get(nameOrTag);
      } else if(nameOrTag.equalsIgnoreCase("X")) {
         return ANY;
      } else if(nameOrTag.equals(";") || nameOrTag.equals("...") || nameOrTag.equals("-") || nameOrTag.equals("--")) {
         return COLON;
      } else if(nameOrTag.equals("?") || nameOrTag.equals("!")) {
         return PERIOD;
      } else if(nameOrTag.equals("``") || nameOrTag.equals("''") || nameOrTag.equals("\"\"") || nameOrTag.equals("'") || nameOrTag
            .equals("\"")) {
         return QUOTE;
      } else if(nameOrTag.equals("UH")) {
         return UH;
      } else if(nameOrTag.endsWith("{")) {
         return LCB;
      } else if(nameOrTag.endsWith("}")) {
         return RCB;
      } else if(nameOrTag.endsWith("[")) {
         return LSB;
      } else if(nameOrTag.endsWith("]")) {
         return RSB;
      } else if(nameOrTag.endsWith("(")) {
         return LRB;
      } else if(nameOrTag.endsWith(")")) {
         return RRB;
      } else if(!Strings.hasLetter(nameOrTag)) {
         return SYMBOL;
      } else if(nameOrTag.equalsIgnoreCase("ANY")) {
         return ANY;
      }  else if(nameOrTag.equalsIgnoreCase("URL")) {
         return NN;
      }
      throw new IllegalArgumentException(nameOrTag + " is not a known PartOfSpeech");
   }

   /**
    * @return All known and registered PartOfSpeech tags
    */
   public static Collection<PartOfSpeech> values() {
      return Collections.unmodifiableCollection(tags.values());
   }

   private PartOfSpeech(String name,
                        String tag,
                        PartOfSpeech parent,
                        boolean isPhraseTag,
                        boolean isUniversalTag,
                        UniversalFeatureSet features) {
      this.features = features;
      this.isPhraseTag = isPhraseTag;
      this.isUniversalTag = isUniversalTag;
      this.parent = parent;
      this.name = name;
      this.tag = tag;
   }

   @Override
   public boolean equals(Object o) {
      if(this == o) {
         return true;
      }
      if(o instanceof PartOfSpeech) {
         return name.equalsIgnoreCase(Cast.<PartOfSpeech>as(o).name);
      }
      return false;
   }

   /**
    * @return The Universal PartOfSpeech this PartOfSpeech maps to
    */
   public PartOfSpeech getUniversalTag() {
      if(this == ANY) {
         return ANY;
      }
      PartOfSpeech tag = this;
      while(tag != null && !tag.isUniversalTag()) {
         tag = tag.parent();
      }
      return tag;
   }

   @Override
   public int hashCode() {
      return name.hashCode();
   }

   /**
    * @return True if this PartOfSpeech is an Adjective
    */
   public boolean isAdjective() {
      return isInstance(ADJECTIVE);
   }

   /**
    * @return True if this PartOfSpeech is an Adposition
    */
   public boolean isAdposition() {
      return isInstance(ADPOSITION);
   }

   /**
    * @return True if this PartOfSpeech is an Adverb
    */
   public boolean isAdverb() {
      return isInstance(ADVERB);
   }

   /**
    * @return True if this PartOfSpeech is an Auxiliary
    */
   public boolean isAuxiliary() {
      return isInstance(AUXILIARY);
   }

   /**
    * @return True if this PartOfSpeech is a Coordinating Conjunction
    */
   public boolean isCoordinatingConjunction() {
      return isInstance(CCONJ);
   }

   /**
    * @return True if this PartOfSpeech is a Determiner
    */
   public boolean isDeterminer() {
      return isInstance(DETERMINER);
   }

   @Override
   public boolean isInstance(@NonNull Tag tag) {
      if(tag == ANY) {
         return true;
      }
      if(tag instanceof PartOfSpeech) {
         PartOfSpeech p = this;
         if(p == PROPER_NOUN && tag == NOUN) {
            return true;
         }
         while(p != null) {
            if(p.name().equalsIgnoreCase(tag.name())) {
               return true;
            }
            p = p.parent();
         }
      }
      return false;
   }

   /**
    * @return True if this PartOfSpeech is an Interjection
    */
   public boolean isInterjection() {
      return isInstance(INTERJECTION);
   }

   /**
    * @return True if this PartOfSpeech is a Noun
    */
   public boolean isNoun() {
      return isInstance(NOUN) || isInstance(PROPER_NOUN);
   }

   /**
    * @return True if this PartOfSpeech is a Numeral
    */
   public boolean isNumeral() {
      return isInstance(NUMERAL);
   }

   /**
    * @return True if this PartOfSpeech is a Particle
    */
   public boolean isParticle() {
      return isInstance(PARTICLE);
   }

   /**
    * @return True if this PartOfSpeech is a Pronoun
    */
   public boolean isPronoun() {
      return isInstance(PRONOUN);
   }

   /**
    * @return True if this PartOfSpeech is a Proper Noun
    */
   public boolean isProperNoun() {
      return isInstance(PROPER_NOUN);
   }

   /**
    * @return True if this PartOfSpeech is a Punctuation
    */
   public boolean isPunctuation() {
      return isInstance(PUNCTUATION);
   }

   /**
    * @return True if this PartOfSpeech is a Subordination Conjunction
    */
   public boolean isSubordinationConjunction() {
      return isInstance(SCONJ);
   }

   /**
    * @return True if this PartOfSpeech is a Symbol
    */
   public boolean isSymbol() {
      return isInstance(SYMBOL);
   }

   /**
    * @return True if this PartOfSpeech represents a Universal POS tag.
    */
   public boolean isUniversalTag() {
      return isUniversalTag;
   }

   /**
    * @return True if this PartOfSpeech is a verb
    */
   public boolean isVerb() {
      return isInstance(VERB);
   }

   @Override
   @JsonValue
   public String name() {
      return name;
   }

   public PartOfSpeech parent() {
      return parent;
   }

   protected Object readResolve() throws ObjectStreamException {
      return create(name, tag, parent, isPhraseTag, isUniversalTag, features);
   }

   /**
    * @return the tag form of the PartOfSpeech
    */
   public String tag() {
      return tag;
   }

   @Override
   public String toString() {
      return name;
   }

}//END OF PartOfSpeech
