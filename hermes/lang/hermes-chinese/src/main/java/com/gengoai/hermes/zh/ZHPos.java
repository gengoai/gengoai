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

package com.gengoai.hermes.zh;

import com.gengoai.annotation.Preload;
import com.gengoai.hermes.morphology.PartOfSpeech;

@Preload
public final class ZHPos {


   public static final PartOfSpeech IP = PartOfSpeech.create("IP", "IP", PartOfSpeech.ADVERB, true);
   public static final PartOfSpeech LCP = PartOfSpeech.create("LCP", "LCP", PartOfSpeech.ADVERB, true);
   public static final PartOfSpeech VCD = PartOfSpeech.create("VCD", "VCD", PartOfSpeech.ADVERB, true);
   public static final PartOfSpeech CP = PartOfSpeech.create("CP", "CP", PartOfSpeech.ADVERB, true);
   public static final PartOfSpeech CLP = PartOfSpeech.create("CLP", "CLP", PartOfSpeech.ADVERB, true);
   public static final PartOfSpeech DNP = PartOfSpeech.create("DNP", "DNP", PartOfSpeech.ADVERB, true);
   public static final PartOfSpeech DVP = PartOfSpeech.create("DVP", "DVP", PartOfSpeech.ADVERB, true);
   public static final PartOfSpeech DP = PartOfSpeech.create("DP", "DP", PartOfSpeech.ADVERB, true);
   public static final PartOfSpeech VRD = PartOfSpeech.create("VRD", "VRD", PartOfSpeech.ADVERB, true);
   public static final PartOfSpeech INF = PartOfSpeech.create("INF", "INF", PartOfSpeech.ADVERB, true);
   public static final PartOfSpeech VSB = PartOfSpeech.create("VSB", "VSB", PartOfSpeech.ADVERB, true);
   public static final PartOfSpeech NT_SHORT = PartOfSpeech.create("NT-SHORT", "NT-SHORT", PartOfSpeech.ADVERB, true);
   public static final PartOfSpeech VNV = PartOfSpeech.create("VNV", "VNV", PartOfSpeech.ADVERB, true);
   public static final PartOfSpeech VPT = PartOfSpeech.create("VPT", "VPT", PartOfSpeech.ADVERB, true);
   public static final PartOfSpeech VCP = PartOfSpeech.create("VCP", "VCP", PartOfSpeech.ADVERB, true);
   public static final PartOfSpeech NN_SHORT = PartOfSpeech.create("NN-SHORT", "NN-SHORT", PartOfSpeech.ADVERB, true);
   public static final PartOfSpeech NR_SHORT = PartOfSpeech.create("NR-SHORT", "NR-SHORT", PartOfSpeech.ADVERB, true);
   public static final PartOfSpeech PRD = PartOfSpeech.create("PRD", "PRD", PartOfSpeech.PRONOUN, false);
   public static final PartOfSpeech SFN = PartOfSpeech.create("SFN", "SFN", PartOfSpeech.PARTICLE, false);
   public static final PartOfSpeech SFV = PartOfSpeech.create("SFV", "SFV", PartOfSpeech.PARTICLE, false);
   public static final PartOfSpeech SFA = PartOfSpeech.create("SFA", "SFA", PartOfSpeech.PARTICLE, false);
   public static final PartOfSpeech PFA = PartOfSpeech.create("PFA", "PFA", PartOfSpeech.PARTICLE, false);
   public static final PartOfSpeech PFN = PartOfSpeech.create("PFN", "PFN", PartOfSpeech.PARTICLE, false);
   public static final PartOfSpeech NNB = PartOfSpeech.create("NNB", "NNB", PartOfSpeech.NOUN, false);
   public static final PartOfSpeech BB = PartOfSpeech.create("BB", "BB", PartOfSpeech.VERB, false);
   public static final PartOfSpeech EC = PartOfSpeech.create("EC", "EC", PartOfSpeech.PUNCTUATION, false);
//   public static final PartOfSpeech CP = PartOfSpeech.create("CP", "CP", PartOfSpeech.ADVERB, true);
//   public static final PartOfSpeech CLP = PartOfSpeech.create("CLP", "CLP", PartOfSpeech.ADVERB, true);
//   public static final PartOfSpeech DNP = PartOfSpeech.create("DNP", "DNP", PartOfSpeech.ADVERB, true);
//   public static final PartOfSpeech DVP = PartOfSpeech.create("DVP", "DVP", PartOfSpeech.ADVERB, true);
//   public static final PartOfSpeech DP = PartOfSpeech.create("DP", "DP", PartOfSpeech.ADVERB, true);
//   public static final PartOfSpeech VRD = PartOfSpeech.create("VRD", "VRD", PartOfSpeech.ADVERB, true);
//   public static final PartOfSpeech VSB = PartOfSpeech.create("VSB", "VSB", PartOfSpeech.ADVERB, true);
//   public static final PartOfSpeech NT_SHORT = PartOfSpeech.create("NT-SHORT", "NT-SHORT", PartOfSpeech.ADVERB, true);
//   public static final PartOfSpeech VNV = PartOfSpeech.create("VNV", "VNV", PartOfSpeech.ADVERB, true);

