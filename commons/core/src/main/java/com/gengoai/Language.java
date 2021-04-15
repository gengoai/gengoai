/*
 * (c) 2005 David B. Bracewell
 *
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

package com.gengoai;

import com.gengoai.stream.Streams;
import com.gengoai.string.Strings;

import java.text.Collator;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * <p>Enumeration of world languages with helpful information on whether or not the language is Whitespace delimited
 * or if language is read right to left (May not be complete).</p>
 *
 * @author David B. Bracewell
 */
public enum Language {
   /**
    * English.
    */
   ENGLISH("EN") {
      @Override
      public Locale asLocale() {
         return Locale.US;
      }

   },
   /**
    * Japanese.
    */
   JAPANESE("JA") {
      @Override
      public boolean usesWhitespace() {
         return false;
      }

      @Override
      public Locale asLocale() {
         return Locale.JAPAN;
      }
   },
   /**
    * Chinese.
    */
   CHINESE("ZH") {
      @Override
      public boolean usesWhitespace() {
         return false;
      }

      @Override
      public Locale asLocale() {
         return Locale.CHINA;
      }

   },
   /**
    * Abkhazian language.
    */
   ABKHAZIAN("AB"),
   /**
    * Afar language.
    */
   AFAR("AA"),
   /**
    * Afrikaans language.
    */
   AFRIKAANS("AF"),
   /**
    * Albanian language.
    */
   ALBANIAN("SQ"),
   /**
    * Amharic language.
    */
   AMHARIC("AM"),
   /**
    * Arabic.
    */
   ARABIC("AR") {
      @Override
      public boolean isRightToLeft() {
         return true;
      }
   },
   /**
    * Armenian language.
    */
   ARMENIAN("HY"),
   /**
    * Assamese language.
    */
   ASSAMESE("AS"),
   /**
    * Aymara language.
    */
   AYMARA("AY"),
   /**
    * Azerbaijani language.
    */
   AZERBAIJANI("AZ"),
   /**
    * Bashkir language.
    */
   BASHKIR("BA"),
   /**
    * Basque language.
    */
   BASQUE("EU"),
   /**
    * Bengali language.
    */
   BENGALI("BN"),
   /**
    * Bhutani language.
    */
   BHUTANI("DZ"),
   /**
    * Bihari language.
    */
   BIHARI("BH"),
   /**
    * Bislama language.
    */
   BISLAMA("BI"),
   /**
    * Breton language.
    */
   BRETON("BR"),
   /**
    * Bulgarian language.
    */
   BULGARIAN("BG"),
   /**
    * Burmese language.
    */
   BURMESE("MY"),
   /**
    * Byelorussian language.
    */
   BYELORUSSIAN("BE"),
   /**
    * Cambodian language.
    */
   CAMBODIAN("KM"),
   /**
    * Catalan language.
    */
   CATALAN("CA"),
   /**
    * Corsican language.
    */
   CORSICAN("CO"),
   /**
    * Croatian language.
    */
   CROATIAN("HR"),
   /**
    * Czech language.
    */
   CZECH("CS"),
   /**
    * Danish language.
    */
   DANISH("DA"),
   /**
    * Dutch language.
    */
   DUTCH("NL"),
   /**
    * Esperanto language.
    */
   ESPERANTO("EO"),
   /**
    * Estonian language.
    */
   ESTONIAN("ET"),
   /**
    * Faeroese language.
    */
   FAEROESE("FO"),
   /**
    * Fiji language.
    */
   FIJI("FJ"),
   /**
    * Finnish language.
    */
   FINNISH("FI"),
   /**
    * French language.
    */
   FRENCH("FR"),
   /**
    * Frisian language.
    */
   FRISIAN("FY"),
   /**
    * Gaelic language.
    */
   GAELIC("GD"),
   /**
    * Galician language.
    */
   GALICIAN("GL"),
   /**
    * Georgian language.
    */
   GEORGIAN("KA"),
   /**
    * German language.
    */
   GERMAN("DE"),
   /**
    * Greek language.
    */
   GREEK("EL"),
   /**
    * Greenlandic language.
    */
   GREENLANDIC("KL"),
   /**
    * Guarani language.
    */
   GUARANI("GN"),
   /**
    * Gujarati language.
    */
   GUJARATI("GU"),
   /**
    * Hausa language.
    */
   HAUSA("HA"),
   /**
    * Hebrew.
    */
   HEBREW("IW") {
      @Override
      public boolean isRightToLeft() {
         return true;
      }
   },
   /**
    * Hindi language.
    */
   HINDI("HI"),
   /**
    * Hungarian language.
    */
   HUNGARIAN("HU"),
   /**
    * Icelandic language.
    */
   ICELANDIC("IS"),
   /**
    * Indonesian language.
    */
   INDONESIAN("IN"),
   /**
    * Interlingua language.
    */
   INTERLINGUA("IA"),
   /**
    * Interlingue language.
    */
   INTERLINGUE("IE"),
   /**
    * Inupiak language.
    */
   INUPIAK("IK"),
   /**
    * Irish language.
    */
   IRISH("GA"),
   /**
    * Italian language.
    */
   ITALIAN("IT"),
   /**
    * Javanese.
    */
   JAVANESE("JW") {
      @Override
      public boolean isRightToLeft() {
         return true;
      }
   },
   /**
    * Kannada language.
    */
   KANNADA("KN"),
   /**
    * Kashmiri.
    */
   KASHMIRI("KS") {
      @Override
      public boolean isRightToLeft() {
         return true;
      }
   },
   /**
    * Kazakh language.
    */
   KAZAKH("KK"),
   /**
    * Kinyarwanda language.
    */
   KINYARWANDA("RW"),
   /**
    * Kirghiz language.
    */
   KIRGHIZ("KY"),
   /**
    * Kirundi language.
    */
   KIRUNDI("RN"),
   /**
    * Korean language.
    */
   KOREAN("KO"),
   /**
    * Kurdish.
    */
   KURDISH("KU") {
      @Override
      public boolean isRightToLeft() {
         return true;
      }
   },
   /**
    * Laothian language.
    */
   LAOTHIAN("LO"),
   /**
    * Latin language.
    */
   LATIN("LA"),
   /**
    * Latvian language.
    */
   LATVIAN("LV"),
   /**
    * Lingala language.
    */
   LINGALA("LN"),
   /**
    * Lithuanian language.
    */
   LITHUANIAN("LT"),
   /**
    * Macedonian language.
    */
   MACEDONIAN("MK"),
   /**
    * Malagasy language.
    */
   MALAGASY("MG"),
   /**
    * Malay.
    */
   MALAY("MS") {
      @Override
      public boolean isRightToLeft() {
         return true;
      }
   },
   /**
    * Malayalam.
    */
   MALAYALAM("ML") {
      @Override
      public boolean isRightToLeft() {
         return true;
      }
   },
   /**
    * Maltese language.
    */
   MALTESE("MT"),
   /**
    * Maori language.
    */
   MAORI("MI"),
   /**
    * Marathi language.
    */
   MARATHI("MR"),
   /**
    * Moldavian language.
    */
   MOLDAVIAN("MO"),
   /**
    * Mongolian language.
    */
   MONGOLIAN("MN"),
   /**
    * Nauru language.
    */
   NAURU("NA"),
   /**
    * Nepali language.
    */
   NEPALI("NE"),
   /**
    * Norwegian language.
    */
   NORWEGIAN("NO"),
   /**
    * Occitan language.
    */
   OCCITAN("OC"),
   /**
    * Oriya language.
    */
   ORIYA("OR"),
   /**
    * Oromo language.
    */
   OROMO("OM"),
   /**
    * Pashto.
    */
   PASHTO("PS") {
      @Override
      public boolean isRightToLeft() {
         return true;
      }
   },
   /**
    * Persian.
    */
   PERSIAN("FA") {
      @Override
      public boolean isRightToLeft() {
         return true;
      }
   },
   /**
    * Polish language.
    */
   POLISH("PL"),
   /**
    * Portuguese language.
    */
   PORTUGUESE("PT"),
   /**
    * Punjabi.
    */
   PUNJABI("PA") {
      @Override
      public boolean isRightToLeft() {
         return true;
      }
   },
   /**
    * Quechua language.
    */
   QUECHUA("QU"),
   /**
    * Romanian language.
    */
   ROMANIAN("RO"),
   /**
    * Russian language.
    */
   RUSSIAN("RU"),
   /**
    * Samoan language.
    */
   SAMOAN("SM"),
   /**
    * Sangro language.
    */
   SANGRO("SG"),
   /**
    * Sanskrit language.
    */
   SANSKRIT("SA"),
   /**
    * Serbian language.
    */
   SERBIAN("SR"),
   /**
    * Serbo croatian language.
    */
   SERBO_CROATIAN("SH"),
   /**
    * Sesotho language.
    */
   SESOTHO("ST"),
   /**
    * Setswana language.
    */
   SETSWANA("TN"),
   /**
    * Shona language.
    */
   SHONA("SN"),
   /**
    * Sindhi.
    */
   SINDHI("SD") {
      @Override
      public boolean isRightToLeft() {
         return true;
      }
   },
   /**
    * Singhalese language.
    */
   SINGHALESE("SI"),
   /**
    * Siswati language.
    */
   SISWATI("SS"),
   /**
    * Slovak language.
    */
   SLOVAK("SK"),
   /**
    * Slovenian language.
    */
   SLOVENIAN("SL"),
   /**
    * Somali.
    */
   SOMALI("SO") {
      @Override
      public boolean isRightToLeft() {
         return true;
      }
   },
   /**
    * Spanish language.
    */
   SPANISH("ES"),
   /**
    * Sudanese language.
    */
   SUDANESE("SU"),
   /**
    * Swahili language.
    */
   SWAHILI("SW"),
   /**
    * Swedish language.
    */
   SWEDISH("SV"),
   /**
    * Tagalog language.
    */
   TAGALOG("TL"),
   /**
    * Tajik language.
    */
   TAJIK("TG"),
   /**
    * Tamil language.
    */
   TAMIL("TA"),
   /**
    * Tatar language.
    */
   TATAR("TT"),
   /**
    * Telugu language.
    */
   TELUGU("TE"),
   /**
    * Thai language.
    */
   THAI("TH"),
   /**
    * Tibetan language.
    */
   TIBETAN("BO"),
   /**
    * Tigrinya language.
    */
   TIGRINYA("TI"),
   /**
    * Tonga language.
    */
   TONGA("TO"),
   /**
    * Tsonga language.
    */
   TSONGA("TS"),
   /**
    * Turkish language.
    */
   TURKISH("TR"),
   /**
    * Turkmen.
    */
   TURKMEN("TK") {
      @Override
      public boolean isRightToLeft() {
         return true;
      }
   },
   /**
    * Twi language.
    */
   TWI("TW"),
   /**
    * Ukrainian language.
    */
   UKRAINIAN("UK"),
   /**
    * Urdu.
    */
   URDU("UR") {
      @Override
      public boolean isRightToLeft() {
         return true;
      }
   },
   /**
    * Uzbek language.
    */
   UZBEK("UZ"),
   /**
    * Vietnamese language.
    */
   VIETNAMESE("VI"),
   /**
    * Volapuk language.
    */
   VOLAPUK("VO"),
   /**
    * Welsh language.
    */
   WELSH("CY"),
   /**
    * Wolof language.
    */
   WOLOF("WO"),
   /**
    * Xhosa language.
    */
   XHOSA("XH"),
   /**
    * Yiddish.
    */
   YIDDISH("JI") {
      @Override
      public boolean isRightToLeft() {
         return true;
      }
   },
   /**
    * Yoruba language.
    */
   YORUBA("YO"),
   /**
    * Zulu language.
    */
   ZULU("ZU"),
   /**
    * Unknown.
    */
   UNKNOWN("UNKNOWN") {
      @Override
      public Locale asLocale() {
         return Locale.getDefault();
      }
   };

