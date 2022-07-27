/* The following code was generated by JFlex 1.6.1 */

package com.gengoai.config;

import com.gengoai.parsing.ParseException;
import com.gengoai.parsing.ParserToken;



/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.6.1
 * from the specification file <tt>/Users/ik/prj/gengoai/mono-repo/commons/core/src/main/jflex/ConfigScanner.flex</tt>
 */
public class ConfigScanner {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0, 0
  };

  /** 
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED = 
    "\11\0\1\43\1\6\1\44\1\43\1\6\22\0\1\43\1\0\1\2"+
    "\1\5\7\0\1\13\1\34\1\7\1\4\1\0\1\10\11\11\1\41"+
    "\2\0\1\42\2\0\1\14\4\3\1\12\25\3\1\35\1\1\1\36"+
    "\1\0\1\3\1\0\1\32\1\3\1\21\1\3\1\20\1\31\2\3"+
    "\1\27\2\3\1\33\1\30\1\23\1\22\1\17\1\3\1\25\1\24"+
    "\1\15\1\26\3\3\1\16\1\3\1\37\1\0\1\40\7\0\1\44"+
    "\u1fa2\0\1\44\1\44\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\udfe6\0";

  /** 
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\1\0\2\1\1\2\1\3\1\4\1\1\2\5\2\1"+
    "\3\2\1\6\1\7\1\10\1\11\1\12\1\13\1\14"+
    "\2\0\1\15\2\0\1\16\4\0\3\2\1\15\1\5"+
    "\1\0\1\5\4\0\3\2\3\0\1\17\1\20\1\21"+
    "\1\22\5\0\1\23\4\0\1\2";

  private static int [] zzUnpackAction() {
    int [] result = new int[63];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\45\0\112\0\157\0\224\0\45\0\271\0\336"+
    "\0\u0103\0\u0128\0\u014d\0\u0172\0\u0197\0\u01bc\0\45\0\45"+
    "\0\45\0\45\0\45\0\45\0\45\0\112\0\u01e1\0\45"+
    "\0\u0206\0\u022b\0\45\0\u0250\0\u0275\0\u029a\0\u02bf\0\u02e4"+
    "\0\u0309\0\u032e\0\112\0\u0353\0\u0378\0\u0378\0\u039d\0\u03c2"+
    "\0\u03e7\0\u040c\0\u0431\0\u0456\0\u047b\0\u04a0\0\u04c5\0\u04ea"+
    "\0\45\0\157\0\157\0\45\0\u050f\0\u0534\0\u0559\0\u057e"+
    "\0\u05a3\0\45\0\u05c8\0\u05ed\0\u0612\0\u0637\0\45";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[63];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\2\2\1\3\1\4\1\2\1\5\1\6\1\7\1\10"+
    "\1\11\1\4\1\12\1\13\1\14\5\4\1\15\5\4"+
    "\1\16\2\4\1\17\1\20\1\21\1\22\1\23\1\24"+
    "\1\25\1\6\46\0\1\26\1\27\1\30\42\26\3\0"+
    "\2\4\3\0\3\4\2\0\17\4\11\0\6\5\1\0"+
    "\36\5\10\0\1\10\1\11\37\0\1\31\5\0\1\32"+
    "\5\0\1\32\30\0\1\31\3\0\2\11\1\32\5\0"+
    "\1\32\66\0\1\33\17\0\1\34\3\0\1\35\5\0"+
    "\1\36\7\0\1\37\10\0\2\4\3\0\3\4\2\0"+
    "\10\4\1\40\6\4\14\0\2\4\3\0\3\4\2\0"+
    "\11\4\1\41\5\4\14\0\2\4\3\0\3\4\2\0"+
    "\15\4\1\42\1\4\11\0\1\26\1\27\1\43\42\26"+
    "\10\0\2\44\42\0\1\45\2\46\1\0\1\45\47\0"+
    "\1\47\50\0\1\50\52\0\1\51\17\0\1\52\6\0"+
    "\1\52\2\0\17\52\14\0\2\4\3\0\3\4\2\0"+
    "\11\4\1\53\5\4\14\0\2\4\3\0\3\4\2\0"+
    "\16\4\1\54\14\0\2\4\3\0\3\4\2\0\16\4"+
    "\1\55\21\0\2\44\1\32\5\0\1\32\34\0\2\46"+
    "\52\0\1\56\50\0\1\57\40\0\1\60\30\0\2\52"+
    "\3\0\3\52\2\0\17\52\4\0\1\61\7\0\2\4"+
    "\3\0\3\4\2\0\3\4\1\62\13\4\14\0\2\4"+
    "\3\0\3\4\2\0\16\4\1\63\14\0\2\4\3\0"+
    "\3\4\2\0\7\4\1\53\7\4\31\0\1\64\50\0"+
    "\1\65\42\0\1\66\37\0\1\67\54\0\1\70\44\0"+
    "\1\71\34\0\1\72\55\0\1\73\37\0\1\74\40\0"+
    "\1\75\51\0\1\76\47\0\1\77\17\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[1628];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unknown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\1\0\1\11\3\1\1\11\10\1\7\11\2\0\1\11"+
    "\2\0\1\11\4\0\5\1\1\0\1\1\4\0\3\1"+
    "\3\0\1\11\2\1\1\11\5\0\1\11\4\0\1\11";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[63];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private char zzBuffer[] = new char[ZZ_BUFFERSIZE];

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /** number of newlines encountered up to the start of the matched text */
  private int yyline;

  /** the number of characters up to the start of the matched text */
  private int yychar;

  /**
   * the number of characters from the last newline up to the start of the 
   * matched text
   */
  private int yycolumn;

  /** 
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** denotes if the user-EOF-code has already been executed */
  private boolean zzEOFDone;
  
  /** 
   * The number of occupied positions in zzBuffer beyond zzEndRead.
   * When a lead/high surrogate has been read from the input stream
   * into the final zzBuffer position, this will have a value of 1;
   * otherwise, it will have a value of 0.
   */
  private int zzFinalHighSurrogate = 0;


  /**
   * Creates a new scanner
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public ConfigScanner(java.io.Reader in) {
    this.zzReader = in;
  }


  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    char [] map = new char[0x110000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 160) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }


  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   * 
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {

    /* first: make room (if you can) */
    if (zzStartRead > 0) {
      zzEndRead += zzFinalHighSurrogate;
      zzFinalHighSurrogate = 0;
      System.arraycopy(zzBuffer, zzStartRead,
                       zzBuffer, 0,
                       zzEndRead-zzStartRead);

      /* translate stored positions */
      zzEndRead-= zzStartRead;
      zzCurrentPos-= zzStartRead;
      zzMarkedPos-= zzStartRead;
      zzStartRead = 0;
    }

    /* is the buffer big enough? */
    if (zzCurrentPos >= zzBuffer.length - zzFinalHighSurrogate) {
      /* if not: blow it up */
      char newBuffer[] = new char[zzBuffer.length*2];
      System.arraycopy(zzBuffer, 0, newBuffer, 0, zzBuffer.length);
      zzBuffer = newBuffer;
      zzEndRead += zzFinalHighSurrogate;
      zzFinalHighSurrogate = 0;
    }

    /* fill the buffer with new input */
    int requested = zzBuffer.length - zzEndRead;
    int numRead = zzReader.read(zzBuffer, zzEndRead, requested);

    /* not supposed to occur according to specification of java.io.Reader */
    if (numRead == 0) {
      throw new java.io.IOException("Reader returned 0 characters. See JFlex examples for workaround.");
    }
    if (numRead > 0) {
      zzEndRead += numRead;
      /* If numRead == requested, we might have requested to few chars to
         encode a full Unicode character. We assume that a Reader would
         otherwise never return half characters. */
      if (numRead == requested) {
        if (Character.isHighSurrogate(zzBuffer[zzEndRead - 1])) {
          --zzEndRead;
          zzFinalHighSurrogate = 1;
        }
      }
      /* potentially more input available */
      return false;
    }

    /* numRead < 0 ==> end of stream */
    return true;
  }

    
  /**
   * Closes the input stream.
   */
  public final void yyclose() throws java.io.IOException {
    zzAtEOF = true;            /* indicate end of file */
    zzEndRead = zzStartRead;  /* invalidate buffer    */

    if (zzReader != null)
      zzReader.close();
  }


  /**
   * Resets the scanner to read from a new input stream.
   * Does not close the old reader.
   *
   * All internal variables are reset, the old input stream 
   * <b>cannot</b> be reused (internal buffer is discarded and lost).
   * Lexical state is set to <tt>ZZ_INITIAL</tt>.
   *
   * Internal scan buffer is resized down to its initial length, if it has grown.
   *
   * @param reader   the new input stream 
   */
  public final void yyreset(java.io.Reader reader) {
    zzReader = reader;
    zzAtBOL  = true;
    zzAtEOF  = false;
    zzEOFDone = false;
    zzEndRead = zzStartRead = 0;
    zzCurrentPos = zzMarkedPos = 0;
    zzFinalHighSurrogate = 0;
    yyline = yychar = yycolumn = 0;
    zzLexicalState = YYINITIAL;
    if (zzBuffer.length > ZZ_BUFFERSIZE)
      zzBuffer = new char[ZZ_BUFFERSIZE];
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final String yytext() {
    return new String( zzBuffer, zzStartRead, zzMarkedPos-zzStartRead );
  }


  /**
   * Returns the character at position <tt>pos</tt> from the 
   * matched text. 
   * 
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch. 
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer[zzStartRead+pos];
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of 
   * yypushback(int) and a match-all fallback rule) this method 
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  } 


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public com.gengoai.parsing.ParserToken next() throws java.io.IOException,     com.gengoai.parsing.ParseException
 {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char [] zzBufferL = zzBuffer;
    char [] zzCMapL = ZZ_CMAP;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      yychar+= zzMarkedPosL-zzStartRead;

      boolean zzR = false;
      int zzCh;
      int zzCharCount;
      for (zzCurrentPosL = zzStartRead  ;
           zzCurrentPosL < zzMarkedPosL ;
           zzCurrentPosL += zzCharCount ) {
        zzCh = Character.codePointAt(zzBufferL, zzCurrentPosL, zzMarkedPosL);
        zzCharCount = Character.charCount(zzCh);
        switch (zzCh) {
        case '\u000B':
        case '\u000C':
        case '\u0085':
        case '\u2028':
        case '\u2029':
          yyline++;
          zzR = false;
          break;
        case '\r':
          yyline++;
          zzR = true;
          break;
        case '\n':
          if (zzR)
            zzR = false;
          else {
            yyline++;
          }
          break;
        default:
          zzR = false;
        }
      }

      if (zzR) {
        // peek one character ahead if it is \n (if we have counted one line too much)
        boolean zzPeek;
        if (zzMarkedPosL < zzEndReadL)
          zzPeek = zzBufferL[zzMarkedPosL] == '\n';
        else if (zzAtEOF)
          zzPeek = false;
        else {
          boolean eof = zzRefill();
          zzEndReadL = zzEndRead;
          zzMarkedPosL = zzMarkedPos;
          zzBufferL = zzBuffer;
          if (eof) 
            zzPeek = false;
          else 
            zzPeek = zzBufferL[zzMarkedPosL] == '\n';
        }
        if (zzPeek) yyline--;
      }
      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;
  
      zzState = ZZ_LEXSTATE[zzLexicalState];

      // set up zzAction for empty match case:
      int zzAttributes = zzAttrL[zzState];
      if ( (zzAttributes & 1) == 1 ) {
        zzAction = zzState;
      }


      zzForAction: {
        while (true) {
    
          if (zzCurrentPosL < zzEndReadL) {
            zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL, zzEndReadL);
            zzCurrentPosL += Character.charCount(zzInput);
          }
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL, zzEndReadL);
              zzCurrentPosL += Character.charCount(zzInput);
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMapL[zzInput] ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
        zzAtEOF = true;
        return null;
      }
      else {
        switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
          case 1: 
            { throw new ParseException("Illegal character: "+yytext()+"\" at line: " + yyline + " char offset: " + yychar + " state: " + yystate());
            }
          case 20: break;
          case 2: 
            { return new ParserToken(ConfigTokenType.KEY,yytext(), yychar);
            }
          case 21: break;
          case 3: 
            { return new ParserToken(ConfigTokenType.COMMENT,yytext(), yychar);
            }
          case 22: break;
          case 4: 
            { /* ignore white space. */
            }
          case 23: break;
          case 5: 
            { return new ParserToken(ConfigTokenType.STRING,yytext(), yychar);
            }
          case 24: break;
          case 6: 
            { return new ParserToken(ConfigTokenType.VALUE_SEPARATOR,yytext(), yychar);
            }
          case 25: break;
          case 7: 
            { return new ParserToken(ConfigTokenType.BEGIN_ARRAY,yytext(), yychar);
            }
          case 26: break;
          case 8: 
            { return new ParserToken(ConfigTokenType.END_ARRAY,yytext(), yychar);
            }
          case 27: break;
          case 9: 
            { return new ParserToken(ConfigTokenType.BEGIN_OBJECT,yytext(), yychar);
            }
          case 28: break;
          case 10: 
            { return new ParserToken(ConfigTokenType.END_OBJECT,yytext(), yychar);
            }
          case 29: break;
          case 11: 
            { return new ParserToken(ConfigTokenType.KEY_VALUE_SEPARATOR,yytext(), yychar);
            }
          case 30: break;
          case 12: 
            { return new ParserToken(ConfigTokenType.EQUAL_PROPERTY,yytext(), yychar);
            }
          case 31: break;
          case 13: 
            { return new ParserToken(ConfigTokenType.STRING,yytext().substring(1,yytext().length()-1),yychar);
            }
          case 32: break;
          case 14: 
            { return new ParserToken(ConfigTokenType.APPEND_PROPERTY,yytext(), yychar);
            }
          case 33: break;
          case 15: 
            { return new ParserToken(ConfigTokenType.BEAN,yytext(), yychar);
            }
          case 34: break;
          case 16: 
            { return  new ParserToken(ConfigTokenType.BOOLEAN,yytext(), yychar);
            }
          case 35: break;
          case 17: 
            { return new ParserToken(ConfigTokenType.NULL,null,yychar);
            }
          case 36: break;
          case 18: 
            { return new ParserToken(ConfigTokenType.KEY, yytext(),yychar);
            }
          case 37: break;
          case 19: 
            { return new ParserToken(ConfigTokenType.IMPORT,yytext(), yychar);
            }
          case 38: break;
          default:
            zzScanError(ZZ_NO_MATCH);
        }
      }
    }
  }


}
