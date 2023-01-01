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

package com.gengoai.hermes;

import com.gengoai.Tag;
import com.gengoai.conversion.TypeConversionException;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public enum SuperSense implements Tag {
   NOUN_ACT("N.ACT"),
   NOUN_ANIMAL("N.ANIMAL"),
   NOUN_ARTIFACT("N.ARTIFACT"),
   NOUN_ATTRIBUTE("N.ATTRIBUTE"),
   NOUN_BODY("N.BODY"),
   NOUN_COGNITION("N.COGNITION"),
   NOUN_COMMUNICATION("N.COMMUNICATION"),
   NOUN_EVENT("N.EVENT"),
   NOUN_FEELING("N.FEELING"),
   NOUN_FOOD("N.FOOD"),
   NOUN_GROUP("N.GROUP"),
   NOUN_LOCATION("N.LOCATION"),
   NOUN_MOTIVE("N.MOTIVE"),
   NOUN_NATURAL_OBJECT("N.NATURALOBJECT"),
   NOUN_OTHER("N.OTHER"),
   NOUN_PERSON("N.PERSON"),
   NOUN_PHENOMENON("N.PHENOMENON"),
   NOUN_PLANT("N.PLANT"),
   NOUN_POSSESSION("N.POSSESSION"),
   NOUN_PROCESS("N.PROCESS"),
   NOUN_QUANTITY("N.QUANTITY"),
   NOUN_RELATION("N.RELATION"),
   NOUN_SHAPE("N.SHAPE"),
   NOUN_STATE("N.STATE"),
   NOUN_SUBSTANCE("N.SUBSTANCE"),
   NOUN_TIME("N.TIME"),

   PREPOSITION_AGENT("P.AGENT"),
   PREPOSITION_ANCILLARY("P.ANCILLARY"),
   PREPOSITION_BENEFICIARY("P.BENEFICIARY"),
   PREPOSITION_GESTALT("P.GESTALT"),
   PREPOSITION_LOCUS("P.LOCUS"),
   PREPOSITION_SOURCE("P.SOURCE"),
   PREPOSITION_APPROXIMATOR("P.APPROXIMATOR"),
   PREPOSITION_GOAL("P.GOAL"),
   PREPOSITION_DIRECTION("P.DIRECTION"),
   PREPOSITION_POSSESSOR("P.POSSESSOR"),
   PREPOSITION_THEME("P.THEME"),
   PREPOSITION_SOCIALREL("P.SOCIALREL"),
   PREPOSITION_TOPIC("P.TOPIC"),
   PREPOSITION_PURPOSE("P.PURPOSE"),
   PREPOSITION_ENDTIME("P.ENDTIME"),
   PREPOSITION_STARTTIME("P.STARTTIME"),
   PREPOSITION_STIMULUS("P.STIMULUS"),
   PREPOSITION_EXPLANATION("P.EXPLANATION"),
   PREPOSITION_CHARACTERISTIC("P.CHARACTERISTIC"),
   PREPOSITION_COMPARISONREF("P.COMPARISONREF"),
   PREPOSITION_COST("P.COST"),
   PREPOSITION_DURATION("P.DURATION"),
   PREPOSITION_EXPERIENCER("P.EXPERIENCER"),
   PREPOSITION_EXTENT("P.EXTENT"),
   PREPOSITION_IDENTITY("P.IDENTITY"),
   PREPOSITION_MANNER("P.MANNER"),
   PREPOSITION_ORG("P.ORG"),
   PREPOSITION_ORGMEMBER("P.ORGMEMBER"),
   PREPOSITION_ORIGINATOR("P.ORIGINATOR"),
   PREPOSITION_PATH("P.PATH"),
   PREPOSITION_QUANTITYITEM("P.QUANTITYITEM"),
   PREPOSITION_RECIPIENT("P.RECIPIENT"),
   PREPOSITION_SPECIES("P.SPECIES"),
   PREPOSITION_TIME("P.TIME"),
   PREPOSITION_WHOLE("P.WHOLE"),
   PREPOSITION_CIRCUMSTANCE("P.CIRCUMSTANCE"),
   PREPOSITION_ENSEMBLE("P.ENSEMBLE"),
   PREPOSITION_FORCE("P.FORCE"),
   PREPOSITION_FREQUENCY("P.FREQUENCY"),
   PREPOSITION_INSTRUMENT("P.INSTRUMENT"),
   PREPOSITION_INTERVAL("P.INTERVAL"),
   PREPOSITION_MEANS("P.MEANS"),
   PREPOSITION_PARTPORTION("P.PARTPORTION"),
   PREPOSITION_POSSESSION("P.POSSESSION"),
   PREPOSITION_SETITERATION("P.SETITERATION"),
   PREPOSITION_STUFF("P.STUFF"),
   VERB_COGNITION("V.COGNITION"),
   VERB_COMMUNICATION("V.COMMUNICATION"),
   VERB_EMOTION("V.EMOTION"),
   VERB_MOTION("V.MOTION"),
   VERB_STATIVE("V.STATIVE"),

   VERB_PERCEPTION("V.PERCEPTION"),
   VERB_CHANGE("V.CHANGE"),
   VERB_POSSESSION("V.POSSESSION"),

   VERB_CONSUMPTION("V.CONSUMPTION"),
   VERB_CREATION("V.CREATION"),
   VERB_BODY("V.BODY"),
   VERB_CONTACT("V.CONTACT"),
   VERB_COMPETITION("V.COMPETITION"),
   VERB_SOCIAL("V.SOCIAL");


   private final String tag;
   private static final Map<String, SuperSense> tag2Sense = new HashMap<>();
   private static final Map<String, SuperSense> sense2Tag = new HashMap<>();

   static {
      for (SuperSense value : SuperSense.values()) {
         tag2Sense.put(value.tag, value);
         sense2Tag.put(value.name(), value);
      }
   }

   SuperSense(String tag) {
      this.tag = tag;
   }

   public static boolean isSuperSense(String tag) {
      return tag2Sense.containsKey(tag) || sense2Tag.containsKey(tag);
   }

   public static SuperSense byTag(String tag) {
      if (tag2Sense.containsKey(tag)) {
         return tag2Sense.get(tag);
      }
      if (sense2Tag.containsKey(tag)) {
         return sense2Tag.get(tag);
      }
      throw new IllegalArgumentException(tag + " is not valid");
   }

   @MetaInfServices
   public static class Converter implements com.gengoai.conversion.TypeConverter {

      @Override
      public Object convert(Object source, Type... parameters) throws TypeConversionException {
         if (source instanceof SuperSense) {
            return source;
         } else if (source instanceof CharSequence) {
            return SuperSense.byTag(source.toString());
         }
         throw new TypeConversionException(source, SuperSense.class);
      }

      @Override
      public Class[] getConversionType() {
         return new Class[]{SuperSense.class};
      }
   }

}