   private final String code;
   private transient List<Locale> locales;

   Language(String code) {
      this.code = code;
   }

   /**
    * Parses a language code to get its corresponding LanguageId
    *
    * @param code language code
    * @return parsed language or null
    */
   public static Language fromString(String code) {
      try {
         return Language.valueOf(code.toUpperCase());
      } catch(Exception e) {

         Locale toFind;
         if(code.contains("_") || code.contains("-")) {
            String[] parts = code.split("[_\\-]");
            if(parts.length >= 2) {
               toFind = new Locale(parts[0], parts[1]);
            } else {
               toFind = new Locale(code);
            }
         } else {
            toFind = new Locale(code);
         }

         return fromLocale(toFind);
      }
   }

   /**
    * From locale language.
    *
    * @param locale locale
    * @return language language
    */
   public static Language fromLocale(Locale locale) {
      if(locale == null) {
         locale = Locale.getDefault();
      }
      for(Language l : Language.values()) {
         if(l.asLocale().getLanguage().equals(locale.getLanguage())) {
            return l;
         }
      }
      return UNKNOWN;
   }

   /**
    * Gets language as a {@link Locale}
    *
    * @return language locale
    */
   public Locale asLocale() {
      return Locale.forLanguageTag(name());
   }

   /**
    * Gets language code.
    *
    * @return ISO2 Language code
    */
   public String getCode() {
      return code;
   }

