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

package com.gengoai.hermes.ja;

import com.gengoai.annotation.Preload;
import com.gengoai.hermes.morphology.PartOfSpeech;

@Preload
public class JAPos {

    public static final PartOfSpeech OTHER = PartOfSpeech.create("その他", "その他", PartOfSpeech.OTHER, false);
    public static final PartOfSpeech OTHER_INTERJECTION = PartOfSpeech.create("その他間投", "その他間投", PartOfSpeech.INTERJECTION, false);
    public static final PartOfSpeech FILLER = PartOfSpeech.create("フィラー", "フィラー", PartOfSpeech.OTHER, false);
    public static final PartOfSpeech ADVERB = PartOfSpeech.create("副詞", "副詞", PartOfSpeech.ADVERB, false);
    public static final PartOfSpeech ADVERB_MISC = PartOfSpeech.create("副詞一般", "副詞一般", PartOfSpeech.ADVERB, false);
    public static final PartOfSpeech ADVERB_PARTICLE_CONJUNCTION = PartOfSpeech.create("副詞助詞類接続", "副詞助詞類接続", PartOfSpeech.ADVERB, false);
    public static final PartOfSpeech AUX_VERB = PartOfSpeech.create("助動詞", "助動詞", PartOfSpeech.AUXILIARY, false);
    public static final PartOfSpeech PARTICLE = PartOfSpeech.create("助詞", "助詞", PartOfSpeech.ADPOSITION, false);
    public static final PartOfSpeech PARTICLE_COORDINATE = PartOfSpeech.create("助詞並立助詞", "助詞並立助詞", PartOfSpeech.CCONJ, false);
    public static final PartOfSpeech PARTICLE_DEPENDENCY = PartOfSpeech.create("助詞係助詞", "助詞係助詞", PartOfSpeech.ADPOSITION, false);
    public static final PartOfSpeech PARTICLE_ADVERBIAL = PartOfSpeech.create("助詞副助詞", "助詞副助詞", PartOfSpeech.ADPOSITION, false);
    public static final PartOfSpeech PARTICLE_ADVERBIAL_CONJUNCTIVE_FINAL = PartOfSpeech.create("助詞副助詞／並立助詞／終助詞", "助詞副助詞／並立助詞／終助詞", PartOfSpeech.PARTICLE, false);
    public static final PartOfSpeech PARTICLE_ADOMINALIZER_1 = PartOfSpeech.create("助詞連体化", "助詞連体化", PartOfSpeech.ADPOSITION, false);
    public static final PartOfSpeech PARTICLE_ADOMINALIZER_2 = PartOfSpeech.create("助詞副詞化", "助詞副詞化", PartOfSpeech.ADPOSITION, false);
    public static final PartOfSpeech PARTICLE_CASE = PartOfSpeech.create("助詞格助詞", "助詞格助詞", PartOfSpeech.ADPOSITION, false);
    public static final PartOfSpeech PARTICLE_CONJUNCTIVE = PartOfSpeech.create("助詞接続助詞", "助詞接続助詞", PartOfSpeech.ADPOSITION, false);
    public static final PartOfSpeech PARTICLE_SPECIAL = PartOfSpeech.create("助詞特殊", "助詞特殊", PartOfSpeech.ADPOSITION, false);
    public static final PartOfSpeech PARTICLE_FINAL = PartOfSpeech.create("助詞終助詞", "助詞終助詞", PartOfSpeech.ADPOSITION, false);
    public static final PartOfSpeech PARTICLE_INTERJECTIVE = PartOfSpeech.create("助詞間投助詞", "助詞間投助詞", PartOfSpeech.ADPOSITION, false);

    public static final PartOfSpeech VERB = PartOfSpeech.create("動詞", "動詞", PartOfSpeech.VERB, false);

    public static final PartOfSpeech VERB_SUFFIX = PartOfSpeech.create("動詞接尾", "動詞接尾", PartOfSpeech.VERB, false);
    public static final PartOfSpeech VERB_MAIN = PartOfSpeech.create("動詞自立", "動詞自立", PartOfSpeech.VERB, false);