   //Adverb
   public static final PartOfSpeech AD = PartOfSpeech.create("AD", "AD", PartOfSpeech.ADVERB, false);
   //Aspect Marker
   public static final PartOfSpeech AS = PartOfSpeech.create("AS", "AS", PartOfSpeech.PARTICLE, false);
   //Ba-Construction
   public static final PartOfSpeech BA = PartOfSpeech.create("BA", "BA", PartOfSpeech.OTHER, false);
   //CC & CD in PennTreeBank
   //Subordinating Conjunction
   public static final PartOfSpeech CS = PartOfSpeech.create("CS", "CS", PartOfSpeech.SCONJ, false);
   //relative clause marker
   public static final PartOfSpeech DEC = PartOfSpeech.create("DEC", "DEC", PartOfSpeech.PARTICLE, false);
   //associative
   public static final PartOfSpeech DEG = PartOfSpeech.create("DEG", "DEG", PartOfSpeech.PARTICLE, false);
   //in V-de const and V-de-R
   public static final PartOfSpeech DER = PartOfSpeech.create("DER", "DER", PartOfSpeech.PARTICLE, false);
   //地 before VP
   public static final PartOfSpeech DEV = PartOfSpeech.create("DEV", "DEV", PartOfSpeech.PARTICLE, false);
   //DT in PennTreeBank
   //or words 等, 等等
   public static final PartOfSpeech ETC = PartOfSpeech.create("ETC", "ETC", PartOfSpeech.PARTICLE, false);
   //FW in PennTreeBank
   //Interjection
   public static final PartOfSpeech IJ = PartOfSpeech.create("IJ", "IJ", PartOfSpeech.INTERJECTION, false);
   //被 in long bei-const
   public static final PartOfSpeech LB = PartOfSpeech.create("LB", "LB", PartOfSpeech.OTHER, false);
   //Localizer
   public static final PartOfSpeech LC = PartOfSpeech.create("LC", "LC", PartOfSpeech.PARTICLE, false);
   //Measure word
   public static final PartOfSpeech M = PartOfSpeech.create("M", "M", PartOfSpeech.NUMERAL, false);
   //other particle
   public static final PartOfSpeech MSP = PartOfSpeech.create("MSP", "MSP", PartOfSpeech.PARTICLE, false);
   //NN in PenTreeBank
   //Proper Noun
   public static final PartOfSpeech NR = PartOfSpeech.create("NR", "NR", PartOfSpeech.PROPER_NOUN, false);
   //Temporal Noun
   public static final PartOfSpeech NT = PartOfSpeech.create("NT", "NT", PartOfSpeech.NOUN, false);
   //Ordinal Number
   public static final PartOfSpeech OD = PartOfSpeech.create("OD", "OD", PartOfSpeech.NUMERAL, false);
   //Onomatopoeia
   public static final PartOfSpeech ON = PartOfSpeech.create("ON", "ON", PartOfSpeech.OTHER, false);
   //Prepositions
   public static final PartOfSpeech P = PartOfSpeech.create("P", "P", PartOfSpeech.ADPOSITION, false);
   //Pronoun
   public static final PartOfSpeech PN = PartOfSpeech.create("PN", "PN", PartOfSpeech.PRONOUN, false);
   //Punctuation
   public static final PartOfSpeech PU = PartOfSpeech.create("PU", "PU", PartOfSpeech.PUNCTUATION, false);
   //被 in short bei-const
   public static final PartOfSpeech SB = PartOfSpeech.create("SB", "SB", PartOfSpeech.OTHER, false);
   //Sentence final particle
   public static final PartOfSpeech SP = PartOfSpeech.create("SP", "SP", PartOfSpeech.PARTICLE, false);
   //Predicative adjective
   public static final PartOfSpeech VA = PartOfSpeech.create("VA", "VA", PartOfSpeech.VERB, false);
   //copula
   public static final PartOfSpeech VC = PartOfSpeech.create("VC", "VC", PartOfSpeech.VERB, false);
   //有 as the main verb
   public static final PartOfSpeech VE = PartOfSpeech.create("VE", "VE", PartOfSpeech.VERB, false);
   //Other verbs
   public static final PartOfSpeech VV = PartOfSpeech.create("VV", "VV", PartOfSpeech.VERB, false);


}