   /**
    * Convenience method for constructing a collator.
    *
    * @param strength      strength of {@link Collator}
    * @param decomposition decomposition of {@link Collator}
    * @return collator collator
    */
   public final Collator getCollator(int strength, int decomposition) {
      Collator collator = Collator.getInstance(asLocale());
      collator.setStrength(strength);
      collator.setDecomposition(decomposition);
      return collator;
   }

   /**
    * Convenience method for constructing a collator using <code>FULL_DECOMPOSITION</code>
    *
    * @param strength strength of {@link Collator}
    * @return collator collator
    */
   public final Collator getCollator(int strength) {
      return getCollator(strength, Collator.FULL_DECOMPOSITION);
   }

   /**
    * Convenience method for constructing a collator using a strength of <code>TERTIARY</code> and decomposition of
    * <code>FULL_DECOMPOSITION</code>
    *
    * @return collator collator
    */
   public final Collator getCollator() {
      return getCollator(Collator.TERTIARY, Collator.FULL_DECOMPOSITION);
   }

   /**
    * Gets currency format.
    *
    * @return currency format
    */
   public NumberFormat getCurrencyFormat() {
      return DecimalFormat.getCurrencyInstance(asLocale());
   }

   /**
    * Gets date format.
    *
    * @param style style
    * @return date format
    */
   public DateFormat getDateFormat(int style) {
      return DateFormat.getDateInstance(style, asLocale());
   }