    public static final PartOfSpeech VERB_AUX = PartOfSpeech.create("動詞非自立", "動詞非自立", PartOfSpeech.AUXILIARY, false);
    public static final PartOfSpeech NOUN = PartOfSpeech.create("名詞", "名詞", PartOfSpeech.NOUN, false);
    public static final PartOfSpeech ADJECTIVAL_NOUN = PartOfSpeech.create("名詞サ変接続", "名詞サ変接続", PartOfSpeech.NOUN, false);
    public static final PartOfSpeech NAI_ADJECTIVAL_NOUN = PartOfSpeech.create("名詞ナイ形容詞語幹", "名詞ナイ形容詞語幹", PartOfSpeech.NOUN, false);
    public static final PartOfSpeech COMMON_NOUN = PartOfSpeech.create("名詞一般", "名詞一般", PartOfSpeech.NOUN, false);
    public static final PartOfSpeech PRONOUN = PartOfSpeech.create("名詞代名詞", "名詞代名詞", PartOfSpeech.PRONOUN, false);
    public static final PartOfSpeech ADVERBIAL_NOUN = PartOfSpeech.create("名詞副詞可能", "名詞副詞可能", PartOfSpeech.NOUN, false);
    public static final PartOfSpeech VERBAL_AUX_NOUN = PartOfSpeech.create("名詞動詞非自立的", "名詞動詞非自立的", PartOfSpeech.NOUN, false);
    public static final PartOfSpeech VERBAL_NOUN = PartOfSpeech.create("名詞サ変接続", "名詞サ変接続", PartOfSpeech.NOUN, false);
    public static final PartOfSpeech PROPER_NOUN = PartOfSpeech.create("名詞固有名詞", "名詞固有名詞", PartOfSpeech.PROPER_NOUN, false);
    public static final PartOfSpeech NOUN_QUOTE = PartOfSpeech.create("名詞引用文字列", "名詞引用文字列", PartOfSpeech.NOUN, false);
    public static final PartOfSpeech NOUN_ADJECTIVE_BASE = PartOfSpeech.create("名詞形容動詞語幹", "名詞形容動詞語幹", PartOfSpeech.NOUN, false);
    public static final PartOfSpeech NOUN_AFFIX = PartOfSpeech.create("名詞非自立", "名詞非自立", PartOfSpeech.NOUN, false);
    public static final PartOfSpeech NOUN_SUFFIX = PartOfSpeech.create("名詞接尾", "名詞接尾", PartOfSpeech.NOUN, false);
    public static final PartOfSpeech NUMBER = PartOfSpeech.create("名詞数", "名詞数", PartOfSpeech.NUMERAL, false);
    public static final PartOfSpeech NOUN_SPECIAL_AUX = PartOfSpeech.create("名詞特殊", "名詞特殊", PartOfSpeech.NOUN, false);
    public static final PartOfSpeech NOUN_SUFFIX_CONJUNCTIVE = PartOfSpeech.create("名詞接続詞的", "名詞接続詞的", PartOfSpeech.CCONJ, false);
    public static final PartOfSpeech ADJECTIVE = PartOfSpeech.create("形容詞", "形容詞", PartOfSpeech.ADJECTIVE, false);
    public static final PartOfSpeech ADJECTIVE_SUFFIX = PartOfSpeech.create("形容詞接尾", "形容詞接尾", PartOfSpeech.ADJECTIVE, false);
    public static final PartOfSpeech ADJECTIVE_MAIN = PartOfSpeech.create("形容詞自立", "形容詞自立", PartOfSpeech.ADJECTIVE, false);
    public static final PartOfSpeech ADJECTIVE_SUB = PartOfSpeech.create("形容詞非自立", "形容詞非自立", PartOfSpeech.ADJECTIVE, false);
    public static final PartOfSpeech EXCLAMATION = PartOfSpeech.create("感動詞", "感動詞", PartOfSpeech.INTERJECTION, false);
    public static final PartOfSpeech CONJUNCTION = PartOfSpeech.create("接続詞", "接続詞", PartOfSpeech.CCONJ, false);

    public static final PartOfSpeech PREFIX = PartOfSpeech.create("接頭詞", "接頭詞", PartOfSpeech.OTHER, false);
    public static final PartOfSpeech PREFIX_VERBAL = PartOfSpeech.create("接頭詞動詞接続", "接頭詞動詞接続", PartOfSpeech.VERB, false);
    public static final PartOfSpeech PREFIX_NOMINAL = PartOfSpeech.create("接頭詞名詞接続 ", "接頭詞名詞接続", PartOfSpeech.NOUN, false);
    public static final PartOfSpeech PREFIX_ADJECTIVAL = PartOfSpeech.create("接頭詞形容詞接続", "接頭詞形容詞接続", PartOfSpeech.ADJECTIVE, false);
    public static final PartOfSpeech PREFIX_NUMERICAL = PartOfSpeech.create("接頭詞数接続", "接頭詞数接続", PartOfSpeech.NUMERAL, false);
    public static final PartOfSpeech SYMBOL = PartOfSpeech.create("記号", "記号", PartOfSpeech.SYMBOL, false);
    public static final PartOfSpeech SYMBOL_ALPHABET = PartOfSpeech.create("記号アルファベット", "記号アルファベット", PartOfSpeech.SYMBOL, false);
    public static final PartOfSpeech SYMBOL_MISC = PartOfSpeech.create("記号一般", "記号一般", PartOfSpeech.SYMBOL, false);
    public static final PartOfSpeech SYMBOL_PERIOD = PartOfSpeech.create("記号句点", "記号句点", PartOfSpeech.PUNCTUATION, false);
    public static final PartOfSpeech SYMBOL_OPEN_BRACKET = PartOfSpeech.create("記号括弧開", "記号括弧開", PartOfSpeech.SYMBOL, false);
    public static final PartOfSpeech SYMBOL_CLOSE_BRACKET = PartOfSpeech.create("記号括弧閉", "記号括弧閉", PartOfSpeech.SYMBOL, false);
    public static final PartOfSpeech SYMBOL_COMMA = PartOfSpeech.create("記号読点", "記号読点", PartOfSpeech.PUNCTUATION, false);
    public static final PartOfSpeech SYMBOL_SPACE = PartOfSpeech.create("記号空白", "記号空白", PartOfSpeech.PUNCTUATION, false);

    public static final PartOfSpeech ADNOMINAL = PartOfSpeech.create("連体詞", "連体詞", PartOfSpeech.ADJECTIVE, false);

    public static final PartOfSpeech NON_VERBAL_SOUND = PartOfSpeech.create("非言語音", "非言語音", PartOfSpeech.OTHER, false);
    public static final PartOfSpeech WORD_FRAGMENT = PartOfSpeech.create("語断片", "語断片", PartOfSpeech.OTHER, false);

}