   /**
    * Gets locales.
    *
    * @return possible locales associated with language.
    */
   public synchronized List<Locale> getLocales() {
      if(locales != null) {
         return locales;
      }
      locales = new ArrayList<>();
      for(Locale locale : DateFormat.getAvailableLocales()) {
         if(locale.getLanguage().equalsIgnoreCase(code)) {
            locales.add(locale);
         }
      }
      return locales;
   }

   /**
    * Gets number format.
    *
    * @return number format
    */
   public NumberFormat getNumberFormat() {
      return DecimalFormat.getNumberInstance(asLocale());
   }

   /**
    * Gets percent format.
    *
    * @return percent format
    */
   public NumberFormat getPercentFormat() {
      return DecimalFormat.getPercentInstance(asLocale());
   }

   /**
    * Is the language written right to left
    *
    * @return True if language is written  right to left
    */
   public boolean isRightToLeft() {
      return false;
   }

   /**
    * Joins the words in the array separating them with a space if the language uses whitespace
    *
    * @param words the words to join
    * @return the joined words
    */
   public String join(String[] words) {
      return Streams.asStream(words)
                    .collect(Collectors.joining((usesWhitespace()
                                                 ? Strings.BLANK
                                                 : Strings.EMPTY)));
   }

   /**
    * Joins the words in the array separating them with a space if the language uses whitespace
    *
    * @param words the words to join
    * @return the joined words
    */
   public String join(CharSequence[] words) {
      return Streams.asStream(words)
                    .collect(Collectors.joining((usesWhitespace()
                                                 ? Strings.BLANK
                                                 : Strings.EMPTY)));
   }

   /**
    * Joins the words in the array separating them with a space if the language uses whitespace
    *
    * @param words the words to join
    * @return the joined words
    */
   public String join(Iterable<? extends CharSequence> words) {
      return Streams.asStream(words)
                    .collect(Collectors.joining((usesWhitespace()
                                                 ? Strings.BLANK
                                                 : Strings.EMPTY)));
   }

   /**
    * Does the language use whitespace to separate words
    *
    * @return True if language uses white space to separate words, false if not
    */
   public boolean usesWhitespace() {
      return true;
   }

}// END OF Language
